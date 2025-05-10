package com.example.kkubeok.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

import androidx.activity.compose.BackHandler

import androidx.compose.runtime.*

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectingTopBar(){
    CenterAlignedTopAppBar(
        title = {Text("Data Collecting",
            modifier = Modifier.padding(top = 50.dp, bottom = 25.dp))
                },
        modifier = Modifier.height(140.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected:(String) -> Unit,
    enabled: Boolean
){
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if(enabled) expanded = !expanded }
    ) {
        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Posture") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(200.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if(enabled) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onOptionSelected(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SensorDataBox(
    accelValue: Triple<Float,Float,Float>,
    gravityValue: Triple<Float,Float,Float>,
    gyroValue: Triple<Float,Float,Float>
){
    Column(
        modifier = Modifier.
        fillMaxWidth().
        padding(16.dp)
    ){
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Accel", modifier = Modifier.weight(1f))
            Text("Gravity", modifier = Modifier.weight(1f))
            Text("Gyro", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("x: %.5f".format(accelValue.first), modifier = Modifier.weight(1f))
            Text("x: %.5f".format(gravityValue.first), modifier = Modifier.weight(1f))
            Text("x: %.5f".format(gyroValue.first), modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("y: %.5f".format(accelValue.second), modifier = Modifier.weight(1f))
            Text("y: %.5f".format(gravityValue.second), modifier = Modifier.weight(1f))
            Text("y: %.5f".format(gyroValue.second), modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("z: %.5f".format(accelValue.third), modifier = Modifier.weight(1f))
            Text("z: %.5f".format(gravityValue.third), modifier = Modifier.weight(1f))
            Text("z: %.5f".format(gyroValue.third), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TimerBox(title: String, time:String, modifier: Modifier=Modifier){
    Card(
        modifier = modifier
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun DataCollecting(navController: NavController) {
    val options = listOf("Others","Nodding Off","Lean Back","Resting Head(Right)","Resting Head(Left)")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    val accelerometersensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gravitysensor = remember {sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)}
    val gyroscopesensor = remember {sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)}

    var accelData = remember {mutableStateListOf<String>()}
    var gravityData = remember {mutableStateListOf<String>()}
    var gyroData = remember {mutableStateListOf<String>()}

    var accelValue by remember { mutableStateOf(Triple(0f,0f,0f))}
    var gravityValue by remember { mutableStateOf(Triple(0f,0f,0f))}
    var gyroValue by remember { mutableStateOf(Triple(0f,0f,0f))}

    var totalTime by remember{mutableStateOf(0L)}
    var currentTime by remember{mutableStateOf(0L)}
    var microSleepTime by remember { mutableStateOf(0L) }
    var startTimestamp by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }
    //TODO 사람 이름을 어떻게 넘길 지

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val timestamp = System.currentTimeMillis()
                    val (x,y,z) = it.values


                    when(it.sensor.type){
                        Sensor.TYPE_ACCELEROMETER -> {
                            accelValue = Triple(x,y,z)
                            accelData.add("$timestamp,$x,$y,$z")
                        }
                        Sensor.TYPE_GRAVITY -> {
                            gravityValue = Triple(x,y,z)
                            gravityData.add("$timestamp,$x,$y,$z")
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                            gyroValue = Triple(x,y,z)
                            gyroData.add("$timestamp,$x,$y,$z")
                        }
                        else -> {}
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    var isListening by remember { mutableStateOf(false) }

    LaunchedEffect(isListening) {
        if (isListening) {
            sensorManager.registerListener(sensorListener, accelerometersensor, 10000)
            sensorManager.registerListener(sensorListener, gravitysensor, 10000)
            sensorManager.registerListener(sensorListener, gyroscopesensor, 10000)

        } else {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    BackHandler {
        navController.popBackStack()
    }
    Scaffold (
        topBar = { DataCollectingTopBar()}
    )
    { innerPadding ->
        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ){
        DropdownMenuBox(
            options = options,
            selectedOption = selectedOptionText,
            onOptionSelected = { selectedOptionText = it},
            enabled = !isListening
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
            TimerBox(title = "Total Time", time = formatSeconds(totalTime+currentTime), modifier=Modifier.weight(1f))

            TimerBox(title = "MicroSleep Time", time = formatSeconds(currentTime),modifier=Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        SensorDataBox(
            accelValue = accelValue,
            gravityValue = gravityValue,
            gyroValue = gyroValue
        )

        Spacer(modifier = Modifier.height(30.dp))

        if(!isListening)
        {
            Button(onClick = { isListening = true
                startTimestamp = System.currentTimeMillis()
                timerJob = coroutineScope.launch{
                    while (true) {
                        delay(1000L)
                        currentTime = (System.currentTimeMillis() - (startTimestamp ?: System.currentTimeMillis())) / 1000
                    }
                }
                             },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary)) {
                Text("Start")
            }
        }
        else{
            Button(onClick = { isListening = false },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary)) {
                Text("Pause")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { isListening = false
                timerJob?.cancel()
                timerJob = null
                totalTime += currentTime
                currentTime = 0
                saveCSV(context, selectedOptionText, accelData, gravityData, gyroData) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary)) {
                Text("Save")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }}
}

fun formatSeconds(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

fun saveCSV(context: Context, selectedOptionText: String, accelData: List<String>,gravityData: List<String>, gyroData: List<String>) {

    val filesAndData = listOf(
        "${selectedOptionText}_accel.csv" to accelData,
        "${selectedOptionText}_gravity.csv" to gravityData,
        "${selectedOptionText}_gyro.csv" to gyroData
    )

    filesAndData.forEach { (fileName, dataList) ->
        val file = File(context.getExternalFilesDir(null), fileName)
        val fileExists = file.exists()

        val writer = BufferedWriter(FileWriter(file, true))

        writer.use {
            if (!fileExists) {
                it.write("timestamp,x,y,z\n")
            }
            dataList.forEach { line ->
                it.write("$line\n")
            }
        }
        Log.d("SaveCSV", "Saving to: ${file.absolutePath}")

    }

}