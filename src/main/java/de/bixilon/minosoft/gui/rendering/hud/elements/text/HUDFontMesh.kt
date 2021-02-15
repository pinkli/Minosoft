package de.bixilon.minosoft.gui.rendering.hud.elements.text

import glm_.BYTES
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class HUDFontMesh(data: FloatArray) {
    var vAO: Int = glGenVertexArrays()
    var vBO: Int = glGenBuffers()
    var trianglesCount: Int = data.size / BYTES_PER_VERTEX

    fun draw() {
        glBindVertexArray(vAO)
        glDrawArrays(GL_TRIANGLES, 0, trianglesCount)
    }

    fun unload() {
        glDeleteVertexArrays(vAO)
        glDeleteBuffers(vBO)
    }

    init {
        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vAO)
        glBindBuffer(GL_ARRAY_BUFFER, vBO)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, BYTES_PER_VERTEX * Float.BYTES, 0L)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, BYTES_PER_VERTEX * Float.BYTES, (3 * Float.BYTES).toLong())
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(2, 1, GL_FLOAT, false, BYTES_PER_VERTEX * Float.BYTES, (5 * Float.BYTES).toLong())
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(3, 1, GL_FLOAT, false, BYTES_PER_VERTEX * Float.BYTES, (6 * Float.BYTES).toLong())
        glEnableVertexAttribArray(3)

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    companion object {
        private const val BYTES_PER_VERTEX = 7
    }
}
