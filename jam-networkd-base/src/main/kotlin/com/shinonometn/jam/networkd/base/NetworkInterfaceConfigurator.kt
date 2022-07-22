package com.shinonometn.jam.networkd.base

interface NetworkInterfaceConfigurator : BaseService {
    fun up(nInterface : NetworkInterface, callback : (Result<Any>) -> Unit)
    fun down(nInterface: NetworkInterface, callback : (Result<Any>) -> Unit)
}