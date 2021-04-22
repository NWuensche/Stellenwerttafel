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
    val camera by lazy { OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
                            .apply { setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()) }} // INFO Resets origin (0,0) to top left }
    val sR: ShapeRenderer by lazy { // Used for drawing lines,circles,...
        ShapeRenderer()
                .apply { color = Constants.lineColor }
                .apply { projectionMatrix = camera.combined }
    }
    val world: World by lazy { World(Vector2(0f, 0f), true) } //First is gravity vector, second is CPU optimization on
    val board: Board by lazy { Board(sR, world) }
    val debugRenderer: Box2DDebugRenderer by lazy { Box2DDebugRenderer() } //TODO Use real renderer


    override fun create() {
        Gdx.gl.glLineWidth(Constants.lineWidth)

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

        createBordersBox2D()

        val bodyDef = BodyDef()
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody
// Set our body's starting position in the world
// Set our body's starting position in the world
        bodyDef.position.set(100f, 300f)

// Create our body in the world using our body definition

// Create our body in the world using our body definition
        val body: Body = world.createBody(bodyDef)

        bodyDef.position.set(101f, 300f)
        val body2: Body = world.createBody(bodyDef)

        bodyDef.position.set(100f, 295f)
        val body3: Body = world.createBody(bodyDef)


// Create a circle shape and set its radius to 6

// Create a circle shape and set its radius to 6
        val circle = CircleShape()
        circle.radius = 50f

// Create a fixture definition to apply our shape to

// Create a fixture definition to apply our shape to
        val fixtureDef = FixtureDef()
        fixtureDef.shape = circle
        fixtureDef.density = 0.2f
        fixtureDef.friction = 0.1f
        fixtureDef.restitution = 0.6f // Make it bounce a little bit


// Create our fixture and attach it to the body

// Create our fixture and attach it to the body
        val fixture: Fixture = body.createFixture(fixtureDef)
        val fixture2: Fixture = body2.createFixture(fixtureDef)
        val fixture3: Fixture = body3.createFixture(fixtureDef)

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
    }

    override fun render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f) //White Background
        board.draw()
        doPhysicsStep(Gdx.graphics.deltaTime)
        debugRenderer.render(world, camera.combined) //INFO Should always be done !after! drawing graphics
    }

    private var accumulator = 0f

    private fun doPhysicsStep(deltaTime: Float) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        val frameTime = deltaTime.coerceAtMost(0.25f)
        accumulator += frameTime
        while (accumulator >= Constants.timeStep) {
            world.step(Constants.timeStep, 6, 10)
            accumulator -= Constants.timeStep
        }
    }

    override fun dispose() {
        sR.dispose()
    }
}