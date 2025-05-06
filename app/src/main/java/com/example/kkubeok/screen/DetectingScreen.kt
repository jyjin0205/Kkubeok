package com.example.kkubeok.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import java.io.File

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*


import androidx.compose.runtime.*
import kotlinx.coroutines.selects.select


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectingTopBar(){
    CenterAlignedTopAppBar(
        title = {Text("Data Collecting")}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected:(String) -> Unit
){
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Posture") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(150.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
            Text("x: ${accelValue.first}", modifier = Modifier.weight(1f))
            Text("x: ${gravityValue.first}", modifier = Modifier.weight(1f))
            Text("x: ${gyroValue.first}", modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("y: ${accelValue.second}", modifier = Modifier.weight(1f))
            Text("y: ${gravityValue.second}", modifier = Modifier.weight(1f))
            Text("y: ${gyroValue.second}", modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("z: ${accelValue.third}", modifier = Modifier.weight(1f))
            Text("z: ${gravityValue.third}", modifier = Modifier.weight(1f))
            Text("z: ${gyroValue.third}", modifier = Modifier.weight(1f))
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
fun DetectingScreen() {
    val options = listOf("Others","Nodding Off","Lean Back","Resting Head(Right)","Resting Head(Left)")
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var textFieldValue by remember { mutableStateOf("") }

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

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val timestamp = System.currentTimeMillis()
                    val (x,y,z) = it.values

                    //TODO:SensorData를 Add해야함
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
            sensorManager.registerListener(sensorListener, accelerometersensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(sensorListener, gravitysensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(sensorListener, gyroscopesensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            sensorManager.unregisterListener(sensorListener)
        }
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
            onOptionSelected = { selectedOptionText = it}
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
            TimerBox(title = "Total Time", time = "00:00", modifier=Modifier.weight(1f))

            TimerBox(title = "MicroSleep Time", time = "00:00",modifier=Modifier.weight(1f))
        }

        SensorDataBox(
            accelValue = accelValue,
            gravityValue = gravityValue,
            gyroValue = gyroValue
        )

        Spacer(modifier = Modifier.height(8.dp))

        if(!isListening)
        {
            Button(onClick = { isListening = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Start")
            }
        }
        else{
            Button(onClick = { isListening = false }, modifier = Modifier.fillMaxWidth()) {
                Text("Pause")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isListening = false }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }}


}

fun saveCSV(context: Context, data: List<String>) {
    val fileName = "sensor_data_${System.currentTimeMillis()}.csv"
    val file = File(context.getExternalFilesDir(null), fileName)

    file.bufferedWriter().use { writer ->
        writer.write("timestamp,x,y,z\n")
        data.forEach {
            writer.write("$it\n")
        }
    }
}