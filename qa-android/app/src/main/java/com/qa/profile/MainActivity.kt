package com.qa.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = SharedPrefsProfileRepository(applicationContext)
        setContent {
            MaterialTheme {
                Surface(color = Color.White) {
                    val vm: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(repo) as T
                    })
                    ProfileScreen(vm, onBack = { onBackPressedDispatcher.onBackPressed() })
                }
            }
        }
    }
}
