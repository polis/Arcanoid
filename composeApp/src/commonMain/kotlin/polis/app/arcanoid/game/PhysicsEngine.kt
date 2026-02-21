package polis.app.arcanoid.game

import kotlin.math.max
import kotlin.math.min

object PhysicsEngine {
    fun update(state: GameState, deltaTime: Float): GameState {
        if (state.status != GameStatus.RUNNING) return state

        var nextBall = state.ball.copy(
            x = state.ball.x + state.ball.vx * deltaTime,
            y = state.ball.y + state.ball.vy * deltaTime
        )

        var vx = nextBall.vx
        var vy = nextBall.vy
        var x = nextBall.x
        var y = nextBall.y
        var score = state.score
        var lives = state.lives
        var status = state.status

        // Wall collisions
        if (x - nextBall.radius < 0) {
            x = nextBall.radius
            vx = -vx
        } else if (x + nextBall.radius > 1f) {
            x = 1f - nextBall.radius
            vx = -vx
        }

        if (y - nextBall.radius < 0) {
            y = nextBall.radius
            vy = -vy
        } else if (y + nextBall.radius > 1f) {
            // Ball lost
            lives--
            if (lives <= 0) {
                status = GameStatus.GAME_OVER
            } else {
                return state.copy(
                    ball = Ball(x = 0.5f, y = 0.8f, vx = 0f, vy = 0f),
                    paddle = Paddle(x = 0.5f),
                    lives = lives,
                    status = GameStatus.IDLE
                )
            }
        }

        // Paddle collision
        val paddle = state.paddle
        if (y + nextBall.radius > 0.9f - paddle.height / 2 &&
            y - nextBall.radius < 0.9f + paddle.height / 2 &&
            x + nextBall.radius > paddle.x - paddle.width / 2 &&
            x - nextBall.radius < paddle.x + paddle.width / 2
        ) {
            vy = -vy
            y = 0.9f - paddle.height / 2 - nextBall.radius
            // Add spin based on where it hit the paddle
            val hitPos = (x - paddle.x) / (paddle.width / 2)
            vx += hitPos * 0.5f
        }

        // Brick collisions
        val nextBricks = state.bricks.toMutableList()
        for (i in nextBricks.indices) {
            val brick = nextBricks[i]
            if (!brick.isDestroyed) {
                if (x + nextBall.radius > brick.x &&
                    x - nextBall.radius < brick.x + brick.width &&
                    y + nextBall.radius > brick.y &&
                    y - nextBall.radius < brick.y + brick.height
                ) {
                    nextBricks[i] = brick.copy(isDestroyed = true)
                    vy = -vy
                    score += 10
                    break // Collide only with one brick per frame
                }
            }
        }

        if (nextBricks.all { it.isDestroyed }) {
            status = GameStatus.WON
        }

        return state.copy(
            ball = nextBall.copy(x = x, y = y, vx = vx, vy = vy),
            bricks = nextBricks,
            score = score,
            lives = lives,
            status = status
        )
    }
}
