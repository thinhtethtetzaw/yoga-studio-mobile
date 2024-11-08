package com.example.universalyogaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.universalyogaapp.ui.theme.*
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.*
import com.example.universalyogaapp.screens.CoursesScreen
import com.example.universalyogaapp.screens.ClassesScreen
import com.example.universalyogaapp.screens.InstructorsScreen
import com.example.universalyogaapp.screens.ParticipantsScreen
import com.example.universalyogaapp.screens.ProfileScreen
import androidx.compose.material3.MaterialTheme
import com.example.universalyogaapp.screens.CreateCourseScreen
import com.example.universalyogaapp.screens.CreateInstructorScreen
import com.example.universalyogaapp.screens.CourseDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.universalyogaapp.screens.EditCourseScreen
import com.example.universalyogaapp.screens.AddClassScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        setContent {
            UniversalYogaAppTheme {
                val navController = rememberNavController()
                val startDestination = if (sessionManager.fetchAuthToken() != null) {
                    Routes.Home.route
                } else {
                    Routes.Intro.route
                }
                
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    builder = {
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
                        composable(Routes.Courses.route) {
                            CoursesScreen(navController = navController)
                        }
                        composable(Routes.Classes.route) {
                            ClassesScreen(navController = navController)
                        }
                        composable(Routes.Instructors.route) {
                            InstructorsScreen(navController = navController)
                        }
                        composable(Routes.CreateInstructor.route) {
                            CreateInstructorScreen(navController = navController)
                        }
                        composable(Routes.Participants.route) {
                            ParticipantsScreen(navController = navController)
                        }
                        composable(Routes.Profile.route) {
                            ProfileScreen(navController = navController)
                        }
                        composable(Routes.CreateCourse.route) {
                            CreateCourseScreen(navController)
                        }
                        composable(
                            route = "course_detail/{courseId}",
                            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            CourseDetailScreen(
                                navController = navController,
                                courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
                            )
                        }
                        composable(
                            "edit_course/{courseId}",
                            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
                            EditCourseScreen(navController = navController, courseId = courseId)
                        }
                        composable(Routes.AddClass.route) {
                            AddClassScreen(navController = navController)
                        }
                    }
                )
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
        // Image(
        //     painter = painterResource(id = R.drawable.yoga_illustration),
        //     contentDescription = "Yoga Illustration",
        //     modifier = Modifier.size(300.dp)
        // )

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
            onClick = { 
                // Navigate to Login screen instead of Home
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.Intro.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "Get started",
                color = Color.White
            )
        }
    }
}

@Composable
fun LottieAnimation() {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Url("https://lottie.host/8d3b4400-3c38-44df-b314-10c5a5cdf0f5/QSnjwU8SK0.json"))
    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)
}
