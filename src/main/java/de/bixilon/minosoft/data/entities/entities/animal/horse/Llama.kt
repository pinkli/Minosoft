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
package de.bixilon.minosoft.data.entities.entities.animal.horse

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class Llama(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractChestedHorse(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData(name = "Strength")
    val strength: Int
        get() = data.sets.getInt(EntityDataFields.LLAMA_STRENGTH)

    @get:SynchronizedEntityData(name = "CarpetColor")
    val carpetColor: Int
        get() = data.sets.getInt(EntityDataFields.LLAMA_CARPET_COLOR)

    @get:SynchronizedEntityData(name = "Variant")
    val variant: Int
        get() = data.sets.getInt(EntityDataFields.LLAMA_VARIANT)


    companion object : EntityFactory<Llama> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("llama")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Llama {
            return Llama(connection, entityType, data, position, rotation)
        }
    }
}
