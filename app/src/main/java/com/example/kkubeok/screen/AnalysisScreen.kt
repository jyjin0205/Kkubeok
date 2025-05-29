package com.example.kkubeok.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.room.Room
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.kkubeok.BottomNavigationBar
import com.example.kkubeok.database.AppDatabase
import com.example.kkubeok.database.Detected

@Composable
fun AnalysisScreen(navController: NavHostController?=null) {
    val context = LocalContext.current
    val scope= rememberCoroutineScope()

    /* Connecting with DataBase */
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("user_id",null)

    val db= remember{
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kkubeok_database"
        ).fallbackToDestructiveMigration().build()
    }
    val detectedDao=db.detectedDao()
    var detectedData by remember {mutableStateOf(listOf<Detected>())}

    /* Load Date */
    val dateList = remember { generateDateList() }
    val averageTime = remember { mutableStateOf("00:00") }

    /* Food Coma */
    val foodComaEmojis=remember{mutableStateListOf<String>()}

    LaunchedEffect(Unit) {
        scope.launch {
            detectedData=detectedDao.getDetectedByUser(userId!!)
            if (userId != null) {
                val allDetected = detectedDao.getDetectedByUser(userId) // get the detected data
                val allMeals=db.mealDao().getMealsByUser(userId) // get the meal data

                // previous 7 days
                val now = Calendar.getInstance()
                now.set(Calendar.HOUR_OF_DAY, 0)
                now.set(Calendar.MINUTE, 0)
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                val todayStartMillis = now.timeInMillis
                val todayEndMillis = todayStartMillis + 24 * 60 * 60 * 1000 - 1
                val weekAgoMillis = todayStartMillis - 6 * 24 * 60 * 60 * 1000 // 7 days ago

                val dateFormatter=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = todayStartMillis

                // recent 7 days filtering
                val recentData = allDetected.filter { detected ->
                    val start = detected.start_time ?: 0
                    start in weekAgoMillis..todayEndMillis
                }

                // duration sum
                if (recentData.isNotEmpty()) {
                    val totalMillis = recentData.sumOf { (it.end_time ?: 0L) - (it.start_time ?: 0L) }
                    val avgMillis = totalMillis / 7
                    val minutes = avgMillis / 1000 / 60
                    val seconds = (avgMillis / 1000) % 60
                    averageTime.value = "%02d:%02d".format(minutes, seconds)
                } else {
                    averageTime.value = "No recent data" // exception
                }

                /* Food Coma */
                foodComaEmojis.clear()
                for (i in 0 until 7) {
                    val dateStr = dateFormatter.format(calendar.time) // 날짜 문자열
                    val mealsOfDay = allMeals.filter { it.meal_date == dateStr }
                    val detectedOfDay = allDetected.filter { it.calendar_date == dateStr }

                    // find detected food coma microsleep
                    var found = false
                    for (meal in mealsOfDay) {
                        val mealTime = meal.meal_time ?: continue
                        for (detected in detectedOfDay) {
                            val sleepStart = detected.start_time ?: continue
                            if (kotlin.math.abs(sleepStart - mealTime) <= 2 * 60 * 60 * 1000) {
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                    foodComaEmojis.add(if (found) "😴" else "⚪")
                    calendar.add(Calendar.DATE, -1) // 하루 전으로
                }
                foodComaEmojis.reverse()

            }
        }
    }


    Scaffold(
        bottomBar={
            navController?.let{
                BottomNavigationBar(navController=it)
            }
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Analysis",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Average Microsleep Time
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Average Microsleep Time", fontSize = 18.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(averageTime.value, fontSize = 36.sp, fontWeight = FontWeight.Bold)

                }
            }

            // Food Coma
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Food Coma", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        foodComaEmojis.forEach { emoji ->
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Walking after meals helps prevent food coma", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Night vs Day Sleep Placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Night Sleep vs Day Sleep", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.LightGray)
                    ) {
                        // Placeholder for future chart
                    }
                }
            }

            // Posture
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Posture", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You usually sleep to the right.\nHow about the left next time?", fontSize = 14.sp)
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AnalysisScreenPreview() {
    AnalysisScreen()
}


