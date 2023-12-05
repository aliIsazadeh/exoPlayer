package com.test.exoplayer.front

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.test.exoplayer.front.viewModel.MainViewModel
import com.test.exoplayer.findActivity

@Composable
fun VideoScreen(videoModel: MainViewModel = hiltViewModel<MainViewModel>()) {


    Surface(modifier = Modifier.fillMaxSize()) {


        val videoItems by videoModel.videoItems.collectAsState()
        val selectVideoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                uri?.let(videoModel::addVideoUri)
            }
        )
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
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).also {
                        it.player = videoModel.player
                        it.setFullscreenButtonClickListener { isFullScreen ->
                            if (isFullScreen) {
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
            IconButton(onClick = { selectVideoLauncher.launch("video/mp4") }) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Select video"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(videoItems) { item ->
                    Text(text = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { videoModel.playVideo(item.contentUris) }
                            .padding(16.dp))
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
    if (fullScreenPlayerView.parent != null) {
        (fullScreenPlayerView.parent as ViewGroup).removeView(fullScreenPlayerView)
    }


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
@OptIn(UnstableApi::class) private fun exitFullScreen(player: Player, playerView: PlayerView, context: Context) {
    // Restore portrait mode
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    val fullScreenPlayerView = FullScreenPlayerView(context)


    // Switch player rendering back to the original view
    PlayerView.switchTargetView(player, fullScreenPlayerView, playerView)
}
