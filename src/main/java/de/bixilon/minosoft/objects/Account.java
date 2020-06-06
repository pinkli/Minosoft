/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.objects;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.HTTP;
import de.bixilon.minosoft.util.Util;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.util.UUID;

public class Account {

    final String username;
    final String password;
    String playerName;

    String token;
    UUID uuid;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void login() {
        JSONObject payload = new JSONObject();
        payload.put("agent", new JSONObject().put("name", "Minecraft").put("version", 1));
        payload.put("username", username);
        payload.put("password", password);
        // ToDo not in main thread
        HttpResponse<String> response = HTTP.postJson("https://authserver.mojang.com/authenticate", payload);
        if (response == null || response.statusCode() != 200) {
            assert response != null;
            Log.info(String.format("[Mojang API] Login failed with username=%s (%s)", username, response.statusCode()));
            return;
        }


        Log.info(String.format("[Mojang API] Login successful with username=%s", username));

        // login good
        JSONObject raw = new JSONObject(response.body());

        token = raw.getString("accessToken");

        uuid = Util.formatUUID(raw.getJSONObject("selectedProfile").getString("id"));
        playerName = raw.getJSONObject("selectedProfile").getString("name");
    }

    public void join(String serverId) {
        JSONObject payload = new JSONObject();
        payload.put("accessToken", token);
        payload.put("selectedProfile", getUUID().toString().replace("-", ""));
        payload.put("serverId", serverId);
        // ToDo not in main thread
        HttpResponse<String> response = HTTP.postJson("https://sessionserver.mojang.com/session/minecraft/join", payload);
        if (response == null || response.statusCode() != 204) {
            assert response != null;
            Log.info("[Mojang API] Login to join server!");
            return;
        }

        Log.info("[Mojang API] Joined server successfully");
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
