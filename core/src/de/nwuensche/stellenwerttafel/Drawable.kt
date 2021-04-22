package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.glutils.ShapeRenderer

interface Drawable {
    //INFO Problem when batch + ShapeRenderer both are open at the same time (both begin()), use after another
    fun draw(sR: ShapeRenderer)
}