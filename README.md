<p align="center">
  <img src="assets/app-icon/pdfcraft-1024.png" alt="PdfCraft's app icon" width="128">
</p>

<h1 align="center">PdfCraft for Android</h1>

<p align="center">
  <b>PdfCraft-android is an open-source reimplementation of the Adobe Acrobat Reader Android app, created using the same Rust engine, <a href="https://github.com/storytold/pdfcraft">PdfCraft</a>, as the desktop app, and built with a Kotlin/Jetpack Compose UI framework.</b><br>
  Many credits to the original maintainers, <a href="https://github.com/storytold">storytold</a>.
</p>

<p align="center">
  <a href="https://github.com/artcraft-android/pdfcraft-android/releases"><img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" height="60"></a>
  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/artcraft-android/pdfcraft-android"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60"></a>
</p>

<p align="center">
  <img alt="Rust engine" src="https://img.shields.io/badge/engine-Rust-lightblue?style=flat&logo=rust&logoColor=white">
  <img alt="Kotlin and Jetpack Compose" src="https://img.shields.io/badge/UI-Kotlin%20%C2%B7%20Jetpack%20Compose-blue?style=flat">
  <img alt="Minimum: Android 10 (API 29)" src="https://img.shields.io/badge/min-Android%2010%20%28API%2029%29-blue?style=flat">
  <img alt="Target: Android 16 (API 36)" src="https://img.shields.io/badge/target-Android%2016%20%28API%2036%29-blue?style=flat">
  <img alt="License: MIT OR Apache-2.0" src="https://img.shields.io/badge/license-MIT%20OR%20Apache--2.0-blue?style=flat">
  <img alt="Status: beta" src="https://img.shields.io/badge/status-beta-lightblue?style=flat">
</p>

<p align="center">
  <a href="#reading">Reading</a> ·
  <a href="#comments">Comments</a> ·
  <a href="#forms--fill--sign">Forms &amp; Fill &amp; Sign</a> ·
  <a href="#edit-pdf">Edit PDF</a> ·
  <a href="#read-aloud">Read aloud</a> ·
  <a href="#pages--tools">Pages &amp; tools</a> ·
  <a href="#home">Home</a> ·
  <a href="#light-theme">Light theme</a> ·
  <a href="#planned-features">Planned features</a> ·
  <a href="#building-instructions">Building</a>
</p>

<br>

<p align="center">
  <img src="docs/images/viewer.png" alt="The PdfCraft Showcase open on its cover: a deep blue and orange gradient with the title Showcase and 'A tour of the page, glyph by glyph.', the page pill at 1, the top bar with back, the Read aloud switch, Find, Share and More, and the quick toolbar: Edit PDF, Comment, Highlight, Draw, Fill & Sign, Export PDF and More tools" width="100%">
  <br>
  <sub>The PdfCraft Showcase, a 13-page specimen PDF, open on its cover.</sub>
</p>

<br>

## Reading

<p align="center">
  <img src="docs/images/scripts.png" alt="The Scripts of the World page with the bars hidden: Arabic, Hebrew, Devanagari, Thai, Greek, Cyrillic, Chinese, Korean, IPA, Armenian, Georgian and Tamil samples, vertical Japanese in the margin, mixed-direction text and colour emoji, with the Mathematics page below" width="100%">
  <br>
  <sub>Twelve writing systems, vertical Japanese and colour emoji, with the bars hidden by a tap.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/reading-mode.png" alt="Reading Mode on the foreword: the heading Why a specimen? and its paragraphs reflowed in large type to the width of the screen, under the viewer's top bar" width="100%">
  <br>
  <sub>Reading Mode: the page's text reflowed to the screen. Pinch or double-tap for bigger text.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/night-mode.png" alt="Night Mode: the Scripts of the World and Mathematics pages shown as light text on black" width="100%">
  <br>
  <sub>Night Mode, from More › View settings.</sub>
</p>

<br>

## Comments

<p align="center">
  <img src="docs/images/comment-panel.png" alt="The Review and Markup page with a sticky note selected and ringed in blue, and the comment panel below it: 3 of 17 with arrows, a thread of two comments by PdfCraft Reviewer and Layout Editor, and a Reply box" width="100%">
  <br>
  <sub>Tap a comment: its thread in a panel, ‹ 3 of 17 › to step through the others, and Reply.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/comment-list.png" alt="The full-screen comment list titled 17 comment threads, grouped by page: notes, a highlighted phrase with its comment, a threaded note with a reply and a text box, each with Reply" width="100%">
  <br>
  <sub>The Comment List: every thread by page, with a picture of the marked text.</sub>
</p>

<br>

## Forms & Fill & Sign

<p align="center">
  <img src="docs/images/forms.png" alt="The Interactive Form page: text fields, a date, an amount, a multiline comment, checkboxes, radio buttons, a combo box, a multi-select list, a push button and a signature field, every fillable field tinted blue" width="100%">
  <br>
  <sub>Every fillable field tinted blue: tap one to fill it in.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/fill-sign.png" alt="Fill and Sign on the Interactive Form page: a drawn signature placed in the signature field, Done at the top left with undo and redo, and the Sign tool in the bar below" width="100%">
  <br>
  <sub>Fill & Sign: a signature drawn once, saved on the phone and placed with a tap.</sub>
</p>

<br>

## Edit PDF

<p align="center">
  <img src="docs/images/edit-pdf.png" alt="Edit PDF on the Review and Markup page: a paragraph selected in a blue box with round handles, a menu with Edit text, Select all and Delete above it, and the formatting bar below: bold, italic, underline, size, font, colour and alignment" width="100%">
  <br>
  <sub>Edit PDF: tap a paragraph to edit its text, change its style, move it or rewrap it.</sub>
</p>

<br>

## Read aloud

<p align="center">
  <img src="docs/images/read-aloud.png" alt="Read aloud playing on the Code and Images page: the running header washed yellow with the current word stronger, the Read aloud switch turned on, and the player at the bottom with a progress bar, back 15 seconds, pause, forward 15 seconds and 1.0x" width="100%">
  <br>
  <sub>Read aloud with the phone's voice: the words are marked as they're read.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/read-aloud-player.png" alt="The full Read aloud player: a picture of the page being read, the file name pdfcraft-showcase, a progress bar with 20:20 and -5:03, back 15 seconds, pause and forward 15 seconds, then Voice and 1.0x" width="100%">
  <br>
  <sub>The full player: the page, the time left, Voice and speed.</sub>
</p>

<br>

## Pages & tools

<p align="center">
  <img src="docs/images/pages.png" alt="The Pages grid: two columns of page thumbnails numbered 1 to 8, the current page outlined in blue, with Edit PDF and Crop pages in the bar" width="100%">
  <br>
  <sub>Pages: every page at a glance; hold and drag to move one.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/bookmarks.png" alt="Bookmarks and Table of Contents over the cover: the Contents tab open on the document's outline, from Contents and Why a specimen? to Review and Markup and Interactive Form" width="100%">
  <br>
  <sub>Bookmarks & Table of Contents: the document's outline, and bookmarks of your own.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/more-tools.png" alt="The More tools sheet over the Scripts of the World page: Combine files, Compress PDF, Set password, Organize pages, Crop pages, Recognize text, Create PDF, Text, Scan and Read aloud, then Customize toolbar" width="100%">
  <br>
  <sub>More tools, and Customize toolbar to choose what the quick toolbar holds.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/compress.png" alt="Compress PDF: the file card for pdfcraft-showcase, PDF, 2.8 MB, then High, Medium and Low compression with Medium chosen, and the Compress button" width="100%">
  <br>
  <sub>Compress PDF, one of the tools that make a new file: compress, combine, set password, create, export and scan.</sub>
</p>

<br>

## Home

<p align="center">
  <img src="docs/images/home.png" alt="Home: Welcome back, Start with a tool with Open file, Edit PDF, Read aloud, Fill & Sign, Create PDF and Organize pages, then Recent, Starred and On device with application and pdfcraft-showcase listed, the add button, and the Home, Files, Tools and Search tabs" width="100%">
  <br>
  <sub>Home: the tools, then Recent, Starred and On device.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/tools.png" alt="The Tools tab: All, Read, Create, Edit and Convert, and cards for Fill & Sign, Add comment, Read aloud, Create PDF, New scan, Combine files, Organize pages, Crop pages, Edit PDF, Compress PDF, Export PDF and Set password" width="100%">
  <br>
  <sub>The Tools tab: every tool, by what it does.</sub>
</p>

<br>

## Light theme

<p align="center">
  <img src="docs/images/light-viewer.png" alt="The viewer in the light theme on the Scripts of the World page, with the white top bar and quick toolbar" width="100%">
  <br>
  <sub>The app follows the phone's light or dark theme.</sub>
</p>

<br>

<p align="center">
  <img src="docs/images/light-home.png" alt="Home in the light theme: the tools on pale tiles, Recent with application and pdfcraft-showcase, and the tabs" width="100%">
  <br>
  <sub>Home in the light theme.</sub>
</p>

<br>

## Planned features

PdfCraft for Android is in beta.

| Area | Status |
|---|---|
| Open PDFs from files, other apps and Recent; Starred, On device, Files and Search | ✅ |
| Reading: continuous and single page, Reading Mode, Night Mode, find, text selection, links, bookmarks and contents, attachments | ✅ |
| Read aloud with the phone's voice, the words marked as they're read, and a full player | ✅ |
| Comments: notes, voice comments, highlight, underline, strikethrough, drawing (snap to shape), text; the Comment List and threads | ✅ |
| Forms, and Fill & Sign: text, marks and a drawn or imported signature | ✅ |
| Edit PDF: edit text, add text and images, move, resize and turn pictures | ✅ · cropping a picture ⬜ |
| Organize and crop pages | ✅ |
| Compress, combine, set password, create PDF (images, text, office documents), scan | ✅ |
| Export to Word, RTF, Excel, PowerPoint, JPEG and PNG | ✅ · Word 97 ⬜ |
| Recognize text | ✅ (the Latin alphabet without accents) |
| A customizable quick toolbar, upright and sideways layouts, light and dark themes | ✅ |
| AI assistant, cloud storage, sharing for review | 🚫 PdfCraft is a local app |

<sub>✅ works today · 🚧 in progress · ⬜ not started · 🚫 not planned</sub>

<br>

## Building Instructions
*One Gradle command builds the whole app: it compiles the Rust engine with [cargo-ndk](https://github.com/bbqsrc/cargo-ndk) and generates the Kotlin bindings.*

* Clone this repository
* Make sure you have at least 3 GB of free disk space
* optionally run `apps/pdfcraft-android/fetch-models.sh` for Recognize text (12 MB of models; without them the app builds and the tool says it isn't installed)
* enter `apps/pdfcraft-android/android` and run `./gradlew assembleRelease`, or open that folder in Android Studio and press Run.

Build time dependencies:

<details>
  <summary>required tools</summary>

  ```
      rustup (it installs the Rust release and Android targets rust-toolchain.toml names)
      cargo install cargo-ndk --version 4.1.2 --locked
      Android SDK (API 37) and NDK 29.0.14206865
      JDK 17 or newer
  ```
</details>

*Builds are only tested on Windows 11.*

<br>

## License

[MIT](LICENSE-MIT) OR [Apache-2.0](LICENSE-APACHE), at your option. PdfCraft for Android is not affiliated with Adobe;
Acrobat is a trademark of Adobe Inc.

<br>

## Misc info for myself

Try to use [semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index)
messages. Use `feat:` / `fix:` / `chore:` / `docs:` / etc.
