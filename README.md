# TeamUp — Skill-Based Team Formation Platform

## Desktop Application (Java2D / Pure AWT)

Built in strict compliance with the **TeamUp Landing Page – Product Requirements Document (PRD v1.0, Dribbble Edition)** and interactive **Student Workspace Dashboard**.
_Team:_ Sadia Rimi, Sanjida Mostak, Md. Shakib Akando

---

### 1. Clean Project Architecture & Folder Structure

```
teamup/
├── src/                        # Clean application source code
│   ├── Main.java               # Entry point
│   ├── LandingWindow.java      # Main 60 FPS double-buffered frame & view manager
│   ├── Theme.java              # Color tokens, fonts, and dark/light system
│   ├── Anim.java               # Motion solvers (spring physics, cursor lag, ease tweens)
│   ├── Draw.java               # Vector graphics & icon rendering library
│   ├── Elastic.java            # Spring physics engine
│   ├── LeadStore.java          # Thread-safe persistent storage with CSV sanitization
│   ├── Section.java            # Section contract interface
│   ├── dashboard/              # Dedicated dashboard modules
│   │   └── StudentDashboard.java # Student workspace overview dashboard
│   ├── sections/               # Modular landing page sections
│   │   ├── Hero.java           # Eyebrow, staggered words, squiggle, masonry grid, marquee
│   │   ├── NavBar.java         # Sticky glass navigation with dashboard toggle
│   │   ├── Problem.java        # Pain-point interactive cards
│   │   ├── HowItWorks.java     # 4-step timeline with gradient badges
│   │   ├── Features.java       # Bento pastel grid with micro-animations
│   │   ├── MatchDemo.java      # Live interactive candidate re-ranking simulation
│   │   ├── Audience.java       # Role tabs (Students, Developers, Freelancers...)
│   │   ├── Compare.java        # Side-by-side feature matrix
│   │   ├── NumbersStrip.java   # Count-up metric counters
│   │   ├── Faq.java            # Animated accordion list
│   │   ├── Cta.java            # Dark contrast band with lead capture
│   │   └── Footer.java         # Dark footer band with navigation links
│   └── widgets/                # Reusable UI components
│       ├── Modal.java          # Glassmorphic Login & Registration dialog
│       ├── TextBox.java        # Custom text box with cursor & password eye toggle
│       ├── Dropdown.java       # Frosted glass popup dropdown selector
│       ├── Button.java         # Primary & ghost button components
│       ├── Chip.java           # Interactive toggle chips
│       └── Accordion.java      # Smooth expandable accordion item
├── bin/                        # Compiled bytecode output (.class files, gitignored)
├── data/                       # Local persistent data storage
│   └── leads.csv               # Registered leads
├── tests/                      # Unit and integration test scripts
│   └── TestLeadStore.java
├── build.bat / build.sh        # One-click build scripts (src -> bin, Java 8 compatible)
├── run.bat / run.sh            # One-click launcher scripts (with auto-build)
├── .gitignore                  # Git rules ignoring bin/, *.class, and jdk/
└── README.md
```

---

### 2. Technology Constraints

- **Pure Core Java**: Rendered entirely with Java2D (`java.awt.Frame`, `java.awt.Canvas`, double buffering `BufferStrategy`, and `Graphics2D`).
- **Zero Third-Party Dependencies**: No Maven, Gradle, Spring, JavaFX, or Swing (`javax.swing`).
- **Zero Web Technologies**: No HTML, CSS, JavaScript, or browser runtimes.
- **Java 8 Bytecode Compatible**: Compiled with `--release 8 -d bin` (major version 52) to run smoothly across Java 8 through Java 21+.

---

### 3. Build & Run Instructions

#### Run with Shell Scripts (Git Bash or CMD):

```bash
# Launch Landing Page
./run.sh

# Or launch directly into Student Dashboard
./run.sh --dashboard
```

#### In Windows Command Prompt:

```cmd
run.bat
run.bat --dashboard
```

#### Direct Compilation & Execution:

```bash
# Compile src -> bin
javac --release 8 -d bin src/*.java src/sections/*.java src/widgets/*.java src/dashboard/*.java

# Run from bin
java -cp bin Main
java -cp bin Main --dashboard
```

#### Hotkeys:

- Press **`F2`** anytime to instantly toggle between **Landing Page** and **Student Dashboard**!
- Press **`M`** to toggle motion / animations on and off.
- Use **`Ctrl +`** / **`Ctrl -`** / **`Ctrl 0`** to zoom interface in, out, or reset.
