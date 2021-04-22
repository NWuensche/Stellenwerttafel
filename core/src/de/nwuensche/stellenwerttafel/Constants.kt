package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color

object Constants {
    val width = 1f // INFO Box2D wants small camera value (not pixels x pixels, but 1 x normalzied), then movements is also fast
    val height = Gdx.graphics.height.toFloat() / Gdx.graphics.width
    val convertRatio = 1f/Gdx.graphics.width

    //TODO Relative to width/height
    val lineWidth = 10f
    val lineWidthNormalized = 10f * convertRatio
    val radius = 50f * convertRatio

    val timeStep = 1/60f

    val lineColor = Color.BLACK
    val circleRedColor = Color.RED
}