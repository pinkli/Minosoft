/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.raw.display

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

data class ModelDisplay(
    val rotation: Vec3?,
    val translation: Vec3?,
    val scale: Vec3?,
) {
    companion object {

        fun deserialize(data: JsonObject): ModelDisplay {
            return ModelDisplay(
                rotation = data["rotation"]?.toVec3()?.rad,
                translation = data["translation"]?.toVec3(),
                scale = data["scale"]?.toVec3(),
            )
        }
    }
}
