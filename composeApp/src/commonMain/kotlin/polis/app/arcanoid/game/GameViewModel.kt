package polis.app.arcanoid.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import polis.app.arcanoid.currentTimeMillis
import kotlin.math.sqrt

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    init {
        resetLevel(1, resetStats = true)
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
                    
                    var finalState = nextState
                    
                    // If power-up collected (canShoot true but timer not set)
                    if (finalState.canShoot && finalState.shootingActiveUntil == 0L) {
                        finalState = finalState.copy(shootingActiveUntil = currentTime + 15000)
                    }
                    
                    // If power-up expires
                    if (finalState.canShoot && currentTime > finalState.shootingActiveUntil && finalState.shootingActiveUntil != 0L) {
                        finalState = finalState.copy(canShoot = false, shootingActiveUntil = 0L)
                    }

                    if (finalState.status == GameStatus.GAME_OVER || finalState.status == GameStatus.WON) {
                        ScoreRepository.saveHighScore(finalState.score)
                    }
                    finalState.copy(highScore = ScoreRepository.getHighScore())
                }
                delay(16) // ~60 FPS
            }
        }
    }

    fun fireProjectile() {
        _state.update { 
            if (it.canShoot && it.status != GameStatus.WON && it.status != GameStatus.GAME_OVER) {
                val newProjectiles = it.projectiles.toMutableList()
                newProjectiles.add(Projectile(x = it.paddle.x - it.paddle.width / 4, y = 0.85f))
                newProjectiles.add(Projectile(x = it.paddle.x + it.paddle.width / 4, y = 0.85f))
                it.copy(projectiles = newProjectiles)
            } else {
                it
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

    fun resetLevel(levelIndex: Int = _state.value.level, resetStats: Boolean = false) {
        val bricks = when (levelIndex) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            else -> createLevel1() // Default
        }

        _state.update { 
            GameState(
                bricks = bricks,
                level = levelIndex,
                status = GameStatus.IDLE,
                highScore = ScoreRepository.getHighScore(),
                score = if (resetStats) 0 else it.score,
                lives = if (resetStats) 3 else it.lives
            )
        }
    }

    private fun createLevel3(): List<Brick> {
        val bricks = mutableListOf<Brick>()
        val rows = 8
        val cols = 8
        val brickWidth = 0.1f
        val brickHeight = 0.04f
        val padding = 0.01f

        val startX = (1f - (cols * (brickWidth + padding))) / 2
        val startY = 0.1f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                // Circular/Diamond pattern
                val centerX = 3.5f
                val centerY = 3.5f
                val dist = kotlin.math.sqrt((col - centerX) * (col - centerX) + (row - centerY) * (row - centerY))
                
                if (dist < 4) {
                    bricks.add(
                        Brick(
                            x = startX + col * (brickWidth + padding),
                            y = startY + row * (brickHeight + padding),
                            width = brickWidth,
                            height = brickHeight,
                            colorIndex = row % 5,
                            health = if (dist < 2) 3 else 1
                        )
                    )
                }
            }
        }
        return bricks
    }

    private fun createLevel1(): List<Brick> {
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
                        colorIndex = row,
                        health = 1
                    )
                )
            }
        }
        return bricks
    }

    private fun createLevel2(): List<Brick> {
        val bricks = mutableListOf<Brick>()
        val rows = 7
        val cols = 8
        val brickWidth = 0.1f
        val brickHeight = 0.04f
        val padding = 0.01f

        val startX = (1f - (cols * (brickWidth + padding))) / 2
        val startY = 0.1f

        for (row in 0 until rows) {
            val colsInRow = if (row % 2 == 0) cols else cols - 1
            val rowOffset = if (row % 2 == 0) 0f else (brickWidth + padding) / 2
            
            for (col in 0 until colsInRow) {
                val health = if (row < 2) 2 else 1
                bricks.add(
                    Brick(
                        x = startX + col * (brickWidth + padding) + rowOffset,
                        y = startY + row * (brickHeight + padding),
                        width = brickWidth,
                        height = brickHeight,
                        colorIndex = row % 5,
                        health = health
                    )
                )
            }
        }
        return bricks
    }

    fun nextLevel() {
        val currentLevel = _state.value.level
        resetLevel(currentLevel + 1)
    }

}
