import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.Arrays;

/**
 * Professional, highly-polished Glassmorphism Login and Registration modal interface
 * with authentic refractive frosted glass effect, dual-tone specular borders,
 * ambient glow halos, two-column responsive form layout, password visibility toggle,
 * password strength meter, social auth buttons (Google, GitHub), and LeadStore integration.
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

    // Social buttons hit areas
    private int googleBtnX, googleBtnY, googleBtnW, googleBtnH;
    private int githubBtnX, githubBtnY, githubBtnW, githubBtnH;
    private boolean googleHovered = false;
    private boolean githubHovered = false;

    // Submit button hit area
    private int submitBtnX, submitBtnY, submitBtnW, submitBtnH;
    private boolean submitHovered = false;

    // Close button hit area
    private int closeX, closeY, closeSize = 28;
    private boolean closeHovered = false;

    // Tab buttons hit areas
    private int tab1X, tab1Y, tab1W, tab1H;
    private int tab2X, tab2Y, tab2W, tab2H;
    private boolean tab1Hovered = false;
    private boolean tab2Hovered = false;

    // Toggle switch link hit area
    private int switchLinkX, switchLinkY, switchLinkW, switchLinkH;
    private boolean switchLinkHovered = false;

    // Checkbox hit area
    private int checkX, checkY, checkSize = 18;

    public Modal() {
        nameInput = new TextBox("Alex Rivera");
        emailInput = new TextBox("name@domain.com");
        passInput = new TextBox("••••••••••••", true);

        roleSelect = new Dropdown(Arrays.asList(
                "Developer", "UI/UX Designer", "Team Leader", "Researcher", "Freelancer"
        ));
    }

    public void show(String title, String message, boolean isRegister) {
        this.isRegister = isRegister;
        this.visible = true;
        this.feedbackMessage = "";
        this.isSuccess = false;
        clearErrors();
    }

    public void hide() {
        this.visible = false;
        nameInput.focused = false;
        emailInput.focused = false;
        passInput.focused = false;
        roleSelect.open = false;
    }

    public void update(float dt) {
        float target = visible ? 1f : 0f;
        animProgress = Anim.approach(animProgress, target, dt, 12f);

        if (visible) {
            nameInput.update(dt);
            emailInput.update(dt);
            passInput.update(dt);
        }
    }

    private void clearErrors() {
        nameInput.hasError = false;
        emailInput.hasError = false;
        passInput.hasError = false;
    }

    private void handleAuthSubmit() {
        clearErrors();
        String email = emailInput.getText().trim();
        String pass = passInput.getText();

        if (isRegister) {
            String name = nameInput.getText().trim();
            if (name.isEmpty()) {
                nameInput.hasError = true;
                feedbackMessage = "Please enter your full name.";
                isSuccess = false;
                return;
            }
        }

        if (email.isEmpty()) {
            emailInput.hasError = true;
            feedbackMessage = "Please enter your email address.";
            isSuccess = false;
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            emailInput.hasError = true;
            feedbackMessage = "Please enter a valid email address.";
            isSuccess = false;
            return;
        }

        if (pass.length() < 6) {
            passInput.hasError = true;
            feedbackMessage = "Password must be at least 6 characters.";
            isSuccess = false;
            return;
        }

        if (isRegister) {
            String role = roleSelect.getSelected();
            LeadStore.ValidationResult res = LeadStore.saveLead(email, role);
            if (res.success) {
                isSuccess = true;
                feedbackMessage = "Welcome to TeamUp! Account created successfully.";
            } else {
                isSuccess = false;
                feedbackMessage = res.message;
            }
        } else {
            isSuccess = true;
            feedbackMessage = "Welcome back! Login authenticated.";
        }
    }

    private int getModalHeight() {
        int h = isRegister ? 540 : 470;
        if (!feedbackMessage.isEmpty()) {
            h += 22;
        }
        return h;
    }

    public void paint(Graphics2D g, int screenW, int screenH) {
        if (animProgress <= 0.001f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // 1. Translucent Backdrop Dim with ambient tint
        int backdropAlpha = Math.round(195 * animProgress);
        g2.setColor(new Color(6, 9, 20, backdropAlpha));
        g2.fillRect(0, 0, screenW, screenH);

        // Modal Box dimensions
        int modalW = Math.min(510, screenW - 32);
        int modalH = getModalHeight();
        int modalX = (screenW - modalW) / 2;
        int targetY = Math.max(16, (screenH - modalH) / 2);
        int modalY = targetY - Math.round(18 * (1f - Anim.easeOutBack(animProgress)));

        // 2. Ambient Refractive Glass Glow Orbs (behind modal card)
        // Top-Left Pink Light Glow
        paintAmbientOrb(g2, modalX + 40, modalY + 30, 160, Theme.BRAND_PINK, 0.25f * animProgress);
        // Bottom-Right Cyan/Sky Light Glow
        paintAmbientOrb(g2, modalX + modalW - 40, modalY + modalH - 30, 170, Theme.ACCENT_CYAN, 0.22f * animProgress);

        // 3. Multi-layer Drop Shadow for Realistic Elevation
        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 26, 26);
        g2.setColor(new Color(0, 0, 0, Math.round(45 * animProgress)));
        g2.fill(new RoundRectangle2D.Float(modalX - 2, modalY + 16, modalW + 4, modalH, 30, 30));
        g2.setColor(new Color(6, 10, 26, Math.round(75 * animProgress)));
        g2.fill(new RoundRectangle2D.Float(modalX, modalY + 8, modalW, modalH, 26, 26));

        // 4. Frosted Glass Surface (Translucent gradient fill allowing background depth)
        GradientPaint glassFill;
        if (Theme.isDark) {
            glassFill = new GradientPaint(
                    modalX, modalY, new Color(24, 32, 54, 225),
                    modalX, modalY + modalH, new Color(13, 17, 32, 210)
            );
        } else {
            glassFill = new GradientPaint(
                    modalX, modalY, new Color(255, 255, 255, 230),
                    modalX, modalY + modalH, new Color(246, 247, 252, 205)
            );
        }
        g2.setPaint(glassFill);
        g2.fill(box);

        // 5. Specular Sheen Highlight at top of glass card
        GradientPaint sheen = new GradientPaint(
                modalX, modalY, new Color(255, 255, 255, Theme.isDark ? 35 : 85),
                modalX, modalY + 100, new Color(255, 255, 255, 0)
        );
        g2.setPaint(sheen);
        g2.fill(new RoundRectangle2D.Float(modalX + 1, modalY + 1, modalW - 2, 100, 26, 26));

        // 6. Dual-tone Specular Glass Border (Light from top-left, pink reflection bottom-right)
        GradientPaint glassBorder;
        if (Theme.isDark) {
            glassBorder = new GradientPaint(
                    modalX, modalY, new Color(255, 255, 255, 120),
                    modalX + modalW, modalY + modalH, new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 80)
            );
        } else {
            glassBorder = new GradientPaint(
                    modalX, modalY, new Color(255, 255, 255, 230),
                    modalX + modalW, modalY + modalH, new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 90)
            );
        }
        g2.setPaint(glassBorder);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(box);

        // 7. Frosted Glass Close Button (top-right)
        closeX = modalX + modalW - 38;
        closeY = modalY + 18;
        closeSize = 28;
        if (closeHovered) {
            g2.setColor(Theme.isDark ? new Color(255, 255, 255, 25) : new Color(0, 0, 0, 10));
            g2.fillOval(closeX - 2, closeY - 2, closeSize + 4, closeSize + 4);
        }
        Draw.drawCloseIcon(g2, closeX, closeY, closeSize, closeHovered ? Theme.BRAND_PINK : Theme.textMuted());

        int pad = 32;
        int contentX = modalX + pad;
        int contentW = modalW - pad * 2;
        int curY = modalY + 24;

        // 8. Brand Logo + Title
        Draw.drawLogo(g2, contentX, curY, 32);

        g2.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        String titleStr = isRegister ? "Create your account" : "Welcome back";
        g2.drawString(titleStr, contentX + 44, curY + 20);

        g2.setFont(Theme.fontSmall());
        g2.setColor(Theme.textSecondary());
        String subStr = isRegister ? "Join verified peers and form high-impact teams." : "Sign in to manage your teams and messages.";
        g2.drawString(subStr, contentX + 44, curY + 36);

        curY += 50;

        // 9. Frosted Segmented Tab Switcher (Log In vs Create Account)
        int tabH = 34;
        int tabW = contentW;
        RoundRectangle2D tabTrack = new RoundRectangle2D.Float(contentX, curY, tabW, tabH, tabH, tabH);
        g2.setColor(Theme.isDark ? new Color(255, 255, 255, 14) : new Color(0, 0, 0, 8));
        g2.fill(tabTrack);

        int singleTabW = tabW / 2;
        tab1X = contentX;
        tab1Y = curY;
        tab1W = singleTabW;
        tab1H = tabH;

        tab2X = contentX + singleTabW;
        tab2Y = curY;
        tab2W = singleTabW;
        tab2H = tabH;

        // Active tab glass pill
        int activeTabX = isRegister ? tab2X : tab1X;
        RoundRectangle2D activePill = new RoundRectangle2D.Float(activeTabX + 2, curY + 2, singleTabW - 4, tabH - 4, tabH, tabH);
        g2.setColor(Theme.isDark ? new Color(34, 46, 76) : Color.WHITE);
        g2.fill(activePill);
        g2.setColor(new Color(255, 255, 255, Theme.isDark ? 30 : 180));
        g2.draw(activePill);

        g2.setFont(Theme.fontSmallBold());
        FontMetrics tabFm = g2.getFontMetrics();

        // Tab 1: Log In
        g2.setColor(!isRegister ? Theme.BRAND_PINK : Theme.textSecondary());
        String t1 = "Log In";
        g2.drawString(t1, tab1X + (singleTabW - tabFm.stringWidth(t1)) / 2, curY + (tabH - tabFm.getHeight()) / 2 + tabFm.getAscent());

        // Tab 2: Create Account
        g2.setColor(isRegister ? Theme.BRAND_PINK : Theme.textSecondary());
        String t2 = "Create Account";
        g2.drawString(t2, tab2X + (singleTabW - tabFm.stringWidth(t2)) / 2, curY + (tabH - tabFm.getHeight()) / 2 + tabFm.getAscent());

        curY += tabH + 16;

        // 10. FORM FIELDS (Well-proportioned layout)
        int fieldH = 38;

        if (isRegister) {
            // Row 1: Full Name & Primary Role side-by-side
            int colGap = 12;
            int colW = (contentW - colGap) / 2;

            paintFieldLabel(g2, "Full Name", contentX, curY);
            paintFieldLabel(g2, "Primary Role", contentX + colW + colGap, curY);
            curY += 16;

            nameInput.setBounds(contentX, curY, colW, fieldH);
            nameInput.paint(g2);

            roleSelect.setBounds(contentX + colW + colGap, curY, colW, fieldH);
            roleSelect.paint(g2);
            curY += fieldH + 10;
        }

        // Email Field
        paintFieldLabel(g2, "Email Address", contentX, curY);
        curY += 16;
        emailInput.setBounds(contentX, curY, contentW, fieldH);
        emailInput.paint(g2);
        curY += fieldH + 10;

        // Password Field
        paintFieldLabel(g2, "Password", contentX, curY);
        curY += 16;
        passInput.setBounds(contentX, curY, contentW, fieldH);
        passInput.paint(g2);
        curY += fieldH + 6;

        // Password strength meter (Registration only)
        if (isRegister) {
            int pLen = passInput.getText().length();
            int meterW = contentW;
            int meterH = 3;
            g2.setColor(Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15));
            g2.fill(new RoundRectangle2D.Float(contentX, curY, meterW, meterH, meterH, meterH));

            if (pLen > 0) {
                Color sColor = (pLen >= 8) ? Theme.SUCCESS_GREEN : ((pLen >= 5) ? Theme.ACCENT_SUNNY : Theme.ERROR_RED);
                int fillW = Math.min(meterW, Math.round(meterW * (pLen / 10f)));
                g2.setColor(sColor);
                g2.fill(new RoundRectangle2D.Float(contentX, curY, fillW, meterH, meterH, meterH));
            }
            curY += meterH + 10;
        }

        // Remember Me / Terms Checkbox
        checkX = contentX;
        checkY = curY;
        paintCheckbox(g2, checkX, checkY, isRegister ? termsAccepted : rememberMe);

        g2.setFont(Theme.fontSmall());
        g2.setColor(Theme.textSecondary());
        String checkLabel = isRegister ? "I agree to TeamUp Terms & Code of Conduct" : "Keep me signed in on this computer";
        g2.drawString(checkLabel, checkX + 24, checkY + 13);

        if (!isRegister) {
            // Forgot Password link
            g2.setFont(Theme.fontSmallBold());
            g2.setColor(Theme.BRAND_PINK);
            String fp = "Forgot password?";
            int fpX = contentX + contentW - g2.getFontMetrics().stringWidth(fp);
            g2.drawString(fp, fpX, checkY + 13);
        }

        curY += 24;

        // Main Submit Action Button
        submitBtnX = contentX;
        submitBtnY = curY;
        submitBtnW = contentW;
        submitBtnH = 40;
        String actionText = isRegister ? "Create Free Account" : "Sign In to TeamUp";
        Draw.drawPinkPillButton(g2, submitBtnX, submitBtnY, submitBtnW, submitBtnH, actionText, submitHovered, false, false, 0f, 0f);

        curY += submitBtnH + 8;

        // Inline Feedback Message
        if (!feedbackMessage.isEmpty()) {
            g2.setFont(Theme.fontSmallBold());
            FontMetrics mfm = g2.getFontMetrics();
            g2.setColor(isSuccess ? Theme.SUCCESS_GREEN : Theme.ERROR_RED);
            int mx = contentX + (contentW - mfm.stringWidth(feedbackMessage)) / 2;
            g2.drawString((isSuccess ? "✓ " : "⚠ ") + feedbackMessage, Math.max(contentX, mx), curY + 12);
            curY += 18;
        }

        // Social Login Divider
        g2.setColor(Theme.isDark ? new Color(255, 255, 255, 25) : new Color(0, 0, 0, 20));
        g2.drawLine(contentX, curY + 7, contentX + (contentW - 120) / 2, curY + 7);
        g2.drawLine(contentX + contentW - (contentW - 120) / 2, curY + 7, contentX + contentW, curY + 7);

        g2.setFont(Theme.fontSmall());
        g2.setColor(Theme.textMuted());
        String divStr = "or continue with";
        int divX = contentX + (contentW - g2.getFontMetrics().stringWidth(divStr)) / 2;
        g2.drawString(divStr, divX, curY + 11);

        curY += 18;

        // Social Buttons: Google & GitHub (Frosted glass pills)
        int sBtnGap = 12;
        int sBtnW = (contentW - sBtnGap) / 2;
        int sBtnH = 36;

        googleBtnX = contentX;
        googleBtnY = curY;
        googleBtnW = sBtnW;
        googleBtnH = sBtnH;
        paintSocialButton(g2, googleBtnX, googleBtnY, googleBtnW, googleBtnH, "Google", true, googleHovered);

        githubBtnX = contentX + sBtnW + sBtnGap;
        githubBtnY = curY;
        githubBtnW = sBtnW;
        githubBtnH = sBtnH;
        paintSocialButton(g2, githubBtnX, githubBtnY, githubBtnW, githubBtnH, "GitHub", false, githubHovered);

        curY += sBtnH + 14;

        // Switch Mode Link ("Already have an account? Log in" / "Don't have an account? Sign up")
        // Sitting comfortably inside the bottom of the card with clean padding
        g2.setFont(Theme.fontSmall());
        FontMetrics swFm = g2.getFontMetrics();
        String prefix = isRegister ? "Already have an account? " : "Don't have an account? ";
        String link = isRegister ? "Log in" : "Sign up";

        int totalSwW = swFm.stringWidth(prefix + link);
        switchLinkX = contentX + (contentW - totalSwW) / 2;
        switchLinkY = curY;
        switchLinkW = totalSwW;
        switchLinkH = 20;

        g2.setColor(Theme.textSecondary());
        g2.drawString(prefix, switchLinkX, curY + swFm.getAscent());

        g2.setColor(switchLinkHovered ? Theme.INK_DARK : Theme.BRAND_PINK);
        g2.setFont(Theme.fontSmallBold());
        g2.drawString(link, switchLinkX + swFm.stringWidth(prefix), curY + swFm.getAscent());

        // Paint Dropdown menu on top of other content if open
        if (isRegister && roleSelect.open) {
            roleSelect.paintMenu(g2);
        }

        g2.dispose();
    }

    private void paintAmbientOrb(Graphics2D g, int cx, int cy, int radius, Color color, float alpha) {
        if (alpha <= 0.01f) return;
        int steps = 6;
        for (int i = steps; i >= 1; i--) {
            int r = Math.round(radius * (i / (float) steps));
            float stepAlpha = (alpha / steps) * (1f - (i / (float) (steps + 1)));
            g.setColor(new Color(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    Math.min(1f, Math.max(0f, stepAlpha))
            ));
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void paintFieldLabel(Graphics2D g, String label, int x, int y) {
        g.setFont(Theme.fontSmallBold());
        g.setColor(Theme.textPrimary());
        g.drawString(label, x + 2, y + 11);
    }

    private void paintCheckbox(Graphics2D g, int x, int y, boolean checked) {
        RoundRectangle2D cb = new RoundRectangle2D.Float(x, y, checkSize, checkSize, 6, 6);
        if (checked) {
            g.setColor(Theme.BRAND_PINK);
            g.fill(cb);
            Draw.drawCheckIcon(g, x, y, checkSize, Color.WHITE);
        } else {
            g.setColor(Theme.isDark ? new Color(255, 255, 255, 15) : new Color(255, 255, 255, 180));
            g.fill(cb);
            g.setColor(Theme.inputBorder());
            g.setStroke(new BasicStroke(1.2f));
            g.draw(cb);
        }
    }

    private void paintSocialButton(Graphics2D g, int x, int y, int w, int h, String name, boolean isGoogle, boolean hovered) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);

        // Glass background
        if (hovered) {
            g.setColor(Theme.isDark ? new Color(255, 255, 255, 30) : new Color(255, 255, 255, 240));
        } else {
            g.setColor(Theme.isDark ? new Color(255, 255, 255, 14) : new Color(255, 255, 255, 160));
        }
        g.fill(shape);

        // Glass border with subtle white highlight
        g.setColor(hovered ? Theme.BRAND_PINK : (Theme.isDark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 20)));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        // Vector Social Icon
        int iconX = x + 24;
        int iconY = y + (h - 16) / 2;
        if (isGoogle) {
            // Draw colorful vector 'G'
            g.setColor(new Color(0x42, 0x85, 0xF4));
            g.fillOval(iconX, iconY, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.drawString("G", iconX + 4, iconY + 12);
        } else {
            // Draw vector GitHub Cat silhouette
            g.setColor(Theme.textPrimary());
            g.fillOval(iconX, iconY, 16, 16);
            g.setColor(Theme.isDark ? new Color(24, 32, 54) : Color.WHITE);
            g.fillOval(iconX + 3, iconY + 4, 10, 10);
        }

        g.setFont(Theme.fontSmallBold());
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.textPrimary());
        int tx = iconX + 26;
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(name, tx, ty);
    }

    public boolean onClick(int mx, int my, int screenW, int screenH) {
        if (!visible) return false;

        // Close button
        if (mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize) {
            hide();
            return true;
        }

        // Tab 1: Log In
        if (mx >= tab1X && mx <= tab1X + tab1W && my >= tab1Y && my <= tab1Y + tab1H) {
            isRegister = false;
            feedbackMessage = "";
            return true;
        }

        // Tab 2: Register
        if (mx >= tab2X && mx <= tab2X + tab2W && my >= tab2Y && my <= tab2Y + tab2H) {
            isRegister = true;
            feedbackMessage = "";
            return true;
        }

        // Checkbox click
        if (mx >= checkX && mx <= checkX + 300 && my >= checkY - 4 && my <= checkY + checkSize + 4) {
            if (isRegister) {
                termsAccepted = !termsAccepted;
            } else {
                rememberMe = !rememberMe;
            }
            return true;
        }

        // Submit Button
        if (mx >= submitBtnX && mx <= submitBtnX + submitBtnW && my >= submitBtnY && my <= submitBtnY + submitBtnH) {
            handleAuthSubmit();
            return true;
        }

        // Social Buttons
        if (mx >= googleBtnX && mx <= googleBtnX + googleBtnW && my >= googleBtnY && my <= googleBtnY + googleBtnH) {
            isSuccess = true;
            feedbackMessage = "Authenticated via Google Account.";
            return true;
        }

        if (mx >= githubBtnX && mx <= githubBtnX + githubBtnW && my >= githubBtnY && my <= githubBtnY + githubBtnH) {
            isSuccess = true;
            feedbackMessage = "Authenticated via GitHub Account.";
            return true;
        }

        // Switch Mode Link
        if (mx >= switchLinkX && mx <= switchLinkX + switchLinkW && my >= switchLinkY && my <= switchLinkY + switchLinkH) {
            isRegister = !isRegister;
            feedbackMessage = "";
            return true;
        }

        // Role Dropdown check (if register)
        if (isRegister && roleSelect.onClick(mx, my)) {
            return true;
        }

        // Input clicks
        if (isRegister && nameInput.onMouseDown(mx, my)) return true;
        if (emailInput.onMouseDown(mx, my)) return true;
        if (passInput.onMouseDown(mx, my)) return true;

        // Outside click closes modal
        int modalW = Math.min(510, screenW - 32);
        int modalH = getModalHeight();
        int modalX = (screenW - modalW) / 2;
        int modalY = Math.max(16, (screenH - modalH) / 2);
        if (mx < modalX || mx > modalX + modalW || my < modalY || my > modalY + modalH) {
            hide();
            return true;
        }

        return true;
    }

    public boolean onMouseMove(int mx, int my) {
        if (!visible) return false;

        closeHovered = (mx >= closeX && mx <= closeX + closeSize && my >= closeY && my <= closeY + closeSize);
        submitHovered = (mx >= submitBtnX && mx <= submitBtnX + submitBtnW && my >= submitBtnY && my <= submitBtnY + submitBtnH);
        googleHovered = (mx >= googleBtnX && mx <= googleBtnX + googleBtnW && my >= googleBtnY && my <= googleBtnY + googleBtnH);
        githubHovered = (mx >= githubBtnX && mx <= githubBtnX + githubBtnW && my >= githubBtnY && my <= githubBtnY + githubBtnH);
        switchLinkHovered = (mx >= switchLinkX && mx <= switchLinkX + switchLinkW && my >= switchLinkY && my <= switchLinkY + switchLinkH);

        tab1Hovered = (mx >= tab1X && mx <= tab1X + tab1W && my >= tab1Y && my <= tab1Y + tab1H);
        tab2Hovered = (mx >= tab2X && mx <= tab2X + tab2W && my >= tab2Y && my <= tab2Y + tab2H);

        if (isRegister) roleSelect.onMouseMove(mx, my);

        return closeHovered || submitHovered || googleHovered || githubHovered || switchLinkHovered || tab1Hovered || tab2Hovered;
    }

    public boolean onKey(KeyEvent e) {
        if (!visible) return false;

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            hide();
            return true;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleAuthSubmit();
            return true;
        }

        // Tab key navigation between inputs
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (isRegister) {
                if (nameInput.focused) {
                    nameInput.focused = false;
                    emailInput.focused = true;
                } else if (emailInput.focused) {
                    emailInput.focused = false;
                    passInput.focused = true;
                } else {
                    passInput.focused = false;
                    nameInput.focused = true;
                }
            } else {
                if (emailInput.focused) {
                    emailInput.focused = false;
                    passInput.focused = true;
                } else {
                    passInput.focused = false;
                    emailInput.focused = true;
                }
            }
            return true;
        }

        if (isRegister && nameInput.onKey(e)) return true;
        if (emailInput.onKey(e)) return true;
        if (passInput.onKey(e)) return true;

        return false;
    }
}
