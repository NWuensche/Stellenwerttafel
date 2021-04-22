package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch

class Board: Drawable {
    override fun draw(batch: Batch) {
        val img = Texture("badlogic.jpg")
        batch.draw(img, 0f, 0f)
    }
}