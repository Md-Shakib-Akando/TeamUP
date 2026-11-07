import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Dashboard - Categories & Skills Management View.
 * Matches Figma reference Image 5 with exact dual-column layout, badges, search, and action toggles.
 * Fully wired to AdminDataStore for real-time reactivity.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminCategories {

    public static class CatHitbox {
        public AdminDataStore.CategoryModel model;
        public int editX, editY, editW, editH;
        public int toggleX, toggleY, toggleW, toggleH;
        public boolean editHovered = false;
        public boolean toggleHovered = false;
    }

    public static class SkillHitbox {
        public AdminDataStore.SkillModel model;
        public int editX, editY, editW, editH;
        public int toggleX, toggleY, toggleW, toggleH;
        public boolean editHovered = false;
        public boolean toggleHovered = false;
    }

    private final List<CatHitbox> catHitboxes = new ArrayList<>();
    private final List<SkillHitbox> skillHitboxes = new ArrayList<>();

    private String catSearch = "";
    private boolean catSearchFocused = false;
    private int catSearchX, catSearchY, catSearchW, catSearchH = 34;

    private String skillSearch = "";
    private boolean skillSearchFocused = false;
    private int skillSearchX, skillSearchY, skillSearchW, skillSearchH = 34;

    private int addCatBtnX, addCatBtnY, addCatBtnW = 126, addCatBtnH = 36;
    private boolean addCatHovered = false;

    private int addSkillBtnX, addSkillBtnY, addSkillBtnW = 104, addSkillBtnH = 36;
    private boolean addSkillHovered = false;

    private int viewAllCatX, viewAllCatY, viewAllCatW, viewAllCatH = 20;
    private boolean viewAllCatHovered = false;

    private int viewAllSkillX, viewAllSkillY, viewAllSkillW, viewAllSkillH = 20;
    private boolean viewAllSkillHovered = false;

    private boolean showAllCategories = false;
    private boolean showAllSkills = false;

    public AdminCategories() {
    }

    public int getComputedHeight(int width) {
        return 740;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;

        // 1. Title Header
        paintHeader(g, x + padX, curY, contentW);
        curY += 72;

        // 2. Dual Columns (50% / 50% split with 24px gap)
        int gap = 24;
        int colW = (contentW - gap) / 2;

        paintCategoryColumn(g, x + padX, curY, colW, dashboard);
        paintSkillColumn(g, x + padX + colW + gap, curY, colW, dashboard);

        curY += 460;

        // 3. Information Banner at bottom
        paintBottomBanner(g, x + padX, curY, contentW);
    }

    private void paintHeader(Graphics2D g, int x, int y, int w) {
        g.setFont(AdminTheme.FONT_BOLD_24);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Categories & Skills", x, y + 24);

        FontMetrics fmTitle = g.getFontMetrics();
        int titleW = fmTitle.stringWidth("Categories & Skills");

        // "Sample data" pill
        int pillX = x + titleW + 12;
        int pillY = y + 8;
        int pillW = 74;
        int pillH = 18;
        RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 8, 8);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(pill);
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString("Sample data", pillX + 6, pillY + 13);

        // Subtitle
        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Keep project discovery and skill matching relevant, clear and consistent.", x, y + 48);
    }

    private void paintCategoryColumn(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.CategoryModel> allCats = AdminDataStore.getInstance().getAllCategories();
        long activeCount = allCats.stream().filter(c -> c.active).count();

        // Header line: "Project categories", sub: "X categories · Y active", Button: "+ Add category"
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Project categories", x, y + 16);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(allCats.size() + " categories · " + activeCount + " active", x, y + 34);

        // "+ Add category" button
        addCatBtnX = x + w - addCatBtnW;
        addCatBtnY = y;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addCatBtnX, addCatBtnY, addCatBtnW, addCatBtnH, 8, 8);
        g.setColor(addCatHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBtn);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("+ Add category", addCatBtnX + 16, addCatBtnY + 22);

        int curY = y + 48;

        // Search Input
        catSearchX = x;
        catSearchY = curY;
        catSearchW = w;
        paintSearchBar(g, catSearchX, catSearchY, catSearchW, catSearchH, catSearch, "Search categories...", catSearchFocused);

        curY += 46;

        // Filter categories by search
        List<AdminDataStore.CategoryModel> filteredCats = allCats.stream()
                .filter(c -> catSearch.isEmpty() || c.name.toLowerCase().contains(catSearch.toLowerCase()))
                .collect(Collectors.toList());

        // Card Container
        int tableH = 310;
        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        // Table Header
        int headH = 38;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(new RoundRectangle2D.Float(x, curY, w, headH, 12, 12));
        g.fillRect(x, curY + headH - 8, w, 8);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, curY + headH, x + w, curY + headH);

        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("CATEGORY / USAGE", x + 16, curY + 23);
        g.drawString("STATUS", x + w - 150, curY + 23);
        g.drawString("ACTIONS", x + w - 75, curY + 23);

        // Rows
        int rowY = curY + headH;
        int rowH = 43;
        catHitboxes.clear();

        int displayLimit = showAllCategories ? filteredCats.size() : Math.min(6, filteredCats.size());

        for (int i = 0; i < displayLimit; i++) {
            AdminDataStore.CategoryModel item = filteredCats.get(i);
            int ry = rowY + i * rowH;

            if (i > 0) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, ry, x + w, ry);
            }

            // Category Name & count
            g.setFont(AdminTheme.FONT_BOLD_13);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(item.name, x + 16, ry + 18);

            g.setFont(AdminTheme.FONT_REGULAR_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(item.projectCount + " projects", x + 16, ry + 32);

            // Status Pill
            int badgeX = x + w - 152;
            int badgeY = ry + 12;
            paintStatusBadge(g, badgeX, badgeY, item.active ? "Active" : "Disabled", item.active);

            // Action Links: Edit | Disable/Enable
            int actionX = x + w - 85;
            int actionY = ry + 24;

            CatHitbox hb = new CatHitbox();
            hb.model = item;
            hb.editX = actionX;
            hb.editY = ry + 10;
            hb.editW = 28;
            hb.editH = 20;

            hb.toggleX = actionX + 34;
            hb.toggleY = ry + 10;
            hb.toggleW = 46;
            hb.toggleH = 20;

            catHitboxes.add(hb);

            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString("Edit", hb.editX, actionY);

            if (item.active) {
                g.setColor(AdminTheme.STATUS_RED_TEXT);
                g.drawString("Disable", hb.toggleX, actionY);
            } else {
                g.setColor(AdminTheme.STATUS_GREEN_TEXT);
                g.drawString("Enable", hb.toggleX, actionY);
            }
        }

        // Footer "Showing X of Y categories · View all categories →"
        int footY = curY + tableH + 18;
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String footPrefix = "Showing " + displayLimit + " of " + allCats.size() + " categories · ";
        g.drawString(footPrefix, x, footY);

        FontMetrics fmFoot = g.getFontMetrics();
        int preW = fmFoot.stringWidth(footPrefix);

        viewAllCatX = x + preW;
        viewAllCatY = footY - 14;
        String viewAllText = showAllCategories ? "Show less ↑" : "View all categories →";
        viewAllCatW = fmFoot.stringWidth(viewAllText);
        viewAllCatH = 18;

        g.setColor(viewAllCatHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.drawString(viewAllText, viewAllCatX, footY);
    }

    private void paintSkillColumn(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.SkillModel> allSkills = AdminDataStore.getInstance().getAllSkills();
        long activeCount = allSkills.stream().filter(s -> s.active).count();

        // Header line: "Skill tags", sub: "X skills · Y active", Button: "+ Add skill"
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Skill tags", x, y + 16);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(allSkills.size() + " skills · " + activeCount + " active", x, y + 34);

        // "+ Add skill" button
        addSkillBtnX = x + w - addSkillBtnW;
        addSkillBtnY = y;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addSkillBtnX, addSkillBtnY, addSkillBtnW, addSkillBtnH, 8, 8);
        g.setColor(addSkillHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBtn);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("+ Add skill", addSkillBtnX + 16, addSkillBtnY + 22);

        int curY = y + 48;

        // Search Input
        skillSearchX = x;
        skillSearchY = curY;
        skillSearchW = w;
        paintSearchBar(g, skillSearchX, skillSearchY, skillSearchW, skillSearchH, skillSearch, "Search skill tags...", skillSearchFocused);

        curY += 46;

        // Filter skills by search
        List<AdminDataStore.SkillModel> filteredSkills = allSkills.stream()
                .filter(s -> skillSearch.isEmpty() || s.name.toLowerCase().contains(skillSearch.toLowerCase()))
                .collect(Collectors.toList());

        // Card Container
        int tableH = 310;
        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        // Table Header
        int headH = 38;
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(new RoundRectangle2D.Float(x, curY, w, headH, 12, 12));
        g.fillRect(x, curY + headH - 8, w, 8);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, curY + headH, x + w, curY + headH);

        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("SKILL / USAGE", x + 16, curY + 23);
        g.drawString("STATUS", x + w - 150, curY + 23);
        g.drawString("ACTIONS", x + w - 75, curY + 23);

        // Rows
        int rowY = curY + headH;
        int rowH = 43;
        skillHitboxes.clear();

        int displayLimit = showAllSkills ? filteredSkills.size() : Math.min(6, filteredSkills.size());

        for (int i = 0; i < displayLimit; i++) {
            AdminDataStore.SkillModel item = filteredSkills.get(i);
            int ry = rowY + i * rowH;

            if (i > 0) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x, ry, x + w, ry);
            }

            // Skill Name & members
            g.setFont(AdminTheme.FONT_BOLD_13);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(item.name, x + 16, ry + 18);

            g.setFont(AdminTheme.FONT_REGULAR_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(String.format("%,d members", item.memberCount), x + 16, ry + 32);

            // Status Pill
            int badgeX = x + w - 152;
            int badgeY = ry + 12;
            paintStatusBadge(g, badgeX, badgeY, item.active ? "Active" : "Disabled", item.active);

            // Action Links: Edit | Disable/Enable
            int actionX = x + w - 85;
            int actionY = ry + 24;

            SkillHitbox hb = new SkillHitbox();
            hb.model = item;
            hb.editX = actionX;
            hb.editY = ry + 10;
            hb.editW = 28;
            hb.editH = 20;

            hb.toggleX = actionX + 34;
            hb.toggleY = ry + 10;
            hb.toggleW = 46;
            hb.toggleH = 20;

            skillHitboxes.add(hb);

            g.setFont(AdminTheme.FONT_BOLD_12);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString("Edit", hb.editX, actionY);

            if (item.active) {
                g.setColor(AdminTheme.STATUS_RED_TEXT);
                g.drawString("Disable", hb.toggleX, actionY);
            } else {
                g.setColor(AdminTheme.STATUS_GREEN_TEXT);
                g.drawString("Enable", hb.toggleX, actionY);
            }
        }

        // Footer "Showing X of Y skills · View all skills →"
        int footY = curY + tableH + 18;
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String footPrefix = "Showing " + displayLimit + " of " + allSkills.size() + " skills · ";
        g.drawString(footPrefix, x, footY);

        FontMetrics fmFoot = g.getFontMetrics();
        int preW = fmFoot.stringWidth(footPrefix);

        viewAllSkillX = x + preW;
        viewAllSkillY = footY - 14;
        String viewAllText = showAllSkills ? "Show less ↑" : "View all skills →";
        viewAllSkillW = fmFoot.stringWidth(viewAllText);
        viewAllSkillH = 18;

        g.setColor(viewAllSkillHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.drawString(viewAllText, viewAllSkillX, footY);
    }

    private void paintSearchBar(Graphics2D g, int sx, int sy, int sw, int sh, String text, String placeholder, boolean focused) {
        RoundRectangle2D sbox = new RoundRectangle2D.Float(sx, sy, sw, sh, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sbox);
        g.setColor(focused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(sbox);

        // Search lens icon
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawOval(sx + 10, sy + 10, 10, 10);
        g.drawLine(sx + 18, sy + 18, sx + 22, sy + 22);

        // Text
        g.setFont(AdminTheme.FONT_REGULAR_12);
        if (text.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(placeholder, sx + 28, sy + 21);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(text, sx + 28, sy + 21);
        }
    }

    private void paintStatusBadge(Graphics2D g, int bx, int by, String text, boolean active) {
        int bw = active ? 52 : 62;
        int bh = 20;
        RoundRectangle2D pill = new RoundRectangle2D.Float(bx, by, bw, bh, 10, 10);
        g.setColor(active ? AdminTheme.STATUS_GREEN_BG : AdminTheme.STATUS_GRAY_BG);
        g.fill(pill);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(active ? AdminTheme.STATUS_GREEN_TEXT : AdminTheme.STATUS_GRAY_TEXT);
        g.drawString(text, bx + (active ? 10 : 8), by + 14);
    }

    private void paintBottomBanner(Graphics2D g, int x, int y, int w) {
        int banH = 56;
        RoundRectangle2D banner = new RoundRectangle2D.Float(x, y, w, banH, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(banner);
        g.setColor(new Color(0xFD, 0xD7, 0xE4));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(banner);

        int cx = x + 18;
        int cy = y + 18;

        // Checkmark badge
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(cx, cy, 18, 18);
        g.drawLine(cx + 4, cy + 9, cx + 7, cy + 13);
        g.drawLine(cx + 7, cy + 13, cx + 13, cy + 5);

        // Bold Title
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_DARK);
        g.drawString("Disabling does not delete existing data", cx + 28, y + 24);

        // Body Description
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Disabled categories and skills remain on existing profiles and projects, but can't be selected for new ones.", cx + 28, y + 42);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        // Add Category button
        if (mx >= addCatBtnX && mx <= addCatBtnX + addCatBtnW && my >= addCatBtnY && my <= addCatBtnY + addCatBtnH) {
            dashboard.openAddCategoryModal();
            return true;
        }

        // Add Skill button
        if (mx >= addSkillBtnX && mx <= addSkillBtnX + addSkillBtnW && my >= addSkillBtnY && my <= addSkillBtnY + addSkillBtnH) {
            dashboard.openAddSkillModal();
            return true;
        }

        // Search focuses
        if (mx >= catSearchX && mx <= catSearchX + catSearchW && my >= catSearchY && my <= catSearchY + catSearchH) {
            catSearchFocused = true;
            skillSearchFocused = false;
            return true;
        }
        if (mx >= skillSearchX && mx <= skillSearchX + skillSearchW && my >= skillSearchY && my <= skillSearchY + skillSearchH) {
            skillSearchFocused = true;
            catSearchFocused = false;
            return true;
        }
        catSearchFocused = false;
        skillSearchFocused = false;

        // Category Actions
        for (CatHitbox c : catHitboxes) {
            if (mx >= c.editX && mx <= c.editX + c.editW && my >= c.editY && my <= c.editY + c.editH) {
                dashboard.showToast("Editing category: " + c.model.name);
                return true;
            }
            if (mx >= c.toggleX && mx <= c.toggleX + c.toggleW && my >= c.toggleY && my <= c.toggleY + c.toggleH) {
                AdminDataStore.getInstance().toggleCategory(c.model.name);
                dashboard.showToast((!c.model.active ? "Enabled" : "Disabled") + " category: " + c.model.name);
                return true;
            }
        }

        // Skill Actions
        for (SkillHitbox s : skillHitboxes) {
            if (mx >= s.editX && mx <= s.editX + s.editW && my >= s.editY && my <= s.editY + s.editH) {
                dashboard.showToast("Editing skill: " + s.model.name);
                return true;
            }
            if (mx >= s.toggleX && mx <= s.toggleX + s.toggleW && my >= s.toggleY && my <= s.toggleY + s.toggleH) {
                AdminDataStore.getInstance().toggleSkill(s.model.name);
                dashboard.showToast((!s.model.active ? "Enabled" : "Disabled") + " skill: " + s.model.name);
                return true;
            }
        }

        // Footers
        if (mx >= viewAllCatX && mx <= viewAllCatX + viewAllCatW && my >= viewAllCatY && my <= viewAllCatY + viewAllCatH) {
            showAllCategories = !showAllCategories;
            dashboard.showToast(showAllCategories ? "Displaying all categories" : "Displaying compact categories view");
            return true;
        }
        if (mx >= viewAllSkillX && mx <= viewAllSkillX + viewAllSkillW && my >= viewAllSkillY && my <= viewAllSkillY + viewAllSkillH) {
            showAllSkills = !showAllSkills;
            dashboard.showToast(showAllSkills ? "Displaying all skills" : "Displaying compact skills view");
            return true;
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        boolean pAddCat = addCatHovered;
        addCatHovered = (mx >= addCatBtnX && mx <= addCatBtnX + addCatBtnW && my >= addCatBtnY && my <= addCatBtnY + addCatBtnH);
        if (pAddCat != addCatHovered) changed = true;

        boolean pAddSkill = addSkillHovered;
        addSkillHovered = (mx >= addSkillBtnX && mx <= addSkillBtnX + addSkillBtnW && my >= addSkillBtnY && my <= addSkillBtnY + addSkillBtnH);
        if (pAddSkill != addSkillHovered) changed = true;

        for (CatHitbox c : catHitboxes) {
            boolean pE = c.editHovered;
            boolean pT = c.toggleHovered;
            c.editHovered = (mx >= c.editX && mx <= c.editX + c.editW && my >= c.editY && my <= c.editY + c.editH);
            c.toggleHovered = (mx >= c.toggleX && mx <= c.toggleX + c.toggleW && my >= c.toggleY && my <= c.toggleY + c.toggleH);
            if (pE != c.editHovered || pT != c.toggleHovered) changed = true;
        }

        for (SkillHitbox s : skillHitboxes) {
            boolean pE = s.editHovered;
            boolean pT = s.toggleHovered;
            s.editHovered = (mx >= s.editX && mx <= s.editX + s.editW && my >= s.editY && my <= s.editY + s.editH);
            s.toggleHovered = (mx >= s.toggleX && mx <= s.toggleX + s.toggleW && my >= s.toggleY && my <= s.toggleY + s.toggleH);
            if (pE != s.editHovered || pT != s.toggleHovered) changed = true;
        }

        boolean pVc = viewAllCatHovered;
        viewAllCatHovered = (mx >= viewAllCatX && mx <= viewAllCatX + viewAllCatW && my >= viewAllCatY && my <= viewAllCatY + viewAllCatH);
        if (pVc != viewAllCatHovered) changed = true;

        boolean pVs = viewAllSkillHovered;
        viewAllSkillHovered = (mx >= viewAllSkillX && mx <= viewAllSkillX + viewAllSkillW && my >= viewAllSkillY && my <= viewAllSkillY + viewAllSkillH);
        if (pVs != viewAllSkillHovered) changed = true;

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (catSearchFocused) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (catSearch.length() > 0) catSearch = catSearch.substring(0, catSearch.length() - 1);
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                catSearchFocused = false;
                return true;
            } else if (ch >= 32 && ch <= 126) {
                catSearch += ch;
                return true;
            }
        } else if (skillSearchFocused) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (skillSearch.length() > 0) skillSearch = skillSearch.substring(0, skillSearch.length() - 1);
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                skillSearchFocused = false;
                return true;
            } else if (ch >= 32 && ch <= 126) {
                skillSearch += ch;
                return true;
            }
        }
        return false;
    }
}
