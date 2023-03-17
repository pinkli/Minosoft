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

package de.bixilon.minosoft.data.registries.item.items.tool.axe

import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.item.items.tool.ToolTest
import de.bixilon.minosoft.data.registries.item.items.tool.materials.DiamondTool
import de.bixilon.minosoft.data.registries.item.items.tool.materials.IronTool
import de.bixilon.minosoft.data.registries.item.items.tool.materials.WoodenTool
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["item_digging"])
class AxeItemTest : ToolTest() {

    fun sapling() {
        val (suitable, speed) = mine(WoodenTool.WoodenAxe, MinecraftBlocks.OAK_SAPLING)
        assertTrue(suitable)
        assertEquals(speed, 2.0f)
    }

    fun wool() {
        val (suitable, speed) = mine(WoodenTool.WoodenAxe, MinecraftBlocks.RED_WOOL)
        assertTrue(suitable)
        assertEquals(speed, 1.0f)
    }

    fun planks() {
        val (suitable, speed) = mine(WoodenTool.WoodenAxe, MinecraftBlocks.OAK_PLANKS)
        assertTrue(suitable)
        assertEquals(speed, 2.0f)
    }

    fun obsidianWood() {
        val (suitable, speed) = mine(WoodenTool.WoodenAxe, MinecraftBlocks.OBSIDIAN)
        assertFalse(suitable)
        assertEquals(speed, 1.0f)
    }

    fun obsidianIron() {
        val (suitable, speed) = mine(IronTool.IronAxe, MinecraftBlocks.OBSIDIAN)
        assertFalse(suitable)
        assertEquals(speed, 1.0f)
    }

    fun obsidianDiamond() {
        val (suitable, speed) = mine(DiamondTool.DiamondAxe, MinecraftBlocks.OBSIDIAN)
        assertFalse(suitable)
        assertEquals(speed, 1.0f)
    }
}
