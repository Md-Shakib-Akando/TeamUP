import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Compare implements Section {

    private static class RowData {
        String feature;
        boolean teamUp;
        boolean campLink;
        boolean collabConnect;
        boolean devpost;

        RowData(String feature, boolean tu, boolean cl, boolean cc, boolean dp) {
            this.feature = feature;
            this.teamUp = tu;
            this.campLink = cl;
            this.collabConnect = cc;
            this.devpost = dp;
        }
    }

    private final List<RowData> rows = new ArrayList<>();
    private final Anim.RevealState reveal = new Anim.RevealState();

    public Compare() {
        rows.add(new RowData("Skill-Based Member Matching", true, false, true, false));
        rows.add(new RowData("Full Project Lifecycle Tracking", true, false, false, true));
        rows.add(new RowData("Built-in Kanban Task Management", true, false, false, false));
        rows.add(new RowData("Post-Project Peer Ratings & Reviews", true, false, false, false));
        rows.add(new RowData("Direct Messaging with Notifications", true, false, false, false));
        rows.add(new RowData("Admin Moderation & Security Panel", true, false, false, true));
        reveal.trigger();
    }

    @Override
    public String getId() {
        return "compare";
    }

    @Override
    public int getHeight(int width) {
        return 600;
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

        int topY = y + 24;

        // Eyebrow & Title
        Draw.drawSectionBadge(g2, "COMPARISON MATRIX", padX, topY, Theme.BRAND_PINK);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        int titleY = topY + 44 + tfm.getAscent();
        g2.drawString("Why TeamUp stands out", padX, titleY);

        g2.setFont(Theme.fontBody());
        g2.setColor(Theme.textSecondary());
        g2.drawString("Purpose-built for student and builder teams with full lifecycle tracking, chat, and verified peer reviews.", padX, titleY + 22);

        // Table container
        int tableY = titleY + 48;
        int tableW = width - padX * 2;
        int rowH = 50;
        int tableH = (rows.size() + 1) * rowH + 20;
        Draw.drawGlassCard(g2, padX, tableY, tableW, tableH, 20, 0f, false);

        // Column widths
        int featColW = Math.max(180, Math.round(tableW * 0.40f));
        int remainingW = tableW - featColW;
        int colW = remainingW / 4;

        int xTU = padX + featColW;
        int xCL = xTU + colW;
        int xCC = xCL + colW;
        int xDP = xCC + colW;

        // Highlight background column for TeamUp (with subtle gradient)
        LinearGradientPaint tugp = new LinearGradientPaint(
                xTU + 4, tableY + 8, xTU + colW - 4, tableY + tableH - 8,
                new float[]{0f, 1f},
                new Color[]{
                        new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 20),
                        new Color(Theme.ACCENT_VIOLET.getRed(), Theme.ACCENT_VIOLET.getGreen(), Theme.ACCENT_VIOLET.getBlue(), 12)
                }
        );
        Paint oldPaint = g2.getPaint();
        g2.setPaint(tugp);
        g2.fill(new RoundRectangle2D.Float(xTU + 4, tableY + 8, colW - 8, tableH - 16, 14, 14));
        g2.setPaint(oldPaint);

        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 70));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(xTU + 4, tableY + 8, colW - 8, tableH - 16, 14, 14));

        // Header Row
        int headerY = tableY + 14;
        g2.setFont(Theme.fontBodyBold());
        FontMetrics rfm = g2.getFontMetrics();

        g2.setColor(Theme.textMuted());
        g2.drawString("Platform Capabilities", padX + 24, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        // TeamUp Header with recommended badge
        int tuCenter = xTU + colW / 2;
        int badgeW = 92;
        int badgeH = 18;
        int badgeX = tuCenter - badgeW / 2;
        int badgeY = headerY + 4;
        RoundRectangle2D tuPill = new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, badgeH, badgeH);
        g2.setColor(Theme.BRAND_PINK);
        g2.fill(tuPill);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics bmf = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString("RECOMMENDED", badgeX + (badgeW - bmf.stringWidth("RECOMMENDED")) / 2, badgeY + 13);

        g2.setFont(Theme.fontBodyBold());
        g2.setColor(Theme.BRAND_PINK);
        String tuStr = "TeamUp";
        g2.drawString(tuStr, tuCenter - rfm.stringWidth(tuStr) / 2, headerY + 38);

        g2.setColor(Theme.textSecondary());
        String clStr = "CampLink";
        g2.drawString(clStr, xCL + (colW - rfm.stringWidth(clStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        String ccStr = "CollabConnect";
        g2.drawString(ccStr, xCC + (colW - rfm.stringWidth(ccStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        String dpStr = "Devpost";
        g2.drawString(dpStr, xDP + (colW - rfm.stringWidth(dpStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        // Divider
        g2.setColor(Theme.cardBorder());
        g2.drawLine(padX + 16, headerY + rowH + 6, padX + tableW - 16, headerY + rowH + 6);

        // Data Rows
        int curY = headerY + rowH + 6;
        g2.setFont(Theme.fontBody());
        FontMetrics dfm = g2.getFontMetrics();

        for (int i = 0; i < rows.size(); i++) {
            RowData r = rows.get(i);
            int rowCenterY = curY + (rowH - dfm.getHeight()) / 2 + dfm.getAscent();

            g2.setColor(Theme.textPrimary());
            g2.drawString(r.feature, padX + 24, rowCenterY);

            // Icons
            int iconSize = 22;
            int iconY = curY + (rowH - iconSize) / 2;

            // TeamUp
            drawTickOrDash(g2, xTU + (colW - iconSize) / 2, iconY, iconSize, r.teamUp, true);
            // CampLink
            drawTickOrDash(g2, xCL + (colW - iconSize) / 2, iconY, iconSize, r.campLink, false);
            // CollabConnect
            drawTickOrDash(g2, xCC + (colW - iconSize) / 2, iconY, iconSize, r.collabConnect, false);
            // Devpost
            drawTickOrDash(g2, xDP + (colW - iconSize) / 2, iconY, iconSize, r.devpost, false);

            curY += rowH;
            if (i < rows.size() - 1) {
                g2.setColor(Theme.cardBorder());
                g2.drawLine(padX + 16, curY, padX + tableW - 16, curY);
            }
        }

        g2.dispose();
    }

    private void drawTickOrDash(Graphics2D g, int x, int y, int size, boolean tick, boolean isTeamUp) {
        if (tick) {
            Color color = isTeamUp ? Theme.BRAND_PINK : Theme.textSecondary();
            Draw.drawCheckIcon(g, x, y, size, color);
        } else {
            Draw.drawDashIcon(g, x, y, size, Theme.textMuted());
        }
    }

    @Override
    public boolean onClick(int mx, int my) { return false; }
    @Override
    public boolean onMouseMove(int mx, int my) { return false; }
    @Override
    public boolean onMouseDown(int mx, int my) { return false; }
    @Override
    public boolean onMouseUp(int mx, int my) { return false; }
    @Override
    public boolean onKey(KeyEvent e) { return false; }
}
