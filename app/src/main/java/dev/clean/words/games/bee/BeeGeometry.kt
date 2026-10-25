package dev.clean.words.games.bee

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class BeeCellPosition(val x: Float, val y: Float)

/** Center + polygon corners: triangle has 4 letters, square 5, hexagon 7. */
object BeeGeometry {
    fun positions(sides: Int): List<BeeCellPosition> {
        require(sides in 3..8)
        return listOf(BeeCellPosition(.5f, .5f)) + List(sides) { index ->
            val angle = index * 2 * PI / sides
            BeeCellPosition(.5f + .31f * cos(angle).toFloat(), .5f + .31f * sin(angle).toFloat())
        }
    }

    fun diameter(sides: Int): Float {
        require(sides in 3..8)
        return minOf(.38f, (.67 * sin(PI / sides)).toFloat())
    }
}
