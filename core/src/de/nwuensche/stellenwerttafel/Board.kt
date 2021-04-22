package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*

typealias Circle = Vector2
class Board(val sR: ShapeRenderer, val world: World): Drawable {
    val circles = arrayListOf<Fixture>()

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch on nothing
    }
    private var dragState = DragState.NONE
    var draggedCircle: Fixture? = null


    override fun draw() {
        sR.drawLine(Vector2(Constants.firstLineBorderWidth, 0f), Vector2(Constants.firstLineBorderWidth, Constants.height))
        sR.drawLine(Vector2(Constants.secondLineBorderWidth, 0f), Vector2(Constants.secondLineBorderWidth, Constants.height))

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
            circles.add(fixture)
        }

        //reset
        draggedCircle = null
        dragState = DragState.NONE
    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        if (draggedCircle != null) {
            draggedCircle?.body?.setTransform(screenXNormalized, screenYNormalized, 0f)
            return
        }
        
        for (circle in circles.asReversed()) { //TODO Might be slow, because really reverse list
            if (((circle.body.position.x - Constants.radius) <= screenXNormalized)
                    && (screenXNormalized <= (circle.body.position.x + Constants.radius))
                    && ((circle.body.position.y - Constants.radius) <= screenYNormalized)
                    && (screenYNormalized <= (circle.body.position.y + Constants.radius))) {
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

fun ShapeRenderer.drawCircle(c: Fixture) {
    val pos = c.body.position
    this.begin(ShapeRenderer.ShapeType.Filled)
    this.circle(pos.x,pos.y,Constants.radius,100) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = Constants.circleRedColor
    this.circle(pos.x,pos.y,Constants.radius - Constants.lineWidth, 100) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = Constants.lineColor
    this.end() //TODO I should only open shaperenderer once, because explensive: https://stackoverflow.com/questions/29035553/trying-to-draw-a-circle-in-libgdx
}