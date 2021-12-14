package de.bixilon.minosoft.gui.rendering.framebuffer.defaultf

import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class DefaultFramebuffer(
    private val renderWindow: RenderWindow,
) : Drawable {
    private var shader = renderWindow.renderSystem.createShader("minosoft:framebuffer/default".toResourceLocation())
    val framebuffer: Framebuffer = renderWindow.renderSystem.createFramebuffer()
    private val mesh = DefaultFramebufferMesh(renderWindow)

    fun init() {
        framebuffer.init()
        shader.load()
        mesh.load()
    }

    override fun draw() {
        renderWindow.renderSystem.framebuffer = null
        framebuffer.bindTexture()
        shader.use()
        shader.setInt("uTexture", 0)
        mesh.draw()
    }
}
