package me.weishu.kernelsu.ui.component.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.Backdrop

/**
 * Adapts the manager's MIUIX backdrop to the official Kyant0 backdrop API so the liquid glass
 * bottom bar can render the same app content behind it.
 */
class MiuixBackdrop(
    private val delegate: top.yukonga.miuix.kmp.blur.Backdrop,
) : Backdrop {

    override val isCoordinatesDependent: Boolean = delegate.isCoordinatesDependent

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
    ) {
        with(delegate) { drawBackdrop(density, coordinates, layerBlock, 1) }
    }
}

@Composable
fun rememberLiquidBackdrop(
    backdrop: top.yukonga.miuix.kmp.blur.Backdrop,
): Backdrop = remember(backdrop) { MiuixBackdrop(backdrop) }
