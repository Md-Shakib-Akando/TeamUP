import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Faq implements Section {

    private final List<Accordion> items = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Faq() {
        items.add(new Accordion(
                "Is TeamUp completely free to use?",
                "Yes, TeamUp is 100% free for students, developers, and researchers. There are no paywalls, hidden fees, or limits on team formation during the early access and academic rollout."
        ));
        items.add(new Accordion(
                "Who can join and collaborate?",
                "Anyone looking to build! Whether you are a student assembling a coursework team or hackathon squad, a freelancer grouping into client pods, a researcher needing technical firepower, or an early startup founder."
        ));
        items.add(new Accordion(
                "How does the skill matching engine work?",
                "Our algorithm computes multi-dimensional compatibility vectors considering complementary technical skills, availability quotas, desired project roles, and verified feedback scores."
        ));
        items.add(new Accordion(
                "How are freeloaders prevented?",
                "Accountability is baked into the platform lifecycle. Upon project completion or milestone wrap-up, all collaborators submit constructive peer ratings covering reliability, communication, and code quality."
        ));
        items.add(new Accordion(
                "How is my data and privacy secured?",
                "Passwords undergo salted PBKDF2 cryptographic hashing, direct messages and tokens are session-scoped, and private contact info remains masked until invitations are formally accepted."
        ));
        items.add(new Accordion(
                "What platforms and OS are supported?",
                "TeamUp is built desktop-first for Windows, macOS, and Linux, powered directly by high-performance core Java without heavy web browser runtimes."
        ));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "faq";
    }

    @Override
    public int getHeight(int width) {
        int baseH = 170;
        Font titleF = Theme.fontCardTitle();
        Font bodyF = Theme.fontBody();

        FontMetrics tfm = new Canvas().getFontMetrics(titleF);
        FontMetrics bfm = new Canvas().getFontMetrics(bodyF);

        int totalH = baseH;
        for (Accordion item : items) {
            totalH += item.getHeight(width - 112, tfm, bfm) + 16;
        }
        return totalH + 40;
    }

    @Override
    public void update(float dt) {
        reveal.update(dt);
        for (Accordion item : items) {
            item.update(dt);
        }
    }

    @Override
    public void paint(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int padX = width > 1100 ? 56 : (width > 600 ? 28 : 20);

        Graphics2D g2 = (Graphics2D) g.create();
        int rise = reveal.getYOffset();
        g2.translate(0, rise);

        int topY = y + 24;

        // Eyebrow & Title
        Draw.drawSectionBadge(g2, "SUPPORT & FAQ", padX, topY, Theme.BRAND_PINK);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int titleY = topY + 44 + tfm.getAscent();
        g2.drawString("Got questions? We've got answers", padX, titleY);

        g2.setFont(Theme.fontBody());
        g2.setColor(Theme.textSecondary());
        g2.drawString("Find instant answers to common questions about platform access, peer accountability, and matching mechanics.", padX, titleY + 22);

        // Accordion list
        int curY = titleY + 48;
        int listW = width - padX * 2;
        FontMetrics titleFm = g2.getFontMetrics(Theme.fontCardTitle());
        FontMetrics bodyFm = g2.getFontMetrics(Theme.fontBody());

        for (Accordion item : items) {
            item.paint(g2, padX, curY, listW);
            curY += item.getHeight(listW, titleFm, bodyFm) + 16;
        }

        g2.dispose();
    }

    @Override
    public boolean onClick(int mx, int my) {
        for (Accordion item : items) {
            if (item.onClick(mx, my)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;
        for (Accordion item : items) {
            if (item.onMouseMove(mx, my)) {
                changed = true;
            }
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

