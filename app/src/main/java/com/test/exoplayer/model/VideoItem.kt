package com.test.exoplayer.model

import android.net.Uri
import androidx.media3.common.MediaItem

data class VideoItem(
    val contentUris: Uri,
    val mediaItem : MediaItem ,
    val name : String ,
)
