import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

public class Cta implements Section {

    private final TextBox emailBox;
    private final Dropdown roleDropdown;
    private final Button joinBtn;

    private String statusMessage = "";
    private boolean isSuccess = false;
    private float statusFade = 0f;

    private final Anim.RevealState reveal = new Anim.RevealState();

    public Cta() {
        emailBox = new TextBox("Enter your email address...");
        roleDropdown = new Dropdown(Arrays.asList("Student", "Freelancer", "Developer", "Researcher", "Startup"));
        joinBtn = new Button("Join Early Access →", true, this::submitLead);
        joinBtn.isWarm = true;
        reveal.trigger();
    }

    private void submitLead() {
        String email = emailBox.getText();
        String role = roleDropdown.getSelected();

        LeadStore.ValidationResult res = LeadStore.saveLead(email, role);
        this.statusMessage = res.message;
        this.isSuccess = res.success;
        this.statusFade = 1f;

        if (res.success) {
            emailBox.setText("");
            emailBox.hasError = false;
        } else {
            emailBox.hasError = true;
        }
    }

    @Override
    public String getId() {
        return "cta";
    }

    @Override
    public int getHeight(int width) {
        return width < 780 ? 520 : 420;
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        emailBox.update(dt);
        if (statusFade > 0f) {
            // Keep status visible
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);
        boolean isMobile = width < 780;

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int panelX = padX;
        int panelY = y + 20;
        int panelW = width - padX * 2;
        int panelH = h - 40;
        int radius = 28;

        RoundRectangle2D panelShape = new RoundRectangle2D.Float(panelX, panelY, panelW, panelH, radius, radius);

        // Dark band (#0D0C22) for Final CTA contrast
        g2.setColor(Theme.INK_DARK);
        g2.fill(panelShape);

        // Ambient radial glow in top right and bottom left
        Graphics2D gClip = (Graphics2D) g2.create();
        gClip.setClip(panelShape);

        Point2D p1 = new Point2D.Float(panelX + panelW * 0.85f, panelY + 40);
        RadialGradientPaint rgp1 = new RadialGradientPaint(
                p1, 240f, new float[]{0f, 1f},
                new Color[]{new Color(0x7C, 0x5C, 0xFF, 55), new Color(0x7C, 0x5C, 0xFF, 0)}
        );
        gClip.setPaint(rgp1);
        gClip.fill(panelShape);

        Point2D p2 = new Point2D.Float(panelX + panelW * 0.15f, panelY + panelH - 40);
        RadialGradientPaint rgp2 = new RadialGradientPaint(
                p2, 220f, new float[]{0f, 1f},
                new Color[]{new Color(0xEA, 0x4C, 0x89, 50), new Color(0xEA, 0x4C, 0x89, 0)}
        );
        gClip.setPaint(rgp2);
        gClip.fill(panelShape);
        gClip.dispose();

        // Subtle gradient highlight border
        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(panelShape);

        // Content centered
        int contentY = panelY + 36;

        // Eyebrow badge
        int badgeW = 160;
        int badgeX = panelX + (panelW - badgeW) / 2;
        Draw.drawSectionBadge(g2, "GET EARLY ACCESS", badgeX, contentY, Theme.BRAND_PINK);

        // Headline
        contentY += 46;
        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        String heading = "Ready to build your next breakthrough team?";
        int headX = panelX + (panelW - tfm.stringWidth(heading)) / 2;
        if (headX < panelX + 24) headX = panelX + 24;
        g2.drawString(heading, headX, contentY + tfm.getAscent());

        // Subtitle
        int subY = contentY + tfm.getHeight() + 10;
        g2.setFont(Theme.fontBody());
        FontMetrics sfm = g2.getFontMetrics();
        g2.setColor(new Color(0xD1, 0xD5, 0xDB));
        String sub = "Join students, researchers, and creators already collaborating on TeamUp.";
        int subX = panelX + (panelW - sfm.stringWidth(sub)) / 2;
        if (subX < panelX + 24) subX = panelX + 24;
        g2.drawString(sub, subX, subY + sfm.getAscent());

        // Form layout
        int formY = subY + sfm.getHeight() + 32;

        if (!isMobile) {
            int formW = 710;
            int formX = panelX + (panelW - formW) / 2;
            int itemH = 46;

            emailBox.setBounds(formX, formY, 320, itemH);
            roleDropdown.setBounds(formX + 332, formY, 170, itemH);
            joinBtn.setBounds(formX + 514, formY, 196, itemH);

            emailBox.paint(g2);
            joinBtn.paint(g2);
            // Dropdown painted last so popup menu renders on top!
            roleDropdown.paint(g2);

            // Inline Status Feedback
            if (!statusMessage.isEmpty()) {
                paintStatus(g2, panelX + (panelW - 600) / 2, formY + itemH + 16, 600);
            }

            // Trust badges row
            int trustY = formY + itemH + 34;
            g2.setFont(Theme.fontSmall());
            g2.setColor(new Color(0x94, 0xA3, 0xB8));
            String trust = "🔒 PBKDF2 Encrypted   ·   ⚡ Instant Setup   ·   🎓 100% Free for Students";
            FontMetrics trFm = g2.getFontMetrics();
            g2.drawString(trust, panelX + (panelW - trFm.stringWidth(trust)) / 2, trustY);

        } else {
            int itemH = 44;
            int fieldW = Math.min(360, panelW - 48);
            int fieldX = panelX + (panelW - fieldW) / 2;

            emailBox.setBounds(fieldX, formY, fieldW, itemH);
            roleDropdown.setBounds(fieldX, formY + itemH + 12, fieldW, itemH);
            joinBtn.setBounds(fieldX, formY + (itemH + 12) * 2, fieldW, itemH);

            emailBox.paint(g2);
            joinBtn.paint(g2);
            roleDropdown.paint(g2);

            if (!statusMessage.isEmpty()) {
                paintStatus(g2, fieldX, formY + (itemH + 12) * 3 + 12, fieldW);
            }
        }

        g2.dispose();
    }

    private void paintStatus(Graphics2D g, int x, int y, int w) {
        g.setFont(Theme.fontBodyBold());
        FontMetrics fm = g.getFontMetrics();

        Color col = isSuccess ? Theme.SUCCESS_GREEN : Theme.ERROR_RED;
        g.setColor(col);

        int textX = x + (w - fm.stringWidth(statusMessage)) / 2;
        if (textX < x) textX = x;
        g.drawString((isSuccess ? "✓  " : "⚠  ") + statusMessage, textX, y + fm.getAscent());
    }

    @Override
    public boolean onClick(int mx, int my) {
        if (roleDropdown.onClick(mx, my)) return true;
        if (emailBox.onMouseDown(mx, my)) return true;
        if (joinBtn.onMouseUp(mx, my)) return true;
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean c1 = emailBox.contains(mx, my);
        boolean c2 = roleDropdown.onMouseMove(mx, my);
        boolean c3 = joinBtn.onMouseMove(mx, my);
        return c1 || c2 || c3;
    }

    @Override
    public boolean onMouseDown(int mx, int my) {
        if (roleDropdown.contains(mx, my)) {
            return true;
        }
        boolean b1 = emailBox.onMouseDown(mx, my);
        boolean b2 = joinBtn.onMouseDown(mx, my);
        return b1 || b2;
    }

    @Override
    public boolean onMouseUp(int mx, int my) {
        return joinBtn.onMouseUp(mx, my);
    }

    @Override
    public boolean onKey(KeyEvent e) {
        if (emailBox.focused) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                submitLead();
                return true;
            }
            return emailBox.onKey(e);
        }
        return false;
    }
}
