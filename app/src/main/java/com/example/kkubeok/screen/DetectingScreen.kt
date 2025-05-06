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


import androidx.compose.runtime.*
import kotlinx.coroutines.selects.select


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
            label = { Text("Menu") },
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
fun DetectingScreen() {
    val options = listOf("M1","M2","M3")
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var textFieldValue by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometersensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val gravitysensor = remember {sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)}
    val gyroscopesensor = remember {sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)}

    val sensorData = remember { mutableStateListOf<String>() }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val timestamp = System.currentTimeMillis()
                    val data = "$timestamp,${it.values[0]},${it.values[1]},${it.values[2]}"
                    sensorData.add(data)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    var isListening by remember { mutableStateOf(false) }

    LaunchedEffect(isListening) {
        if (isListening) {
            sensorManager.registerListener(sensorListener, accelerometersensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        DropdownMenuBox(
            options = options,
            selectedOption = selectedOptionText,
            onOptionSelected = { selectedOptionText = it}
        )

        Button(onClick = { isListening = true }, modifier = Modifier.fillMaxWidth()) {
            Text("센서 시작")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { isListening = false }, modifier = Modifier.fillMaxWidth()) {
            Text("센서 중지")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { saveCSV(context, sensorData) }, modifier = Modifier.fillMaxWidth()) {
            Text("CSV 저장")
        }
    }
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