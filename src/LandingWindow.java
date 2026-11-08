import java.awt.*;
import java.awt.event.*;
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

    // Floating Back to Top Button
    private int topBtnX, topBtnY, topBtnSize = 42;
    private boolean topBtnHovered = false;

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

        sections.add(new Hero(this));
        sections.add(new Problem());
        sections.add(new HowItWorks());
        sections.add(new Features());
        sections.add(new MatchDemo());
        sections.add(new Audience());
        sections.add(new Compare());
        sections.add(new NumbersStrip());
        sections.add(new Faq());
        sections.add(new Cta());
        sections.add(new Footer(this));

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

            // Target ~60 FPS
            try {
                Thread.sleep(16);
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
        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14.0f);

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
                adminDashboard.paint(g2, width, height);
            } else if (showDashboard) {
                dashboard.paint(g2, width, height);
                modal.paint(g2, width, height);
            } else {
                // Background with warm off-white canvas & ambient tint
                Draw.drawBackground(g2, width, height);

                // Motion Spec #4: Soft pink glow follows mouse with lag
                Draw.drawCursorGlow(g2);

                int navH = navBar.getHeight(width);
                int viewportY = Math.round(scrollY);

                // Calculate document height & paint visible sections
                int curY = navH;
                for (Section s : sections) {
                    int sh = s.getHeight(width);
                    int screenY = curY - viewportY;

                    // Viewport culling: only paint if visible on screen
                    if (screenY + sh >= 0 && screenY <= height) {
                        s.paint(g2, screenY, width, viewportY, height);
                    }
                    curY += sh;
                }
                totalDocHeight = curY;

                // Paint Sticky Glass Nav on top
                navBar.paint(g2, 0, width, viewportY, height);

                // Motion Spec #10: Thin pink progress bar at the very top
                int maxSc = Math.max(1, totalDocHeight - height);
                float scrollProgress = Math.min(1.0f, Math.max(0f, scrollY / (float) maxSc));
                g2.setColor(Theme.BRAND_PINK);
                g2.fillRect(0, 0, Math.round(width * scrollProgress), 3);

                // Paint Custom Scrollbar
                paintScrollbar(g2, width, height);

                // Floating Back-to-Top Button when scrolled down
                if (scrollY > 350) {
                    paintBackToTop(g2, width, height);
                }

                // Paint Modal Dialog if active
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

    private void paintScrollbar(Graphics2D g, int width, int height) {
        int maxScroll = Math.max(0, totalDocHeight - height);
        if (maxScroll <= 0) return;

        int barW = 8;
        int barX = width - barW - 4;
        int navH = navBar.getHeight(width);
        int trackH = height - navH - 8;
        int trackY = navH + 4;

        float ratio = (float) height / (float) totalDocHeight;
        int thumbH = Math.max(36, Math.round(trackH * ratio));
        float scrollPct = scrollY / (float) maxScroll;
        int thumbY = trackY + Math.round((trackH - thumbH) * scrollPct);

        // Thumb
        Color thumbColor = Theme.isDark ? new Color(255, 255, 255, 60) : new Color(0, 0, 0, 50);
        if (isDraggingScrollbar) {
            thumbColor = Theme.ACCENT_CYAN;
        }
        g.setColor(thumbColor);
        g.fill(new RoundRectangle2D.Float(barX, thumbY, barW, thumbH, barW, barW));
    }

    private void paintBackToTop(Graphics2D g, int width, int height) {
        topBtnSize = 44;
        topBtnX = width - topBtnSize - 22;
        topBtnY = height - topBtnSize - 22;

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // Soft drop shadow
        g2.setColor(Theme.isDark ? new Color(0, 0, 0, 90) : new Color(0, 0, 0, 35));
        g2.fillRoundRect(topBtnX + 1, topBtnY + 3, topBtnSize, topBtnSize, topBtnSize, topBtnSize);

        // Circular background
        if (topBtnHovered) {
            g2.setColor(Theme.BRAND_PINK);
        } else {
            g2.setColor(Theme.cardBg());
        }
        g2.fillRoundRect(topBtnX, topBtnY, topBtnSize, topBtnSize, topBtnSize, topBtnSize);

        // Ring border
        g2.setColor(topBtnHovered ? Theme.BRAND_PINK_HOVER : Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(topBtnX, topBtnY, topBtnSize, topBtnSize, topBtnSize, topBtnSize);

        // Up arrow icon
        g2.setColor(topBtnHovered ? Color.WHITE : Theme.textPrimary());
        int cx = topBtnX + topBtnSize / 2;
        int cy = topBtnY + topBtnSize / 2;
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy + 5, cx, cy - 6);
        g2.drawLine(cx - 5, cy - 1, cx, cy - 6);
        g2.drawLine(cx + 5, cy - 1, cx, cy - 6);

        g2.dispose();
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
        int notches = e.getWheelRotation();
        targetScrollY += notches * 85;
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

        // 3. Floating Back-to-Top Button check
        if (scrollY > 350 && mx >= topBtnX && mx <= topBtnX + topBtnSize && my >= topBtnY && my <= topBtnY + topBtnSize) {
            targetScrollY = 0f;
            return;
        }

        // 4. Sticky Nav click check (header or expanded mobile drawer)
        int navH = navBar.getHeight(width);
        if (my <= navH || navBar.isMobileMenuOpen()) {
            if (navBar.onClick(mx, my)) return;
        }

        // 5. Document sections click
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

        if (modal.visible) return;

        int navH = navBar.getHeight(width);
        if (my <= navH) {
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

        if (modal.visible) return;

        int navH = navBar.getHeight(width);
        if (my <= navH) {
            navBar.onMouseUp(mx, my);
            return;
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
        if (my <= navH || navBar.isMobileMenuOpen()) {
            if (navBar.onMouseMove(mx, my)) anyHover = true;
        } else {
            navBar.onMouseMove(-100, -100);
        }

        // Floating Back-to-Top Button hover check
        if (scrollY > 350 && mx >= topBtnX && mx <= topBtnX + topBtnSize && my >= topBtnY && my <= topBtnY + topBtnSize) {
            topBtnHovered = true;
            anyHover = true;
        } else {
            topBtnHovered = false;
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

        // Global view toggles & shortcuts
        if (code == KeyEvent.VK_F2 || (code == KeyEvent.VK_D && !modal.visible && !showDashboard && !showAdminDashboard)) {
            showDashboard = !showDashboard;
            showAdminDashboard = false;
            return;
        }

        if (code == KeyEvent.VK_A && !modal.visible && !showDashboard && !showAdminDashboard) {
            showAdminDashboard = true;
            showDashboard = false;
            return;
        }

        if (code == KeyEvent.VK_T && !modal.visible) {
            Theme.toggleTheme();
            return;
        }

        if (code == KeyEvent.VK_ESCAPE) {
            if (navBar.isMobileMenuOpen()) {
                navBar.closeMobileMenu();
                return;
            }
            if (showDashboard || showAdminDashboard) {
                showDashboard = false;
                showAdminDashboard = false;
                return;
            }
        }

        if (showAdminDashboard) {
            if (adminDashboard.onKey(e.getKeyChar(), code)) return;
            return;
        }
        if (showDashboard) {
            if (dashboard.onKey(e)) return;
            return;
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

