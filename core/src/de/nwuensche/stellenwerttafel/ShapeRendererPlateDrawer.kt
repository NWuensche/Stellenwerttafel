package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture

class ShapeRendererPlateDrawer(val sR: ShapeRenderer) : PlateDrawer {
    override fun begin() {
        sR.begin(ShapeRenderer.ShapeType.Filled)
    }

    override fun end() {
        sR.end()
    }

    override fun drawPlate(pos: Vector2, c: Color) {
        sR.run {
            this.circle(pos.x, pos.y, Constants.radiusSprite, 50) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = c
            this.circle(pos.x, pos.y, Constants.radiusSprite - Constants.circleBoarderWidth, 20) //INFO With Segments, circle border much smoother + For me only way to get them actually drawn when using Box2D, otherwise invisible or completely strange forms
            this.color = Constants.lineColor
        }
    }

    override fun drawPlate(f: Fixture) {
        drawPlate(f.body.position, f.getColor())
    }
}