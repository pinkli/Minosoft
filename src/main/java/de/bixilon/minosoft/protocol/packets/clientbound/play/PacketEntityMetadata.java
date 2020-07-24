/*
 * Codename Minosoft
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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.objectLoader.entities.meta.EntityMetaData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PacketEntityMetadata implements ClientboundPacket {
    HashMap<Integer, EntityMetaData.MetaDataSet> sets;
    int entityId;
    ProtocolVersion version;


    @Override
    public boolean read(InByteBuffer buffer) {
        this.version = buffer.getVersion();
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                entityId = buffer.readInt();
                break;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                entityId = buffer.readVarInt();
                break;
            default:
                return false;
        }
        sets = buffer.readMetaData();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received entity metadata (entityId=%d)", entityId));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public HashMap<Integer, EntityMetaData.MetaDataSet> getSets() {
        return sets;
    }

    public EntityMetaData getEntityData(Class<? extends EntityMetaData> clazz) {
        try {
            return clazz.getConstructor(HashMap.class, ProtocolVersion.class).newInstance(sets, version);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
