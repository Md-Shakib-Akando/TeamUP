import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

//sdkoerwohrioh
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
        return 560;
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
        g2.drawString("COMPARISON MATRIX", padX, topY + 14);

        g2.setFont(Theme.fontTitle());
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(Theme.textPrimary());
        g2.drawString("Why TeamUp stands out", padX, topY + 14 + tfm.getAscent() + 10);

        // Table container
        int tableY = topY + 80;
        int tableW = width - padX * 2;
        int rowH = 48;
        int tableH = (rows.size() + 1) * rowH + 16;
        Draw.drawGlassCard(g2, padX, tableY, tableW, tableH, 20, 0f, false);

        // Column widths
        int featColW = Math.max(180, Math.round(tableW * 0.40f));
        int remainingW = tableW - featColW;
        int colW = remainingW / 4;

        int xTU = padX + featColW;
        int xCL = xTU + colW;
        int xCC = xCL + colW;
        int xDP = xCC + colW;

        // Highlight background column for TeamUp
        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 16));
        g2.fill(new RoundRectangle2D.Float(xTU + 4, tableY + 8, colW - 8, tableH - 16, 12, 12));
        g2.setColor(new Color(Theme.BRAND_PINK.getRed(), Theme.BRAND_PINK.getGreen(), Theme.BRAND_PINK.getBlue(), 60));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(xTU + 4, tableY + 8, colW - 8, tableH - 16, 12, 12));

        // Header Row
        int headerY = tableY + 12;
        g2.setFont(Theme.fontBodyBold());
        FontMetrics rfm = g2.getFontMetrics();

        g2.setColor(Theme.textMuted());
        g2.drawString("Capability", padX + 24, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        // TeamUp Header
        g2.setColor(Theme.BRAND_PINK);
        String tuStr = "TeamUp";
        g2.drawString(tuStr, xTU + (colW - rfm.stringWidth(tuStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        g2.setColor(Theme.textSecondary());
        String clStr = "CampLink";
        g2.drawString(clStr, xCL + (colW - rfm.stringWidth(clStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        String ccStr = "CollabConnect";
        g2.drawString(ccStr, xCC + (colW - rfm.stringWidth(ccStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        String dpStr = "Devpost";
        g2.drawString(dpStr, xDP + (colW - rfm.stringWidth(dpStr)) / 2, headerY + (rowH - rfm.getHeight()) / 2 + rfm.getAscent());

        // Divider
        g2.setColor(Theme.cardBorder());
        g2.drawLine(padX + 16, headerY + rowH, padX + tableW - 16, headerY + rowH);

        // Data Rows
        int curY = headerY + rowH;
        g2.setFont(Theme.fontBody());
        FontMetrics dfm = g2.getFontMetrics();

        for (int i = 0; i < rows.size(); i++) {
            RowData r = rows.get(i);
            int rowCenterY = curY + (rowH - dfm.getHeight()) / 2 + dfm.getAscent();

            g2.setColor(Theme.textPrimary());
            g2.drawString(r.feature, padX + 24, rowCenterY);

            // Icons
            int iconSize = 20;
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

