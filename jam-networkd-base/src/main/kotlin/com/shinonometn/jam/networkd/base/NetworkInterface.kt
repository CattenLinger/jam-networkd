package com.shinonometn.jam.networkd.base

interface NetworkInterface {
    enum class State {
        UP, DOWN, UNKNOWN;

        companion object {
            fun stateOf(name : String) : State {
                for(va in values()) if(name.uppercase() == name) return va
                return UNKNOWN
            }
        }
    }

    val name : String
    val state : State
}