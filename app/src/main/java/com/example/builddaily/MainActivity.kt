package com.example.builddaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.builddaily.ui.navigation.BuildDailyNavigation
import com.example.builddaily.ui.theme.BuildDailyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuildDailyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BuildDailyNavigation()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    BuildDailyTheme {
        BuildDailyNavigation()
    }
}