package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.data.YogaClass
import com.example.universalyogaapp.viewmodels.ClassViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import com.example.universalyogaapp.components.DatePickerField
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.ui.res.painterResource
import com.example.universalyogaapp.R
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.example.universalyogaapp.components.NetworkStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(navController: NavController) {
    val context = LocalContext.current
    val classViewModel: ClassViewModel = viewModel()
    val courseViewModel: CourseViewModel = viewModel()
    val classes by classViewModel.classes.collectAsState()
    val courses by courseViewModel.coursesWithCount.collectAsState()
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var showSyncError by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var courseExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNetworkStatus by remember { mutableStateOf(false) }
    var networkStatusTimer: Job? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()
        classViewModel.loadInstructors()
        courseViewModel.loadCourses()
    }


    CommonScaffold(
        navController = navController,
        title = "Classes",
        actions = {
            IconButton(
                onClick = {
                    showNetworkStatus = true
                    networkStatusTimer?.cancel()
                    networkStatusTimer = scope.launch {
                        if (isNetworkAvailable(context)) {
                            isSyncing = true
                            try {
                                classViewModel.syncClasses()
                                showSyncError = false
                                // Hide network status after successful sync
                                delay(2000) // Show for 2 seconds
                                showNetworkStatus = false
                            } catch (e: Exception) {
                                Log.e("ClassesScreen", "Sync error", e)
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
                        contentDescription = "Sync classes",
                        tint = if (isNetworkAvailable(context)) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            OutlinedButton(
                onClick = { 
                    try {
                        navController.navigate(Routes.AddClass.route)
                    } catch (e: Exception) {
                        println("Navigation error: ${e.message}")
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.padding(end = 16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("+ Add", color = MaterialTheme.colorScheme.secondary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Add NetworkStatusBar at the top of the Column
            NetworkStatusBar(
                isOnline = isNetworkAvailable(context),
                visible = showNetworkStatus
            )

            // Search and Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp,16.dp, 16.dp, 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(6.dp)),
                            placeholder = { Text("Class by name or instructor") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = "Search",
                                    tint = Color.Gray
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_close),
                                            contentDescription = "Clear",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFEEEEEE),
                                focusedBorderColor = Color(0xFFDDDDDD)
                            )
                        )

                        // Filter Icon Button
                        IconButton(
                            onClick = { showFilters = true },
                            modifier = Modifier.padding(start = 8.dp).background(Color.White, RoundedCornerShape(6.dp)),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Filter",
                                tint = if (selectedCourse.isNotEmpty() || selectedDate.isNotEmpty()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    Color.Gray
                            )
                        }
                    }
                }
            }

            // Filters Section
            if (showFilters) {
                AlertDialog(
                    onDismissRequest = { showFilters = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Filters",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showFilters = false }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Close",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Course Filter
                            ExposedDropdownMenuBox(
                                expanded = courseExpanded,
                                onExpandedChange = { courseExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedCourse,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Course") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = courseExpanded,
                                    onDismissRequest = { courseExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Courses") },
                                        onClick = {
                                            selectedCourse = ""
                                            courseExpanded = false
                                        }
                                    )
                                    courses.forEach { courseWithCount ->
                                        DropdownMenuItem(
                                            text = { Text(courseWithCount.course.courseName) },
                                            onClick = {
                                                selectedCourse = courseWithCount.course.courseName
                                                courseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Date Filter
                            DatePickerField(
                                value = selectedDate,
                                onDateSelected = { selectedDate = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedCourse = ""
                                    selectedDate = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Clear All", color = Color.DarkGray)
                            }
                            Button(
                                onClick = { showFilters = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                )
            }

            // Filtered Classes List - Updated to include search query
            val filteredClasses = remember(
                classes, 
                searchQuery, 
                selectedCourse, 
                selectedDate
            ) {
                classes.filter { yogaClass ->
                    val matchesSearch = searchQuery.isEmpty() || 
                        yogaClass.name.contains(searchQuery, ignoreCase = true) ||
                        yogaClass.instructorName.contains(searchQuery, ignoreCase = true)
                    val matchesCourse = selectedCourse.isEmpty() || yogaClass.courseName == selectedCourse
                    val matchesDate = selectedDate.isEmpty() || yogaClass.date == selectedDate

                    matchesSearch && matchesCourse && matchesDate
                }
            }

            if (filteredClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (classes.isEmpty()) "No classes available" else "No matching classes found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredClasses) { yogaClass ->
                        ClassCard(yogaClass = yogaClass)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassCard(
    yogaClass: YogaClass,
    classViewModel: ClassViewModel = viewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Calendar Icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date: ${formatDate(yogaClass.date)}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = yogaClass.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = yogaClass.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Instructor: ${yogaClass.instructorName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!yogaClass.comment.isNullOrBlank()) {
                    Text(
                        text = "Note: ${yogaClass.comment}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Edit",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            showEditDialog = true
                        },
                        modifier = Modifier.height(32.dp)
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFB00020),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Delete",
                                    color = Color(0xFFB00020)
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Are you sure to delete this class?") },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                color = Color.Black
                            )
                        }
                        
                        Button(
                            onClick = {
                                classViewModel.deleteClass(yogaClass.id)
                                showDeleteDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Confirm",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        )
    }

    if (showEditDialog) {
        val courseViewModel: CourseViewModel = viewModel()
        var editName by remember { mutableStateOf(yogaClass.name) }
        var editInstructorName by remember { mutableStateOf(yogaClass.instructorName) }
        var editCourseName by remember { mutableStateOf(yogaClass.courseName) }
        var editDate by remember { mutableStateOf(yogaClass.date) }
        var editComment by remember { mutableStateOf(yogaClass.comment) }
        var instructorExpanded by remember { mutableStateOf(false) }
        var courseExpanded by remember { mutableStateOf(false) }
        var selectedCourseId by remember { mutableStateOf(yogaClass.courseId) }

        // Collect data from ViewModels
        val instructors by classViewModel.instructors.collectAsState()
        val coursesWithCount by courseViewModel.coursesWithCount.collectAsState()

        // Load data when dialog opens
        LaunchedEffect(Unit) {
            classViewModel.loadInstructors()
            courseViewModel.loadCourses()
        }

        // Debug logging
        LaunchedEffect(coursesWithCount) {
            println("Courses loaded in edit dialog: ${coursesWithCount.size}")
            coursesWithCount.forEach { 
                println("Course in edit dialog: ${it.course.courseName}")
            }
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Class") },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Class Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Instructor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = instructorExpanded,
                        onExpandedChange = { instructorExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editInstructorName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Instructor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instructorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = instructorExpanded,
                            onDismissRequest = { instructorExpanded = false }
                        ) {
                            instructors.forEach { instructor ->
                                DropdownMenuItem(
                                    text = { Text(instructor) },
                                    onClick = {
                                        editInstructorName = instructor
                                        instructorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Course Dropdown
                    ExposedDropdownMenuBox(
                        expanded = courseExpanded,
                        onExpandedChange = { courseExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editCourseName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Course") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = courseExpanded,
                            onDismissRequest = { courseExpanded = false }
                        ) {
                            coursesWithCount.forEach { courseWithCount ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = courseWithCount.course.courseName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        editCourseName = courseWithCount.course.courseName
                                        selectedCourseId = courseWithCount.course.id.toLong()
                                        courseExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    DatePickerField(
                        value = editDate,
                        onDateSelected = { editDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editComment,
                        onValueChange = { editComment = it },
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showEditDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                color = Color.Black
                            )
                        }
                        
                        Button(
                            onClick = {
                                classViewModel.updateClass(
                                    id = yogaClass.id,
                                    name = editName,
                                    instructorName = editInstructorName,
                                    courseId = selectedCourseId,
                                    courseName = editCourseName,
                                    date = editDate,
                                    comment = editComment
                                )
                                showEditDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Save",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}