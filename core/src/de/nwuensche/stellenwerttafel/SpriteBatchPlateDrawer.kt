package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.lang.IllegalArgumentException


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class SpriteBatchPlateDrawer(val sB: SpriteBatch) : PlateDrawer {
    override fun drawPlate(pos: Vector2, c: Color) {
        //TODO Store them in custom object so to not recreate sprite all the time
        val texture = when {
            c == Constants.mattGreen -> Constants.PlateTextures.GREEN.texture
            c == Constants.mattBlue -> Constants.PlateTextures.BLUE.texture
            c == Constants.mattRed -> Constants.PlateTextures.RED.texture
            else -> throw IllegalArgumentException(c.toString())
        }
        val s = Sprite(texture).apply {
            x = pos.x
            y = pos.y
            setSize(0.05f,0.05f) //TODO Density Based look Jewel
        }
        s.draw(sB)
    }
}
