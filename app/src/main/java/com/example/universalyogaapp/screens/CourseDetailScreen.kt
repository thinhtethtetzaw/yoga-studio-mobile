package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.universalyogaapp.BottomNavItem
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.R
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.universalyogaapp.components.CommonScaffold
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: Long,
    courseViewModel: CourseViewModel = viewModel()
) {
    val course by courseViewModel.getCourseById(courseId).collectAsState(initial = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    CommonScaffold(
        navController = navController,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    course?.let { course ->
                        Text(
                            text = "${course.courseName} Course",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = course.daysOfWeek.split(",").joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${course.timeOfCourse} | ${course.duration/60} Hours",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = "£${course.pricePerClass}/class",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFE57373),
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            InfoCard(
                                title = "Capacity",
                                value = "${course.capacity}",
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                title = "Level",
                                value = "${course.difficultyLevel}",
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                title = "Type",
                                value = "${course.typeOfClass}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "2 Classes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Class cards would go here
                        // You might want to create a separate composable for class cards
                    }
                }

                // Add buttons at the bottom
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("edit_course/$courseId") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A635D)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Edit")
                        }
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }


        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            title = { Text("Are you sure to delete this instructor?") },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Cancel",
                                color = Color.Black
                            )
                        }
                        Button(
                            onClick = {
                                course?.let { courseViewModel.deleteCourse(it) }
                                showDeleteDialog = false
                                navController.navigateUp()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm")
                        }
                    }

                }
            },
            dismissButton = null,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(80.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
} 