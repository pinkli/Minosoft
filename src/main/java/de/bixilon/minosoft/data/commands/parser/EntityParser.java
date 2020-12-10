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

package de.bixilon.minosoft.data.commands.parser;

import de.bixilon.minosoft.data.commands.parser.entity.EntitySelectorArgumentParser;
import de.bixilon.minosoft.data.commands.parser.entity.NameEntitySelectorArgumentParser;
import de.bixilon.minosoft.data.commands.parser.properties.EntityParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

import java.util.HashMap;
import java.util.HashSet;

public class EntityParser extends CommandParser {
    public static final EntityParser ENTITY_PARSER = new EntityParser();
    private static final HashMap<String, EntitySelectorArgumentParser> ENTITY_FILTER_PARAMETER_LIST = new HashMap<>();

    static {
        // ToDo: add all parsers
        /*
        ENTITY_FILTER_PARAMETER_LIST.put("advancements", null);
        ENTITY_FILTER_PARAMETER_LIST.put("distance", null);
        ENTITY_FILTER_PARAMETER_LIST.put("dx", null);
        ENTITY_FILTER_PARAMETER_LIST.put("dy", null);
        ENTITY_FILTER_PARAMETER_LIST.put("dz", null);
        ENTITY_FILTER_PARAMETER_LIST.put("gamemode", null);
        ENTITY_FILTER_PARAMETER_LIST.put("level", null);
        ENTITY_FILTER_PARAMETER_LIST.put("limit", null);

         */
        ENTITY_FILTER_PARAMETER_LIST.put("name", NameEntitySelectorArgumentParser.NAME_ENTITY_SELECTOR_ARGUMENT_PARSER);
        /*
        ENTITY_FILTER_PARAMETER_LIST.put("nbt", null);
        ENTITY_FILTER_PARAMETER_LIST.put("predicate", null);
        ENTITY_FILTER_PARAMETER_LIST.put("scores", null);
        ENTITY_FILTER_PARAMETER_LIST.put("sort", null);
        ENTITY_FILTER_PARAMETER_LIST.put("tag", null);
        ENTITY_FILTER_PARAMETER_LIST.put("team", null);
        ENTITY_FILTER_PARAMETER_LIST.put("x", null);
        ENTITY_FILTER_PARAMETER_LIST.put("x_rotation", null);
        ENTITY_FILTER_PARAMETER_LIST.put("y", null);
        ENTITY_FILTER_PARAMETER_LIST.put("y_rotation", null);
        ENTITY_FILTER_PARAMETER_LIST.put("z", null);

         */
    }

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new EntityParserProperties(buffer);
    }

    @Override
    public boolean isParsable(ParserProperties properties, ImprovedStringReader stringReader) {
        if (stringReader.getChar().equals("@")) {
            // selector
            if (((EntityParserProperties) properties).isOnlySingleEntity()) {
                return false;
            }
            stringReader.skip(1); // skip @
            String selectorChar = stringReader.readChar();
            if (!selectorChar.equals("a") && !selectorChar.equals("e") && !selectorChar.equals("p") && !selectorChar.equals("r") && !selectorChar.equals("s")) {
                // only @a, @e, @p, @r and @s possible
                return false;
            }

            // parse entity selector

            // example: /msg @a[ name = "Bixilon" ] asd
            if (!stringReader.getChar().equals("[")) {
                // no meta data given, valid
                return true;
            }
            stringReader.skip(1);

            // meta data, parse it!
            HashSet<String> parameters = new HashSet<>();
            while (true) {
                stringReader.skipSpaces();
                String parameterName = stringReader.readUntil("=").key.replaceAll("\\s", "");
                if (parameters.contains(parameterName)) {
                    return false;
                }
                if (!ENTITY_FILTER_PARAMETER_LIST.containsKey(parameterName)) {
                    return false;
                }

                stringReader.skipSpaces();

                EntitySelectorArgumentParser parser = ENTITY_FILTER_PARAMETER_LIST.get(parameterName);
                if (!parser.isParsable(stringReader)) {
                    return false;
                }
                stringReader.skipSpaces();
                parameters.add(parameterName);
                String nextChar = stringReader.getChar();
                if (nextChar.equals("]")) {
                    stringReader.skip(1);
                    break;
                }
                if (nextChar.equals(",")) {
                    stringReader.skip(1);
                }
            }
            stringReader.skipSpaces();
            return true;
        }
        String value = stringReader.readUntilNextCommandArgument();
        if (ProtocolDefinition.MINECRAFT_NAME_VALIDATOR.matcher(value).matches()) {
            return true;
        }
        if (((EntityParserProperties) properties).isOnlyPlayers()) {
            return false;
        }
        try {
            Util.getUUIDFromString(value);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
