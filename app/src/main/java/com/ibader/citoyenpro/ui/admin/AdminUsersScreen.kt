package com.ibader.citoyenpro.ui.admin

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
fun AdminUsersScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Utilisateurs (admin) — à venir", style = MaterialTheme.typography.headlineSmall)
    }
}

@Preview(name = "Utilisateurs admin", showBackground = true)
@Composable
private fun AdminUsersScreenPreview() {
    CitoyenProTheme {
        AdminUsersScreen()
    }
}
