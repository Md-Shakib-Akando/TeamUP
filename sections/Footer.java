import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Footer implements Section {

    private final NavBar.NavListener listener;

    private static class FooterLink {
        String label;
        String targetId;
        int x, y, w, h;
        boolean hovered;
        FooterLink(String l, String t) {
            this.label = l;
            this.targetId = t;
        }
    }

    private final List<FooterLink> links = new ArrayList<>();

    public Footer(NavBar.NavListener listener) {
        this.listener = listener;
        links.add(new FooterLink("Features", "features"));
        links.add(new FooterLink("How it works", "how-it-works"));
        links.add(new FooterLink("Interactive Demo", "demo"));
        links.add(new FooterLink("Comparison", "compare"));
        links.add(new FooterLink("FAQ", "faq"));
        links.add(new FooterLink("Early Access", "cta"));
    }

    @Override
    public String getId() {
        return "footer";
    }

    @Override
    public int getHeight(int width) {
        return width < 780 ? 340 : 240;
    }

    @Override
    public void update(float dt) {}

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);
        boolean isMobile = width < 780;

        Graphics2D g2 = (Graphics2D) g.create();

        // Dark band (#0D0C22) for contrast
        g2.setColor(Theme.INK_DARK);
        g2.fillRect(0, y, width, h);

        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(0, y, width, y);

        int curY = y + 36;

        // Brand & Description
        Draw.drawLogo(g2, padX, curY, 32);

        g2.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString("TeamUp", padX + 44, curY + (32 - tfm.getHeight()) / 2 + tfm.getAscent());

        g2.setFont(Theme.fontSmall());
        FontMetrics sfm = g2.getFontMetrics();
        g2.setColor(new Color(0xD1, 0xD5, 0xDB));
        g2.drawString("Skill-Based Team Formation & Collaboration Platform", padX, curY + 54);

        // Project credits from PRD
        g2.setColor(new Color(0x94, 0xA3, 0xB8));
        g2.drawString("Designed & Developed by Sadia Rimi, Sanjida Mostak, Md. Shakib Akando", padX, curY + 76);

        // Navigation Links
        int linksY = isMobile ? (curY + 110) : curY;
        int linksStartX = isMobile ? padX : (width - Math.min(520, Math.round(width * 0.45f)));

        g2.setFont(Theme.fontSmallBold());
        FontMetrics lfm = g2.getFontMetrics();
        int lx = linksStartX;
        int ly = linksY + 12;

        for (FooterLink fl : links) {
            fl.w = lfm.stringWidth(fl.label) + 12;
            fl.h = 24;
            fl.x = lx;
            fl.y = ly - lfm.getAscent();

            g2.setColor(fl.hovered ? Theme.BRAND_PINK : new Color(0xD1, 0xD5, 0xDB));
            g2.drawString(fl.label, lx, ly);

            lx += fl.w + 14;
            if (isMobile && lx > width - padX - 80) {
                lx = linksStartX;
                ly += 28;
            }
        }

        // Bottom Strip: Keyboard shortcuts & Copyright
        int bottomY = y + h - 28;
        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawLine(padX, bottomY - 14, width - padX, bottomY - 14);

        g2.setFont(Theme.fontSmall());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(new Color(0x94, 0xA3, 0xB8));
        g2.drawString("© 2026 TeamUp Platform. All rights reserved.", padX, bottomY);

        String shortcuts = "Shortcuts: [M] Toggle Motion  ·  [Ctrl +/-] Scale Font  ·  [Tab/Enter] Navigate";
        int sw = bfm.stringWidth(shortcuts);
        if (!isMobile && width - padX - sw > padX + 300) {
            g2.drawString(shortcuts, width - padX - sw, bottomY);
        }

        g2.dispose();
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (FooterLink fl : links) {
            if (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h) {
                if (listener != null) listener.scrollToSection(fl.targetId);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (FooterLink fl : links) {
            boolean was = fl.hovered;
            fl.hovered = (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h);
            if (fl.hovered != was) changed = true;
        }
        return changed;
    }

    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }
    @Override
    public boolean onKey(KeyEvent e) { return false; }
}

