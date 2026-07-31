package com.ibader.citoyenpro.ui.citizen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ibader.citoyenpro.ui.theme.CitoyenProTheme

@Composable
fun CitizenReportsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Mes signalements — à venir", style = MaterialTheme.typography.headlineSmall)
    }
}

@Preview(name = "Mes signalements", showBackground = true)
@Composable
private fun CitizenReportsScreenPreview() {
    CitoyenProTheme {
        CitizenReportsScreen()
    }
}
