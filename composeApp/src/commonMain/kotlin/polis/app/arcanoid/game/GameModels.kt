package polis.app.arcanoid.game

data class Paddle(
    val x: Float, // Center X (0.0 to 1.0)
    val width: Float = 0.2f,
    val height: Float = 0.02f
)

data class Ball(
    val x: Float,
    val y: Float,
    val radius: Float = 0.015f,
    val vx: Float,
    val vy: Float
)

data class Brick(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isDestroyed: Boolean = false,
    val colorIndex: Int = 0
)

enum class GameStatus {
    IDLE, RUNNING, PAUSED, GAME_OVER, WON
}

enum class PaddleDirection {
    NONE, LEFT, RIGHT
}

data class GameState(
    val paddle: Paddle = Paddle(x = 0.5f),
    val paddleDirection: PaddleDirection = PaddleDirection.NONE,
    val ball: Ball = Ball(x = 0.5f, y = 0.8f, vx = 0f, vy = 0f),
    val bricks: List<Brick> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val lives: Int = 3,
    val status: GameStatus = GameStatus.IDLE
)
