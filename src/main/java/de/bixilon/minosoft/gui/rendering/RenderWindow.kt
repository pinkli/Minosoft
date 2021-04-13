/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.WorldRenderer
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.hud.elements.input.KeyConsumer
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.modding.event.EventInvokerCallback
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketPlayerPositionAndRotation
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.concurrent.ConcurrentLinkedQueue

class RenderWindow(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    private val keyBindingCallbacks: MutableMap<ResourceLocation, Pair<KeyBinding, MutableSet<((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)>>> = mutableMapOf()
    private val keysDown: MutableSet<KeyCodes> = mutableSetOf()
    private val keyBindingDown: MutableSet<KeyBinding> = mutableSetOf()
    val renderStats = RenderStats()
    var screenDimensions = Vec2i(900, 500)
        private set
    var screenDimensionsF = Vec2(screenDimensions)
        private set

    private var windowId = 0L
    private var deltaFrameTime = 0.0 // time between current frame and last frame

    private var lastFrame = 0.0
    val camera: Camera = Camera(connection, Minosoft.getConfig().config.game.camera.fov, this)
    private val latch = CountUpAndDownLatch(1)

    private var renderingStatus = RenderingStates.RUNNING

    private var polygonEnabled = false
    private var mouseCatch = !StaticConfiguration.DEBUG_MODE

    private val screenshotTaker = ScreenshotTaker(this)
    val tintColorCalculator = TintColorCalculator(connection.world)
    val font = Font()
    val textures = TextureArray(mutableListOf())

    // all renderers
    val worldRenderer: WorldRenderer = WorldRenderer(connection, connection.world, this)
    val hudRenderer: HUDRenderer = HUDRenderer(connection, this)

    val renderQueue = ConcurrentLinkedQueue<Runnable>()

    private var _currentInputConsumer: KeyConsumer? = null
    val currentElement: MutableList<ResourceLocation> = mutableListOf(KeyBindingsNames.WHEN_IN_GAME, KeyBindingsNames.WHEN_PLAYER_IS_FLYING)

    private var skipNextChatPress = false

    lateinit var WHITE_TEXTURE: TextureLike

    var currentKeyConsumer: KeyConsumer?
        get() = _currentInputConsumer
        set(value) {
            _currentInputConsumer = value
            for ((_, binding) in keyBindingCallbacks) {
                if (!keyBindingDown.contains(binding.first)) {
                    continue
                }
                if (!binding.first.action.containsKey(KeyAction.TOGGLE) && !binding.first.action.containsKey(KeyAction.CHANGE)) {
                    continue
                }

                for (keyCallback in binding.second) {
                    keyCallback.invoke(KeyCodes.KEY_UNKNOWN, KeyAction.RELEASE)
                }
            }
            // ToDo: move to mouse consumer
            if (value == null) {
                if (mouseCatch) {
                    renderQueue.add { glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED) }
                }
            } else {
                renderQueue.add { glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL) }
            }
            keyBindingDown.clear()
        }

    init {
        connection.registerEvent(EventInvokerCallback<ConnectionStateChangeEvent> {
            if (it.connection.isDisconnected) {
                renderQueue.add {
                    glfwSetWindowShouldClose(windowId, true)
                }
            }
        })
        connection.registerEvent(EventInvokerCallback<PacketReceiveEvent> {
            val packet = it.packet
            if (packet !is PacketPlayerPositionAndRotation) {
                return@EventInvokerCallback
            }
            if (latch.count > 0) {
                latch.countDown()
            }
            renderQueue.add {
                camera.setPosition(packet.position)
                camera.setRotation(packet.rotation.yaw, packet.rotation.pitch)
            }
        })
    }

    fun init(latch: CountUpAndDownLatch) {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize  Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        windowId = glfwCreateWindow(screenDimensions.x, screenDimensions.y, "Minosoft", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowId == MemoryUtil.NULL) {
            glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
        camera.init(this)

        tintColorCalculator.init(connection.assetsManager)


        glfwSetKeyCallback(this.windowId) { _: Long, key: Int, _: Int, action: Int, _: Int ->
            val keyCode = KeyCodes.KEY_CODE_GLFW_ID_MAP[key] ?: KeyCodes.KEY_UNKNOWN
            val keyAction = when (action) {
                GLFW_PRESS -> KeyAction.PRESS
                GLFW_RELEASE -> KeyAction.RELEASE
                // ToDo: Double, Hold
                else -> return@glfwSetKeyCallback
            }
            if (keyAction == KeyAction.PRESS) {
                keysDown.add(keyCode)
            } else if (keyAction == KeyAction.RELEASE) {
                keysDown.remove(keyCode)
            }

            if (keyAction == KeyAction.PRESS) {
                // ToDo: Repeatable keys, long holding, etc

                currentKeyConsumer?.keyInput(keyCode)
            }

            for ((_, keyCallbackPair) in keyBindingCallbacks) {
                run {
                    val keyBinding = keyCallbackPair.first
                    val keyCallbacks = keyCallbackPair.second


                    var andWhenValid = keyBinding.`when`.isEmpty()
                    for (or in keyBinding.`when`) {
                        var andValid = true
                        for (and in or) {
                            if (!currentElement.contains(and)) {
                                andValid = false
                                break
                            }
                        }
                        if (andValid) {
                            andWhenValid = true
                            break
                        }
                    }
                    if (!andWhenValid) {
                        return@run
                    }

                    var anyCheckRun = false

                    keyBinding.action[KeyAction.MODIFIER]?.let {
                        val previousKeysDown = if (keyAction == KeyAction.RELEASE) {
                            val previousKeysDown = keysDown.toMutableList()
                            previousKeysDown.add(keyCode)
                            previousKeysDown
                        } else {
                            keysDown
                        }
                        if (!previousKeysDown.containsAll(it)) {
                            return@run
                        }
                        anyCheckRun = true
                    }
                    keyBinding.action[KeyAction.CHANGE]?.let {
                        if (!it.contains(keyCode)) {
                            return@run
                        }
                        anyCheckRun = true
                    }

                    // release or press
                    if (keyBinding.action[KeyAction.CHANGE] == null) {
                        keyBinding.action[keyAction].let {
                            if (it == null) {
                                return@run
                            }
                            if (!it.contains(keyCode)) {
                                return@run
                            }
                            anyCheckRun = true
                        }
                    }

                    if (!anyCheckRun) {
                        return@run
                    }

                    if (keyAction == KeyAction.PRESS) {
                        keyBindingDown.add(keyBinding)
                    } else if (keyAction == KeyAction.RELEASE) {
                        keyBindingDown.remove(keyBinding)
                    }
                    for (keyCallback in keyCallbacks) {
                        keyCallback.invoke(keyCode, keyAction)
                        skipNextChatPress = true
                    }
                }
            }
        }

        glfwSetCharCallback(windowId) { _: Long, char: Int ->
            if (skipNextChatPress) {
                skipNextChatPress = false
                return@glfwSetCharCallback
            }
            currentKeyConsumer?.charInput(char.toChar())
        }

        if (mouseCatch) {
            glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetCursorPosCallback(windowId) { _: Long, xPos: Double, yPos: Double -> camera.mouseCallback(xPos, yPos) }
        MemoryStack.stackPush().let { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(windowId, (videoMode.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId)
        // Enable v-sync
        glfwSwapInterval(1)


        // Make the window visible
        GL.createCapabilities()

        setSkyColor(RGBColor("#fffe7a"))

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_CULL_FACE)


        textures.allTextures.add(Texture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION))
        WHITE_TEXTURE = TextureLikeTexture(
            texture = Texture(ResourceLocation("minosoft:textures/white.png")),
            uvStart = Vec2(0, 0),
            uvEnd = Vec2(1.0f, 1.0f),
            size = Vec2i(16, 16)
        )
        textures.allTextures.add(WHITE_TEXTURE.texture)

        font.load(connection.assetsManager)

        font.preLoadAtlas(textures)

        worldRenderer.init()
        hudRenderer.init()


        textures.preLoad(connection.assetsManager)

        font.loadAtlas()
        textures.load()


        worldRenderer.postInit()
        hudRenderer.postInit()


        glfwSetWindowSizeCallback(windowId, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0, 0, width, height)
                screenDimensions = Vec2i(width, height)
                screenDimensionsF = Vec2(screenDimensions)
                camera.screenChangeResizeCallback()
                hudRenderer.screenChangeResizeCallback(screenDimensions)
            }
        })

        glfwSetWindowFocusCallback(windowId, object : GLFWWindowFocusCallback() {
            override fun invoke(window: Long, focused: Boolean) {
                setRenderStatus(if (focused) {
                    RenderingStates.RUNNING
                } else {
                    RenderingStates.SLOW
                })
            }
        })

        glfwSetWindowIconifyCallback(windowId, object : GLFWWindowIconifyCallback() {
            override fun invoke(window: Long, iconified: Boolean) {
                setRenderStatus(if (iconified) {
                    RenderingStates.PAUSED
                } else {
                    RenderingStates.RUNNING
                })
            }
        })


        registerGlobalKeyCombinations()

        hudRenderer.screenChangeResizeCallback(screenDimensions)

        camera.addShaders(worldRenderer.chunkShader)

        camera.screenChangeResizeCallback()

        glEnable(GL_DEPTH_TEST)

        Log.debug("Rendering is prepared and ready to go!")
        latch.countDown()
        latch.waitUntilZero()
        this.latch.waitUntilZero()
        glfwShowWindow(windowId)
    }

    private fun registerGlobalKeyCombinations() {
        registerKeyCallback(KeyBindingsNames.DEBUG_POLYGON) { _: KeyCodes, _: KeyAction ->
            polygonEnabled = !polygonEnabled
            glPolygonMode(GL_FRONT_AND_BACK, if (polygonEnabled) {
                GL_LINE
            } else {
                GL_FILL
            })
            sendDebugMessage("Toggled polygon mode!")
        }
        registerKeyCallback(KeyBindingsNames.DEBUG_MOUSE_CATCH) { _: KeyCodes, _: KeyAction ->
            mouseCatch = !mouseCatch
            if (mouseCatch) {
                glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            } else {
                glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
            }
            sendDebugMessage("Toggled mouse catch!")
        }
        registerKeyCallback(KeyBindingsNames.QUIT_RENDERING) { _: KeyCodes, _: KeyAction ->
            glfwSetWindowShouldClose(windowId, true)
        }
        registerKeyCallback(KeyBindingsNames.TAKE_SCREENSHOT) { _: KeyCodes, _: KeyAction ->
            screenshotTaker.takeScreenshot()
        }
    }

    fun startRenderLoop() {
        while (!glfwWindowShouldClose(windowId)) {
            if (renderingStatus == RenderingStates.PAUSED) {
                Thread.sleep(100L)
                glfwPollEvents()
                continue
            }
            renderStats.startFrame()
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            val currentFrame = glfwGetTime()
            deltaFrameTime = currentFrame - lastFrame
            lastFrame = currentFrame


            textures.animator.draw()


            worldRenderer.draw()
            hudRenderer.draw()

            renderStats.endDraw()


            glfwSwapBuffers(windowId)
            glfwPollEvents()
            camera.draw()
            camera.handleInput(deltaFrameTime)

            // handle opengl context tasks, but limit it per frame
            var actionsDone = 0
            for (renderQueueElement in renderQueue) {
                if (actionsDone == RenderConstants.MAXIMUM_CALLS_PER_FRAME) {
                    break
                }
                renderQueueElement.run()
                renderQueue.remove(renderQueueElement)
                actionsDone++
            }

            when (renderingStatus) {
                RenderingStates.SLOW -> Thread.sleep(100L)
                RenderingStates.RUNNING, RenderingStates.PAUSED -> {
                }
                RenderingStates.STOPPED -> glfwSetWindowShouldClose(windowId, true)
            }
            renderStats.endFrame()

            if (RenderConstants.SHOW_FPS_IN_WINDOW_TITLE) {
                glfwSetWindowTitle(windowId, "Minosoft | FPS: ${renderStats.fpsLastSecond}")
            }
        }
    }

    fun exit() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowId)
        glfwDestroyWindow(windowId)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()

        // disconnect
        connection.disconnect()
    }

    private fun setRenderStatus(renderingStatus: RenderingStates) {
        if (renderingStatus == this.renderingStatus) {
            return
        }
        if (this.renderingStatus == RenderingStates.PAUSED) {
            renderQueue.clear()
            worldRenderer.refreshChunkCache()
        }
        this.renderingStatus = renderingStatus
    }

    fun registerKeyCallback(resourceLocation: ResourceLocation, callback: ((keyCode: KeyCodes, keyEvent: KeyAction) -> Unit)) {
        var resourceLocationCallbacks = keyBindingCallbacks[resourceLocation]?.second
        if (resourceLocationCallbacks == null) {
            resourceLocationCallbacks = mutableSetOf()
            val keyBinding = Minosoft.getConfig().config.game.controls.keyBindings.entries[resourceLocation] ?: return
            keyBindingCallbacks[resourceLocation] = Pair(keyBinding, resourceLocationCallbacks)
        }
        resourceLocationCallbacks.add(callback)
    }

    fun setSkyColor(color: RGBColor) {
        glClearColor(color.floatRed, color.floatGreen, color.floatBlue, 1.0f)
    }

    fun sendDebugMessage(message: String) {
        connection.sender.sendFakeChatMessage(RenderConstants.DEBUG_MESSAGES_PREFIX + message)
    }

    fun unregisterKeyBinding(it: ResourceLocation) {
        keyBindingCallbacks.remove(it)
    }
}
