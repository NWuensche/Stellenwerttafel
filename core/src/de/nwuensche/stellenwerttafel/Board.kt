package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.I18NBundle
import com.ibm.icu.text.MessageFormat
import java.util.*
import kotlin.random.Random


//batch only used for text
class Board(val batch: SpriteBatch, val sR: ShapeRenderer, val world: World, val font: BitmapFont) : Drawable {
    val circles = arrayListOf<Fixture>()
    val flyingCircles: MutableList<FlyingCircle> = arrayListOf()
    var titleTable100Number = 0 // Updating string also updates text on screen automatically
    var titleTable10Number = 0
    var titleTable1Number = 0
    val glyph = GlyphLayout(font, "")
    val myBundle: I18NBundle by lazy {
        val baseFileHandle = Gdx.files.internal("i18n/MyBundle")
        I18NBundle.createBundle(baseFileHandle, Locale.getDefault())
    }

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch on nothing
    }
    private var dragState = DragState.NONE
    var dragStartPosition: Vector2? = null
    var dragCircle: Fixture? = null
    var dragStartColor: Color? = null // Will for jump back circles be overwritten, so store it first
    //TODO END also name app English when uploading english version

    override fun draw() {
        drawGrid()
        drawDeleteButton()
        drawTexts() //Text on top of button

        sR.drawCircles { circles.forEach {sR.drawCircle(it)} } // Draw 'normal' circles
        drawAndHandleFlyingCircles()

    }

    //draw flying circles (without box2d box) + remove them when they are at goal and shall be removed
    private fun drawAndHandleFlyingCircles() {
        val flyingCirclesToDelete = arrayListOf<FlyingCircle>() //Store all circles which are at endposition, because I cant remove while iterating over collection, is forbidden
        flyingCircles.forEach {
            val atEnd = it.drawAndFinished()
            if (atEnd) {
                flyingCirclesToDelete.add(it)
            }
        }
        //INFO Dont clear all flying circles once one is at goal, could do two seperate movements at once
        flyingCirclesToDelete.forEach {
            if (it.keep) { //Create new circle with physics (used when jump back circle)
                createNewCircle(it.endPosition.x, it.endPosition.y)
            }
            flyingCircles.remove(it)
        }
    }

    private fun drawTexts() {
        batch.begin()

        val sum = Constants.circle100Value * titleTable100Number + Constants.circle10Value * titleTable10Number + Constants.circle1Value * titleTable1Number
        val sumLocalizedText = MessageFormat.format("{0,spellout}", sum).replace(" ", "").replace("\u00AD", "").replace("-", "").capitalize() //replace '-' and unicode-version of '-' because zweihundert is actually 'zwei-hundert', same holds for ' ' in english
        font.drawCentered(batch, this.glyph, "$sum = $sumLocalizedText", 0f, Constants.width, 0f, Constants.secondLineBorderY) //INFO Does not look good when xRight=ButtonX
        font.color = if (titleTable100Number >= Constants.circle10Value) Constants.overflowColor else Constants.lineColor

        font.drawCentered(batch, this.glyph, myBundle.format("namePlaceValueHundreds", titleTable100Number), 0f, Constants.firstLineBorderX, Constants.secondLineBorderY, Constants.firstLineBorderY)
        font.color = if (titleTable10Number >= Constants.circle10Value) Constants.overflowColor else Constants.lineColor
        font.drawCentered(batch, this.glyph, myBundle.format("namePlaceValueTens", titleTable10Number), Constants.firstLineBorderX, Constants.secondLineBorderX, Constants.secondLineBorderY, Constants.firstLineBorderY)
        font.color = if (titleTable1Number >= Constants.circle10Value) Constants.overflowColor else Constants.lineColor
        font.drawCentered(batch, this.glyph, myBundle.format("namePlaceValueOnes", titleTable1Number), Constants.secondLineBorderX, Constants.width, Constants.secondLineBorderY, Constants.firstLineBorderY)

        font.color = Constants.fontColorButton
        font.drawCentered(batch, this.glyph, "\u00D7", Constants.buttonX, Constants.buttonX + Constants.buttonWidth, Constants.buttonY, Constants.buttonY+Constants.buttonHeight)

        font.color = Constants.lineColor
        batch.end()
    }

    fun drawDeleteButton() {
        sR.drawRoundedRect(Constants.buttonX, Constants.buttonY, Constants.buttonWidth, Constants.buttonHeight, Constants.buttonCornerRadius)
    }

    //TODO End refactor long methods

    private fun drawGrid() {
        sR.drawLine(Vector2(Constants.firstLineBorderX, Constants.secondLineBorderY), Vector2(Constants.firstLineBorderX, Constants.height))
        sR.drawLine(Vector2(Constants.secondLineBorderX, Constants.secondLineBorderY), Vector2(Constants.secondLineBorderX, Constants.height))

        sR.drawLine(Vector2(0f, Constants.firstLineBorderY), Vector2(Constants.width, Constants.firstLineBorderY))
        sR.drawLine(Vector2(0f, Constants.secondLineBorderY), Vector2(Constants.width, Constants.secondLineBorderY))
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
            createNewCircle(screenXNormalized, screenYNormalized, completelyNewSingleCircle = true)
        } else {
            //Moved Circle, update everything
            val oldValue = dragCircle!!.getValue()
            dragCircle!!.updateColor()
            val newValue = dragCircle!!.getValue()

            val ratio = oldValue.toFloat()/newValue
            if (ratio >= 1) { // Add circles or do nothing
                repeat(ratio.toInt() - 1) { //Keep original dragged circle, so only create one less, Without keeping dragged circle, new circles wont move, so keep it
                    createNewCircle(screenXNormalized, screenYNormalized)
                }
            } else { //Remove circles
                //Again, keep original dragged circle
                val numCirclesToRemove = (1/ratio).toInt() - 1 //keep dragged one, so -1
                val circlesToRemove = circles.getCirclesOfValue(numCirclesToRemove, oldValue)
                //INFO Don't want that moving circle collides with anything, so remove its fixture/body first
                if (circlesToRemove == null) {
                    flyingCircles.add(FlyingCircle(dragCircle!!.body.position, dragStartPosition!!, dragStartColor!!, sR, keep = true))
                    circles.remove(dragCircle!!)
                    dragCircle!!.destroy()
                }
                circlesToRemove?.forEach {
                    flyingCircles.add(FlyingCircle(it.body.position, Vector2(screenXNormalized, screenYNormalized), it.getColor(), sR))
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

        //update counter
        //TODO Could be faster by traversing only once
        titleTable100Number = circles.filter{it.getColor() == Constants.circle100Color}.size
        titleTable10Number = circles.filter{it.getColor() == Constants.circle10Color}.size
        titleTable1Number = circles.filter{it.getColor() == Constants.circle1Color}.size
    }

    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        //Check if delete Button Pressed
        if (containedInRect(screenXNormalized, screenYNormalized, Constants.buttonX, Constants.buttonY, Constants.buttonX+Constants.buttonWidth, Constants.buttonY+Constants.buttonHeight)) {
            deleteAllCircles()
        }

        //Dont need to handle touchUp when pressing button, because I ignore touchUp in header

        //update counter
        //TODO Could be faster by traversing only once
        titleTable100Number = circles.filter{it.getColor() == Constants.circle100Color}.size
        titleTable10Number = circles.filter{it.getColor() == Constants.circle10Color}.size
        titleTable1Number = circles.filter{it.getColor() == Constants.circle1Color}.size
    }

    //Remove all box2d boxes from circles, make circles fly in random directions
    fun deleteAllCircles() {
        for (circle in circles) {
            //random which border the circle flyies to, Other value is random
            val whichOneIsBorder = Random.Default.nextInt(4) //4 non-inclusive
            val offset = 0.1f //Add to coordinates so that flying circle already offscreen when it gets destroyed
            val endX = when(whichOneIsBorder) {
                0 -> -offset
                1 -> Constants.width + offset
                else -> Random.Default.nextFloat() * Constants.width //Normalize
            }
            val endY = when(whichOneIsBorder) {
                2 -> -offset
                3 -> Constants.height + offset
                else -> Random.Default.nextFloat() * Constants.height //Normalize
            }
            //copy position vector, because else would also be destroyed
            flyingCircles.add(FlyingCircle(circle.body.position.copy(), Vector2(endX, endY), circle.getColor(), sR))
            circle.destroy()
        }
        circles.clear() // Last, because cant remove while iterating over list
    }

    //Draw new Circle, add Box2D physics and add to list
    fun createNewCircle(x: Float, y: Float, completelyNewSingleCircle: Boolean = false) {
        //When I move 100-circle to top border of 1-circle box, then most 1-circles get generated above border
        //Thus, I have to 'get them back' with the coerceIn function and cannot remove them
        //But when I add a single new circle, I don't want to allow to create any circle by pressing above header and want to remove them in this case
        //Thus, I keep track with flag in which mode I currently am in and whether I can remove the new circle above header or move it down
        if (completelyNewSingleCircle && y <=  Constants.firstLineBorderY) {
            return
        }
        //If I dont check this, then it can happen that when moving 100-circle fast to right/up/down border of 1-value that some circles are generated out of screen (can be seen when going back to 100)
        val x1 = x.coerceIn(Constants.widthCircleAndHitbox, Constants.width - Constants.widthCircleAndHitbox)
        val y1 = y.coerceIn(Constants.firstLineBorderY + Constants.widthCircleAndHitbox, Constants.height - Constants.widthCircleAndHitbox)
        //Also check not inside hitbox of border, else it can happen that e.g. when putting 100-circle on border (to 10-circle) of 1-circle then some circles left and some right, but all green
        val x2 = when {
            (x1 >= Constants.firstLineBorderX - Constants.widthCircleAndHitbox) && (x1 <= Constants.firstLineBorderX) -> Constants.firstLineBorderX - Constants.widthCircleAndHitbox //In first hitbox, but closer to 100-box
            (x1 >= Constants.firstLineBorderX) && (x1 <= Constants.firstLineBorderX + Constants.widthCircleAndHitbox) -> Constants.firstLineBorderX + Constants.widthCircleAndHitbox //In first hitbox, but closer to 10-box

            (x1 >= Constants.secondLineBorderX - Constants.widthCircleAndHitbox) && (x1 <= Constants.secondLineBorderX) -> Constants.secondLineBorderX - Constants.widthCircleAndHitbox //In second hitbox, but closer to 10-box
            (x1 >= Constants.secondLineBorderX) && (x1 <= Constants.secondLineBorderX + Constants.widthCircleAndHitbox) -> Constants.secondLineBorderX + Constants.widthCircleAndHitbox //In second hitbox, but closer to 1-box
            else -> x1 // No border-collision detected
        }
        //TODO Alex was passiert mit Circlen wenn die über Header landen/liegen? gehen die da überhaupt hin mit drag? Oder werden die direkt gelöscht
        val y2 = y1 //INFO Dont need border thing like for x2 for y2, because coerceIn handles custom border (+  not bottom, there physically not possible)
        circleDef.position.set(x2, y2)
        //TODO End auch Englische Beschreibung + Englischen Namen in Google Play Store

        val body = world.createBody(circleDef)
        // INFO without this, creating many circles next to each other does not make them move until I pull first by hand
        body.applyLinearImpulse(Vector2(0.0001f, 0.0001f), body.position, true) //Impulse applies once, Force continiously
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
            if((screenXNormalized- Constants.widthCircleAndHitbox >= 0) && (screenXNormalized + Constants.widthCircleAndHitbox <= Constants.width)){
                newX = screenXNormalized
            }
            if((screenYNormalized- Constants.widthCircleAndHitbox >= Constants.firstLineBorderY) && (screenYNormalized + Constants.widthCircleAndHitbox <= Constants.height)){
                newY = screenYNormalized
            }
            dragCircle!!.body.setTransform(newX, newY, 0f)
            return
        }
        
        for (circle in circles.asReversed()) { //TODO Might be slow, because really reverse list, TODO Cant reverse list, because then drawing is inside-out (highest circle is below all others - TODO DoubleLinkedList?
            if (containedInCircle(screenXNormalized, screenYNormalized, circle.body.position.x, circle.body.position.y, Constants.radiusSprite)) {
                dragCircle = circle
                dragStartPosition = circle.body.position.copy() // Without copy will be changed whole time
                dragStartColor = circle.getColor()
                break
            }
        }
        dragState = if (dragCircle != null) DragState.DRAGCIRCLE else DragState.DRAGNOTHING
    }

    //Returns True iff inX/inY Point inside circle (containemt includes border)
    fun containedInCircle(inX: Float, inY: Float, mX: Float, mY: Float, radius: Float): Boolean {
        return containedInRect(inX, inY, mX-radius, mY-radius, mX+radius, mY+radius)
    }

    //Returns True iff inX/inY Point inside rectangle (containemt includes border)
    fun containedInRect(inX: Float, inY: Float, rectTopLeftX: Float, rectTopLeftY: Float, rectBottomRightX: Float, rectBottomRightY: Float): Boolean {
        return (rectTopLeftX <= inX)
                && (inX <= rectBottomRightX)
                && (rectTopLeftY <= inY)
                && (inY <= rectBottomRightY)
    }
}

fun ShapeRenderer.drawLine(v1: Vector2, v2: Vector2) {
    this.begin(ShapeRenderer.ShapeType.Line)
    this.line(v1, v2)
    this.end()
}

fun ShapeRenderer.drawCircle(c: Fixture) {
    val pos = c.body.position
    this.circle(pos.x, pos.y, Constants.radiusSprite, 50) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = c.getColor()
    this.circle(pos.x, pos.y, Constants.radiusSprite - (Constants.lineWidth * 0.5).toFloat(), 20) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = Constants.lineColor
}

//INFO I should only open shaperenderer once, because explensive, thus use that
fun ShapeRenderer.drawCircles(f: () -> Unit) {
    this.begin(ShapeRenderer.ShapeType.Filled)
    f()
    this.end()
}

//Credit to https://gamedev.stackexchange.com/a/118396
fun ShapeRenderer.drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float) {
    // Central rectangle
    this.begin(ShapeRenderer.ShapeType.Filled) //INFO Need filled, else would see too many Lines
    this.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius)

    // Four side rectangles, in clockwise order
    this.rect(x + radius, y, width - 2 * radius, radius)
    this.rect(x + width - radius, y + radius, radius, height - 2 * radius)
    this.rect(x + radius, y + height - radius, width - 2 * radius, radius)
    this.rect(x, y + radius, radius, height - 2 * radius)

    // Four arches, clockwise too
    this.arc(x + radius, y + radius, radius, 180f, 90f)
    this.arc(x + width - radius, y + radius, radius, 270f, 90f)
    this.arc(x + width - radius, y + height - radius, radius, 0f, 90f)
    this.arc(x + radius, y + height - radius, radius, 90f, 90f)
    this.end()
}

//Return right color, depending on x-coordinate
//Dont do this all the time because circle should keep color while dragging
fun Fixture.updateColor() {
    val x = this.body.position.x
    this.body.userData = when {
        x >= Constants.secondLineBorderX -> Pair(Constants.circle1Color, Constants.circle1Value)
        x >= Constants.firstLineBorderX -> Pair(Constants.circle10Color, Constants.circle10Value)
        else -> Pair(Constants.circle100Color, Constants.circle100Value)
    }
}

fun Fixture.getColor(): Color {
    return (this.body.userData as Pair<Color, Int>).first
}

fun Fixture.getValue(): Int {
    return (this.body.userData as Pair<Color, Int>).second
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

fun BitmapFont.drawCentered(batch: SpriteBatch, glyph: GlyphLayout, s: String, xLeft: Float, xRight: Float, yUp: Float, yDown: Float) {
    glyph.setText(this, s)

    val textWidth = glyph.width
    val marginX = (xRight-(xLeft + textWidth))/2

    val textHeight = glyph.height
    val marginY = (yDown-(yUp + textHeight))/2

    this.draw(batch, s, xLeft + marginX, yUp + marginY)
}
