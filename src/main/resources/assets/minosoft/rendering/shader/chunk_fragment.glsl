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

#version 330 core

out vec4 outColor;

flat in uint passTextureIdIndex;
in vec3 passTextureCoordinates;
in vec4 passTintColor;

uniform sampler2DArray textureArray[7];

void main() {
    vec4 texelColor = texture(textureArray[passTextureIdIndex], passTextureCoordinates);
    if (texelColor.a == 0.0f) { // ToDo: This only works for alpha == 0. What about semi transparency? We would need to sort the faces, etc. See: https://learnopengl.com/Advanced-OpenGL/Blending
        discard;
    }
    //vec3 mixedColor = mix(texelColor.rgb, passTintColor.rgb, passTintColor.a);
    vec3 mixedColor = texelColor.rgb * passTintColor.rgb;
    outColor = vec4(mixedColor, texelColor.a);
}
