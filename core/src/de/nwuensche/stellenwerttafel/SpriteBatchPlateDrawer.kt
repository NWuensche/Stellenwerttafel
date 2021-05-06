package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.lang.IllegalArgumentException


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture

//INFO Not significantly faster than ShapeRendererPlateDrawer (slows down after 250 circles instead of 200, 1000 circles still dont work)
//TODO When pause + resume app, all circles are black squares (maybe textures are garbage collected?)
//TODO Slowdown because of box2d physics, not because of ShapeRenderer/SpriteBatch
//TODO Sprite Smaller than ShapeRenderer, but when making bigger then margin between border and sprites is not nice anymore
class SpriteBatchPlateDrawer(private val sB: SpriteBatch) : PlateDrawer {
    override fun begin() {
        sB.begin()
    }

    override fun end() {
        sB.end()
    }

    //TODO This is directly called for flying circles, but this is slow (creating new sprite). Fix this (only needed when using SpriteBatchPlateDrawer, not when using ShapeRendererPlateDrawer)
    override fun drawPlate(pos: Vector2, c: Color) {
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

    //INFO Dont have to dispose Sprite, only Texture when not needed anymore
    private fun Fixture.getSprite(): Sprite {
        val m = this.body.userData as MutableMap<String, Any>
        if (!m.containsKey("Sprite")) {
            //TODO Store them in custom object so to not recreate sprite all the time
            val texture = when (this.getColor()) {
                Constants.mattGreen -> Constants.PlateTextures.GREEN.texture
                Constants.mattBlue -> Constants.PlateTextures.BLUE.texture
                Constants.mattRed -> Constants.PlateTextures.RED.texture
                else -> throw IllegalArgumentException(this.getColor().toString())
            }
            val s = Sprite(texture).apply {
                x = this@getSprite.body.position.x
                y = this@getSprite.body.position.x
                setSize(Constants.radiusSprite,Constants.radiusSprite) //TODO Density Based look Jewel
            }
            m["Sprite"] = s
        }
        return m["Sprite"] as Sprite
    }

    override fun drawPlate(f: Fixture) {
        val s = f.getSprite()
        //update position
        s.x = f.body.position.x
        s.y = f.body.position.y
        f.getSprite().draw(sB)
    }
}
