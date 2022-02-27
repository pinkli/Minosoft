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

package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityModel
import de.bixilon.minosoft.util.KUtil.format
import glm_.vec3.Vec3

class VisibleMeshes(val cameraPosition: Vec3 = Vec3.EMPTY) {
    val opaque: MutableList<SingleWorldMesh> = mutableListOf()
    val translucent: MutableList<SingleWorldMesh> = mutableListOf()
    val transparent: MutableList<SingleWorldMesh> = mutableListOf()
    val blockEntities: MutableList<BlockEntityModel<*>> = mutableListOf()

    val sizeString: String
        get() = "${opaque.size.format()}|${translucent.size.format()}|${transparent.size.format()}"


    fun addMesh(mesh: WorldMesh) {
        val distance = (cameraPosition - mesh.center).length2()
        mesh.opaqueMesh?.let {
            it.distance = distance
            opaque += it
        }
        mesh.translucentMesh?.let {
            it.distance = distance
            translucent += it
        }
        mesh.transparentMesh?.let {
            it.distance = distance
            transparent += it
        }
        mesh.blockEntities?.let {
            blockEntities += it
        }
    }


    fun sort() {
        opaque.sortBy { it.distance }
        translucent.sortBy { -it.distance }
        transparent.sortBy { it.distance }
    }


    fun removeMesh(mesh: WorldMesh) {
        mesh.opaqueMesh?.let { opaque -= it }
        mesh.translucentMesh?.let { translucent -= it }
        mesh.transparentMesh?.let { transparent -= it }
        mesh.blockEntities?.let { blockEntities -= it }
    }

    fun clear() {
        opaque.clear()
        translucent.clear()
        transparent.clear()
        blockEntities.clear()
    }
}
