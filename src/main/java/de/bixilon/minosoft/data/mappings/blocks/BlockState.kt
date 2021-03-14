/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.blocks

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderInterface
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer
import java.util.*
import kotlin.random.Random

data class BlockState(
    val owner: Block,
    val properties: Set<BlockProperties> = setOf(),
    val rotation: BlockRotations = BlockRotations.NONE,
    val renders: Set<BlockRenderInterface> = setOf(),
    val tintColor: RGBColor? = null,
) {

    override fun hashCode(): Int {
        return Objects.hash(owner, properties, rotation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other is BlockState) {
            return owner.resourceLocation == other.owner.resourceLocation && rotation == other.rotation && properties == other.properties && owner.resourceLocation.namespace == other.owner.resourceLocation.namespace
        }
        if (other is ResourceLocation) {
            return super.equals(other)
        }
        return false
    }

    fun bareEquals(obj: Any): Boolean {
        if (this === obj) {
            return true
        }
        if (obj is BlockState) {
            if (owner.resourceLocation.namespace != obj.owner.resourceLocation.namespace || owner.resourceLocation.path != obj.owner.resourceLocation.path) {
                return false
            }
            if (obj.rotation != BlockRotations.NONE) {
                if (obj.rotation != rotation) {
                    return false
                }
            }
            for (property in obj.properties) {
                if (!properties.contains(property)) {
                    return false
                }
            }
            return true
        }
        return if (obj is ResourceLocation) {
            super.equals(obj)
        } else false
    }

    override fun toString(): String {
        val out = StringBuilder()
        if (rotation != BlockRotations.NONE) {
            out.append(" (")
            out.append("rotation=")
            out.append(rotation)
        }
        if (properties.isNotEmpty()) {
            if (out.isNotEmpty()) {
                out.append(", ")
            } else {
                out.append(" (")
            }
            out.append("properties=")
            out.append(properties)
        }
        if (out.isNotEmpty()) {
            out.append(")")
        }
        return String.format("%s%s", owner.resourceLocation, out)
    }

    fun getBlockRenderer(position: BlockPosition): BlockRenderInterface {
        if (Minosoft.getConfig().config.game.other.antiMoirePattern) {
            // ToDo: Support weight attribute
            return renders.random(Random(position.hashCode()))
        }
        return renders.iterator().next()
    }


    companion object {
        val ROTATION_PROPERTIES = setOf("facing", "rotation", "orientation", "axis")

        val SPECIAL_RENDERERS = mutableMapOf(
            Pair("water", FluidRenderer("block/water_still", "block/water_flow", "water")),
            Pair("lava", FluidRenderer("block/lava_still", "block/lava_flow", "lava")),
        )

        fun deserialize(owner: Block, data: JsonObject, models: Map<ResourceLocation, BlockModel>): BlockState {
            val (rotation, properties) = data["properties"]?.asJsonObject?.let {
                getProperties(it)
            } ?: Pair(BlockRotations.NONE, mutableSetOf())
            val renders: MutableSet<BlockRenderInterface> = mutableSetOf()

            data["render"]?.let {
                when (it) {
                    is JsonArray -> {
                        for (model in it) {
                            check(model is JsonObject)
                            addBlockModel(model, renders, models)
                        }
                    }
                    is JsonObject -> {
                        addBlockModel(it.asJsonObject, renders, models)
                    }
                    else -> error("Not a render json!")
                }
            }

            owner.multipartMapping?.let {
                val elementRenderers: MutableList<JsonObject> = mutableListOf()
                for ((condition, model) in it.entries) {
                    if (condition.contains(properties, rotation)) {
                        elementRenderers.addAll(model)
                    }
                }
                renders.add(BlockRenderer(elementRenderers, models))
            }

            val tintColor: RGBColor? = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) } ?: owner.tintColor

            for ((regex, renderer) in SPECIAL_RENDERERS) {
                if (owner.resourceLocation.full.contains(regex)) {
                    renders.clear()
                    renders.add(renderer)
                }
            }

            return BlockState(
                owner = owner,
                properties = properties.toSet(),
                rotation = rotation,
                renders = renders.toSet(),
                tintColor = tintColor
            )
        }

        private fun getProperties(json: JsonObject) : Pair<BlockRotations, MutableSet<BlockProperties>> {
            var rotation = BlockRotations.NONE
            val properties = mutableSetOf<BlockProperties>()
            for ((propertyName, propertyJsonValue) in json.entrySet()) {
                check(propertyJsonValue is JsonPrimitive) { "Not a json primitive!" }
                val propertyValue: Any = when {
                    propertyJsonValue.isBoolean -> {
                        propertyJsonValue.asBoolean
                    }
                    propertyJsonValue.isNumber -> {
                        propertyJsonValue.asInt
                    }
                    else -> {
                        // ToDo: Why is this needed?
                        try {
                            Integer.parseInt(propertyJsonValue.asString)
                        } catch (exception: Exception) {
                            propertyJsonValue.asString.toLowerCase()
                        }
                    }
                }
                try {
                    if (propertyName in ROTATION_PROPERTIES) {
                        rotation = BlockRotations.ROTATION_MAPPING[propertyValue]!!
                    } else {
                        properties.add(BlockProperties.PROPERTIES_MAPPING[propertyName]!![propertyValue]!!)
                    }
                } catch (exception: NullPointerException) {
                    throw NullPointerException("Invalid block property $propertyName or value $propertyValue")
                }
            }
            return Pair(rotation, properties)
        }

        private fun addBlockModel(data: JsonObject, renders: MutableSet<BlockRenderInterface>, models: Map<ResourceLocation, BlockModel>) {
            val model = models[ResourceLocation(data["model"].asString)] ?: error("Can not find block model ${data["model"]}")
            renders.add(BlockRenderer(data, model))
        }
    }
}
