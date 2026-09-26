package fr.husi.compose.material3

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import fr.husi.compose.collectAsStateWithLifecycle
import fr.husi.compose.theme.LocalAppDarkMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import fr.husi.database.DataStore
import java.awt.BasicStroke
import java.awt.Cursor
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import fr.husi.bg.BackendState
import fr.husi.resources.Res
import fr.husi.resources.comet_logo
import java.awt.geom.Rectangle2D

internal object DesktopPlatformMaterialApi : PlatformMaterialApi by standardPlatformMaterialApi() {
    @Composable
    override fun NavigationSuite(
        items: ImmutableList<NavigationSuiteItem>,
        showNavigation: Boolean,
        snackbarHost: @Composable () -> Unit,
        floatingActionButton: @Composable () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val initialWidth = remember {
            val saved = DataStore.desktopNavRailWidth.getBlocking()
            if (saved < 190) 210.dp else saved.dp
        }
        var railWidth by remember { mutableStateOf(initialWidth) }
        val frostedGlass by DataStore.windowFrostedGlass.collectAsStateWithLifecycle()
        val isDarkMode = LocalAppDarkMode.current
        val serviceStatus by BackendState.status.collectAsState()
        val isRunning = serviceStatus.state == fr.husi.bg.ServiceState.Connected

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. 底层梦幻星云流光与星芒背景
            fr.husi.compose.theme.CosmicBackgroundAura(
                modifier = Modifier.fillMaxSize(),
                isDarkMode = isDarkMode,
            )

            // 2. 主体毛玻璃脚手架
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = snackbarHost,
                floatingActionButton = floatingActionButton,
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    val containerWidth = maxWidth
                    fun clampWidth(width: Dp) = width.coerceIn(
                        180.dp,
                        minOf(360.dp, containerWidth * 0.45f).coerceAtLeast(180.dp),
                    )

                    val displayedWidth = clampWidth(railWidth)
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

                    Row(modifier = Modifier.fillMaxSize()) {
                        // 左侧梦幻琉璃侧边栏 (Cosmic Sidebar)
                        Box(
                            modifier = Modifier
                                .width(displayedWidth)
                                .fillMaxHeight()
                                .background(
                                    if (isDarkMode) {
                                        Color(0xFF161828).copy(alpha = if (frostedGlass) 0.60f else 0.88f)
                                    } else {
                                        Color(0xFFFFFFFF).copy(alpha = if (frostedGlass) 0.65f else 0.82f)
                                    },
                                ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // 顶部 Comet 行星 Logo (Windows 风格放大展示，移除 macOS 控制点)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 18.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = org.jetbrains.compose.resources.painterResource(fr.husi.resources.Res.drawable.comet_logo),
                                        contentDescription = "Comet Logo",
                                        modifier = Modifier.size(96.dp),
                                    )
                                }

                                // 导航项列表 (Pill Shape Navigation Items)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    items.forEach { item ->
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val isHovered by interactionSource.collectIsHoveredAsState()
                                        val selected = item.selected

                                        val backgroundColor = when {
                                            selected -> if (isDarkMode) {
                                                Color(0xFF3B2D68).copy(alpha = 0.70f)
                                            } else {
                                                Color(0xFFEDE9FE).copy(alpha = 0.90f)
                                            }
                                            isHovered -> if (isDarkMode) {
                                                Color(0x1FFFFFFF)
                                            } else {
                                                Color(0x0F000000)
                                            }
                                            else -> Color.Transparent
                                        }

                                        val contentColor = when {
                                            selected -> if (isDarkMode) Color(0xFFC4B5FD) else Color(0xFF6B4DF5)
                                            else -> if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                                .background(backgroundColor)
                                                .hoverable(interactionSource)
                                                .clickable(onClick = item.onClick)
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.CenterStart,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Icon(
                                                    imageVector = vectorResource(item.icon),
                                                    contentDescription = stringResource(item.label),
                                                    tint = contentColor,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                                androidx.compose.material3.Text(
                                                    text = stringResource(item.label),
                                                    color = contentColor,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }

                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

                                // 底部左下角状态与版本信息 (Status & Version Indicator)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(
                                                        if (isRunning) Color(0xFF10B981) else Color(0xFF94A3B8),
                                                    ),
                                            )
                                            androidx.compose.material3.Text(
                                                text = if (isRunning) "运行中" else "未连接",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = if (isRunning) Color(0xFF10B981) else Color(0xFF94A3B8),
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                                ),
                                            )
                                        }
                                        androidx.compose.material3.Text(
                                            text = "v2.2.0",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                                            ),
                                            modifier = Modifier.padding(start = 16.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // 侧边栏与主内容区的琉璃细分割线
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(
                                    if (isDarkMode) {
                                        Color(0x18FFFFFF)
                                    } else {
                                        Color(0xFFE2E8F0).copy(alpha = 0.6f)
                                    },
                                ),
                        )

                        // 主内容展示区
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            content()
                        }
                    }

                    DesktopNavRailResizeHandle(
                        modifier = Modifier
                            .align(
                                if (isRtl) {
                                    Alignment.CenterEnd
                                } else {
                                    Alignment.CenterStart
                                },
                            )
                            .offset(
                                x = if (isRtl) {
                                    -(displayedWidth - 6.dp)
                                } else {
                                    displayedWidth - 6.dp
                                },
                            )
                            .zIndex(1f),
                        onDrag = { delta ->
                            railWidth = clampWidth(
                                railWidth + if (isRtl) {
                                    -delta
                                } else {
                                    delta
                                },
                            )
                        },
                        onDragFinished = {
                            DataStore.desktopNavRailWidth.setBlocking(railWidth.value.roundToInt())
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopNavRailResizeHandle(
    onDrag: (Dp) -> Unit,
    onDragFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    var dragging by remember { mutableStateOf(false) }
    val dividerColor by animateColorAsState(
        targetValue = if (hovered || dragging) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
    )
    val draggableState = rememberDraggableState { deltaPx ->
        currentOnDrag(with(density) { deltaPx.toDp() })
    }
    val resizeIcon = remember { horizontalResizePointerIcon() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(12.dp)
            .pointerHoverIcon(resizeIcon)
            .hoverable(interactionSource)
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = { dragging = true },
                onDragStopped = {
                    dragging = false
                    currentOnDragFinished()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = dividerColor,
        )
    }
}

/**
 * [java.awt.Cursor.E_RESIZE_CURSOR] only displays left and right arrow on Windows.
 * So we draw it ourselves to make sure it always left and right arrow.
 *
 * Translate from: [adwaita.svg - lc-resize](https://gitlab.gnome.org/GNOME/adwaita-icon-theme/-/blob/d78e7194cd8319914959e2abd40442108fe2805f/src/cursors/adwaita.svg#L11701)
 * with [LGPL](https://gitlab.gnome.org/GNOME/adwaita-icon-theme/-/raw/d78e7194cd8319914959e2abd40442108fe2805f/COPYING_LGPL)
 */
private fun horizontalResizePointerIcon(): PointerIcon {
    val toolkit = Toolkit.getDefaultToolkit()
    val best = toolkit.getBestCursorSize(48, 48)
    val width = if (best.width > 0) best.width else 48
    val height = if (best.height > 0) best.height else 48
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val centerX = width / 2f
    val centerY = height / 2f

    val arrow = Path2D.Float().apply {
        val tip = width * 0.46f
        val neck = width * 0.22f
        val bar = height * 0.11f
        val head = height * 0.34f
        moveTo(centerX - tip, centerY)
        lineTo(centerX - neck, centerY - head)
        lineTo(centerX - neck, centerY - bar)
        lineTo(centerX + neck, centerY - bar)
        lineTo(centerX + neck, centerY - head)
        lineTo(centerX + tip, centerY)
        lineTo(centerX + neck, centerY + head)
        lineTo(centerX + neck, centerY + bar)
        lineTo(centerX - neck, centerY + bar)
        lineTo(centerX - neck, centerY + head)
        closePath()
    }

    graphics.stroke = BasicStroke(
        (width / 24f).coerceAtLeast(1f),
        BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_MITER,
        10f,
    )
    graphics.color = java.awt.Color.WHITE
    graphics.draw(arrow)
    graphics.color = java.awt.Color.BLACK
    graphics.fill(arrow)

    val barHalfW = (width / 36f).coerceAtLeast(0.5f)
    val barHalfH = height * 0.46f
    val bar = Rectangle2D.Float(
        centerX - barHalfW, centerY - barHalfH,
        barHalfW * 2, barHalfH * 2,
    )
    graphics.stroke = BasicStroke(
        (width / 12f).coerceAtLeast(2f),
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER,
        10f,
    )
    graphics.color = java.awt.Color.WHITE
    graphics.draw(bar)
    graphics.color = java.awt.Color.BLACK
    graphics.fill(bar)

    graphics.dispose()
    return runCatching {
        PointerIcon(
            toolkit.createCustomCursor(image, Point(width / 2, height / 2), "col-resize"),
        )
    }.getOrElse {
        PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
    }
}