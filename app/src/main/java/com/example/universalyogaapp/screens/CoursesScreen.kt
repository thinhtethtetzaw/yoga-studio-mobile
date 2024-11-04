package com.example.universalyogaapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.universalyogaapp.components.CommonScaffold


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navController: NavController,
    courseViewModel: CourseViewModel = viewModel()
) {
    val courses by courseViewModel.allCourses.collectAsState()

    CommonScaffold(
        navController = navController,
        content = { padding ->
            Column(
                modifier = Modifier.padding(top = padding.calculateTopPadding())
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Courses",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    },
                    actions = {
                        OutlinedButton(
                            onClick = { navController.navigate("create_course") },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.padding(end = 16.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+ Add", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )

                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No courses available.\nClick + to add a new course.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = padding.calculateBottomPadding() + 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(courses) { course ->
                            CourseCard(
                                course = course,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CourseCard(course: Course, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("course_detail/${course.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_course),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = course.daysOfWeek.split(",").joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${course.timeOfCourse} | ${course.duration/60} Hours",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "capacity: ${course.capacity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${course.daysOfWeek.split(",").size} classes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    text = "£${course.pricePerClass}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
