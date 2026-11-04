import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Audience implements Section {

    public static class RoleTab {
        String name;
        String persona;
        String benefit;
        String useCase;
        int x, y, w, h;
        boolean hovered = false;

        RoleTab(String name, String persona, String benefit, String useCase) {
            this.name = name;
            this.persona = persona;
            this.benefit = benefit;
            this.useCase = useCase;
        }
    }

    private final List<RoleTab> tabs = new ArrayList<>();
    private int selectedTabIndex = 0;
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Audience() {
        tabs.add(new RoleTab(
                "Students",
                "Rafi · CS Undergraduate",
                "Find capable classmates who actually write code instead of getting stuck carrying group assignments alone.",
                "\"I posted our semester software engineering project needing a Java backend lead and UI designer. In two hours, our team was formed.\""
        ));
        tabs.add(new RoleTab(
                "Freelancers",
                "Nila · Independent Designer",
                "Form reliable sub-contracting pods and build a verified rating history that wins high-value client contracts.",
                "\"Instead of turning down full-stack client inquiries, I pair with vetted Java and database developers and deliver turn-key projects.\""
        ));
        tabs.add(new RoleTab(
                "Developers",
                "Tanvir · Full-Stack Builder",
                "Join ambitious hackathons and open-source ventures matched to your exact engineering strengths.",
                "\"I filter by technical stacks I want to work with. No time wasted on mismatched skill sets or ghosted chats.\""
        ));
        tabs.add(new RoleTab(
                "Researchers",
                "Dr. Hasan · Lab Director",
                "Recruit specialized technical contributors for scientific prototyping and computational modeling.",
                "\"We needed someone with deep algorithmic optimization experience for our bioinformatics simulation. TeamUp matched us instantly.\""
        ));
        tabs.add(new RoleTab(
                "Startups",
                "MVP Founders",
                "Assemble multi-disciplinary founding crews and early product builders without expensive agency retainers.",
                "\"Validated our prototype architecture with a designer and systems architect recruited directly through verified skill reviews.\""
        ));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "audience";
    }

    @Override
    public int getHeight(int width) {
        return width < 700 ? 680 : 560;
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int h = getHeight(width);
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int topY = y + 20;

        // Eyebrow & Title
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.BRAND_PINK);
        g2.drawString("WHO IT'S FOR", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Designed for every builder and team", padX, topY + 14 + tfm.getAscent() + 10);

        // Tab buttons row
        int tabY = topY + 85;
        int tabX = padX;
        g2.setFont(Theme.fontBodyBold());
        FontMetrics tabFm = g2.getFontMetrics();

        for (int i = 0; i < tabs.size(); i++) {
            RoleTab tab = tabs.get(i);
            tab.w = tabFm.stringWidth(tab.name) + 28;
            tab.h = 38;
            tab.x = tabX;
            tab.y = tabY;

            boolean isSel = (i == selectedTabIndex);
            if (isSel) {
                LinearGradientPaint p = new LinearGradientPaint(
                        tab.x, tab.y, tab.x + tab.w, tab.y,
                        new float[]{0f, 1f},
                        new Color[]{Theme.BRAND_PINK, new Color(0xFF, 0x8A, 0x5B)}
                );
                Paint old = g2.getPaint();
                g2.setPaint(p);
                g2.fill(new RoundRectangle2D.Float(tab.x, tab.y, tab.w, tab.h, tab.h, tab.h));
                g2.setPaint(old);
                g2.setColor(Color.WHITE);
            } else {
                Color fill = tab.hovered ? (Theme.isDark ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 15))
                                         : (Theme.isDark ? new Color(255, 255, 255, 10) : new Color(0, 0, 0, 6));
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(tab.x, tab.y, tab.w, tab.h, tab.h, tab.h));
                g2.setColor(tab.hovered ? Theme.ACCENT_CYAN : Theme.cardBorder());
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(tab.x, tab.y, tab.w, tab.h, tab.h, tab.h));
                g2.setColor(tab.hovered ? Theme.textPrimary() : Theme.textSecondary());
            }

            int tx = tab.x + (tab.w - tabFm.stringWidth(tab.name)) / 2;
            int ty = tab.y + (tab.h - tabFm.getHeight()) / 2 + tabFm.getAscent();
            g2.drawString(tab.name, tx, ty);

            tabX += tab.w + 12;
            if (tabX + 100 > width - padX) {
                // Wrap to next line on small screens
                tabX = padX;
                tabY += tab.h + 8;
            }
        }

        // Active Tab Display Card
        RoleTab active = tabs.get(selectedTabIndex);
        int cardY = tabY + 56;
        int cardW = width - padX * 2;
        int cardH = 180;
        Draw.drawGlassCard(g2, padX, cardY, cardW, cardH, 24, 0f, true);

        // Persona & Benefit
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.ACCENT_CYAN);
        g2.drawString(active.persona.toUpperCase(), padX + 28, cardY + 34);

        g2.setFont(Theme.fontCardTitle());
        FontMetrics btfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        List<String> benLines = Draw.wrapText(active.benefit, btfm, cardW - 56);
        int by = cardY + 62 + btfm.getAscent();
        for (String bl : benLines) {
            g2.drawString(bl, padX + 28, by);
            by += Math.round(btfm.getHeight() * 1.3f);
        }

        // Quote
        g2.setFont(Theme.fontBody());
        FontMetrics qfm = g2.getFontMetrics();
        g2.setColor(Theme.textSecondary());
        List<String> qLines = Draw.wrapText(active.useCase, qfm, cardW - 56);
        int qy = by + 12 + qfm.getAscent();
        for (String ql : qLines) {
            g2.drawString(ql, padX + 28, qy);
            qy += Math.round(qfm.getHeight() * 1.35f);
        }

        // Platform roles explanation sub-strip
        int subRolesY = cardY + cardH + 24;
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.textPrimary());
        g2.drawString("Platform Roles:", padX, subRolesY + 14);

        g2.setFont(Theme.fontSmall());
        g2.setColor(Theme.textSecondary());
        g2.drawString("Team Leader (create projects, recruit, manage milestones)  ·  Team Member (match skills, accept invites, earn ratings)",
                padX + 110, subRolesY + 14);

        g2.dispose();
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (int i = 0; i < tabs.size(); i++) {
            RoleTab tab = tabs.get(i);
            if (mx >= tab.x && mx <= tab.x + tab.w && my >= tab.y && my <= tab.y + tab.h) {
                selectedTabIndex = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (RoleTab tab : tabs) {
            boolean was = tab.hovered;
            tab.hovered = (mx >= tab.x && mx <= tab.x + tab.w && my >= tab.y && my <= tab.y + tab.h);
            if (tab.hovered != was) changed = true;
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

