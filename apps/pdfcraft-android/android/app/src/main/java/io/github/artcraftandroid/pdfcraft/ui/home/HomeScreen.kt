package io.github.artcraftandroid.pdfcraft.ui.home

import io.github.artcraftandroid.pdfcraft.ui.FloatEdge
import io.github.artcraftandroid.pdfcraft.ui.FloatSide
import io.github.artcraftandroid.pdfcraft.ui.floatingSurface
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import io.github.artcraftandroid.pdfcraft.ui.PdfShapes
import io.github.artcraftandroid.pdfcraft.ui.GlassSource
import io.github.artcraftandroid.pdfcraft.ui.glassSource
import io.github.artcraftandroid.pdfcraft.ui.safeTopPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.artcraftandroid.pdfcraft.R
import io.github.artcraftandroid.pdfcraft.ui.AppViewModel
import io.github.artcraftandroid.pdfcraft.ui.BarIcon
import io.github.artcraftandroid.pdfcraft.ui.LucideIcons
import io.github.artcraftandroid.pdfcraft.ui.PdfBadge
import io.github.artcraftandroid.pdfcraft.ui.PdfSheet
import io.github.artcraftandroid.pdfcraft.ui.SheetDivider
import io.github.artcraftandroid.pdfcraft.ui.SheetRow
import io.github.artcraftandroid.pdfcraft.ui.pdfColors
import io.github.artcraftandroid.pdfcraft.ui.viewer.ToolHues
import io.github.artcraftandroid.pdfcraft.ui.viewer.formatSize
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The tabs along the bottom of the app. */
enum class HomeTab(val label: String) { Home("Home"), Files("Files"), Tools("Tools"), Search("Search") }

/** A file as the lists show it, wherever it comes from (recent, starred, on this device). */
data class FileRow(
    val key: String,
    val name: String,
    /** A local path to draw its thumbnail from, if there is one. */
    val thumbPath: String?,
    val location: String,
    val date: Long,
    val size: Long,
    val starred: Boolean,
    val open: () -> Unit,
)

/** The app outside the viewer: the current tab's screen over the bottom bar. */
@Composable
fun HomeRoot(vm: AppViewModel, onBrowse: () -> Unit, glass: GlassSource) {
    val c = pdfColors
    var fabMenu by remember { mutableStateOf(false) }
    // Home and its tabs sit on the reference's home background: white, or black in dark mode
    val reserve = HomeNavHeight + FloatEdge * 2 + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize()) {
        // the tabs run under the floating nav; each leaves [reserve] at the foot of its list
        androidx.compose.runtime.CompositionLocalProvider(LocalHomeNavReserve provides reserve) {
            Box(Modifier.fillMaxSize().background(c.home).glassSource(glass)) {
                when (vm.homeTab) {
                    HomeTab.Home -> HomeTabScreen(vm, onBrowse) { fabMenu = true }
                    HomeTab.Files -> FilesTab(vm, onBrowse)
                    HomeTab.Tools -> ToolsTab(vm)
                    HomeTab.Search -> SearchTab(vm)
                }
            }
        }
        BottomNav(vm.homeTab, Modifier.align(Alignment.BottomCenter).padding(bottom = FloatEdge).navigationBarsPadding()) { vm.homeTab = it }
        if (fabMenu) {
            PdfSheet(onDismiss = { fabMenu = false }, dim = true) {
                SheetRow(LucideIcons.FolderOpen, "Open file", { fabMenu = false; onBrowse() })
                SheetRow(LucideIcons.Camera, "New scan", { fabMenu = false; vm.startScan() })
                SheetDivider()
                SheetRow(LucideIcons.FilePenLine, "Edit PDF", { fabMenu = false; vm.toolForFile(FileTool.Edit) })
                SheetRow(LucideIcons.FilePlus2, "Create PDF", { fabMenu = false; vm.openCreate() })
                SheetRow(LucideIcons.Combine, "Combine files", { fabMenu = false; vm.pickFiles(io.github.artcraftandroid.pdfcraft.ui.FilePickKind.Combine) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** How much room the lists leave at their foot for the floating nav (see [HomeNavHeight]). */
val LocalHomeNavReserve = androidx.compose.runtime.compositionLocalOf { 0.dp }

/** The nav's height; the lists leave this much (plus its margins) clear at their foot. */
val HomeNavHeight = 64.dp

/**
 * The three main destinations as a floating glass pill, as wide as its items (the viewer's bottom bar
 * is the same pill). Search is reached from Home.
 */
@Composable
private fun BottomNav(selected: HomeTab, modifier: Modifier = Modifier, onSelect: (HomeTab) -> Unit) {
    val c = pdfColors
    val accent = homeAccent(c.dark)
    Row(
        modifier.height(HomeNavHeight).floatingSurface(PdfShapes.Pill).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        for (t in listOf(HomeTab.Home, HomeTab.Files, HomeTab.Tools)) {
            val icon = when (t) {
                HomeTab.Home -> LucideIcons.House
                HomeTab.Files -> LucideIcons.Folder
                HomeTab.Tools -> LucideIcons.LayoutGrid
                HomeTab.Search -> LucideIcons.Search
            }
            val active = t == selected || (t == HomeTab.Home && selected == HomeTab.Search)
            val iconTint = if (active) accent else c.textSecondary
            Column(
                Modifier.width(86.dp).height(52.dp).clip(PdfShapes.Pill)
                    .background(if (active) tint(accent, c.dark) else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(t) }.semantics { contentDescription = t.label },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(21.dp))
                Text(t.label, color = iconTint, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Home: the app's name and Settings, a welcome, the tools carousel, then Recent, Starred and On device. */
@Composable
private fun HomeTabScreen(vm: AppViewModel, onBrowse: () -> Unit, onOpenFab: () -> Unit) {
    val c = pdfColors
    // -1 is the overview; the old full lists remain reachable from each section's See all.
    var tab by remember { mutableStateOf(-1) }
    var menu by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmDeleteSelected by remember { mutableStateOf(false) }
    LaunchedEffect(tab) { if (tab == 2 || tab == -1) vm.scanLocal(); vm.selectedFiles.clear() }
    androidx.activity.compose.BackHandler(enabled = vm.selectedFiles.isNotEmpty() || tab >= 0) {
        if (vm.selectedFiles.isNotEmpty()) vm.selectedFiles.clear() else tab = -1
    }
    if (confirmDeleteSelected) {
        val rows = vm.selectedFiles.values.toList()
        io.github.artcraftandroid.pdfcraft.ui.PdfDialog(
            onDismiss = { confirmDeleteSelected = false },
            title = if (rows.size == 1) "Delete 1 file?" else "Delete ${rows.size} files?",
            confirm = "Delete", destructive = true,
            onConfirm = { confirmDeleteSelected = false; rows.forEach(vm::deleteFile); vm.selectedFiles.clear() },
        ) { Text("They are removed from this phone.", color = c.text, fontSize = 14.sp) }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(if (tab == -1) homeCanvas(c.dark) else c.home).safeTopPadding()) {
            if (vm.selectedFiles.isNotEmpty()) SelectionBar(vm, recent = tab == 0, onDelete = { confirmDeleteSelected = true }) else
            Row(Modifier.fillMaxWidth().height(if (tab == -1) 74.dp else 56.dp).padding(start = if (tab == -1) 23.dp else 16.dp, end = if (tab == -1) 23.dp else 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(if (tab == -1) 39.dp else 28.dp)
                    .clip(RoundedCornerShape(12.dp)).background(homeAccent(c.dark)), contentAlignment = Alignment.Center) {
                    Text("P", color = Color.White, fontSize = if (tab == -1) 23.sp else 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(13.dp))
                Text("PDFCraft", color = if (tab == -1) homeInk(c.dark) else c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(if (tab == -1) homeIconWash(c.dark) else c.surfaceRaised)
                        .clickable { vm.settingsOpen = true }.semantics { contentDescription = "Settings" },
                    contentAlignment = Alignment.Center,
                ) { Icon(LucideIcons.Settings, null, tint = c.icon, modifier = Modifier.size(22.dp)) }
            }
            if (tab == -1) {
                val recents = vm.recentRows()
                val last = vm.recentList.firstOrNull()
                // each file shows once: the Continue card's file is left out below, and Recent skips what Starred shows
                val starred = vm.starredRows().filter { it.key != recents.firstOrNull()?.key }.take(2)
                val starredKeys = starred.map { it.key }.toSet()
                val recentShown = recents.drop(1).filter { it.key !in starredKeys }.take(3)
                val newFiles = vm.newLocalRows()
                LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = LocalHomeNavReserve.current + 12.dp)) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 6.dp)
                                .clip(PdfShapes.Medium).background(c.surface)
                                .border(1.dp, homeLine(c.dark), PdfShapes.Medium)
                                .clickable(role = Role.Button) { vm.homeTab = HomeTab.Search }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .semantics { contentDescription = "Search your PDFs" },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(LucideIcons.Search, null, tint = homeMuted(c.dark), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Search your PDFs", color = homeMuted(c.dark), fontSize = 14.sp)
                        }
                    }
                    if (last != null && recents.isNotEmpty()) {
                        item {
                            HomeSectionHeader("Continue reading")
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 23.dp)
                                    .clip(RoundedCornerShape(22.dp)).background(homeContinueBrush(c.dark))
                                    .border(1.dp, homeContinueLine(c.dark), RoundedCornerShape(22.dp))
                                    .clickable(role = Role.Button, onClick = recents.first().open)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FileThumb(vm, recents.first().thumbPath, 72, 94)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    val pages = last.pages.coerceAtLeast(1)
                                    Text(last.name.removeSuffix(".pdf").removeSuffix(".PDF"), color = homeInk(c.dark), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(5.dp))
                                    Text("Page ${last.lastPage + 1} of $pages · ${(((last.lastPage + 1) * 100) / pages).coerceIn(0, 100)}%", color = homeInk(c.dark), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(agoLabel(last.opened), color = homeMuted(c.dark), fontSize = 12.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(LucideIcons.ChevronRight, null, tint = homeAccent(c.dark), modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    item {
                        HomeSectionHeader("Start something")
                        Row(Modifier.fillMaxWidth().padding(horizontal = 23.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HomeAction(LucideIcons.FolderOpen, "Open file", Color(0xFF5179BD), Modifier.weight(1f), onBrowse)
                            HomeAction(LucideIcons.Camera, "Scan", Color(0xFFD7734F), Modifier.weight(1f)) { vm.startScan() }
                            HomeAction(LucideIcons.FilePlus2, "Create PDF", Color(0xFF8664C1), Modifier.weight(1f)) { vm.openCreate() }
                        }
                        // the tools people reach for most; the rest are in the Tools tab
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 23.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HomeChip(LucideIcons.FileArchive, "Compress") { vm.toolForFile(FileTool.Compress) }
                            HomeChip(LucideIcons.Signature, "Fill & Sign") { vm.toolForFile(FileTool.FillSign) }
                            HomeChip(LucideIcons.Combine, "Combine") { vm.pickFiles(io.github.artcraftandroid.pdfcraft.ui.FilePickKind.Combine) }
                            HomeChip(LucideIcons.FilePenLine, "Edit") { vm.toolForFile(FileTool.Edit) }
                        }
                    }
                    if (newFiles.isNotEmpty()) item {
                        val folders = newFiles.map { it.location }.distinct()
                        HomeBanner(
                            LucideIcons.FileDown,
                            if (newFiles.size == 1) "New PDF on this phone" else "${newFiles.size} new PDFs on this phone",
                            if (newFiles.size == 1) newFiles.first().name.removeSuffix(".pdf").removeSuffix(".PDF") else if (folders.size == 1) "In ${folders.first()}" else "In ${folders.size} folders",
                            "View", { vm.markLocalSeen(); tab = 2 }, { vm.markLocalSeen() },
                        )
                    } else if (!vm.hasFileAccess && !vm.folderPromptDismissed) item {
                        HomeBanner(
                            LucideIcons.FolderPlus, "See PDFs on this phone", "Pick Documents or Download",
                            "Add", { vm.requestFileAccess() }, { vm.dismissFolderPrompt() },
                        )
                    }
                    if (starred.isNotEmpty()) {
                        item { HomeSectionHeader("Starred", onSeeAll = { tab = 1 }) }
                        items(starred, key = { "starred:${it.key}" }) { r -> HomeFileRow(vm, r) }
                    }
                    item { HomeSectionHeader("Recent files", onSeeAll = { tab = 0 }) }
                    if (recentShown.isEmpty()) item {
                        Text(if (recents.isEmpty()) "Files you open will appear here." else "Other files you open will appear here.", color = homeMuted(c.dark), fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 10.dp))
                    }
                    else items(recentShown, key = { "recent:${it.key}" }) { r -> HomeFileRow(vm, r) }
                }
            } else LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = LocalHomeNavReserve.current)) {
                item {
                    Row(Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BarIcon(LucideIcons.ArrowLeft, "Back to Home", { tab = -1 })
                        listOf("Recent", "Starred", "On device").forEachIndexed { i, label ->
                            Column(Modifier.clickable(role = Role.Tab) { tab = i }.padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, color = if (tab == i) c.text else c.textSecondary, fontSize = 15.sp, fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(vertical = 10.dp))
                                Box(Modifier.width(48.dp).height(2.dp).background(if (tab == i) c.text else Color.Transparent))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Box {
                            BarIcon(LucideIcons.EllipsisVertical, "More options", { menu = true })
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = c.surface) {
                                DropdownMenuItem(text = { Text(if (vm.gridView) "List view" else "Grid view") }, onClick = { menu = false; vm.gridView = !vm.gridView })
                                if (tab == 0 && vm.recentList.isNotEmpty()) DropdownMenuItem(text = { Text("Clear recent") }, onClick = { menu = false; confirmClear = true })
                                if (tab == 1) {
                                    DropdownMenuItem(text = { Text("Sort by name") }, onClick = { menu = false; vm.starSort = 1 })
                                    DropdownMenuItem(text = { Text("Sort by date") }, onClick = { menu = false; vm.starSort = 0 })
                                }
                            }
                        }
                    }
                    SheetDivider(inset = 0.dp)
                }
                val rows = when (tab) {
                    0 -> vm.recentRows()
                    1 -> vm.starredRows()
                    else -> vm.localRows().take(10)
                }
                if (rows.isEmpty()) {
                    item {
                        when (tab) {
                            0 -> EmptyState(LucideIcons.Clock, "No recent files", "Files you open will appear here.", "Open file", onBrowse)
                            1 -> EmptyState(LucideIcons.Star, "No starred files", "Star a file from its menu to find it here quickly.")
                            else -> if (!vm.hasFileAccess) EmptyState(LucideIcons.Smartphone, "Files on your device", "Choose folders to list PDFs from, such as Documents or a folder inside Download. Files you open from anywhere stay in Recent.", "Add a folder") { vm.requestFileAccess() }
                            else EmptyState(LucideIcons.Smartphone, "No PDFs in your folders", "PDFs in the folders you added will appear here.", "Add a folder") { vm.requestFileAccess() }
                        }
                    }
                } else if (vm.gridView) {
                    items(rows.chunked(2)) { pair ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { r -> FileGridCell(vm, r, Modifier.weight(1f), recent = tab == 0) }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                } else {
                    items(rows, key = { it.key }) { r -> FileListRow(vm, r, recent = tab == 0) }
                }
                if (tab == 2 && vm.localRows().size > 10) {
                    item {
                        Text("Other files on this phone", color = c.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().clickable { vm.homeTab = HomeTab.Files; vm.filesShowLocal = true }.padding(16.dp))
                    }
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
        if (tab >= 0) FloatingActionButton(
            onClick = onOpenFab,
            containerColor = c.button,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + LocalHomeNavReserve.current).semantics { contentDescription = "Tools" },
        ) { Icon(LucideIcons.Plus, null, modifier = Modifier.size(26.dp)) }
    }
    if (confirmClear) {
        io.github.artcraftandroid.pdfcraft.ui.PdfDialog(
            onDismiss = { confirmClear = false }, title = "Clear recent files?",
            confirm = "Clear", onConfirm = { confirmClear = false; vm.clearRecents() }, destructive = true,
        ) { Text("The list of recent files is cleared. The files themselves stay where they are.", color = c.text, fontSize = 14.sp) }
    }
}

@Composable
private fun HomeSectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    val c = pdfColors
    Row(
        Modifier.fillMaxWidth().padding(start = 23.dp, end = 23.dp, top = 28.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = homeInk(c.dark), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (onSeeAll != null) Row(Modifier.clickable(role = Role.Button, onClick = onSeeAll).padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("See all", color = homeAccent(c.dark), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(2.dp))
            Icon(LucideIcons.ChevronRight, null, tint = homeAccent(c.dark), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun HomeAction(icon: ImageVector, label: String, hue: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = pdfColors
    Column(
        modifier.heightIn(min = 105.dp)
            .clip(RoundedCornerShape(19.dp)).background(c.surface)
            .border(1.dp, homeLine(c.dark), RoundedCornerShape(19.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(14.dp)
            .semantics { contentDescription = label },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.size(37.dp).clip(PdfShapes.Small).background(homeActionWash(hue, c.dark)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = hue, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(label, color = homeInk(c.dark), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
    }
}

@Composable
private fun HomeFileRow(vm: AppViewModel, r: FileRow) {
    val c = pdfColors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 4.dp)
            .clip(PdfShapes.Medium).background(c.surface)
            .border(1.dp, if (c.dark) Color(0xFF3A4542) else Color(0xFFE5EBE8), PdfShapes.Medium)
            .padding(start = 10.dp, end = 0.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f).clickable(role = Role.Button, onClick = r.open), verticalAlignment = Alignment.CenterVertically) {
            FileThumb(vm, r.thumbPath, 40, 49)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(r.name.removeSuffix(".pdf").removeSuffix(".PDF"), color = homeInk(c.dark), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(listOfNotNull(whenLabel(r.date), r.size.takeIf { it > 0 }?.let { formatSize(it) }).joinToString(" · "),
                    color = homeMuted(c.dark), fontSize = 11.sp, maxLines = 1)
            }
        }
        Box(Modifier.size(48.dp).clickable(role = Role.Button) { vm.setStarred(r.key, !r.starred) }
            .semantics { contentDescription = if (r.starred) "Unstar ${r.name}" else "Star ${r.name}" },
            contentAlignment = Alignment.Center) {
            StarGlyph(r.starred, Modifier.size(19.dp))
        }
        BarIcon(LucideIcons.EllipsisVertical, "More options for ${r.name}", { vm.fileSheet = r to true }, size = 48.dp)
    }
}

/** A shortcut to a tool, as a pill. */
@Composable
private fun HomeChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = pdfColors
    Row(
        Modifier.heightIn(min = 44.dp).clip(PdfShapes.Pill).background(c.surface)
            .border(1.dp, homeLine(c.dark), PdfShapes.Pill)
            .clickable(role = Role.Button, onClick = onClick).padding(start = 13.dp, end = 16.dp)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = homeAccent(c.dark), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = homeInk(c.dark), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** A line of news under the tools: what it is, what it says, its action and a way to dismiss it. */
@Composable
private fun HomeBanner(icon: ImageVector, title: String, body: String, action: String, onAction: () -> Unit, onDismiss: () -> Unit) {
    val c = pdfColors
    Row(
        Modifier.fillMaxWidth().padding(start = 23.dp, end = 23.dp, top = 18.dp)
            .clip(PdfShapes.Medium).background(homeIconWash(c.dark))
            .clickable(role = Role.Button, onClick = onAction).padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(PdfShapes.Small).background(c.surface), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = homeAccent(c.dark), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(vertical = 11.dp)) {
            Text(title, color = homeInk(c.dark), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(body, color = homeMuted(c.dark), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(action, color = homeAccent(c.dark), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
        BarIcon(LucideIcons.X, "Dismiss", onDismiss, size = 48.dp, iconSize = 18.dp, tint = homeMuted(c.dark))
    }
}

@Composable
private fun StarGlyph(filled: Boolean, modifier: Modifier = Modifier) {
    val outline = if (filled) Color(0xFFDC9B3E) else homeMuted(pdfColors.dark)
    Canvas(modifier) {
        val star = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        for (point in 0 until 10) {
            val angle = -PI / 2 + point * PI / 5
            val radius = (if (point % 2 == 0) 0.46f else 0.21f) * size.minDimension
            val x = centerX + cos(angle).toFloat() * radius
            val y = centerY + sin(angle).toFloat() * radius
            if (point == 0) star.moveTo(x, y) else star.lineTo(x, y)
        }
        star.close()
        if (filled) drawPath(star, Color(0xFFFFDC91))
        drawPath(star, outline, style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round))
    }
}

/** The accent of the app: the selected tab here, the open tool in the bottom bar of the viewer. */
fun homeAccent(dark: Boolean) = if (dark) Color(0xFF75C7B8) else Color(0xFF006F66)
private fun homeInk(dark: Boolean) = if (dark) Color(0xFFF0F4F2) else Color(0xFF172124)
private fun homeMuted(dark: Boolean) = if (dark) Color(0xFFAEBBB7) else Color(0xFF5A676A)
private fun homeCanvas(dark: Boolean) = if (dark) Color(0xFF191E1D) else Color(0xFFFBFCFB)
private fun homeLine(dark: Boolean) = if (dark) Color(0xFF404C49) else Color(0xFFDFE6E5)
private fun homeIconWash(dark: Boolean) = if (dark) Color(0xFF293633) else Color(0xFFEDF2EF)
private fun homeContinueBrush(dark: Boolean) = if (dark) Brush.linearGradient(listOf(Color(0xFF203A34), Color(0xFF1C342F)))
    else Brush.linearGradient(listOf(Color(0xFFEFFAF5), Color(0xFFE5F4EE)))
private fun homeContinueLine(dark: Boolean) = if (dark) Color(0xFF42695E) else Color(0xFFB8D9CB)
private fun homeActionWash(hue: Color, dark: Boolean) = if (dark) tint(hue, true) else when (hue) {
    Color(0xFF5179BD) -> Color(0xFFEDF3FB)
    Color(0xFFD7734F) -> Color(0xFFFFF0E8)
    else -> Color(0xFFEFEAFA)
}

/** A tool that starts by choosing a PDF. */
enum class FileTool { Edit, ReadAloud, FillSign, Organize, Compress, Export, Protect, Comment, Crop, Ocr }

fun tint(hue: Color, dark: Boolean): Color =
    if (dark) Color(hue.red * 0.22f + 0.07f, hue.green * 0.22f + 0.07f, hue.blue * 0.22f + 0.09f) else Color(hue.red * 0.12f + 0.88f, hue.green * 0.12f + 0.88f, hue.blue * 0.12f + 0.88f)

/** An empty list's picture, title, line and optional action. */
@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = pdfColors
    Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(c.surfaceRaised), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, color = c.textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Text(action, color = c.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onAction).padding(8.dp))
        }
    }
}

/** "Just now", the time if today, "Yesterday", else a medium date. */
fun whenLabel(t: Long): String {
    val now = System.currentTimeMillis()
    if (now - t < 60_000) return "Just now"
    val cal = Calendar.getInstance().apply { timeInMillis = t }
    val today = Calendar.getInstance()
    val sameDay = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    if (sameDay) return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(t))
    today.add(Calendar.DAY_OF_YEAR, -1)
    if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) return "Yesterday"
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(t))
}

/** "Just now", "19 min ago", "3 h ago", else as [whenLabel]. */
fun agoLabel(t: Long): String {
    val min = (System.currentTimeMillis() - t) / 60_000
    return when {
        min < 1 -> "Just now"
        min < 60 -> "$min min ago"
        min < 24 * 60 && Calendar.getInstance().apply { timeInMillis = t }.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) -> "${min / 60} h ago"
        else -> whenLabel(t)
    }
}

/** A document's first page, or the PDF badge while it draws (or when it can't). */
@Composable
fun FileThumb(vm: AppViewModel, path: String?, width: Int, height: Int) {
    var img by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        if (path != null) img = withContext(Dispatchers.IO) { vm.thumbnails.of(path)?.asImageBitmap() }
    }
    val c = pdfColors
    Box(Modifier.size(width.dp, height.dp).clip(PdfShapes.Thumb).background(if (img != null) Color.White else c.surfaceRaised).border(1.dp, c.divider, PdfShapes.Thumb), contentAlignment = Alignment.Center) {
        val i = img
        if (i != null) Image(i, null, modifier = Modifier.size(width.dp, height.dp), contentScale = ContentScale.Crop) else PdfBadge((width * 0.55f).toInt())
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun FileListRow(vm: AppViewModel, r: FileRow, recent: Boolean = false) {
    val c = pdfColors
    val selecting = vm.selectedFiles.isNotEmpty()
    val ticked = r.key in vm.selectedFiles
    // a long press ticks the file (several can then be shared, combined or deleted together)
    Row(
        Modifier.fillMaxWidth()
            .background(if (ticked) c.toolAccent.copy(alpha = 0.12f) else Color.Transparent)
            .combinedClickable(onClick = { if (selecting) vm.toggleSelected(r) else r.open() }, onLongClick = { vm.toggleSelected(r) })
            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) { FileThumb(vm, r.thumbPath, 36, 42) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.name.removeSuffix(".pdf").removeSuffix(".PDF"), color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (r.starred) {
                    Spacer(Modifier.width(6.dp))
                    StarGlyph(true, Modifier.size(14.dp).semantics { contentDescription = "Starred" })
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // where it is, then type, date and size: "PDF  •  Yesterday  •  2.3 MB"
                Icon(LucideIcons.Smartphone, "Stored on ${r.location}", tint = c.textTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(listOfNotNull("PDF", whenLabel(r.date), r.size.takeIf { it > 0 }?.let { formatSize(it) }).joinToString("  •  "), color = c.textTertiary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
        if (selecting) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { SelectBox(ticked) }
        } else {
            BarIcon(LucideIcons.EllipsisVertical, "More options for ${r.name}", { vm.fileSheet = r to recent })
        }
    }
}

/** A round tick for a ticked file, an empty ring for the others. */
@Composable
private fun SelectBox(ticked: Boolean) {
    val c = pdfColors
    Box(
        Modifier.size(22.dp).clip(CircleShape).background(if (ticked) c.toolAccent else Color.Transparent)
            .border(2.dp, if (ticked) c.toolAccent else c.textSecondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) { if (ticked) Icon(LucideIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
}

/** Home's bar while files are ticked: how many, Share, Combine, and the rest in a menu. */
@Composable
private fun SelectionBar(vm: AppViewModel, recent: Boolean, onDelete: () -> Unit) {
    val c = pdfColors
    val rows = vm.selectedFiles.values.toList()
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        BarIcon(LucideIcons.X, "Clear selection", { vm.selectedFiles.clear() })
        Text(if (rows.size == 1) "1 file selected" else "${rows.size} files selected", color = c.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 8.dp))
        BarIcon(LucideIcons.Share2, "Share", { vm.shareFiles(rows); vm.selectedFiles.clear() })
        if (rows.size >= 2) BarIcon(LucideIcons.Combine, "Combine files", { vm.combineSelected(rows); vm.selectedFiles.clear() })
        Box {
            BarIcon(LucideIcons.EllipsisVertical, "More options", { menu = true })
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = c.surface) {
                if (recent) DropdownMenuItem(text = { Text("Remove from recent") }, onClick = { menu = false; vm.removeRecents(rows); vm.selectedFiles.clear() })
                DropdownMenuItem(text = { Text("Delete", color = c.red) }, onClick = { menu = false; onDelete() })
            }
        }
    }
}

@Composable
private fun FileGridCell(vm: AppViewModel, r: FileRow, modifier: Modifier, recent: Boolean) {
    val c = pdfColors
    Column(modifier.clickable(onClick = r.open).padding(vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) { FileThumb(vm, r.thumbPath, 120, 156) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(r.name.removeSuffix(".pdf"), color = c.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(whenLabel(r.date), color = c.textSecondary, fontSize = 12.sp)
            }
            BarIcon(LucideIcons.EllipsisVertical, "More options for ${r.name}", { vm.fileSheet = r to recent }, size = 40.dp)
        }
    }
}

/**
 * A file's "..." sheet: its tools, then Share, Print, Open with, Star, Rename, Duplicate, Save a
 * copy, Remove from recent (Recent) and Delete.
 */
@Composable
fun FileSheet(vm: AppViewModel, r: FileRow, recent: Boolean, onDismiss: () -> Unit) {
    val c = pdfColors
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    fun act(f: () -> Unit) {
        onDismiss()
        f()
    }
    PdfSheet(onDismiss = onDismiss, maxHeightFraction = 0.85f) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            FileThumb(vm, r.thumbPath, 32, 42)
            Spacer(Modifier.width(14.dp))
            Text(r.name.removeSuffix(".pdf"), color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        SheetDivider()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SheetRow(LucideIcons.FilePenLine, "Edit PDF", { act { vm.openFor(r, FileTool.Edit) } })
            SheetRow(LucideIcons.Volume2, "Read aloud", { act { vm.openFor(r, FileTool.ReadAloud) } })
            SheetRow(LucideIcons.FileOutput, "Export as...", { act { vm.openFor(r, FileTool.Export) } })
            SheetRow(LucideIcons.FileArchive, "Compress PDF", { act { vm.openFor(r, FileTool.Compress) } })
            SheetRow(LucideIcons.FileLock2, "Set password", { act { vm.openFor(r, FileTool.Protect) } })
            SheetDivider()
            SheetRow(LucideIcons.Share2, "Share", { act { vm.shareFile(r) } })
            SheetRow(LucideIcons.Printer, "Print", { act { vm.printFile(r) } })
            SheetRow(LucideIcons.ExternalLink, "Open with", { act { vm.openWith(r) } })
            SheetRow(LucideIcons.Star, if (r.starred) "Unstar" else "Star", { act { vm.setStarred(r.key, !r.starred) } })
            SheetRow(LucideIcons.Pencil, "Rename", { renaming = true })
            SheetRow(LucideIcons.Copy, "Duplicate", { act { vm.duplicateFile(r) } })
            SheetRow(LucideIcons.FileDown, "Save a copy", { act { vm.saveFileCopy(r) } })
            if (recent) SheetRow(LucideIcons.X, "Remove from recent", { act { vm.removeRecent(r.key) } })
            SheetRow(LucideIcons.Trash2, "Delete", { deleting = true }, tint = c.red)
            Spacer(Modifier.height(8.dp))
        }
    }
    if (renaming) {
        var name by remember { mutableStateOf(r.name.removeSuffix(".pdf")) }
        io.github.artcraftandroid.pdfcraft.ui.PdfDialog(
            onDismiss = { renaming = false }, title = "Rename",
            confirm = "Done", onConfirm = { renaming = false; onDismiss(); vm.renameFile(r, name) }, confirmEnabled = name.isNotBlank() && !name.startsWith("."),
        ) { io.github.artcraftandroid.pdfcraft.ui.PdfField(name, { name = it }, "Enter name") }
    }
    if (deleting) {
        io.github.artcraftandroid.pdfcraft.ui.PdfDialog(
            onDismiss = { deleting = false }, title = "Delete file?",
            confirm = "Delete", onConfirm = { deleting = false; onDismiss(); vm.deleteFile(r) }, destructive = true,
        ) { Text("Are you sure you want to permanently delete '${r.name}'?", color = c.text, fontSize = 14.sp) }
    }
}

