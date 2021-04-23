package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*

class Board(val sR: ShapeRenderer, val world: World) : Drawable {
    val circles = arrayListOf<Fixture>()
    var movingCircles: MutableList<MovingCircle> = arrayListOf<MovingCircle>()

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch on nothing
    }
    private var dragState = DragState.NONE
    var draggedCircle: Fixture? = null


    override fun draw() {
        sR.drawLine(Vector2(Constants.firstLineBorderX, 0f), Vector2(Constants.firstLineBorderX, Constants.height))
        sR.drawLine(Vector2(Constants.secondLineBorderX, 0f), Vector2(Constants.secondLineBorderX, Constants.height))

        circles.forEach {sR.drawCircle(it)}
        movingCircles.forEach {it.draw()}
        //INFO Filter out the ones which are finished
        //INFO Dont clear all, could do two seperate movements at once
        movingCircles = movingCircles.filter {it.stillMoving}.toMutableList() //TODO Might be very time/space consuming
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
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        //Draw new Circle and add to list
        fun createNewCircle(x: Float, y: Float) {
            circleDef.position.set(x,y)
            val body: Body = world.createBody(circleDef)
            // INFO without this, laying x circle above each other does not make them move until I pull first by hand
            body.applyForceToCenter(0.00001f, 0.00001f, true)

            val fixture = body.createFixture(fixtureDef) //TODO dispose fixture when circle merged or deleted
            fixture.updateColor()
            circles.add(fixture)
        }

        if (dragState != DragState.DRAGCIRCLE) { // As long as not moved circle, create new one
            //TODO IN circles.add(Circle(screenX.toFloat(), screenY.toFloat()))
                // TODO everything might move for all time when I put 100-circle in 1-block
            createNewCircle(screenXNormalized, screenYNormalized)
        } else {
            //Moved Circle, update everything
            val oldValue = draggedCircle!!.getValue()
            draggedCircle!!.updateColor()
            val newValue = draggedCircle!!.getValue()

            //TODO Dispose draggedCircle

            val ratio = oldValue.toFloat()/newValue
            if (ratio >= 1) { // Add circles or do nothing
                repeat(ratio.toInt() -1) { //Keep original dragged circle, so only create one less, Without keeping dragged circle, new circles wont move, so keep it
                    createNewCircle(screenXNormalized, screenYNormalized)
                }
            } else { //Remove circles
                //Again, keep original dragged circle
                val numCirclesToRemove = (1/ratio).toInt() - 1 //keep dragged one, so -1
                val circlesToRemove = circles.getCirclesOfValue(numCirclesToRemove, oldValue)
                val testCircle = circlesToRemove!![0]

                //INFO Don't want that moving circle collides with anything, so remove its fixture/body first
                circlesToRemove?.forEach {
                    circles.remove(it)
                    it.destroy() //INFO Needed, else still lag although moved circles from 1 to 100
                    movingCircles.add(MovingCircle(it.body.position, Vector2(screenXNormalized, screenYNormalized), it.getColor(), sR))
                }
                //TODO Snap-Back if list is null
            }
        }

        //reset
        draggedCircle = null
        dragState = DragState.NONE

    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        if (draggedCircle != null) {
            //TODO I can move circle out of screen (Top,Down,Left,Right)
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
    this.color = c.getColor()
    this.circle(pos.x,pos.y,Constants.radius - (Constants.lineWidth * 0.5).toFloat(), 100) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = Constants.lineColor
    this.end() //TODO I should only open shaperenderer once, because explensive: https://stackoverflow.com/questions/29035553/trying-to-draw-a-circle-in-libgdx
}

//Return right color, depending on x-coordinate
//Dont do this all the time because circle should keep color while dragging
fun Fixture.updateColor() {
    val x = this.body.position.x
    this.body.userData = when {
        x >= Constants.secondLineBorderX -> Pair(Constants.circleGreenColor, Constants.circleGreenValue)
        x >= Constants.firstLineBorderX -> Pair(Constants.circleBlueColor, Constants.circleBlueValue)
        else -> Pair(Constants.circleRedColor, Constants.circleRedValue)
    }
}

fun Fixture.getColor(): Color {
    return (this.body.userData as Pair<Color,Int>).first
}

fun Fixture.getValue(): Int {
    return (this.body.userData as Pair<Color,Int>).second
}

fun Fixture.destroy() = this.body.destroyFixture(this)

fun List<Fixture>.getCirclesOfValue(num: Int, value: Int): List<Fixture>? {//Return no list if not enough circles
    if (num == 0) return emptyList() // Do this for easier if-else stuff after for-loop

    val out = arrayListOf<Fixture>()
    for (circle in this.asReversed()) { //TODO Might be slow, because really reverse list
        if (circle.getValue() == value) {
            out.add(circle)
        }
        if (out.size == num) {
            return out
        }
    }
    return null
}
