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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.TamableAnimal
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class Wolf(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : TamableAnimal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData(name = "Is beging")
    val isBegging: Boolean
        get() = data.sets.getBoolean(EntityDataFields.WOLF_IS_BEGGING)

    @get:SynchronizedEntityData(name = "Collar color")
    val collarColor: RGBColor
        get() = ChatColors[data.sets.getInt(EntityDataFields.WOLF_COLLAR_COLOR)]

    // ToDo
    @get:SynchronizedEntityData(name = "Anger time")
    val angerTime: Int
        get() = if (versionId <= ProtocolVersions.V_1_8_9) { // ToDo
            if (data.sets.getBitMask(EntityDataFields.TAMABLE_ENTITY_FLAGS, 0x02)) 1 else 0
        } else {
            data.sets.getInt(EntityDataFields.WOLF_ANGER_TIME)
        }

    @SynchronizedEntityData(name = "Health")
    override val health: Double
        get() = if (versionId > ProtocolVersions.V_19W45B) {
            super.health
        } else {
            data.sets.getFloat(EntityDataFields.WOLF_HEALTH).toDouble()
        }

    companion object : EntityFactory<Wolf> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("wolf")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Wolf {
            return Wolf(connection, entityType, data, position, rotation)
        }
    }
}
