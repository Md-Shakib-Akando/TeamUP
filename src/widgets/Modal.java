import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Login / registration dialog with a true frosted-glass effect.
 *
 * When the dialog opens, the host renders the scene behind it once at 1/4 scale
 * ({@link #setBackdrop}); it is blurred twice — lightly for the whole page
 * (backdrop blur) and heavily for the pane itself (frosted glass) — and upscaled
 * to full size, so every frame is just a couple of plain image blits.
 * On top sit drifting colour orbs that "refract" through the pane, film grain,
 * a specular sheen and light-catching edges.
 */
public class Modal {
    public boolean visible = false;
    public float animProgress = 0f;
    public boolean isRegister = false;

    // Form inputs
    private final TextBox nameInput;
    private final TextBox emailInput;
    private final TextBox passInput;
    private final Dropdown roleSelect;

    // State
    private boolean rememberMe = true;
    private boolean termsAccepted = true;
    private String feedbackMessage = "";
    private boolean isSuccess = false;
    private float feedbackT = 0f;

    // Hit areas
    private int googleBtnX, googleBtnY, googleBtnW, googleBtnH;
    private int githubBtnX, githubBtnY, githubBtnW, githubBtnH;
    private boolean googleHovered, githubHovered;
    private int submitBtnX, submitBtnY, submitBtnW, submitBtnH;
    private boolean submitHovered, submitPressed;
    private int closeX, closeY, closeSize = 34;
    private boolean closeHovered;
    private int tab1X, tab1Y, tab1W, tab1H;
    private int tab2X, tab2Y, tab2W, tab2H;
    private boolean tab1Hovered, tab2Hovered;
    private int switchLinkX, switchLinkY, switchLinkW, switchLinkH;
    private boolean switchLinkHovered;
    private int forgotX, forgotY, forgotW, forgotH;
    private boolean forgotHovered;
    private int checkX, checkY, checkSize = 18;

    // Motion
    private final Elastic tabSlide = new Elastic(0f, 300f, 26f);
    private final Elastic submitHov = new Elastic(0f, 260f, 20f);
    private final Elastic heightAnim = new Elastic(0f, 260f, 26f);
    private float openT = 0f;

    // Frosted glass backdrop
    private BufferedImage backdrop;   // lightly blurred full-screen copy of the scene
    private BufferedImage frosted;    // heavily blurred + tinted copy, shown through the pane
    private int bdW = -1, bdH = -1;
    private boolean backdropDirty = true;
    private float frostedTheme = -1f;

    public Modal() {
        nameInput = new TextBox("Alex Rivera");
        emailInput = new TextBox("name@university.edu");
        passInput = new TextBox("At least 6 characters", true);
        roleSelect = new Dropdown(Arrays.asList(
                "Developer", "UI/UX Designer", "Team Leader", "Researcher", "Freelancer"
        ));
        nameInput.onGlass = true;
        emailInput.onGlass = true;
        passInput.onGlass = true;
        roleSelect.onGlass = true;
        heightAnim.setValue(contentHeight());
    }

    public void show(String title, String message, boolean isRegister) {
        this.isRegister = isRegister;
        this.visible = true;
        this.feedbackMessage = "";
        this.isSuccess = false;
        this.backdropDirty = true;
        this.openT = 0f;
        tabSlide.setValue(isRegister ? 1f : 0f);
        heightAnim.setValue(contentHeight());
        clearErrors();
    }

    public void hide() {
        this.visible = false;
        nameInput.focused = false;
        emailInput.focused = false;
        passInput.focused = false;
        roleSelect.open = false;
    }

    // ------------------------------------------------------------------ backdrop

    /** True when the host should render the scene and call {@link #setBackdrop}. */
    public boolean needsBackdrop(int w, int h) {
        return visible && (backdropDirty || backdrop == null || bdW != w || bdH != h);
    }

    /** Receives the scene rendered at reduced scale; builds the blurred layers. */
    public void setBackdrop(BufferedImage small, int w, int h) {
        BufferedImage light = Fx.copy(small);
        Fx.blur(light, 2, 3);
        BufferedImage heavy = Fx.copy(small);
        Fx.blur(heavy, 6, 3);
        backdrop = Fx.upscale(light, w, h);
        frosted = Fx.upscale(heavy, w, h);
        tintFrosted();
        bdW = w;
        bdH = h;
        backdropDirty = false;
    }

    private void tintFrosted() {
        if (frosted == null) return;
        Graphics2D g = frosted.createGraphics();
        // milky glass: lift the blurred scene towards white (light) or a soft ink (dark)
        if (Fx.dark()) g.setColor(new Color(150, 140, 210, 34));
        else g.setColor(new Color(255, 255, 255, 115));
        g.fillRect(0, 0, frosted.getWidth(), frosted.getHeight());
        g.dispose();
        frostedTheme = Theme.themeTransition;
    }

    /** When fully open, the blurred backdrop hides the scene completely. */
    public boolean coversScene() {
        return visible && animProgress > 0.995f && backdrop != null && !backdropDirty;
    }

    // ------------------------------------------------------------------ update

    public void update(float dt) {
        animProgress = Anim.approach(animProgress, visible ? 1f : 0f, dt, 9f);
        if (!visible && animProgress < 0.002f) {
            backdrop = null;
            frosted = null;
        }
        if (visible) {
            openT += dt;
            nameInput.update(dt);
            emailInput.update(dt);
            passInput.update(dt);
        }
        feedbackT += dt;
        tabSlide.setTarget(isRegister ? 1f : 0f);
        tabSlide.update(dt);
        submitHov.setTarget(submitHovered ? 1f : 0f);
        submitHov.update(dt);
        heightAnim.setTarget(contentHeight());
        heightAnim.update(dt);
    }

    private void clearErrors() {
        nameInput.hasError = false;
        emailInput.hasError = false;
        passInput.hasError = false;
    }

    private void setFeedback(String msg, boolean ok) {
        feedbackMessage = msg;
        isSuccess = ok;
        feedbackT = 0f;
    }

    private void handleAuthSubmit() {
        clearErrors();
        String email = emailInput.getText().trim();
        String pass = passInput.getText();

        if (isRegister) {
            if (nameInput.getText().trim().isEmpty()) {
                nameInput.hasError = true;
                setFeedback("Please enter your full name.", false);
                return;
            }
        }
        if (email.isEmpty()) {
            emailInput.hasError = true;
            setFeedback("Please enter your email address.", false);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            emailInput.hasError = true;
            setFeedback("Please enter a valid email address.", false);
            return;
        }
        if (pass.length() < 6) {
            passInput.hasError = true;
            setFeedback("Password must be at least 6 characters.", false);
            return;
        }
        if (isRegister) {
            if (!termsAccepted) {
                setFeedback("Please accept the Terms to continue.", false);
                return;
            }
            AuthStore.AuthResult res = AuthStore.register(
                    nameInput.getText().trim(), email, pass, roleSelect.getSelected());
            if (res.success) {
                LeadStore.saveLead(email, roleSelect.getSelected());
                setFeedback(res.message, true);
            } else {
                emailInput.hasError = true;
                setFeedback(res.message, false);
            }
        } else {
            AuthStore.AuthResult res = AuthStore.login(email, pass);
            if (res.success) {
                setFeedback(res.message, true);
            } else {
                if (res.message.toLowerCase().contains("password")) passInput.hasError = true;
                else emailInput.hasError = true;
                setFeedback(res.message, false);
            }
        }
    }

    // ------------------------------------------------------------------ layout

    private static final int PAD = 36, FIELD_H = 46;

    private int contentHeight() {
        int h = 30 + 64 + 44 + 24;                 // header + tabs
        if (isRegister) h += 20 + FIELD_H + 16;   // name / role
        h += 20 + FIELD_H + 16;                    // email
        h += 20 + FIELD_H + 10;                    // password
        if (isRegister) h += 4 + 14;               // strength meter
        h += 20 + 20;                              // checkbox row
        h += 50 + 16;                              // submit
        if (!feedbackMessage.isEmpty()) h += 30;
        h += 16 + 16;                              // divider
        h += 46 + 22;                              // social
        h += 20 + 30;                              // switch link + bottom pad
        return h;
    }

    private int modalW(int screenW) { return Math.min(480, screenW - 32); }

    private int modalH() { return Math.round(heightAnim.getValue()); }

    // ------------------------------------------------------------------ paint

    public void paint(Graphics2D g, int screenW, int screenH) {
        if (animProgress <= 0.001f) return;
        if (frosted != null && Math.abs(frostedTheme - Theme.themeTransition) > 0.01f) tintFrosted();

        float p = animProgress;
        float e = Fx.easeOutQuart(p);
        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // 1. Backdrop blur of the whole page + tint
        if (backdrop != null) {
            Composite oc = g2.getComposite();
            Fx.fadeBy(g2, p);
            g2.drawImage(backdrop, 0, 0, null);
            g2.setComposite(oc);
        }
        g2.setColor(Fx.dark() ? new Color(4, 4, 14, Math.round(120 * p)) : new Color(235, 232, 248, Math.round(80 * p)));
        g2.fillRect(0, 0, screenW, screenH);

        int mw = modalW(screenW);
        int mh = modalH();
        int mx = (screenW - mw) / 2;
        int targetY = Math.max(16, (screenH - mh) / 2);
        int my = Math.round(targetY + (1f - e) * 26f);
        float scale = 0.96f + 0.04f * e;

        // 2. Coloured light orbs drifting behind the pane (they "refract" through it)
        float t = Fx.time();
        float ok = Fx.dark() ? 1f : 0.75f;
        Fx.glow(g2, mx + 30 + 30 * (float) Math.sin(t * 0.6f), my + 60 + 20 * (float) Math.cos(t * 0.5f), 230, Fx.PINK, 0.55f * p * ok);
        Fx.glow(g2, mx + mw - 20 + 25 * (float) Math.cos(t * 0.45f), my + mh - 70 + 25 * (float) Math.sin(t * 0.55f), 250, Fx.CYAN, 0.42f * p * ok);
        Fx.glow(g2, mx + mw * 0.8f, my + 40, 170, Fx.VIOLET, 0.45f * p * ok);

        Graphics2D c = (Graphics2D) g2.create();
        Fx.fadeBy(c, Fx.clamp(p * 1.3f));
        c.translate(mx + mw / 2f, my + mh / 2f);
        c.scale(scale, scale);
        c.translate(-(mx + mw / 2f), -(my + mh / 2f));

        RoundRectangle2D.Float pane = Fx.rr(mx, my, mw, mh, 28);

        // 3. Shadow
        Fx.softShadow(c, mx, my, mw, mh, 28, Fx.dark() ? 1f : 0.45f, 40);

        // 4. Frosted pane: the heavily blurred scene seen through the glass
        Graphics2D gl = (Graphics2D) c.create();
        gl.clip(pane);
        if (frosted != null) {
            gl.drawImage(frosted, 0, 0, null);
        } else {
            gl.setColor(Fx.dark() ? new Color(30, 28, 60) : new Color(250, 249, 255));
            gl.fill(pane);
        }
        // light passing through the glass from the orbs behind it
        Fx.glow(gl, mx + 30 + 30 * (float) Math.sin(t * 0.6f), my + 60 + 20 * (float) Math.cos(t * 0.5f), 260, Fx.PINK, 0.30f * ok);
        Fx.glow(gl, mx + mw - 20 + 25 * (float) Math.cos(t * 0.45f), my + mh - 70 + 25 * (float) Math.sin(t * 0.55f), 280, Fx.CYAN, 0.22f * ok);
        // milky body + vertical light falloff
        gl.setPaint(new GradientPaint(0, my, Fx.dark() ? new Color(255, 255, 255, 26) : new Color(255, 255, 255, 120),
                0, my + mh, Fx.dark() ? new Color(255, 255, 255, 6) : new Color(255, 255, 255, 60)));
        gl.fill(pane);
        // specular sheen: diagonal highlight across the upper-left
        gl.setPaint(new LinearGradientPaint(mx, my, mx + mw * 0.7f, my + mh * 0.45f, new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(255, 255, 255, Fx.dark() ? 40 : 90), new Color(255, 255, 255, Fx.dark() ? 10 : 25), new Color(255, 255, 255, 0)}));
        gl.fill(pane);
        // film grain
        BufferedImage gt = Fx.grainTile();
        gl.setPaint(new TexturePaint(gt, new Rectangle(0, 0, gt.getWidth(), gt.getHeight())));
        gl.fill(pane);
        gl.dispose();

        // 5. Glass edges: bright rim on top-left, coloured reflection bottom-right
        c.setStroke(new BasicStroke(1.4f));
        c.setPaint(new GradientPaint(mx, my, new Color(255, 255, 255, Fx.dark() ? 120 : 230),
                mx + mw, my + mh, Fx.a(Fx.PINK, Fx.dark() ? 90 : 110)));
        c.draw(pane);
        c.setPaint(new LinearGradientPaint(mx + 28, my + 1, mx + mw - 28, my + 1, new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, Fx.dark() ? 150 : 255), new Color(255, 255, 255, 0)}));
        c.draw(new Line2D.Float(mx + 28, my + 1.2f, mx + mw - 28, my + 1.2f));
        Fx.spotlightBorder(c, pane, Fx.VIOLET, 0.8f);

        paintContent(c, mx, my, mw, mh);
        c.dispose();
        g2.dispose();
    }

    private Graphics2D stagger(Graphics2D g, int i) {
        float p = Fx.easeOutQuart((openT - 0.05f - i * 0.04f) / 0.45f);
        return Fx.fade(g, Fx.clamp(p), 12);
    }

    private void paintContent(Graphics2D g, int mx, int my, int mw, int mh) {
        int cx = mx + PAD;
        int cw = mw - PAD * 2;
        int cur = my + 30;
        int row = 0;

        // Close button (glass circle)
        closeSize = 34;
        closeX = mx + mw - closeSize - 18;
        closeY = my + 18;
        Ellipse2D.Float cb = new Ellipse2D.Float(closeX, closeY, closeSize, closeSize);
        g.setColor(closeHovered ? Fx.surfaceHi() : Fx.subtle());
        g.fill(cb);
        g.setColor(closeHovered ? Fx.borderHi() : Fx.border());
        g.draw(cb);
        Graphics2D x = (Graphics2D) g.create();
        x.translate(closeX + closeSize / 2f, closeY + closeSize / 2f);
        x.rotate(closeHovered ? Math.PI / 2 : 0);
        x.setColor(closeHovered ? Fx.text() : Fx.text2());
        x.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        x.drawLine(-5, -5, 5, 5);
        x.drawLine(5, -5, -5, 5);
        x.dispose();

        // Header
        Graphics2D h = stagger(g, row++);
        Fx.glow(h, cx + 22, cur + 22, 36, Fx.PINK, 0.5f);
        Draw.drawLogo(h, cx, cur, 44);
        h.setFont(Fx.heading(23));
        h.setColor(Fx.text());
        h.drawString(isRegister ? "Create your account" : "Welcome back", cx + 58, cur + 20);
        h.setFont(Fx.body(13.5f));
        h.setColor(Fx.text2());
        h.drawString(isRegister ? "Join verified peers and build high-impact teams." : "Sign in to manage your teams and messages.", cx + 58, cur + 40);
        h.dispose();
        cur += 64;

        // Segmented tabs with sliding glass pill
        int tabH = 44;
        tab1X = cx; tab1Y = cur; tab1W = cw / 2; tab1H = tabH;
        tab2X = cx + cw / 2; tab2Y = cur; tab2W = cw - cw / 2; tab2H = tabH;
        Graphics2D tg = stagger(g, row++);
        RoundRectangle2D.Float track = Fx.rr(cx, cur, cw, tabH, tabH / 2f);
        tg.setColor(Fx.dark() ? new Color(0, 0, 0, 60) : new Color(20, 18, 50, 14));
        tg.fill(track);
        tg.setColor(Fx.border());
        tg.draw(track);
        float sx = cx + 4 + tabSlide.getValue() * (cw / 2f);
        RoundRectangle2D.Float pill = Fx.rr(sx, cur + 4, cw / 2f - 8, tabH - 8, (tabH - 8) / 2f);
        Fx.glow(tg, sx + cw / 4f, cur + tabH / 2f, cw / 4f, Fx.PINK, 0.25f);
        tg.setPaint(new GradientPaint(sx, cur, Fx.dark() ? new Color(255, 255, 255, 46) : Color.WHITE,
                sx, cur + tabH, Fx.dark() ? new Color(255, 255, 255, 18) : new Color(250, 248, 255)));
        tg.fill(pill);
        tg.setColor(new Color(255, 255, 255, Fx.dark() ? 70 : 255));
        tg.draw(pill);
        tg.setFont(Fx.bold(14));
        float k = tabSlide.getValue();
        tg.setColor(Fx.mix(Fx.text(), Fx.text2(), k));
        if (tab1Hovered && isRegister) tg.setColor(Fx.text2());
        Fx.textCenter(tg, "Log in", tab1X + tab1W / 2f, cur + tabH / 2f);
        tg.setColor(Fx.mix(Fx.text2(), Fx.text(), k));
        if (tab2Hovered && !isRegister) tg.setColor(Fx.text2());
        Fx.textCenter(tg, "Create account", tab2X + tab2W / 2f, cur + tabH / 2f);
        tg.dispose();
        cur += tabH + 24;

        // Clip the form to the (animating) pane height so it never spills
        Graphics2D f = (Graphics2D) g.create();
        f.clipRect(mx, my, mw, mh);

        if (isRegister) {
            Graphics2D r = stagger(f, row++);
            int gap = 12, colW = (cw - gap) / 2;
            label(r, "Full name", cx, cur);
            label(r, "Primary role", cx + colW + gap, cur);
            nameInput.setBounds(cx, cur + 20, colW, FIELD_H);
            roleSelect.setBounds(cx + colW + gap, cur + 20, cw - colW - gap, FIELD_H);
            nameInput.paint(r);
            roleSelect.paint(r);
            r.dispose();
            cur += 20 + FIELD_H + 16;
        }

        Graphics2D em = stagger(f, row++);
        label(em, "Email address", cx, cur);
        emailInput.setBounds(cx, cur + 20, cw, FIELD_H);
        emailInput.paint(em);
        em.dispose();
        cur += 20 + FIELD_H + 16;

        Graphics2D pw = stagger(f, row++);
        label(pw, "Password", cx, cur);
        int pLen = passInput.getText().length();
        if (isRegister && pLen > 0) {
            String st = pLen >= 10 ? "Strong" : (pLen >= 6 ? "Good" : "Too short");
            Color sc = pLen >= 10 ? Fx.MINT : (pLen >= 6 ? Fx.AMBER : Fx.RED);
            pw.setFont(Fx.bold(12));
            pw.setColor(Fx.ink(sc));
            pw.drawString(st, cx + cw - pw.getFontMetrics().stringWidth(st), cur + 12);
        }
        passInput.setBounds(cx, cur + 20, cw, FIELD_H);
        passInput.paint(pw);
        cur += 20 + FIELD_H + 10;
        if (isRegister) {
            // 4-segment strength meter
            int segGap = 6, segW = (cw - segGap * 3) / 4;
            int level = pLen == 0 ? 0 : (pLen < 6 ? 1 : (pLen < 8 ? 2 : (pLen < 10 ? 3 : 4)));
            Color[] lc = {Fx.RED, Fx.AMBER, Fx.CYAN, Fx.MINT};
            for (int i = 0; i < 4; i++) {
                RoundRectangle2D.Float seg = Fx.rr(cx + i * (segW + segGap), cur, segW, 4, 2);
                if (i < level) pw.setColor(lc[level - 1]);
                else pw.setColor(Fx.border());
                pw.fill(seg);
            }
            cur += 4 + 14;
        }
        pw.dispose();

        // Checkbox + forgot password
        Graphics2D ck = stagger(f, row++);
        checkX = cx;
        checkY = cur;
        paintCheckbox(ck, checkX, checkY, isRegister ? termsAccepted : rememberMe);
        ck.setFont(Fx.body(13));
        ck.setColor(Fx.text2());
        Fx.textMid(ck, isRegister ? "I agree to the Terms & Code of Conduct" : "Keep me signed in", checkX + 28, checkY + checkSize / 2f);
        if (!isRegister) {
            ck.setFont(Fx.bold(13));
            String fp = "Forgot password?";
            forgotW = ck.getFontMetrics().stringWidth(fp);
            forgotH = 20;
            forgotX = cx + cw - forgotW;
            forgotY = checkY - 1;
            ck.setColor(forgotHovered ? Fx.text() : Fx.ink(Fx.PINK));
            Fx.textMid(ck, fp, forgotX, checkY + checkSize / 2f);
        } else {
            forgotX = -100;
        }
        ck.dispose();
        cur += 20 + 20;

        // Submit
        submitBtnX = cx;
        submitBtnY = cur;
        submitBtnW = cw;
        submitBtnH = 50;
        Graphics2D sb = stagger(f, row++);
        Fx.primaryButton(sb, submitBtnX, submitBtnY, submitBtnW, submitBtnH,
                isRegister ? "Create free account" : "Sign in to TeamUp", submitHov.getValue(), submitPressed ? 1f : 0f, true);
        sb.dispose();
        cur += 50 + 16;

        // Feedback (glass toast)
        if (!feedbackMessage.isEmpty()) {
            float fp = Fx.easeOutQuart(feedbackT / 0.35f);
            Graphics2D fb = Fx.fade(f, fp, -6);
            Color col = isSuccess ? Fx.MINT : Fx.RED;
            RoundRectangle2D.Float toast = Fx.rr(cx, cur - 4, cw, 28, 10);
            fb.setColor(Fx.a(col, Fx.dark() ? 34 : 26));
            fb.fill(toast);
            fb.setColor(Fx.a(col, 110));
            fb.draw(toast);
            fb.setFont(Fx.bold(12.5f));
            if (isSuccess) Fx.checkCircle(fb, cx + 8, cur + 1, 18, Fx.MINT, 1f);
            else {
                fb.setColor(Fx.ink(col));
                fb.fill(new Ellipse2D.Float(cx + 12, cur + 6, 8, 8));
            }
            fb.setColor(Fx.ink(col));
            String msg = feedbackMessage;
            FontMetrics fm = fb.getFontMetrics();
            while (fm.stringWidth(msg) > cw - 44 && msg.length() > 8) msg = msg.substring(0, msg.length() - 2);
            if (!msg.equals(feedbackMessage)) msg = msg.trim() + "...";
            Fx.textMid(fb, msg, cx + 34, cur + 10);
            fb.dispose();
            cur += 30;
        }

        // Divider
        Graphics2D dv = stagger(f, row++);
        dv.setFont(Fx.body(12.5f));
        String divStr = "or continue with";
        int dw = dv.getFontMetrics().stringWidth(divStr);
        float mid = cur + 8;
        dv.setPaint(new GradientPaint(cx, 0, Fx.a(Fx.borderHi(), 0), cx + (cw - dw) / 2f - 12, 0, Fx.borderHi()));
        dv.fill(new Rectangle2D.Float(cx, mid, (cw - dw) / 2f - 12, 1));
        dv.setPaint(new GradientPaint(cx + (cw + dw) / 2f + 12, 0, Fx.borderHi(), cx + cw, 0, Fx.a(Fx.borderHi(), 0)));
        dv.fill(new Rectangle2D.Float(cx + (cw + dw) / 2f + 12, mid, (cw - dw) / 2f - 12, 1));
        dv.setColor(Fx.text3());
        Fx.textCenter(dv, divStr, cx + cw / 2f, mid);
        dv.dispose();
        cur += 16 + 16;

        // Social buttons
        int gap = 12, sw = (cw - gap) / 2, sh = 46;
        googleBtnX = cx; googleBtnY = cur; googleBtnW = sw; googleBtnH = sh;
        githubBtnX = cx + sw + gap; githubBtnY = cur; githubBtnW = cw - sw - gap; githubBtnH = sh;
        Graphics2D so = stagger(f, row++);
        paintSocialButton(so, googleBtnX, googleBtnY, googleBtnW, googleBtnH, "Google", true, googleHovered);
        paintSocialButton(so, githubBtnX, githubBtnY, githubBtnW, githubBtnH, "GitHub", false, githubHovered);
        so.dispose();
        cur += sh + 22;

        // Switch link
        Graphics2D sl = stagger(f, row);
        sl.setFont(Fx.body(13.5f));
        FontMetrics swFm = sl.getFontMetrics();
        String prefix = isRegister ? "Already have an account? " : "New to TeamUp? ";
        String link = isRegister ? "Log in" : "Create an account";
        int pre = swFm.stringWidth(prefix);
        int lw = sl.getFontMetrics(Fx.bold(13.5f)).stringWidth(link);
        switchLinkX = cx + (cw - pre - lw) / 2;
        switchLinkY = cur;
        switchLinkW = pre + lw;
        switchLinkH = 20;
        sl.setColor(Fx.text2());
        Fx.textMid(sl, prefix, switchLinkX, cur + 10);
        sl.setFont(Fx.bold(13.5f));
        sl.setColor(switchLinkHovered ? Fx.text() : Fx.ink(Fx.PINK));
        Fx.textMid(sl, link, switchLinkX + pre, cur + 10);
        if (switchLinkHovered) {
            sl.setPaint(new GradientPaint(switchLinkX + pre, 0, Fx.PINK, switchLinkX + pre + lw, 0, Fx.VIOLET));
            sl.fillRect(switchLinkX + pre, cur + 19, lw, 2);
        }
        sl.dispose();

        // Dropdown menu last so it overlays the form
        if (isRegister && roleSelect.open) roleSelect.paintMenu(f);
        f.dispose();
    }

    private void label(Graphics2D g, String text, int x, int y) {
        g.setFont(Fx.bold(12.5f));
        g.setColor(Fx.text2());
        g.drawString(text, x + 2, y + 12);
    }

    private void paintCheckbox(Graphics2D g, int x, int y, boolean checked) {
        RoundRectangle2D.Float cb = Fx.rr(x, y, checkSize, checkSize, 6);
        if (checked) {
            Fx.glow(g, x + checkSize / 2f, y + checkSize / 2f, 16, Fx.PINK, 0.4f);
            g.setPaint(new GradientPaint(x, y, Fx.PINK, x + checkSize, y + checkSize, Fx.VIOLET));
            g.fill(cb);
            Draw.drawCheckIcon(g, x, y, checkSize, Color.WHITE);
        } else {
            g.setColor(Fx.surface());
            g.fill(cb);
            g.setColor(Fx.borderHi());
            g.setStroke(new BasicStroke(1.2f));
            g.draw(cb);
        }
    }

    private void paintSocialButton(Graphics2D g, int x, int y, int w, int h, String name, boolean isGoogle, boolean hovered) {
        RoundRectangle2D.Float shape = Fx.rr(x, y, w, h, 14);
        g.setColor(Fx.glassField(hovered));
        g.fill(shape);
        g.setColor(hovered ? Fx.borderHi() : Fx.border());
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        if (hovered) Fx.spotlightBorder(g, shape, isGoogle ? new Color(0x42, 0x85, 0xF4) : Fx.VIOLET, 1f);

        g.setFont(Fx.bold(14));
        FontMetrics fm = g.getFontMetrics();
        int icon = 18;
        int total = icon + 10 + fm.stringWidth(name);
        int ix = x + (w - total) / 2;
        int iy = y + (h - icon) / 2;
        if (isGoogle) paintGoogleG(g, ix, iy, icon);
        else paintGitHub(g, ix, iy, icon);
        g.setColor(Fx.text());
        Fx.textMid(g, name, ix + icon + 10, y + h / 2f);
    }

    /** Four-colour "G" built from arcs. */
    private void paintGoogleG(Graphics2D g, float x, float y, float s) {
        Graphics2D G = (Graphics2D) g.create();
        float sw = s * 0.2f;
        G.setStroke(new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        float in = sw / 2f;
        Arc2D.Float arc = new Arc2D.Float(x + in, y + in, s - sw, s - sw, 0, 0, Arc2D.OPEN);
        Color[] cols = {new Color(0xEA, 0x43, 0x35), new Color(0xFB, 0xBC, 0x05), new Color(0x34, 0xA8, 0x53), new Color(0x42, 0x85, 0xF4)};
        float[][] seg = {{40, 100}, {140, 75}, {215, 95}, {310, 50}};
        for (int i = 0; i < 4; i++) {
            G.setColor(cols[i]);
            arc.setAngleStart(seg[i][0]);
            arc.setAngleExtent(seg[i][1]);
            G.draw(arc);
        }
        G.setColor(cols[3]);
        G.fill(new Rectangle2D.Float(x + s / 2f, y + s / 2f - sw / 2f, s / 2f - in + sw / 2f, sw));
        G.dispose();
    }

    /** Simplified GitHub mark: cat head silhouette in a circle. */
    private void paintGitHub(Graphics2D g, float x, float y, float s) {
        Graphics2D G = (Graphics2D) g.create();
        G.setColor(Fx.text());
        G.fill(new Ellipse2D.Float(x, y, s, s));
        G.setColor(Fx.dark() ? new Color(0x20, 0x1E, 0x3A) : Color.WHITE);
        Path2D.Float cat = new Path2D.Float();
        float cx = x + s / 2f;
        cat.moveTo(cx - s * 0.26f, y + s * 0.62f);
        cat.lineTo(cx - s * 0.27f, y + s * 0.26f);
        cat.lineTo(cx - s * 0.12f, y + s * 0.34f);
        cat.lineTo(cx + s * 0.12f, y + s * 0.34f);
        cat.lineTo(cx + s * 0.27f, y + s * 0.26f);
        cat.lineTo(cx + s * 0.26f, y + s * 0.62f);
        cat.quadTo(cx, y + s * 0.78f, cx - s * 0.26f, y + s * 0.62f);
        cat.closePath();
        G.fill(cat);
        G.fill(new Rectangle2D.Float(cx - s * 0.08f, y + s * 0.66f, s * 0.16f, s * 0.3f));
        G.dispose();
    }

    // ------------------------------------------------------------------ input

    public boolean onClick(int mx, int my, int screenW, int screenH) {
        if (!visible) return false;

        if (Fx.inside(mx, my, closeX, closeY, closeSize, closeSize)) { hide(); return true; }
        if (Fx.inside(mx, my, tab1X, tab1Y, tab1W, tab1H)) { isRegister = false; feedbackMessage = ""; roleSelect.open = false; return true; }
        if (Fx.inside(mx, my, tab2X, tab2Y, tab2W, tab2H)) { isRegister = true; feedbackMessage = ""; return true; }

        // Dropdown first so its open menu captures clicks over the fields below it
        if (isRegister && roleSelect.onClick(mx, my)) return true;

        if (forgotX > 0 && Fx.inside(mx, my, forgotX, forgotY, forgotW, forgotH)) {
            setFeedback("Reset link sent - check your inbox.", true);
            return true;
        }
        if (mx >= checkX && mx <= checkX + 300 && my >= checkY - 4 && my <= checkY + checkSize + 4) {
            if (isRegister) termsAccepted = !termsAccepted;
            else rememberMe = !rememberMe;
            return true;
        }
        if (Fx.inside(mx, my, submitBtnX, submitBtnY, submitBtnW, submitBtnH)) { handleAuthSubmit(); return true; }
        if (Fx.inside(mx, my, googleBtnX, googleBtnY, googleBtnW, googleBtnH)) { setFeedback("Authenticated via Google account.", true); return true; }
        if (Fx.inside(mx, my, githubBtnX, githubBtnY, githubBtnW, githubBtnH)) { setFeedback("Authenticated via GitHub account.", true); return true; }
        if (Fx.inside(mx, my, switchLinkX, switchLinkY - 4, switchLinkW, switchLinkH + 8)) {
            isRegister = !isRegister;
            feedbackMessage = "";
            return true;
        }

        boolean any = false;
        if (isRegister) any |= nameInput.onMouseDown(mx, my); else nameInput.focused = false;
        any |= emailInput.onMouseDown(mx, my);
        any |= passInput.onMouseDown(mx, my);
        if (any) return true;

        // Outside click closes the dialog
        int mw = modalW(screenW), mh = modalH();
        int x0 = (screenW - mw) / 2, y0 = Math.max(16, (screenH - mh) / 2);
        if (mx < x0 || mx > x0 + mw || my < y0 || my > y0 + mh) hide();
        return true;
    }

    public boolean onMouseMove(int mx, int my) {
        if (!visible) return false;
        closeHovered = Fx.inside(mx, my, closeX, closeY, closeSize, closeSize);
        submitHovered = Fx.inside(mx, my, submitBtnX, submitBtnY, submitBtnW, submitBtnH);
        googleHovered = Fx.inside(mx, my, googleBtnX, googleBtnY, googleBtnW, googleBtnH);
        githubHovered = Fx.inside(mx, my, githubBtnX, githubBtnY, githubBtnW, githubBtnH);
        switchLinkHovered = Fx.inside(mx, my, switchLinkX, switchLinkY - 4, switchLinkW, switchLinkH + 8);
        forgotHovered = forgotX > 0 && Fx.inside(mx, my, forgotX, forgotY, forgotW, forgotH);
        tab1Hovered = Fx.inside(mx, my, tab1X, tab1Y, tab1W, tab1H);
        tab2Hovered = Fx.inside(mx, my, tab2X, tab2Y, tab2W, tab2H);
        boolean dd = isRegister && roleSelect.contains(mx, my);
        if (isRegister) roleSelect.onMouseMove(mx, my);
        boolean check = mx >= checkX && mx <= checkX + 300 && my >= checkY - 4 && my <= checkY + checkSize + 4;
        return closeHovered || submitHovered || googleHovered || githubHovered || switchLinkHovered
                || forgotHovered || tab1Hovered || tab2Hovered || dd || check;
    }

    public boolean onKey(KeyEvent e) {
        if (!visible) return false;
        boolean pressed = e.getID() == KeyEvent.KEY_PRESSED;

        if (pressed && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (roleSelect.open) roleSelect.open = false;
            else hide();
            return true;
        }
        if (pressed && e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleAuthSubmit();
            return true;
        }
        if (pressed && e.getKeyCode() == KeyEvent.VK_TAB) {
            if (isRegister) {
                if (nameInput.focused) { nameInput.focused = false; emailInput.focused = true; }
                else if (emailInput.focused) { emailInput.focused = false; passInput.focused = true; }
                else { passInput.focused = false; nameInput.focused = true; }
            } else {
                if (emailInput.focused) { emailInput.focused = false; passInput.focused = true; }
                else { passInput.focused = false; emailInput.focused = true; }
            }
            return true;
        }

        if (isRegister && nameInput.onKey(e)) return true;
        if (emailInput.onKey(e)) return true;
        if (passInput.onKey(e)) return true;
        // swallow everything while open so page shortcuts (M, arrows...) don't fire
        return true;
    }

    /** Called by the host on mouse release so the submit button can show its press state. */
    public void setPressed(boolean down, int mx, int my) {
        submitPressed = down && Fx.inside(mx, my, submitBtnX, submitBtnY, submitBtnW, submitBtnH);
    }
}
