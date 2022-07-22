package com.shinonometn.jam.networkd.ipjson

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.shinonometn.jam.networkd.base.utils.ProcessExecuteContext

private val json = ObjectMapper()

fun ProcessExecuteContext.ExecuteResult.outputAsJson() : JsonNode {
    return json.readTree(stdout)
}