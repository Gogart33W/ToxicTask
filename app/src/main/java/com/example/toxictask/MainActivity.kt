package com.example.toxictask

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toxictask.ui.theme.ToxicTaskTheme
import com.example.toxictask.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        setContent {
            val viewModel: TaskViewModel = viewModel()
            val currentLang by viewModel.language.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val tasks by viewModel.tasks.collectAsState()

            if (tasks == null) {
                // Initial loading state to prevent flickering
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Or a background color that matches the theme
                ) {}
            } else {
                LocaleWrapper(currentLang.code) {
                    ToxicTaskTheme(themeMode = themeMode) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            ToxicTaskScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
