/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.AmbientEntityEffectParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.spell.EntityEffectParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class LivingEntity(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {
    private val entityEffectParticle = connection.registries.particleTypeRegistry[EntityEffectParticle]
    private val ambientEntityEffectParticle = connection.registries.particleTypeRegistry[AmbientEntityEffectParticle]

    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.LIVING_ENTITY_FLAGS, bitMask)
    }

    @get:SynchronizedEntityData(name = "Is hand active")
    val isHandActive: Boolean
        get() = getLivingEntityFlag(0x01)

    @get:SynchronizedEntityData(name = "Main hand")
    open val activeHand: Hands?
        get() = if (getLivingEntityFlag(0x02)) Hands.OFF else Hands.MAIN

    @get:SynchronizedEntityData(name = "Is auto spin attack") // aka using riptide
    val isSpinAttacking: Boolean
        get() = getLivingEntityFlag(0x04)

    @get:SynchronizedEntityData(name = "Health")
    open val health: Double
        get() {
            val meta = data.sets.getFloat(EntityDataFields.LIVING_ENTITY_HEALTH)
            return if (meta == Float.MIN_VALUE) {
                type.attributes[DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH] ?: 1.0
            } else {
                meta.toDouble()
            }
        }

    @get:SynchronizedEntityData(name = "Effect color")
    val effectColor: RGBColor
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_EFFECT_COLOR).asRGBColor()

    @get:SynchronizedEntityData(name = "Is effect ambient")
    val effectAmbient: Boolean
        get() = data.sets.getBoolean(EntityDataFields.LIVING_ENTITY_EFFECT_AMBIENCE)

    @get:SynchronizedEntityData(name = "Arrows in entity")
    val arrowCount: Int
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_ARROW_COUNT)

    @get:SynchronizedEntityData(name = "Absorption hearts")
    val absorptionHearts: Int
        get() = data.sets.getInt(EntityDataFields.LIVING_ENTITY_ABSORPTION_HEARTS)

    @get:SynchronizedEntityData(name = "Bed location")
    val bedPosition: Vec3i?
        get() = data.sets.getBlockPosition(EntityDataFields.LIVING_ENTITY_BED_POSITION)

    open val isSleeping: Boolean
        get() = bedPosition != null

    override val pose: Poses?
        get() = when {
            isSleeping -> Poses.SLEEPING
            isSpinAttacking -> Poses.SPIN_ATTACK
            else -> super.pose
        }

    override val spawnSprintingParticles: Boolean
        get() = super.spawnSprintingParticles && health > 0.0

    private fun tickStatusEffects() {
        if (entityEffectParticle == null && ambientEntityEffectParticle == null) {
            return
        }
        if (effectColor == ChatColors.BLACK) {
            return
        }
        var spawnParticles = if (isInvisible) {
            random.nextInt(15) == 0
        } else {
            random.nextBoolean()
        }

        if (effectAmbient) {
            spawnParticles = spawnParticles && random.chance(20)
        }

        if (!spawnParticles) {
            return
        }

        val particlePosition = position + Vec3d.horizontal(
            { dimensions.x * ((2.0 * random.nextDouble() - 1.0) * 0.5) },
            dimensions.y * random.nextDouble()
        )
        if (effectAmbient) {
            ambientEntityEffectParticle ?: return
            connection.world += AmbientEntityEffectParticle(connection, particlePosition, effectColor, ambientEntityEffectParticle.default())
        } else {
            entityEffectParticle ?: return
            connection.world += EntityEffectParticle(connection, particlePosition, effectColor, entityEffectParticle.default())
        }
    }

    override fun realTick() {
        super.realTick()
        tickStatusEffects()

        if (isSleeping) {
            rotation = rotation.copy(pitch = 0.0)
        }
    }
}
