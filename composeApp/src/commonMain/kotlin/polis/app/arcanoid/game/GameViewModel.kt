package polis.app.arcanoid.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import polis.app.arcanoid.currentTimeMillis

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    init {
        resetLevel()
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var lastTime = currentTimeMillis()
            while (true) {
                val currentTime = currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 1000f
                lastTime = currentTime

                _state.update { currentState ->
                    val nextState = PhysicsEngine.update(currentState, deltaTime)
                    if (nextState.status == GameStatus.GAME_OVER || nextState.status == GameStatus.WON) {
                        ScoreRepository.saveHighScore(nextState.score)
                    }
                    nextState.copy(highScore = ScoreRepository.getHighScore())
                }
                delay(16) // ~60 FPS
            }
        }
    }

    fun movePaddle(targetX: Float) {
        _state.update { it.copy(
            paddle = it.paddle.copy(x = targetX.coerceIn(0.1f, 0.9f)),
            paddleDirection = PaddleDirection.NONE
        ) }
    }

    fun setPaddleDirection(direction: PaddleDirection) {
        _state.update { it.copy(paddleDirection = direction) }
    }

    fun launchBall() {
        _state.update { 
            if (it.status == GameStatus.IDLE) {
                it.copy(
                    status = GameStatus.RUNNING,
                    ball = it.ball.copy(vx = 0.5f, vy = -0.5f)
                )
            } else {
                it
            }
        }
    }

    fun resetLevel() {
        val bricks = mutableListOf<Brick>()
        val rows = 5
        val cols = 8
        val brickWidth = 0.1f
        val brickHeight = 0.04f
        val padding = 0.01f

        val startX = (1f - (cols * (brickWidth + padding))) / 2
        val startY = 0.1f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                bricks.add(
                    Brick(
                        x = startX + col * (brickWidth + padding),
                        y = startY + row * (brickHeight + padding),
                        width = brickWidth,
                        height = brickHeight,
                        colorIndex = row
                    )
                )
            }
        }

        _state.update { 
            GameState(
                bricks = bricks,
                status = GameStatus.IDLE,
                highScore = ScoreRepository.getHighScore()
            )
        }
    }

}
