import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Faq implements Section {

    private final List<Accordion> items = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Faq() {
        items.add(new Accordion(
                "Is it free?",
                "Yes, TeamUp is completely free for students, developers, and researchers during the early access and academic release phase. There are no paywalls or hidden limits on team formation."
        ));
        items.add(new Accordion(
                "Who can join?",
                "Anyone looking to build or collaborate! Whether you are a student working on coursework or hackathons, a freelancer assembling a client pod, a researcher seeking computational expertise, or a startup founder."
        ));
        items.add(new Accordion(
                "How does matching work?",
                "TeamUp computes compatibility scores using multidimensional skill overlap, availability quotas, desired roles, and historical project feedback. The top candidates rank instantly in your search feed."
        ));
        items.add(new Accordion(
                "Can I rate teammates?",
                "Absolutely. Accountability is central to TeamUp. When a project or sprint ends, collaborators exchange constructive ratings across communication, reliability, and technical contributions."
        ));
        items.add(new Accordion(
                "Is my data secure?",
                "All user passwords undergo salted cryptographic hashing, session tokens are strictly scoped, and all administrative actions are audited. Your private contact details remain protected until you accept an invite."
        ));
        items.add(new Accordion(
                "Which platform is supported?",
                "TeamUp is designed desktop-first for Windows, macOS, and Linux, powered directly by Java without heavyweight web dependencies. Web and mobile companion apps are planned for future releases."
        ));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "faq";
    }

    @Override
    public int getHeight(int width) {
        // Base title height + items height
        int baseH = 140;
        Font titleF = Theme.fontCardTitle();
        Font bodyF = Theme.fontBody();

        // Dummy graphics for metrics
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

        int topY = y + 20;

        // Eyebrow & Title
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(Theme.BRAND_PINK);
        g2.drawString("FREQUENTLY ASKED QUESTIONS", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Got questions? We've got answers", padX, topY + 14 + tfm.getAscent() + 10);

        // Accordion list
        int curY = topY + 90;
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

