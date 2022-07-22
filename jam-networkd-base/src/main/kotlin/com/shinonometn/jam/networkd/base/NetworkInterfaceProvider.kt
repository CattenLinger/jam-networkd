package com.shinonometn.jam.networkd.base

interface NetworkInterfaceProvider : BaseService {
    fun listInterfaces(callback : (Result<List<NetworkInterface>>) -> Unit)
}