import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Final call-to-action: living aurora panel, early-access form and a confetti burst on success. */
public class Cta extends LpSection {

    private final TextBox emailBox;
    private final Dropdown roleDropdown;
    private final Button joinBtn;
    private final Elastic joinHov = new Elastic(0f, 260f, 20f);

    private String statusMessage = "";
    private boolean isSuccess = false;
    private float statusT = 0f;

    private static class Bit {
        float x, y, vx, vy, rot, vr, life, size;
        Color c;
    }
    private final List<Bit> confetti = new ArrayList<>();
    private final Random rnd = new Random();
    private int panelX, panelY, panelW, panelH;

    public Cta() {
        emailBox = new TextBox("abcd@gmail.com");
        roleDropdown = new Dropdown(Arrays.asList("Student", "Freelancer", "Developer", "Researcher", "Startup"));
        joinBtn = new Button("Join early access", true, this::submitLead);
    }

    public boolean hasFocus() { return emailBox.focused; }

    private void submitLead() {
        LeadStore.ValidationResult res = LeadStore.saveLead(emailBox.getText(), roleDropdown.getSelected());
        statusMessage = res.message;
        isSuccess = res.success;
        statusT = 0.08f;
        if (res.success) {
            emailBox.setText("");
            emailBox.hasError = false;
            burst(joinBtn.x + joinBtn.width / 2f, joinBtn.y + joinBtn.height / 2f);
        } else {
            emailBox.hasError = true;
        }
    }

    private void validateWhileEditing() {
        String txt = emailBox.getText().trim();
        if (txt.isEmpty()) {
            if (!isSuccess) {
                emailBox.hasError = false;
                statusMessage = "";
            }
            return;
        }
        if (LeadStore.isValidEmail(txt)) {
            emailBox.hasError = false;
            if (!isSuccess) statusMessage = "";
        } else if (!isSuccess && !statusMessage.isEmpty()) {
            emailBox.hasError = true;
            statusMessage = "Invalid email structure - please enter a valid email (e.g. abcd@gmail.com).";
        }
    }

    private void burst(float x, float y) {
        Color[] cols = {Fx.PINK, Fx.VIOLET, Fx.CYAN, Fx.MINT, Fx.AMBER, Color.WHITE};
        for (int i = 0; i < 90; i++) {
            Bit b = new Bit();
            b.x = x; b.y = y;
            double ang = -Math.PI / 2 + (rnd.nextDouble() - 0.5) * Math.PI * 1.3;
            float sp = 260 + rnd.nextFloat() * 420;
            b.vx = (float) Math.cos(ang) * sp;
            b.vy = (float) Math.sin(ang) * sp;
            b.rot = rnd.nextFloat() * 6.28f;
            b.vr = (rnd.nextFloat() - 0.5f) * 14f;
            b.life = 1.6f + rnd.nextFloat() * 1.0f;
            b.size = 5 + rnd.nextFloat() * 6;
            b.c = cols[rnd.nextInt(cols.length)];
            confetti.add(b);
        }
    }

    @Override public String getId() { return "cta"; }

    @Override
    public void update(float dt) {
        super.update(dt);
        emailBox.update(dt);
        statusT += dt;
        joinHov.setTarget(joinBtn.hovered ? 1f : 0f);
        joinHov.update(dt);
        for (int i = confetti.size() - 1; i >= 0; i--) {
            Bit b = confetti.get(i);
            b.vy += 900 * dt;
            b.vx *= (1 - 1.2f * dt);
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            b.rot += b.vr * dt;
            b.life -= dt;
            if (b.life <= 0) confetti.remove(i);
        }
    }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 60 : 90);
        int[] c = Fx.container(width);
        boolean mobile = c[1] < 760;
        int pw = c[1];
        int px = c[0];

        // measure content
        Font hf = Fx.display(mobile ? 34 : 54);
        String h1 = "Ready to build your", h2 = "breakthrough team?";
        int ph = 76 + 30 + 28 + 2 * Math.round(g.getFontMetrics(hf).getHeight() * 1.02f) + 16
                + Fx.paragraphHeight(g, "Join students, researchers and creators already collaborating on TeamUp. Early access is free.", Fx.body(17), Math.min(560, pw - 60), 1.5f)
                + 36 + (mobile ? 3 * 52 + 12 : 56) + 26 + 22 + 76;
        if (!measuring) { panelX = px; panelY = cur; panelW = pw; panelH = ph; }
        if (measuring) return cur + ph - top + 60;

        Graphics2D p = Fx.fade(g, rv.p(0f, 0.9f), 40);
        RoundRectangle2D.Float panel = Fx.rr(px, cur, pw, ph, 32);

        // outer glow
        Fx.glowOval(p, px + pw / 2f, cur + ph / 2f, pw * 0.6f, ph * 0.8f, Fx.VIOLET, Fx.dark() ? 0.25f : 0.2f);
        Fx.softShadow(p, px, cur, pw, ph, 32, 0.6f, 30);

        // panel fill: deep ink in dark mode, vivid gradient in light mode
        float lt = Theme.themeTransition;
        p.setColor(Fx.mix(new Color(0x0C, 0x0A, 0x22), new Color(0x4C, 0x1D, 0x95), lt));
        p.fill(panel);
        Graphics2D in = (Graphics2D) p.create();
        in.clip(panel);
        float t = Fx.time();
        Fx.glow(in, px + pw * (0.2f + 0.08f * (float) Math.sin(t * 0.5f)), cur + ph * 0.1f, pw * 0.5f, Fx.PINK, 0.55f);
        Fx.glow(in, px + pw * (0.85f + 0.06f * (float) Math.cos(t * 0.4f)), cur + ph * 0.3f, pw * 0.45f, Fx.VIOLET, 0.6f);
        Fx.glow(in, px + pw * (0.55f + 0.1f * (float) Math.sin(t * 0.3f + 1)), cur + ph * 1.05f, pw * 0.5f, Fx.CYAN, 0.35f);
        // perspective grid floor
        float horizon = cur + ph * 0.6f;
        in.setPaint(new GradientPaint(0, horizon, new Color(255, 255, 255, 0), 0, cur + ph, new Color(255, 255, 255, 34)));
        for (int i = -14; i <= 14; i++) {
            float bx = px + pw / 2f + i * 34;
            in.drawLine(Math.round(px + pw / 2f + i * 4), Math.round(horizon), Math.round(bx + i * 60), cur + ph);
        }
        float scroll = Theme.motionEnabled ? (t * 0.25f) % 1f : 0f;
        for (int i = 0; i < 8; i++) {
            float k = ((i + scroll) / 8f);
            float ly = horizon + (cur + ph - horizon) * k * k;
            in.setColor(new Color(255, 255, 255, Math.round(26 * k * k)));
            in.drawLine(px, Math.round(ly), px + pw, Math.round(ly));
        }
        in.dispose();
        Fx.gradientBorder(p, panel, 1.5f, 0.9f, t * 0.4f);

        float cx = px + pw / 2f;
        int yy = cur + 76;

        // eyebrow (white-on-glass)
        p.setFont(Fx.label(11.5f));
        String eb = "EARLY ACCESS IS OPEN";
        int ebw = p.getFontMetrics().stringWidth(eb) + 44;
        RoundRectangle2D.Float ebp = Fx.rr(cx - ebw / 2f, yy, ebw, 30, 15);
        p.setColor(new Color(255, 255, 255, 22));
        p.fill(ebp);
        p.setColor(new Color(255, 255, 255, 60));
        p.draw(ebp);
        float pulse = (t % 1.6f) / 1.6f;
        Fx.glow(p, cx - ebw / 2f + 16, yy + 15, 8 + pulse * 8, Fx.MINT, 0.8f * (1 - pulse));
        p.setColor(Fx.MINT);
        p.fill(new java.awt.geom.Ellipse2D.Float(cx - ebw / 2f + 13, yy + 12, 6, 6));
        p.setColor(Color.WHITE);
        Fx.textMid(p, eb, cx - ebw / 2f + 27, yy + 15);
        yy += 30 + 28;

        p.setFont(hf);
        FontMetrics fm = p.getFontMetrics();
        int lh = Math.round(fm.getHeight() * 1.02f);
        p.setColor(Color.WHITE);
        p.drawString(h1, cx - fm.stringWidth(h1) / 2f, yy + fm.getAscent());
        yy += lh;
        Fx.gradientText(p, h2, cx - fm.stringWidth(h2) / 2f, yy + fm.getAscent(),
                new Color[]{new Color(0xFF, 0xB3, 0xD1), Color.WHITE, new Color(0xA5, 0xF3, 0xFC)}, Fx.wave(0.5f, 0) * 0.3f);
        yy += lh + 16;
        int sw = Math.min(560, pw - 60);
        yy = Fx.paragraph(p, "Join students, researchers and creators already collaborating on TeamUp. Early access is free.",
                Fx.body(17), new Color(255, 255, 255, 190), Math.round(cx - sw / 2f), yy, sw, 1.5f, true);
        yy += 36;

        // form
        if (!mobile) {
            int fw = Math.min(720, pw - 60);
            int fx = Math.round(cx - fw / 2f);
            int ih = 56;
            int bw = 210, dw = 168;
            int ew = fw - bw - dw - 20;
            emailBox.setBounds(fx, yy, ew, ih);
            roleDropdown.setBounds(fx + ew + 10, yy, dw, ih);
            joinBtn.setBounds(fx + ew + dw + 20, yy, bw, ih);
            yy += ih;
        } else {
            int fw = Math.min(380, pw - 40);
            int fx = Math.round(cx - fw / 2f);
            emailBox.setBounds(fx, yy, fw, 52);
            roleDropdown.setBounds(fx, yy + 64, fw, 52);
            joinBtn.setBounds(fx, yy + 128, fw, 52);
            yy += 52 * 3 + 24;
        }
        emailBox.paint(p);
        Fx.primaryButton(p, joinBtn.x, joinBtn.y, joinBtn.width, joinBtn.height, joinBtn.text, joinHov.getValue(), joinBtn.pressed ? 1f : 0f, true);

        yy += 26;
        if (!statusMessage.isEmpty()) {
            float sp = Fx.easeOutQuart(statusT / 0.4f);
            Graphics2D st = Fx.fade(p, sp, 8);
            st.setFont(Fx.bold(14));
            Color col = isSuccess ? Fx.MINT : new Color(0xFF, 0x8A, 0x8A);
            int tw = st.getFontMetrics().stringWidth(statusMessage);
            float sx = cx - (tw + 26) / 2f;
            if (isSuccess) Fx.checkCircle(st, sx, yy - 2, 18, Fx.MINT, 1f);
            else {
                st.setColor(col);
                st.fill(new java.awt.geom.Ellipse2D.Float(sx + 4, yy + 3, 9, 9));
            }
            st.setColor(col);
            Fx.textMid(st, statusMessage, sx + 26, yy + 7);
            st.dispose();
        } else {
            // trust row
            String[] items = {"Encrypted with PBKDF2", "Set up in seconds", "Free for students"};
            String[] icons = {"lock", "bolt", "check"};
            p.setFont(Fx.body(13.5f));
            FontMetrics tfm = p.getFontMetrics();
            int total = 0;
            for (String s : items) total += tfm.stringWidth(s) + 22 + 28;
            total -= 28;
            float tx = cx - total / 2f;
            for (int i = 0; i < items.length; i++) {
                Fx.icon(p, icons[i], tx, yy - 1, 15, new Color(255, 255, 255, 170));
                p.setColor(new Color(255, 255, 255, 170));
                Fx.textMid(p, items[i], tx + 22, yy + 7);
                tx += tfm.stringWidth(items[i]) + 22 + 28;
            }
        }

        // dropdown last so its menu overlays everything
        roleDropdown.paint(p);
        p.dispose();

        // confetti on top of everything in this section
        for (Bit b : confetti) {
            Graphics2D cg = (Graphics2D) g.create();
            Fx.fadeBy(cg, Fx.clamp(b.life / 0.6f));
            cg.translate(b.x, b.y);
            cg.rotate(b.rot);
            cg.setColor(b.c);
            cg.fill(new Rectangle2D.Float(-b.size / 2f, -b.size / 4f, b.size, b.size / 2f));
            cg.dispose();
        }

        return cur + ph - top + 60;
    }

    @Override
    public boolean onClick(int mx, int my) {
        if (roleDropdown.onClick(mx, my)) return true;
        if (emailBox.onMouseDown(mx, my)) return true;
        return Fx.inside(mx, my, joinBtn.x, joinBtn.y, joinBtn.width, joinBtn.height);
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean d = roleDropdown.contains(mx, my);
        roleDropdown.onMouseMove(mx, my);
        joinBtn.onMouseMove(mx, my);
        return d || joinBtn.hovered;
    }

    @Override
    public boolean onMouseDown(int mx, int my) {
        if (roleDropdown.contains(mx, my)) return true;
        boolean wasFocused = emailBox.focused;
        boolean b1 = emailBox.onMouseDown(mx, my);
        boolean b2 = joinBtn.onMouseDown(mx, my);
        if (wasFocused && !b1 && !b2) {
            String cur = emailBox.getText().trim();
            if (!cur.isEmpty() && !LeadStore.isValidEmail(cur)) {
                emailBox.hasError = true;
                isSuccess = false;
                statusMessage = "Invalid email structure - please enter a valid email (e.g. abcd@gmail.com).";
                statusT = 0.08f;
            }
        }
        return b1 || b2;
    }

    @Override
    public boolean onMouseUp(int mx, int my) {
        return joinBtn.onMouseUp(mx, my);
    }

    @Override
    public boolean onKey(KeyEvent e) {
        if (!emailBox.focused) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                submitLead();
                return true;
            }
            if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(e.getKeyChar()) || e.isControlDown()) {
                boolean handled = emailBox.onKey(e);
                if (handled) validateWhileEditing();
                return handled;
            }
            return true;
        }
        if (e.getID() == KeyEvent.KEY_TYPED) {
            boolean handled = emailBox.onKey(e);
            if (handled) validateWhileEditing();
            return handled;
        }
        return false;
    }
}
