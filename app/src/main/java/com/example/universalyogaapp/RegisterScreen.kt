package com.example.universalyogaapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.universalyogaapp.ui.theme.*
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create an Account",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
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
                containerColor = Color(0xFFB47B84)
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate(Routes.Login.route) }) {
            Text("Already have an account? Login", color = Color.DarkGray)
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
    val result = dbHelper.addUser(name, email, password)
    if (result != -1L) {
        Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
        navController.navigate(Routes.Login.route) {
            popUpTo(Routes.Register.route) { inclusive = true }
        }
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
