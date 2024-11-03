package com.example.universalyogaapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.universalyogaapp.data.Course
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    navController: NavController,
    courseViewModel: CourseViewModel = viewModel()
) {
    var courseName by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var fromTime by remember { mutableStateOf("") }
    var toTime by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var pricePerClass by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Dropdown states
    var expandedFromTimeDropdown by remember { mutableStateOf(false) }
    var expandedToTimeDropdown by remember { mutableStateOf(false) }

    // Add new dropdown states
    var expandedTypeDropdown by remember { mutableStateOf(false) }
    var expandedLevelDropdown by remember { mutableStateOf(false) }

    // Add dropdown state for days
    var expandedDaysDropdown by remember { mutableStateOf(false) }

    // Add predefined lists for type and level
    val yogaTypes = listOf("Flow Yoga", "Aerial Yoga", "Family Yoga")
    val levels = listOf("Beginner", "Intermediate", "Advanced")

    val timeSlots = (6..22).flatMap { hour ->
        listOf("$hour:00", "$hour:30")
    }

    // Replace errors list with individual error states
    var courseNameError by remember { mutableStateOf<String?>(null) }
    var daysOfWeekError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var capacityError by remember { mutableStateOf<String?>(null) }
    var levelError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    // Updated validation function to set individual errors
    fun validateInputs(): Boolean {
        var isValid = true
        
        // Reset all errors
        courseNameError = null
        daysOfWeekError = null
        timeError = null
        capacityError = null
        levelError = null
        typeError = null
        priceError = null
        
        if (courseName.isBlank()) {
            courseNameError = "Course name is required"
            isValid = false
        }
        
        if (selectedDays.isEmpty()) {
            daysOfWeekError = "At least one day must be selected"
            isValid = false
        }
        
        if (fromTime.isBlank() || toTime.isBlank()) {
            timeError = "Both start and end time are required"
            isValid = false
        }
        
        if (capacity.isBlank()) {
            capacityError = "Capacity is required"
            isValid = false
        } else if (capacity.toIntOrNull() == null) {
            capacityError = "Capacity must be a valid number"
            isValid = false
        }
        
        if (level.isBlank()) {
            levelError = "Level is required"
            isValid = false
        }
        
        if (type.isBlank()) {
            typeError = "Type is required"
            isValid = false
        }
        
        if (pricePerClass.isBlank()) {
            priceError = "Price is required"
            isValid = false
        } else if (pricePerClass.toDoubleOrNull() == null) {
            priceError = "Price must be a valid number"
            isValid = false
        }

        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Yoga Course") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Create a new course for yoga course",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Course Name
            OutlinedTextField(
                value = courseName,
                onValueChange = { 
                    courseName = it
                    courseNameError = null 
                },
                label = { Text("Course Name*") },
                isError = courseNameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (courseNameError != null) {
                Text(
                    text = courseNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Days of Week
            ExposedDropdownMenuBox(
                expanded = expandedDaysDropdown,
                onExpandedChange = { expandedDaysDropdown = !expandedDaysDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (selectedDays.isEmpty()) {
                        "Select days"
                    } else {
                        selectedDays.joinToString(", ")
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Days of the week*") },
                    trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "Dropdown arrow") },
                    isError = daysOfWeekError != null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedDaysDropdown,
                    onDismissRequest = { expandedDaysDropdown = false }
                ) {
                    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    daysOfWeek.forEach { day ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(day)
                                    if (selectedDays.contains(day)) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                                daysOfWeekError = null
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            if (daysOfWeekError != null) {
                Text(
                    text = daysOfWeekError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Time Selection

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // From Time
                ExposedDropdownMenuBox(
                    expanded = expandedFromTimeDropdown,
                    onExpandedChange = { expandedFromTimeDropdown = !expandedFromTimeDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = fromTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From*") },
                        trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "Dropdown arrow") },
                        isError = timeError != null,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFromTimeDropdown,
                        onDismissRequest = { expandedFromTimeDropdown = false }
                    ) {
                        timeSlots.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    fromTime = time
                                    timeError = null
                                    expandedFromTimeDropdown = false
                                }
                            )
                        }
                    }
                }

                // To Time
                ExposedDropdownMenuBox(
                    expanded = expandedToTimeDropdown,
                    onExpandedChange = { expandedToTimeDropdown = !expandedToTimeDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = toTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To*") },
                        trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "Dropdown arrow") },
                        isError = timeError != null,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedToTimeDropdown,
                        onDismissRequest = { expandedToTimeDropdown = false }
                    ) {
                        timeSlots.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    toTime = time
                                    timeError = null
                                    expandedToTimeDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            if (timeError != null) {
                Text(
                    text = timeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Capacity with number input
            OutlinedTextField(
                value = capacity,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        capacity = newValue
                        capacityError = null
                    }
                },
                label = { Text("Capacity*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = capacityError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (capacityError != null) {
                Text(
                    text = capacityError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Level Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedLevelDropdown,
                onExpandedChange = { expandedLevelDropdown = !expandedLevelDropdown }
            ) {
                OutlinedTextField(
                    value = level,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Level*") },
                    trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "Dropdown arrow") },
                    isError = levelError != null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedLevelDropdown,
                    onDismissRequest = { expandedLevelDropdown = false }
                ) {
                    levels.forEach { levelOption ->
                        DropdownMenuItem(
                            text = { Text(levelOption) },
                            onClick = {
                                level = levelOption
                                levelError = null
                                expandedLevelDropdown = false
                            }
                        )
                    }
                }
            }
            if (levelError != null) {
                Text(
                    text = levelError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedTypeDropdown,
                onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type*") },
                    trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, "Dropdown arrow") },
                    isError = typeError != null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTypeDropdown,
                    onDismissRequest = { expandedTypeDropdown = false }
                ) {
                    yogaTypes.forEach { yogaType ->
                        DropdownMenuItem(
                            text = { Text(yogaType) },
                            onClick = {
                                type = yogaType
                                typeError = null
                                expandedTypeDropdown = false
                            }
                        )
                    }
                }
            }
            if (typeError != null) {
                Text(
                    text = typeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Price with number input
            OutlinedTextField(
                value = pricePerClass,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        pricePerClass = newValue
                        priceError = null
                    }
                },
                label = { Text("Price per class (£)*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = priceError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (priceError != null) {
                Text(
                    text = priceError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Description (optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (validateInputs()) {
                            val course = Course(
                                courseName = courseName,
                                daysOfWeek = selectedDays.joinToString(","),
                                timeOfCourse = "$fromTime - $toTime",
                                capacity = capacity.toIntOrNull() ?: 0,
                                duration = calculateDuration(fromTime, toTime),
                                pricePerClass = pricePerClass.toDoubleOrNull() ?: 0.0,
                                typeOfClass = type,
                                description = description,
                                difficultyLevel = level
                            )
                            courseViewModel.insertCourse(course)
                            navController.navigateUp()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Create")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper function to calculate duration between two times
private fun calculateDuration(fromTime: String, toTime: String): Int {
    try {
        val from = fromTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val to = toTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        return to - from
    } catch (e: Exception) {
        return 0
    }
} 