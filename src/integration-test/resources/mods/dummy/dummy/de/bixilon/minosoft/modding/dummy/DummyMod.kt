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

package de.bixilon.minosoft.modding.dummy

import de.bixilon.minosoft.assets.util.InputStreamUtil.readAsString
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.modding.loader.mod.ModMain

object DummyMod : ModMain() {
    var dummy = 1
    var init = 0
    var postInit = 0

    var flag = "<tba>"

    override fun init() {
        init++
    }

    override fun postInit() {
        postInit++
        flag = assets[ResourceLocation("nothin", "flag.txt")].readAsString()
    }
}
