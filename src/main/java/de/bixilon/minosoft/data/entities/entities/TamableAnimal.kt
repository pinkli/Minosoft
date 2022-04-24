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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.animal.Animal
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class TamableAnimal(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    private fun getTameableFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.TAMABLE_ENTITY_FLAGS, bitMask)
    }

    @get:SynchronizedEntityData(name = "Is sitting")
    val isSitting: Boolean
        get() = getTameableFlag(0x01)

    @get:SynchronizedEntityData(name = "Is tamed")
    val isTamed: Boolean
        get() = getTameableFlag(0x04)

    @get:SynchronizedEntityData(name = "Owner UUID")
    val owner: UUID?
        get() = data.sets.getUUID(EntityDataFields.TAMABLE_ENTITY_OWNER_UUID)
}
