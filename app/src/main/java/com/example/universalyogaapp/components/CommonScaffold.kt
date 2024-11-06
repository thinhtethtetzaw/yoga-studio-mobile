package com.example.universalyogaapp.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
    title: String? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val isHomeScreen = navController.currentDestination?.route == Routes.Home.route

    Scaffold(
        topBar = {
            if (!isHomeScreen) {
                CenterAlignedTopAppBar(
                    title = { Text(text = title ?: "Universal Yoga") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBackIosNew, "Back")
                        }
                    }
                )
            }
        },
        bottomBar = { BottomNavigation(navController) },
        floatingActionButton = if (floatingActionButton != null) {
            floatingActionButton
        } else {
            {}
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun BottomNavigation(navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
    ) {
        val items = listOf(
            BottomNavItem("Home", icon = Icons.Filled.Home, route = Routes.Home.route),
            BottomNavItem("Courses", iconResId = R.drawable.ic_course, route = Routes.Courses.route),
            BottomNavItem("Classes", icon = Icons.Filled.DateRange, route = Routes.Classes.route),
            BottomNavItem("Profile", icon = Icons.Filled.AccountCircle, route = Routes.Profile.route)
        )
        
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    } else if (item.iconResId != null) {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.title,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                },
                label = {
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        if (item.route == Routes.Home.route) {
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) {
                                    inclusive = true
                                }
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(Routes.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
