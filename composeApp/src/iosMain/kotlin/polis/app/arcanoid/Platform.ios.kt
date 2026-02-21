package polis.app.arcanoid

import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun saveHighScore(score: Int) {
    NSUserDefaults.standardUserDefaults.setInteger(score.toLong(), "high_score")
}
actual fun getHighScore(): Int {
    return NSUserDefaults.standardUserDefaults.integerForKey("high_score").toInt()
}