package com.example.universalyogaapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.universalyogaapp.ui.theme.*
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.MaterialTheme
import com.example.universalyogaapp.models.Admin
import com.example.universalyogaapp.utils.FirebaseUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Admin Account",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                placeholder = { Text("Enter your full name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("Confirm your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validateInputs(name, email, password, confirmPassword)) {
                        registerUser(context, dbHelper, name, email, password, navController)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Create")
            }
        }
    }
}

private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
    return when {
        name.isBlank() -> false
        email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> false
        password.length < 6 -> false
        password != confirmPassword -> false
        else -> true
    }
}

private fun registerUser(
    context: Context,
    dbHelper: DatabaseHelper,
    name: String,
    email: String,
    password: String,
    navController: NavController
) {
    // Create Admin object
    val admin = Admin(
        name = name,
        email = email,
        password = password,
        createdAt = System.currentTimeMillis()
    )

    // First save to local database
    val result = dbHelper.addUser(name, email, password)
    
    if (result != -1L) {
        // If local save is successful, save to Firebase
        FirebaseUtils.addAdmin(
            admin = admin,
            onSuccess = {
                // Start sync process
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val localAdmins = dbHelper.getAllAdmins()
                        FirebaseUtils.syncAdmins(localAdmins)
                            .onSuccess {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Admin account created and synced successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.Profile.route) {
                                        popUpTo(Routes.Register.route) { inclusive = true }
                                    }
                                }
                            }
                            .onFailure { e ->
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Sync Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onFailure = { e ->
                Toast.makeText(context, "Firebase Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    UniversalYogaAppTheme {
        RegisterScreen(navController = rememberNavController())
    }
}
