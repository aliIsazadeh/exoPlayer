package com.test.exoplayer.metadata_reader

import android.net.Uri
import androidx.media3.common.Metadata

interface MetaDataReader {
    fun getMetaDataFromUri(contentUri : Uri) : MetaData?
}