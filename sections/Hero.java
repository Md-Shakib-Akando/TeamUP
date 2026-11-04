import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Hero implements Section {

    private final NavBar.NavListener listener;

    // Search bar state
    private final TextBox heroSearchBox;
    private final Button heroSearchBtn;

    // Popular skill pills that jump on hover
    private static class JumpingPill {
        String label;
        int x, y, w, h;
        boolean hovered;
        final Elastic jumpY = new Elastic(0f, 220f, 12f);
        JumpingPill(String l) { this.label = l; }
    }
    private final List<JumpingPill> popularPills = new ArrayList<>();

    // CTA buttons (Motion Spec #8: Magnetic button)
    private int btn1X, btn1Y, btn1W = 210, btn1H = 48;
    private int btn2X, btn2Y, btn2W = 180, btn2H = 48;
    private boolean btn1Hovered = false;
    private boolean btn2Hovered = false;
    private final Elastic magnet1X = new Elastic(0f, 160f, 14f);
    private final Elastic magnet1Y = new Elastic(0f, 160f, 14f);

    // Member Shot Cards for Parallax Masonry Grid (Motion Spec #5, #6, #7)
    public static class MemberShot {
        String name, role, initials;
        String[] skills;
        int matchPct;
        double rating;
        int styleIdx;
        Color c1, c2;
        int col, baseH;
        int x, y, w, h;

        boolean hovered = false;
        boolean invited = false;
        boolean liked = false;

        final Elastic tiltX = new Elastic(0f, 140f, 12f);
        final Elastic tiltY = new Elastic(0f, 140f, 12f);
        final Elastic scale = new Elastic(1.0f, 180f, 14f);
        final Elastic overlayAlpha = new Elastic(0f, 200f, 16f);

        MemberShot(String name, String role, String initials, String[] skills,
                   int matchPct, double rating, int styleIdx, Color c1, Color c2, int col, int baseH) {
            this.name = name;
            this.role = role;
            this.initials = initials;
            this.skills = skills;
            this.matchPct = matchPct;
            this.rating = rating;
            this.styleIdx = styleIdx;
            this.c1 = c1;
            this.c2 = c2;
            this.col = col;
            this.baseH = baseH;
        }
    }

    private final List<MemberShot> memberShots = new ArrayList<>();

    // Marquee strip (Motion Spec #9)
    private final String[] marqueeItems = {
            "⚡ Java Full-Stack", "🎨 UI/UX Design", "🚀 React Native", "📊 Data Science",
            "🛡 Cloud & DevOps", "Students", "Developers", "Freelancers", "Designers",
            "Researchers", "Startups", "🤖 PyTorch / AI", "🗄 PostgreSQL", "🔬 System Architect"
    };
    private float marqueeOffset = 0f;
    private boolean marqueeHovered = false;

    // Squiggle self-draw progress
    private float squiggleProgress = 0f;

    public Hero(NavBar.NavListener listener) {
        this.listener = listener;

        heroSearchBox = new TextBox("Search by skill: Java, UI/UX, MySQL...");
        heroSearchBtn = new Button("Search", true, () -> {
            if (listener != null) listener.scrollToSection("demo");
        });

        popularPills.add(new JumpingPill("Java"));
        popularPills.add(new JumpingPill("UI/UX"));
        popularPills.add(new JumpingPill("Python"));
        popularPills.add(new JumpingPill("React"));
        popularPills.add(new JumpingPill("MySQL"));
        popularPills.add(new JumpingPill("Figma"));

        // Initialize 6 member shot cards across 3 columns with varied heights
        memberShots.add(new MemberShot("Sarah Lin", "Backend & Distributed", "SL",
                new String[]{"Java", "Spring", "Docker"}, 96, 4.9, 0,
                new Color(0xFF, 0x8A, 0x5B), Theme.BRAND_PINK, 0, 290));

        memberShots.add(new MemberShot("David Chen", "Design Systems Lead", "DC",
                new String[]{"UI/UX", "Figma", "Design"}, 92, 5.0, 1,
                Theme.ACCENT_VIOLET, new Color(0x6E, 0xC1, 0xFF), 1, 320));

        memberShots.add(new MemberShot("Priya Patel", "Full Stack Developer", "PP",
                new String[]{"React", "Node", "Postgres"}, 94, 4.8, 2,
                Theme.ACCENT_MINT, new Color(0x22, 0xD3, 0xEE), 2, 280));

        memberShots.add(new MemberShot("Marcus Vance", "Mobile Architect", "MV",
                new String[]{"Flutter", "Kotlin", "Swift"}, 88, 4.7, 1,
                new Color(0xFF, 0xD1, 0x66), new Color(0xFF, 0x8A, 0x5B), 0, 270));

        memberShots.add(new MemberShot("Nila Rahman", "Brand & Motion", "NR",
                new String[]{"Motion", "Illustrator", "Web"}, 91, 5.0, 0,
                Theme.BRAND_PINK, Theme.ACCENT_VIOLET, 1, 280));

        memberShots.add(new MemberShot("Tanvir Ahmed", "AI & ML Researcher", "TA",
                new String[]{"Python", "NLP", "PyTorch"}, 95, 4.9, 2,
                new Color(0x6E, 0xC1, 0xFF), Theme.ACCENT_MINT, 2, 310));
    }

    @Override
    public String getId() {
        return "hero";
    }

    @Override
    public int getHeight(int width) {
        if (width < 780) {
            return 1560;
        } else if (width < 1100) {
            return 1480;
        } else {
            return 1420;
        }
    }

    @Override
    public void update(float dt) {
        heroSearchBox.update(dt);

        // Motion Spec #3: Self-drawing squiggle delay and progress
        if (Anim.pageLoadTime > 0.4f) {
            squiggleProgress = Anim.approach(squiggleProgress, 1.0f, dt, 3.5f);
        }

        // Popular pills hover jumping physics
        for (JumpingPill pill : popularPills) {
            pill.jumpY.setTarget(pill.hovered ? -8f : 0f);
            pill.jumpY.update(dt);
        }

        // Motion Spec #8: Magnetic CTAs
        float btnCenterX = btn1X + btn1W / 2f;
        float btnCenterY = btn1Y + btn1H / 2f;
        float[] mag = Anim.calculateMagneticOffset(btnCenterX, btnCenterY, 80f, 8f);
        magnet1X.setTarget(mag[0]);
        magnet1Y.setTarget(mag[1]);
        magnet1X.update(dt);
        magnet1Y.update(dt);

        // Member shot cards physics (tilt, scale, overlay)
        for (MemberShot shot : memberShots) {
            shot.scale.setTarget(shot.hovered ? 1.03f : 1.0f);
            shot.overlayAlpha.setTarget(shot.hovered ? 1.0f : 0.0f);
            shot.scale.update(dt);
            shot.overlayAlpha.update(dt);

            if (shot.hovered) {
                float cx = shot.x + shot.w / 2f;
                float cy = shot.y + shot.h / 2f;
                shot.tiltX.setTarget((Anim.mouseX - cx) * 0.15f);
                shot.tiltY.setTarget((Anim.mouseY - cy) * 0.15f);
            } else {
                shot.tiltX.setTarget(0f);
                shot.tiltY.setTarget(0f);
            }
            shot.tiltX.update(dt);
            shot.tiltY.update(dt);
        }

        // Motion Spec #9: Marquee strip endless scrolling
        if (Theme.motionEnabled) {
            float speed = marqueeHovered ? 25f : 65f;
            marqueeOffset += speed * dt;
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        boolean isMobile = width < 780;
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        int curY = y + 40;

        // 1. Eyebrow pill: "Skill-based team formation" with pulsing pink dot
        int badgeH = 34;
        int badgeW = 230;
        int badgeX = (width - badgeW) / 2;
        RoundRectangle2D badgeShape = new RoundRectangle2D.Float(badgeX, curY, badgeW, badgeH, badgeH, badgeH);

        g2.setColor(Theme.isDark ? new Color(255, 255, 255, 12) : Color.WHITE);
        g2.fill(badgeShape);
        g2.setColor(Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(badgeShape);

        Draw.drawRadarBeacon(g2, badgeX + 18, curY + badgeH / 2, Theme.BRAND_PINK);

        g2.setFont(Theme.fontSmallBold());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Skill-based team formation", badgeX + 34, curY + (badgeH - bfm.getHeight()) / 2 + bfm.getAscent());

        curY += badgeH + 24;

        // 2. Headline (Motion Spec #2: Staggered words rising 40px and fading in)
        g2.setFont(isMobile ? Theme.fontHeroMobile() : Theme.fontHero());
        FontMetrics hfm = g2.getFontMetrics();
        String h1 = "Find the right ";
        String h2 = "teammates.";
        String h3 = "Build what matters.";

        int totalH1W = hfm.stringWidth(h1) + hfm.stringWidth(h2);
        int line1X = (width - totalH1W) / 2;
        int line2X = (width - hfm.stringWidth(h3)) / 2;

        float intro = Anim.pageLoadTime;

        // Line 1: "Find the right teammates."
        float wordDelay1 = Math.max(0f, Math.min(1f, (intro - 0.1f) / 0.5f));
        float wordDelay2 = Math.max(0f, Math.min(1f, (intro - 0.22f) / 0.5f));

        int wordY1 = curY + hfm.getAscent() + Math.round(40 * (1f - Anim.easeOutCubic(wordDelay1)));
        int wordY2 = curY + hfm.getAscent() + Math.round(40 * (1f - Anim.easeOutCubic(wordDelay2)));

        g2.setColor(Theme.textPrimary());
        g2.drawString(h1, line1X, wordY1);

        int teamX = line1X + hfm.stringWidth(h1);
        g2.setColor(Theme.BRAND_PINK);
        g2.drawString(h2, teamX, wordY2);

        // Motion Spec #3: Self-drawing pink squiggle under "teammates"
        Draw.drawPinkSquiggle(g2, teamX, wordY2 + 10, hfm.stringWidth(h2), squiggleProgress);

        // Line 2: "Build what matters."
        float wordDelay3 = Math.max(0f, Math.min(1f, (intro - 0.35f) / 0.5f));
        int wordY3 = curY + hfm.getHeight() + 10 + hfm.getAscent() + Math.round(40 * (1f - Anim.easeOutCubic(wordDelay3)));
        g2.setColor(Theme.textPrimary());
        g2.drawString(h3, line2X, wordY3);

        curY += hfm.getHeight() * 2 + 20;

        // 3. Subtext
        g2.setFont(Theme.fontSubtitle());
        FontMetrics sfm = g2.getFontMetrics();
        g2.setColor(Theme.textSecondary());
        String sub = "TeamUp matches students, developers, freelancers and researchers by skill, so you spend less time searching and more time building.";
        int subMaxW = Math.min(680, width - padX * 2);
        List<String> subLines = Draw.wrapText(sub, sfm, subMaxW);
        for (String sl : subLines) {
            int sx = (width - sfm.stringWidth(sl)) / 2;
            g2.drawString(sl, sx, curY + sfm.getAscent());
            curY += Math.round(sfm.getHeight() * 1.35f);
        }

        curY += 16;

        // 4. Hero Search Bar: big white pill + pink Search button + popular pills
        int searchW = Math.min(620, width - padX * 2);
        int searchH = 54;
        int searchX = (width - searchW) / 2;
        int searchY = curY;

        RoundRectangle2D searchPill = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, searchH, searchH);
        g2.setColor(Color.WHITE);
        g2.fill(searchPill);
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fill(new RoundRectangle2D.Float(searchX, searchY + 4, searchW, searchH, searchH, searchH));
        g2.setColor(Theme.cardBorder());
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(searchPill);

        int sBtnW = 110;
        int sBtnH = 42;
        int sBtnX = searchX + searchW - sBtnW - 6;
        int sBtnY = searchY + 6;

        heroSearchBox.setBounds(searchX + 16, searchY + 5, searchW - sBtnW - 28, searchH - 10);
        heroSearchBox.paint(g2);

        Draw.drawPinkPillButton(g2, sBtnX, sBtnY, sBtnW, sBtnH, "Search", false, false, false, 0f, 0f);

        curY += searchH + 16;

        // Popular pills row (jumping on hover)
        g2.setFont(Theme.fontSmallBold());
        FontMetrics pfm = g2.getFontMetrics();
        int popularTotalW = pfm.stringWidth("Popular:") + 14;
        for (JumpingPill p : popularPills) {
            p.w = pfm.stringWidth(p.label) + 24;
            p.h = 26;
            popularTotalW += p.w + 8;
        }

        int popX = (width - popularTotalW) / 2;
        if (popX < padX) popX = padX;
        g2.setColor(Theme.textMuted());
        g2.drawString("Popular:", popX, curY + 18);
        popX += pfm.stringWidth("Popular:") + 14;

        for (JumpingPill pill : popularPills) {
            pill.x = popX;
            pill.y = Math.round(curY + pill.jumpY.getValue());
            RoundRectangle2D pillShape = new RoundRectangle2D.Float(pill.x, pill.y, pill.w, pill.h, pill.h, pill.h);

            g2.setColor(pill.hovered ? Theme.INK_DARK : (Theme.isDark ? new Color(255, 255, 255, 12) : Color.WHITE));
            g2.fill(pillShape);
            g2.setColor(Theme.cardBorder());
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(pillShape);

            g2.setColor(pill.hovered ? Color.WHITE : Theme.textPrimary());
            int ptx = pill.x + (pill.w - pfm.stringWidth(pill.label)) / 2;
            int pty = pill.y + (pill.h - pfm.getHeight()) / 2 + pfm.getAscent();
            g2.drawString(pill.label, ptx, pty);

            popX += pill.w + 8;
        }

        curY += 46;

        // 5. CTAs: "Get Started, it's free" (pink pill, magnetic) and "See how it works" (ghost)
        int ctaTotalW = btn1W + 16 + btn2W;
        btn1X = (width - ctaTotalW) / 2;
        btn1Y = curY;
        btn2X = btn1X + btn1W + 16;
        btn2Y = curY;

        Draw.drawPinkPillButton(g2, btn1X, btn1Y, btn1W, btn1H, "Get Started, it's free",
                btn1Hovered, false, false, magnet1X.getValue(), magnet1Y.getValue());

        Draw.drawGhostButton(g2, btn2X, btn2Y, btn2W, btn2H, "See how it works",
                btn2Hovered, false, false);

        curY += btn1H + 48;

        // 6. Masonry of Member Shots (Motion Spec #5: Parallax columns drifting at different speeds)
        paintMasonryGrid(g2, curY, width, padX, viewportY);

        curY += 480;

        // 7. Marquee Strip (Motion Spec #9: endless row of skill pills & audiences)
        paintMarqueeStrip(g2, curY, width);

        g2.dispose();
    }

    private void paintMasonryGrid(Graphics2D g, int startY, int width, int padX, int viewportY) {
        int gap = 20;
        int totalW = width - padX * 2;
        boolean isMobile = width < 780;
        boolean isTablet = width >= 780 && width < 1100;

        int cols = isMobile ? 1 : (isTablet ? 2 : 3);
        int colW = (totalW - gap * (cols - 1)) / cols;

        int[] colY = new int[cols];
        for (int i = 0; i < cols; i++) colY[i] = startY;

        // Motion Spec #5: Parallax Masonry offsets
        // Column 0 moves normally, column 1 moves opposite way!
        float factorCol0 = (viewportY * 0.08f);
        float factorCol1 = -(viewportY * 0.06f);
        float factorCol2 = (viewportY * 0.04f);

        for (int i = 0; i < memberShots.size(); i++) {
            MemberShot shot = memberShots.get(i);
            int c = isMobile ? 0 : (isTablet ? (i % 2) : shot.col);
            int cardX = padX + c * (colW + gap);

            float parallaxOffset = (c == 1) ? factorCol1 : ((c == 2) ? factorCol2 : factorCol0);
            int cardY = Math.round(colY[c] + parallaxOffset);

            shot.x = cardX;
            shot.y = cardY;
            shot.w = colW;
            shot.h = shot.baseH;

            Draw.drawMemberCard(g, shot.x, shot.y, shot.w, shot.h,
                    shot.name, shot.role, shot.initials, shot.skills,
                    shot.matchPct, shot.rating, shot.styleIdx, shot.c1, shot.c2,
                    shot.tiltX.getValue(), shot.tiltY.getValue(), shot.scale.getValue(),
                    shot.hovered, shot.invited, shot.liked, shot.overlayAlpha.getValue(),
                    () -> shot.invited = !shot.invited,
                    () -> shot.liked = !shot.liked);

            colY[c] += shot.h + gap;
        }
    }

    private void paintMarqueeStrip(Graphics2D g, int y, int width) {
        int stripH = 50;

        g.setColor(Theme.isDark ? new Color(255, 255, 255, 8) : Color.WHITE);
        g.fillRect(0, y, width, stripH);
        g.setColor(Theme.cardBorder());
        g.drawLine(0, y, width, y);
        g.drawLine(0, y + stripH, width, y + stripH);

        g.setFont(Theme.fontSmallBold());
        FontMetrics fm = g.getFontMetrics();

        int pillPad = 32;
        int totalMarqueeW = 0;
        for (String item : marqueeItems) {
            totalMarqueeW += fm.stringWidth(item) + pillPad * 2;
        }

        float renderOffset = -(marqueeOffset % totalMarqueeW);
        while (renderOffset < width) {
            for (String item : marqueeItems) {
                int itemW = fm.stringWidth(item) + pillPad;
                int ix = Math.round(renderOffset);
                int iy = y + (stripH - 28) / 2;

                RoundRectangle2D mpill = new RoundRectangle2D.Float(ix, iy, itemW, 28, 28, 28);
                g.setColor(Theme.isDark ? new Color(255, 255, 255, 12) : new Color(0, 0, 0, 6));
                g.fill(mpill);

                g.setColor(Theme.textPrimary());
                g.drawString(item, ix + pillPad / 2, iy + (28 - fm.getHeight()) / 2 + fm.getAscent());

                renderOffset += itemW + 16;
            }
        }
    }

    @Override
    public boolean onClick(int mx, int my) {
        if (heroSearchBox.onMouseDown(mx, my)) return true;

        if (mx >= btn1X && mx <= btn1X + btn1W && my >= btn1Y && my <= btn1Y + btn1H) {
            if (listener != null) {
                listener.openModal("Get Started with TeamUp", "Join now to explore projects, match with peers, and build your next portfolio showcase.", true);
            }
            return true;
        }

        if (mx >= btn2X && mx <= btn2X + btn2W && my >= btn2Y && my <= btn2Y + btn2H) {
            if (listener != null) listener.scrollToSection("how-it-works");
            return true;
        }

        // Popular pills click sets search box text
        for (JumpingPill pill : popularPills) {
            if (mx >= pill.x && mx <= pill.x + pill.w && my >= pill.y && my <= pill.y + pill.h) {
                heroSearchBox.setText(pill.label);
                return true;
            }
        }

        // Member shot cards click invite or like
        for (MemberShot shot : memberShots) {
            if (mx >= shot.x && mx <= shot.x + shot.w && my >= shot.y && my <= shot.y + shot.h) {
                shot.invited = !shot.invited;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean p1 = btn1Hovered;
        boolean p2 = btn2Hovered;
        btn1Hovered = (mx >= btn1X && mx <= btn1X + btn1W && my >= btn1Y && my <= btn1Y + btn1H);
        btn2Hovered = (mx >= btn2X && mx <= btn2X + btn2W && my >= btn2Y && my <= btn2Y + btn2H);
        if (btn1Hovered != p1 || btn2Hovered != p2) changed = true;

        for (JumpingPill pill : popularPills) {
            boolean was = pill.hovered;
            pill.hovered = (mx >= pill.x && mx <= pill.x + pill.w && my >= pill.y && my <= pill.y + pill.h);
            if (pill.hovered != was) changed = true;
        }

        for (MemberShot shot : memberShots) {
            boolean was = shot.hovered;
            shot.hovered = (mx >= shot.x && mx <= shot.x + shot.w && my >= shot.y && my <= shot.y + shot.h);
            if (shot.hovered != was) changed = true;
        }

        marqueeHovered = (my >= getHeight(1200) - 100);

        return changed;
    }

    @Override
    public boolean onMouseDown(int mx, int my) {
        return heroSearchBox.onMouseDown(mx, my);
    }

    @Override
    public boolean onMouseUp(int mx, int my) { return false; }

    @Override
    public boolean onKey(KeyEvent e) {
        return heroSearchBox.onKey(e);
    }
}
