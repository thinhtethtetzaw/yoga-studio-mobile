package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.SessionManager
import com.example.universalyogaapp.components.CommonScaffold
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme


@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val userName = sessionManager.fetchUserName() ?: "User"
    val userEmail = sessionManager.fetchUserEmail() ?: "No email"

    CommonScaffold(navController = navController, title = "Profile") { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profile Header with Background
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            // Background Circles
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            
                            // User Avatar
                            Surface(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(40.dp),
                                    tint = Color.White
                                )
                            }
                            
                        }
                        
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = (-50).dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(10.dp, 20.dp)) {
                    MenuListItem(
                        icon = Icons.Default.Group,
                        title = "Create New Admin Account",
                        subtitle = "Create a new admin account",
                        onClick = { navController.navigate(Routes.Register.route) }
                    )
                }
            }

            // Logout Button
            Button(
                onClick = {
                    sessionManager.clearSession()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp).offset(y = (-20).dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Logout")
            }

            // Version Text
            Text(
                text = "Version 1.0.0",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally).offset(y = (-130).dp),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MenuListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
