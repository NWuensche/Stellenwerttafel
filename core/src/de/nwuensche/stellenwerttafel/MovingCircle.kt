package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sign

//TODO Need to distiguish keep or not keep circle? (Sprite + Create new box2d-body or not?)
//keep = true if after movement circle should still be their (used when circle jumps back)
class MovingCircle(var currentPosition: Vector2, val endPosition: Vector2, val color: Color, val sR: ShapeRenderer, val keep: Boolean = false) : Drawable {
    private val completeDistance: Vector2 = endPosition.copy().sub(currentPosition)
    private val startAngleSign = endPosition.angle(currentPosition).sign.toInt()
    var atEnd = false
    override fun draw() {
        updatePosition()

        sR.run {
            this.begin(ShapeRenderer.ShapeType.Filled)
            this.circle(currentPosition.x,currentPosition.y,Constants.radius,100) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = this@MovingCircle.color
            this.circle(currentPosition.x,currentPosition.y,Constants.radius - (Constants.lineWidth * 0.5).toFloat(), 100) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = Constants.lineColor
            this.end()
        }
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
