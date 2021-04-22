package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class Board: Drawable {
    val width = Gdx.graphics.width.toFloat()
    val height = Gdx.graphics.height.toFloat()

    //Cache
    val firstLineWidth = width/3f
    val secondLineWidth = (2f/3f) * width



    override fun draw(sR: ShapeRenderer) {
        sR.drawLine(Vector2(firstLineWidth, 0f), Vector2(firstLineWidth,height))
        sR.drawLine(Vector2(secondLineWidth, 0f), Vector2(secondLineWidth,height))
    }
}

fun ShapeRenderer.drawLine(v1: Vector2, v2: Vector2) {
    this.begin(ShapeRenderer.ShapeType.Line)
    this.line(v1,v2)
    this.end()
}