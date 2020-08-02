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

import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;


public class PacketTitle implements ClientboundPacket {
    TitleAction action;

    //fields depend on action
    TextComponent text;
    TextComponent subText;
    int fadeInTime;
    int stayTime;
    int fadeOutTime;


    @Override
    public boolean read(InByteBuffer buffer) {
        action = TitleAction.byId(buffer.readVarInt(), buffer.getProtocolId());
        switch (action) {
            case SET_TITLE:
                text = buffer.readTextComponent();
                break;
            case SET_SUBTITLE:
                subText = buffer.readTextComponent();
                break;
            case SET_TIMES_AND_DISPLAY:
                fadeInTime = buffer.readInt();
                stayTime = buffer.readInt();
                fadeOutTime = buffer.readInt();
                break;
        }
        return true;
    }

    @Override
    public void log() {
        switch (action) {
            case SET_TITLE:
                Log.protocol(String.format("Received title (action=%s, text=%s)", action, text.getColoredMessage()));
                break;
            case SET_SUBTITLE:
                Log.protocol(String.format("Received title (action=%s, subText=%s)", action, subText.getColoredMessage()));
                break;
            case SET_TIMES_AND_DISPLAY:
                Log.protocol(String.format("Received title (action=%s, fadeInTime=%d, stayTime=%d, fadeOutTime=%d)", action, fadeInTime, stayTime, fadeOutTime));
                break;
            case HIDE:
            case RESET:
                Log.protocol(String.format("Received title (action=%s)", action));
                break;
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getFadeInTime() {
        return fadeInTime;
    }

    public int getFadeOutTime() {
        return fadeOutTime;
    }

    public int getStayTime() {
        return stayTime;
    }

    public TextComponent getSubText() {
        return subText;
    }

    public TextComponent getText() {
        return text;
    }

    public TitleAction getAction() {
        return action;
    }

    public enum TitleAction {

        SET_TITLE(0),
        SET_SUBTITLE(1),
        SET_ACTION_BAR(new MapSet[]{new MapSet<>(302, 2)}),
        SET_TIMES_AND_DISPLAY(new MapSet[]{new MapSet<>(18, 2), new MapSet<>(302, 3)}),
        HIDE(new MapSet[]{new MapSet<>(18, 3), new MapSet<>(302, 4)}),
        RESET(new MapSet[]{new MapSet<>(18, 4), new MapSet<>(302, 5)});

        final VersionValueMap<Integer> valueMap;

        TitleAction(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        TitleAction(int id) {
            valueMap = new VersionValueMap<>(id);
        }

        public static TitleAction byId(int id, int protocolId) {
            for (TitleAction action : values()) {
                if (action.getId(protocolId) == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId(int protocolId) {
            Integer ret = valueMap.get(protocolId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
