package com.example.virelia.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailScreen() {

    var comment by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(20.dp)
    ) {

        item {

            // TOP BAR
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Detail Note",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // USERNAME
            Text(
                text = "@alex_r",
                color = Color(0xFF1565FF),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // TITLE
            Text(
                text = "Reflections on Minimalist Architecture",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ISI NOTE
            Text(
                text = "The intersection of silence and space creates a dialogue that most modern structures fail to acknowledge. Minimalist architecture is not merely about reducing objects, but refining intention and emotional resonance within a room.",
                lineHeight = 24.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // LIKE
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.Red
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "892 Likes",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Comments",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // COMMENT CARD
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "@jordan",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565FF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "This perspective is beautifully written."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "@sam",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565FF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "I love the atmosphere created in this note."
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // INPUT COMMENT
            OutlinedTextField(
                value = comment,
                onValueChange = {
                    comment = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Write a comment...")
                },
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565FF)
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Post Comment",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}