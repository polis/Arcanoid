package polis.app.arcanoid

import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun dateNow(): Double

actual fun currentTimeMillis(): Long = dateNow().toLong()

actual fun saveHighScore(score: Int) {
    window.localStorage.setItem("high_score", score.toString())
}
actual fun getHighScore(): Int {
    return window.localStorage.getItem("high_score")?.toIntOrNull() ?: 0
}