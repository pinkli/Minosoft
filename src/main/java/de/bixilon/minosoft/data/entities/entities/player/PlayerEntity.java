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

package de.bixilon.minosoft.data.entities.entities.player;

import de.bixilon.minosoft.data.Gamemodes;
import de.bixilon.minosoft.data.PlayerPropertyData;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.LivingEntity;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import glm_.vec3.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.UUID;

public class PlayerEntity extends LivingEntity {
    private String name;
    private UUID uuid;
    private final HashSet<PlayerPropertyData> properties;
    private Gamemodes gamemode;

    public PlayerEntity(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
        this.name = "Ghost Player";
        this.properties = null;
    }

    public PlayerEntity(Connection connection, Vec3 position, EntityRotation rotation, String name, UUID uuid, @Nullable HashSet<PlayerPropertyData> properties, Gamemodes gamemode) {
        super(connection, position, rotation);
        this.name = name;
        this.uuid = uuid;
        this.properties = properties;
        this.gamemode = gamemode;
        this.hasCollisions = gamemode != Gamemodes.SPECTATOR;
    }

    @EntityMetaDataFunction(name = "Absorption hearts")
    public float getPlayerAbsorptionHearts() {
        return getEntityMetaData().getSets().getFloat(EntityMetaDataFields.PLAYER_ABSORPTION_HEARTS);
    }

    @EntityMetaDataFunction(name = "Score")
    public int getScore() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.PLAYER_SCORE);
    }

    private boolean getSkinPartsFlag(int bitMask) {
        return getEntityMetaData().getSets().getBitMask(EntityMetaDataFields.PLAYER_SKIN_PARTS_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "Main hand")
    public Hands getMainHand() {
        return getEntityMetaData().getSets().getByte(EntityMetaDataFields.PLAYER_SKIN_MAIN_HAND) == 0x01 ? Hands.OFF_HAND : Hands.MAIN_HAND;
    }

    @EntityMetaDataFunction(name = "Left shoulder entity data")
    @Nullable
    public CompoundTag getLeftShoulderData() {
        return getEntityMetaData().getSets().getNBT(EntityMetaDataFields.PLAYER_LEFT_SHOULDER_DATA);
    }

    @EntityMetaDataFunction(name = "Right shoulder entity data")
    @Nullable
    public CompoundTag getRightShoulderData() {
        return getEntityMetaData().getSets().getNBT(EntityMetaDataFields.PLAYER_RIGHT_SHOULDER_DATA);
    }

    @EntityMetaDataFunction(name = "Name")
    public String getName() {
        return this.name;
    }

    @EntityMetaDataFunction(name = "Properties")
    @Nullable
    public HashSet<PlayerPropertyData> getProperties() {
        return this.properties;
    }

    public Gamemodes getGamemode() {
        return this.gamemode;
    }

    public void setGamemode(Gamemodes gamemode) {
        this.gamemode = gamemode;
        this.hasCollisions = gamemode == Gamemodes.SPECTATOR;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}

