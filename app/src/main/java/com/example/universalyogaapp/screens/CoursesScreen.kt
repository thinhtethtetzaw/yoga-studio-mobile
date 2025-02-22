package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.universalyogaapp.data.CourseWithClassCount
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.universalyogaapp.components.CommonScaffold
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import com.example.universalyogaapp.components.NetworkStatusBar
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    navController: NavController,
    courseViewModel: CourseViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isSyncing by remember { mutableStateOf(false) }
    var showSyncError by remember { mutableStateOf(false) }
    var showNetworkStatus by remember { mutableStateOf(false) }
    var networkStatusTimer: Job? by remember { mutableStateOf(null) }
    
    LaunchedEffect(Unit) {
        courseViewModel.loadCourses()
    }

    val coursesWithCount by courseViewModel.coursesWithCount.collectAsState()


    CommonScaffold(
        navController = navController,
        title = "Courses",
        actions = {
            // Existing Sync Button
            IconButton(
                onClick = {
                    showNetworkStatus = true
                    networkStatusTimer?.cancel()
                    networkStatusTimer = scope.launch {
                        if (isNetworkAvailable(context)) {
                            isSyncing = true
                            try {
                                courseViewModel.syncCourses()
                                showSyncError = false
                                // Hide network status after successful sync
                                delay(2000) // Show for 2 seconds
                                showNetworkStatus = false
                            } catch (e: Exception) {
                                Log.e("CoursesScreen", "Sync error", e)
                                showSyncError = true
                            } finally {
                                isSyncing = false
                            }
                        } else {
                            showSyncError = true
                            // Keep network status visible for error state
                            delay(3000) // Show for 3 seconds
                            showNetworkStatus = false
                        }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync courses",
                        tint = if (isNetworkAvailable(context)) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Add Course Button
            OutlinedButton(
                onClick = { navController.navigate("create_course") },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(end = 16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("+ Add", color = MaterialTheme.colorScheme.secondary)
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Network status bar - only shows when showNetworkStatus is true
                NetworkStatusBar(
                    isOnline = isNetworkAvailable(context),
                    visible = showNetworkStatus
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(coursesWithCount) { courseWithCount ->
                            CourseCard(
                                courseWithCount = courseWithCount,
                                onClick = {
                                    navController.navigate("course_detail/${courseWithCount.course.id}")
                                }
                            )
                        }
                    }

                }
            }
        }
    )
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${courseWithCount.course.timeOfCourse} | ${courseWithCount.course.duration / 60} Hours",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text = courseWithCount.course.courseName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp)
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
                text = "£${courseWithCount.course.pricePerClass}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
