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

import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String {
    val currentTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
    val currentDate = Date(currentTime)
    return dateFormat.format(currentDate)
}

fun getYesterDaySleepTime(): Pair<Long, Long>{
    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime

    calendar.add(Calendar.DAY_OF_YEAR, -1)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val yesterday11pm = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 7)
    val today7am = calendar.timeInMillis

    return Pair(yesterday11pm, today7am)
}

fun getMillisSleepTime(startTime: String, endTime:String) : Pair<Long, Long>{
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance()

    startCalendar.time = timeFormat.parse(startTime)!!
    endCalendar.time = timeFormat.parse(endTime)!!

    val today = Calendar.getInstance()
    startCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
    endCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))

    // Slept before 24:00
    if(startCalendar.get(Calendar.HOUR_OF_DAY) >= 12)
    {
        startCalendar.set(Calendar.DAY_OF_YEAR, -1)
    }

    return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis)
}


@Composable
fun HomeScreen(navController: NavHostController?=null, context: Context) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("user_id",null)
    val db = remember { DatabaseProvider.getDatabase(context) }
    var userSleepLog by remember { mutableStateOf<SleepLog?>(null) }

    val currentDate = getCurrentDate().toString()

    // 스레드를 분리해야 했었다
    LaunchedEffect(Unit) {
        if (userId != null) {
            val sleepLog = withContext(Dispatchers.IO) {
                db.sleepLogDao().getSleepLogsByUserAndDate(userId, currentDate)
            }

            if (sleepLog != null) {
                withContext(Dispatchers.Main) {
                    userSleepLog = sleepLog
                }
            } else {
                val (yesterday11pm, today7am) = getYesterDaySleepTime()
                val newSleepLog = SleepLog(
                    user_id = userId,
                    sleep_date = currentDate,
                    sleep_start = yesterday11pm,
                    sleep_end = today7am
                )

                withContext(Dispatchers.Main) {
                    userSleepLog = newSleepLog
                }

                withContext(Dispatchers.IO) {
                    db.sleepLogDao().insert(newSleepLog)
                }
            }
        }
    }


    var showDialog by remember { mutableStateOf(false)}
    if(showDialog)
    {
        EditAllDialog(onDismiss = {showDialog = false},
            onSleepTimeSelected = {start, end ->
                if(userId != null)
                {
                    CoroutineScope(Dispatchers.IO).launch{
                        val newSleepLog = SleepLog(
                            user_id = userId,
                            sleep_date = currentDate,
                            sleep_start = start,
                            sleep_end = end
                        )
                        db.sleepLogDao().insert(newSleepLog)
                    }
                }
            }
        )
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
                value = userSleepLog
            )

            SleepStatCard(
                icon = Icons.Filled.LightMode,
                label = "Microsleep Time",
                value = null
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
fun SleepStatCard(icon: ImageVector, label: String, value: SleepLog?) {
    val customColor = Color(255,200,0)
    var sleepDurationString = "0 hr 0 min"
    if(value != null) {
        val durationMillis = (value.sleep_end ?: 0L) - (value.sleep_start ?: 0L)
        val durationMinutes = durationMillis / 1000 / 60
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        sleepDurationString = "${hours} hr ${minutes} min"
    }

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
                    text = sleepDurationString,
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
fun EditAllDialog(
    onDismiss: () -> Unit,
    onSleepTimeSelected: (Long, Long) -> Unit
){
    var currentScreen by remember { mutableStateOf("menu") }

    when(currentScreen){
        "menu" -> {
            EditButtonWithDialog(
                onDismiss = onDismiss,
                onNightSleepClick = { currentScreen = "sleep" }
            )
        }

        "sleep" -> {
            SleepTimePicker(
                onValidTimeSelected = { start, end ->
                    onSleepTimeSelected(start, end)
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun EditButtonWithDialog(
    onDismiss: () -> Unit,
    onNightSleepClick: () -> Unit
){
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
                    onClick = onNightSleepClick,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("NightSleep")
                }
            }
        }
    }
}


@Composable
fun SleepTimePicker(
    onValidTimeSelected: (start: Long, end: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var startHour by remember { mutableStateOf("22") }
    var startMinute by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("07") }
    var endMinute by remember { mutableStateOf("00") }

    var errorText by remember { mutableStateOf("") }

    val hours = (0..23).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "15", "30", "45")

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
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Text("Night Sleep Time", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sleep Start Time")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DropdownSelector("Hour", hours, startHour) { startHour = it }
                        Spacer(modifier = Modifier.width(16.dp))
                        DropdownSelector("Minute", minutes, startMinute) { startMinute = it }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Wake-up Time")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DropdownSelector("Hour", hours, endHour) { endHour = it }
                        Spacer(modifier = Modifier.width(16.dp))
                        DropdownSelector("Minute", minutes, endMinute) { endMinute = it }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorText.isNotEmpty()) {
                        Text(text = errorText, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(onClick = {
                        val startTime = "${startHour}:${startMinute}"
                        val endTime = "${endHour}:${endMinute}"

                        val (startMillis, endMillis) = getMillisSleepTime(startTime, endTime)

                        if (startMillis >= endMillis) {
                            errorText = "잠든 시간은 일어난 시간보다 빨라야 해요."
                        } else {
                            errorText = ""
                            onValidTimeSelected(startMillis, endMillis)
                        }
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }
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