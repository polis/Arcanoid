package polis.app.arcanoid

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long

expect fun saveHighScore(score: Int)
expect fun getHighScore(): Int