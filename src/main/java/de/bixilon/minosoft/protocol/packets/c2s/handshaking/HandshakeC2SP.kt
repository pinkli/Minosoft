/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.c2s.handshaking

import de.bixilon.minosoft.protocol.packets.c2s.AllC2SPacket
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class HandshakeC2SP(
    val address: ServerAddress,
    val nextState: ProtocolStates = ProtocolStates.STATUS,
    val protocolId: Int = ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID,
) : AllC2SPacket {

    override fun write(buffer: OutByteBuffer) {
        buffer.writeVarInt(protocolId)
        buffer.writeString(address.hostname)
        buffer.writeShort(address.port)
        buffer.writeVarInt(nextState.protocolId)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Handshake (protocolId=$protocolId, hostname=${address.hostname}, port=${address.port}, nextState=$nextState)" }
    }
}
