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




    //TODO Relative to width? Check with emulator + Other smartphones like Alex
    //TODO radius 2x basierend auf density
    val density = Gdx.graphics.density
    val lineWidthOriginal = 10f //Only use this to set line width for ShapeRenderer, for everything else use other lineWidth

    val lineWidth = 10f * convertRatio

    val radiusSprite = 50f * convertRatio //Radius for drawn circle
    val radiusHitBox = 20f * convertRatio //Radius for box2d box circle, has to be smaller because otherwise 100 1-circles wobble too much
    val marginCircle = radiusSprite - radiusHitBox
    val ratioLineWidth = 0.5
    val widthHitBoxBorders = marginCircle + (ratioLineWidth * lineWidth).toFloat() //Need margin so that circle + border dont overlap because circle spriteradius != hitbox radius + used to not drag circle out of screen
    val widthCircleAndHitbox = radiusSprite + (ratioLineWidth * lineWidth).toFloat()

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

    val fontSize = (density * 35).toInt() //TODO End check emulator + Mama/Alex if this wors fine on other displays as well
    val scaleFont = 1f/Gdx.graphics.width - 1

    //delete button dimensions
    val buttonX = 0.9f * width
    val buttonY = lineWidth
    val buttonWidth = width - (buttonX + lineWidth)
    val buttonHeight = secondLineBorderY - 2.5f*lineWidth //INFO .5 because of linewidth of secondLineBorderY, so that margin top and bottom of button are same height
    val buttonCornerRadius = 0.005f
}