package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import de.nwuensche.stellenwerttafel.Constants.height
import de.nwuensche.stellenwerttafel.Constants.lineWidth
import de.nwuensche.stellenwerttafel.Constants.width
import de.nwuensche.stellenwerttafel.Constants.widthCircleAndHitbox
import de.nwuensche.stellenwerttafel.Constants.widthHitBoxBorders

class Board(val sR: ShapeRenderer, val world: World) : Drawable {
    val circles = arrayListOf<Fixture>()
    val movingCircles: MutableList<MovingCircle> = arrayListOf()

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch on nothing
    }
    private var dragState = DragState.NONE
    var dragStartPosition: Vector2? = null
    var dragCircle: Fixture? = null
    var dragStartColor: Color? = null // Will for jump back circles be overwritten, so store it first


    override fun draw() {
        sR.drawLine(Vector2(Constants.firstLineBorderX, 0f), Vector2(Constants.firstLineBorderX, Constants.height))
        sR.drawLine(Vector2(Constants.secondLineBorderX, 0f), Vector2(Constants.secondLineBorderX, Constants.height))

        circles.forEach {sR.drawCircle(it)}
        for (it in movingCircles) {

        }
        val movingCirclesToDelete = arrayListOf<MovingCircle>() //Store all circles which are at endposition, because I cant remove while iterating over collection, is forbidden
        movingCircles.forEach {
            val atEnd = it.drawAndFinished()
            if (atEnd) {
                movingCirclesToDelete.add(it)
            }
        }
        movingCirclesToDelete.forEach {
            if (it.keep) { //Create new circle with physics (used when jump back circle)
                createNewCircle(it.endPosition.x, it.endPosition.y)
            }
            movingCircles.remove(it)
        }

        //INFO Filter out the ones which are finished
        //INFO Dont clear all, could do two seperate movements at once
        //movingCircles = movingCircles.filter {it.stillMoving}.toMutableList() //TODO Might be very time/space consuming
    }


    val circleDef = BodyDef().apply { this.type = BodyDef.BodyType.DynamicBody }
    val circleShape = CircleShape().apply { this.radius = Constants.radiusHitBox }
    val fixtureDef = FixtureDef().apply {
        this.shape = circleShape
        this.density = 0.2f
        this.friction = 0.1f
        this.restitution = 0.6f
    }

    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        if (dragState != DragState.DRAGCIRCLE) { // As long as not moved circle, create new one
            createNewCircle(screenXNormalized, screenYNormalized)
        } else {
            //Moved Circle, update everything
            val oldValue = dragCircle!!.getValue()
            dragCircle!!.updateColor()
            val newValue = dragCircle!!.getValue()

            val ratio = oldValue.toFloat()/newValue
            if (ratio >= 1) { // Add circles or do nothing
                repeat(ratio.toInt() -1) { //Keep original dragged circle, so only create one less, Without keeping dragged circle, new circles wont move, so keep it
                    createNewCircle(screenXNormalized, screenYNormalized)
                }
            } else { //Remove circles
                //Again, keep original dragged circle
                val numCirclesToRemove = (1/ratio).toInt() - 1 //keep dragged one, so -1
                val circlesToRemove = circles.getCirclesOfValue(numCirclesToRemove, oldValue)
                //INFO Don't want that moving circle collides with anything, so remove its fixture/body first
                if (circlesToRemove == null) {
                    movingCircles.add(MovingCircle(dragCircle!!.body.position, dragStartPosition!!, dragStartColor!!, sR, keep = true))
                    circles.remove(dragCircle!!)
                    dragCircle!!.destroy()
                }
                circlesToRemove?.forEach {
                    movingCircles.add(MovingCircle(it.body.position, Vector2(screenXNormalized, screenYNormalized), it.getColor(), sR))
                    circles.remove(it)
                    it.destroy() //INFO Needed, else still lag although moved circles from 1 to 100
                }
            }
        }

        //reset
        dragCircle = null
        dragState = DragState.NONE
        dragStartPosition = null
        dragStartColor = null
    }

    //Draw new Circle, add Box2D physics and add to list
    fun createNewCircle(x: Float, y: Float) {
        //If I dont check this, then it can happen that when moving 100-circle fast to right/up/down border of 1-value that some circles are generated out of screen (can be seen when going back to 100)
        circleDef.position.set(x.coerceIn(widthCircleAndHitbox, width-widthCircleAndHitbox),y.coerceIn(widthCircleAndHitbox, height - widthCircleAndHitbox))
        val body: Body = world.createBody(circleDef)
        // INFO without this, laying x circle above each other does not make them move until I pull first by hand
        body.applyForceToCenter(0.00001f, 0.00001f, true)

        val fixture = body.createFixture(fixtureDef)
        fixture.updateColor()
        circles.add(fixture)
    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        if (dragCircle != null) {
            //INFO Moving on e.g. y axis ok, even if at border of y axis, so compute both seperately
            var newX = dragCircle!!.body.position.x
            var newY = dragCircle!!.body.position.y
            if((screenXNormalized- widthCircleAndHitbox >= 0) && (screenXNormalized + widthCircleAndHitbox <= width)){
                newX = screenXNormalized
            }
            if((screenYNormalized- widthCircleAndHitbox >= 0) && (screenYNormalized + widthCircleAndHitbox <= height)){
                newY = screenYNormalized
            }
            dragCircle!!.body.setTransform(newX, newY, 0f)
            return
        }
        
        for (circle in circles.asReversed()) { //TODO Might be slow, because really reverse list
            if (((circle.body.position.x - Constants.radiusSprite) <= screenXNormalized)
                    && (screenXNormalized <= (circle.body.position.x + Constants.radiusSprite))
                    && ((circle.body.position.y - Constants.radiusSprite) <= screenYNormalized)
                    && (screenYNormalized <= (circle.body.position.y + Constants.radiusSprite))) {
                dragCircle = circle
                dragStartPosition = circle.body.position.copy() // Without copy will be changed whole time
                dragStartColor = circle.getColor()
                break
            }
        }
        dragState = if (dragCircle != null) DragState.DRAGCIRCLE else DragState.DRAGNOTHING
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
    this.circle(pos.x,pos.y,Constants.radiusSprite,50) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = c.getColor()
    this.circle(pos.x,pos.y,Constants.radiusSprite - (Constants.lineWidth * 0.5).toFloat(), 20) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
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
