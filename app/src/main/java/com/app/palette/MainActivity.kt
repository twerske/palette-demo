package com.app.palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import com.app.palette.ui.theme.PaletteTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.Text as Text


private const val REQUEST_GALLERY = 0x2908

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: PaletteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PaletteViewModel::class.java)
        setContent {
            PaletteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ColorPaletteGrid (
                        onClick = { showGallery() },
                        onSwatchClick = { copyHexCode("in onCreate") }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    intent?.let {
                        it.data?.let { uri ->
                            val source = ImageDecoder.createSource(
                                contentResolver,
                                uri
                            )
                            val bitmap = ImageDecoder.decodeBitmap(source).asShared()
                            viewModel.setBitmap(bitmap)
                            lifecycleScope.launch {
                                viewModel.setPalette(
                                    Palette.Builder(bitmap).generate()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes =
            arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun copyHexCode(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("color hex", text)
        clipboard.setPrimaryClip(clip)
    }
}

@Composable
fun ColorSwatch(
    color: Palette.Swatch,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hexCode = "#" + Integer.toHexString(color.rgb).toString().removePrefix("ff")
    Box(
        modifier = modifier
            .width(90.dp)
            .height(90.dp)
            .padding(2.dp)
            .clip(CircleShape)
            .background(Color(color.rgb))
            .clickable { onClick(hexCode) },
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = hexCode,
            style = MaterialTheme.typography.labelLarge,
            color = Color(color.titleTextColor),
        )
    }
}

@Preview(
    name = "Color Swatch",
    showBackground = true
)
@Composable
fun PalettePreview() {
    PaletteTheme {
        ColorSwatch(Palette.Swatch(-6768576, 1429), onClick = {})
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteGrid (
    viewModel: PaletteViewModel = viewModel(),
    onClick: () -> Unit,
    onSwatchClick: (String) -> Unit
) {
    val bitmap = viewModel.bitmap.observeAsState()
    val palette = viewModel.palette.observeAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    Icons.Outlined.FileUpload,
                    contentDescription = stringResource(id = R.string.select),
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            bitmap.value?.run {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    bitmap = asImageBitmap(),
                    contentDescription = null,
                    alignment = Alignment.Center
                )
            }

            LazyVerticalGrid (
                cells = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                palette.value?.run {
                    swatches.forEach {
                        item {
                            ColorSwatch(
                                color = it,
                                onClick = onSwatchClick,
                            )
                        }
                    }
                }
            }
        }
    }
}