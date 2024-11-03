package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.universalyogaapp.R
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.BottomNavItem
import com.example.universalyogaapp.data.Course
import com.example.universalyogaapp.viewmodels.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navController: NavController,
    courseViewModel: CourseViewModel = viewModel()
) {
    val courses by courseViewModel.allCourses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yoga Courses") },
                actions = {
                    IconButton(onClick = { navController.navigate("create_course") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Course")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                val items = listOf(
                    BottomNavItem("Home", Icons.Default.Home, route = Routes.Home.route),
                    BottomNavItem(
                        title = "Courses",
                        iconResId = R.drawable.ic_course,
                        route = Routes.Courses.route
                    ),
                    BottomNavItem(
                        title = "Classes",
                        icon = Icons.Default.DateRange,
                        route = Routes.Classes.route
                    ),
                    BottomNavItem(
                        title = "Profile",
                        icon = Icons.Default.AccountCircle,
                        route = Routes.Profile.route
                    )
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item.icon != null) {
                                Icon(item.icon, contentDescription = item.title)
                            } else if (item.iconResId != null) {
                                Icon(
                                    painter = painterResource(id = item.iconResId),
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = { Text(item.title) },
                        selected = item.route == Routes.Courses.route,
                        onClick = { 
                            navController.navigate(item.route) {
                                popUpTo(Routes.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        }
    ) { padding ->
        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No courses available.\nClick + to add a new course.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseCard(course = course)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = course.daysOfWeek.split(",").joinToString(", "),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = course.timeOfCourse,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = course.typeOfClass,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "£${course.pricePerClass}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Level: ${course.difficultyLevel}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Capacity: ${course.capacity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (course.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
