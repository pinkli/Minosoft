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

package de.bixilon.minosoft.data.entities.entities.animal;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.TamableAnimal;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

public class Cat extends TamableAnimal {

    public Cat(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
    }

    @EntityMetaDataFunction(name = "Variant")
    public CatVariants getVariant() {
        return CatVariants.byId(getEntityMetaData().getSets().getInt(EntityMetaDataFields.CAT_VARIANT));
    }

    @EntityMetaDataFunction(name = "Lying")
    public boolean isLying() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.CAT_IS_LYING);
    }

    @EntityMetaDataFunction(name = "Relaxed")
    public boolean isRelaxed() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.CAT_IS_RELAXED);
    }

    @EntityMetaDataFunction(name = "Collar color")
    public RGBColor getCollarColor() {
        return ChatColors.getColorById(getEntityMetaData().getSets().getInt(EntityMetaDataFields.CAT_GET_COLLAR_COLOR));
    }

    public enum CatVariants {
        TABBY,
        BLACK,
        RED,
        SIAMESE,
        BRITISH_SHORT_HAIR,
        CALICO,
        PERSIAN,
        RAGDOLL,
        ALL_BLACK;

        private static final CatVariants[] CAT_VARIANTS = values();

        public static CatVariants byId(int id) {
            return CAT_VARIANTS[id];
        }
    }
}
