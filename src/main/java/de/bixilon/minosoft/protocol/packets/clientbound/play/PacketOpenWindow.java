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

import de.bixilon.minosoft.data.inventory.InventoryProperties;
import de.bixilon.minosoft.data.inventory.InventoryTypes;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketOpenWindow extends ClientboundPacket {
    byte windowId;
    InventoryTypes type;
    ChatComponent title;
    byte slotCount;
    int entityId;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W03B) {
            this.windowId = buffer.readByte();
            this.type = InventoryTypes.byId(buffer.readUnsignedByte());
            this.title = buffer.readChatComponent();
            this.slotCount = buffer.readByte();
            if (!buffer.readBoolean()) {
                // no custom name
                this.title = null;
            }
            this.entityId = buffer.readInt();
            return true;
        }
        this.windowId = buffer.readByte();
        this.type = InventoryTypes.byIdentifier(buffer.readIdentifier());
        this.title = buffer.readChatComponent();
        if (buffer.getVersionId() < V_19W02A || buffer.getVersionId() >= V_19W11A) {
            this.slotCount = buffer.readByte();
        }
        if (this.type == InventoryTypes.HORSE) {
            this.entityId = buffer.readInt();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.getPlayer().createInventory(getInventoryProperties());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received inventory open packet (windowId=%d, type=%s, title=%s, entityId=%d, slotCount=%d)", this.windowId, this.type, this.title, this.entityId, this.slotCount));
    }

    public byte getSlotCount() {
        return this.slotCount;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public ChatComponent getTitle() {
        return this.title;
    }

    public InventoryProperties getInventoryProperties() {
        return new InventoryProperties(getWindowId(), getType(), this.title, this.slotCount);
    }

    public byte getWindowId() {
        return this.windowId;
    }

    public InventoryTypes getType() {
        return this.type;
    }
}
