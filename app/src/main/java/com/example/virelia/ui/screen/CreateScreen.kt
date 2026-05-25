package com.example.virelia.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.virelia.ui.viewmodel.CreateViewModel
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.text.BasicTextField
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(

    noteId: Int? = null,

    onBackClick: () -> Unit = {},

    viewModel: CreateViewModel = viewModel()
) {

    val note by viewModel.note.collectAsState()

    // =========================
    // LOAD NOTE UNTUK EDIT
    // =========================
    LaunchedEffect(noteId) {

        if (
            noteId != null &&
            noteId != -1
        ) {

            viewModel.getNote(noteId)
        }
    }

    // =========================
    // STATE
    // =========================
    var title by remember {
        mutableStateOf("")
    }

    val state = rememberRichTextState()

    var isBold by remember {
        mutableStateOf(false)
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var imageUrl by remember {
        mutableStateOf("")
    }

    var showDeleteImageDialog by remember {
        mutableStateOf(false)
    }

    // =========================
    // SET DATA EDIT
    // =========================
    LaunchedEffect(note) {

        note?.let {

            title = it.title
            state.setHtml(it.desc)
            imageUrl = it.imageUrl

            selectedImageUri = null
        }
    }

    // =========================
    // IMAGE PICKER
    // =========================
    fun uploadGambar(uri: Uri, onSuccess: (String) -> Unit) {
        val ref = FirebaseStorage.getInstance().reference
            .child("catatan/${System.currentTimeMillis()}.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onSuccess(url.toString())
                }
            }
    }

    val imageLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.GetContent()

        ) { uri ->
            selectedImageUri = uri
        }

    // =========================
    // UI
    // =========================
    Scaffold(

        containerColor = Color(0xFFF5F5F7),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text =
                            if (note == null)
                                "New Story"
                            else
                                "Edit Story",

                        fontWeight =
                            FontWeight.SemiBold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.ArrowBack,

                            contentDescription = null
                        )
                    }
                },

                actions = {

                    TextButton(

                        enabled =
                            !viewModel.isSaving,

                        onClick = {

                            // VALIDASI
                            if (
                                title.isBlank() ||
                                state.toHtml().isBlank()
                            ) {
                                return@TextButton
                            }

                            // SAVE NOTE
                            if (selectedImageUri != null) {

                                uploadGambar(selectedImageUri!!) { uploadedUrl ->

                                    viewModel.saveNote(
                                        note = note,
                                        title = title,
                                        content = android.text.Html
                                            .fromHtml(
                                                state.toHtml(),
                                                android.text.Html.FROM_HTML_MODE_COMPACT
                                            )
                                            .toString(),
                                        imageUrl = uploadedUrl,
                                        localImageUri = selectedImageUri.toString(),
                                        onSuccess = {
                                            onBackClick()
                                        }
                                    )
                                }

                            } else {

                                viewModel.saveNote(
                                    note = note,
                                    title = title,
                                    content = android.text.Html
                                        .fromHtml(
                                            state.toHtml(),
                                            android.text.Html.FROM_HTML_MODE_COMPACT
                                        )
                                        .toString(),
                                    imageUrl = imageUrl,
                                    localImageUri = "",
                                    onSuccess = {
                                        onBackClick()
                                    }
                                )
                            }
                        }
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Save,

                            contentDescription = null,

                            tint = Color(0xFF1565FF)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(

                            text =
                                if (viewModel.isSaving)
                                    "Saving..."
                                else
                                    "Save",

                            color = Color(0xFF1565FF),

                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )
                }
            )
        },

        // =========================
        // BOTTOM BAR
        // =========================
        bottomBar = {

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(

                        horizontal = 18.dp,

                        vertical = 10.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(16.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                //BOLD
                IconButton(
                    onClick = {

                        isBold = !isBold

                        state.toggleSpanStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.FormatBold,

                        contentDescription = null,

                        tint =
                            if (isBold)
                                Color(0xFF1565FF)
                            else
                                Color.Gray
                    )
                }

                // IMAGE
                IconButton(

                    onClick = {

                        imageLauncher.launch(
                            "image/*"
                        )
                    }
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.AddPhotoAlternate,

                        contentDescription = null,

                        tint = Color(0xFF1565FF)
                    )
                }
            }
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(

                text =
                    "Created March 24, 2025 • Personal",

                color = Color.Gray,

                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // =========================
            // TITLE
            // =========================
            BasicTextField(

                value = title,

                onValueChange = {
                    title = it
                },

                modifier = Modifier
                    .fillMaxWidth(),

                textStyle = TextStyle(

                    fontSize = 30.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = Color.Black
                ),

                decorationBox = { innerTextField ->

                    if (title.isEmpty()) {

                        Text(

                            text = "Title",

                            color = Color.LightGray,

                            fontSize = 30.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    innerTextField()
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =========================
// CONTENT
// =========================

            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            ) {

                BasicRichTextEditor(

                    state = state,

                    textStyle = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black
                    ),

                    modifier = Modifier
                        .fillMaxWidth()
                )

                // PLACEHOLDER
                if (
                    state.toHtml()
                        .replace("<p>", "")
                        .replace("</p>", "")
                        .replace("<br>", "")
                        .trim()
                        .isBlank()
                ) {

                    Text(

                        text = "Write your story",

                        color = Color.LightGray,

                        fontSize = 16.sp,

                        fontWeight = FontWeight.Bold,

                        modifier = Modifier.padding(
                            start = 2.dp,
                            top = 2.dp
                        )
                    )
                }
            }

            // =========================
// IMAGE PREVIEW
// =========================

            if (
                selectedImageUri != null ||
                imageUrl.isNotEmpty()
            ) {

                Box {

                    Image(

                        painter = rememberAsyncImagePainter(
                            model =
                                selectedImageUri ?: imageUrl
                        ),

                        contentDescription = null,

                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp)
                            .clip(
                                RoundedCornerShape(20.dp)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        showDeleteImageDialog = true
                                    }
                                )
                            },

                        contentScale = ContentScale.FillWidth
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                if (showDeleteImageDialog) {

                    AlertDialog(

                        onDismissRequest = {
                            showDeleteImageDialog = false
                        },

                        title = {
                            Text("Delete Image")
                        },

                        text = {
                            Text("Do you want to remove this image?")
                        },

                        confirmButton = {

                            TextButton(

                                onClick = {

                                    selectedImageUri = null
                                    imageUrl = ""

                                    showDeleteImageDialog = false
                                }
                            ) {

                                Text(
                                    "Delete",
                                    color = Color.Red
                                )
                            }
                        },

                        dismissButton = {

                            TextButton(
                                onClick = {
                                    showDeleteImageDialog = false
                                }
                            ) {

                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}