package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.ScreenUtils


class MyGdxGame : ApplicationAdapter() {
    val camera by lazy { OrthographicCamera(Constants.width, Constants.height)
                            .apply { setToOrtho(true, Constants.width, Constants.height) }} // INFO Resets origin (0,0) to top left }
    val sR: ShapeRenderer by lazy { // Used for drawing lines,circles,...
        ShapeRenderer()
                .apply { color = Constants.lineColor }
                .apply { projectionMatrix = camera.combined }
    }
    val batch: SpriteBatch by lazy {SpriteBatch().apply { projectionMatrix = camera.combined }} //INFO Used only for text
    val font: BitmapFont by lazy {
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/liberationsans.ttf"))
        val font = createFont(fontGenerator, Constants.fontSize).apply {
            color = Constants.lineColor
            data.scale(Constants.scaleFont)
            setUseIntegerPositions(false) //Else font not visible in small box2d setting with float coordinates + not crisp
        }
        fontGenerator.dispose()
        font
    }
    //INFO Setting doSleep=false makes movement of other circles while dragging better, and both true/false lag when 200 1-values
    //INFO Furthermore, when doSleep=false no problem with possibility of putting circle directly on boarder and keeping it there
    val world: World by lazy { World(Vector2(0f, 0f), false) } //First is gravity vector, second is CPU optimization on
    var dialogValue = -1 // How many columns should table have
    val board: Board by lazy { Board(batch, sR, world, font, dialogValue) } //Dialogvalue will be set before board first init
    //val debugRenderer: Box2DDebugRenderer by lazy { Box2DDebugRenderer() }

    //INFO Always Create Font, because Bitmap.setScale() didn't worked consitently (not always same size) between devices
    //INFO And I dont like default font because it does not scale well for Box2D size
    fun createFont(ftfg: FreeTypeFontGenerator, newSize: Int): BitmapFont {

        return ftfg.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = newSize
            flip = true // turn font 180 upside down to make it readable
        })
    }

    fun dialogFinished(v: Int) {
        dialogValue = v //Will automatically draw everything with next render
    }


    override fun create() {
        Gdx.gl.glLineWidth(Constants.lineWidthOriginal)


        //This does not init lazy board, so in dialog really no background
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                board.touchUp(screenX, screenY, pointer, button)
                return true
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                board.touchDown(screenX, screenY, pointer, button)
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                board.touchDragged(screenX, screenY, pointer)
                return true
            }
        }
    }



    override fun render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f) //White Background

        if (dialogValue != -1) {
            board.draw()
            doPhysicsStep(Gdx.graphics.deltaTime)
        }
        //debugRenderer.render(world, camera.combined) //INFO Should always be done !after! drawing graphics
    }

    private var accumulator = 0f

    private fun doPhysicsStep(deltaTime: Float) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        val frameTime = deltaTime.coerceAtMost(0.25f)
        accumulator += frameTime
        while (accumulator >= Constants.timeStep) {
            world.step(Constants.timeStep, 6, 2)
            accumulator -= Constants.timeStep
        }
    }

    override fun dispose() {
        sR.dispose()
        batch.dispose()
        font.dispose()
        world.dispose()
    }
}