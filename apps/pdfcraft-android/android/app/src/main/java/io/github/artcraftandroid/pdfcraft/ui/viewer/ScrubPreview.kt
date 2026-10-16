package io.github.artcraftandroid.pdfcraft.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artcraftandroid.pdfcraft.engine.toBitmap
import io.github.artcraftandroid.pdfcraft.ui.AppViewModel
import io.github.artcraftandroid.pdfcraft.ui.GLASS_PANEL_ALPHA
import io.github.artcraftandroid.pdfcraft.ui.OpenDocument
import io.github.artcraftandroid.pdfcraft.ui.PdfShapes
import io.github.artcraftandroid.pdfcraft.ui.floatingSurface
import io.github.artcraftandroid.pdfcraft.ui.pdfColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** The longest side of a preview, in dp, and in pixels rendered. */
private const val PREVIEW_PX = 360f

/**
 * A small glass card with the page under the scrubber's thumb: its thumbnail (drawn once the thumb
 * rests a moment, so a fast drag doesn't queue a render for every page it passes) over its number.
 */
@Composable
fun ScrubPreview(vm: AppViewModel, doc: OpenDocument, page: Int, modifier: Modifier = Modifier) {
    val c = pdfColors
    var shown by remember(doc.info.generation) { mutableStateOf<Pair<Int, ImageBitmap>?>(null) }
    val info = doc.info.pages.getOrNull(page)
    LaunchedEffect(page, doc.info.generation) {
        val eng = vm.engine ?: return@LaunchedEffect
        val p = info ?: return@LaunchedEffect
        if (shown?.first == page) return@LaunchedEffect
        delay(60)
        val scale = PREVIEW_PX / maxOf(p.width, p.height).coerceAtLeast(1f)
        val img = runCatching {
            withContext(eng.thumbnails) { eng.session.renderPage(doc.id, page.toUInt(), scale, null).toBitmap().asImageBitmap() }
        }.getOrNull()
        if (img != null) shown = page to img
    }
    // the card keeps the page's proportions, and holds the last picture until the new one arrives
    val aspect = info?.let { it.width / it.height.coerceAtLeast(1f) }?.coerceIn(0.5f, 2f) ?: 0.75f
    val h = 128.dp
    val w = (h.value * aspect).coerceIn(72f, 176f).dp
    Column(
        modifier.floatingSurface(PdfShapes.Medium, alpha = GLASS_PANEL_ALPHA).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(w, h).clip(PdfShapes.Thumb).background(Color.White), contentAlignment = Alignment.Center) {
            shown?.second?.let { Image(it, null, Modifier.size(w, h), contentScale = ContentScale.Fit) }
        }
        Text("${page + 1}", color = c.text, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
    }
}
