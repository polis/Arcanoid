package polis.app.arcanoid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import polis.app.arcanoid.game.GameState
import polis.app.arcanoid.game.GameStatus

@Composable
fun ArcanoidCanvas(
    state: GameState,
    onPaddleMove: (Float) -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brickColors = listOf(
        Color(0xFFFF5252),
        Color(0xFFFF4081),
        Color(0xFFE040FB),
        Color(0xFF7C4DFF),
        Color(0xFF536DFE)
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val relativeX = change.position.x / size.width
                    onPaddleMove(relativeX)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onLaunch()
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Draw Bricks
        state.bricks.forEach { brick ->
            if (!brick.isDestroyed) {
                val color = brickColors.getOrElse(brick.colorIndex) { Color.Gray }
                
                // Draw main brick
                drawRoundRect(
                    color = color,
                    topLeft = Offset(brick.x * w, brick.y * h),
                    size = Size(brick.width * w, brick.height * h),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // If health > 1, draw a border or indicator
                if (brick.health > 1) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.5f),
                        topLeft = Offset(brick.x * w, brick.y * h),
                        size = Size(brick.width * w, brick.height * h),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }

        // Draw PowerUps
        state.powerUps.forEach { pu ->
            drawRoundRect(
                color = Color(0xFFFF9800), // Orange
                topLeft = Offset((pu.x - pu.width / 2) * w, pu.y * h),
                size = Size(pu.width * w, pu.height * h),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Optional: Draw a "P" inside? Or just keep it orange.
        }

        // Draw Projectiles
        state.projectiles.forEach { proj ->
            drawRect(
                color = Color.White,
                topLeft = Offset(proj.x * w, proj.y * h),
                size = Size(0.01f * w, 0.02f * h)
            )
        }

        // Draw Paddle
        val paddle = state.paddle
        val paddleColor = if (state.canShoot) Color(0xFFFF5252) else Color(0xFF2196F3)
        drawRoundRect(
            color = paddleColor,
            topLeft = Offset((paddle.x - paddle.width / 2) * w, 0.9f * h - (paddle.height / 2) * h),
            size = Size(paddle.width * w, paddle.height * h),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Draw Ball
        val ball = state.ball
        drawCircle(
            color = Color.White,
            center = Offset(ball.x * w, ball.y * h),
            radius = ball.radius * w
        )

        // Overlay text based on status
        if (state.status == GameStatus.IDLE) {
            // "Tap to Start" would be drawn here or via Composable UI
        }
    }
}
