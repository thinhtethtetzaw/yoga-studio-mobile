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
import com.example.universalyogaapp.data.CourseWithClassCount
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
    val coursesWithCount by courseViewModel.coursesWithCount.collectAsState()

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

                if (coursesWithCount.isEmpty()) {
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
                        items(coursesWithCount) { courseWithCount ->
                            CourseCard(
                                courseWithCount = courseWithCount,
                                onClick = { navController.navigate("course_detail/${courseWithCount.course.id}") }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CourseCard(courseWithCount: CourseWithClassCount, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_course),
                    contentDescription = "Instructor Icon",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = courseWithCount.course.daysOfWeek,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${courseWithCount.course.timeOfCourse} | ${courseWithCount.course.duration / 60} Hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = courseWithCount.course.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "capacity: ${courseWithCount.course.capacity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${courseWithCount.classCount} ${if (courseWithCount.classCount > 1) "Classes" else "Class"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price
            Text(
                text = "$${courseWithCount.course.pricePerClass}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE57373),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
