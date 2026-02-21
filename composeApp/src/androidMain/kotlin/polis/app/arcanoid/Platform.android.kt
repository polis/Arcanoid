package polis.app.arcanoid

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

private var mockHighScore = 0
actual fun saveHighScore(score: Int) { mockHighScore = score }
actual fun getHighScore(): Int = mockHighScore