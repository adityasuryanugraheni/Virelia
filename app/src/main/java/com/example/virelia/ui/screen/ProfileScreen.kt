package com.example.virelia.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.virelia.R
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ProfileScreen() {

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        imageUri = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(90.dp))

        // FOTO PROFIL
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {

            Image(
                painter =
                    if (imageUri != null)
                        rememberAsyncImagePainter(imageUri)
                    else
                        painterResource(id = R.drawable.profile),

                contentDescription = "Profile",

                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),

                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2979FF))
                    .clickable {
                        launcher.launch("image/*")
                    },

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White
                )

            }

        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Alex Rivera",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // STATISTIK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            ProfileStat("124", "Notes")
            ProfileStat("48", "Public")
            ProfileStat("892", "Likes")
            ProfileStat("1.2k", "Comment")

        }

        Spacer(modifier = Modifier.height(40.dp))

        // BUTTON LOGOUT
        OutlinedButton(
            onClick = {

            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),

            shape = RoundedCornerShape(12.dp),

            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Red
            )
        ) {

            Icon(
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = "Logout",
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Logout",
                color = Color.White
            )

        }

    }

}

@Composable
fun ProfileStat(
    number: String,
    title: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = number,
            color = Color(0xFF2979FF),
            fontSize = 18.sp
        )

        Text(
            text = title,
            color = Color.Gray
        )

    }

}