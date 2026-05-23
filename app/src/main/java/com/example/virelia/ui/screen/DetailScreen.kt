package com.example.virelia.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.virelia.data.Comment
import com.example.virelia.ui.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    firestoreId: String,
    title: String,
    desc: String,
    username: String,
    viewModel: DetailViewModel = viewModel()
) {

    LaunchedEffect(firestoreId) {
        viewModel.getComments(firestoreId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(20.dp)
    ) {

        item {
            Column {

                // TOP BAR
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detail Story",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = username,
                    color = Color(0xFF1565FF),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = desc,
                    lineHeight = 24.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // INPUT COMMENT
                OutlinedTextField(
                    value = viewModel.comment,
                    onValueChange = { viewModel.comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write a comment...") },
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.postComment(firestoreId) },
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
                    Text(text = "Post Comment", color = Color.White)
                }

                if (viewModel.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.message,
                        color = if (viewModel.message.contains("berhasil"))
                            Color(0xFF2979FF) else Color.Red,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // LIST KOMENTAR
                viewModel.commentList.forEach { item ->
                    CommentCard(
                        item = item,
                        firestoreId = firestoreId,
                        currentUserId = viewModel.currentUserId,
                        onDelete = { docId ->
                            viewModel.deleteComment(firestoreId, docId)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun CommentCard(
    item: Comment,
    firestoreId: String,
    currentUserId: String,
    onDelete: (String) -> Unit
) {

    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // HANYA MUNCUL DIALOG JIKA KOMENTAR MILIK SENDIRI
                        if (item.userId == currentUserId) {
                            showDeleteDialog = true
                        }
                    }
                )
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.username,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565FF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.comment)
        }
    }

    // DIALOG KONFIRMASI HAPUS
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Comment") },
            text = { Text("Are you sure want to delete this comment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(item.docId)
                    }
                ) {
                    Text(text = "Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}