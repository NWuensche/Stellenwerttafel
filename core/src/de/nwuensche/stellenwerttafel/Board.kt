package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*

typealias Circle = Vector2
class Board(val sR: ShapeRenderer, val world: World): Drawable {
    val circles = arrayListOf<Circle>()

    //Cache
    val firstLineWidth = Constants.width / 3f
    val secondLineWidth = (2 / 3f) * Constants.width

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch on nothing
    }
    private var dragState = DragState.NONE
    var draggedCircle: Circle? = null


    override fun draw() {
        sR.drawLine(Vector2(firstLineWidth, 0f), Vector2(firstLineWidth, Constants.height))
        sR.drawLine(Vector2(secondLineWidth, 0f), Vector2(secondLineWidth, Constants.height))

        for (circle in circles) {
            sR.drawCircle(circle)
        }
    }


    val circleDef = BodyDef().apply { this.type = BodyDef.BodyType.DynamicBody }
    val circleShape = CircleShape().apply { this.radius = Constants.radius }
    val fixtureDef = FixtureDef().apply {
        this.shape = circleShape
        this.density = 0.2f
        this.friction = 0.1f
        this.restitution = 0.6f
    }

    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        if (dragState != DragState.DRAGCIRCLE) { // As long as not moved circle, create new one
            //TODO IN circles.add(Circle(screenX.toFloat(), screenY.toFloat()))
            circleDef.position.set(screenX.toFloat() * Constants.convertRatio, screenY.toFloat() * Constants.convertRatio)
            val body: Body = world.createBody(circleDef)
            val fixture = body.createFixture(fixtureDef) //TODO dispose fixture when circle merged or deleted
        }

        //reset
        draggedCircle = null
        dragState = DragState.NONE
    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        if (draggedCircle != null) {
            draggedCircle?.x = screenX.toFloat()
            draggedCircle?.y = screenY.toFloat()
            return
        }

        //Start with 'highest' circle (thus reverse list), find highest circle which is below touch event
        for (circle in circles.asReversed()) { //TODO Might be slow, because really reverse list
            if (((circle.x - Constants.radius) <= screenX)
                    && (screenX <= (circle.x + Constants.radius))
                    && ((circle.y - Constants.radius) <= screenY)
                    && (screenY <= (circle.y + Constants.radius))) {
                draggedCircle = circle
                break
            }
        }
        dragState = if (draggedCircle != null) DragState.DRAGCIRCLE else DragState.DRAGNOTHING
    }
}

fun ShapeRenderer.drawLine(v1: Vector2, v2: Vector2) {
    this.begin(ShapeRenderer.ShapeType.Line)
    this.line(v1,v2)
    this.end()
}

fun ShapeRenderer.drawCircle(m: Circle) {
    this.begin(ShapeRenderer.ShapeType.Filled)
    this.circle(m.x,m.y,Constants.radius)
    this.color = Constants.circleRedColor
    this.circle(m.x,m.y,Constants.radius - Constants.lineWidthNormalized)
    this.color = Constants.lineColor
    this.end() //TODO I should only open shaperenderer once, because explensive: https://stackoverflow.com/questions/29035553/trying-to-draw-a-circle-in-libgdx
}