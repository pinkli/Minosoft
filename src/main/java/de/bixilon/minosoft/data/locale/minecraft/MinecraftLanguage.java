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

package de.bixilon.minosoft.data.locale.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MinecraftLanguage {
    private final String language;
    private final HashMap<String, String> data = new HashMap<>();

    protected MinecraftLanguage(String language, JsonObject json) {
        this.language = language;
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            this.data.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsString());
        }
    }

    protected MinecraftLanguage(String language, String string) {
        this.language = language;

        for (String line : string.split("\\r?\\n")) {
            if (line.isBlank()) {
                continue;
            }
            String[] splitLine = line.split("=", 2);
            this.data.put(splitLine[0].toLowerCase(Locale.ROOT), splitLine[1]);
        }
    }

    public String getLanguage() {
        return this.language;
    }

    public boolean canTranslate(String key) {
        return this.data.containsKey(key);
    }

    public String translate(String key, Object... data) {
        String placeholder = this.data.get(key);
        if (placeholder == null) {
            return null;
        }
        return String.format(placeholder, data);
    }

    @Override
    public String toString() {
        return this.language;
    }
}
