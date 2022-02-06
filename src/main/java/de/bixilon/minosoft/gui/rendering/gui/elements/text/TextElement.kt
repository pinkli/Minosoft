/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.renderer.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.InfiniteSizeElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.offset
import glm_.vec2.Vec2i

open class TextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    override var fontAlignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    background: Boolean = true,
    var backgroundColor: RGBColor = RenderConstants.TEXT_BACKGROUND_COLOR,
    noBorder: Boolean = false,
    parent: Element? = null,
    scale: Float = 1.0f,
) : LabeledElement(guiRenderer) {
    lateinit var renderInfo: TextRenderInfo
        private set

    // ToDo: Reapply if backgroundColor or fontAlignment changes

    var scale: Float = scale
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var background: Boolean = background
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var noBorder: Boolean = noBorder
        @Synchronized set(value) {
            if (field == value) {
                return
            }
            field = value
            applyNoBorder()
            forceApply()
        }
    var charHeight: Int = 0
        private set
    var charMargin: Int = 0
        private set

    override var size: Vec2i
        get() = super.size
        set(value) {}

    override var text: Any = text
        set(value) {
            chatComponent = ChatComponent.of(value, translator = Minosoft.LANGUAGE_MANAGER /*guiRenderer.connection.language*/) // Should the server be allowed to send minosoft namespaced translation keys?
            field = value
        }

    private var emptyMessage: Boolean = true

    private var _chatComponent: ChatComponent = unsafeNull()
        set(value) {
            if (value == field) {
                return
            }
            field = value
            emptyMessage = value.message.isEmpty()
            val prefSize = Vec2i.EMPTY
            if (!emptyMessage) {
                val renderInfo = TextRenderInfo(
                    fontAlignment = fontAlignment,
                    charHeight = charHeight,
                    charMargin = charMargin,
                    scale = scale,
                )
                ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, prefSize, 0, InfiniteSizeElement(guiRenderer), renderWindow, null, null, renderInfo, value)
            }
            _prefSize = prefSize
        }

    override var chatComponent: ChatComponent
        get() = _chatComponent
        protected set(value) {
            _chatComponent = value
            forceApply()
        }

    init {
        this._parent = parent
        applyNoBorder()
        this._chatComponent = ChatComponent.of(text)
        forceSilentApply()
    }

    private fun applyNoBorder() {
        charHeight = (noBorder.decide(Font.CHAR_HEIGHT, Font.TOTAL_CHAR_HEIGHT) * scale).toInt()
        charMargin = (noBorder.decide(0, Font.CHAR_MARGIN) * scale).toInt()
    }

    override fun forceSilentApply() {
        val size = Vec2i.EMPTY
        val renderInfo = TextRenderInfo(
            fontAlignment = fontAlignment,
            charHeight = charHeight,
            charMargin = charMargin,
            scale = scale,
        )
        if (!emptyMessage) {
            ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, size, 0, this, renderWindow, null, null, renderInfo, chatComponent)
            renderInfo.currentLineNumber = 0
        }
        this.renderInfo = renderInfo

        this.cacheUpToDate = false
        _size = size
    }

    override fun onChildChange(child: Element) = error("A TextElement can not have a child!")

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        if (emptyMessage) {
            return 0
        }
        val initialOffset = offset + margin.offset

        ChatComponentRenderer.render(initialOffset, Vec2i(initialOffset), Vec2i.EMPTY, z + 1, this, renderWindow, consumer, options, renderInfo, chatComponent)
        renderInfo.currentLineNumber = 0

        if (background) {
            for ((line, info) in renderInfo.lines.withIndex()) {
                val start = initialOffset + Vec2i(fontAlignment.getOffset(size.x, info.width), line * charHeight)
                consumer.addQuad(start, start + Vec2i(info.width + charMargin, charHeight), z, renderWindow.WHITE_TEXTURE, backgroundColor, options)
            }
        }

        return LAYERS
    }

    override fun toString(): String {
        return chatComponent.toString()
    }

    companion object {
        const val LAYERS = 5 // 1 layer for the text, 1 for strikethrough, * 2 for shadow, 1 for background
    }
}
