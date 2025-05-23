package com.example.kkubeok.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.navigation.NavHostController
import com.example.kkubeok.BottomNavigationBar
import com.example.kkubeok.ui.theme.KkubeokTheme
import com.example.kkubeok.database.AppDatabase
import com.example.kkubeok.database.Detected
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.kkubeok.database.DatabaseProvider
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch


data class MicrosleepSession(
    val date: String,
    val totalTime: String,
    val records: List<MicrosleepRecord>
)

data class MicrosleepRecord(
    val emoji: String,
    val label: String,
    val duration: String,
    val time: String
)

// --- Main Screen ---
@Composable
fun TimelineScreen(navController: NavHostController?=null) {
    val context = LocalContext.current
    val scope=rememberCoroutineScope()

    /* Connecting with DataBase */
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("user_id",null)

    val db=remember{
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kkubeok_database"
        ).fallbackToDestructiveMigration().build()
    }
    val detectedDao=db.detectedDao()

    LaunchedEffect(Unit){
        /* Dummy Data Insert */
        scope.launch{
            val detected1=Detected(
                user_id=userId!!,
                calendar_date="May 4 (Sun)",
                action_name="Dozing",
                start_time=parseTime("14:17:00"),
                end_time=parseTime("14:29:44"),
                direction="None"
            )
            val detected2=Detected(
                user_id=userId,
                calendar_date="May 4 (Sun)",
                action_name="Nap",
                start_time=parseTime("15:45:00"),
                end_time=parseTime("16:12:05"),
                direction="Left"
            )
            detectedDao.insert(detected2)
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
                .padding(horizontal = 16.dp)
        ){
            var selectedIndex by remember { mutableStateOf(0) }

            val sampleData = listOf(
                MicrosleepSession(
                    date = "May 4 (Sun)",
                    totalTime = "39:49",
                    records = listOf(
                        MicrosleepRecord("\uD83D\uDE2A", "Dozing", "12:44", "02:17 PM"),
                        MicrosleepRecord("\uD83D\uDE34", "Nap", "27:05", "03:45 PM")
                    )
                ),
                MicrosleepSession(
                    date = "May 3 (Sat)",
                    totalTime = "20:29",
                    records = listOf(
                        MicrosleepRecord("\uD83D\uDE34", "Nap", "15:24", "10:12 AM"),
                        MicrosleepRecord("\uD83D\uDE2A", "Dozing", "05:05", "02:25 PM")
                    )
                ),
                MicrosleepSession(
                    date = "May 2 (Fri)",
                    totalTime = "27:09",
                    records = listOf(
                        MicrosleepRecord("\uD83D\uDE34", "Nap", "10:07", "12:15 PM"),
                        MicrosleepRecord("\uD83D\uDE2A", "Dozing", "17:02", "01:25 PM")
                    )
                )

            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TimelineDateHeader(
                    selectedDateIndex = selectedIndex,
                    onDateSelected = { index -> selectedIndex = index }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sampleData) { session ->
                        SessionCard(session)
                    }
                }
            }
        }
    }

}

// --- Timeline Session Card ---
@Composable
fun SessionCard(session: MicrosleepSession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F6F6), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(session.date, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(session.totalTime, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        session.records.forEach {
            MicrosleepRecordCard(it)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TimelineDateHeader(
    selectedDateIndex: Int = 1,
    onDateSelected: (Int) -> Unit = {}
) {
    val days = listOf(
        "Sun" to "4", "Mon" to "5", "Tue" to "6", "Wed" to "7",
        "Thu" to "8", "Fri" to "9", "Sat" to "10", "Sun" to "11", "Mon" to "12"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // 양 옆 화살표
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            itemsIndexed(days) { index, (day, date) ->
                val isSelected = index == selectedDateIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) Color(0xFFE0E0E0) else Color.Transparent)
                        .clickable { onDateSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = day,
                        fontWeight = FontWeight.Medium,
                        color = if (index == 0) Color.Gray else Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = date,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MicrosleepRecordCard(record: MicrosleepRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(record.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(record.label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(record.duration, fontSize = 14.sp)
                Text(record.time, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

fun parseTime(timeStr: String): Long {
    val sdf=SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val date=sdf.parse(timeStr)?:return System.currentTimeMillis()
    return date.time
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    KkubeokTheme {
        val navController=rememberNavController()
        TimelineScreen(navController=navController)
    }
}
