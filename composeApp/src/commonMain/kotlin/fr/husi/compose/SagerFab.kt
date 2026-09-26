package fr.husi.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import fr.husi.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import fr.husi.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import fr.husi.bg.ServiceState
import fr.husi.database.DataStore
import fr.husi.platform.PlatformInfo
import fr.husi.repository.resolveRepository
import fr.husi.resources.*
import fr.husi.ui.StringOrRes
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import fr.husi.compose.theme.CosmicAurora

private val FabSize = 56.dp

// The margin Scaffold keeps between the floating action button and the content edge.
private val FabMargin = 16.dp

/** Bottom content padding that keeps list ends reachable above [SagerFab]. */
val SagerFabClearance = FabSize + FabMargin

@Composable
fun SagerFab(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    state: ServiceState,
    showSnackbar: (message: StringOrRes) -> Unit,
) {
    val connector = rememberVpnServiceLauncher {
        showSnackbar(StringOrRes.Res(Res.string.vpn_permission_denied))
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        Surface(
            onClick = {
                if (state.canStop) {
                    if (!PlatformInfo.isAndroid) {
                        DataStore.systemProxy.setBlocking(false)
                    }
                    resolveRepository().stopService()
                } else {
                    connector()
                }
            },
            modifier = modifier
                .size(FabSize)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFC044F5),
                    ambientColor = Color(0xFF7C4DFF),
                ),
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmicAurora.ButtonGradient, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above,
                    ),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(Res.string.connect))
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides Color.White,
                    ) {
                        if (state == ServiceState.Connected) {
                            Icon(
                                rememberVectorPainter(vectorResource(Res.drawable.ic_service_busy)),
                                stringResource(Res.string.connect),
                                tint = Color.White,
                            )
                        } else {
                            val animKey = when (state) {
                                ServiceState.Connecting -> 0
                                ServiceState.Stopping -> 1
                                else -> 2
                            }
                            key(animKey) {
                                AnimatedServiceIcon(state, stringResource(Res.string.connect))
                            }
                        }
                    }
                }
            }
        }
    }
}
