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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other

import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.GenericTarget
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.CustomHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

class CrosshairHUDElement(guiRenderer: GUIRenderer) : CustomHUDElement(guiRenderer) {
    private val profile = guiRenderer.connection.profiles.gui
    private val crosshairProfile = profile.hud.crosshair
    private var crosshairAtlasElement: AtlasElement? = null
    private var mesh: GUIMesh? = null
    private var previousDebugEnabled: Boolean? = true
    private var reapply = true
    private var target: GenericTarget? = null

    override fun init() {
        crosshairAtlasElement = guiRenderer.atlasManager[ATLAS_NAME]
        crosshairProfile::color.profileWatch(this, profile = profile) { reapply = true }
    }

    override fun draw() {
        val debugHUDElement: LayoutedGUIElement<DebugHUDElement>? = guiRenderer.hud[DebugHUDElement]

        if (debugHUDElement?.enabled != previousDebugEnabled || reapply) {
            apply()
            previousDebugEnabled = debugHUDElement?.enabled
        }
        val target = renderWindow.camera.targetHandler.target
        if (target != this.target) {
            this.target = target
            apply()
        }

        val mesh = mesh ?: return

        if (crosshairProfile.complementaryColor) {
            renderWindow.renderSystem.reset(blending = true, sourceRGB = BlendingFunctions.ONE_MINUS_DESTINATION_COLOR, destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_COLOR)
        } else {
            renderWindow.renderSystem.reset()
        }

        guiRenderer.shader.use()
        mesh.draw()
    }

    override fun apply() {
        mesh?.unload()
        this.mesh = null
        val crosshairAtlasElement = crosshairAtlasElement ?: return


        val mesh = GUIMesh(renderWindow, guiRenderer.matrix, DirectArrayFloatList(42))

        // Custom draw to make the crosshair inverted
        if (renderWindow.connection.player.gamemode == Gamemodes.SPECTATOR) {
            val target = target
            if (target !is EntityTarget && (target !is BlockTarget || target.blockState.block !is BlockWithEntity<*>)) {
                return
            }
        }

        val debugHUDElement: LayoutedGUIElement<DebugHUDElement>? = guiRenderer.hud[DebugHUDElement]

        if (debugHUDElement?.enabled == true) {
            // ToDo: Debug crosshair
            // return
        }

        val start = (guiRenderer.scaledSize - CROSSHAIR_SIZE) / 2

        mesh.addQuad(start, start + CROSSHAIR_SIZE, crosshairAtlasElement, crosshairProfile.color, null)


        // ToDo: Attack indicator

        mesh.load()
        this.mesh = mesh
        this.reapply = false
    }


    companion object : HUDBuilder<CrosshairHUDElement> {
        const val CROSSHAIR_SIZE = 16
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:crosshair".toResourceLocation()

        private val ATLAS_NAME = "minecraft:crosshair".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): CrosshairHUDElement {
            return CrosshairHUDElement(guiRenderer)
        }
    }
}
