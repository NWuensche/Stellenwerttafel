package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter() {
    lateinit var batch: SpriteBatch
    var img: Texture? = null
    //lateinit var
    //lateinit var board: Board
    val board: Board by lazy { Board() }
    override fun create() {
        batch = SpriteBatch()
        //board = Board()
    }

    override fun render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f) //White Background
        batch.begin()
        board.draw(batch)
        batch.end()
    }

    override fun dispose() {
        //batch!!.dispose()
        //img!!.dispose()
    }
}