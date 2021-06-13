/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

import de.bixilon.minosoft.data.registries.blocks.properties.serializer.BlockPropertiesSerializer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

enum class Axes {
    X,
    Y,
    Z,
    ;

    fun choose(vec3: Vec3): Float {
        return choose(vec3.x, vec3.y, vec3.z)
    }

    fun choose(vec3: Vec3d): Double {
        return choose(vec3.x, vec3.y, vec3.z)
    }

    fun choose(vec3i: Vec3i): Int {
        return choose(Vec3(vec3i)).toInt()
    }

    private fun choose(x: Float, y: Float, z: Float): Float {
        return when (this) {
            X -> x
            Y -> y
            Z -> z
        }
    }

    private fun choose(x: Double, y: Double, z: Double): Double {
        return when (this) {
            X -> x
            Y -> y
            Z -> z
        }
    }

    companion object : ValuesEnum<Axes>, BlockPropertiesSerializer {
        override val VALUES: Array<Axes> = values()
        override val NAME_MAP: Map<String, Axes> = KUtil.getEnumValues(VALUES)

        fun byDirection(direction: Directions): Axes {
            return when (direction) {
                Directions.EAST, Directions.WEST -> X
                Directions.UP, Directions.DOWN -> Y
                Directions.NORTH, Directions.SOUTH -> Z
            }
        }

        override fun deserialize(value: Any): Axes {
            return NAME_MAP[value] ?: throw IllegalArgumentException("No such property: $value")
        }
    }
}
