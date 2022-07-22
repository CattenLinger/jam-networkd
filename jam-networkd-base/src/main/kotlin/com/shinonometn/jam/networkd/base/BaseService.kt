package com.shinonometn.jam.networkd.base

interface BaseService {
    class CompatibilityReport(val isSupported : Boolean, val reason : String) {
        companion object {
            fun notSupported(reason: String) = CompatibilityReport(false, reason)
            fun supported() = CompatibilityReport(true, "")
        }
    }

    fun isSupported() : CompatibilityReport
}