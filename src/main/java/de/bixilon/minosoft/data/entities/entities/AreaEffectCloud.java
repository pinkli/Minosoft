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

package de.bixilon.minosoft.data.entities.entities;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class AreaEffectCloud extends Entity {

    public AreaEffectCloud(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    @EntityMetaDataFunction(identifier = "Radius")
    public float getRadius() {
        return getMetaData().getSets().getFloat(EntityMetaDataFields.AREA_EFFECT_CLOUD_RADIUS);
    }

    @EntityMetaDataFunction(identifier = "Color")
    public int getColor() {
        return getMetaData().getSets().getInt(EntityMetaDataFields.AREA_EFFECT_CLOUD_COLOR);
    }

    // ignore radius???
    @EntityMetaDataFunction(identifier = "Is waiting")
    public boolean isWaiting() {
        return getMetaData().getSets().getBoolean(EntityMetaDataFields.AREA_EFFECT_CLOUD_WAITING);
    }

    @EntityMetaDataFunction(identifier = "Particle")
    public ParticleData getParticle() {
        return getMetaData().getSets().getParticle(EntityMetaDataFields.AREA_EFFECT_CLOUD_PARTICLE);
    }
}

