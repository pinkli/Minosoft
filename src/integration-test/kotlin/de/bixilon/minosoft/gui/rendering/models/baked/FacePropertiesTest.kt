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

package de.bixilon.minosoft.gui.rendering.models.baked

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class FacePropertiesTest {

    private fun BlockRender.assertProperties(direction: Directions, vararg properties: FaceProperties) {
        val actual = this.getProperties(direction) ?: return assertEquals(properties.size, 0)

        assertEquals(actual.faces, properties)
    }

    fun `full cube`() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertProperties(Directions.DOWN, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.UP, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.NORTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.SOUTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.WEST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.EAST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
    }

    fun `smaller cube`() {
        val from = Vec3(0.1f)
        val to = Vec3(0.9f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertProperties(Directions.DOWN)
        baked.assertProperties(Directions.UP)
        baked.assertProperties(Directions.NORTH)
        baked.assertProperties(Directions.SOUTH)
        baked.assertProperties(Directions.WEST)
        baked.assertProperties(Directions.EAST)
    }

}
