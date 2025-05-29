package com.example.kkubeok.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.kkubeok.ui.theme.KkubeokTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

import com.example.kkubeok.BottomNavigationBar
import com.example.kkubeok.database.DatabaseProvider
import com.example.kkubeok.database.Detected

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import org.json.JSONObject
import java.util.Date
import kotlin.math.pow
import kotlin.math.sqrt

import libsvm.*
import java.io.FileOutputStream

@Composable
fun DetectingScreen(navController: NavHostController?=null){
    val context = LocalContext.current
    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gravity = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }
    val gyroscope = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    var accelVal by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var gravityVal by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var gyroVal by remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    /* For Data Storage */
    var accelData=remember {mutableStateListOf<String>()}
    var gravityData=remember {mutableStateListOf<String>()}
    var gyroData=remember{mutableStateListOf<String>()}

    var totalTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var startTimestamp by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    /* Connecting with DataBase */
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("user_id",null)
    val db = remember { DatabaseProvider.getDatabase(context) }
    val detectedDao = db.detectedDao()

    val listener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {evt->
                    val (x, y, z) = evt.values
                    val timestamp=System.currentTimeMillis()
                    val timeString=SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                    val line="$timeString,$timestamp,$x,$y,$z"

                    when(evt.sensor.type){
                        Sensor.TYPE_ACCELEROMETER->{
                            accelVal=Triple(x,y,z)
                            accelData.add(line)
                        }
                        Sensor.TYPE_GRAVITY->{
                            gravityVal=Triple(x,y,z)
                            gravityData.add(line)
                        }
                        Sensor.TYPE_GYROSCOPE->{
                            gyroVal=Triple(x,y,z)
                            gyroData.add(line)
                        }
                        else ->{Log.d("SensorEvent", "Unhandled sensor type:${evt.sensor.type}")}
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    Scaffold(
        bottomBar={
            navController?.let{
                BottomNavigationBar(navController=it)
            }
        }
    ){ paddingValues->
        Column(
            modifier=Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier=Modifier.height(16.dp))

            // Header
            Text(
                text="Detecting",
                style=MaterialTheme.typography.headlineSmall,
                fontWeight= FontWeight.Bold,
                modifier=Modifier.align(Alignment.Start)
            )

            // Day Block
            val todayDate by produceState(initialValue = getFormattedToday()) {
                while (true) {
                    value = getFormattedToday()
                    delay(60 * 1000L)
                }
            }
            Text(
                text = todayDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize=18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            // Time Block
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ){
                // Real World Current Time
                Card(
                    modifier=Modifier
                        .weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Time", fontWeight = FontWeight.Medium, fontSize=15.sp, maxLines=1)
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentClock by produceState(initialValue=getCurrentHourMinute()){
                            while(true){
                                value=getCurrentHourMinute()
                                delay(60*1000L)
                            }
                        }
                        Text(currentClock, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Detecting Time Block
                Card(
                    modifier=Modifier
                        .weight(1f)
                        .padding(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Detecting Time", fontWeight = FontWeight.Medium, fontSize=15.sp, maxLines=1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatSeconds(totalTime+currentTime), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier=Modifier.height(8.dp))

            // Sensor Values
            SensorTable(accelVal, gravityVal, gyroVal)

            Spacer(modifier=Modifier.height(8.dp))

            // Start/Stop Button
            var isSensing by remember{mutableStateOf(false)}

            Button(
                onClick={
                    if(!isSensing){
                        // Start
                        sensorManager.registerListener(listener, accelerometer, 10000)
                        sensorManager.registerListener(listener, gravity, 10000)
                        sensorManager.registerListener(listener, gyroscope, 10000)
                        startTimestamp=System.currentTimeMillis()
                        timerJob=coroutineScope.launch{
                            while(true){
                                delay(1000L)
                                currentTime = (System.currentTimeMillis() - (startTimestamp ?: 0L)) / 1000
                            }
                        }
                    } else {
                        // Stop
                        sensorManager.unregisterListener(listener)
                        timerJob?.cancel()
                        totalTime+=currentTime
                        currentTime=0L
                        // Store Data at external csv file
                        val timestamp=startTimestamp?:System.currentTimeMillis()
                        saveDetectingCSV(context, userId!!, accelData, gravityData, gyroData, timestamp)
                        accelData.clear()
                        gravityData.clear()
                        gyroData.clear()
                    }
                    isSensing=!isSensing
                },
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors=ButtonDefaults.buttonColors(containerColor= Color.Black)
            )
            {
                Text(
                    if(isSensing) "Stop" else "Start",
                    color=Color.White)
            }

            Button(
                onClick = {
                    val predictions = analysisSensorCSV(context,userId!!)
                    /*val file = File(context.getExternalFilesDir(null),"predictions.csv")
                    file.printWriter().use { out ->
                        out.println("prediction") // 헤더
                        predictions.forEach { pred ->
                            out.println(pred)
                        }
                    }*/

                    val result = getLabelIntervals(predictions, setOf(0,1,2,3,4))

                    val currentTime = System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
                    val currentDate = Date(currentTime)
                    val calendar = dateFormat.format(currentDate)

                    val activityLabels = mapOf(
                        0 to "Lean Back",
                        1 to "Resting Head(Left)",
                        2 to "Resting Head(Right)",
                        3 to "Nodding Off",
                        4 to "Others"
                    )

                    var direction: String? = null
                    var action: String? = "Others"
                    val detectedList = result.mapNotNull { (label, start, end) ->
                        if (label == 4) return@mapNotNull null

                        direction = when (label) {
                            0 -> "Back"
                            1 -> "Left"
                            2 -> "Right"
                            else -> null
                        }

                        action = when(label){
                            0 -> "Nap"
                            1 -> "Nap"
                            2 -> "Nap"
                            3 -> "Dozing"
                            else -> ""
                        }

                        Detected(
                            user_id = userId,
                            calendar_date = calendar,
                            action_name = action,
                            start_time = start,
                            end_time = end,
                            direction = direction
                        )
                    }

                    CoroutineScope(Dispatchers.IO).launch{
                        detectedDao.insertAll(detectedList)
                    }
                    },

                modifier=Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors=ButtonDefaults.buttonColors(containerColor= Color.Black)
            ){
                Text(
                    "Analysis",
                    color = Color.White
                )
            }
        }
    }
}
fun getFormattedToday(): String{
    val calendar=Calendar.getInstance()
    val day=calendar.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    val weekday = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
    return "$day $month ($weekday)"
}
fun getCurrentHourMinute(): String {
    val calendar = Calendar.getInstance()
    val hour=calendar.get(Calendar.HOUR_OF_DAY)
    val minute=calendar.get(Calendar.MINUTE)
    return "%02dh %02dm".format(hour,minute)
}

@Composable
fun SensorTable(accel:Triple<Float,Float,Float>,gravity:Triple<Float,Float,Float>, gyro:Triple<Float,Float,Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("Accel", fontWeight=FontWeight.Bold)
                Text("Gravity", fontWeight=FontWeight.Bold)
                Text("Gyro", fontWeight=FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("X %.2f".format(accel.first))
                Text("X %.2f".format(gravity.first))
                Text("X %.2f".format(gyro.first))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Y %.2f".format(accel.second))
                Text("Y %.2f".format(gravity.second))
                Text("Y %.2f".format(gyro.second))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Z %.2f".format(accel.third))
                Text("Z %.2f".format(gravity.third))
                Text("Z %.2f".format(gyro.third))
            }
        }
    }
}

fun saveDetectingCSV(
    context: Context,
    userId: String,
    accelData: List<String>,
    gravityData: List<String>,
    gyroData: List<String>,
    startTimestamp: Long
){
    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(startTimestamp)
    val filesAndData=listOf(
        "${userId}_accel_${dateStr}.csv" to accelData,
        "${userId}_gravity_${dateStr}.csv" to gravityData,
        "${userId}_gyro_${dateStr}.csv" to gyroData
    )

    filesAndData.forEach{ (fileName, dataList) ->
        if(dataList.isEmpty()) return@forEach
        val file=File(context.getExternalFilesDir(null), fileName)
        val writer=BufferedWriter(FileWriter(file, true))
        writer.use{
            if(!file.exists()){
              it.write("time_string,timestamp,x,y,z\n")
            }
            dataList.forEach{line->it.write("$line\n")}
        }
    }
}

data class SensorTriple(val timestamp: Long, val x: Float, val y: Float, val z: Float)

fun loadNormalizationParams(context: Context, userId: String): Pair<List<Float>, List<Float>> {
    return try {
        val fileName = "${userId}_norm_params.json"
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        val xMinJsonArray = jsonObject.getJSONArray("X_min")
        val xMaxJsonArray = jsonObject.getJSONArray("X_max")

        val minList = List(xMinJsonArray.length()) { xMinJsonArray.getDouble(it).toFloat() }
        val maxList = List(xMaxJsonArray.length()) { xMaxJsonArray.getDouble(it).toFloat() }

        Pair(minList, maxList)
    } catch (e: Exception) {
        e.printStackTrace()
        throw RuntimeException("정규화 파라미터 로딩 중 오류 발생: ${e.message}")
    }
}

fun extractFeatures(
    gravity: List<SensorTriple>,
    gyro: List<SensorTriple>,
    linear: List<SensorTriple>
): List<Float> {
    fun List<SensorTriple>.meanStd(): List<Float> {
        val xs = this.map { it.x }
        val ys = this.map { it.y }
        val zs = this.map { it.z }

        fun mean(vs: List<Float>) = vs.average().toFloat()
        fun std(vs: List<Float>): Float {
            val mean = vs.average()
            return sqrt(vs.map { (it - mean).toDouble().pow(2) }.average()).toFloat()
        }

        val energy = this.sumOf { (it.x * it.x + it.y * it.y + it.z * it.z).toDouble() }.toFloat()

        return listOf(
            mean(xs), std(xs),
            mean(ys), std(ys),
            mean(zs), std(zs),
            energy
        )
    }

    return gravity.meanStd() + gyro.meanStd() + linear.meanStd()
}

fun normalizeFeatures(
    rawFeatures: List<Float>,
    xMin: List<Float>,
    xMax: List<Float>
): List<Float> {
    return rawFeatures.mapIndexed { index, value ->
        val min = xMin[index]
        val max = xMax[index]
        if ((max - min).toDouble() == 0.0) 0.0.toFloat() else (2 * (value - min) / (max - min + 1e-8) - 1).toFloat()
    }
}

fun predict(model: svm_model, features: List<Float>): Int {
    val nodes = features.mapIndexed { i, v ->
        svm_node().apply {
            index = i + 1
            value = v.toDouble()
        }
    }.toTypedArray()
    return svm.svm_predict(model, nodes).toInt()
}

fun loadSvmModelFromAssets(context: Context, userId: String): svm_model {
    val assetManager = context.assets
    val inputStream = assetManager.open(userId+"_activity_model.model")

    val modelFile = File(context.cacheDir, "temp_svm_model.model")
    inputStream.use { input ->
        FileOutputStream(modelFile).use { output ->
            input.copyTo(output)
        }
    }

    return svm.svm_load_model(modelFile.absolutePath)
}

fun readSensorCSV(context: Context, fileName: String): List<SensorTriple> {
    val file = File(context.getExternalFilesDir(null), fileName)
    val sensorList = mutableListOf<SensorTriple>()

    file.bufferedReader().useLines { lines ->
        lines.drop(1).forEach { line ->
            val tokens = line.split(",")
            if (tokens.size >= 5) {
                val timestamp = tokens[1].trim().toLongOrNull()
                val x = tokens[2].trim().toFloatOrNull()
                val y = tokens[3].trim().toFloatOrNull()
                val z = tokens[4].trim().toFloatOrNull()
                if (timestamp != null && x != null && y != null && z != null) {
                    sensorList.add(SensorTriple(timestamp, x, y, z))
                }
            }
        }
    }

    file.writeText("")
    file.writeText("time_string,timestamp,x,y,z\n")

    return sensorList
}


fun analysisSensorCSV(
    context: Context,
    userId: String
): List<Pair<Long, Int>>{
    val windowSize = 200
    val stride = 100

    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(System.currentTimeMillis())

    val gravity = readSensorCSV(context,"${userId}_gravity_${dateStr}.csv" )
    val gyro = readSensorCSV(context,"${userId}_gyro_${dateStr}.csv" )
    val linear = readSensorCSV(context,"${userId}_accel_${dateStr}.csv" )

    val (xMin, xMax) = loadNormalizationParams(context, userId)
    val model = loadSvmModelFromAssets(context, userId)

    val size = listOf(gravity.size, gyro.size, linear.size).minOrNull() ?: return emptyList()

    val predictions = mutableListOf<Pair<Long, Int>>()

    for (start in 0 until size - windowSize step stride) {
        val gWin = gravity.subList(start, start + windowSize)
        val rWin = gyro.subList(start, start + windowSize)
        val lWin = linear.subList(start, start + windowSize)

        val rawFeat = extractFeatures(gWin, rWin, lWin)
        val normFeat = normalizeFeatures(rawFeat, xMin, xMax)
        val pred = predict(model, normFeat)

        val time = gWin.first().timestamp
        predictions.add(time to pred)
    }

    return predictions
}

fun getLabelIntervals(predictions: List<Pair<Long, Int>>, targetLabels: Set<Int>): List<Triple<Int, Long, Long>> {
    val result = mutableListOf<Triple<Int, Long, Long>>()
    val maxGapMillis = 60000L

    var currentLabel: Int? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var prevTimestamp: Long? = null


    for ((timestamp, label) in predictions) {
        if (label in targetLabels) {
            if (label != currentLabel || (prevTimestamp != null && timestamp - prevTimestamp > maxGapMillis)) {
                // 이전 구간 저장
                if (currentLabel != null && startTime != null && endTime != null && endTime-startTime >= 5000) {
                    result.add(Triple(currentLabel, startTime, endTime))
                }
                // 새 구간 시작
                currentLabel = label
                startTime = timestamp
            }
            endTime = timestamp
            prevTimestamp = timestamp
        } else {
            if (currentLabel != null && startTime != null && endTime != null && endTime-startTime >= 5000) {
                result.add(Triple(currentLabel, startTime, endTime))
            }
            currentLabel = null
            startTime = null
            endTime = null
            prevTimestamp = null
        }
    }

    // 마지막 구간 저장
    if (currentLabel != null && startTime != null && endTime != null && endTime-startTime >= 5000) {
        result.add(Triple(currentLabel, startTime, endTime))
    }

    return result
}


// !-------------- detect direction ---------------!
data class Quaternion(val w: Double, val x: Double, val y: Double, val z: Double) {

    companion object {
        fun identity() = Quaternion(1.0, 0.0, 0.0, 0.0)
    }

    fun fromRotVec(v: Triple<Double, Double, Double>, dt: Float): Quaternion {
        val angle = Math.sqrt(v.first*v.first + v.second*v.second + v.third*v.third) * dt
        if (angle == 0.0) return identity()

        val axis = Triple(v.first, v.second, v.third)
        val norm = Math.sqrt(axis.first * axis.first + axis.second * axis.second + axis.third * axis.third)
        val ux = axis.first / norm
        val uy = axis.second / norm
        val uz = axis.third / norm

        val halfAngle = angle / 2.0
        val sinHalf = Math.sin(halfAngle)
        return Quaternion(
            Math.cos(halfAngle),
            ux * sinHalf,
            uy * sinHalf,
            uz * sinHalf
        )
    }

    operator fun times(q: Quaternion): Quaternion {
        val w1 = this.w
        val x1 = this.x
        val y1 = this.y
        val z1 = this.z
        val w2 = q.w
        val x2 = q.x
        val y2 = q.y
        val z2 = q.z

        return Quaternion(
            w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2,
            w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2,
            w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2,
            w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2
        )
    }

    fun rotate(v: Triple<Double, Double, Double>): Triple<Double, Double, Double> {
        val p = Quaternion(0.0, v.first, v.second, v.third)
        val qInv = this.conjugate()
        val rotated = this * p * qInv
        return Triple(rotated.x, rotated.y, rotated.z)
    }

    fun conjugate() = Quaternion(w, -x, -y, -z)
}

fun analysisDirectionCSV(
    context: Context,
    userId: String
){
    val gyroList = mutableListOf<DataPoint>()

    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(System.currentTimeMillis())
    val fileName = "${userId}_gyro_${dateStr}.csv"
    val file = File(context.getExternalFilesDir(null),fileName)

    var startTimestamp: Long? = null

    if(file.exists())
    {
        file.bufferedReader().useLines { lines ->
            lines.drop(1)
                .forEach{line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 4) {
                        val timestamp = tokens[0].trim().toLongOrNull()
                        val x = tokens[1].trim().toFloatOrNull()
                        val y = tokens[2].trim().toFloatOrNull()
                        val z = tokens[3].trim().toFloatOrNull()
                        if (timestamp != null && x != null && y != null && z != null) {
                            if (startTimestamp == null) {
                                startTimestamp = timestamp
                            }

                            // 시작 후 3초 지난 데이터만 추가
                            if (timestamp - startTimestamp!! >= 3000) {
                                gyroList.add(DataPoint(timestamp, x, y, z))
                            }                        }
                    }

                }

        }
    }

    val dtList = mutableListOf<Float>()
    dtList.add(0f)
    for (i in 1 until gyroList.size){
        val dt = (gyroList[i].timestamp - gyroList[i - 1].timestamp) / 1000f // ms → s
        dtList.add(dt)    }

    val quaternions = mutableListOf(Quaternion.identity())
    val startVector = Triple(0.0, 0.0, 1.0)

    for (i in 1 until gyroList.size) {
        val omega = Triple(
            gyroList[i].x.toDouble(),
            gyroList[i].y.toDouble(),
            gyroList[i].z.toDouble()
        )
        val dt = dtList[i]

        val deltaRot = Quaternion.identity().fromRotVec(omega, dt)
        val cumulative = quaternions.last() * deltaRot

        quaternions.add(cumulative)
    }

    fun determineDirection(x: Double, y: Double, z: Double): Int {
        val bx = if (x >= 0) 1 else 0
        val by = if (y >= 0) 1 else 0
        val bz = if (z >= 0) 1 else 0
        return (bx shl 2) or (by shl 1) or bz
    }

    val directionCounts = IntArray(8) { 0 }

    for (q in quaternions) {
        val rotated = q.rotate(startVector)
        val dir = determineDirection(rotated.first, rotated.second, rotated.third)
        directionCounts[dir]++
    }

}

data class DataPoint(val timestamp: Long, val x: Float, val y: Float, val z: Float)
// !----------------------------------------------!
