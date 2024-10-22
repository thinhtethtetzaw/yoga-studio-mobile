package com.example.universalyogaapp

import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.universalyogaapp.ui.theme.*
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniversalYogaAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.Login.route) {
                    composable(Routes.Login.route) {
                        LoginScreen(navController = navController)
                    }
                    composable(Routes.Register.route) {
                        RegisterScreen(navController = navController)
                    }
                    composable(Routes.Intro.route) {
                        IntroScreen(navController = navController)
                    }
                    composable(Routes.Home.route) {
                        HomeScreen(navController = navController)
                    }
                    // ... (other composables)
                }
            }
        }
    }
}

@Composable
fun IntroScreen(navController: NavController) {
    val welcomeMessage = "Welcome to Yoga Studio"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.yoga_illustration),
            contentDescription = "Yoga Illustration",
            modifier = Modifier.size(300.dp)
        )

        LottieAnimation()
        
        Text(
            text = welcomeMessage,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Manage your yoga classes, schedules, and instructors with ease.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { navController.navigate(Routes.Login) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
               containerColor = Color(0xFFB47B84)
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Get started")
        }
    }
}

@Composable
fun LottieAnimation() {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://lottie.host/8d3b4400-3c38-44df-b314-10c5a5cdf0f5/QSnjwU8SK0.json"))
    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)
}
