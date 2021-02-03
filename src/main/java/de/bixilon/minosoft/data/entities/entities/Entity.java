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

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.entities.*;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.MobEffect;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.logging.Log;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.UUID;

public abstract class Entity {
    protected final Connection connection;
    protected final EntityInformation information;
    protected final int entityId;
    protected final UUID uuid;
    protected final HashMap<InventorySlots.EntityInventorySlots, Slot> equipment = new HashMap<>();
    protected final HashSet<StatusEffect> effectList = new HashSet<>();
    protected final int versionId;
    protected Location location;
    protected EntityRotation rotation;
    protected int attachedTo = -1;
    protected EntityMetaData metaData;

    public Entity(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        this.connection = connection;
        this.information = connection.getMapping().getEntityInformation(getClass());
        this.entityId = entityId;
        this.uuid = uuid;
        this.versionId = connection.getVersion().getVersionId();
        this.location = location;
        this.rotation = rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocation(RelativeLocation relativeLocation) {
        this.location = new Location(this.location.getX() + relativeLocation.getX(), this.location.getY() + relativeLocation.getY(), this.location.getZ() + relativeLocation.getZ());
    }

    public Slot getEquipment(InventorySlots.EntityInventorySlots slot) {
        return this.equipment.get(slot);
    }

    public HashMap<InventorySlots.EntityInventorySlots, Slot> getEquipment() {
        return this.equipment;
    }

    public void setEquipment(HashMap<InventorySlots.EntityInventorySlots, Slot> slots) {
        this.equipment.putAll(slots);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public HashSet<StatusEffect> getEffectList() {
        return this.effectList;
    }

    public void addEffect(StatusEffect effect) {
        // effect already applied, maybe the duration or the amplifier changed?
        this.effectList.removeIf(listEffect -> listEffect.getEffect() == effect.getEffect());
        this.effectList.add(effect);
    }

    public void removeEffect(MobEffect effect) {
        this.effectList.removeIf(listEffect -> listEffect.getEffect() == effect);
    }

    public void attachTo(int vehicleId) {
        this.attachedTo = vehicleId;
    }

    public boolean isAttached() {
        return this.attachedTo != -1;
    }

    public int getAttachedEntity() {
        return this.attachedTo;
    }

    public void detach() {
        this.attachedTo = -1;
    }

    public EntityRotation getRotation() {
        return this.rotation;
    }

    public void setRotation(EntityRotation rotation) {
        this.rotation = rotation;
    }

    public void setRotation(int yaw, int pitch) {
        this.rotation = new EntityRotation(yaw, pitch, this.rotation.getHeadYaw());
    }

    public void setRotation(int yaw, int pitch, int headYaw) {
        this.rotation = new EntityRotation(yaw, pitch, headYaw);
    }

    public void setHeadRotation(int headYaw) {
        this.rotation = new EntityRotation(this.rotation.getYaw(), this.rotation.getPitch(), headYaw);
    }

    @Unsafe
    public EntityMetaData getMetaData() {
        return this.metaData;
    }

    @Unsafe
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = metaData;
        if (StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
            Log.verbose(String.format("Metadata of entity %s (entityId=%d): %s", toString(), getEntityId(), getEntityMetaDataAsString()));
        }
    }

    public EntityInformation getEntityInformation() {
        return this.information;
    }

    private boolean getEntityFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.ENTITY_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(identifier = "On fire")
    public boolean isOnFire() {
        return getEntityFlag(0x01);
    }

    private boolean isCrouching() {
        return getEntityFlag(0x02);
    }

    @EntityMetaDataFunction(identifier = "Is sprinting")
    public boolean isSprinting() {
        return getEntityFlag(0x08);
    }

    private boolean isSwimming() {
        return getEntityFlag(0x10);
    }

    @EntityMetaDataFunction(identifier = "Is invisible")
    public boolean isInvisible() {
        return getEntityFlag(0x20);
    }

    @EntityMetaDataFunction(identifier = "Has glowing effect")
    public boolean hasGlowingEffect() {
        return getEntityFlag(0x20);
    }

    private boolean isFlyingWithElytra() {
        return getEntityFlag(0x80);
    }

    @EntityMetaDataFunction(identifier = "Air supply")
    private int getAirSupply() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.ENTITY_AIR_SUPPLY);
    }

    @EntityMetaDataFunction(identifier = "Custom name")
    @Nullable
    private ChatComponent getCustomName() {
        return this.metaData.getSets().getChatComponent(EntityMetaDataFields.ENTITY_CUSTOM_NAME);
    }

    @EntityMetaDataFunction(identifier = "Is custom name visible")
    public boolean isCustomNameVisible() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    @EntityMetaDataFunction(identifier = "Is silent")
    public boolean isSilent() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_SILENT);
    }

    @EntityMetaDataFunction(identifier = "Has no gravity")
    public boolean hasNoGravity() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_NO_GRAVITY);
    }

    @EntityMetaDataFunction(identifier = "Pose")
    public Poses getPose() {
        if (isCrouching()) {
            return Poses.SNEAKING;
        }
        if (isSwimming()) {
            return Poses.SWIMMING;
        }
        if (isFlyingWithElytra()) {
            return Poses.FLYING;
        }
        return this.metaData.getSets().getPose(EntityMetaDataFields.ENTITY_POSE);
    }

    @EntityMetaDataFunction(identifier = "Ticks frozen")
    public int getTicksFrozen() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.ENTITY_TICKS_FROZEN);
    }

    @Override
    public String toString() {
        if (this.information == null) {
            return this.getClass().getCanonicalName();
        }
        return String.format("%s", this.information);
    }

    public String getEntityMetaDataAsString() {
        return getEntityMetaDataFormatted().toString();
    }

    public TreeMap<String, Object> getEntityMetaDataFormatted() {
        // scan all methods of current class for EntityMetaDataFunction annotation and write it into a list
        TreeMap<String, Object> values = new TreeMap<>();
        if (this.metaData == null) {
            return values;
        }
        Class<?> clazz = this.getClass();
        while (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EntityMetaDataFunction.class)) {
                    continue;
                }
                if (method.getParameterCount() > 0) {
                    continue;
                }
                method.setAccessible(true);
                try {
                    String identifier = method.getAnnotation(EntityMetaDataFunction.class).identifier();
                    if (values.containsKey(identifier)) {
                        continue;
                    }
                    Object methodRetValue = method.invoke(this);
                    if (methodRetValue == null) {
                        continue;
                    }
                    values.put(identifier, methodRetValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass();
        }
        return values;
    }
}
