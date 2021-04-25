package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color

object Constants {
    val width = 1f // INFO Box2D wants small camera value (not pixels x pixels, but 1 x normalzied), then movements is also fast
    val height = Gdx.graphics.height.toFloat() / Gdx.graphics.width
    val convertRatio = 1f/Gdx.graphics.width

    //2 borders for board x position
    val firstLineBorderX = width / 3f
    val secondLineBorderX = (2 / 3f) * width
    //2 borders for header
    val firstLineBorderY = height/4f
    val secondLineBorderY = height/8f




    val density = Gdx.graphics.density
    val lineWidthOriginal =  4 * density //Only use this to set line width for ShapeRenderer, for everything else use other lineWidth

    val lineWidth = lineWidthOriginal * convertRatio

    val ratioLineWidthBorderCircle = 0.5f
    val circleBoarderWidth = ratioLineWidthBorderCircle * lineWidth // Circles with thick boarders look bad, so reduce width
    val radiusSprite = 30f * density *  convertRatio //Radius for drawn circle, depend on density because very small when using Alex phone
    val radiusHitBox = 12f * density * convertRatio //Radius for box2d box circle, has to be smaller because otherwise 100 1-circles wobble too much
    val marginCircle = radiusSprite - radiusHitBox
    val widthHitBoxBorders = marginCircle + (ratioLineWidthBorderCircle * lineWidth).toFloat() //Need margin so that circle + border dont overlap because circle spriteradius != hitbox radius + used to not drag circle out of screen
    val widthCircleAndHitbox = radiusSprite + (ratioLineWidthBorderCircle * lineWidth).toFloat()

    val timeStep = 1/60f
    val speedFactor = 2 //Move circle in 1/x seconds
    val offset = 0.1f //Add to coordinates when removing circle so that flying circle already offscreen when it gets destroyed

    val lineColor = Color.BLACK
    val fontColorButton = Color.WHITE
    val overflowColor = Color.RED // Color when e.g. 11 tens or 10 ones
    val circle100Color = Color.RED
    val circle10Color = Color.BLUE
    val circle1Color = Color.GREEN
    val circle100Value = 100
    val circle10Value = 10
    val circle1Value = 1
    val valuesColumns = listOf(1, 10, 100, 1000)
    val colorsColumns = listOf(Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW) //TODO Is yellow=100 correct?
    //TODO Remove all unnecesary constant colors/values/coordinates

    val fontSize = (density * 35).toInt()
    val scaleFont = 1f/Gdx.graphics.width - 1

    //delete button dimensions
    val buttonX = 0.9f * width
    val buttonY = lineWidth
    val buttonWidth = width - (buttonX + lineWidth)
    val buttonHeight = secondLineBorderY - 2.5f*lineWidth //INFO .5 because of linewidth of secondLineBorderY, so that margin top and bottom of button are same height
    val buttonCornerRadius = 0.005f
}