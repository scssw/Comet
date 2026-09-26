package fr.husi.compose.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import fr.husi.database.ProxyEntity

/**
 * Comet Cosmic Aurora Design System
 * 梦幻星云琉璃主题调色板与视觉资源定义
 */
object CosmicAurora {
    // 主流光颜色
    val VioletPrimary = Color(0xFF6B4DF5)
    val VioletSecondary = Color(0xFF8B5CF6)
    val PinkGlow = Color(0xFFFF6584)
    val MagentaAccent = Color(0xFFC044F5)
    val SkyCyan = Color(0xFF38BDF8)
    val MintGreen = Color(0xFF10B981)
    val CosmicAmber = Color(0xFFFBBF24)

    // 浅色模式琉璃背景与卡片
    val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.85f)
    val GlassWhiteSubtle = Color(0xFFFFFFFF).copy(alpha = 0.55f)
    val GlassCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.70f)
    val GlassBorderGlow = Color(0xFFD8B4FE).copy(alpha = 0.60f)

    // 暗色模式星空琉璃
    val DarkNebulaBase = Color(0xFF121324)
    val DarkGlassCard = Color(0xFF1A1C30).copy(alpha = 0.85f)
    val DarkGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)

    // 文本颜色
    val TextPrimaryLight = Color(0xFF1E1B4B)
    val TextSecondaryLight = Color(0xFF64748B)
    val TextPrimaryDark = Color(0xFFF8FAFC)
    val TextSecondaryDark = Color(0xFF94A3B8)

    // 渐变画刷
    val ButtonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF7C4DFF), Color(0xFFDF40FB)),
    )

    val ActiveCardBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF85A2), Color(0xFF7C4DFF)),
    )

    val ChipActiveGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF635BFF), Color(0xFFA855F7)),
    )

    /**
     * 根据协议类型获取卡片左侧图标块的渐变背景与图标色
     */
    fun protocolColors(type: Int): Pair<Brush, Color> {
        return when (type) {
            ProxyEntity.TYPE_SSR, ProxyEntity.TYPE_SS -> {
                // 梦幻淡紫
                Brush.linearGradient(
                    listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)),
                ) to Color(0xFF7C3AED)
            }
            ProxyEntity.TYPE_NAIVE, ProxyEntity.TYPE_HTTP, ProxyEntity.TYPE_SOCKS -> {
                // 天空蔚蓝
                Brush.linearGradient(
                    listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
                ) to Color(0xFF0284C7)
            }
            ProxyEntity.TYPE_VLESS, ProxyEntity.TYPE_VMESS, ProxyEntity.TYPE_TROJAN -> {
                // 清新薄荷绿
                Brush.linearGradient(
                    listOf(Color(0xFFECFDF5), Color(0xFFA7F3D0)),
                ) to Color(0xFF059669)
            }
            ProxyEntity.TYPE_HYSTERIA, ProxyEntity.TYPE_TUIC, ProxyEntity.TYPE_JUICITY -> {
                // 极速珊瑚粉橙
                Brush.linearGradient(
                    listOf(Color(0xFFFFF1F2), Color(0xFFFECDD3)),
                ) to Color(0xFFE11D48)
            }
            else -> {
                // 默认优雅淡紫
                Brush.linearGradient(
                    listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)),
                ) to Color(0xFF9333EA)
            }
        }
    }
}

/**
 * 绘制用户截图中标志性的四芒星光（Twinkle Star）
 */
fun DrawScope.drawCosmicStar(
    center: Offset,
    radius: Float,
    color: Color = Color(0xFF818CF8).copy(alpha = 0.85f),
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color, style = Fill)
}

/**
 * 绘制用户截图中右上角/左下角的梦幻星云波浪与光环（Cosmic Glow Ring & Nebula Ribbon）
 */
@Composable
fun CosmicBackgroundAura(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_aura")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        // 1. 全局柔和基底微光 (Ambient Light Gradient)
        if (isDarkMode) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF141426),
                        Color(0xFF191830),
                        Color(0xFF131526),
                    ),
                ),
            )
        } else {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF6F8FF),
                        Color(0xFFF1F5FE),
                        Color(0xFFF8F5FF),
                    ),
                ),
            )
        }

        // 2. 左下角星云流光与紫粉光晕 (Bottom-Left Nebula Bloom)
        val blRadius = minOf(width, height) * 0.65f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (isDarkMode) {
                    listOf(
                        Color(0xFF6B4DF5).copy(alpha = 0.28f),
                        Color(0xFFFF6584).copy(alpha = 0.16f),
                        Color(0xFF38BDF8).copy(alpha = 0.08f),
                        Color.Transparent,
                    )
                } else {
                    listOf(
                        Color(0xFFC4B5FD).copy(alpha = 0.45f),
                        Color(0xFFFBCFE8).copy(alpha = 0.35f),
                        Color(0xFFBAE6FD).copy(alpha = 0.25f),
                        Color.Transparent,
                    )
                },
                center = Offset(width * 0.08f, height * 0.92f),
                radius = blRadius,
            ),
            center = Offset(width * 0.08f, height * 0.92f),
            radius = blRadius,
        )

        // 3. 右上角梦幻微行星与光晕 (Top-Right Planet Glow & Ring)
        val trRadius = minOf(width, height) * 0.45f * pulse
        val planetCenter = Offset(width * 0.88f, height * 0.12f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (isDarkMode) {
                    listOf(
                        Color(0xFFFF7BB0).copy(alpha = 0.25f),
                        Color(0xFF7C4DFF).copy(alpha = 0.18f),
                        Color.Transparent,
                    )
                } else {
                    listOf(
                        Color(0xFFFBCFE8).copy(alpha = 0.40f),
                        Color(0xFFDDD6FE).copy(alpha = 0.25f),
                        Color.Transparent,
                    )
                },
                center = planetCenter,
                radius = trRadius,
            ),
            center = planetCenter,
            radius = trRadius,
        )

        // 右上角行星光环微弱圆弧
        drawOval(
            color = if (isDarkMode) Color(0xFFC084FC).copy(alpha = 0.22f) else Color(0xFFC084FC).copy(alpha = 0.35f),
            topLeft = Offset(planetCenter.x - 90f, planetCenter.y - 25f),
            size = androidx.compose.ui.geometry.Size(180f, 50f),
            style = Stroke(width = 2.5f),
        )

        // 4. 右下角发送按钮周围的梦幻光晕 (Bottom-Right Glow Bloom)
        val brCenter = Offset(width * 0.90f, height * 0.88f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = if (isDarkMode) {
                    listOf(
                        Color(0xFFDF40FB).copy(alpha = 0.22f),
                        Color(0xFF7C4DFF).copy(alpha = 0.12f),
                        Color.Transparent,
                    )
                } else {
                    listOf(
                        Color(0xFFF472B6).copy(alpha = 0.30f),
                        Color(0xFFC084FC).copy(alpha = 0.18f),
                        Color.Transparent,
                    )
                },
                center = brCenter,
                radius = minOf(width, height) * 0.35f,
            ),
            center = brCenter,
            radius = minOf(width, height) * 0.35f,
        )

        // 5. 散落的点缀星芒 (Twinkle Stars)
        val starTint = if (isDarkMode) Color(0xFFE2E8F0).copy(alpha = 0.7f) else Color(0xFF818CF8).copy(alpha = 0.75f)
        drawCosmicStar(Offset(width * 0.09f, height * 0.68f), radius = 10f * pulse, color = starTint)
        drawCosmicStar(Offset(width * 0.76f, height * 0.16f), radius = 7f, color = starTint)
        drawCosmicStar(Offset(width * 0.69f, height * 0.82f), radius = 8f * pulse, color = starTint)
        drawCosmicStar(Offset(width * 0.92f, height * 0.83f), radius = 5f, color = starTint)
    }
}
