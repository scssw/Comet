package fr.husi.compose

import androidx.compose.runtime.Composable
import fr.husi.Key
import fr.husi.database.DataStore
import fr.husi.repository.resolveRepository

@Composable
actual fun rememberVpnServiceLauncher(onFailed: () -> Unit): () -> Unit {
    return {
        if (DataStore.serviceMode.getBlocking() == Key.MODE_PROXY) {
            DataStore.systemProxy.setBlocking(true)
        } else if (DataStore.serviceMode.getBlocking() == Key.MODE_VPN) {
            if (DataStore.systemProxy.getBlocking()) {
                DataStore.systemProxy.setBlocking(false)
            }
        }
        resolveRepository().startService()
    }
}
