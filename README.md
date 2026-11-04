# TeamUp — Skill-Based Team Formation Platform
## Landing Page Desktop Application (Java2D / AWT)

Built in strict compliance with the **TeamUp Landing Page – Product Requirements Document (PRD v1.0, Dribbble Edition)**.
*Team:* Sadia Rimi, Sanjida Mostak, Md. Shakib Akando

---

### 1. Technology Constraints & Architecture
- **Pure Core Java**: Rendered entirely with Java2D (`java.awt.Frame`, `java.awt.Canvas`, double buffering `BufferStrategy`, and `Graphics2D`).
- **Zero Third-Party Dependencies**: No Maven, Gradle, Spring, JavaFX, or Swing (`javax.swing`).
- **Zero Web Technologies**: No HTML, CSS, JavaScript, or browser runtimes.
- **Java 8 Bytecode Compatible**: Compiled with `--release 8 -d .` (class file major version 52) to run smoothly on any JRE 8+ and JDK 17/21+.
- **Elastic Physics Engine (`Elastic.java`)**: Custom physical spring solver (stiffness and damping) driving all hover, tilt, and magnetic motion.
- **Persistence**: Synchronized, thread-safe lead validation with CSV injection guard writing to `data/leads.csv`.

---

### 2. Dribbble-Style Design & Motion Specs (PRD Section 5 & Motion Spec)

| # | Effect | Behavior | Java2D Technique |
| :-: | :--- | :--- | :--- |
| **1** | **Page-Load Intro** | Logo scales in, sticky nav slides down, hero words rise one by one | Time-based tween, `AlphaComposite` |
| **2** | **Staggered Headline** | Words rise 40 px and fade in, 80 ms apart, ease-out | Per-word delay with `easeOutCubic` |
| **3** | **Self-Drawing Underline** | A pink squiggle under the key word *"teammates"* draws itself | Multi-curve `Path2D` with partial stroke clip |
| **4** | **Cursor Glow** | Soft pink glow follows the mouse with lag | `RadialGradientPaint`, `lerp 0.12` per frame |
| **5** | **Parallax Masonry** | 3 member-card columns move at different speeds; the middle one the opposite way | Column parallax offset = `scrollY × factor` |
| **6** | **Card Tilt and Lift** | On hover the card tilts toward cursor, grows to 103%, shadow deepens | `AffineTransform` shear & scale with elastic return |
| **7** | **Hover Overlay** | Bottom gradient fades in with candidate name, *"Invite"* button, and popping heart | `AlphaComposite` with scale bounce |
| **8** | **Magnetic Buttons** | Button leans up to 8 px toward cursor within 80 px | Distance check with elastic offset |
| **9** | **Skill Marquee** | Skill pills scroll sideways forever, slow down on hover | Continuous time-based x offset with modulo wrap |
| **10** | **Scroll Reveal & Progress** | Section children fade and rise with 60 ms stagger; thin pink progress bar at the top | Viewport test + top progress bar |
| **11** | **Micro-Animations in Features** | Chips snap to profile, stars fill one by one, chat bubbles, shaking bell, closing lock | Looping tweens per pastel card |
| **12** | **Count-Up Numbers** | Numbers count up smoothly when entering view | Eased count-up interpolation |

---

### 3. Page Structure & Palette
- **Canvas**: Clean warm off-white (`#F8F7F4`), deep ink text (`#0D0C22`), and brand hot pink (`#EA4C89`).
- **Pastel Card Tints**: Violet (`#F3F0FF`), Mint (`#EBFBF5`), Sunny (`#FFFAEC`), Sky (`#F0F8FF`), and Pink (`#FFF0F5`).
- **Dark Contrast Band**: Rich dark navy (`#0D0C22`) on the final CTA and footer for dramatic visual contrast.
- **Hero Search Bar**: Big white pill with search text box, pink search button, and jumping skill pills (`Java`, `UI/UX`, `Python`, `React`, `MySQL`, `Figma`).
- **Generated Cover Art**: Each member card in the masonry grid features generated abstract vector artwork in pure Java2D (geometric circles, wave blobs, angled stripes).

---

### 4. Build & Run Instructions

#### In Git Bash / MINGW64:
```bash
./run.sh
```
*(Or directly using Java)*:
```bash
java Main
```

#### In Windows Command Prompt:
```cmd
run.bat
```
*(Or manual build)*:
```cmd
build.bat
```

### Keyboard Shortcuts
- **`M`**: Toggle motion animations on/off.
- **`Ctrl +` / `Ctrl -`**: Scale UI font size up or down.
- **`Ctrl 0`**: Reset UI zoom.
- **`Up` / `Down` / `PageUp` / `PageDown` / `Home` / `End`**: Smooth document scrolling.
- **`Esc`**: Close active dialog.
