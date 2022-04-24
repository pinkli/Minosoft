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
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Pig(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @SynchronizedEntityData(name = "Has saddle")
    fun hasSaddle(): Boolean {
        return data.sets.getBoolean(EntityDataFields.PIG_HAS_SADDLE)
    }

    @get:SynchronizedEntityData(name = "Boost time")
    val boostTime: Int
        get() = data.sets.getInt(EntityDataFields.PIG_BOOST_TIME)


    companion object : EntityFactory<Pig> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("pig")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Pig {
            return Pig(connection, entityType, data, position, rotation)
        }
    }
}
