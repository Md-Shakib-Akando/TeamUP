import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Hero — interactive particle "team network", typewriter gradient headline,
 * glass search bar with live skill hand-off to the Match demo, magnetic CTAs,
 * and a mouse-tilting app preview window with floating stat badges.
 */
public class Hero extends LpSection {

    private final NavBar.NavListener listener;
    private Consumer<String> onSearch;

    // ---------------------------------------------------------------- search
    private final TextBox searchBox;
    private int sbX, sbY, sbW, sbH;
    private int sBtnX, sBtnY, sBtnW, sBtnH;
    private boolean sBtnHover;
    private final Elastic sBtnHov = new Elastic(0f, 260f, 20f);

    private static class Pill {
        final String label;
        int x, y, w, h;
        boolean hovered;
        final Elastic jump = new Elastic(0f, 320f, 14f);
        Pill(String l) { label = l; }
    }
    private final List<Pill> pills = new ArrayList<>();

    // ---------------------------------------------------------------- announcement + CTAs
    private int annX, annY, annW, annH;
    private boolean annHover;
    private int b1X, b1Y, b1W, b1H, b2X, b2Y, b2W, b2H;
    private boolean b1Hover, b2Hover, b1Press;
    private final Elastic b1Hov = new Elastic(0f, 260f, 20f);
    private final Elastic b2Hov = new Elastic(0f, 260f, 20f);
    private final Elastic magX = new Elastic(0f, 170f, 14f);
    private final Elastic magY = new Elastic(0f, 170f, 14f);

    // ---------------------------------------------------------------- typewriter
    private final String[] words = {"teammates.", "developers.", "designers.", "researchers.", "co-founders."};
    private int wordIdx = 0;
    private float typed = 0f;     // characters shown (fractional)
    private int phase = 0;        // 0 typing, 1 hold, 2 deleting
    private float phaseT = 0f;

    // ---------------------------------------------------------------- particle network
    private static class Node {
        float x, y, vx, vy, r;
        int color;
    }
    private final List<Node> nodes = new ArrayList<>();
    private int fieldW = -1;
    private static final int FIELD_H = 760;
    private int lastSectionY = 0;

    // ---------------------------------------------------------------- app preview
    public static class Member {
        final String name, role, initials;
        final String[] skills;
        final int match;
        final Color c1, c2;
        int x, y, w, h;          // window-local (untransformed) bounds
        int bx, by, bw, bh;      // invite button
        boolean hovered, btnHovered, invited;
        final Elastic lift = new Elastic(0f, 220f, 18f);
        final Elastic ring = new Elastic(0f, 60f, 11f);

        Member(String n, String r, String i, String[] s, int m, Color c1, Color c2) {
            name = n; role = r; initials = i; skills = s; match = m; this.c1 = c1; this.c2 = c2;
        }
    }
    private final List<Member> members = new ArrayList<>();
    private int visibleMembers = 3;

    private int winX, winY, winW, winH;
    private int dashBtnX, dashBtnY, dashBtnW, dashBtnH;
    private boolean dashBtnHovered;
    private final Elastic dashBtnHov = new Elastic(0f, 260f, 20f);
    private final Elastic tiltX = new Elastic(0f, 70f, 12f);
    private final Elastic tiltY = new Elastic(0f, 70f, 12f);
    private AffineTransform winTx = new AffineTransform();
    private boolean mouseOverWin;

    private final String[][] activity = {
            {"SL", "Sarah Lin", "accepted your invite"},
            {"TA", "Tanvir", "matched 95% for ML role"},
            {"NR", "Nila", "shipped the onboarding flow"},
            {"PP", "Priya", "moved API auth to Done"},
            {"DC", "David", "left a 5-star review"},
            {"MV", "Marcus", "joined Campus Connect"},
    };
    private float activityT = 0f;
    private int activityHead = 0;

    // ---------------------------------------------------------------- marquee
    private final String[] marquee = {
            "Java Full-Stack", "UI/UX Design", "React Native", "Data Science", "Cloud & DevOps",
            "PyTorch / AI", "PostgreSQL", "System Architecture", "Figma", "Cybersecurity",
            "Product Strategy", "Flutter", "Motion Design", "Research"
    };
    private float marqueeOff = 0f;
    private boolean marqueeHover;
    private int marqueeY = -1000, marqueeH = 0;

    private final Random rnd = new Random(7);

    public Hero(NavBar.NavListener listener) {
        this.listener = listener;
        searchBox = new TextBox("Search by skill — Java, UI/UX, Python...");
        searchBox.bare = true;

        for (String s : new String[]{"Java", "UI/UX", "Python", "React", "MySQL", "Figma"}) pills.add(new Pill(s));

        members.add(new Member("Sarah Lin", "Backend Engineer", "SL", new String[]{"Java", "Spring", "Docker"}, 96,
                Fx.ORANGE, Fx.PINK));
        members.add(new Member("David Chen", "Product Designer", "DC", new String[]{"Figma", "UI/UX", "Motion"}, 92,
                Fx.VIOLET, new Color(0x6E, 0xC1, 0xFF)));
        members.add(new Member("Tanvir Ahmed", "ML Researcher", "TA", new String[]{"Python", "NLP", "PyTorch"}, 89,
                Fx.MINT, Fx.CYAN));
    }

    /** Called with the search query when the user searches from the hero. */
    public void setOnSearch(Consumer<String> c) { this.onSearch = c; }

    private void submitSearch() {
        String q = searchBox.getText().trim();
        if (onSearch != null) onSearch.accept(q);
        else if (listener != null) listener.scrollToSection("demo");
    }

    public boolean hasFocus() { return searchBox.focused; }

    @Override
    public String getId() { return "hero"; }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Override
    public void update(float dt) {
        super.update(dt);
        searchBox.update(dt);

        // Typewriter
        String w = words[wordIdx];
        phaseT += dt;
        if (!Theme.motionEnabled) {
            typed = w.length();
        } else if (phase == 0) {
            typed = Math.min(w.length(), typed + dt / 0.065f);
            if (typed >= w.length()) { phase = 1; phaseT = 0f; }
        } else if (phase == 1) {
            if (phaseT > 2.2f) { phase = 2; phaseT = 0f; }
        } else {
            typed = Math.max(0f, typed - dt / 0.032f);
            if (typed <= 0f) {
                phase = 0; phaseT = 0f;
                wordIdx = (wordIdx + 1) % words.length;
            }
        }

        for (Pill p : pills) {
            p.jump.setTarget(p.hovered ? -6f : 0f);
            p.jump.update(dt);
        }

        b1Hov.setTarget(b1Hover ? 1f : 0f); b1Hov.update(dt);
        b2Hov.setTarget(b2Hover ? 1f : 0f); b2Hov.update(dt);
        sBtnHov.setTarget(sBtnHover ? 1f : 0f); sBtnHov.update(dt);
        dashBtnHov.setTarget(dashBtnHovered ? 1f : 0f); dashBtnHov.update(dt);

        float[] mag = Anim.calculateMagneticOffset(b1X + b1W / 2f, b1Y + b1H / 2f, 110f, 9f);
        magX.setTarget(mag[0]); magY.setTarget(mag[1]);
        magX.update(dt); magY.update(dt);

        // Window tilt follows the cursor while it is near the preview
        if (mouseOverWin && Theme.motionEnabled) {
            float nx = (Anim.mouseX - (winX + winW / 2f)) / Math.max(1f, winW / 2f);
            float ny = (Anim.mouseY - (winY + winH / 2f)) / Math.max(1f, winH / 2f);
            tiltX.setTarget(Fx.clamp(nx, -1f, 1f));
            tiltY.setTarget(Fx.clamp(ny, -1f, 1f));
        } else {
            tiltX.setTarget(0f);
            tiltY.setTarget(0f);
        }
        tiltX.update(dt);
        tiltY.update(dt);

        boolean winVisible = rv.p(0.55f, 1f) > 0.3f;
        for (Member m : members) {
            m.lift.setTarget(m.hovered ? 1f : 0f);
            m.lift.update(dt);
            m.ring.setTarget(winVisible ? m.match / 100f : 0f);
            m.ring.update(dt);
        }

        activityT += dt;
        if (activityT > 2.8f && Theme.motionEnabled) {
            activityT = 0f;
            activityHead = (activityHead + 1) % activity.length;
        }

        if (Theme.motionEnabled) marqueeOff += (marqueeHover ? 18f : 46f) * dt;

        updateParticles(dt);
    }

    private void updateParticles(float dt) {
        if (fieldW <= 0 || !Theme.motionEnabled) return;
        float mx = Anim.mouseX, my = Anim.mouseY - lastSectionY;
        for (Node n : nodes) {
            n.x += n.vx * dt;
            n.y += n.vy * dt;
            float dx = n.x - mx, dy = n.y - my;
            float d2 = dx * dx + dy * dy;
            if (d2 < 150f * 150f && d2 > 1f) {
                float d = (float) Math.sqrt(d2);
                float f = (1f - d / 150f) * 70f * dt;
                n.x += dx / d * f;
                n.y += dy / d * f;
            }
            if (n.x < -20) n.x += fieldW + 40;
            if (n.x > fieldW + 20) n.x -= fieldW + 40;
            if (n.y < -20) n.y += FIELD_H + 40;
            if (n.y > FIELD_H + 20) n.y -= FIELD_H + 40;
        }
    }

    private void ensureParticles(int width) {
        if (width == fieldW) return;
        int count = width < 700 ? 34 : (width < 1100 ? 52 : 70);
        if (fieldW > 0 && nodes.size() == count) {
            float sx = width / (float) fieldW;
            for (Node n : nodes) n.x *= sx;
        } else {
            nodes.clear();
            for (int i = 0; i < count; i++) {
                Node n = new Node();
                n.x = rnd.nextFloat() * width;
                n.y = rnd.nextFloat() * FIELD_H;
                float ang = rnd.nextFloat() * 6.283f;
                float sp = 6f + rnd.nextFloat() * 14f;
                n.vx = (float) Math.cos(ang) * sp;
                n.vy = (float) Math.sin(ang) * sp;
                n.r = 1.2f + rnd.nextFloat() * 1.8f;
                n.color = rnd.nextInt(3);
                nodes.add(n);
            }
        }
        fieldW = width;
    }

    // =====================================================================
    // RENDER
    // =====================================================================

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        boolean mobile = width < 700;
        boolean tablet = width < 1100;
        int[] c = Fx.container(width);
        float cx = width / 2f;
        int top = y;

        if (!measuring) {
            lastSectionY = y;
            ensureParticles(width);
            paintBackdrop(g, y, width);
        }

        int cur = y + (mobile ? 40 : 72);

        // 1. Announcement pill -------------------------------------------------
        {
            Graphics2D a = Fx.fade(g, rv.p(0.05f), 16);
            Font f = Fx.font(Font.PLAIN, 13.5f);
            a.setFont(f);
            String msg = mobile ? "Skill-based team matching" : "Skill-based matching for every student team";
            int tw = a.getFontMetrics().stringWidth(msg);
            int h = 36;
            int w = 52 + 12 + tw + 34;
            int x = Math.round(cx - w / 2f);
            if (!measuring) { annX = x; annY = cur; annW = w; annH = h; }
            RoundRectangle2D.Float pill = Fx.rr(x, cur, w, h, h / 2f);
            a.setColor(annHover ? Fx.surfaceHi() : Fx.surface());
            a.fill(pill);
            a.setColor(Fx.border());
            a.setStroke(new BasicStroke(1f));
            a.draw(pill);
            Fx.gradientBorder(a, pill, 1.2f, 0.55f + 0.45f * (annHover ? 1 : 0), Fx.time() * 0.8f);
            Fx.tag(a, "NEW", x + 6, cur + 7, Fx.PINK, true);
            a.setFont(f);
            a.setColor(Fx.text2());
            Fx.textMid(a, msg, x + 58, cur + h / 2f);
            Fx.arrow(a, x + 58 + tw + 10 + (annHover ? 3 : 0), cur + h / 2f, 10, Fx.text2(), 1.6f);
            a.dispose();
            cur += h + (mobile ? 26 : 34);
        }

        // 2. Headline -----------------------------------------------------------
        float hs = mobile ? 42 : (tablet ? 62 : 80);
        Font hf = Fx.display(hs);
        while (hs > 30 && Fx.textW(g, hf, "Build with the right") > c[1]) { hs -= 2; hf = Fx.display(hs); }
        g.setFont(hf);
        FontMetrics hfm = g.getFontMetrics();
        int lineH = Math.round(hfm.getAscent() * 1.12f);
        {
            String l1 = "Build with the right";
            String[] parts = l1.split(" ");
            int lw = hfm.stringWidth(l1);
            float x = cx - lw / 2f;
            for (int i = 0; i < parts.length; i++) {
                String word = parts[i] + (i < parts.length - 1 ? " " : "");
                Graphics2D wg = Fx.fade(g, rv.p(0.12f + i * 0.07f, 0.9f), 34);
                wg.setFont(hf);
                wg.setColor(Fx.text());
                wg.drawString(word, x, cur + hfm.getAscent());
                wg.dispose();
                x += hfm.stringWidth(word);
            }
            cur += lineH;
        }
        {
            // Typewriter gradient word, left-anchored to the widest word so it doesn't jitter
            int maxW = 0;
            for (String w : words) maxW = Math.max(maxW, hfm.stringWidth(w));
            String full = words[wordIdx];
            int n = Math.max(0, Math.min(full.length(), (int) Math.floor(typed)));
            String shown = full.substring(0, n);
            int fullW = hfm.stringWidth(full);
            float x = cx - fullW / 2f;
            float base = cur + hfm.getAscent();
            Graphics2D wg = Fx.fade(g, rv.p(0.4f, 0.9f), 34);
            wg.setFont(hf);
            float shift = Fx.time() * 0.15f;
            wg.setPaint(new LinearGradientPaint(new Point2D.Float(x - shift * fullW, base - hfm.getAscent()),
                    new Point2D.Float(x - shift * fullW + fullW + 1, base),
                    new float[]{0f, 0.5f, 1f}, Fx.BRAND, MultipleGradientPaint.CycleMethod.REFLECT));
            wg.drawString(shown, x, base);
            int sw = hfm.stringWidth(shown);
            // caret
            boolean blink = phase != 1 || ((int) (Fx.time() * 2.2f)) % 2 == 0;
            if (blink && Theme.motionEnabled) {
                wg.setPaint(new GradientPaint(0, base - hfm.getAscent() * 0.8f, Fx.PINK, 0, base, Fx.VIOLET));
                wg.fill(Fx.rr(x + sw + 6, base - hfm.getAscent() * 0.78f, Math.max(3, hs * 0.06f), hfm.getAscent() * 0.9f, 2));
            }
            // underline swash that grows with the typed word
            if (sw > 8) paintSwash(wg, x, base + hs * 0.16f, sw);
            wg.dispose();
            cur += lineH + (mobile ? 18 : 26);
        }

        // 3. Subtitle -----------------------------------------------------------
        {
            int maxW = Math.min(660, c[1]);
            Graphics2D s = Fx.fade(g, rv.p(0.55f), 24);
            cur = Fx.paragraph(s, "TeamUp matches students, developers, freelancers and researchers by verified skill - so you spend less time searching and more time building what matters.",
                    Fx.body(mobile ? 16 : 19), Fx.text2(), Math.round(cx - maxW / 2f), cur, maxW, 1.5f, true);
            s.dispose();
            cur += mobile ? 26 : 34;
        }

        // 4. Search bar ---------------------------------------------------------
        {
            Graphics2D s = Fx.fade(g, rv.p(0.68f), 24);
            int w = Math.min(640, c[1]);
            int h = mobile ? 54 : 62;
            int x = Math.round(cx - w / 2f);
            int bw = mobile ? 96 : 124, bh = h - 12;
            if (!measuring) {
                sbX = x; sbY = cur; sbW = w; sbH = h;
                sBtnX = x + w - bw - 6; sBtnY = cur + 6; sBtnW = bw; sBtnH = bh;
            }
            RoundRectangle2D.Float bar = Fx.rr(x, cur, w, h, h / 2f);
            if (searchBox.focused) Fx.glowOval(s, cx, cur + h / 2f, w * 0.62f, h * 1.6f, Fx.VIOLET, 0.35f);
            Fx.softShadow(s, x, cur, w, h, h / 2f, Fx.dark() ? 0.6f : 0.25f, 20);
            s.setColor(Fx.solid());
            s.fill(bar);
            s.setColor(Fx.surface());
            s.fill(bar);
            s.setColor(Fx.border());
            s.setStroke(new BasicStroke(1f));
            s.draw(bar);
            if (searchBox.focused) Fx.gradientBorder(s, bar, 1.6f, 1f, Fx.time() * 0.6f);
            else Fx.spotlightBorder(s, bar, Fx.VIOLET, 0.8f);

            Draw.drawSearchIcon(s, x + 18, cur + h / 2 - 11, 22, Fx.text3());
            searchBox.setBounds(x + 50, cur + 6, w - 50 - bw - 16, h - 12);
            searchBox.paint(s);
            Fx.primaryButton(s, sBtnX, sBtnY, bw, bh, "Search", sBtnHov.getValue(), 0f, false);
            s.dispose();
            cur += h + 18;
        }

        // Popular pills
        {
            Graphics2D s = Fx.fade(g, rv.p(0.76f), 18);
            Font pf = Fx.font(Font.PLAIN, 13);
            Font lf = Fx.bold(13);
            s.setFont(pf);
            FontMetrics pfm = s.getFontMetrics();
            String lbl = "Popular:";
            int total = s.getFontMetrics(lf).stringWidth(lbl) + 10;
            int shown = mobile ? 4 : pills.size();
            for (int i = 0; i < shown; i++) total += pfm.stringWidth(pills.get(i).label) + 26 + 8;
            int x = Math.round(cx - total / 2f);
            s.setFont(lf);
            s.setColor(Fx.text3());
            Fx.textMid(s, lbl, x, cur + 15);
            x += s.getFontMetrics().stringWidth(lbl) + 10;
            s.setFont(pf);
            for (int i = 0; i < pills.size(); i++) {
                Pill p = pills.get(i);
                if (i >= shown) { if (!measuring) p.x = -1000; continue; }
                int pw = pfm.stringWidth(p.label) + 26;
                int py = Math.round(cur + p.jump.getValue());
                if (!measuring) { p.x = x; p.y = cur; p.w = pw; p.h = 30; }
                RoundRectangle2D.Float shape = Fx.rr(x, py, pw, 30, 15);
                if (p.hovered) {
                    Fx.glow(s, x + pw / 2f, py + 15, pw * 0.8f, Fx.PINK, 0.25f);
                    s.setPaint(new GradientPaint(x, py, Fx.a(Fx.PINK, 50), x + pw, py, Fx.a(Fx.VIOLET, 50)));
                } else {
                    s.setColor(Fx.surface());
                }
                s.fill(shape);
                s.setColor(p.hovered ? Fx.a(Fx.PINK, 140) : Fx.border());
                s.draw(shape);
                s.setColor(p.hovered ? Fx.text() : Fx.text2());
                Fx.textMid(s, p.label, x + 13, py + 15);
                x += pw + 8;
            }
            s.dispose();
            cur += 30 + (mobile ? 28 : 36);
        }

        // 5. CTAs ---------------------------------------------------------------
        {
            int w1 = mobile ? Math.min(300, c[1]) : 230, w2 = mobile ? Math.min(300, c[1]) : 196, h = 54;
            int x1, x2, y1, y2;
            if (mobile) {
                x1 = x2 = Math.round(cx - w1 / 2f);
                y1 = cur; y2 = cur + h + 12;
            } else {
                int tw = w1 + 14 + w2;
                x1 = Math.round(cx - tw / 2f); x2 = x1 + w1 + 14;
                y1 = y2 = cur;
            }
            if (!measuring) {
                b1X = x1; b1Y = y1; b1W = w1; b1H = h;
                b2X = x2; b2Y = y2; b2W = w2; b2H = h;
            }
            Graphics2D b = Fx.fade(g, rv.p(0.84f), 20);
            Fx.primaryButton(b, x1 + magX.getValue(), y1 + magY.getValue(), w1, h, "Get started - it's free",
                    b1Hov.getValue(), b1Press ? 1f : 0f, true);
            Fx.ghostButton(b, x2, y2, w2, h, "See how it works", b2Hov.getValue(), false);
            b.dispose();
            cur = (mobile ? y2 : y1) + h + 30;
        }

        // 6. Social proof -------------------------------------------------------
        {
            Graphics2D s = Fx.fade(g, rv.p(0.95f), 16);
            String txt = mobile ? "Built for builders" : "Built for students, developers & researchers";
            s.setFont(Fx.font(Font.PLAIN, 13.5f));
            int tw = s.getFontMetrics().stringWidth(txt);
            int avs = 32, overlap = 6, count = 5;
            int stackW = avs + (count - 1) * (avs - overlap);
            int starsW = 5 * 15;
            int total = stackW + 14 + starsW + 10 + tw;
            float x = cx - total / 2f;
            String[] ini = {"SL", "DC", "PP", "MV", "NR"};
            Color[][] cols = {{Fx.ORANGE, Fx.PINK}, {Fx.VIOLET, Fx.CYAN}, {Fx.MINT, Fx.CYAN}, {Fx.AMBER, Fx.ORANGE}, {Fx.PINK, Fx.VIOLET}};
            for (int i = 0; i < count; i++) {
                float ax = x + i * (avs - overlap);
                s.setColor(Fx.bg());
                s.fill(new Ellipse2D.Float(ax - 2, cur - 2, avs + 4, avs + 4));
                Draw.drawAvatar(s, Math.round(ax), cur, avs, ini[i], cols[i][0], cols[i][1]);
            }
            x += stackW + 14;
            for (int i = 0; i < 5; i++) Draw.drawStarIcon(s, Math.round(x + i * 15), cur + 8, 14, Fx.AMBER);
            x += starsW + 10;
            s.setColor(Fx.text3());
            s.setFont(Fx.font(Font.PLAIN, 13.5f));
            Fx.textMid(s, txt, x, cur + avs / 2f);
            s.dispose();
            cur += avs + (mobile ? 44 : 64);
        }

        // 7. App preview window -------------------------------------------------
        cur = paintPreview(g, cur, width, c, mobile, tablet);

        // 8. Marquee ------------------------------------------------------------
        cur += mobile ? 48 : 72;
        cur = paintMarquee(g, cur, width);

        return cur - top + 40;
    }

    // ---------------------------------------------------------------- backdrop
    private void paintBackdrop(Graphics2D g, int y, int width) {
        // Hero spotlight beams
        float k = Fx.dark() ? 1f : 0.55f;
        Fx.glowOval(g, width / 2f, y + 120, width * 0.45f, 330, Fx.VIOLET, 0.22f * k);
        Fx.glowOval(g, width * 0.32f, y + 40, width * 0.25f, 220, Fx.PINK, 0.16f * k);
        Fx.glowOval(g, width * 0.7f, y + 60, width * 0.22f, 200, Fx.CYAN, 0.10f * k);

        // Particle network
        Color[] pal = {Fx.PINK, Fx.VIOLET, Fx.CYAN};
        float fieldAlpha = Fx.clamp(rv.p(0f, 1.6f));
        if (fieldAlpha <= 0.01f) return;
        Graphics2D p = (Graphics2D) g.create();
        Fx.fadeBy(p, fieldAlpha * (Fx.dark() ? 1f : 0.75f));
        p.setStroke(new BasicStroke(1f));
        float link = width < 700 ? 95f : 130f;
        float link2 = link * link;
        int n = nodes.size();
        float mx = Anim.mouseX, my = Anim.mouseY;
        Color lineC = Fx.dark() ? new Color(180, 170, 255) : new Color(110, 80, 200);
        for (int i = 0; i < n; i++) {
            Node a = nodes.get(i);
            float ay = a.y + y;
            for (int j = i + 1; j < n; j++) {
                Node b = nodes.get(j);
                float dx = a.x - b.x, dy = a.y - b.y;
                float d2 = dx * dx + dy * dy;
                if (d2 < link2) {
                    float t = 1f - (float) Math.sqrt(d2) / link;
                    p.setColor(Fx.a(lineC, Math.round(70 * t * t)));
                    p.draw(new Line2D.Float(a.x, ay, b.x, b.y + y));
                }
            }
            // connect to cursor
            float cdx = a.x - mx, cdy = ay - my;
            float cd2 = cdx * cdx + cdy * cdy;
            if (cd2 < 190f * 190f) {
                float t = 1f - (float) Math.sqrt(cd2) / 190f;
                p.setColor(Fx.a(Fx.PINK, Math.round(150 * t)));
                p.draw(new Line2D.Float(a.x, ay, mx, my));
            }
        }
        for (Node a : nodes) {
            Color col = pal[a.color];
            p.setColor(Fx.a(col, 200));
            p.fill(new Ellipse2D.Float(a.x - a.r, a.y + y - a.r, a.r * 2, a.r * 2));
        }
        // fade the field out towards the bottom so it melts into the preview
        p.setComposite(AlphaComposite.SrcOver);
        p.dispose();
    }

    private void paintSwash(Graphics2D g, float x, float y, float w) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(x, y + 6);
        path.curveTo(x + w * 0.3f, y - 4, x + w * 0.62f, y + 10, x + w, y);
        Graphics2D s = (Graphics2D) g.create();
        s.setPaint(new LinearGradientPaint(x, y, x + w + 1, y, new float[]{0f, 0.5f, 1f},
                new Color[]{Fx.a(Fx.PINK, 220), Fx.a(Fx.VIOLET, 200), Fx.a(Fx.CYAN, 120)}));
        s.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        s.draw(path);
        s.dispose();
    }

    // ---------------------------------------------------------------- preview window
    private int paintPreview(Graphics2D g, int cur, int width, int[] c, boolean mobile, boolean tablet) {
        int ww = Math.min(1080, c[1]);
        boolean showSide = ww >= 860;
        boolean showFeed = ww >= 1000;
        visibleMembers = ww < 560 ? 1 : (ww < 760 ? 2 : 3);
        int memberH = 214;
        int contentH = 44 + 26 + 86 + 18 + 26 + memberH + 24;
        int wh = contentH;
        int wx = Math.round(width / 2f - ww / 2f);
        int wy = cur;
        if (!measuring) { winX = wx; winY = wy; winW = ww; winH = wh; }
        if (measuring) return cur + wh;

        float ent = rv.p(0.95f, 1.2f);

        // Halo behind window
        float k = Fx.dark() ? 1f : 0.6f;
        Fx.glowOval(g, width / 2f, wy + wh * 0.35f, ww * 0.62f, wh * 0.75f, Fx.VIOLET, 0.32f * k * ent);
        Fx.glowOval(g, width / 2f - ww * 0.25f, wy + wh * 0.2f, ww * 0.3f, wh * 0.45f, Fx.PINK, 0.22f * k * ent);
        Fx.glowOval(g, width / 2f + ww * 0.3f, wy + wh * 0.5f, ww * 0.3f, wh * 0.45f, Fx.CYAN, 0.14f * k * ent);

        // Rise-in entrance + cursor parallax. Translation only: rotating/scaling text
        // every frame defeats Java2D's glyph cache and costs ~20ms per frame.
        AffineTransform tx = new AffineTransform();
        tx.translate(Math.round(tiltX.getValue() * 10f), Math.round((1f - ent) * 70f + tiltY.getValue() * 7f));
        winTx = tx;

        Graphics2D w = (Graphics2D) g.create();
        Fx.fadeBy(w, Fx.clamp(ent * 1.4f));
        w.transform(tx);

        RoundRectangle2D.Float frame = Fx.rr(wx, wy, ww, wh, 20);
        Fx.softShadow(w, wx, wy, ww, wh, 20, Fx.dark() ? 0.9f : 0.35f, 40);
        w.setColor(Fx.dark() ? new Color(0x0D, 0x0D, 0x1E) : new Color(0xFD, 0xFC, 0xFF));
        w.fill(frame);
        w.setPaint(new GradientPaint(0, wy, Fx.a(Fx.VIOLET, Fx.dark() ? 18 : 8), 0, wy + wh, Fx.a(Fx.PINK, 0)));
        w.fill(frame);
        Fx.gradientBorder(w, frame, 1.2f, 0.65f, Fx.time() * 0.35f);

        // Title bar
        Graphics2D clip = (Graphics2D) w.create();
        clip.clip(frame);
        clip.setColor(Fx.subtle());
        clip.fillRect(wx, wy, ww, 44);
        clip.setColor(Fx.border());
        clip.drawLine(wx, wy + 44, wx + ww, wy + 44);
        clip.dispose();
        Color[] dots = {new Color(0xFF, 0x5F, 0x57), new Color(0xFE, 0xBC, 0x2E), new Color(0x28, 0xC8, 0x40)};
        for (int i = 0; i < 3; i++) {
            w.setColor(dots[i]);
            w.fill(new Ellipse2D.Float(wx + 18 + i * 18, wy + 17, 10, 10));
        }
        int urlW = Math.min(340, ww - 200);
        if (urlW > 120) {
            int ux = wx + ww / 2 - urlW / 2;
            w.setColor(Fx.subtle());
            w.fill(Fx.rr(ux, wy + 10, urlW, 24, 8));
            Fx.lock(w, ux + 10, wy + 15, 13, Fx.text3(), 1f);
            w.setFont(Fx.font(Font.PLAIN, 12));
            w.setColor(Fx.text3());
            Fx.textMid(w, "teamup.app/projects/campus-connect", ux + 30, wy + 22);
        }

        int bodyY = wy + 44;
        int sideW = showSide ? 196 : 0;
        if (showSide) paintSidebar(w, wx, bodyY, sideW, wh - 44);

        int feedW = showFeed ? 250 : 0;
        int mainX = wx + sideW + 24;
        int mainW = ww - sideW - feedW - 48 - (showFeed ? 0 : 0);

        // Project header
        int py = bodyY + 26;
        w.setFont(Fx.heading(20));
        w.setColor(Fx.text());
        w.drawString("Campus Connect", mainX, py + 18);
        int ttw = w.getFontMetrics().stringWidth("Campus Connect");
        Fx.tag(w, "Recruiting", mainX + ttw + 12, py + 1, Fx.MINT, false);
        w.setFont(Fx.font(Font.PLAIN, 13));
        w.setColor(Fx.text3());
        w.drawString("Mobile app  -  4 roles  -  Sprint 3 of 6", mainX, py + 42);

        // progress card
        int pcW = Math.min(240, mainW / 2 - 10);
        if (mainW > 420) {
            int pcx2 = mainX + mainW - pcW;
            w.setFont(Fx.bold(12));
            w.setColor(Fx.text2());
            w.drawString("Team assembled", pcx2, py + 12);
            String pct = "3 / 4";
            w.setColor(Fx.text());
            w.drawString(pct, pcx2 + pcW - w.getFontMetrics().stringWidth(pct), py + 12);
            w.setColor(Fx.subtle());
            w.fill(Fx.rr(pcx2, py + 22, pcW, 8, 4));
            float prog = 0.75f * Fx.easeOutQuart(rv.lin(1.4f, 1.4f));
            w.setPaint(new GradientPaint(pcx2, 0, Fx.PINK, pcx2 + pcW, 0, Fx.VIOLET));
            w.fill(Fx.rr(pcx2, py + 22, pcW * prog, 8, 4));
            w.setFont(Fx.font(Font.PLAIN, 11.5f));
            w.setColor(Fx.text3());
            w.drawString("1 role left: Mobile (Flutter)", pcx2, py + 46);
        }

        // Section label
        int ly = py + 86;
        w.setFont(Fx.label(11));
        w.setColor(Fx.text3());
        w.drawString("RECOMMENDED TEAMMATES", mainX, ly);
        w.setFont(Fx.font(Font.PLAIN, 12));
        String sorted = "Sorted by skill match";
        w.drawString(sorted, mainX + mainW - w.getFontMetrics().stringWidth(sorted), ly);

        // Member cards
        int gy = ly + 16;
        int gap = 14;
        int mw = (mainW - gap * (visibleMembers - 1)) / visibleMembers;
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            if (i >= visibleMembers) { m.x = -10000; continue; }
            m.x = mainX + i * (mw + gap);
            m.y = gy;
            m.w = mw;
            m.h = memberH;
            float appear = rv.p(1.25f + i * 0.12f, 0.8f);
            Graphics2D mg = Fx.fade(w, appear, 20);
            paintMember(mg, m);
            mg.dispose();
        }

        // Bottom preview bar: interactive hint + "Explore full dashboard ->" button
        {
            int footY = gy + memberH + 12;
            String ctaTxt = "Explore full dashboard";
            w.setFont(Fx.bold(12.5f));
            FontMetrics cfm = w.getFontMetrics();
            dashBtnW = cfm.stringWidth(ctaTxt) + 42;
            dashBtnH = 30;
            dashBtnX = mainW > 380 ? (mainX + mainW - dashBtnW) : (mainX + (mainW - dashBtnW) / 2);
            dashBtnY = footY;

            if (mainW > 420) {
                w.setFont(Fx.font(Font.PLAIN, 11.5f));
                w.setColor(Fx.text3());
                Fx.textMid(w, "Interactive preview · Try clicking Invite", mainX, footY + dashBtnH / 2f);
            }

            float hv = dashBtnHov.getValue();
            RoundRectangle2D.Float db = Fx.rr(dashBtnX, dashBtnY, dashBtnW, dashBtnH, 15);
            if (hv > 0.01f) {
                Fx.glow(w, dashBtnX + dashBtnW / 2f, dashBtnY + dashBtnH / 2f, dashBtnW * 0.55f, Fx.PINK, 0.25f * hv);
            }
            w.setPaint(new GradientPaint(dashBtnX, dashBtnY, Fx.mix(Fx.a(Fx.PINK, 26), Fx.PINK, hv),
                    dashBtnX + dashBtnW, dashBtnY, Fx.mix(Fx.a(Fx.VIOLET, 26), Fx.VIOLET, hv)));
            w.fill(db);
            w.setColor(Fx.mix(Fx.a(Fx.PINK, 110), Color.WHITE, hv * 0.4f));
            w.setStroke(new BasicStroke(1f));
            w.draw(db);

            w.setFont(Fx.bold(12.5f));
            w.setColor(hv > 0.45f ? Color.WHITE : Fx.ink(Fx.PINK));
            int tw = cfm.stringWidth(ctaTxt);
            Fx.textMid(w, ctaTxt, dashBtnX + 14, dashBtnY + dashBtnH / 2f);
            Fx.arrow(w, dashBtnX + 14 + tw + 10 + hv * 2f, dashBtnY + dashBtnH / 2f, 9,
                    hv > 0.45f ? Color.WHITE : Fx.ink(Fx.PINK), 1.6f);
        }

        if (showFeed) paintFeed(w, wx + ww - feedW, bodyY, feedW, wh - 44);

        if (mouseOverWin) {
            Point2D mp = toWindow(Math.round(Anim.mouseX), Math.round(Anim.mouseY));
            Graphics2D gl = (Graphics2D) w.create();
            gl.clipRect(wx + 6, wy + 6, ww - 12, wh - 12);
            Fx.glow(gl, (float) mp.getX(), (float) mp.getY(), 300, Fx.dark() ? Color.WHITE : Fx.VIOLET, Fx.dark() ? 0.05f : 0.06f);
            gl.dispose();
        }

        w.dispose();

        // Floating badges (outside the transform so they feel layered)
        if (width >= 1000) {
            float bob1 = Fx.wave(1.3f, 0) * 7f;
            float bob2 = Fx.wave(1.1f, 2f) * 8f;
            Graphics2D b1 = Fx.fade(g, rv.p(1.7f, 0.8f), 20);
            paintBadgeTeam(b1, wx + ww - 250, wy - 34 + bob1);
            b1.dispose();
            Graphics2D b2 = Fx.fade(g, rv.p(1.9f, 0.8f), 20);
            paintBadgeMatch(b2, Math.max(12, wx - 64), wy + wh - 30 + bob2);
            b2.dispose();
        }
        return cur + wh;
    }

    private void paintSidebar(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Fx.subtle());
        g.fill(new Rectangle2D.Float(x, y, w, h - 20));
        g.fill(Fx.rr(x, y + h - 40, w, 40, 20));
        g.setColor(Fx.border());
        g.drawLine(x + w, y, x + w, y + h);

        int cy = y + 22;
        Draw.drawLogo(g, x + 18, cy, 26);
        g.setFont(Fx.heading(15));
        g.setColor(Fx.text());
        Fx.textMid(g, "TeamUp", x + 52, cy + 13);
        cy += 48;

        String[][] items = {{"Overview", "chart"}, {"Projects", "task"}, {"Teammates", "users"}, {"Reviews", "star"}};
        for (int i = 0; i < items.length; i++) {
            boolean act = i == 1;
            if (act) {
                g.setPaint(new GradientPaint(x + 10, 0, Fx.a(Fx.PINK, Fx.dark() ? 46 : 30), x + w - 10, 0, Fx.a(Fx.VIOLET, 10)));
                g.fill(Fx.rr(x + 10, cy, w - 20, 34, 9));
                g.setColor(Fx.PINK);
                g.fill(Fx.rr(x + 10, cy + 9, 3, 16, 1.5f));
            }
            Fx.icon(g, items[i][1], x + 24, cy + 9, 16, act ? Fx.ink(Fx.PINK) : Fx.text3());
            g.setFont(act ? Fx.bold(13) : Fx.font(Font.PLAIN, 13));
            g.setColor(act ? Fx.text() : Fx.text2());
            Fx.textMid(g, items[i][0], x + 50, cy + 17);
            if (i == 3) {
                g.setColor(Fx.PINK);
                g.fill(Fx.rr(x + w - 40, cy + 9, 22, 16, 8));
                g.setColor(Color.WHITE);
                g.setFont(Fx.bold(10.5f));
                Fx.textCenter(g, "3", x + w - 29, cy + 17);
            }
            cy += 40;
        }

        // Upgrade card
        int cardY = y + h - 104;
        if (cardY > cy + 10) {
            RoundRectangle2D.Float card = Fx.rr(x + 12, cardY, w - 24, 84, 12);
            g.setPaint(new GradientPaint(x, cardY, Fx.a(Fx.VIOLET, 60), x + w, cardY + 84, Fx.a(Fx.PINK, 40)));
            g.fill(card);
            g.setColor(Fx.a(Color.WHITE, 30));
            g.draw(card);
            g.setFont(Fx.bold(12.5f));
            g.setColor(Fx.text());
            g.drawString("Hackathon mode", x + 26, cardY + 26);
            g.setFont(Fx.font(Font.PLAIN, 11.5f));
            g.setColor(Fx.text2());
            g.drawString("Form a team in 48h", x + 26, cardY + 44);
            Fx.arrow(g, x + 26, cardY + 64, 12, Fx.ink(Fx.PINK), 1.8f);
        }
    }

    private void paintMember(Graphics2D g, Member m) {
        float lift = m.lift.getValue();
        float y = m.y - lift * 6f;
        RoundRectangle2D.Float card = Fx.rr(m.x, y, m.w, m.h, 14);

        if (lift > 0.01f) Fx.glow(g, m.x + m.w / 2f, y + m.h / 2f, m.w * 0.8f, m.c2, 0.18f * lift);
        g.setColor(Fx.dark() ? new Color(0x14, 0x14, 0x28) : Color.WHITE);
        g.fill(card);
        g.setColor(Fx.surface());
        g.fill(card);
        g.setColor(Fx.mix(Fx.border(), Fx.a(m.c2, 150), lift));
        g.setStroke(new BasicStroke(1f));
        g.draw(card);

        int px = m.x + 16;
        float py = y + 16;
        Fx.avatar(g, px, py, 42, m.initials, m.c1, m.c2, true);

        // match ring
        float rs = 44;
        float rx = m.x + m.w - 16 - rs, ry = py - 1;
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Fx.subtle());
        g.draw(new Ellipse2D.Float(rx, ry, rs, rs));
        float v = m.ring.getValue();
        g.setPaint(new GradientPaint(rx, ry, m.c1, rx + rs, ry + rs, m.c2));
        g.draw(new Arc2D.Float(rx, ry, rs, rs, 90, -360f * v, Arc2D.OPEN));
        g.setFont(Fx.bold(11.5f));
        g.setColor(Fx.text());
        Fx.textCenter(g, Math.round(v * 100) + "%", rx + rs / 2f, ry + rs / 2f);

        py += 60;
        g.setFont(Fx.bold(15));
        g.setColor(Fx.text());
        g.drawString(m.name, px, py + 4);
        g.setFont(Fx.font(Font.PLAIN, 12.5f));
        g.setColor(Fx.text3());
        g.drawString(m.role, px, py + 23);

        // skills
        float sx = px;
        float sy = py + 36;
        g.setFont(Fx.font(Font.PLAIN, 11.5f));
        FontMetrics fm = g.getFontMetrics();
        for (String s : m.skills) {
            int sw = fm.stringWidth(s) + 16;
            if (sx + sw > m.x + m.w - 12) break;
            g.setColor(Fx.subtle());
            g.fill(Fx.rr(sx, sy, sw, 22, 11));
            g.setColor(Fx.text2());
            Fx.textMid(g, s, sx + 8, sy + 11);
            sx += sw + 6;
        }

        // invite button
        int bh = 34;
        int bx = m.x + 16, bw = m.w - 32;
        int by = Math.round(y + m.h - bh - 16);
        m.bx = bx; m.by = by; m.bw = bw; m.bh = bh;
        RoundRectangle2D.Float btn = Fx.rr(bx, by, bw, bh, 10);
        if (m.invited) {
            g.setColor(Fx.a(Fx.MINT, Fx.dark() ? 36 : 30));
            g.fill(btn);
            g.setColor(Fx.a(Fx.MINT, 120));
            g.draw(btn);
            g.setFont(Fx.bold(13));
            int tw = g.getFontMetrics().stringWidth("Invite sent");
            float tx = bx + (bw - tw - 20) / 2f;
            Draw.drawCheckIcon(g, Math.round(tx), by + 8, 18, Fx.ink(Fx.MINT));
            g.setColor(Fx.ink(Fx.MINT));
            Fx.textMid(g, "Invite sent", tx + 22, by + bh / 2f);
        } else if (m.btnHovered || lift > 0.5f) {
            g.setPaint(new GradientPaint(bx, by, Fx.PINK, bx + bw, by, Fx.VIOLET));
            g.fill(btn);
            g.setColor(Color.WHITE);
            g.setFont(Fx.bold(13));
            Fx.textCenter(g, "Invite to team", bx + bw / 2f, by + bh / 2f);
        } else {
            g.setColor(Fx.subtle());
            g.fill(btn);
            g.setColor(Fx.border());
            g.draw(btn);
            g.setColor(Fx.text());
            g.setFont(Fx.bold(13));
            Fx.textCenter(g, "Invite to team", bx + bw / 2f, by + bh / 2f);
        }
    }

    private void paintFeed(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Fx.border());
        g.drawLine(x, y + 16, x, y + h - 16);
        int px = x + 20;
        int cy = y + 28;
        g.setFont(Fx.label(11));
        g.setColor(Fx.text3());
        g.drawString("LIVE ACTIVITY", px, cy + 10);
        float pulse = (Fx.time() % 1.6f) / 1.6f;
        Fx.glow(g, x + w - 26, cy + 6, 6 + pulse * 10, Fx.MINT, 0.7f * (1 - pulse));
        g.setColor(Fx.MINT);
        g.fill(new Ellipse2D.Float(x + w - 30, cy + 2, 8, 8));
        cy += 30;

        Color[][] cols = {{Fx.ORANGE, Fx.PINK}, {Fx.MINT, Fx.CYAN}, {Fx.PINK, Fx.VIOLET}, {Fx.VIOLET, Fx.CYAN}, {Fx.VIOLET, new Color(0x6E, 0xC1, 0xFF)}, {Fx.AMBER, Fx.ORANGE}};
        float slide = Fx.easeOutQuart(Math.min(1f, activityT / 0.6f));
        int rowH = 62;
        Graphics2D c = (Graphics2D) g.create();
        c.clipRect(x + 1, cy - 4, w - 2, h - (cy - y) - 10);
        for (int i = 0; i < 5; i++) {
            int idx = ((activityHead - i) % activity.length + activity.length) % activity.length;
            float ry = cy + (i - 1 + slide) * rowH;
            float alpha = i == 0 ? slide : Fx.clamp(1f - (i - 1 + slide) * 0.22f);
            if (alpha <= 0.02f) continue;
            Graphics2D r = (Graphics2D) c.create();
            Fx.fadeBy(r, alpha);
            if (i == 0) Fx.glow(r, px + 120, ry + 24, 120, Fx.VIOLET, 0.12f * slide);
            RoundRectangle2D.Float row = Fx.rr(px - 6, ry, w - 34, rowH - 10, 12);
            r.setColor(i == 0 ? Fx.surfaceHi() : Fx.subtle());
            r.fill(row);
            String[] a = activity[idx];
            Draw.drawAvatar(r, px + 4, Math.round(ry + 10), 30, a[0], cols[idx][0], cols[idx][1]);
            r.setFont(Fx.bold(12.5f));
            r.setColor(Fx.text());
            r.drawString(a[1], px + 44, ry + 22);
            r.setFont(Fx.font(Font.PLAIN, 11.5f));
            r.setColor(Fx.text2());
            String t = a[2];
            FontMetrics fm = r.getFontMetrics();
            while (fm.stringWidth(t) > w - 92 && t.length() > 4) t = t.substring(0, t.length() - 2);
            if (!t.equals(a[2])) t = t.trim() + "...";
            r.drawString(t, px + 44, ry + 39);
            r.dispose();
        }
        c.dispose();
    }

    private void paintBadgeTeam(Graphics2D g, float x, float y) {
        int w = 220, h = 66;
        Fx.card(g, x, y, w, h, 16, 0f, null);
        Fx.checkCircle(g, x + 14, y + 15, 36, Fx.MINT, 1f);
        g.setFont(Fx.bold(14));
        g.setColor(Fx.text());
        g.drawString("Team formed", x + 62, y + 29);
        g.setFont(Fx.font(Font.PLAIN, 12));
        g.setColor(Fx.text2());
        g.drawString("in 2 hours, not 2 weeks", x + 62, y + 47);
    }

    private void paintBadgeMatch(Graphics2D g, float x, float y) {
        int w = 196, h = 70;
        Fx.card(g, x, y, w, h, 16, 0f, null);
        float rs = 42, rx = x + 14, ry = y + 14;
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Fx.subtle());
        g.draw(new Ellipse2D.Float(rx, ry, rs, rs));
        float v = 0.96f * Fx.easeOutQuart(rv.lin(2.0f, 1.6f));
        g.setPaint(new GradientPaint(rx, ry, Fx.PINK, rx + rs, ry + rs, Fx.CYAN));
        g.draw(new Arc2D.Float(rx, ry, rs, rs, 90, -360f * v, Arc2D.OPEN));
        g.setFont(Fx.bold(17));
        g.setColor(Fx.text());
        g.drawString(Math.round(v * 100) + "%", x + 68, y + 32);
        g.setFont(Fx.font(Font.PLAIN, 12));
        g.setColor(Fx.text2());
        g.drawString("average skill match", x + 68, y + 50);
    }

    // ---------------------------------------------------------------- marquee
    private int paintMarquee(Graphics2D g, int y, int width) {
        int h = 92;
        if (!measuring) { marqueeY = y; marqueeH = h; }
        if (measuring) return y + h;

        Graphics2D m = Fx.fade(g, rv.p(1.6f, 1f), 0);
        m.setFont(Fx.label(11));
        m.setColor(Fx.text3());
        Fx.textCenter(m, "SKILL TRACKS TEAMS ARE HIRING FOR", width / 2f, y + 8);

        Font f = Fx.bold(17);
        java.awt.image.BufferedImage layer = new java.awt.image.BufferedImage(Math.max(1, width), 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D outer = m;
        m = layer.createGraphics();
        Draw.setQualityHints(m);
        m.translate(0, -(y + 20));
        m.setFont(f);
        FontMetrics fm = m.getFontMetrics();
        int gap = 56;
        int total = 0;
        for (String s : marquee) total += fm.stringWidth(s) + gap;
        float off = -(marqueeOff % total);
        int rowY = y + 52;
        Color[] dots = {Fx.PINK, Fx.VIOLET, Fx.CYAN, Fx.MINT, Fx.AMBER};
        float x = off;
        int i = 0;
        while (x < width) {
            String s = marquee[i % marquee.length];
            int sw = fm.stringWidth(s);
            if (x + sw + gap > 0) {
                Fx.bolt(m, x - 26, rowY - 9, 16, Fx.a(dots[i % dots.length], 210));
                m.setColor(Fx.text2());
                Fx.textMid(m, s, x, rowY);
            }
            x += sw + gap;
            i++;
        }
        // soft edge mask (alpha fade, works over any background)
        m.translate(0, y + 20);
        int fw = Math.min(240, width / 5);
        m.setComposite(AlphaComposite.DstIn);
        m.setPaint(new LinearGradientPaint(0, 0, width, 0, new float[]{0f, fw / (float) width, 1f - fw / (float) width, 1f},
                new Color[]{new Color(0, 0, 0, 0), Color.BLACK, Color.BLACK, new Color(0, 0, 0, 0)}));
        m.fillRect(0, 0, width, 64);
        m.dispose();
        outer.drawImage(layer, 0, y + 20, null);
        outer.dispose();
        return y + h;
    }

    // =====================================================================
    // INPUT
    // =====================================================================

    private Point2D toWindow(int mx, int my) {
        try {
            return winTx.inverseTransform(new Point2D.Float(mx, my), null);
        } catch (NoninvertibleTransformException e) {
            return new Point2D.Float(mx, my);
        }
    }

    @Override
    public boolean onClick(int mx, int my) {
        if (Fx.inside(mx, my, sBtnX, sBtnY, sBtnW, sBtnH)) {
            submitSearch();
            return true;
        }
        if (searchBox.onMouseDown(mx, my)) return true;

        if (Fx.inside(mx, my, annX, annY, annW, annH)) {
            if (listener != null) listener.scrollToSection("demo");
            return true;
        }
        if (Fx.inside(mx, my, b1X, b1Y, b1W, b1H)) {
            if (listener != null) listener.openModal("Get Started with TeamUp",
                    "Join now to explore projects, match with peers, and build your next portfolio showcase.", true);
            return true;
        }
        if (Fx.inside(mx, my, b2X, b2Y, b2W, b2H)) {
            if (listener != null) listener.scrollToSection("how-it-works");
            return true;
        }
        for (Pill p : pills) {
            if (Fx.inside(mx, my, p.x, p.y - 6, p.w, p.h + 6)) {
                searchBox.setText(p.label);
                searchBox.focused = false;
                submitSearch();
                return true;
            }
        }
        Point2D w = toWindow(mx, my);
        int wx = (int) w.getX(), wy = (int) w.getY();
        if (dashBtnW > 0 && Fx.inside(wx, wy, dashBtnX, dashBtnY, dashBtnW, dashBtnH)) {
            if (listener != null) listener.openDashboard();
            return true;
        }
        for (Member m : members) {
            if (Fx.inside(wx, wy, m.bx, m.by, m.bw, m.bh)) {
                m.invited = !m.invited;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean hand = false;
        annHover = Fx.inside(mx, my, annX, annY, annW, annH);
        b1Hover = Fx.inside(mx, my, b1X, b1Y, b1W, b1H);
        b2Hover = Fx.inside(mx, my, b2X, b2Y, b2W, b2H);
        sBtnHover = Fx.inside(mx, my, sBtnX, sBtnY, sBtnW, sBtnH);
        hand = annHover || b1Hover || b2Hover || sBtnHover;
        for (Pill p : pills) {
            p.hovered = Fx.inside(mx, my, p.x, p.y - 6, p.w, p.h + 6);
            hand |= p.hovered;
        }
        mouseOverWin = Fx.inside(mx, my, winX - 80, winY - 80, winW + 160, winH + 160);
        Point2D w = toWindow(mx, my);
        int wx = (int) w.getX(), wy = (int) w.getY();
        dashBtnHovered = dashBtnW > 0 && Fx.inside(wx, wy, dashBtnX, dashBtnY, dashBtnW, dashBtnH);
        hand |= dashBtnHovered;
        for (Member m : members) {
            m.hovered = Fx.inside(wx, wy, m.x, m.y, m.w, m.h);
            m.btnHovered = Fx.inside(wx, wy, m.bx, m.by, m.bw, m.bh);
            hand |= m.btnHovered;
        }
        marqueeHover = Fx.inside(mx, my, 0, marqueeY, 100000, marqueeH);
        return hand;
    }

    @Override
    public boolean onMouseDown(int mx, int my) {
        b1Press = Fx.inside(mx, my, b1X, b1Y, b1W, b1H);
        if (Fx.inside(mx, my, sBtnX, sBtnY, sBtnW, sBtnH)) return true;
        return searchBox.onMouseDown(mx, my);
    }

    @Override
    public boolean onMouseUp(int mx, int my) {
        b1Press = false;
        return false;
    }

    @Override
    public boolean onKey(KeyEvent e) {
        if (searchBox.focused && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
            submitSearch();
            return true;
        }
        return searchBox.onKey(e);
    }
}
