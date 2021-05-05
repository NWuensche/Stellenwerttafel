package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sign

//keep = true if after movement circle should still be their (used when circle jumps back)
class FlyingCircle(var currentPosition: Vector2, val endPosition: Vector2, val color: Color, val pD: PlateDrawer, val keep: Boolean = false) : Drawable {
    private val completeDistance: Vector2 = endPosition.copy().sub(currentPosition)
    private val startAngleSign = endPosition.angle(currentPosition).sign.toInt()
    var atEnd = false
    override fun draw() {
        updatePosition()
        pD.drawPlate(currentPosition, color)
    }

    //returns true iff circle at end destination
    fun drawAndFinished(): Boolean {
        this.draw()
        return atEnd
    }

    //INFO Do Vector Subtraction here to get vector from end to beginning
    fun updatePosition() {
        val new = currentPosition.copy().mulAdd(completeDistance, Constants.speedFactor * Gdx.graphics.deltaTime)
        val newAngleSign = endPosition.angle(new).sign.toInt()
        //INFO 0 iff same vector, != iff "behind" end position
        if (newAngleSign != startAngleSign) {
            currentPosition = endPosition
            atEnd = true
            //Cant do that if currently in foreach-loop in Board.draw: movingCircles.remove(this)
        } else {
            currentPosition = new
        }
    }
}

//INFO Because sub really subtracts from the vector
fun Vector2.copy(): Vector2 {
    return Vector2(x, y)
}
