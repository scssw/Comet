package fr.husi

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import fr.husi.compose.collectAsStateWithLifecycle
import fr.husi.compose.theme.LocalAppDarkMode
import fr.husi.database.DataStore
import fr.husi.ktx.Logs
import fr.husi.platform.PlatformInfo
import kotlinx.coroutines.delay
import java.awt.Window

internal object WindowsDwm {
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
    private const val DWMWA_CAPTION_COLOR = 35
    private const val DWMWA_TEXT_COLOR = 36
    private const val DWMWA_SYSTEMBACKDROP_TYPE = 38

    const val DWMSBT_AUTO = 0
    const val DWMSBT_NONE = 1
    const val DWMSBT_MAINWINDOW = 2
    const val DWMSBT_TRANSIENTWINDOW = 3 // Acrylic
    const val DWMSBT_TABBEDWINDOW = 4

    const val DWMWA_COLOR_DEFAULT = 0xFFFFFFFF.toInt()
    const val DWMWA_COLOR_NONE = 0xFFFFFFFE.toInt()

    @Suppress("FunctionName")
    private interface Dwmapi : Library {
        fun DwmSetWindowAttribute(
            hwnd: Pointer,
            dwAttribute: Int,
            pvAttribute: Pointer,
            cbAttribute: Int,
        ): Int
    }

    private val dwmapi: Dwmapi? by lazy {
        if (!PlatformInfo.isWindows) null
        else runCatching {
            Native.load("dwmapi", Dwmapi::class.java)
        }.onFailure { Logs.w("Failed to load dwmapi.dll", it) }.getOrNull()
    }

    private fun setDwmIntAttribute(hwnd: Pointer, attribute: Int, value: Int): Boolean {
        val lib = dwmapi ?: return false
        return runCatching {
            val mem = Memory(4)
            mem.setInt(0, value)
            lib.DwmSetWindowAttribute(hwnd, attribute, mem, 4) == 0
        }.getOrDefault(false)
    }

    fun applyWindowStyle(
        window: Window,
        colorScheme: ColorScheme,
        isDarkMode: Boolean,
        frostedGlass: Boolean,
    ) {
        if (!PlatformInfo.isWindows) return
        val hwnd = runCatching { Native.getWindowPointer(window) }.getOrNull() ?: return

        if (frostedGlass) {
            // Windows 11 Fluent Mica Alt (TabbedWindow) backdrop
            setDwmIntAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, DWMSBT_TABBEDWINDOW)
            // Allow backdrop to extend under the title bar
            setDwmIntAttribute(hwnd, DWMWA_CAPTION_COLOR, DWMWA_COLOR_NONE)
            setDarkMode(hwnd, isDarkMode)
            val textColorRef = colorToColorRef(if (isDarkMode) colorScheme.onSurface else Color(0xFF1E293B))
            setDwmIntAttribute(hwnd, DWMWA_TEXT_COLOR, textColorRef)
        } else {
            // Standard theme: reset backdrop to solid
            setDwmIntAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, DWMSBT_NONE)

            if (isDarkMode) {
                setDarkMode(hwnd, true)
                val captionColorRef = colorToColorRef(colorScheme.surface)
                val textColorRef = colorToColorRef(colorScheme.onSurface)
                setDwmIntAttribute(hwnd, DWMWA_CAPTION_COLOR, captionColorRef)
                setDwmIntAttribute(hwnd, DWMWA_TEXT_COLOR, textColorRef)
            } else {
                // In light mode: Win11 clean light title bar with dark caption icons/text
                setDarkMode(hwnd, false)
                val captionColorRef = colorToColorRef(Color(0xFFF0F4FA))
                val textColorRef = colorToColorRef(Color(0xFF1E293B))
                setDwmIntAttribute(hwnd, DWMWA_CAPTION_COLOR, captionColorRef)
                setDwmIntAttribute(hwnd, DWMWA_TEXT_COLOR, textColorRef)
            }
        }
    }

    private fun setDarkMode(hwnd: Pointer, isDark: Boolean) {
        val value = if (isDark) 1 else 0
        if (!setDwmIntAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, value)) {
            setDwmIntAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1, value)
        }
    }

    private fun colorToColorRef(color: Color): Int {
        val argb = color.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return (b shl 16) or (g shl 8) or r
    }
}

@Composable
internal fun ConfigureWindowsWindow(window: Window) {
    if (!PlatformInfo.isWindows) return
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = LocalAppDarkMode.current
    val frostedGlass by DataStore.windowFrostedGlass.collectAsStateWithLifecycle()

    LaunchedEffect(window, colorScheme, isDarkMode, frostedGlass) {
        while (!window.isDisplayable) {
            delay(50)
        }
        WindowsDwm.applyWindowStyle(
            window = window,
            colorScheme = colorScheme,
            isDarkMode = isDarkMode,
            frostedGlass = frostedGlass,
        )
    }
}
