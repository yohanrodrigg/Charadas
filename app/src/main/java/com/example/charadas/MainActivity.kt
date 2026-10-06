package com.example.charadas

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mantiene la pantalla encendida mientras se juega.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            CharadasTheme {
                CharadasApp(viewModel)
            }
        }
    }
}

@Composable
fun CharadasApp(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.phase != Phase.MENU) {
        viewModel.backToMenu()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .safeDrawingPadding()
    ) {
        when (state.phase) {
            Phase.MENU -> MenuScreen(onPlay = viewModel::startGame)
            Phase.COUNTDOWN -> CountdownScreen(state)
            Phase.PLAYING -> PlayingScreen(
                state = state,
                onAnswer = viewModel::answer
            )
            Phase.RESULTS -> ResultsScreen(
                state = state,
                onPlayAgain = { viewModel.startGame(state.category) },
                onMenu = viewModel::backToMenu
            )
        }
    }
}
