package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.lang.IllegalArgumentException


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture

class SpriteBatchPlateDrawer(private val sB: SpriteBatch) : PlateDrawer {
    override fun begin() {
        sB.begin()
    }

    override fun end() {
        sB.end()
    }

    override fun drawPlate(pos: Vector2, c: Color) {
        //TODO Store them in custom object so to not recreate sprite all the time
        val texture = when (c) {
            Constants.mattGreen -> Constants.PlateTextures.GREEN.texture
            Constants.mattBlue -> Constants.PlateTextures.BLUE.texture
            Constants.mattRed -> Constants.PlateTextures.RED.texture
            else -> throw IllegalArgumentException(c.toString())
        }
        val s = Sprite(texture).apply {
            x = pos.x
            y = pos.y
            setSize(Constants.radiusSprite,Constants.radiusSprite) //TODO Density Based look Jewel
        }
        s.draw(sB)
    }

    override fun drawPlate(f: Fixture) {
        drawPlate(f.body.position, f.getColor())
    }
}
