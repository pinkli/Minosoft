/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.font.renderer.code.AscentedCodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.code.RasterizedCodePointRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class BitmapCodeRenderer(
    override val texture: Texture,
    override var uvStart: Vec2,
    override var uvEnd: Vec2,
    override val width: Float,
    val ascent1: Int,
) : RasterizedCodePointRenderer, AscentedCodePointRenderer {
    override val ascent: Float
        get() = 1.0f
    override val descent: Float
        get() = 1.0f

    fun updateArray() {
        uvStart = uvStart * texture.array.uvEnd
        uvEnd = uvEnd * texture.array.uvEnd
    }
}
