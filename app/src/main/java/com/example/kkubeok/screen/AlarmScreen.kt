package com.example.kkubeok.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import com.example.kkubeok.BottomNavigationBar
import com.example.kkubeok.ui.theme.KkubeokTheme

@Composable
fun AlarmScreen(navController: NavHostController? = null) {
    var hour by remember {mutableIntStateOf(0)}
    var minute by remember {mutableIntStateOf(0)}
    var second by remember {mutableIntStateOf(0)}

    Scaffold(
        bottomBar = {
            navController?.let { BottomNavigationBar(navController = it) }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Alarm", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Card(
                modifier=Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors=CardDefaults.cardColors(containerColor=Color(0xFFEAE6F2)),
                shape=RoundedCornerShape(24.dp)
            ){
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .padding(vertical=32.dp),
                    horizontalArrangement=Arrangement.SpaceEvenly,
                    verticalAlignment=Alignment.CenterVertically
                ){
                    ScrollTimePicker(label = "Hour", maxValue = 23) { hour = it }
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    ScrollTimePicker(label = "Minute", maxValue = 59) { minute = it }
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    ScrollTimePicker(label = "Second", maxValue = 59) { second = it }
                }
            }

            Button(
                onClick = {
                    navController?.navigate("Countdown")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Start", color = Color.White)
            }
        }
    }
}

@Composable
fun ScrollTimePicker(label: String, maxValue: Int, onValueChange: (Int) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val visibleItems = 3

    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val centerIndex = listState.firstVisibleItemIndex + if (listState.firstVisibleItemScrollOffset > 50) 1 else 0
        onValueChange(centerIndex.coerceIn(0, maxValue))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(108.dp)
                .width(48.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding=PaddingValues(vertical=36.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(count = maxValue + 1) { index ->
                    val centerIndex by remember {
                        derivedStateOf { listState.firstVisibleItemIndex }
                    }
                    val isSelected = index == centerIndex
                    Text(
                        text = "%02d".format(index),
                        fontSize = if (isSelected) 22.sp else 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
