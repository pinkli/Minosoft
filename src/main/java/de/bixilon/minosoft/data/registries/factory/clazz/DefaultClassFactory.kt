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

package de.bixilon.minosoft.data.registries.factory.clazz

import de.bixilon.minosoft.data.registries.RegistryUtil
import java.lang.reflect.ParameterizedType
import kotlin.reflect.jvm.javaType

open class DefaultClassFactory<T : ClassFactory<*>>(vararg factories: T) {
    private val factoryMap: Map<String, T>

    init {
        val ret: MutableMap<String, T> = mutableMapOf()


        for (factory in factories) {
            val className = RegistryUtil.getClassOfFactory(factory::class.supertypes[0].javaType as ParameterizedType).simpleName
            ret[className] = factory
            if (factory is MultiClassFactory<*>) {
                for (name in factory.ALIASES) {
                    ret[name] = factory
                }
            }
        }

        factoryMap = ret.toMap()
    }

    operator fun get(`class`: String?): T? {
        return factoryMap[`class`]
    }
}
