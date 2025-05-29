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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.launch
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
import java.util.Date
import java.util.Calendar
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
    var detectedData by remember {mutableStateOf(listOf<Detected>())}

    /* Load Date */
    val dateList = remember { generateDateList() }
    var selectedIndex by remember { mutableStateOf(dateList.lastIndex) }

    val listState=rememberLazyListState() // session card lazy column
    val dateListState=rememberLazyListState() // date list on the top

    LaunchedEffect(Unit){
        scope.launch{
            detectedData=detectedDao.getDetectedByUser(userId!!)
        }
        dateListState.scrollToItem(dateList.lastIndex)
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
            val grouped = detectedData.groupBy { it.calendar_date }
            val reversedGrouped=grouped.toSortedMap(compareByDescending {it}) // decreasing order

            val fullDateList=generateFullDateList()

            val sessionList = fullDateList.map { dateRaw ->
                val records = grouped[dateRaw] ?: emptyList()
                val formattedRecords = records.map {
                    val durationSec = ((it.end_time ?: 0L) - (it.start_time ?: 0L)) / 1000
                    val minutes = durationSec / 60
                    val seconds = durationSec % 60
                    MicrosleepRecord(
                        emoji = if (it.action_name == "Dozing") "😪" else "😴",
                        label = it.action_name ?: "Unknown",
                        duration = "%02d:%02d".format(minutes, seconds),
                        time = formatTime(it.start_time ?: 0L)
                    )
                }

                val totalSeconds = records.sumOf { ((it.end_time ?: 0L) - (it.start_time ?: 0L)) / 1000 }
                val totalFormatted = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

                MicrosleepSession(
                    date = formatDateForDisplay(dateRaw),
                    totalTime = if (records.isEmpty()) "No Microsleep Day" else totalFormatted,
                    records = formattedRecords
                )
            }.reversed() // recent calendar ordering first

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TimelineDateHeader(
                    selectedDateIndex = selectedIndex,
                    onDateSelected = { index ->
                        selectedIndex = index
                        scope.launch {
                            dateListState.animateScrollToItem(index) // card scroll
                            listState.animateScrollToItem(sessionList.lastIndex - index) // recent date view
                        }
                    },
                    listState=dateListState
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state=listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sessionList) { session ->
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
        Text(session.totalTime, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (session.records.isEmpty()) {
            Text("No recorded microsleep on this day", fontSize = 14.sp, color = Color.Gray)
        } else {
            session.records.forEach {
                MicrosleepRecordCard(it)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun generateDateList(): List<String> {
    val formatter = SimpleDateFormat("EEE d", Locale.US)
    val startDate = Calendar.getInstance().apply {
        set(2025, Calendar.MAY, 20)
    }
    val endDate = Calendar.getInstance() // Today

    val dateList = mutableListOf<String>()
    while (!startDate.after(endDate)) {
        dateList.add(formatter.format(startDate.time))
        startDate.add(Calendar.DATE, 1)
    }
    return dateList
}

@Composable
fun TimelineDateHeader(
    selectedDateIndex: Int,
    onDateSelected: (Int) -> Unit,
    listState: LazyListState
) {
    val days = remember { generateDateList() }
    val todayIndex = days.lastIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            state=listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            itemsIndexed(days) { index, date ->
                val isSelected = index == selectedDateIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) Color(0xFFE0E0E0) else Color.Transparent)
                        .clickable { onDateSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val parts = date.split(" ")
                    Text(parts[0], fontWeight = FontWeight.Medium)
                    Text(parts[1], fontWeight = FontWeight.SemiBold)
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

fun formatTime(timeMillis: Long): String {
    val sdf=SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}

fun buildTimestamp(year: Int, month: Int, day: Int, timeStr: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.parse(String.format("%04d-%02d-%02d %s", year, month + 1, day, timeStr))!!.time
}

fun formatDateForDisplay(rawDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM d (EEE)", Locale.US)
    val date = inputFormat.parse(rawDate)
    return outputFormat.format(date!!)
}

fun generateFullDateList(): List<String> {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDate = Calendar.getInstance().apply {
        set(2025, Calendar.MAY, 20)
    }
    val endDate = Calendar.getInstance()

    val dateList = mutableListOf<String>()
    while (!startDate.after(endDate)) {
        dateList.add(formatter.format(startDate.time))
        startDate.add(Calendar.DATE, 1)
    }
    return dateList
}


