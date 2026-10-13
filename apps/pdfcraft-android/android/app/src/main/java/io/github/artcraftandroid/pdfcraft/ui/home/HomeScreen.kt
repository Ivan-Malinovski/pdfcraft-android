package io.github.artcraftandroid.pdfcraft.ui.home

import io.github.artcraftandroid.pdfcraft.ui.safeTopPadding
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
fun HomeRoot(vm: AppViewModel, onBrowse: () -> Unit) {
    val c = pdfColors
    // Home and its tabs sit on the reference's home background: white, or black in dark mode
    Column(Modifier.fillMaxSize().background(c.home)) {
        Box(Modifier.weight(1f)) {
            when (vm.homeTab) {
                HomeTab.Home -> HomeTabScreen(vm, onBrowse)
                HomeTab.Files -> FilesTab(vm, onBrowse)
                HomeTab.Tools -> ToolsTab(vm)
                HomeTab.Search -> SearchTab(vm)
            }
        }
        BottomNav(vm.homeTab) { vm.homeTab = it }
    }
}

/** The bottom bar: Home, Files, Tools, Search; the chosen one in the accent colour. */
@Composable
private fun BottomNav(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val c = pdfColors
    Column(Modifier.fillMaxWidth().background(c.home).drawBehind { drawLine(c.divider, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }) {
        Row(Modifier.fillMaxWidth().height(61.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            for (t in HomeTab.entries) {
                val icon = when (t) {
                    HomeTab.Home -> LucideIcons.House
                    HomeTab.Files -> LucideIcons.Folder
                    HomeTab.Tools -> LucideIcons.LayoutGrid
                    HomeTab.Search -> LucideIcons.Search
                }
                val tint = if (t == selected) c.toolAccent else c.icon
                Column(
                    Modifier.weight(1f).height(61.dp).clickable(role = Role.Tab) { onSelect(t) }.semantics { contentDescription = t.label },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(5.dp))
                    Text(t.label, color = tint, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** Home: the app's name and Settings, a welcome, the tools carousel, then Recent, Starred and On device. */
@Composable
private fun HomeTabScreen(vm: AppViewModel, onBrowse: () -> Unit) {
    val c = pdfColors
    var tab by remember { mutableStateOf(0) }
    var fabMenu by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    LaunchedEffect(tab) { if (tab == 2) vm.scanLocal() }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeTopPadding()) {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_launcher_tile), null, modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)))
                Spacer(Modifier.width(10.dp))
                Text("PdfCraft", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(c.surfaceRaised).clickable { vm.settingsOpen = true }.semantics { contentDescription = "Settings" },
                    contentAlignment = Alignment.Center,
                ) { Icon(LucideIcons.UserRound, null, tint = c.icon, modifier = Modifier.size(22.dp)) }
            }
            LazyColumn(Modifier.weight(1f)) {
                item {
                    Text(if (vm.recentList.isEmpty()) "Welcome" else "Welcome back", color = c.text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp))
                    Text("Start with a tool", color = c.text, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                    ToolsCarousel(vm, onBrowse)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                            else -> if (!vm.hasFileAccess) EmptyState(LucideIcons.Smartphone, "Files on your device", "To see the PDFs on this phone, allow access to all files in the Settings.", "Allow access") { vm.requestFileAccess() }
                            else EmptyState(LucideIcons.Smartphone, "No PDFs on your device", "PDFs you download or save will appear here.")
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
        FloatingActionButton(
            onClick = { fabMenu = true },
            containerColor = c.button,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = "Tools" },
        ) { Icon(LucideIcons.Plus, null, modifier = Modifier.size(26.dp)) }
    }
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
    if (confirmClear) {
        io.github.artcraftandroid.pdfcraft.ui.PdfDialog(
            onDismiss = { confirmClear = false }, title = "Clear recent files?",
            confirm = "Clear", onConfirm = { confirmClear = false; vm.clearRecents() }, destructive = true,
        ) { Text("The list of recent files is cleared. The files themselves stay where they are.", color = c.text, fontSize = 14.sp) }
    }
}

/** A tool that starts by choosing a PDF. */
enum class FileTool { Edit, ReadAloud, FillSign, Organize, Compress, Export, Protect, Comment, Crop, Ocr }

/** The row of tools under "Start with a tool". */
@Composable
private fun ToolsCarousel(vm: AppViewModel, onBrowse: () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CarouselTile(LucideIcons.FolderOpen, "Open file", ToolHues.crop, onBrowse)
        CarouselTile(LucideIcons.FilePenLine, "Edit PDF", ToolHues.edit) { vm.toolForFile(FileTool.Edit) }
        CarouselTile(LucideIcons.Volume2, "Read aloud", ToolHues.read) { vm.toolForFile(FileTool.ReadAloud) }
        CarouselTile(LucideIcons.Signature, "Fill & Sign", ToolHues.sign) { vm.toolForFile(FileTool.FillSign) }
        CarouselTile(LucideIcons.FilePlus2, "Create PDF", ToolHues.create) { vm.openCreate() }
        CarouselTile(LucideIcons.LayoutGrid, "Organize pages", ToolHues.organize) { vm.toolForFile(FileTool.Organize) }
        CarouselTile(LucideIcons.Combine, "Combine files", ToolHues.combine) { vm.pickFiles(io.github.artcraftandroid.pdfcraft.ui.FilePickKind.Combine) }
        CarouselTile(LucideIcons.FileArchive, "Compress PDF", ToolHues.compress) { vm.toolForFile(FileTool.Compress) }
        CarouselTile(LucideIcons.FileOutput, "Export PDF", ToolHues.export) { vm.toolForFile(FileTool.Export) }
        CarouselTile(LucideIcons.FileLock2, "Set password", ToolHues.protect) { vm.toolForFile(FileTool.Protect) }
    }
}

@Composable
private fun CarouselTile(icon: ImageVector, label: String, hue: Color, onClick: () -> Unit) {
    val c = pdfColors
    // 64 dp tiles 16 dp apart, the tool's hue faint behind its icon, an 11 sp name under it
    Column(Modifier.width(64.dp).clickable(role = Role.Button, onClick = onClick).padding(vertical = 8.dp).semantics { contentDescription = label }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(5.dp)).background(tint(hue, c.dark)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = hue, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = c.text, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp, overflow = TextOverflow.Ellipsis)
    }
}

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

/** A document's first page, or the PDF badge while it draws (or when it can't). */
@Composable
fun FileThumb(vm: AppViewModel, path: String?, width: Int, height: Int) {
    var img by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        if (path != null) img = withContext(Dispatchers.IO) { vm.thumbnails.of(path)?.asImageBitmap() }
    }
    val c = pdfColors
    Box(Modifier.size(width.dp, height.dp).clip(RoundedCornerShape(4.dp)).background(if (img != null) Color.White else c.surfaceRaised).border(1.dp, c.divider, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
        val i = img
        if (i != null) Image(i, null, modifier = Modifier.size(width.dp, height.dp), contentScale = ContentScale.Crop) else PdfBadge((width * 0.55f).toInt())
    }
}

@Composable
fun FileListRow(vm: AppViewModel, r: FileRow, recent: Boolean = false) {
    val c = pdfColors
    Row(Modifier.fillMaxWidth().clickable(onClick = r.open).padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) { FileThumb(vm, r.thumbPath, 36, 42) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.name.removeSuffix(".pdf").removeSuffix(".PDF"), color = c.text, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (r.starred) {
                    Spacer(Modifier.width(6.dp))
                    Icon(LucideIcons.Star, "Starred", tint = Color(0xFFF5B800), modifier = Modifier.size(14.dp))
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
        BarIcon(LucideIcons.EllipsisVertical, "More options for ${r.name}", { vm.fileSheet = r to recent })
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

