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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.lifecycle.viewmodel.compose.viewModel
import polis.app.arcanoid.game.GameViewModel
import polis.app.arcanoid.game.GameStatus
import polis.app.arcanoid.game.PaddleDirection

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
                // Only pad top/bottom to avoid status bar overlap while keeping full width
                .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Vertical)) 
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent {
                    when (it.type) {
                        KeyEventType.KeyDown -> {
                            when (it.key) {
                                Key.DirectionLeft, Key.A -> {
                                    viewModel.setPaddleDirection(PaddleDirection.LEFT)
                                    true
                                }
                                Key.DirectionRight, Key.D -> {
                                    viewModel.setPaddleDirection(PaddleDirection.RIGHT)
                                    true
                                }
                                Key.Spacebar -> {
                                    viewModel.launchBall()
                                    true
                                }
                                else -> false
                            }
                        }
                        KeyEventType.KeyUp -> {
                            when (it.key) {
                                Key.DirectionLeft, Key.A -> {
                                    if (state.paddleDirection == PaddleDirection.LEFT) {
                                        viewModel.setPaddleDirection(PaddleDirection.NONE)
                                    }
                                    true
                                }
                                Key.DirectionRight, Key.D -> {
                                    if (state.paddleDirection == PaddleDirection.RIGHT) {
                                        viewModel.setPaddleDirection(PaddleDirection.NONE)
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
        ) {
            val isPortrait = maxHeight > maxWidth

            if (isPortrait) {
                // Portrait Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Info Bar
                    InfoBar(
                        score = state.score,
                        highScore = state.highScore,
                        lives = state.lives,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 8.dp)
                    )

                    // Game Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp, vertical = 8.dp) // Shrink the window
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f, matchHeightConstraintsFirst = false)
                            .background(Color.Black)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        GameWindow(state, viewModel)
                    }
                    
                    // Bottom spacing
                    Box(modifier = Modifier.weight(0.2f))
                }
            } else {
                // Landscape Layout
                val isWide = maxWidth > maxHeight * 1.5f
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isWide) {
                        SidePanel(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "ARCANOID",
                            score = state.score,
                            highScore = state.highScore,
                            lives = state.lives
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(if (isWide) 2f else 1f)
                            .fillMaxHeight()
                            .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                            .background(Color.Black)
                            .align(Alignment.CenterVertically)
                    ) {
                        GameWindow(state, viewModel)
                    }

                    if (isWide) {
                        SidePanel(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "NEXT LEVELS",
                            score = null,
                            lives = null
                        )
                    } else {
                        // Slim info panel for landscape but not wide
                        SidePanel(
                            modifier = Modifier.width(120.dp).fillMaxHeight(),
                            title = "STATS",
                            score = state.score,
                            highScore = state.highScore,
                            lives = state.lives
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameWindow(state: polis.app.arcanoid.game.GameState, viewModel: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.status == GameStatus.WON) {
            Text(
                "YOU WON!\nScore: ${state.score}\nTap for Next Level",
                color = Color.Green,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun InfoBar(
    score: Int,
    highScore: Int,
    lives: Int,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Scores Column (Left)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "SCORE: $score", 
                color = Color.Yellow, 
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                "BEST: $highScore", 
                color = Color.Green, 
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }

        // Title (Center)
        Text(
            "ARCANOID", 
            color = Color.White, 
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )

        // Lives (Right)
        Text(
            "LIVES: $lives", 
            color = Color.Cyan, 
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1
        )
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
