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

import de.bixilon.minosoft.data.commands.parser.properties.FloatParserProperties;
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class FloatParser extends CommandParser {
    public static final FloatParser FLOAT_PARSER = new FloatParser();

    public boolean isValidValue(FloatParserProperties properties, float value) {
        return value >= properties.getMinValue() && value <= properties.getMaxValue();
    }

    @Override
    public ParserProperties readParserProperties(InByteBuffer buffer) {
        return new FloatParserProperties(buffer);
    }

    @Override
    public boolean isParsable(ParserProperties properties, ImprovedStringReader stringReader) {
        String argument = stringReader.readUntilNextCommandArgument();
        try {
            return isValidValue((FloatParserProperties) properties, Float.parseFloat(argument));
        } catch (Exception ignored) {
            return false;
        }
    }
}
