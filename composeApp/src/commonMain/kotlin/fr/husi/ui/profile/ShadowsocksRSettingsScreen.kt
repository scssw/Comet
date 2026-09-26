package fr.husi.ui.profile

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import fr.husi.compose.IconMaskColors
import fr.husi.compose.ListPreference
import fr.husi.compose.MaskedIcon
import fr.husi.compose.PasswordPreference
import fr.husi.compose.PreferenceCategory
import fr.husi.compose.TextFieldPreference
import fr.husi.compose.material3.Text
import fr.husi.compose.preferenceGroup
import fr.husi.ktx.contentOrUnset
import fr.husi.resources.Res
import fr.husi.resources.directions_boat
import fr.husi.resources.edit
import fr.husi.resources.emoji_symbols
import fr.husi.resources.enc_method
import fr.husi.resources.enhanced_encryption
import fr.husi.resources.profile_config
import fr.husi.resources.profile_name
import fr.husi.resources.proxy_cat
import fr.husi.resources.router
import fr.husi.resources.server_address
import fr.husi.resources.server_port
import fr.husi.resources.settings
import fr.husi.resources.view_in_ar
import fr.husi.ui.NavRoutes
import me.zhanghai.compose.preference.ListPreferenceType
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShadowsocksRSettingsScreen(
    profileId: Long,
    isSubscription: Boolean,
    onResult: (updated: Boolean) -> Unit,
    onOpenConfigEditor: (NavRoutes.ConfigEditor) -> Unit,
) {
    val viewModel: ShadowsocksRSettingsViewModel = profileEditorViewModel(
        profileId = profileId,
        isSubscription = isSubscription,
    ) {
        ShadowsocksRSettingsViewModel()
    }

    ProfileSettingsScreenScaffold(
        title = Res.string.profile_config,
        viewModel = viewModel,
        onResult = onResult,
        onOpenConfigEditor = onOpenConfigEditor,
    ) { uiState, _ ->
        shadowsocksRSettings(
            uiState as ShadowsocksRUiState,
            viewModel,
        )
    }
}

private fun LazyListScope.shadowsocksRSettings(
    uiState: ShadowsocksRUiState,
    viewModel: ShadowsocksRSettingsViewModel,
) {
    val encryptionMethods = listOf(
        "none",
        "aes-128-cfb",
        "aes-192-cfb",
        "aes-256-cfb",
        "aes-128-ctr",
        "aes-192-ctr",
        "aes-256-ctr",
        "rc4-md5",
        "chacha20",
        "chacha20-ietf",
        "salsa20",
    )

    val protocols = listOf(
        "origin",
        "auth_sha1_v4",
        "auth_aes128_md5",
        "auth_aes128_sha1",
        "auth_chain_a",
        "auth_chain_b",
        "verify_sha1",
    )

    val obfsList = listOf(
        "plain",
        "http_simple",
        "http_post",
        "random_head",
        "tls12_ticket_auth",
    )

    preferenceGroup(key = "name") {
        TextFieldPreference(
            value = uiState.name,
            onValueChange = { viewModel.setName(it) },
            title = { Text(stringResource(Res.string.profile_name)) },
            textToValue = { it },
            icon = {
                MaskedIcon(
                    Res.drawable.emoji_symbols,
                    color = IconMaskColors.IconCyan,
                )
            },
            summary = { Text(contentOrUnset(uiState.name)) },
            valueToText = { it },
        )
    }

    item("category_proxy") {
        PreferenceCategory(text = { Text(stringResource(Res.string.proxy_cat)) })
    }
    preferenceGroup(key = "address") {
        TextFieldPreference(
            value = uiState.address,
            onValueChange = { viewModel.setAddress(it) },
            title = { Text(stringResource(Res.string.server_address)) },
            textToValue = { it },
            icon = {
                MaskedIcon(Res.drawable.router, color = IconMaskColors.IconCyan)
            },
            summary = { Text(contentOrUnset(uiState.address)) },
            valueToText = { it },
        )
        TextFieldPreference(
            value = uiState.port,
            onValueChange = { viewModel.setPort(it) },
            title = { Text(stringResource(Res.string.server_port)) },
            textToValue = { it.toIntOrNull() ?: 8388 },
            icon = {
                MaskedIcon(
                    Res.drawable.directions_boat,
                    color = IconMaskColors.IconCyan,
                )
            },
            summary = { Text(uiState.port.toString()) },
            valueToText = { it.toString() },
        )
        PasswordPreference(
            value = uiState.password,
            onValueChange = { viewModel.setPassword(it) },
            icon = {
                MaskedIcon(
                    Res.drawable.enhanced_encryption,
                    color = IconMaskColors.IconCyan,
                )
            },
        )
    }

    item("category_ssr_crypto") {
        PreferenceCategory(text = { Text(stringResource(Res.string.settings)) })
    }
    preferenceGroup(key = "ssr_crypto") {
        ListPreference(
            value = uiState.method,
            onValueChange = { viewModel.setMethod(it) },
            values = if (uiState.method in encryptionMethods) encryptionMethods else listOf(uiState.method) + encryptionMethods,
            title = { Text(stringResource(Res.string.enc_method)) },
            summary = { Text(contentOrUnset(uiState.method)) },
            type = ListPreferenceType.DROPDOWN_MENU,
            icon = {
                MaskedIcon(Res.drawable.edit, color = IconMaskColors.IconCyan)
            },
        )
        ListPreference(
            value = uiState.protocol,
            onValueChange = { viewModel.setProtocol(it) },
            values = if (uiState.protocol in protocols) protocols else listOf(uiState.protocol) + protocols,
            title = { Text("Protocol") },
            summary = { Text(contentOrUnset(uiState.protocol)) },
            type = ListPreferenceType.DROPDOWN_MENU,
            icon = {
                MaskedIcon(Res.drawable.view_in_ar, color = IconMaskColors.IconCyan)
            },
        )
        TextFieldPreference(
            value = uiState.protocolParam,
            onValueChange = { viewModel.setProtocolParam(it) },
            title = { Text("Protocol Param") },
            textToValue = { it },
            summary = { Text(contentOrUnset(uiState.protocolParam)) },
            valueToText = { it },
        )
        ListPreference(
            value = uiState.obfs,
            onValueChange = { viewModel.setObfs(it) },
            values = if (uiState.obfs in obfsList) obfsList else listOf(uiState.obfs) + obfsList,
            title = { Text("Obfs") },
            summary = { Text(contentOrUnset(uiState.obfs)) },
            type = ListPreferenceType.DROPDOWN_MENU,
            icon = {
                MaskedIcon(Res.drawable.view_in_ar, color = IconMaskColors.IconCyan)
            },
        )
        TextFieldPreference(
            value = uiState.obfsParam,
            onValueChange = { viewModel.setObfsParam(it) },
            title = { Text("Obfs Param") },
            textToValue = { it },
            summary = { Text(contentOrUnset(uiState.obfsParam)) },
            valueToText = { it },
        )
    }
}
