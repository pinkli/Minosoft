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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.data.world.InChunkLocation;
import de.bixilon.minosoft.modding.event.events.MultiBlockChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashMap;
import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketMultiBlockChange extends ClientboundPacket {
    private final HashMap<InChunkLocation, Block> blocks = new HashMap<>();
    ChunkLocation location;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W26C) {
            if (buffer.getVersionId() < V_1_7_5) {
                this.location = new ChunkLocation(buffer.readVarInt(), buffer.readVarInt());
            } else {
                this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
            }
            short count = buffer.readShort();
            int dataSize = buffer.readInt(); // should be count * 4
            for (int i = 0; i < count; i++) {
                int raw = buffer.readInt();
                byte meta = (byte) (raw & 0xF);
                short blockId = (short) ((raw & 0xFF_F0) >>> 4);
                byte y = (byte) ((raw & 0xFF_00_00) >>> 16);
                byte z = (byte) ((raw & 0x0F_00_00_00) >>> 24);
                byte x = (byte) ((raw & 0xF0_00_00_00) >>> 28);
                this.blocks.put(new InChunkLocation(x, y, z), buffer.getConnection().getMapping().getBlockById((blockId << 4) | meta));
            }
            return true;
        }
        if (buffer.getVersionId() < V_20W28A) {
            this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                byte pos = buffer.readByte();
                byte y = buffer.readByte();
                int blockId = buffer.readVarInt();
                this.blocks.put(new InChunkLocation((pos & 0xF0 >>> 4) & 0xF, y, pos & 0xF), buffer.getConnection().getMapping().getBlockById(blockId));
            }
            return true;
        }
        long rawPos = buffer.readLong();
        this.location = new ChunkLocation((int) (rawPos >> 42), (int) (rawPos << 22 >> 42));
        int yOffset = ((int) rawPos & 0xFFFFF) * 16;
        if (buffer.getVersionId() > V_1_16_2_PRE3) {
            buffer.readBoolean(); // ToDo
        }
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            long data = buffer.readVarLong();
            this.blocks.put(new InChunkLocation((int) ((data >> 8) & 0xF), yOffset + (int) ((data >> 4) & 0xF), (int) (data & 0xF)), buffer.getConnection().getMapping().getBlockById(((int) (data >>> 12))));
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        Chunk chunk = connection.getPlayer().getWorld().getChunk(getLocation());
        if (chunk == null) {
            // thanks mojang
            return;
        }
        connection.fireEvent(new MultiBlockChangeEvent(connection, this));
        chunk.setBlocks(getBlocks());

        // tweak
        if (!connection.getVersion().isFlattened()) {
            for (Map.Entry<InChunkLocation, Block> entry : getBlocks().entrySet()) {
                Block block = VersionTweaker.transformBlock(entry.getValue(), chunk, entry.getKey());
                if (block == entry.getValue()) {
                    continue;
                }
                chunk.setBlock(entry.getKey(), block);
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Multi block change received at %s (size=%d)", this.location, this.blocks.size()));
    }

    public ChunkLocation getLocation() {
        return this.location;
    }

    public HashMap<InChunkLocation, Block> getBlocks() {
        return this.blocks;
    }
}
