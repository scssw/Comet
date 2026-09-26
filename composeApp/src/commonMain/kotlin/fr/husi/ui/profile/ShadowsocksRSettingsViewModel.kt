package fr.husi.ui.profile

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import fr.husi.fmt.shadowsocksr.ShadowsocksRBean
import fr.husi.ktx.applyDefaultValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Immutable
internal data class ShadowsocksRUiState(
    override val customConfig: String = "",
    override val customOutbound: String = "",
    val name: String = "",
    val address: String = "127.0.0.1",
    val port: Int = 8388,
    val method: String = "none",
    val password: String = "",
    val protocol: String = "auth_chain_a",
    val protocolParam: String = "",
    val obfs: String = "plain",
    val obfsParam: String = "",
) : ProfileEditorUiState

@Stable
internal class ShadowsocksRSettingsViewModel : ProfileEditorViewModel<ShadowsocksRBean>() {
    override fun createBean() = ShadowsocksRBean().applyDefaultValues()

    override val uiState: StateFlow<ShadowsocksRUiState>
        field = MutableStateFlow(ShadowsocksRUiState())

    override suspend fun ShadowsocksRBean.writeToUiState() {
        uiState.update {
            it.copy(
                customConfig = customConfigJson,
                customOutbound = customOutboundJson,
                name = name,
                address = serverAddress,
                port = serverPort,
                method = method,
                password = password,
                protocol = protocol,
                protocolParam = protocolParam,
                obfs = obfs,
                obfsParam = obfsParam,
            )
        }
    }

    override fun ShadowsocksRBean.loadFromUiState() {
        val state = uiState.value
        customConfigJson = state.customConfig
        customOutboundJson = state.customOutbound
        name = state.name
        serverAddress = state.address
        serverPort = state.port
        method = state.method
        password = state.password
        protocol = state.protocol
        protocolParam = state.protocolParam
        obfs = state.obfs
        obfsParam = state.obfsParam
    }

    override fun setCustomConfig(config: String) {
        uiState.update { it.copy(customConfig = config) }
    }

    override fun setCustomOutbound(outbound: String) {
        uiState.update { it.copy(customOutbound = outbound) }
    }

    fun setName(name: String) {
        uiState.update { it.copy(name = name) }
    }

    fun setAddress(address: String) {
        uiState.update { it.copy(address = address) }
    }

    fun setPort(port: Int) {
        uiState.update { it.copy(port = port) }
    }

    fun setMethod(method: String) {
        uiState.update { it.copy(method = method) }
    }

    fun setPassword(password: String) {
        uiState.update { it.copy(password = password) }
    }

    fun setProtocol(protocol: String) {
        uiState.update { it.copy(protocol = protocol) }
    }

    fun setProtocolParam(param: String) {
        uiState.update { it.copy(protocolParam = param) }
    }

    fun setObfs(obfs: String) {
        uiState.update { it.copy(obfs = obfs) }
    }

    fun setObfsParam(param: String) {
        uiState.update { it.copy(obfsParam = param) }
    }
}
