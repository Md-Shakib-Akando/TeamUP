import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern, multi-column, fully responsive Footer for TeamUp.
 * Includes interactive newsletter subscription, live system status beacon,
 * product navigation links, audience solutions, and keyboard shortcuts guide.
 */
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

    private static class SocialPill {
        String name;
        int x, y, w, h;
        boolean hovered;
        SocialPill(String name) { this.name = name; }
    }

    private final List<FooterLink> platformLinks = new ArrayList<FooterLink>();
    private final List<FooterLink> audienceLinks = new ArrayList<FooterLink>();
    private final List<SocialPill> socialPills = new ArrayList<SocialPill>();

    // Interactive Newsletter state
    private String newsletterEmail = "";
    private boolean newsletterFocused = false;
    private String newsletterFeedback = "";
    private int newsInputX, newsInputY, newsInputW = 200, newsInputH = 36;
    private int newsBtnX, newsBtnY, newsBtnW = 90, newsBtnH = 36;
    private boolean newsBtnHovered = false;

    public Footer(NavBar.NavListener listener) {
        this.listener = listener;

        // Platform links
        platformLinks.add(new FooterLink("Features & Bento Grid", "features"));
        platformLinks.add(new FooterLink("How it Works (4 Steps)", "how-it-works"));
        platformLinks.add(new FooterLink("Interactive Matching Engine", "demo"));
        platformLinks.add(new FooterLink("Comparison Matrix", "compare"));
        platformLinks.add(new FooterLink("Frequently Asked Questions", "faq"));
        platformLinks.add(new FooterLink("Early Access Sign-Up", "cta"));

        // Audience links
        audienceLinks.add(new FooterLink("CS Students & Hackathons", "audience"));
        audienceLinks.add(new FooterLink("Design & Frontend Creators", "audience"));
        audienceLinks.add(new FooterLink("Academic Research Labs", "audience"));
        audienceLinks.add(new FooterLink("Freelancer Client Pods", "audience"));
        audienceLinks.add(new FooterLink("Early Startup Builders", "audience"));

        // Social pills
        socialPills.add(new SocialPill("GitHub"));
        socialPills.add(new SocialPill("Discord"));
        socialPills.add(new SocialPill("LinkedIn"));
        socialPills.add(new SocialPill("X (Twitter)"));
    }

    @Override
    public String getId() {
        return "footer";
    }

    @Override
    public int getHeight(int width) {
        if (width < 680) {
            return 580;
        } else if (width < 1040) {
            return 440;
        } else {
            return 340;
        }
    }

    @Override
    public void update(float dt) {}

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1200 ? 56 : (width > 600 ? 28 : 20);
        boolean isMobile = width < 680;
        boolean isTablet = width >= 680 && width < 1040;

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // Deep Ink Dark Surface (#0D0C22)
        g2.setColor(Theme.INK_DARK);
        g2.fillRect(0, y, width, h);

        // Ambient top highlight border
        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(0, y, width, y);

        int contentTop = y + 36;
        int totalW = width - padX * 2;

        if (!isMobile && !isTablet) {
            // ================= 4-COLUMN DESKTOP LAYOUT =================
            int colGap = 28;
            int colW = (totalW - colGap * 3) / 4;

            // Col 1: Brand & Status
            int col1X = padX;
            paintBrandColumn(g2, col1X, contentTop, colW);

            // Col 2: Platform Links
            int col2X = col1X + colW + colGap;
            paintLinksColumn(g2, col2X, contentTop, colW, "PLATFORM", platformLinks);

            // Col 3: Audience Solutions
            int col3X = col2X + colW + colGap;
            paintLinksColumn(g2, col3X, contentTop, colW, "SOLUTIONS", audienceLinks);

            // Col 4: Newsletter
            int col4X = col3X + colW + colGap;
            paintNewsletterColumn(g2, col4X, contentTop, colW);

        } else if (isTablet) {
            // ================= 2x2 TABLET GRID =================
            int colGap = 32;
            int colW = (totalW - colGap) / 2;

            // Row 1: Brand (left) & Newsletter (right)
            paintBrandColumn(g2, padX, contentTop, colW);
            paintNewsletterColumn(g2, padX + colW + colGap, contentTop, colW);

            // Row 2: Platform Links (left) & Solutions (right)
            int row2Y = contentTop + 140;
            paintLinksColumn(g2, padX, row2Y, colW, "PLATFORM", platformLinks);
            paintLinksColumn(g2, padX + colW + colGap, row2Y, colW, "SOLUTIONS", audienceLinks);

        } else {
            // ================= 1-COLUMN MOBILE LAYOUT =================
            int curY = contentTop;
            paintBrandColumn(g2, padX, curY, totalW);
            curY += 130;

            paintNewsletterColumn(g2, padX, curY, totalW);
            curY += 120;

            paintLinksColumn(g2, padX, curY, totalW, "QUICK LINKS", platformLinks);
        }

        // ================= BOTTOM BAR =================
        int bottomY = y + h - 34;
        g2.setColor(new Color(255, 255, 255, 18));
        g2.drawLine(padX, bottomY - 14, width - padX, bottomY - 14);

        g2.setFont(Theme.fontSmall());
        FontMetrics bfm = g2.getFontMetrics();
        g2.setColor(new Color(0x94, 0xA3, 0xB8));
        g2.drawString("© 2026 TeamUp Platform. Built with Pure Core Java.", padX, bottomY);

        // Social pills on the right
        int curSocX = width - padX;
        for (int i = socialPills.size() - 1; i >= 0; i--) {
            SocialPill sp = socialPills.get(i);
            sp.w = bfm.stringWidth(sp.name) + 16;
            sp.h = 22;
            sp.x = curSocX - sp.w;
            sp.y = bottomY - bfm.getAscent() - 2;

            RoundRectangle2D sBox = new RoundRectangle2D.Float(sp.x, sp.y, sp.w, sp.h, 6, 6);
            if (sp.hovered) {
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fill(sBox);
                g2.setColor(Theme.BRAND_PINK);
            } else {
                g2.setColor(new Color(0x94, 0xA3, 0xB8));
            }
            g2.drawString(sp.name, sp.x + 8, sp.y + 15);

            curSocX = sp.x - 8;
        }

        g2.dispose();
    }

    private void paintBrandColumn(Graphics2D g, int x, int y, int w) {
        // Logo & Title
        Draw.drawLogo(g, x, y, 32);

        g.setFont(Theme.fontCardTitle());
        FontMetrics tfm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString("TeamUp", x + 42, y + (32 - tfm.getHeight()) / 2 + tfm.getAscent());

        // Platform subtitle
        g.setFont(Theme.fontSmall());
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.drawString("Skill-Based Team Formation Platform", x, y + 54);

        // Status Badge with pulsing green radar beacon
        int statusW = 162;
        int statusH = 24;
        int statusY = y + 68;
        RoundRectangle2D sPill = new RoundRectangle2D.Float(x, statusY, statusW, statusH, statusH, statusH);
        g.setColor(new Color(0x10, 0xB9, 0x81, 28));
        g.fill(sPill);
        g.setColor(new Color(0x10, 0xB9, 0x81, 65));
        g.setStroke(new BasicStroke(1f));
        g.draw(sPill);

        Draw.drawRadarBeacon(g, x + 12, statusY + statusH / 2, Theme.SUCCESS_GREEN);

        g.setFont(Theme.fontSmallBold());
        g.setColor(new Color(0x6E, 0xEE, 0xB0));
        g.drawString("All systems operational", x + 24, statusY + 16);

        // Credits
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(new Color(0x94, 0xA3, 0xB8));
        g.drawString("Built by Sadia Rimi, Sanjida Mostak, Shakib Akando", x, y + 110);
    }

    private void paintLinksColumn(Graphics2D g, int x, int y, int w, String heading, List<FooterLink> linkList) {
        g.setFont(Theme.fontSmallBold());
        g.setColor(Color.WHITE);
        g.drawString(heading, x, y + 16);

        g.setFont(Theme.fontSmall());
        FontMetrics fm = g.getFontMetrics();
        int curY = y + 36;

        for (FooterLink fl : linkList) {
            fl.x = x;
            fl.y = curY;
            fl.w = fm.stringWidth(fl.label) + 6;
            fl.h = 20;

            if (fl.hovered) {
                g.setColor(Theme.BRAND_PINK);
            } else {
                g.setColor(new Color(0xD1, 0xD5, 0xDB));
            }
            g.drawString(fl.label, fl.x, curY + fm.getAscent());
            curY += 24;
        }
    }

    private void paintNewsletterColumn(Graphics2D g, int x, int y, int w) {
        g.setFont(Theme.fontSmallBold());
        g.setColor(Color.WHITE);
        g.drawString("STAY IN THE LOOP", x, y + 16);

        g.setFont(Theme.fontSmall());
        g.setColor(new Color(0x94, 0xA3, 0xB8));
        g.drawString("Receive product release notes & early invites.", x, y + 36);

        int formY = y + 50;
        newsBtnW = 86;
        newsInputW = Math.max(120, w - newsBtnW - 8);
        newsInputH = 34;
        newsBtnH = 34;

        newsInputX = x;
        newsInputY = formY;

        newsBtnX = x + newsInputW + 8;
        newsBtnY = formY;

        // Input Box
        RoundRectangle2D inBox = new RoundRectangle2D.Float(newsInputX, newsInputY, newsInputW, newsInputH, 8, 8);
        g.setColor(new Color(255, 255, 255, 16));
        g.fill(inBox);
        g.setColor(newsletterFocused ? Theme.BRAND_PINK : new Color(255, 255, 255, 35));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(inBox);

        g.setFont(Theme.fontSmall());
        if (newsletterEmail.isEmpty() && !newsletterFocused) {
            g.setColor(new Color(0x94, 0xA3, 0xB8));
            g.drawString("Your email address...", newsInputX + 10, newsInputY + 22);
        } else {
            g.setColor(Color.WHITE);
            String cur = (newsletterFocused && (System.currentTimeMillis() % 1000 < 500)) ? "|" : "";
            g.drawString(newsletterEmail + cur, newsInputX + 10, newsInputY + 22);
        }

        // Subscribe Button
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(newsBtnX, newsBtnY, newsBtnW, newsBtnH, 8, 8);
        g.setColor(newsBtnHovered ? Theme.BRAND_PINK.brighter() : Theme.BRAND_PINK);
        g.fill(sBtn);

        g.setFont(Theme.fontSmallBold());
        g.setColor(Color.WHITE);
        FontMetrics bfm = g.getFontMetrics();
        g.drawString("Subscribe", newsBtnX + (newsBtnW - bfm.stringWidth("Subscribe")) / 2, newsBtnY + 22);

        // Feedback message
        if (!newsletterFeedback.isEmpty()) {
            g.setFont(Theme.fontSmall());
            g.setColor(Theme.SUCCESS_GREEN);
            g.drawString(newsletterFeedback, x, formY + newsInputH + 18);
        }
    }

    private void submitNewsletter() {
        if (newsletterEmail.trim().isEmpty() || !newsletterEmail.contains("@")) {
            newsletterFeedback = "Please enter a valid email address.";
            return;
        }
        LeadStore.saveLead(newsletterEmail.trim(), "Newsletter");
        newsletterFeedback = "✓ Subscribed! You're on the list.";
        newsletterEmail = "";
    }

    @Override
    public boolean onClick(int mx, int my) {
        // Platform links
        for (FooterLink fl : platformLinks) {
            if (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h) {
                if (listener != null) listener.scrollToSection(fl.targetId);
                return true;
            }
        }

        // Audience links
        for (FooterLink fl : audienceLinks) {
            if (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h) {
                if (listener != null) listener.scrollToSection(fl.targetId);
                return true;
            }
        }

        // Newsletter Input Focus
        if (mx >= newsInputX && mx <= newsInputX + newsInputW && my >= newsInputY && my <= newsInputY + newsInputH) {
            newsletterFocused = true;
            return true;
        } else {
            newsletterFocused = false;
        }

        // Newsletter Submit Button
        if (mx >= newsBtnX && mx <= newsBtnX + newsBtnW && my >= newsBtnY && my <= newsBtnY + newsBtnH) {
            submitNewsletter();
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        for (FooterLink fl : platformLinks) {
            boolean was = fl.hovered;
            fl.hovered = (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h);
            if (fl.hovered != was) changed = true;
        }

        for (FooterLink fl : audienceLinks) {
            boolean was = fl.hovered;
            fl.hovered = (mx >= fl.x && mx <= fl.x + fl.w && my >= fl.y && my <= fl.y + fl.h);
            if (fl.hovered != was) changed = true;
        }

        for (SocialPill sp : socialPills) {
            boolean was = sp.hovered;
            sp.hovered = (mx >= sp.x && mx <= sp.x + sp.w && my >= sp.y && my <= sp.y + sp.h);
            if (sp.hovered != was) changed = true;
        }

        boolean pNews = newsBtnHovered;
        newsBtnHovered = (mx >= newsBtnX && mx <= newsBtnX + newsBtnW && my >= newsBtnY && my <= newsBtnY + newsBtnH);
        if (pNews != newsBtnHovered) changed = true;

        return changed;
    }

    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }

    @Override
    public boolean onKey(KeyEvent e) {
        if (newsletterFocused) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                submitNewsletter();
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (newsletterEmail.length() > 0) {
                    newsletterEmail = newsletterEmail.substring(0, newsletterEmail.length() - 1);
                    return true;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                newsletterFocused = false;
                return true;
            } else {
                char ch = e.getKeyChar();
                if (ch >= 32 && ch <= 126) {
                    newsletterEmail += ch;
                    return true;
                }
            }
        }
        return false;
    }
}
