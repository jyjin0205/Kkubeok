package com.example.kkubeok.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.kkubeok.ui.theme.KkubeokTheme

@Composable
fun DetectingScreen(navController: NavHostController?=null){
    Scaffold{ paddingValues->
        Column(
            modifier=Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal=24.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier=Modifier.height(16.dp))

            // Header
            Text(
                text="Detecting",
                style=MaterialTheme.typography.headlineSmall,
                fontWeight=FontWeight.Bold,
                modifier=Modifier.align(Alignment.Start)
            )

            // Time Block
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ){
                TimerCard(title="Total Time", time = "00 : 00",modifier=Modifier.weight(1f))
                TimerCard(title="Microsleep Time", time="00 : 00", modifier=Modifier.weight(1f))
            }

            Spacer(modifier=Modifier.height(8.dp))

            // Sensor Values
            SensorTable()

            Spacer(modifier=Modifier.height(8.dp))

            // Start Button
            Button(
                onClick={/* TODO: Start logic */},
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(vertical=4.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Color.Black)
            )
            {
                Text("Start", color=Color.White)
            }

            // Pause Button
            Button(
                onClick={/* TODO: Pause logic */},
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(vertical=4.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Color.Black)
            )
            {
                Text("Pause", color=Color.White)
            }
        }
    }
}

@Composable
fun TimerCard(title: String, time: String, modifier: Modifier=Modifier) {
    Card(
        modifier = modifier
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.Medium, fontSize=15.sp, maxLines=1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(time, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SensorTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SensorColumn("Accel")
            SensorColumn("Gravity")
            SensorColumn("Gyro")
        }
    }
}

@Composable
fun SensorColumn(title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("X 0.000")
        Text("Y 0.000")
        Text("Z 0.000")
    }
}

@Preview(showBackground = true)
@Composable
fun DetectingScreenPreview() {
    KkubeokTheme {
        DetectingScreen()
    }
}
