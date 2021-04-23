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

    //TODO Relative to width? Check with emulator + Other smartphones like Alex
    val lineWidthOriginal = 10f //Only use this to set line width for ShapeRenderer, for everything else use other lineWidth

    val lineWidth = 10f * convertRatio
    val radius = 50f * convertRatio

    val timeStep = 1/60f
    val speedFactor = 2 //Move circle in 1/x seconds

    val lineColor = Color.BLACK
    val circleRedColor = Color.RED
    val circleBlueColor = Color.BLUE
    val circleGreenColor = Color.GREEN
    val circleRedValue = 100
    val circleBlueValue = 10
    val circleGreenValue = 1
}