package com.example.ktpwatermark.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ktpwatermark.ui.components.DropdownField
import com.example.ktpwatermark.utils.addWatermark
import com.example.ktpwatermark.utils.saveImage
import com.example.ktpwatermark.utils.watermarkOptions

enum class Screen {Home, Preview, Done}

@ExperimentalMaterialApi
@Composable
fun HomeScreen() {
    val screenState = remember { mutableStateOf(Screen.Home)}
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }

    val launcherImage = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }


    val launcherPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted

        } else {
            // Permission Denied
        }
    }

    val launcherCamera = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.TakePicturePreview()) {
        bitmap.value = it
    }

    Scaffold(
        modifier = Modifier.padding(16.dp)
    ) {
        when (PackageManager.PERMISSION_GRANTED) {
            //Check permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                // You can use the API that requires the permission.

            }
            else -> {
                // Asking for permission
                launcherPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        when (screenState.value) {
            Screen.Preview -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (bitmap.value != null) {
                        IconButton(onClick = {
                            screenState.value = Screen.Home
                        }, modifier = Modifier.align(alignment = Alignment.Start)) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                        }
                        Image(bitmap = bitmap.value!!.asImageBitmap(), contentDescription = null)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            imageUri = saveImage(context, bitmap.value!!)
                            screenState.value = Screen.Done
                        }) {
                            Text(text = "Save")
                        }
                    }
                }
            }
            Screen.Done -> {
                val bitmapImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri!!))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Horee berhasil membuat")
                    Image(bitmap = bitmapImage.asImageBitmap(), contentDescription = null)
                    Text(text = "")
                    Row {
                        FloatingActionButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = imageUri
                            context.startActivity(intent)
                        }) {
                            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        FloatingActionButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "image/png"
                            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                            context.startActivity(intent)
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        }
                    }
                }

            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(
                    rememberScrollState())) {
                    Spacer(modifier = Modifier.height(12.dp))
    
                    imageUri?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            bitmap.value = MediaStore.Images
                                .Media.getBitmap(context.contentResolver, it)
    
                        } else {
                            val source = ImageDecoder
                                .createSource(context.contentResolver, it)
                            bitmap.value = ImageDecoder.decodeBitmap(source)
                        }
    
    
                    }
                    if (bitmap.value != null) {
                        Image(
                            bitmap = bitmap.value!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(400.dp)
                        )
                    } else {
                        Box (modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(72.dp))
                                Spacer(modifier = Modifier.height(32.dp))
                                Row(horizontalArrangement = Arrangement.Center) {
                                    FloatingActionButton(onClick = { launcherImage.launch("image/*") }) {
                                        Icon(imageVector = Icons.Default.Folder, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    FloatingActionButton(onClick = { launcherCamera.launch() }) {
                                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
    
                    val billingPeriodItems = listOf(
                        "Pojok Kanan Atas", "Pojok Kiri Atas",
                        "Pojok Kanan Bawah", "Pojok Kiri Bawah")
                    var billingPeriodExpanded by remember { mutableStateOf(false) }
    
                    var selectedIndex by remember { mutableStateOf(0) }
                    var watermark by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = watermark,
                        onValueChange = { watermark = it },
                        label = { Text("Watermark")},
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
    
                    var fontSize by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        label = { Text("Font Size")},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
    
                    Spacer(modifier = Modifier.height(10.dp))
                    DropdownField(
                        menuItems = billingPeriodItems,
                        menuExpandedState = billingPeriodExpanded,
                        seletedIndex = selectedIndex,
                        updateMenuExpandStatus = {
                            billingPeriodExpanded = true
                        },
                        onDismissMenuView = {
                            billingPeriodExpanded = false
                        },
                        onMenuItemclick = { index->
                            selectedIndex = index
                            billingPeriodExpanded = false
                        }
                    )
    
                    Spacer(modifier = Modifier.height(8.dp))
    
                    val coroutineScope = rememberCoroutineScope()
    
                    Row {
                        Button(onClick = { /*TODO*/ }, modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)) {
                            Text("Reset")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (bitmap.value != null) {
                                    val watermarkOptions = watermarkOptions(
                                        cornerStr = billingPeriodItems[selectedIndex],
                                        textColor = Color.RED
                                    )
                                    bitmap.value =  addWatermark(bitmap.value!!, watermark, watermarkOptions)
                                    screenState.value = Screen.Preview
    
                                } else {
                                    Log.d("HOME", "null image")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)) {
                            Text("Lanjut")
                        }
                    }
                }
            }
        }

    }

}
