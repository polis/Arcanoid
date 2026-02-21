package polis.app.arcanoid.game

import polis.app.arcanoid.getHighScore
import polis.app.arcanoid.saveHighScore

object ScoreRepository {
    fun saveHighScore(score: Int) {
        val currentMax = getHighScore()
        if (score > currentMax) {
            polis.app.arcanoid.saveHighScore(score)
        }
    }

    fun getHighScore(): Int {
        return polis.app.arcanoid.getHighScore()
    }
}
