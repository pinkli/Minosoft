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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Instruments
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.NoteParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class NoteBlockBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    private val noteParticleType = connection.registries.particleTypeRegistry[NoteParticle]
    var instrument: Instruments? = null
        private set
    var pitch: Int? = null
        private set
    private var showParticleNextTick = false

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        instrument = when (data1.toInt()) {
            0 -> Instruments.HARP
            1 -> Instruments.BASS
            2 -> Instruments.SNARE
            3 -> Instruments.BANJO // ToDo: Was CLICKS_STICKS before
            4 -> Instruments.BASE_DRUM
            else -> null
        }

        pitch = data2.toInt()

        showParticleNextTick = true
        // ToDo: Play sound?
    }

    override fun tick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.tick(connection, blockState, blockPosition, random)
        if (!showParticleNextTick) {
            return
        }
        showParticleNextTick = false


        noteParticleType?.let {
            connection.world += NoteParticle(connection, blockPosition.toVec3d + Vec3d(0.5, 1.2, 0.5), (blockState.properties[BlockProperties.NOTE]?.toInt() ?: 0) / 24.0f, it.default())
        }
    }

    companion object : BlockEntityFactory<NoteBlockBlockEntity>, AliasedIdentified {
        override val identifier: ResourceLocation = ResourceLocation("minecraft:note_block")

        override val identifiers: Set<ResourceLocation> = setOf("minecraft:noteblock".toResourceLocation())

        override fun build(connection: PlayConnection): NoteBlockBlockEntity {
            return NoteBlockBlockEntity(connection)
        }
    }

}
