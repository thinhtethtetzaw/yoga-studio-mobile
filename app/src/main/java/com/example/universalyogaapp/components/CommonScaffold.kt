package com.example.universalyogaapp.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.universalyogaapp.BottomNavItem
import com.example.universalyogaapp.R
import com.example.universalyogaapp.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScaffold(
    navController: NavController,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (navController.currentDestination?.route != Routes.Home.route) {
                TopAppBar(
                    title = { Text(text = title) },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                            }
                        }
                    },
                    actions = actions
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = navController.currentDestination?.route == Routes.Home.route,
                    onClick = { navController.navigate(Routes.Home.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.White
                    )
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            painter = painterResource(id = R.drawable.ic_course),
                            contentDescription = "Courses"
                        )
                    },
                    label = { Text("Courses") },
                    selected = navController.currentDestination?.route == Routes.Courses.route,
                    onClick = { navController.navigate(Routes.Courses.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.White
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Classes") },
                    label = { Text("Classes") },
                    selected = navController.currentDestination?.route == Routes.Classes.route,
                    onClick = { 
                        try {
                            navController.navigate(Routes.Classes.route) {
                                popUpTo(Routes.Home.route)
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            println("Navigation error: ${e.message}")
                            e.printStackTrace()
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.White
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Instructors") },
                    label = { Text("Profile") },
                    selected = navController.currentDestination?.route == Routes.Profile.route,
                    onClick = { navController.navigate(Routes.Profile.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.White
                    )
                )
            }
        },
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        content(paddingValues)
    }
}
