package polis.app.arcanoid

import kotlinx.browser.window
import kotlin.js.Date

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun currentTimeMillis(): Long = Date.now().toLong()

actual fun saveHighScore(score: Int) {
    window.localStorage.setItem("high_score", score.toString())
}

actual fun getHighScore(): Int {
    return window.localStorage.getItem("high_score")?.toIntOrNull() ?: 0
}