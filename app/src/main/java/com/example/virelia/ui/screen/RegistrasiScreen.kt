package com.example.virelia.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.virelia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegistrasiScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = Color.White,
        focusedContainerColor = Color.White
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "REGISTRATION",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2979FF)
        )

        Text(
            text = "Create your account to get started.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    "Full Name",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Name", fontSize = 14.sp) },
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Email",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "name@example.com",
                            fontSize = 14.sp
                        )
                    },
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Password",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = {
                        Text(
                            "Create password",
                            fontSize = 14.sp
                        )
                    },
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Confirm Password",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = {
                        Text(
                            "Repeat password",
                            fontSize = 14.sp
                        )
                    },
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage.isNotEmpty()) {

                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {

                        errorMessage = ""

                        if (
                            name.isEmpty() ||
                            email.isEmpty() ||
                            password.isEmpty() ||
                            confirmPassword.isEmpty()
                        ) {

                            errorMessage = "Semua field harus diisi"
                            return@Button
                        }

                        if (password != confirmPassword) {

                            errorMessage = "Password tidak sama"
                            return@Button
                        }

                        if (password.length < 6) {

                            errorMessage =
                                "Password minimal 6 karakter"
                            return@Button
                        }

                        isLoading = true

                        auth.createUserWithEmailAndPassword(
                            email,
                            password
                        )
                            .addOnSuccessListener {

                                val uid =
                                    auth.currentUser?.uid

                                val user = hashMapOf(
                                    "username" to name,
                                    "email" to email,
                                    "profileImage" to ""
                                )

                                db.collection("users")
                                    .document(uid!!)
                                    .set(user)
                                    .addOnSuccessListener {

                                        isLoading = false

                                        onRegisterSuccess()
                                    }
                                    .addOnFailureListener {

                                        isLoading = false

                                        errorMessage =
                                            it.message.toString()
                                    }
                            }
                            .addOnFailureListener {

                                isLoading = false

                                errorMessage =
                                    it.message.toString()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2979FF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {

                    if (isLoading) {

                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {

            Text(
                "Already have an account? ",
                fontSize = 14.sp
            )

            Text(
                text = "Log In",
                color = Color(0xFF2979FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onLoginClick()
                }
            )
        }
    }
}