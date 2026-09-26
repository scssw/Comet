package fr.husi.fmt.shadowsocksr

import fr.husi.fmt.BeanConverters
import fr.husi.ktx.parseProxies
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ShadowsocksRFmtTest {

    @Test
    fun `parseProxies should parse user ssr link correctly`() = runTest {
        val link = "ssr://bTIuc3Nyci50b2RheToxNDE3ODphdXRoX2NoYWluX2E6bm9uZTpwbGFpbjplRUpXYW5aMy8_cmVtYXJrcz1UVEk2TVRReE56Z3ROaTQ1"
        val proxies = parseProxies(link)
        val bean = assertIs<ShadowsocksRBean>(proxies.single())

        assertEquals("m2.ssrr.today", bean.serverAddress)
        assertEquals(14178, bean.serverPort)
        assertEquals("auth_chain_a", bean.protocol)
        assertEquals("none", bean.method)
        assertEquals("plain", bean.obfs)
        assertEquals("xBVjvw", bean.password)
        assertEquals("M2:14178-6.9", bean.name)
    }

    @Test
    fun `ShadowsocksRBean should serialize and deserialize correctly`() {
        val source = ShadowsocksRBean().apply {
            serverAddress = "m2.ssrr.today"
            serverPort = 14178
            protocol = "auth_chain_a"
            method = "none"
            obfs = "plain"
            password = "xBVjvw"
            name = "M2:14178-6.9"
            obfsParam = "test_obfs_param"
            protocolParam = "test_proto_param"
        }

        val bytes = BeanConverters.serialize(source)
        val restored = BeanConverters.shadowsocksRDeserialize(bytes)!!

        assertEquals(source.serverAddress, restored.serverAddress)
        assertEquals(source.serverPort, restored.serverPort)
        assertEquals(source.protocol, restored.protocol)
        assertEquals(source.method, restored.method)
        assertEquals(source.obfs, restored.obfs)
        assertEquals(source.password, restored.password)
        assertEquals(source.name, restored.name)
        assertEquals(source.obfsParam, restored.obfsParam)
        assertEquals(source.protocolParam, restored.protocolParam)
    }

    @Test
    fun `buildSingBoxOutboundShadowsocksRBean should map all fields`() = runTest {
        val bean = ShadowsocksRBean().apply {
            serverAddress = "m2.ssrr.today"
            serverPort = 14178
            protocol = "auth_chain_a"
            method = "none"
            obfs = "plain"
            password = "xBVjvw"
            name = "M2:14178-6.9"
        }

        val outbound = buildSingBoxOutboundShadowsocksRBean(bean)

        assertEquals("shadowsocksr", outbound.type)
        assertEquals("m2.ssrr.today", outbound.server)
        assertEquals(14178, outbound.server_port)
        assertEquals("auth_chain_a", outbound.protocol)
        assertEquals("none", outbound.method)
        assertEquals("plain", outbound.obfs)
        assertEquals("xBVjvw", outbound.password)
    }
}
