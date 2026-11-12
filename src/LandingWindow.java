import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class LandingWindow extends Frame implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ComponentListener, NavBar.NavListener, StudentDashboard.DashboardListener, AdminDashboard.AdminListener {

    private final Canvas canvas;
    private final List<Section> sections = new ArrayList<>();
    private final NavBar navBar;
    private final Modal modal;
    private final StudentDashboard dashboard;
    private final AdminDashboard adminDashboard;
    public boolean showDashboard = false;
    public boolean showAdminDashboard = false;

    // Scrolling state
    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private int totalDocHeight = 0;

    // Mouse click tracking for rock-solid event delivery
    private int mousePressX = -1;
    private int mousePressY = -1;
    private long lastDashClickTime = 0;

    // Custom scrollbar dragging
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private float dragStartScroll = 0f;

    // Floating back-to-top button
    private final Elastic backTopAnim = new Elastic(0f, 220f, 22f);
    private final Elastic backTopHover = new Elastic(0f, 260f, 20f);
    private boolean backTopHovered = false;
    private int backTopX = -100, backTopY = -100;
    private static final int BACK_TOP_SIZE = 50;

    // Render loop thread
    private Thread renderThread;
    private volatile boolean running = true;

    public LandingWindow() {
        super("TeamUp — Skill-Based Team Formation Platform");

        // Window setup (pure AWT)
        setSize(1280, 800);
        setMinimumSize(new Dimension(360, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Single custom Canvas
        canvas = new Canvas();
        canvas.setFocusable(true);
        add(canvas, BorderLayout.CENTER);

        // Sections creation
        navBar = new NavBar(this);
        modal = new Modal();
        dashboard = new StudentDashboard(this);
        adminDashboard = new AdminDashboard(this);

        sections.addAll(buildSections(this));

        // Hero search hands the query to the live matching demo
        Hero hero = null;
        MatchDemo demo = null;
        for (Section s : sections) {
            if (s instanceof Hero) hero = (Hero) s;
            if (s instanceof MatchDemo) demo = (MatchDemo) s;
        }
        if (hero != null) {
            final MatchDemo fDemo = demo;
            hero.setOnSearch(q -> {
                if (fDemo != null) fDemo.applyQuery(q);
                scrollToSection("demo");
            });
        }

        // Event listeners
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
        canvas.addKeyListener(this);
        addComponentListener(this);

        // Close window handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                dispose();
                System.exit(0);
            }
        });
    }

    /** Landing page sections in display order. */
    public static List<Section> buildSections(NavBar.NavListener listener) {
        List<Section> list = new ArrayList<>();
        list.add(new Hero(listener));
        list.add(new Problem());
        list.add(new HowItWorks());
        list.add(new Features());
        list.add(new MatchDemo());
        list.add(new Audience());
        list.add(new Compare(listener));
        list.add(new NumbersStrip());
        list.add(new Faq(listener));
        list.add(new Cta());
        list.add(new Footer(listener));
        return list;
    }

    public void start() {
        setVisible(true);
        canvas.requestFocus();

        // 2-buffer strategy
        canvas.createBufferStrategy(2);

        renderThread = new Thread(this::renderLoop, "TeamUp-RenderLoop");
        renderThread.start();
    }

    private void renderLoop() {
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000.0f;
            lastTime = now;

            // Cap dt to prevent spiral of death on long pauses
            dt = Math.min(dt, 0.05f);

            // Update animations & physics
            update(dt);

            // Render frame
            render();

            // Frame pacing: sleep only for what is left of the ~60 FPS budget
            long frameNanos = System.nanoTime() - now;
            long sleepMs = (16_666_667L - frameNanos) / 1_000_000L;
            try {
                Thread.sleep(Math.max(1L, sleepMs));
            } catch (InterruptedException ignored) {}
        }
    }

    private void update(float dt) {
        Theme.update(dt);
        Anim.update(dt);
        modal.update(dt);

        if (showAdminDashboard) {
            adminDashboard.update(dt);
            return;
        }
        if (showDashboard) {
            dashboard.update(dt);
            return;
        }

        navBar.update(dt);

        for (Section s : sections) {
            s.update(dt);
        }

        // Smooth scroll interpolation
        int maxScroll = Math.max(0, totalDocHeight - canvas.getHeight());
        targetScrollY = Math.max(0, Math.min(targetScrollY, maxScroll));
        scrollY = Anim.approach(scrollY, targetScrollY, dt, 9.0f);

        backTopAnim.setTarget(scrollY > 700 ? 1f : 0f);
        backTopAnim.update(dt);
        backTopHover.setTarget(backTopHovered ? 1f : 0f);
        backTopHover.update(dt);

        // Active section tracking for navbar
        int currentScrollInt = Math.round(scrollY);
        int navH = navBar.getHeight(canvas.getWidth());
        int runningY = navH;
        String currentActive = "hero";

        for (Section s : sections) {
            int sh = s.getHeight(canvas.getWidth());
            if (currentScrollInt + 200 >= runningY && currentScrollInt + 200 < runningY + sh) {
                currentActive = s.getId();
                break;
            }
            runningY += sh;
        }
        navBar.setActiveSection(currentActive);
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) return;

        Graphics g = null;
        try {
            g = bs.getDrawGraphics();
            Graphics2D g2 = (Graphics2D) g;
            Draw.setQualityHints(g2);

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            if (width <= 0 || height <= 0) return;

            if (showAdminDashboard) {
                Fx.background(g2, width, height, 0f);
                ThemedGraphics tg = new ThemedGraphics(g2);
                adminDashboard.paint(tg, width, height);
            } else {
                // Frosted-glass modal: capture + blur the scene once when it opens
                if (modal.needsBackdrop(width, height)) captureBackdrop(width, height);
                // Once the blurred backdrop fully covers the screen, skip the live scene
                if (!modal.coversScene()) {
                    if (showDashboard) {
                        Fx.background(g2, width, height, 0f);
                        ThemedGraphics tg = new ThemedGraphics(g2);
                        dashboard.paint(tg, width, height);
                    } else {
                        paintLanding(g2, width, height);
                    }
                }
                modal.paint(g2, width, height);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (g != null) g.dispose();
        }

        if (!bs.contentsLost()) {
            bs.show();
        }
    }

    /** Landing page scene (everything except the modal). */
    void paintLanding(Graphics2D g2, int width, int height) {
        int navH = navBar.getHeight(width);
        int viewportY = Math.round(scrollY);

        // Aurora background + cursor light
        Fx.background(g2, width, height, scrollY);
        Fx.cursorLight(g2);

        // Paint visible sections (viewport culling)
        int curY = navH;
        for (Section s : sections) {
            int sh = s.getHeight(width);
            int screenY = curY - viewportY;
            if (screenY + sh >= 0 && screenY <= height) {
                s.paint(g2, screenY, width, viewportY, height);
            }
            curY += sh;
        }
        totalDocHeight = curY;

        // Floating glass nav on top
        navBar.paint(g2, 0, width, viewportY, height);

        // Scroll progress: thin gradient line with a glowing tip
        int maxSc = Math.max(1, totalDocHeight - height);
        float scrollProgress = Math.min(1.0f, Math.max(0f, scrollY / (float) maxSc));
        int pw = Math.round(width * scrollProgress);
        if (pw > 0) {
            g2.setPaint(new GradientPaint(0, 0, Fx.PINK, Math.max(1, width), 0, Fx.CYAN));
            g2.fillRect(0, 0, pw, 2);
            Fx.glow(g2, pw, 1, 18, Fx.VIOLET, 0.8f);
        }

        paintBackToTop(g2, width, height, scrollProgress);
        paintScrollbar(g2, width, height);
    }

    /** Renders the current scene at 1/4 scale and hands it to the modal for blurring. */
    private void captureBackdrop(int w, int h) {
        int sw = Math.max(1, w / 4), sh = Math.max(1, h / 4);
        java.awt.image.BufferedImage small = new java.awt.image.BufferedImage(sw, sh, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = small.createGraphics();
        try {
            Draw.setQualityHints(sg);
            sg.scale(sw / (double) w, sh / (double) h);
            if (showDashboard) {
                Fx.background(sg, w, h, 0f);
                ThemedGraphics tg = new ThemedGraphics(sg);
                dashboard.paint(tg, w, h);
            } else if (showAdminDashboard) {
                Fx.background(sg, w, h, 0f);
                ThemedGraphics tg = new ThemedGraphics(sg);
                adminDashboard.paint(tg, w, h);
            } else {
                paintLanding(sg, w, h);
            }
        } finally {
            sg.dispose();
        }
        modal.setBackdrop(small, w, h);
    }

    private void paintBackToTop(Graphics2D g, int width, int height, float progress) {
        float a = backTopAnim.getValue();
        if (a < 0.01f) { backTopX = -100; return; }
        int s = BACK_TOP_SIZE;
        backTopX = width - s - 28;
        backTopY = height - s - 28;
        Graphics2D b = (Graphics2D) g.create();
        Fx.fadeBy(b, Fx.clamp(a));
        float hv = backTopHover.getValue();
        float x = backTopX, y = backTopY - hv * 3f + (1f - a) * 20f;
        Fx.glow(b, x + s / 2f, y + s / 2f, s * (0.9f + hv * 0.4f), Fx.VIOLET, 0.25f + 0.3f * hv);
        b.setColor(Fx.dark() ? new Color(0x10, 0x10, 0x22, 235) : new Color(255, 255, 255, 240));
        b.fill(new Ellipse2D.Float(x, y, s, s));
        b.setColor(Fx.border());
        b.draw(new Ellipse2D.Float(x, y, s, s));
        // scroll-progress ring
        b.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setPaint(new GradientPaint(x, y, Fx.PINK, x + s, y + s, Fx.CYAN));
        b.draw(new Arc2D.Float(x + 3, y + 3, s - 6, s - 6, 90, -360f * progress, Arc2D.OPEN));
        // arrow up
        Graphics2D r = (Graphics2D) b.create();
        r.translate(x + s / 2f, y + s / 2f - hv * 2f);
        r.rotate(-Math.PI / 2);
        Fx.arrow(r, -7, 0, 14, Fx.text(), 2f);
        r.dispose();
        b.dispose();
    }

    private void paintScrollbar(Graphics2D g, int width, int height) {
        int maxScroll = Math.max(0, totalDocHeight - height);
        if (maxScroll <= 0) return;

        int barW = 5;
        int barX = width - barW - 3;
        int navH = navBar.getHeight(width);
        int trackH = height - navH - 8;
        int trackY = navH + 4;

        float ratio = (float) height / (float) totalDocHeight;
        int thumbH = Math.max(36, Math.round(trackH * ratio));
        float scrollPct = scrollY / (float) maxScroll;
        int thumbY = trackY + Math.round((trackH - thumbH) * scrollPct);

        // Thumb
        if (isDraggingScrollbar) {
            g.setPaint(new GradientPaint(barX, thumbY, Fx.PINK, barX, thumbY + thumbH, Fx.VIOLET));
        } else {
            g.setColor(Fx.dark() ? new Color(255, 255, 255, 55) : new Color(20, 18, 50, 55));
        }
        g.fill(new RoundRectangle2D.Float(barX, thumbY, barW, thumbH, barW, barW));
    }

    // ==================== NAV BAR LISTENER ====================

    @Override
    public void scrollToSection(String sectionId) {
        int navH = navBar.getHeight(canvas.getWidth());
        int runningY = navH;

        for (Section s : sections) {
            if (s.getId().equalsIgnoreCase(sectionId)) {
                targetScrollY = Math.max(0, runningY - navH);
                return;
            }
            runningY += s.getHeight(canvas.getWidth());
        }
        if ("hero".equalsIgnoreCase(sectionId)) {
            targetScrollY = 0;
        }
    }

    @Override
    public void openModal(String title, String message, boolean isRegister) {
        modal.show(title, message, isRegister);
    }

    @Override
    public void openDashboard() {
        this.showDashboard = true;
        this.canvas.requestFocus();
    }

    @Override
    public void openAdminDashboard() {
        this.showDashboard = false;
        this.showAdminDashboard = true;
        this.canvas.requestFocus();
    }

    @Override
    public void switchToStudentDashboard() {
        this.showAdminDashboard = false;
        this.showDashboard = true;
        this.canvas.requestFocus();
    }

    @Override
    public void switchToAdminDashboard() {
        this.showDashboard = false;
        this.showAdminDashboard = true;
        this.canvas.requestFocus();
    }

    @Override
    public void returnToLanding() {
        this.showDashboard = false;
        this.showAdminDashboard = false;
        this.canvas.requestFocus();
    }

    // ==================== MOUSE EVENTS ====================

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (showAdminDashboard) {
            adminDashboard.onMouseWheel(e.getWheelRotation());
            return;
        }
        if (showDashboard) {
            dashboard.onMouseWheel(e.getWheelRotation(), canvas.getHeight());
            return;
        }
        if (modal.visible) return;
        if (navBar.isMenuOpen()) navBar.closeMenu();
        targetScrollY += (float) (e.getPreciseWheelRotation() * 110);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 1. Modal check
        if (modal.visible) {
            if (modal.onClick(mx, my, width, height)) return;
        }

        // 2. Admin & Student Dashboard check
        if (showAdminDashboard) {
            return; // Clicks handled in mouseReleased to prevent double-firing
        }
        if (showDashboard) {
            long now = System.currentTimeMillis();
            if (now - lastDashClickTime > 180) {
                lastDashClickTime = now;
                dashboard.onClick(mx, my);
            }
            return;
        }

        // 3. Back-to-top, then floating nav (and its open menu)
        int navH = navBar.getHeight(width);
        if (backTopAnim.getValue() > 0.5f && Fx.inside(mx, my, backTopX, backTopY, BACK_TOP_SIZE, BACK_TOP_SIZE)) {
            targetScrollY = 0;
            return;
        }
        if (navBar.captures(mx, my, width) || navBar.isMenuOpen()) {
            if (navBar.onClick(mx, my)) return;
        }

        // 3. Document sections click
        int viewportY = Math.round(scrollY);
        int curY = navH;
        for (Section s : sections) {
            int sh = s.getHeight(width);
            int screenY = curY - viewportY;
            if (screenY + sh >= 0 && screenY <= height) {
                if (s.onClick(mx, my)) return;
            }
            curY += sh;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        mousePressX = mx;
        mousePressY = my;

        // Check scrollbar hit
        int maxScroll = Math.max(0, totalDocHeight - height);
        if (maxScroll > 0 && mx >= width - 18) {
            isDraggingScrollbar = true;
            dragStartY = my;
            dragStartScroll = scrollY;
            return;
        }

        if (showDashboard) return;

        if (modal.visible) { modal.setPressed(true, mx, my); return; }

        int navH = navBar.getHeight(width);
        if (navBar.captures(mx, my, width)) {
            navBar.onMouseDown(mx, my);
            return;
        }

        int viewportY = Math.round(scrollY);
        int curY = navH;
        for (Section s : sections) {
            int sh = s.getHeight(width);
            int screenY = curY - viewportY;
            if (screenY + sh >= 0 && screenY <= height) {
                s.onMouseDown(mx, my);
            }
            curY += sh;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isDraggingScrollbar = false;
        int mx = e.getX();
        int my = e.getY();
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (showAdminDashboard) {
            if (Math.abs(mx - mousePressX) <= 6 && Math.abs(my - mousePressY) <= 6) {
                adminDashboard.onClick(mx, my);
            }
            return;
        }
        if (showDashboard) {
            if (Math.abs(mx - mousePressX) <= 6 && Math.abs(my - mousePressY) <= 6) {
                long now = System.currentTimeMillis();
                if (now - lastDashClickTime > 180) {
                    lastDashClickTime = now;
                    dashboard.onClick(mx, my);
                }
            }
            return;
        }

        if (modal.visible) { modal.setPressed(false, mx, my); return; }

        int navH = navBar.getHeight(width);
        if (navBar.captures(mx, my, width)) {
            navBar.onMouseUp(mx, my);
        }

        int viewportY = Math.round(scrollY);
        int curY = navH;
        for (Section s : sections) {
            int sh = s.getHeight(width);
            int screenY = curY - viewportY;
            if (screenY + sh >= 0 && screenY <= height) {
                s.onMouseUp(mx, my);
            }
            curY += sh;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        Anim.setMouseTarget(mx, my);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        boolean anyHover = false;

        if (modal.visible) {
            anyHover = modal.onMouseMove(mx, my);
            updateCursor(anyHover);
            return;
        }

        if (showAdminDashboard) {
            anyHover = adminDashboard.onMouseMove(mx, my);
            updateCursor(anyHover);
            return;
        }
        if (showDashboard) {
            anyHover = dashboard.onMouseMove(mx, my);
            updateCursor(anyHover);
            return;
        }

        int navH = navBar.getHeight(width);
        backTopHovered = backTopAnim.getValue() > 0.5f && Fx.inside(mx, my, backTopX, backTopY, BACK_TOP_SIZE, BACK_TOP_SIZE);
        boolean overNav = navBar.captures(mx, my, width);
        anyHover = navBar.onMouseMove(mx, my) || backTopHovered;
        if (overNav || backTopHovered) {
            for (Section s : sections) s.onMouseMove(-100, -100);
            updateCursor(anyHover);
            return;
        }

        int viewportY = Math.round(scrollY);
        int curY = navH;
        for (Section s : sections) {
            int sh = s.getHeight(width);
            int screenY = curY - viewportY;
            if (screenY + sh >= 0 && screenY <= height) {
                if (s.onMouseMove(mx, my)) {
                    anyHover = true;
                }
            } else {
                s.onMouseMove(-100, -100);
            }
            curY += sh;
        }

        updateCursor(anyHover);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Anim.setMouseTarget(e.getX(), e.getY());
        if (isDraggingScrollbar) {
            int dy = e.getY() - dragStartY;
            int height = canvas.getHeight();
            int maxScroll = Math.max(0, totalDocHeight - height);
            float ratio = (float) maxScroll / (float) (height - 60);
            targetScrollY = dragStartScroll + dy * ratio;
            scrollY = targetScrollY;
        }
    }

    private void updateCursor(boolean hand) {
        canvas.setCursor(Cursor.getPredefinedCursor(hand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // ==================== KEYBOARD NAVIGATION ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Modal key delegation
        if (modal.visible) {
            if (modal.onKey(e)) return;
        }

        // F2 or D key toggles Dashboard view directly
        if (code == KeyEvent.VK_F2) {
            showDashboard = !showDashboard;
            return;
        }

        if (showAdminDashboard) {
            if (adminDashboard.onKey(e.getKeyChar(), code)) return;
            if (code == KeyEvent.VK_ESCAPE) {
                showAdminDashboard = false;
                return;
            }
            return;
        }
        if (showDashboard) {
            if (dashboard.onKey(e)) return;
            if (code == KeyEvent.VK_ESCAPE) {
                showDashboard = false;
                return;
            }
            return;
        }

        if (navBar.onKey(e)) return;

        // A focused text field (hero search, early-access email) consumes typing first
        for (Section s : sections) {
            if (hasFocusedInput(s) && s.onKey(e)) return;
        }

        // Motion toggle: key 'M'
        if (code == KeyEvent.VK_M) {
            Theme.motionEnabled = !Theme.motionEnabled;
            return;
        }

        // Ctrl +/- Zoom
        if (e.isControlDown()) {
            if (code == KeyEvent.VK_EQUALS || code == KeyEvent.VK_PLUS) {
                Theme.zoomIn();
                return;
            } else if (code == KeyEvent.VK_MINUS) {
                Theme.zoomOut();
                return;
            } else if (code == KeyEvent.VK_0) {
                Theme.resetZoom();
                return;
            }
        }

        // Arrow keys & Page scrolling
        if (code == KeyEvent.VK_DOWN) {
            targetScrollY += 60;
            return;
        } else if (code == KeyEvent.VK_UP) {
            targetScrollY -= 60;
            return;
        } else if (code == KeyEvent.VK_PAGE_DOWN) {
            targetScrollY += canvas.getHeight() * 0.8f;
            return;
        } else if (code == KeyEvent.VK_PAGE_UP) {
            targetScrollY -= canvas.getHeight() * 0.8f;
            return;
        } else if (code == KeyEvent.VK_HOME) {
            targetScrollY = 0;
            return;
        } else if (code == KeyEvent.VK_END) {
            targetScrollY = totalDocHeight;
            return;
        }

        // Delegate to active input in sections (e.g. Lead form textbox)
        for (Section s : sections) {
            if (s.onKey(e)) return;
        }
    }

    private boolean hasFocusedInput(Section s) {
        return (s instanceof Hero && ((Hero) s).hasFocus())
                || (s instanceof Cta && ((Cta) s).hasFocus())
                || (s instanceof MatchDemo && ((MatchDemo) s).hasFocus())
                || (s instanceof Audience && ((Audience) s).hasFocus());
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {
        if (modal.visible) {
            if (modal.onKey(e)) return;
        }
        if (showAdminDashboard) {
            return; // Handled in keyPressed with full keyCode support
        }
        if (showDashboard) {
            if (dashboard.onKey(e)) return;
            return;
        }
        for (Section s : sections) {
            if (s.onKey(e)) return;
        }
    }

    // ==================== COMPONENT RESIZING ====================

    @Override
    public void componentResized(ComponentEvent e) {
        // Automatically triggers responsive recalculations in render loop
    }

    @Override
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentShown(ComponentEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}
}

