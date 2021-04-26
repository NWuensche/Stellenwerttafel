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

//TODO Wenn >40 Downloads: Replace Circle ShapeRenderer with SpriteBatch, can make image out of ShapreRenderer with PixMap or already have circle generator for Jewel Trop -
//Dont do, because then finding `highest` circle for dragging and flying very hard - LATER Drop `circles` list, do everything with extra attribute `listCirclesInColumn` and iterate over them (because .size is const time, while .filter is not
data class Column(val leftX: Float, val rightX: Float, val color: Color, val value: Int)

//batch only used for text
class Board(val batch: SpriteBatch, val sR: ShapeRenderer, val world: World, val font: BitmapFont, numColumns: Int) : Drawable {
    val circles = arrayListOf<Fixture>()
    val flyingCircles: MutableList<FlyingCircle> = arrayListOf()
    val titleTableNumbers: MutableList<Int>// Updating string also updates text on screen automatically
    val glyph = GlyphLayout(font, "")
    val columns: List<Column>
    val myBundle: I18NBundle by lazy {
        val baseFileHandle = Gdx.files.internal("i18n/MyBundle")
        I18NBundle.createBundle(baseFileHandle, Locale.getDefault())
    }

    init {
        columns = initBorders(numColumns) //Can only init val in `init`
        titleTableNumbers = MutableList(numColumns) {0}
        //INFO Should not be problem that I do this in init when board is lazy,
        //because before I can do board.draw() in MyGdxGame, I have to init board, thus this init will get called
        createBordersBox2D() // Only do this once, so in init
        setFontRightSize()
    }

    fun initBorders(numColumns: Int): List<Column> {
        val out = mutableListOf<Column>()
        val offset = Constants.valuesColumns.size - numColumns // e.g. when only 3 columns, then ignore first 1000-column
        repeat(numColumns) {
            out.add(Column(it * (Constants.width)/numColumns, (it+1) * (Constants.width)/numColumns, Constants.colorsColumns[it+offset], Constants.valuesColumns[it+offset]))
        }
        return out
    }

    //register box2d boxes borders once
    private fun createBordersBox2D() {
        val borderDef = BodyDef()
        borderDef.position.set(Vector2(0f, Constants.height))
        val groundBody = world.createBody(borderDef)
        val groundBox = PolygonShape()
        groundBox.setAsBox(Constants.width, Constants.widthHitBoxBorders) //INFO Takes half values as inputs, but I want margin around drawn lines, so 'normal' is ok
        groundBody.createFixture(groundBox, 0.0f)

        borderDef.position.set(Vector2(0f, Constants.firstLineBorderY))
        val ceilBody = world.createBody(borderDef)
        val ceilBox = PolygonShape()
        ceilBox.setAsBox(Constants.width, Constants.widthHitBoxBorders)
        ceilBody.createFixture(ceilBox, 0.0f)

        borderDef.position.set(Vector2(0f, 0f))
        val leftBody = world.createBody(borderDef)
        val leftBox = PolygonShape()
        leftBox.setAsBox(Constants.widthHitBoxBorders, Constants.height)
        leftBody.createFixture(leftBox, 0.0f)

        borderDef.position.set(Vector2(Constants.width, 0f))
        val rightBody = world.createBody(borderDef)
        val rightBox = PolygonShape()
        rightBox.setAsBox(Constants.widthHitBoxBorders, Constants.height)
        rightBody.createFixture(rightBox, 0.0f)

        //Column borders
        for (column in columns.dropLast(1)) {
            borderDef.position.set(Vector2(column.rightX, 0f))
            val borderBody = world.createBody(borderDef)
            val borderBox = PolygonShape()
            borderBox.setAsBox(Constants.widthHitBoxBorders, Constants.height)
            borderBody.createFixture(borderBox, 0.0f)
        }
    }

    //Set Text s.t. it fits well in box
    fun setFontRightSize() {
        //Create longest possible text in header, assume that this is largest number in whole app (e.g. 1000) + largest place value (e.g. Thousands) in current Table
        val longestNumInHeader = Constants.valuesColumns[0] // get value of largest possible column (here 1000)
        val longestText = myBundle.format("namePlaceValue${columns[0].value}", longestNumInHeader)

        glyph.setText(font, longestText)

        val textWidth = glyph.width
        val possibleWidth = (columns[0].rightX - 0.5f*Constants.lineWidth)
        val scaleTextWidth = (1 + Constants.scaleFont) * (possibleWidth/textWidth) //for setscale has to be e.g. 0.1 instead of -0.9

        val textHeight = glyph.height
        val possibleHeight = Constants.firstLineBorderY - (Constants.secondLineBorderY + Constants.lineWidth)
        val scaleTextHeight = (1 + Constants.scaleFont) * (possibleHeight/textHeight) //for setscale has to be e.g. 0.1 instead of -0.9

        //Take minimum of scale width/height so it doesnt go over any line, but also consider Constants.scaleFont because dont want to upscale (e.g. when using `10` as largest column)
        font.data.setScale(listOf(scaleTextWidth, scaleTextHeight, 1+Constants.scaleFont).min()!!) // INFO data.scale is wrong, but for setscale has to be e.g. 0.1 instead of -0.9
    }

    private enum class DragState {
        NONE, //Not in dragged state
        DRAGCIRCLE, // dragged, and first touch on a circle
        DRAGNOTHING // dragged, but first touch in header
    }
    private var dragState = DragState.NONE
    //TODO Could be properties of sealed class + use `when`
    var dragStartPosition: Vector2? = null
    var dragCircle: Fixture? = null
    var dragStartColor: Color? = null // Will for jump back circles be overwritten, so store it first

    override fun draw() {
        drawGrid()
        drawDeleteButton()
        drawTexts() //Text on top of button

        //In refernce app, circles above everything (cover boarders, text,...)
        sR.drawFilled { circles.forEach {sR.drawCircle(it)} } // Draw 'normal' circles
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
        val sum = titleTableNumbers.mapIndexed { i,it -> columns[i].value * it }.sum()
        val sumLocalizedText = MessageFormat.format("{0,spellout}", sum).replace(" ", "").replace("\u00AD", "").replace("-", "").capitalize() //replace '-' and unicode-version of '-' because zweihundert is actually 'zwei-hundert', same holds for ' ' in english
        font.drawCentered(batch, this.glyph, "$sum = $sumLocalizedText", 0f, Constants.width, 0f, Constants.secondLineBorderY) //INFO Does not look good when xRight=ButtonX

        font.color = Constants.fontColorButton
        font.drawCentered(batch, this.glyph, "\u00D7", Constants.buttonX, Constants.buttonX + Constants.buttonWidth, Constants.buttonY, Constants.buttonY+Constants.buttonHeight)
        font.color = Constants.lineColor

        for ((i, column) in columns.withIndex()) {
            font.color = if (titleTableNumbers[i] >= Constants.basis) Constants.overflowColor else Constants.lineColor
            font.drawCentered(batch, this.glyph, myBundle.format("namePlaceValue${column.value}", titleTableNumbers[i]), column.leftX, column.rightX, Constants.secondLineBorderY, Constants.firstLineBorderY)
        }

        font.color = Constants.lineColor
        batch.end()
    }

    fun drawDeleteButton() {
        sR.drawRoundedRect(Constants.buttonX, Constants.buttonY, Constants.buttonWidth, Constants.buttonHeight, Constants.buttonCornerRadius)
    }

    //TODO End refactor long methods

    private fun drawGrid() {
        sR.drawFilled {
            //Header borders
            sR.drawLine(Vector2(0f, Constants.firstLineBorderY), Vector2(Constants.width, Constants.firstLineBorderY), Constants.lineWidth)
            sR.drawLine(Vector2(0f, Constants.secondLineBorderY), Vector2(Constants.width, Constants.secondLineBorderY), Constants.lineWidth)

            //Column Borders
            for (column in columns.dropLast(1)) {
                sR.drawLine(Vector2(column.rightX, Constants.secondLineBorderY), Vector2(column.rightX, Constants.height), Constants.lineWidth)
            }
        }
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

        //INFO Cannot use this, because when I re-tap screen while circle is flying, it does not get counted because now another touchUp event -> Also count flying circles which I keep
        // If I fly circle from header back to start place
        // Then the value of the circle is not counted while it is flying
        // To prevent this, I store its value in outOfScopeFlyingValue variable
        // and add it to correct table column when necessary
        //var outOfScopeFlyingValue = -1



        if (dragState == DragState.DRAGCIRCLE) {

            if (dragCircle!!.body.position.y <= Constants.firstLineBorderY) { // Above first line -> In Header
                if (dragCircle!!.body.position.y >= Constants.secondLineBorderY) { // Above first line and below second line -> fly back
                    flyingCircles.add(FlyingCircle(dragCircle!!.body.position.copy(), dragStartPosition!!, dragStartColor!!, sR, keep = true))
                    circles.remove(dragCircle!!)
                    dragCircle?.destroy()
                } else { // Above first line and above second line -> remove and fly to upper boarder screen
                    val newX = dragCircle!!.body.position.x
                    val newY = -Constants.offset //Fly through upper boarder screen
                    flyingCircles.add(FlyingCircle(dragCircle!!.body.position.copy(), Vector2(newX, newY), dragStartColor!!, sR))
                    circles.remove(dragCircle!!)
                    dragCircle?.destroy()
                }
            } else { //Moved Circle inside tables, update everything
                val oldValue = dragCircle!!.getValue()
                dragCircle!!.updateColor(columns)
                val newValue = dragCircle!!.getValue()

                val ratio = oldValue.toFloat() / newValue
                if (ratio >= 1) { // Add circles or do nothing
                    repeat(ratio.toInt() - 1) { //Keep original dragged circle, so only create one less, Without keeping dragged circle, new circles wont move, so keep it
                        createNewCircle(screenXNormalized, screenYNormalized)
                    }
                } else { //Remove circles
                    //Again, keep original dragged circle
                    val numCirclesToRemove = (1 / ratio).toInt() - 1 //keep dragged one, so -1
                    val circlesToRemove = circles.getCirclesOfValue(numCirclesToRemove, oldValue)
                    //INFO Don't want that moving circle collides with anything, so remove its fixture/body first
                    if (circlesToRemove == null) {
                        flyingCircles.add(FlyingCircle(dragCircle!!.body.position.copy(), dragStartPosition!!, dragStartColor!!, sR, keep = true))
                        circles.remove(dragCircle!!)
                        dragCircle!!.destroy()
                    }
                    circlesToRemove?.forEach {
                        flyingCircles.add(FlyingCircle(it.body.position.copy(), Vector2(screenXNormalized, screenYNormalized), it.getColor(), sR))
                        circles.remove(it)
                        it.destroy() //INFO Needed, else still lag although moved circles from 1 to 100
                    }
                }
            }
        }

        //reset (also needed when dragged in header, thus DRAGGEDNOTHING)
        dragCircle = null
        dragState = DragState.NONE
        dragStartPosition = null
        dragStartColor = null

        updateTableCounters()
    }

    private fun updateTableCounters() {
        //TODO Could be faster by traversing only once
        for ((i, column) in columns.withIndex()) {
            titleTableNumbers[i] = circles.filter { it.getColor() == column.color }.size +
                    flyingCircles.filter { it.keep && it.color == column.color }.size
        }
    }

    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        //Check if delete Button Pressed
        if (containedInRect(screenXNormalized, screenYNormalized, Constants.buttonX, Constants.buttonY, Constants.buttonX+Constants.buttonWidth, Constants.buttonY+Constants.buttonHeight)) {
            deleteAllCircles()
        }

        //Dont need to handle touchUp when pressing button, because I ignore touchUp in header

        //Either drag circle or create new one and drag this one (like in app)
        for (circle in circles.asReversed()) { //TODO Might be slow, because really reverse list, TODO Cant reverse list, because then drawing is inside-out (highest circle is below all others - TODO DoubleLinkedList?
            if (containedInCircle(screenXNormalized, screenYNormalized, circle.body.position.x, circle.body.position.y, Constants.radiusSprite)) {
                dragState = DragState.DRAGCIRCLE
                dragCircle = circle
                dragStartPosition = circle.body.position.copy() // Without copy will be changed whole time
                dragStartColor = circle.getColor()
                break
            }
        }
        if (dragState == DragState.NONE) { // Nothing dragged, thus create new circle and drag it
            val newCircle = createNewCircle(screenXNormalized, screenYNormalized, completelyNewSingleCircle = true)
            if (newCircle != null) {
                dragState = DragState.DRAGCIRCLE
                dragCircle = newCircle
                dragStartPosition = newCircle.body.position.copy()
                dragStartColor = newCircle.getColor()
            } else {
                dragState = DragState.DRAGNOTHING
            }
        }

        updateTableCounters()
    }

    //Remove all box2d boxes from circles, make circles fly in random directions
    fun deleteAllCircles() {
        for (circle in circles) {
            //random which border the circle flyies to, Other value is random
            val whichOneIsBorder = Random.Default.nextInt(4) //4 non-inclusive
            val endX = when(whichOneIsBorder) {
                0 -> -Constants.offset
                1 -> Constants.width + Constants.offset
                else -> Random.Default.nextFloat() * Constants.width //Normalize
            }
            val endY = when(whichOneIsBorder) {
                2 -> -Constants.offset
                3 -> Constants.height + Constants.offset
                else -> Random.Default.nextFloat() * Constants.height //Normalize
            }
            //copy position vector, because else would also be destroyed
            flyingCircles.add(FlyingCircle(circle.body.position.copy(), Vector2(endX, endY), circle.getColor(), sR))
            circle.destroy()
        }
        circles.clear() // Last, because cant remove while iterating over list
    }

    //Draw new Circle, add Box2D physics and add to list
    //Return new circle because I need it when I drag it (could be null when started drag in header
    fun createNewCircle(x: Float, y: Float, completelyNewSingleCircle: Boolean = false): Fixture? {
        //When I move 100-circle to top border of 1-circle box, then most 1-circles get generated above border
        //Thus, I have to 'get them back' with the coerceIn function and cannot remove them
        //But when I add a single new circle, I don't want to allow to create any circle by pressing above header and want to remove them in this case
        //Thus, I keep track with flag in which mode I currently am in and whether I can remove the new circle above header or move it down
        if (completelyNewSingleCircle && y <=  Constants.firstLineBorderY) {
            return null
        }
        //If I dont check this, then it can happen that when moving 100-circle fast to right/up/down border of 1-value that some circles are generated out of screen (can be seen when going back to 100)
        val x1 = x.coerceIn(Constants.widthCircleAndHitbox, Constants.width - Constants.widthCircleAndHitbox)
        val y1 = y.coerceIn(Constants.firstLineBorderY + Constants.widthCircleAndHitbox, Constants.height - Constants.widthCircleAndHitbox)
        val x2 = handleXInBorder(x1)
        val y2 = y1 //INFO Dont need border thing like for x2 for y2, because coerceIn handles custom border (+  not bottom, there physically not possible)
        circleDef.position.set(x2, y2)

        val body = world.createBody(circleDef)
        // INFO without this, creating many circles next to each other does not make them move until I pull first by hand
        body.applyLinearImpulse(Vector2(0.0001f, 0.0001f), body.position, true) //Impulse applies once, Force continiously
        val fixture = body.createFixture(fixtureDef)
        fixture.updateColor(columns)
        circles.add(fixture)
        return fixture
    }

    //Also check not inside hitbox of border, else it can happen that e.g. when putting 100-circle on border (to 10-circle) of 1-circle then some circles left and some right, but all green
    fun handleXInBorder(x: Float): Float {
        for (column in columns.dropLast(1)) {
            if ((x >= column.rightX - Constants.widthCircleAndHitbox) && (x <= column.rightX)) {//In border hitbox, but closer to left side
                return column.rightX - Constants.widthCircleAndHitbox
            }
            if ((x >= column.rightX) && (x <= column.rightX + Constants.widthCircleAndHitbox)) {//In border hitbox, but closer to right side
                return column.rightX + Constants.widthCircleAndHitbox
            }
        }
        return x //In no hitbox, so can draw as is
    }

    fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        val screenXNormalized = screenX * Constants.convertRatio
        val screenYNormalized = screenY * Constants.convertRatio

        if (dragCircle != null) {
            //INFO Moving on e.g. y axis ok, even if at border of y axis, so compute both seperately
            var newX = dragCircle!!.body.position.x
            var newY = dragCircle!!.body.position.y
            if ((screenXNormalized - Constants.widthCircleAndHitbox >= 0) && (screenXNormalized + Constants.widthCircleAndHitbox <= Constants.width)) {
                newX = screenXNormalized
            }

            //Ok to go out of screen at top, because then circle will be deleted
            //if((screenYNormalized- Constants.widthCircleAndHitbox >= Constants.firstLineBorderY) && (screenYNormalized + Constants.widthCircleAndHitbox <= Constants.height)){
            if (screenYNormalized + Constants.widthCircleAndHitbox <= Constants.height) {
                newY = screenYNormalized
            }
            dragCircle!!.body.setTransform(newX, newY, 0f)
            return
        }


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

fun ShapeRenderer.drawLine(v1: Vector2, v2: Vector2, width:Float) {
    this.rectLine(v1, v2, width)
}

fun ShapeRenderer.drawCircle(c: Fixture) {
    val pos = c.body.position
    this.circle(pos.x, pos.y, Constants.radiusSprite, 50) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = c.getColor()
    this.circle(pos.x, pos.y, Constants.radiusSprite - Constants.circleBoarderWidth, 20) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
    this.color = Constants.lineColor
}

//INFO I should only open shaperenderer once, because explensive, thus use that
fun ShapeRenderer.drawFilled(f: () -> Unit) {
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
fun Fixture.updateColor(columns: List<Column>) {
    val x = this.body.position.x
    for (column in columns) {
        if (column.rightX >= x) { //Works because of ordering of columns
            this.body.userData = Pair(column.color, column.value)
            return
        }
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
    //TODO Can I create once reversed list with asReversed, and use this all the time (kotlin documentation
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
