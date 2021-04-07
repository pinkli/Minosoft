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

package de.bixilon.minosoft.config.config.game

import com.squareup.moshi.Json

data class CameraGameConfig(
    @Json(name = "render_distance") var renderDistance: Int = 10,
    var fov: Float = 60f,
    @Json(name = "mouse_sensitivity") var moseSensitivity: Float = 0.1f,
    @Json(name = "no_clip_movement") var noCipMovement: Boolean = false,
)
