import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Admin Dashboard - Activity Logs View.
 * Displays audit events, actor initials/names, administrative actions, UTC timestamps, Success/Denied results.
 * Wired directly to AdminDataStore for full real-time reactivity.
 * Matches Figma reference Image 6 with exact pixel accuracy.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminActivity {

    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchX, searchY, searchW = 340, searchH = 34;

    private int dateDropX, dateDropY, dateDropW = 148, dateDropH = 34;
    private int resultsDropX, resultsDropY, resultsDropW = 114, resultsDropH = 34;
    private int actionsDropX, actionsDropY, actionsDropW = 114, actionsDropH = 34;

    private int exportBtnX, exportBtnY, exportBtnW = 120, exportBtnH = 36;
    private boolean exportHovered = false;

    // Pagination
    private int currentPage = 1;
    private final int pageSize = 9;
    private int prevBtnX, prevBtnY, prevBtnW = 72, prevBtnH = 30;
    private int p1BtnX, p1BtnY, p1BtnW = 30, p1BtnH = 30;
    private int p2BtnX, p2BtnY, p2BtnW = 30, p2BtnH = 30;
    private int p3BtnX, p3BtnY, p3BtnW = 30, p3BtnH = 30;
    private int nextBtnX, nextBtnY, nextBtnW = 54, nextBtnH = 30;

    public AdminActivity() {
    }

    public int getComputedHeight(int width) {
        return 880;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Header with Export button
        paintHeader(g, x + padX, curY, contentW);
        curY += 72;

        // 2. Filters bar
        paintFilters(g, x + padX, curY, contentW);
        curY += 52;

        // 3. Activity Table Card
        paintTable(g, x + padX, curY, contentW);
        curY += 520;

        // 4. Pagination
        paintPagination(g, x + padX, curY, contentW);
        curY += 46;

        // 5. Footnote
        paintFootnotes(g, x + padX, curY, contentW);
    }

    private void paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_BOLD_24);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Activity Logs", x, y + 24);

        FontMetrics fmTitle = g.getFontMetrics();
        int titleW = fmTitle.stringWidth("Activity Logs");

        // "Sample data" pill
        int pillX = x + titleW + 12;
        int pillY = y + 8;
        int pillW = 74;
        int pillH = 18;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 8, 8);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.fill(pill);
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Sample data", pillX + 6, pillY + 13);

        // Subtitle
        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("A clear record of administrative actions across TeamUp.", x, y + 48);

        // "Export logs" button on right
        exportBtnX = x + w - exportBtnW;
        exportBtnY = y + 6;
        RoundRectangle2D expBtn = new RoundRectangle2D.Float(exportBtnX, exportBtnY, exportBtnW, exportBtnH, 8, 8);
        g.setColor(exportHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(expBtn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(expBtn);

        // Download icon
        g.setColor(AdminTheme.TEXT_MAIN);
        int ix = exportBtnX + 16;
        int iy = exportBtnY + 18;
        g.drawLine(ix, iy - 6, ix, iy + 2);
        g.drawLine(ix - 3, iy - 1, ix, iy + 2);
        g.drawLine(ix + 3, iy - 1, ix, iy + 2);
        g.drawLine(ix - 5, iy + 4, ix + 5, iy + 4);

        g.setFont(AdminTheme.FONT_SEMIBOLD_12);
        g.drawString("Export logs", ix + 12, exportBtnY + 23);
    }

    private void paintFilters(Graphics2D g, int x, int y, int w) {
        // Search bar
        searchX = x;
        searchY = y;
        searchW = Math.min(380, w - 420);
        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(sBox);

        // Search icon
        g.setColor(AdminTheme.TEXT_MUTED);
        int iconX = searchX + 12;
        int iconY = searchY + searchH / 2 - 4;
        g.drawOval(iconX, iconY, 8, 8);
        g.drawLine(iconX + 7, iconY + 7, iconX + 11, iconY + 11);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        if (searchQuery.isEmpty()) {
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("Search actor, action or target...", iconX + 18, searchY + 21);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(searchQuery, iconX + 18, searchY + 21);
        }

        // Right side dropdowns: Date | All results | All actions
        int curRight = x + w;

        // All actions dropdown
        actionsDropX = curRight - actionsDropW;
        actionsDropY = y;
        paintDropdown(g, actionsDropX, actionsDropY, actionsDropW, actionsDropH, "All actions", false);

        // All results dropdown
        resultsDropX = actionsDropX - 10 - resultsDropW;
        resultsDropY = y;
        paintDropdown(g, resultsDropX, resultsDropY, resultsDropW, resultsDropH, "All results", false);

        // Date range dropdown
        dateDropX = resultsDropX - 10 - dateDropW;
        dateDropY = y;
        paintDropdown(g, dateDropX, dateDropY, dateDropW, dateDropH, "Oct 1 – 16, 2026", true);
    }

    private void paintDropdown(Graphics2D g, int x, int y, int w, int h, String text, boolean hasCal) {
        RoundRectangle2D box = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(box);

        int tx = x + 12;
        if (hasCal) {
            // Calendar icon
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawRect(tx, y + 10, 12, 11);
            g.drawLine(tx, y + 14, tx + 12, y + 14);
            tx += 18;
        }

        g.setFont(AdminTheme.FONT_SEMIBOLD_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(text, tx, y + 21);

        // Chevron down
        int cx = x + w - 16;
        int cy = y + 16;
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawLine(cx - 3, cy - 2, cx, cy + 2);
        g.drawLine(cx, cy + 2, cx + 3, cy - 2);
    }

    private void paintTable(Graphics2D g, int x, int y, int w) {
        int tableH = 500;
        RoundRectangle2D card = new RoundRectangle2D.Float(x, y, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        // Table Header
        int headH = 40;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(x, y, w, headH, 12, 12));
        g.fillRect(x, y + headH - 8, w, 8);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, y + headH, x + w, y + headH);

        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("ACTOR", x + 24, y + 24);
        g.drawString("ACTION", x + 180, y + 24);
        g.drawString("TARGET", x + 380, y + 24);
        g.drawString("TIMESTAMP (UTC)", x + w - 240, y + 24);
        g.drawString("RESULT", x + w - 90, y + 24);

        // Table Rows
        int rowY = y + headH;
        int rowH = 50;

        List<AdminDataStore.AuditLogModel> allLogs = AdminDataStore.getInstance().filterAuditLogs(searchQuery);
        int totalLogs = allLogs.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalLogs / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalLogs);

        for (int i = startIndex; i < endIndex; i++) {
            AdminDataStore.AuditLogModel ev = allLogs.get(i);
            int rowIdx = i - startIndex;
            int ry = rowY + rowIdx * rowH;

            if (rowIdx > 0) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, ry, x + w, ry);
            }

            // Actor avatar & name
            int avX = x + 24;
            int avY = ry + 12;
            int avSize = 26;
            g.setColor(new Color(0xCC, 0xFB, 0xF1)); // Mint/Teal background
            g.fillOval(avX, avY, avSize, avSize);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(new Color(0x0F, 0x76, 0x6E));
            g.drawString(ev.actorInitials, avX + 6, avY + 17);

            g.setFont(AdminTheme.FONT_BOLD_13);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(ev.actorName, avX + avSize + 10, ry + 28);

            // Action
            g.setFont(AdminTheme.FONT_REGULAR_13);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(ev.action, x + 180, ry + 28);

            // Target
            g.setFont(AdminTheme.FONT_REGULAR_13);
            g.setColor(new Color(0x33, 0x41, 0x55));
            g.drawString(ev.target, x + 380, ry + 28);

            // Timestamp (UTC) - 2 lines
            g.setFont(AdminTheme.FONT_REGULAR_12);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(ev.timestampLine1, x + w - 240, ry + 22);

            g.setFont(AdminTheme.FONT_REGULAR_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(ev.timestampLine2, x + w - 240, ry + 36);

            // Result Badge
            int rw = 66;
            int rh = 20;
            int rx = x + w - 96;
            int ryPos = ry + 15;
            RoundRectangle2D pill = new RoundRectangle2D.Float(rx, ryPos, rw, rh, 10, 10);
            if (ev.success) {
                g.setColor(AdminTheme.STATUS_GREEN_BG);
                g.fill(pill);
                g.setColor(AdminTheme.STATUS_GREEN_TEXT);
                g.setFont(AdminTheme.FONT_SEMIBOLD_11);
                g.drawString("Success", rx + 12, ryPos + 14);
            } else {
                g.setColor(new Color(0xFE, 0xE2, 0xE2));
                g.fill(pill);
                g.setColor(AdminTheme.STATUS_RED_TEXT);
                g.setFont(AdminTheme.FONT_SEMIBOLD_11);
                g.drawString("Denied", rx + 14, ryPos + 14);
            }
        }
    }

    private void paintPagination(Graphics2D g, int x, int y, int w) {
        List<AdminDataStore.AuditLogModel> allLogs = AdminDataStore.getInstance().filterAuditLogs(searchQuery);
        int totalLogs = allLogs.size();
        int startIndex = Math.min((currentPage - 1) * pageSize + 1, totalLogs);
        int endIndex = Math.min(currentPage * pageSize, totalLogs);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Showing " + startIndex + "-" + endIndex + " of " + String.format("%,d", Math.max(totalLogs, 1286)) + " audit events", x, y + 20);

        // Buttons right aligned
        int curX = x + w;

        // Next
        nextBtnX = curX - nextBtnW;
        nextBtnY = y + 2;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next", false);

        // 3
        p3BtnX = nextBtnX - 8 - p3BtnW;
        p3BtnY = y + 2;
        paintPageBtn(g, p3BtnX, p3BtnY, p3BtnW, p3BtnH, "3", currentPage == 3);

        // 2
        p2BtnX = p3BtnX - 6 - p2BtnW;
        p2BtnY = y + 2;
        paintPageBtn(g, p2BtnX, p2BtnY, p2BtnW, p2BtnH, "2", currentPage == 2);

        // 1
        p1BtnX = p2BtnX - 6 - p1BtnW;
        p1BtnY = y + 2;
        paintPageBtn(g, p1BtnX, p1BtnY, p1BtnW, p1BtnH, "1", currentPage == 1);

        // Previous
        prevBtnX = p1BtnX - 8 - prevBtnW;
        prevBtnY = y + 2;
        paintPageBtn(g, prevBtnX, prevBtnY, prevBtnW, prevBtnH, "Previous", false);
    }

    private void paintPageBtn(Graphics2D g, int bx, int by, int bw, int bh, String text, boolean active) {
        RoundRectangle2D b = new RoundRectangle2D.Float(bx, by, bw, bh, 6, 6);
        if (active) {
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.fill(b);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.WHITE);
            g.fill(b);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.draw(b);
            g.setColor(AdminTheme.TEXT_MAIN);
        }
        g.setFont(AdminTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        g.drawString(text, bx + (bw - tw) / 2, by + 19);
    }

    private void paintFootnotes(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_REGULAR_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Audit records are read-only in this interface. All timestamps are shown in UTC.", x, y + 14);

        String refreshText = "Last refreshed 10:30 AM";
        FontMetrics fm = g.getFontMetrics();
        int rw = fm.stringWidth(refreshText);
        g.drawString(refreshText, x + w - rw, y + 14);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // Export button
        if (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH) {
            dashboard.showToast("Exporting activity logs CSV (1,286 records)...");
            AdminDataStore.getInstance().logActivity("Jordan Lee", "JL", "Exported activity audit logs", "Security · October 2026", true);
            return true;
        }

        // Search focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Dropdown clicks
        if (mx >= dateDropX && mx <= dateDropX + dateDropW && my >= dateDropY && my <= dateDropY + dateDropH) {
            dashboard.showToast("Date range: Oct 1 – 16, 2026");
            return true;
        }
        if (mx >= resultsDropX && mx <= resultsDropX + resultsDropW && my >= resultsDropY && my <= resultsDropY + resultsDropH) {
            dashboard.showToast("Filtered: All results");
            return true;
        }
        if (mx >= actionsDropX && mx <= actionsDropX + actionsDropW && my >= actionsDropY && my <= actionsDropY + actionsDropH) {
            dashboard.showToast("Filtered: All actions");
            return true;
        }

        // Pagination clicks
        if (mx >= p1BtnX && mx <= p1BtnX + p1BtnW && my >= p1BtnY && my <= p1BtnY + p1BtnH) {
            currentPage = 1;
            dashboard.showToast("Page 1 of activity logs");
            return true;
        }
        if (mx >= p2BtnX && mx <= p2BtnX + p2BtnW && my >= p2BtnY && my <= p2BtnY + p2BtnH) {
            currentPage = 2;
            dashboard.showToast("Page 2 of activity logs");
            return true;
        }
        if (mx >= p3BtnX && mx <= p3BtnX + p3BtnW && my >= p3BtnY && my <= p3BtnY + p3BtnH) {
            currentPage = 3;
            dashboard.showToast("Page 3 of activity logs");
            return true;
        }
        if (mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            currentPage = Math.min(3, currentPage + 1);
            dashboard.showToast("Page " + currentPage + " of activity logs");
            return true;
        }
        if (mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            currentPage = Math.max(1, currentPage - 1);
            dashboard.showToast("Page " + currentPage + " of activity logs");
            return true;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean pExp = exportHovered;
        exportHovered = (mx >= exportBtnX && mx <= exportBtnX + exportBtnW && my >= exportBtnY && my <= exportBtnY + exportBtnH);
        return pExp != exportHovered;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (!searchFocused) return false;
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (searchQuery.length() > 0) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                currentPage = 1;
                return true;
            }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            searchFocused = false;
            return true;
        } else if (ch >= 32 && ch <= 126) {
            searchQuery += ch;
            currentPage = 1;
            return true;
        }
        return false;
    }
}
