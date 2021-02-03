/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

import de.bixilon.minosoft.data.Directions

/**
 * Chunk X, Y and Z location (max 16x16x16)
 */
data class InChunkSectionLocation(val x: Int, val y: Int, val z: Int) {

    override fun toString(): String {
        return "($x $y $z)"
    }

    fun getInChunkLocation(sectionHeight: Int): InChunkLocation {
        return InChunkLocation(x, y + ProtocolDefinition.SECTION_HEIGHT_Y * sectionHeight, z)
    }

    fun getLocationByDirection(direction: Directions): InChunkSectionLocation {
        return when (direction) {
            Directions.DOWN -> InChunkSectionLocation(x, y - 1, z)
            Directions.UP -> InChunkSectionLocation(x, y + 1, z)
            Directions.NORTH -> InChunkSectionLocation(x, y, z - 1)
            Directions.SOUTH -> InChunkSectionLocation(x, y, z + 1)
            Directions.WEST -> InChunkSectionLocation(x - 1, y, z)
            Directions.EAST -> InChunkSectionLocation(x + 1, y, z)
        }
    }
}
