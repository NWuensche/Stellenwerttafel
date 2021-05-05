package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Fixture

class ShapeRendererPlateDrawer(val sR: ShapeRenderer) : PlateDrawer {
    override fun drawPlate(c: Fixture) {
        val pos = c.body.position
        sR.run {
            this.circle(pos.x, pos.y, Constants.radiusSprite, 50) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = c.getColor()
            this.circle(pos.x, pos.y, Constants.radiusSprite - Constants.circleBoarderWidth, 20) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = Constants.lineColor
        }
    }
}