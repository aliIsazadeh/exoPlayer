package com.test.exoplayer.front.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.test.exoplayer.model.VideoItem
import com.test.exoplayer.metadata_reader.MetaDataReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val player: Player,
    private val metaDataReader: MetaDataReader
) : ViewModel() {


    private val videoUris = savedStateHandle.getStateFlow("videoUris", emptyList<Uri>())


    val videoItems = videoUris.map { uris ->
        uris.map { uri ->
            VideoItem(
                contentUris = uri,
                mediaItem = MediaItem.fromUri(uri),
                name = metaDataReader.getMetaDataFromUri(uri)?.fileName ?: "No Name"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isFullScreen by mutableStateOf(false)

    init {
        player.prepare()
    }

    fun addVideoUri(uri: Uri) {
        savedStateHandle["videoUris"] = videoUris.value + uri
        player.addMediaItem(MediaItem.fromUri(uri))
    }


    fun addVideoUrl(url: String) {
        savedStateHandle["videoUris"] = videoUris.value + Uri.parse(url)
        player.addMediaItem(MediaItem.fromUri( url))
    }

    val urlText = mutableStateOf("")

    fun selectUrl(text: String) {
        urlText.value = text
    }


    fun playVideo(uri: Uri) {
        player.setMediaItem(
            videoItems.value.find { it.contentUris == uri }?.mediaItem ?: return
        )
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }


}