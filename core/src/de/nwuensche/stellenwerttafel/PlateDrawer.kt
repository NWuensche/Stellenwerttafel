package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture


// INFO Draw Inner Circle with Border
interface PlateDrawer {
    //TODO DSL
/*    fun drawPlate(x: Float, y: Float)

    fun drawPlate(v: Vector2) {
        drawCircle(v.x, v.y)
    }*/

    fun begin()
    fun end()

    fun drawPlate(v: Vector2, color: Color)

    fun drawPlate(f: Fixture)
}