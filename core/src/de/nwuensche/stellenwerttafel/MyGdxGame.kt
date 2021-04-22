package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter() {
    val sR: ShapeRenderer by lazy { // Used for drawing lines,circles,...
        val camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
                .apply { setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()) } // INFO Resets origin (0,0) to top left

        ShapeRenderer()
                .apply { color = Color.BLACK }
                .apply { projectionMatrix = camera.combined }
    }
    val board: Board by lazy { Board() }

    override fun create() {
        Gdx.gl.glLineWidth(10f)

    }

    override fun render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f) //White Background
        board.draw(sR)
    }

    override fun dispose() {
        sR.dispose()
    }
}