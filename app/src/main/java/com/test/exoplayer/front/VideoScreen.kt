package com.test.exoplayer.front

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.test.exoplayer.front.viewModel.MainViewModel
import com.test.exoplayer.findActivity
import com.test.exoplayer.ui.theme.ExoPlayerTheme

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(videoModel: MainViewModel = hiltViewModel<MainViewModel>()) {

    val videoItems by videoModel.videoItems.collectAsState()
    val selectVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let(videoModel::addVideoUri)
        }
    )
    val localContext = LocalContext.current
    val currentOrientation = localContext.resources.configuration.orientation
    var isFullScreenClicked by remember {
        mutableStateOf(false)
    }


    val view = LocalView.current
    val window = localContext.findActivity()?.window

    val windowInsetsController =
        window?.let { WindowCompat.getInsetsController(it, view ) }

//    fun hideSystemBares(hide : Boolean){
//        windowInsetsController?.hide(WindowInsets.Type.statusBars())
//    }




    Scaffold(topBar = {
        AnimatedVisibility(currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Exo Player Sample",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }
    }, floatingActionButton = {
        IconButton(
            onClick = { selectVideoLauncher.launch("video/mp4") },
            modifier = Modifier
                .background(shape = CircleShape, color = MaterialTheme.colorScheme.primary)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Select Video",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }, modifier = Modifier.fillMaxSize()) { paddingValues ->


        val orientationState =
            remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }

        var lifecycle by remember {
            mutableStateOf(Lifecycle.Event.ON_CREATE)
        }


        val lifecycleOwner = LocalLifecycleOwner.current




        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                lifecycle = event
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer = observer)
            }
        }
        var setFullScreen by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = paddingValues.calculateTopPadding())
        ) {


            AndroidView(
                factory = { context ->
                    PlayerView(context).also {
                        it.player = videoModel.player
                        it.setFullscreenButtonClickListener { isFullScreen ->
                            if (isFullScreen) {
                                isFullScreenClicked = true
                                enterFullScreen(
                                    playerView = it,
                                    context = context,
                                    player = videoModel.player
                                )
                            } else {
                                exitFullScreen(
                                    playerView = it,
                                    context = context,
                                    player = videoModel.player
                                )
                            }
                        }
                    }
                },
                update = {
                    when (lifecycle) {
                        Lifecycle.Event.ON_PAUSE -> {
                            it.onPause()
                            it.player?.pause()
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            it.onResume()
                        }

                        else -> Unit
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Video List", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(16.dp)

            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(videoItems) { item ->
                    Text(text = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { videoModel.playVideo(item.contentUris) }
                            .padding(16.dp))
                }
                if (videoItems.isEmpty()) {
                    item {
                        Text(
                            text = "Video List is empty! How about adding one?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(16.dp)

                        )
                    }
                }

            }

        }


    }

}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Function to enter full screen mode
@OptIn(UnstableApi::class)
private fun enterFullScreen(playerView: PlayerView, context: Context, player: Player) {
    // Set the activity to landscape mode
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    val fullScreenPlayerView = FullScreenPlayerView(context)
//    if (fullScreenPlayerView.parent != null) {
//        (fullScreenPlayerView.parent as ViewGroup).removeView(fullScreenPlayerView)
//    }


    // Create a dialog with a full screen theme
    val fullScreenDialog =
        object : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Deprecated("Deprecated in Java")
            @OptIn(UnstableApi::class)
            override fun onBackPressed() {
                // Restore portrait mode when back button is pressed
                context.findActivity()?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                PlayerView.switchTargetView(player, fullScreenPlayerView, playerView)
                super.onBackPressed()
            }
        }

    // Add the full screen PlayerView to the dialog
    fullScreenDialog.addContentView(
        fullScreenPlayerView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )

    // Show the full screen dialog
    fullScreenDialog.show()

    // Switch player rendering to the full screen view
    PlayerView.switchTargetView(player, playerView, fullScreenPlayerView)
}

// Function to exit full screen mode
@OptIn(UnstableApi::class)
private fun exitFullScreen(player: Player, playerView: PlayerView, context: Context) {
    // Restore portrait mode
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    val fullScreenPlayerView = FullScreenPlayerView(context)


    // Switch player rendering back to the original view
    PlayerView.switchTargetView(player, fullScreenPlayerView, playerView)
}
