package polis.app.arcanoid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import polis.app.arcanoid.game.GameViewModel
import polis.app.arcanoid.game.GameStatus

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.Key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel { GameViewModel() }) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    MaterialTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown) {
                        when (it.key) {
                            Key.DirectionLeft, Key.A -> {
                                viewModel.movePaddle(state.paddle.x - 0.05f)
                                true
                            }
                            Key.DirectionRight, Key.D -> {
                                viewModel.movePaddle(state.paddle.x + 0.05f)
                                true
                            }
                            Key.Spacebar -> {
                                viewModel.launchBall()
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
        ) {
            val isWide = maxWidth > maxHeight * 1.5f

            Row(modifier = Modifier.fillMaxSize()) {
                if (isWide) {
                    // Left panel
                    SidePanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = "ARCANOID",
                        score = state.score,
                        highScore = state.highScore,
                        lives = state.lives
                    )
                }

                // Game Field
                Box(
                    modifier = Modifier
                        .weight(if (isWide) 2f else 1f)
                        .fillMaxHeight()
                        .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                        .background(Color.Black)
                        .align(Alignment.CenterVertically)
                ) {
                    ArcanoidCanvas(
                        state = state,
                        onPaddleMove = { viewModel.movePaddle(it) },
                        onLaunch = { 
                            if (state.status == GameStatus.GAME_OVER || state.status == GameStatus.WON) {
                                viewModel.resetLevel()
                            } else {
                                viewModel.launchBall()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

            if (state.status == GameStatus.IDLE) {
                Text(
                    "Tap to Launch",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.status == GameStatus.GAME_OVER) {
                Text(
                    "GAME OVER\nScore: ${state.score}\nTap to Restart",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.status == GameStatus.WON) {
                Text(
                    "YOU WON!\nScore: ${state.score}\nTap for Next Level",
                    color = Color.Green,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
                }

                if (isWide) {
                    // Right panel
                    SidePanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        title = "NEXT LEVELS",
                        score = null,
                        lives = null
                    )
                }
            }
        }
    }
}

@Composable
fun SidePanel(
    modifier: Modifier,
    title: String,
    score: Int?,
    highScore: Int? = null,
    lives: Int?
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(Color(0xFF1E1E1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
        if (score != null) {
            Spacer(Modifier.height(32.dp))
            Text("SCORE: $score", color = Color.Yellow)
        }
        if (highScore != null) {
            Text("BEST: $highScore", color = Color.Green)
        }
        if (lives != null) {
            Spacer(Modifier.height(16.dp))
            Text("LIVES: $lives", color = Color.Cyan)
        }
    }
}
