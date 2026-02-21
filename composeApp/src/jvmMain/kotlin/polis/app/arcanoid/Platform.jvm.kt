package polis.app.arcanoid

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun saveHighScore(score: Int) {
    java.util.prefs.Preferences.userRoot().node("Arcanoid").putInt("high_score", score)
}
actual fun getHighScore(): Int {
    return java.util.prefs.Preferences.userRoot().node("Arcanoid").getInt("high_score", 0)
}