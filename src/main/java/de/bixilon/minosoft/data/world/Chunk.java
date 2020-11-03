/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world;

import de.bixilon.minosoft.data.mappings.blocks.Block;

import java.util.HashMap;

/**
 * Collection of 16 chunks sections
 */
public class Chunk {
    final HashMap<Byte, ChunkSection> sections;

    public Chunk(HashMap<Byte, ChunkSection> sections) {
        this.sections = sections;
    }

    public Block getBlock(InChunkLocation location) {
        return getBlock(location.getX(), location.getY(), location.getZ());
    }

    public Block getBlock(int x, int y, int z) {
        if (x > 15 || y > 255 || z > 15 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte section = (byte) (y / 16);
        return sections.get(section).getBlock(x, y % 16, z);
    }

    public void setBlock(int x, int y, int z, Block block) {
        byte section = (byte) (y / 16);
        createSection(section);
        sections.get(section).setBlock(x, y % 16, z, block);
    }

    void createSection(byte section) {
        if (sections.get(section) == null) {
            // section was empty before, creating it
            sections.put(section, new ChunkSection());
        }
    }

    public void setBlocks(HashMap<InChunkLocation, Block> blocks) {
        blocks.forEach(this::setBlock);
    }

    public void setBlock(InChunkLocation location, Block block) {
        byte section = (byte) (location.getY() / 16);
        createSection(section);
        sections.get(section).setBlock(location.getInChunkSectionLocation(), block);
    }
}
