package polis.app.arcanoid.game

object PhysicsEngine {
    fun update(state: GameState, deltaTime: Float): GameState {
        if (state.status != GameStatus.RUNNING && state.status != GameStatus.IDLE) return state

        // 1. Update Paddle position
        val paddleSpeed = 1.0f // 1 field width per second
        var paddleX = state.paddle.x
        when (state.paddleDirection) {
            PaddleDirection.LEFT -> paddleX -= paddleSpeed * deltaTime
            PaddleDirection.RIGHT -> paddleX += paddleSpeed * deltaTime
            PaddleDirection.NONE -> {}
        }
        paddleX = paddleX.coerceIn(state.paddle.width / 2, 1f - state.paddle.width / 2)
        val nextPaddle = state.paddle.copy(x = paddleX)

        // 2. Initial state setup
        var vx = state.ball.vx
        var vy = state.ball.vy
        var x = if (state.status == GameStatus.IDLE) nextPaddle.x else state.ball.x + vx * deltaTime
        var y = if (state.status == GameStatus.IDLE) state.ball.y else state.ball.y + vy * deltaTime
        var score = state.score
        var lives = state.lives
        var status = state.status
        var canShoot = state.canShoot

        // 3. Ball movement & Wall collisions (only if not IDLE)
        if (status == GameStatus.RUNNING) {
            // Wall collisions
            if (x - state.ball.radius < 0) {
                x = state.ball.radius
                vx = -vx
            } else if (x + state.ball.radius > 1f) {
                x = 1f - state.ball.radius
                vx = -vx
            }

            if (y - state.ball.radius < 0) {
                y = state.ball.radius
                vy = -vy
            } else if (y + state.ball.radius > 1f) {
                // Ball lost
                lives--
                if (lives <= 0) {
                    status = GameStatus.GAME_OVER
                } else {
                    return state.copy(
                        ball = Ball(x = 0.5f, y = 0.8f, vx = 0f, vy = 0f),
                        paddle = Paddle(x = 0.5f),
                        lives = lives,
                        status = GameStatus.IDLE,
                        projectiles = state.projectiles, // Preserve projectiles!
                        powerUps = state.powerUps
                    )
                }
            }

            // Paddle collision
            val paddle = nextPaddle
            if (y + state.ball.radius > 0.9f - paddle.height / 2 &&
                y - state.ball.radius < 0.9f + paddle.height / 2 &&
                x + state.ball.radius > paddle.x - paddle.width / 2 &&
                x - state.ball.radius < paddle.x + paddle.width / 2
            ) {
                vy = -vy
                y = 0.9f - paddle.height / 2 - state.ball.radius
                val hitPos = (x - paddle.x) / (paddle.width / 2)
                vx += hitPos * 0.5f
            }
        }

        // 4. Update PowerUps (Always falling)
        val nextPowerUps = state.powerUps.map { it.copy(y = it.y + 0.3f * deltaTime) }
            .filter { it.y < 1.0f }
            .toMutableList()

        // 5. Power-up collection
        val powerUpIterator = nextPowerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val pu = powerUpIterator.next()
            if (pu.y + pu.height > 0.9f - nextPaddle.height / 2 &&
                pu.y < 0.9f + nextPaddle.height / 2 &&
                pu.x + pu.width > nextPaddle.x - nextPaddle.width / 2 &&
                pu.x < nextPaddle.x + nextPaddle.width / 2
            ) {
                if (pu.type == PowerUpType.FIRE) {
                    canShoot = true
                }
                powerUpIterator.remove()
            }
        }

        // 6. Update Projectiles (Always moving)
        var nextProjectiles = state.projectiles.map { it.copy(y = it.y + it.vy * deltaTime) }
            .filter { it.y > 0 }
            .toMutableList()

        // 7. Projectile vs Brick collision
        val bricksAfterProjectiles = state.bricks.toMutableList()
        val projectileIterator = nextProjectiles.iterator()
        while (projectileIterator.hasNext()) {
            val proj = projectileIterator.next()
            var projectileHit = false
            for (i in bricksAfterProjectiles.indices) {
                val brick = bricksAfterProjectiles[i]
                if (!brick.isDestroyed &&
                    proj.x > brick.x && proj.x < brick.x + brick.width &&
                    proj.y > brick.y && proj.y < brick.y + brick.height
                ) {
                    val newHealth = brick.health - 1
                    bricksAfterProjectiles[i] = brick.copy(
                        health = newHealth,
                        isDestroyed = newHealth <= 0
                    )
                    score += 10
                    projectileHit = true
                    break
                }
            }
            if (projectileHit) projectileIterator.remove()
        }

        // 8. Brick collisions (Ball, only if running)
        val finalBricks = bricksAfterProjectiles.toMutableList()
        if (status == GameStatus.RUNNING) {
            for (i in finalBricks.indices) {
                val brick = finalBricks[i]
                if (!brick.isDestroyed) {
                    if (x + state.ball.radius > brick.x &&
                        x - state.ball.radius < brick.x + brick.width &&
                        y + state.ball.radius > brick.y &&
                        y - state.ball.radius < brick.y + brick.height
                    ) {
                        val newHealth = brick.health - 1
                        finalBricks[i] = brick.copy(
                          health = newHealth,
                          isDestroyed = newHealth <= 0
                        )
                        vy = -vy
                        score += 10
                        if (finalBricks[i].isDestroyed && (0..9).random() < 2) {
                            nextPowerUps.add(
                                PowerUp(
                                    x = brick.x + brick.width / 2,
                                    y = brick.y + brick.height / 2,
                                    type = PowerUpType.FIRE
                                )
                            )
                        }
                        break
                    }
                }
            }
        }

        if (finalBricks.all { it.isDestroyed }) {
            status = GameStatus.WON
        }

        return state.copy(
            ball = state.ball.copy(x = x, y = y, vx = vx, vy = vy),
            paddle = nextPaddle,
            bricks = finalBricks,
            powerUps = nextPowerUps,
            projectiles = nextProjectiles,
            score = score,
            lives = lives,
            status = status,
            canShoot = canShoot
        )
    }
}
