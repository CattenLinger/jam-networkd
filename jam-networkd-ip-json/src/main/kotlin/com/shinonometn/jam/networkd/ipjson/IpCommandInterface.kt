package com.shinonometn.jam.networkd.ipjson

import com.fasterxml.jackson.databind.JsonNode
import com.shinonometn.jam.networkd.base.NetworkInterface

class IpCommandInterface(override val name: String, override val state: NetworkInterface.State) : NetworkInterface {
    companion object {
        fun fromJson(json : JsonNode) : IpCommandInterface {
            return IpCommandInterface(
                json["ifname"].asText(),
                NetworkInterface.State.stateOf(json["operstate"].asText())
            )
        }
    }
}