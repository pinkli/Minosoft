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
package de.bixilon.minosoft.data.entities.entities.vehicle

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class FurnaceMinecart(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractMinecartContainer(connection, entityType, position, rotation) {

    @SynchronizedEntityData(name = "Has fuel")
    fun hasFuel(): Boolean {
        return data.sets.getBoolean(EntityDataFields.MINECART_FURNACE_HAS_FUEL)
    }

    companion object : EntityFactory<FurnaceMinecart> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("furnace_minecart")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): FurnaceMinecart {
            return FurnaceMinecart(connection, entityType, position, rotation)
        }
    }
}
