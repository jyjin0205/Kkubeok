package com.example.kkubeok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import com.example.kkubeok.ui.theme.KkubeokTheme

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.example.kkubeok.screen.DataCollecting
import com.example.kkubeok.screen.DetectingScreen
import com.example.kkubeok.screen.AlarmScreen
import com.example.kkubeok.screen.HomeScreen
import com.example.kkubeok.screen.TimelineScreen

//import database
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.kkubeok.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = DatabaseProvider.getDatabase(this)

        setContent {
            KkubeokTheme {
                KkubeokMain(db)
            }
        }
    }
}

@Composable
fun KkubeokMain(db: AppDatabase){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Login"){
        composable("Login") { LoginScreen(navController, db) }
        composable("Data Collecting") { DataCollecting(navController) }
        composable("Detecting"){DetectingScreen(navController = navController)}
        composable("Alarm"){AlarmScreen(navController)}
        composable("Home"){ HomeScreen(navController)}
        composable("Timeline"){TimelineScreen(navController)}
    }
}

@Composable
fun LoginScreen(navController: NavHostController, db:AppDatabase) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal=32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center
        ){
            // App Name
            Text(
                text="\uD83E\uDD71 Kkubeok",
                style = MaterialTheme.typography.headlineMedium,
                modifier=Modifier.padding(bottom=32.dp)
            )
            // Welcome Comment
            Text(
                text= "Hello, What's your name?",
                style=MaterialTheme.typography.titleMedium
            )
            Text(
                text="Enter Your Name",
                style=MaterialTheme.typography.bodySmall,
                color=Color.Gray,
                modifier=Modifier.padding(bottom=8.dp)
            )
            // Name Input Bar
            var name by remember{mutableStateOf("")}
            OutlinedTextField(
                value=name,
                onValueChange={name=it},
                placeholder={Text("Gildong")},
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(bottom=24.dp)
            )

            if(name != "") {
                // Start Button
                Button(
                    onClick = {
                        insertUserId(db, name)
                        navController.navigate("Home")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Start", color = Color.White)
                }
                // Detecting Button
                Button(
                    onClick = {
                        insertUserId(db, name)
                        navController.navigate("Data Collecting")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Data Collecting", color = Color.White)
                }
            }
        }
    }
}

fun insertUserId(db: AppDatabase, username: String) {
    CoroutineScope(Dispatchers.IO).launch{
        val existingUser = db.userInfoDao().getUser(username)
        if(existingUser == null)
        {
            db.userInfoDao().insert(
                UserInfo(user_id = username)
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val dummyNavController = rememberNavController()
    KkubeokTheme {
        LoginScreen(navController = dummyNavController)
    }
}
 */



