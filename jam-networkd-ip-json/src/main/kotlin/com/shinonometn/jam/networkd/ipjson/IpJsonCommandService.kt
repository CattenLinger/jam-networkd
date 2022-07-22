package com.shinonometn.jam.networkd.ipjson

import com.shinonometn.jam.networkd.base.BaseService
import com.shinonometn.jam.networkd.base.NetworkInterface
import com.shinonometn.jam.networkd.base.NetworkInterfaceProvider
import com.shinonometn.jam.networkd.base.utils.shell
import org.apache.commons.lang3.SystemUtils

class IpJsonCommandService : NetworkInterfaceProvider {

    override fun listInterfaces(callback: (Result<List<NetworkInterface>>) -> Unit) {
        shell(ip("link")).execute {
            if(it.isFailure) callback(Result.failure(it.exceptionOrNull()!!))
            else callback(Result.success(it.getOrThrow().outputAsJson().map(IpCommandInterface::fromJson)))
        }
    }

    private fun ip(vararg args : String) : String {
        return (arrayOf("ip", "-j") + args).joinToString(" ")
    }

    override fun isSupported(): BaseService.CompatibilityReport {
        if(!SystemUtils.IS_OS_LINUX) return BaseService.CompatibilityReport.notSupported("Require System: Linux")
        val executeResult = shell("command -v ip").execute().get()
        if(executeResult.exitCode != 0) return BaseService.CompatibilityReport.notSupported("Require command: ip")
        return BaseService.CompatibilityReport.supported()
    }
}