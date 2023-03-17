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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.inventory

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement

class LocalInventoryScreen(guiRenderer: GUIRenderer) : InventoryScreen(guiRenderer, guiRenderer.connection.player.items.inventory) {

    companion object : GUIBuilder<LayoutedGUIElement<LocalInventoryScreen>> {

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<LocalInventoryScreen> {
            return LayoutedGUIElement(LocalInventoryScreen(guiRenderer))
        }
    }
}
