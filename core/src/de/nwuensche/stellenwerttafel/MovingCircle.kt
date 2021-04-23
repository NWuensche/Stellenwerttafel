package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sign

//TODO Need to distiguish keep or not keep circle? (Sprite + Create new box2d-body or not?)
class MovingCircle(var currentPosition: Vector2, val endPosition: Vector2, color: Color, sR: ShapeRenderer) : Drawable {
    private val completeDistance: Vector2 = endPosition.copy().sub(currentPosition)
    private val startAngleSign = endPosition.angleDeg(currentPosition).sign.toInt()
    override fun draw() {
        updatePosition()
    }

    //INFO Do Vector Subtraction here to get vector from end to beginning
    fun updatePosition() {
        val new = currentPosition.copy().mulAdd(completeDistance, speedFactor * Gdx.graphics.deltaTime)
        val newAngleSign = endPosition.angleDeg(new).sign.toInt()
        //INFO 0 iff same vector, != iff "behind" end position
        if (newAngleSign != startAngleSign) {
            currentPosition = endPosition
        } else {
            currentPosition = new
        }
    }
}

//INFO Because sub really subtracts from the vector
fun Vector2.copy(): Vector2 {
    return Vector2(x, y)
}
