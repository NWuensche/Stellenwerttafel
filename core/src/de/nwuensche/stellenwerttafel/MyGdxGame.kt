package de.nwuensche.stellenwerttafel

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
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
    //TODO Setting doSleep=false makes movement of other circles while dragging better
    val world: World by lazy { World(Vector2(0f, 0f), false) } //First is gravity vector, second is CPU optimization on
    val board: Board by lazy { Board(sR, world) }
    val debugRenderer: Box2DDebugRenderer by lazy { Box2DDebugRenderer() } //TODO Use real renderer


    override fun create() {
        Gdx.gl.glLineWidth(Constants.lineWidthOriginal)

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                //TODO Interssting if (img.getBoundingRectangle().contains(screenX, screenY)) println("Image Clicked")
                board.touchUp(screenX, screenY, pointer, button) //TODO Alex ist das touchUp oder touchDown?
                //TODO Alex Touch Drag auf nichts erstellt am Ende auch Button oder nicht?
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                board.touchDragged(screenX, screenY, pointer) //TODO Alex ist das touchUp oder touchDown?
                return true
            }
        }

        createBordersBox2D() // Should be after drawing all graphics, else could get out of sync
    }

    private fun createBordersBox2D() {
        val borderDef = BodyDef()
        borderDef.position.set(Vector2(0f, Constants.height))
        val groundBody = world.createBody(borderDef)
        val groundBox = PolygonShape()
        groundBox.setAsBox(Constants.width, Constants.lineWidth) //INFO Takes half values as inputs, but I want margin around drawn lines, so 'normal' is ok
        groundBody.createFixture(groundBox, 0.0f)

        borderDef.position.set(Vector2(0f, 0f))
        val ceilBody = world.createBody(borderDef)
        val ceilBox = PolygonShape()
        ceilBox.setAsBox(Constants.width, Constants.lineWidth)
        ceilBody.createFixture(ceilBox, 0.0f)

        borderDef.position.set(Vector2(0f, 0f))
        val leftBody = world.createBody(borderDef)
        val leftBox = PolygonShape()
        leftBox.setAsBox(Constants.lineWidth, Constants.height)
        leftBody.createFixture(leftBox, 0.0f)

        borderDef.position.set(Vector2(Constants.width, 0f))
        val rightBody = world.createBody(borderDef)
        val rightBox = PolygonShape()
        rightBox.setAsBox(Constants.lineWidth, Constants.height)
        rightBody.createFixture(rightBox, 0.0f)

        borderDef.position.set(Vector2(Constants.firstLineBorderWidth, 0f))
        val firstLineBorderBody = world.createBody(borderDef)
        val firstLineBorderBox = PolygonShape()
        firstLineBorderBox.setAsBox(Constants.lineWidth, Constants.height)
        firstLineBorderBody.createFixture(firstLineBorderBox, 0.0f)

        borderDef.position.set(Vector2(Constants.secondLineBorderWidth, 0f))
        val secondLineBorderBody = world.createBody(borderDef)
        val secondLineBorderBox = PolygonShape()
        secondLineBorderBox.setAsBox(Constants.lineWidth, Constants.height)
        secondLineBorderBody.createFixture(secondLineBorderBox, 0.0f)
    }

    override fun render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f) //White Background
        board.draw()
        doPhysicsStep(Gdx.graphics.deltaTime)
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
    }
}