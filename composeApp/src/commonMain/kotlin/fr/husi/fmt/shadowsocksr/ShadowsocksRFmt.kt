package fr.husi.fmt.shadowsocksr

import fr.husi.fmt.SingBoxOptions
import fr.husi.fmt.parseBoxOutbound
import fr.husi.ktx.JSONMap
import fr.husi.ktx.b64DecodeToString
import fr.husi.ktx.b64EncodeUrlSafe

fun parseShadowsocksR(rawUrl: String): ShadowsocksRBean {
    val b64Part = rawUrl.substringAfter("ssr://").trim()
    val decoded = b64Part.b64DecodeToString()
    val basePart = decoded.substringBefore("/?").substringBefore("?")
    val queryPart = if (decoded.contains("/?")) {
        decoded.substringAfter("/?")
    } else if (decoded.contains("?")) {
        decoded.substringAfter("?")
    } else {
        ""
    }

    val parts = basePart.split(":")
    if (parts.size < 6) {
        throw IllegalArgumentException("Invalid SSR URI: insufficient fields ($basePart)")
    }

    val passwordB64 = parts.last()
    val obfs = parts[parts.size - 2]
    val method = parts[parts.size - 3]
    val protocol = parts[parts.size - 4]
    val port = parts[parts.size - 5].toIntOrNull() ?: 8388
    val host = parts.subList(0, parts.size - 5).joinToString(":").removeSurrounding("[", "]")

    val password = runCatching { passwordB64.b64DecodeToString() }.getOrDefault(passwordB64)

    return ShadowsocksRBean().apply {
        serverAddress = host
        serverPort = port
        this.protocol = protocol
        this.method = method
        this.obfs = obfs
        this.password = password

        if (queryPart.isNotBlank()) {
            val params = queryPart.split("&")
            for (param in params) {
                val key = param.substringBefore("=").lowercase()
                val value = param.substringAfter("=", "")
                val decodedVal = runCatching { value.b64DecodeToString() }.getOrDefault(value)
                when (key) {
                    "remarks" -> name = decodedVal
                    "obfsparam", "obfs_param" -> obfsParam = decodedVal
                    "protoparam", "protocol_param" -> protocolParam = decodedVal
                }
            }
        }
    }
}

fun ShadowsocksRBean.toUri(): String {
    val pwdB64 = password.b64EncodeUrlSafe()
    val base = "${serverAddress}:${serverPort}:${protocol}:${method}:${obfs}:${pwdB64}"
    val queries = mutableListOf<String>()
    if (name.isNotBlank()) {
        queries.add("remarks=${name.b64EncodeUrlSafe()}")
    }
    if (obfsParam.isNotBlank()) {
        queries.add("obfsparam=${obfsParam.b64EncodeUrlSafe()}")
    }
    if (protocolParam.isNotBlank()) {
        queries.add("protoparam=${protocolParam.b64EncodeUrlSafe()}")
    }
    val full = if (queries.isNotEmpty()) {
        "$base/?" + queries.joinToString("&")
    } else {
        base
    }
    return "ssr://${full.b64EncodeUrlSafe()}"
}

fun parseShadowsocksROutbound(json: JSONMap): ShadowsocksRBean = ShadowsocksRBean().apply {
    parseBoxOutbound(json) { key, value ->
        when (key) {
            "password" -> password = value.toString()
            "method" -> method = value.toString()
            "protocol" -> protocol = value.toString()
            "protocol_param" -> protocolParam = value.toString()
            "obfs" -> obfs = value.toString()
            "obfs_param" -> obfsParam = value.toString()
        }
    }
}

suspend fun buildSingBoxOutboundShadowsocksRBean(bean: ShadowsocksRBean): SingBoxOptions.Outbound_ShadowsocksROptions {
    return SingBoxOptions.Outbound_ShadowsocksROptions().apply {
        type = SingBoxOptions.TYPE_SHADOWSOCKSR
        server = bean.serverAddress
        server_port = bean.serverPort
        method = bean.method
        password = bean.password
        if (bean.protocol.isNotBlank()) protocol = bean.protocol
        if (bean.protocolParam.isNotBlank()) protocol_param = bean.protocolParam
        if (bean.obfs.isNotBlank()) obfs = bean.obfs
        if (bean.obfsParam.isNotBlank()) obfs_param = bean.obfsParam
    }
}
