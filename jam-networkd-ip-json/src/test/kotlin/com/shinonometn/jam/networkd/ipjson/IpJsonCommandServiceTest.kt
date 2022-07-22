package com.shinonometn.jam.networkd.ipjson

import com.shinonometn.jam.networkd.base.NetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.suspendCoroutine

class IpJsonCommandServiceTest {
    private val service = IpJsonCommandService()

    @BeforeEach
    fun check() {
        val result = service.isSupported()
        assumeTrue(result.isSupported, "Environment not supported: ${result.reason}.Test skip.")
    }

    @Test
    fun listInterfaces() = runBlocking {
        val result = suspendCoroutine<List<NetworkInterface>> { c ->
            service.listInterfaces {
                c.resumeWith(it)
            }
        }

        result.forEach {
            println("${it.name}\t:\t${it.state}")
        }
    }
}