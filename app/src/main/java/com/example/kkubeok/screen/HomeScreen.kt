package com.example.kkubeok.screen

import android.content.Context
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.kkubeok.database.DatabaseProvider
import com.example.kkubeok.database.AppDatabase
import com.example.kkubeok.database.SleepLog


import com.example.kkubeok.BottomNavigationBar

import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.kkubeok.database.TrainingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun HomeScreen(navController: NavHostController?=null, context: Context) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    var userId = prefs.getString("user_id",null)
    val db = remember { DatabaseProvider.getDatabase(context) }
    var userSleepLog = remember{ mutableStateListOf<SleepLog>() }

    // 스레드를 분리해야 했었다
    LaunchedEffect(Unit) {
        if (userId != null) {
            val sleepLogs = withContext(Dispatchers.IO) {
                db.sleepLogDao().getSleepLogsByUser(userId)
            }
            userSleepLog.clear()
            userSleepLog.addAll(sleepLogs)
        }
    }

    var showDialog by remember { mutableStateOf(false)}
    if(showDialog)
    {
        EditButtonWithDialog(onDismiss = {showDialog = false})
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar={
            navController?.let{
                BottomNavigationBar(navController=it)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
                TextButton(onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("EDIT")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SleepStatCard(
                icon = Icons.Filled.Nightlight,
                label = "Nightsleep Time",
                value = "6 hr 15 min"
            )

            SleepStatCard(
                icon = Icons.Filled.LightMode,
                label = "Microsleep Time",
                value = "1 hr 45 min"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    ,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Spacer(modifier = Modifier.height(8.dp).width(8.dp))
                Text(
                    text = "  Daily Routine",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MealCard(time = "10:00 am")
                MealCard(time = "01:00 pm")
                MealCard(time = "05:00 pm")
            }
        }
    }
}

@Composable
fun SleepStatCard(icon: ImageVector, label: String, value: String) {
    val customColor = Color(255,200,0)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp)
                    .align(Alignment.CenterVertically),
                tint = customColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
            )
            {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun MealCard(time: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Fastfood,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = time,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
            )
        }
    }
}

@Composable
fun EditButtonWithDialog(onDismiss: () -> Unit){
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()){
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ){
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Edit Options", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Meal 버튼 로직 */ },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Meal")
                }

                Button(
                    onClick = { /* NightSleep 버튼 로직 */ },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("NightSleep")
                }
            }
        }
    }
}

@Composable
fun MealTimePickerDropDown(onTimeSelected: (String)-> Unit) {
    var selectedHour by remember { mutableStateOf("12") }
    var selectedMinute by remember { mutableStateOf("00") }
    val hours = (0..23).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "15", "30", "45")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        DropdownSelector(label = "Hour", options = hours, selected = selectedHour, onSelectedChange = { selectedHour = it })
        Spacer(modifier = Modifier.width(16.dp))
        DropdownSelector(label = "Minute", options = minutes, selected = selectedMinute, onSelectedChange = { selectedMinute = it })
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { onTimeSelected("$selectedHour:$selectedMinute") }) {
        Text("Confirm")
    }
}

@Composable
fun DropdownSelector(label: String, options: List<String>, selected: String, onSelectedChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label)
        Box {
            Button(onClick = { expanded = true }) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelectedChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}*/