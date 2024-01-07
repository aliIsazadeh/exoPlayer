package com.test.exoplayer.front

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectResource(
    onSelectLocal: () -> Unit,
    onSelectRemote: (String) -> Unit,
    onCancel: () -> Unit,
    selectUrl: (String) -> Unit,
    url: String
) {

    Dialog(onDismissRequest = { onCancel() }) {

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(value = url,
                    onValueChange = { selectUrl(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "search icon"
                        )
                    },
                    label = { Text(text = "https://enter.your.image") })

                Row (modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.spacedBy(16.dp) ){

                    Button(onClick = {onSelectRemote(url)} , enabled = url.isNotBlank() , modifier = Modifier.padding(8.dp)) {
                        Text(text = "Use selected url")
                    }

                    Button(onClick = {onSelectLocal()}  , modifier = Modifier.padding(8.dp)) {
                        Text(text = "Choose from existing files")
                    }

                }

            }

        }

    }

}