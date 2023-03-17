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

package de.bixilon.minosoft.data.registries.fluid

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.fluid.fluids.EmptyFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.integrated.IntegratedRegistry
import de.bixilon.minosoft.data.registries.registries.Registries

object FluidFactories : DefaultFactory<FluidFactory<*>>(
    EmptyFluid,
    WaterFluid,
    LavaFluid,
), IntegratedRegistry<Fluid> {

    override fun build(name: ResourceLocation, registries: Registries, data: JsonObject): Fluid? {
        return this[name]?.build(name, registries)
    }
}
