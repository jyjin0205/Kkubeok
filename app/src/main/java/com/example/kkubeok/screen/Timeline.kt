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

    val listState=rememberLazyListState() // date list

    LaunchedEffect(Unit){
        scope.launch{
            detectedData=detectedDao.getDetectedByUser(userId!!)
        }
    }

"""
    LaunchedEffect(Unit){
        /* Dummy Data Insert */
        scope.launch{
            // data clear
            detectedDao.deleteAll()

            var formattedDate = formatCalendarDate(2025, Calendar.MAY, 20)

            val detected10=Detected(
                user_id=userId!!,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 20,"09:09:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 20,"09:31:01"),
                direction="None"
            )

            formattedDate = formatCalendarDate(2025, Calendar.MAY, 21)

            val detected9=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 21,"14:15:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 21,"14:31:11"),
                direction="None"
            )

            formattedDate = formatCalendarDate(2025, Calendar.MAY, 22)

            val detected8=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 22,"14:15:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 22,"14:31:11"),
                direction="None"
            )

            formattedDate = formatCalendarDate(2025, Calendar.MAY, 23)

            val detected1=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 23,"13:25:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 23,"13:42:02"),
                direction="None"
            )

            val detected2=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Nap",
                start_time=buildTimestamp(2024, Calendar.MAY, 23,"12:15:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 23,"12:25:07"),
                direction="Right"
            )

            formattedDate = formatCalendarDate(2025, Calendar.MAY, 24)

            val detected3=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 24,"14:25:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 24,"14:30:05"),
                direction="None"
            )

            val detected4=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Nap",
                start_time=buildTimestamp(2024, Calendar.MAY, 24,"10:12:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 24,"10:27:24"),
                direction="Left"
            )

            formattedDate = formatCalendarDate(2025, Calendar.MAY, 25)

            val detected5=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Nap",
                start_time=buildTimestamp(2024, Calendar.MAY, 25,"15:45:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 25,"16:12:05"),
                direction="Left"
            )

            val detected6=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 25,"14:17:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 25,"14:29:44"),
                direction="None"
            )
            formattedDate = formatCalendarDate(2025, Calendar.MAY, 26)
            val detected7=Detected(
                user_id=userId,
                calendar_date=formattedDate,
                action_name="Dozing",
                start_time=buildTimestamp(2024, Calendar.MAY, 26,"14:15:00"),
                end_time=buildTimestamp(2024, Calendar.MAY, 26,"14:31:11"),
                direction="None"
            )
            detectedDao.insert(detected10)
            detectedDao.insert(detected9)
            detectedDao.insert(detected8)
            detectedDao.insert(detected1)
            detectedDao.insert(detected2)
            detectedDao.insert(detected3)
            detectedDao.insert(detected4)
            detectedDao.insert(detected5)
            detectedDao.insert(detected6)
            detectedDao.insert(detected7)
        }
    }
    """



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

            val sessionList = reversedGrouped.map { (date, records) ->
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
                    date = date,
                    totalTime = totalFormatted,
                    records = formattedRecords
                )
            }

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
                            listState.animateScrollToItem(sessionList.lastIndex - index) // 최신 날짜가 위에 오도록 정렬된 상태
                        }
                    }
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
        Text(session.totalTime, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        session.records.forEach {
            MicrosleepRecordCard(it)
            Spacer(modifier = Modifier.height(8.dp))
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
    onDateSelected: (Int) -> Unit
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

fun formatCalendarDate(year: Int, month: Int, day: Int): String {
    val cal = Calendar.getInstance().apply {
        set(year, month, day)
    }
    val formatter = SimpleDateFormat("MMM d (EEE)", Locale.US)
    return formatter.format(cal.time)
}

fun buildTimestamp(year: Int, month: Int, day: Int, timeStr: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.parse(String.format("%04d-%02d-%02d %s", year, month + 1, day, timeStr))!!.time
}
