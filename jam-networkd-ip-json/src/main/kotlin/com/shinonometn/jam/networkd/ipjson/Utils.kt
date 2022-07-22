package com.shinonometn.jam.networkd.ipjson

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.shinonometn.jam.networkd.base.utils.ShellExecuteContext

private val json = ObjectMapper()

fun ShellExecuteContext.ExecuteResult.outputAsJson() : JsonNode {
    return json.readTree(stdout)
}