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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login to Admin Panel",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (validateLogin(email, password)) {
                    val user = dbHelper.getUserByEmail(email)
                    if (user != null && user.second == password) {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter valid email and password", Toast.LENGTH_SHORT).show()
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
            Text("Login")
        }

        TextButton(onClick = { navController.navigate(Routes.Register.route) }) {
            Text("Don't have an account? Register", color = Color.DarkGray)
        }
    }
}

private fun validateLogin(email: String, password: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotBlank()
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    UniversalYogaAppTheme {
        LoginScreen(navController = rememberNavController())
    }
}
