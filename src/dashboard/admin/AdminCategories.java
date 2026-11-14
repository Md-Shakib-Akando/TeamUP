import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Dashboard - Categories & Skills Management View.
 * Matches Figma reference Image 5 with exact dual-column layout, badges, search, and action toggles.
 * Fully dynamic: every button (Add Category, Add Skill, Edit, Enable/Disable, View All, Clear, Reset)
 * features real-time feedback, distinct button styling, reactive hover states, and store mutation.
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

    // Search state & hitboxes
    private String catSearch = "";
    private boolean catSearchFocused = false;
    private int catSearchX, catSearchY, catSearchW, catSearchH = 36;
    private int catClearX, catClearY, catClearSize = 18;
    private boolean catClearHovered = false;
    private int catResetBtnX = -9999, catResetBtnY = -9999, catResetBtnW = 104, catResetBtnH = 28;
    private boolean catResetHovered = false;

    private String skillSearch = "";
    private boolean skillSearchFocused = false;
    private int skillSearchX, skillSearchY, skillSearchW, skillSearchH = 36;
    private int skillClearX, skillClearY, skillClearSize = 18;
    private boolean skillClearHovered = false;
    private int skillResetBtnX = -9999, skillResetBtnY = -9999, skillResetBtnW = 104, skillResetBtnH = 28;
    private boolean skillResetHovered = false;

    // Header buttons
    private int addCatBtnX, addCatBtnY, addCatBtnW = 132, addCatBtnH = 36;
    private boolean addCatHovered = false;

    private int addSkillBtnX, addSkillBtnY, addSkillBtnW = 114, addSkillBtnH = 36;
    private boolean addSkillHovered = false;

    // Footer buttons
    private int viewAllCatX, viewAllCatY, viewAllCatW, viewAllCatH = 24;
    private boolean viewAllCatHovered = false;

    private int viewAllSkillX, viewAllSkillY, viewAllSkillW, viewAllSkillH = 24;
    private boolean viewAllSkillHovered = false;

    private boolean showAllCategories = true;
    private boolean showAllSkills = true;

    public AdminCategories() {
    }

    public int getComputedHeight(int width) {
        int padX = 36;
        int contentW = width - padX * 2;
        boolean stack = (contentW < 820);

        List<AdminDataStore.CategoryModel> allCats = AdminDataStore.getInstance().getAllCategories();
        List<AdminDataStore.CategoryModel> filteredCats = allCats.stream()
                .filter(c -> catSearch.isEmpty() || c.name.toLowerCase().contains(catSearch.toLowerCase()))
                .collect(Collectors.toList());
        int catDisplayLimit = showAllCategories ? filteredCats.size() : Math.min(6, filteredCats.size());
        int catTableH = Math.max(300, 38 + Math.max(1, catDisplayLimit) * 46 + 8);
        int catColH = 48 + 46 + catTableH + 52;

        List<AdminDataStore.SkillModel> allSkills = AdminDataStore.getInstance().getAllSkills();
        List<AdminDataStore.SkillModel> filteredSkills = allSkills.stream()
                .filter(s -> skillSearch.isEmpty() || s.name.toLowerCase().contains(skillSearch.toLowerCase()))
                .collect(Collectors.toList());
        int skillDisplayLimit = showAllSkills ? filteredSkills.size() : Math.min(6, filteredSkills.size());
        int skillTableH = Math.max(300, 38 + Math.max(1, skillDisplayLimit) * 46 + 8);
        int skillColH = 48 + 46 + skillTableH + 52;

        int totalH = 28 + 72; // top padding + title header
        if (stack) {
            totalH += catColH + 24 + skillColH;
        } else {
            totalH += Math.max(catColH, skillColH);
        }
        totalH += 24 + 56 + 36; // gap + bottom banner (56) + bottom padding (36)
        return totalH;
    }

    public void paint(Graphics2D g, int x, int y, int width, int height, AdminDashboard dashboard) {
        int padX = 36;
        int curY = y + 28;
        int contentW = width - padX * 2;
        boolean stack = (contentW < 820);

        // 1. Title Header
        paintHeader(g, x + padX, curY, contentW);
        curY += 72;

        // 2. Dual Columns
        int bottomY;
        if (stack) {
            int catBottom = paintCategoryColumn(g, x + padX, curY, contentW, dashboard);
            bottomY = paintSkillColumn(g, x + padX, catBottom + 24, contentW, dashboard);
        } else {
            int gap = 24;
            int colW = (contentW - gap) / 2;
            int catBottom = paintCategoryColumn(g, x + padX, curY, colW, dashboard);
            int skillBottom = paintSkillColumn(g, x + padX + colW + gap, curY, colW, dashboard);
            bottomY = Math.max(catBottom, skillBottom);
        }

        // 3. Information Banner at bottom
        paintBottomBanner(g, x + padX, bottomY + 24, contentW);
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

    private int paintCategoryColumn(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.CategoryModel> allCats = AdminDataStore.getInstance().getAllCategories();
        long activeCount = allCats.stream().filter(c -> c.active).count();

        // Header line: "Project categories", sub: "X categories · Y active"
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Project categories", x, y + 16);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(allCats.size() + " categories · " + activeCount + " active", x, y + 34);

        // Dynamic "+ Add category" button
        addCatBtnW = 132;
        addCatBtnH = 36;
        addCatBtnX = x + w - addCatBtnW;
        addCatBtnY = y;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addCatBtnX, addCatBtnY, addCatBtnW, addCatBtnH, 8, 8);
        g.setColor(addCatHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBtn);

        // Plus icon
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        int cpx = addCatBtnX + 16;
        int cpy = addCatBtnY + 18;
        g.drawLine(cpx - 4, cpy, cpx + 4, cpy);
        g.drawLine(cpx, cpy - 4, cpx, cpy + 4);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("Add category", addCatBtnX + 26, addCatBtnY + 22);

        int curY = y + 48;

        // Dynamic Search Input with Clear Button
        catSearchX = x;
        catSearchY = curY;
        catSearchW = w;
        catClearSize = 18;
        catClearX = catSearchX + catSearchW - catClearSize - 10;
        catClearY = catSearchY + (catSearchH - catClearSize) / 2;

        paintSearchBar(g, catSearchX, catSearchY, catSearchW, catSearchH, catSearch, "Search categories...",
                catSearchFocused, catClearHovered, catClearX, catClearY, catClearSize);

        curY += 46;

        // Filter categories by search
        List<AdminDataStore.CategoryModel> filteredCats = allCats.stream()
                .filter(c -> catSearch.isEmpty() || c.name.toLowerCase().contains(catSearch.toLowerCase()))
                .collect(Collectors.toList());

        // Card Container - dynamic height
        int headH = 38;
        int rowH = 46;
        int displayLimit = showAllCategories ? filteredCats.size() : Math.min(6, filteredCats.size());
        int tableH = Math.max(300, headH + Math.max(1, displayLimit) * rowH + 8);

        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        // Table Header
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(new RoundRectangle2D.Float(x, curY, w, headH, 12, 12));
        g.fillRect(x, curY + headH - 8, w, 8);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, curY + headH, x + w, curY + headH);

        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("CATEGORY / USAGE", x + 16, curY + 23);
        g.drawString("STATUS", x + w - 186, curY + 23);
        g.drawString("ACTIONS", x + w - 110, curY + 23);

        // Rows
        int rowY = curY + headH;
        catHitboxes.clear();

        if (filteredCats.isEmpty()) {
            g.setFont(AdminTheme.FONT_REGULAR_13);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No categories match \"" + catSearch + "\".", x + 20, rowY + 36);

            // Dynamic Reset search button
            catResetBtnX = x + 20;
            catResetBtnY = rowY + 54;
            catResetBtnW = 106;
            catResetBtnH = 28;
            RoundRectangle2D rbox = new RoundRectangle2D.Float(catResetBtnX, catResetBtnY, catResetBtnW, catResetBtnH, 6, 6);
            g.setColor(catResetHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(rbox);
            g.setColor(catResetHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(rbox);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(catResetHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
            g.drawString("Reset search", catResetBtnX + 16, catResetBtnY + 18);
        } else {
            catResetBtnX = -9999;
            catResetBtnY = -9999;
        }

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
            g.drawString(item.name, x + 16, ry + 20);

            g.setFont(AdminTheme.FONT_REGULAR_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(item.projectCount + " projects", x + 16, ry + 35);

            // Status Badge
            int badgeX = x + w - 190;
            int badgeY = ry + 13;
            paintStatusBadge(g, badgeX, badgeY, item.active ? "Active" : "Disabled", item.active);

            // Dynamic Row Action Buttons: Edit | Disable / Enable
            CatHitbox hb = new CatHitbox();
            hb.model = item;
            hb.editW = 44;
            hb.editH = 24;
            hb.editX = x + w - 128;
            hb.editY = ry + 11;

            hb.toggleW = 58;
            hb.toggleH = 24;
            hb.toggleX = x + w - 74;
            hb.toggleY = ry + 11;

            catHitboxes.add(hb);

            // 1. Dynamic "Edit" button pill
            RoundRectangle2D editBtn = new RoundRectangle2D.Float(hb.editX, hb.editY, hb.editW, hb.editH, 6, 6);
            g.setColor(hb.editHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(editBtn);
            g.setColor(hb.editHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(editBtn);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(hb.editHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MAIN);
            FontMetrics efm = g.getFontMetrics();
            int etw = efm.stringWidth("Edit");
            g.drawString("Edit", hb.editX + (hb.editW - etw) / 2, hb.editY + 16);

            // 2. Dynamic "Disable" / "Enable" toggle button pill
            RoundRectangle2D togBtn = new RoundRectangle2D.Float(hb.toggleX, hb.toggleY, hb.toggleW, hb.toggleH, 6, 6);
            if (item.active) {
                // Disable button (Soft Red styling)
                String togText = "Disable";
                g.setColor(hb.toggleHovered ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xFE, 0xF2, 0xF2));
                g.fill(togBtn);
                g.setColor(hb.toggleHovered ? new Color(0xEF, 0x44, 0x44) : new Color(0xFE, 0xCD, 0xCD));
                g.setStroke(new BasicStroke(1f));
                g.draw(togBtn);

                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(hb.toggleHovered ? new Color(0x99, 0x1B, 0x1B) : AdminTheme.STATUS_RED_TEXT);
                FontMetrics tfm = g.getFontMetrics();
                int ttw = tfm.stringWidth(togText);
                g.drawString(togText, hb.toggleX + (hb.toggleW - ttw) / 2, hb.toggleY + 16);
            } else {
                // Enable button (Soft Green styling)
                String togText = "Enable";
                g.setColor(hb.toggleHovered ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xEC, 0xFD, 0xF5));
                g.fill(togBtn);
                g.setColor(hb.toggleHovered ? new Color(0x16, 0xA3, 0x4A) : new Color(0xA7, 0xF3, 0xD0));
                g.setStroke(new BasicStroke(1f));
                g.draw(togBtn);

                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(hb.toggleHovered ? new Color(0x14, 0x53, 0x2D) : AdminTheme.STATUS_GREEN_TEXT);
                FontMetrics tfm = g.getFontMetrics();
                int ttw = tfm.stringWidth(togText);
                g.drawString(togText, hb.toggleX + (hb.toggleW - ttw) / 2, hb.toggleY + 16);
            }
        }

        // Footer "Showing X of Y categories" (View all button deleted)
        int footY = curY + tableH + 18;
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String footText = "Showing " + displayLimit + " of " + allCats.size() + " categories";
        g.drawString(footText, x, footY + 4);

        viewAllCatX = -9999;
        viewAllCatY = -9999;
        viewAllCatW = 0;
        viewAllCatH = 0;

        return footY + 28;
    }

    private int paintSkillColumn(Graphics2D g, int x, int y, int w, AdminDashboard dashboard) {
        List<AdminDataStore.SkillModel> allSkills = AdminDataStore.getInstance().getAllSkills();
        long activeCount = allSkills.stream().filter(s -> s.active).count();

        // Header line: "Skill tags", sub: "X skills · Y active"
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Skill tags", x, y + 16);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(allSkills.size() + " skills · " + activeCount + " active", x, y + 34);

        // Dynamic "+ Add skill" button
        addSkillBtnW = 114;
        addSkillBtnH = 36;
        addSkillBtnX = x + w - addSkillBtnW;
        addSkillBtnY = y;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addSkillBtnX, addSkillBtnY, addSkillBtnW, addSkillBtnH, 8, 8);
        g.setColor(addSkillHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(addBtn);

        // Plus icon
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        int spx = addSkillBtnX + 16;
        int spy = addSkillBtnY + 18;
        g.drawLine(spx - 4, spy, spx + 4, spy);
        g.drawLine(spx, spy - 4, spx, spy + 4);

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString("Add skill", addSkillBtnX + 26, addSkillBtnY + 22);

        int curY = y + 48;

        // Dynamic Search Input with Clear Button
        skillSearchX = x;
        skillSearchY = curY;
        skillSearchW = w;
        skillClearSize = 18;
        skillClearX = skillSearchX + skillSearchW - skillClearSize - 10;
        skillClearY = skillSearchY + (skillSearchH - skillClearSize) / 2;

        paintSearchBar(g, skillSearchX, skillSearchY, skillSearchW, skillSearchH, skillSearch, "Search skill tags...",
                skillSearchFocused, skillClearHovered, skillClearX, skillClearY, skillClearSize);

        curY += 46;

        // Filter skills by search
        List<AdminDataStore.SkillModel> filteredSkills = allSkills.stream()
                .filter(s -> skillSearch.isEmpty() || s.name.toLowerCase().contains(skillSearch.toLowerCase()))
                .collect(Collectors.toList());

        // Card Container - dynamic height
        int headH = 38;
        int rowH = 46;
        int displayLimit = showAllSkills ? filteredSkills.size() : Math.min(6, filteredSkills.size());
        int tableH = Math.max(300, headH + Math.max(1, displayLimit) * rowH + 8);

        RoundRectangle2D card = new RoundRectangle2D.Float(x, curY, w, tableH, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(card);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(card);

        // Table Header
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(new RoundRectangle2D.Float(x, curY, w, headH, 12, 12));
        g.fillRect(x, curY + headH - 8, w, 8);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(x, curY + headH, x + w, curY + headH);

        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("SKILL / USAGE", x + 16, curY + 23);
        g.drawString("STATUS", x + w - 186, curY + 23);
        g.drawString("ACTIONS", x + w - 110, curY + 23);

        // Rows
        int rowY = curY + headH;
        skillHitboxes.clear();

        if (filteredSkills.isEmpty()) {
            g.setFont(AdminTheme.FONT_REGULAR_13);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("No skills match \"" + skillSearch + "\".", x + 20, rowY + 36);

            // Dynamic Reset search button
            skillResetBtnX = x + 20;
            skillResetBtnY = rowY + 54;
            skillResetBtnW = 106;
            skillResetBtnH = 28;
            RoundRectangle2D rbox = new RoundRectangle2D.Float(skillResetBtnX, skillResetBtnY, skillResetBtnW, skillResetBtnH, 6, 6);
            g.setColor(skillResetHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(rbox);
            g.setColor(skillResetHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(rbox);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(skillResetHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_DARK);
            g.drawString("Reset search", skillResetBtnX + 16, skillResetBtnY + 18);
        } else {
            skillResetBtnX = -9999;
            skillResetBtnY = -9999;
        }

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
            g.drawString(item.name, x + 16, ry + 20);

            g.setFont(AdminTheme.FONT_REGULAR_11);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString(String.format("%,d members", item.memberCount), x + 16, ry + 35);

            // Status Badge
            int badgeX = x + w - 190;
            int badgeY = ry + 13;
            paintStatusBadge(g, badgeX, badgeY, item.active ? "Active" : "Disabled", item.active);

            // Dynamic Row Action Buttons: Edit | Disable / Enable
            SkillHitbox hb = new SkillHitbox();
            hb.model = item;
            hb.editW = 44;
            hb.editH = 24;
            hb.editX = x + w - 128;
            hb.editY = ry + 11;

            hb.toggleW = 58;
            hb.toggleH = 24;
            hb.toggleX = x + w - 74;
            hb.toggleY = ry + 11;

            skillHitboxes.add(hb);

            // 1. Dynamic "Edit" button pill
            RoundRectangle2D editBtn = new RoundRectangle2D.Float(hb.editX, hb.editY, hb.editW, hb.editH, 6, 6);
            g.setColor(hb.editHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(editBtn);
            g.setColor(hb.editHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1f));
            g.draw(editBtn);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(hb.editHovered ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_MAIN);
            FontMetrics efm = g.getFontMetrics();
            int etw = efm.stringWidth("Edit");
            g.drawString("Edit", hb.editX + (hb.editW - etw) / 2, hb.editY + 16);

            // 2. Dynamic "Disable" / "Enable" toggle button pill
            RoundRectangle2D togBtn = new RoundRectangle2D.Float(hb.toggleX, hb.toggleY, hb.toggleW, hb.toggleH, 6, 6);
            if (item.active) {
                // Disable button (Soft Red styling)
                String togText = "Disable";
                g.setColor(hb.toggleHovered ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xFE, 0xF2, 0xF2));
                g.fill(togBtn);
                g.setColor(hb.toggleHovered ? new Color(0xEF, 0x44, 0x44) : new Color(0xFE, 0xCD, 0xCD));
                g.setStroke(new BasicStroke(1f));
                g.draw(togBtn);

                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(hb.toggleHovered ? new Color(0x99, 0x1B, 0x1B) : AdminTheme.STATUS_RED_TEXT);
                FontMetrics tfm = g.getFontMetrics();
                int ttw = tfm.stringWidth(togText);
                g.drawString(togText, hb.toggleX + (hb.toggleW - ttw) / 2, hb.toggleY + 16);
            } else {
                // Enable button (Soft Green styling)
                String togText = "Enable";
                g.setColor(hb.toggleHovered ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xEC, 0xFD, 0xF5));
                g.fill(togBtn);
                g.setColor(hb.toggleHovered ? new Color(0x16, 0xA3, 0x4A) : new Color(0xA7, 0xF3, 0xD0));
                g.setStroke(new BasicStroke(1f));
                g.draw(togBtn);

                g.setFont(AdminTheme.FONT_BOLD_11);
                g.setColor(hb.toggleHovered ? new Color(0x14, 0x53, 0x2D) : AdminTheme.STATUS_GREEN_TEXT);
                FontMetrics tfm = g.getFontMetrics();
                int ttw = tfm.stringWidth(togText);
                g.drawString(togText, hb.toggleX + (hb.toggleW - ttw) / 2, hb.toggleY + 16);
            }
        }

        // Footer "Showing X of Y skills" (View all button deleted)
        int footY = curY + tableH + 18;
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        String footText = "Showing " + displayLimit + " of " + allSkills.size() + " skills";
        g.drawString(footText, x, footY + 4);

        viewAllSkillX = -9999;
        viewAllSkillY = -9999;
        viewAllSkillW = 0;
        viewAllSkillH = 0;

        return footY + 28;
    }

    private void paintSearchBar(Graphics2D g, int sx, int sy, int sw, int sh, String text, String placeholder,
                               boolean focused, boolean clearHovered, int clearX, int clearY, int clearSize) {
        RoundRectangle2D sbox = new RoundRectangle2D.Float(sx, sy, sw, sh, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sbox);
        g.setColor(focused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(focused ? 1.5f : 1.2f));
        g.draw(sbox);

        // Search lens icon
        g.setColor(focused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(sx + 11, sy + 11, 10, 10);
        g.drawLine(sx + 19, sy + 19, sx + 23, sy + 23);

        // Text or Placeholder
        g.setFont(AdminTheme.FONT_REGULAR_12);
        if (text.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString(placeholder, sx + 32, sy + 22);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(text, sx + 32, sy + 22);

            // Dynamic Clear '✕' button
            g.setColor(clearHovered ? new Color(0xE2, 0xE8, 0xF0) : new Color(0xF1, 0xF5, 0xF9));
            g.fillOval(clearX, clearY, clearSize, clearSize);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(clearHovered ? AdminTheme.TEXT_DARK : AdminTheme.TEXT_MUTED);
            FontMetrics xfm = g.getFontMetrics();
            g.drawString("✕", clearX + (clearSize - xfm.stringWidth("✕")) / 2, clearY + 13);
        }
    }

    private void paintStatusBadge(Graphics2D g, int bx, int by, String text, boolean active) {
        int bw = active ? 54 : 64;
        int bh = 20;
        RoundRectangle2D pill = new RoundRectangle2D.Float(bx, by, bw, bh, 10, 10);
        g.setColor(active ? AdminTheme.STATUS_GREEN_BG : AdminTheme.STATUS_GRAY_BG);
        g.fill(pill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(active ? AdminTheme.STATUS_GREEN_TEXT : AdminTheme.STATUS_GRAY_TEXT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + 14);
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
        // 1. Add Category button
        if (mx >= addCatBtnX && mx <= addCatBtnX + addCatBtnW && my >= addCatBtnY && my <= addCatBtnY + addCatBtnH) {
            dashboard.openAddCategoryModal();
            return true;
        }

        // 2. Add Skill button
        if (mx >= addSkillBtnX && mx <= addSkillBtnX + addSkillBtnW && my >= addSkillBtnY && my <= addSkillBtnY + addSkillBtnH) {
            dashboard.openAddSkillModal();
            return true;
        }

        // 3. Search Clear buttons
        if (!catSearch.isEmpty() && mx >= catClearX && mx <= catClearX + catClearSize && my >= catClearY && my <= catClearY + catClearSize) {
            catSearch = "";
            catSearchFocused = false;
            dashboard.showToast("Category search cleared");
            return true;
        }

        if (!skillSearch.isEmpty() && mx >= skillClearX && mx <= skillClearX + skillClearSize && my >= skillClearY && my <= skillClearY + skillClearSize) {
            skillSearch = "";
            skillSearchFocused = false;
            dashboard.showToast("Skill search cleared");
            return true;
        }

        // 4. Empty State Reset Search buttons
        if (catResetBtnX > 0 && mx >= catResetBtnX && mx <= catResetBtnX + catResetBtnW && my >= catResetBtnY && my <= catResetBtnY + catResetBtnH) {
            catSearch = "";
            catSearchFocused = false;
            dashboard.showToast("Category search reset");
            return true;
        }

        if (skillResetBtnX > 0 && mx >= skillResetBtnX && mx <= skillResetBtnX + skillResetBtnW && my >= skillResetBtnY && my <= skillResetBtnY + skillResetBtnH) {
            skillSearch = "";
            skillSearchFocused = false;
            dashboard.showToast("Skill search reset");
            return true;
        }

        // 5. Search Bar Focuses
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

        // 6. Category Row Action Buttons (Edit & Disable/Enable)
        for (CatHitbox c : catHitboxes) {
            if (mx >= c.editX && mx <= c.editX + c.editW && my >= c.editY && my <= c.editY + c.editH) {
                dashboard.openEditCategoryModal(c.model.name);
                return true;
            }
            if (mx >= c.toggleX && mx <= c.toggleX + c.toggleW && my >= c.toggleY && my <= c.toggleY + c.toggleH) {
                AdminDataStore.getInstance().toggleCategory(c.model.name);
                dashboard.showToast((!c.model.active ? "Enabled" : "Disabled") + " category: " + c.model.name);
                return true;
            }
        }

        // 7. Skill Row Action Buttons (Edit & Disable/Enable)
        for (SkillHitbox s : skillHitboxes) {
            if (mx >= s.editX && mx <= s.editX + s.editW && my >= s.editY && my <= s.editY + s.editH) {
                dashboard.openEditSkillModal(s.model.name);
                return true;
            }
            if (mx >= s.toggleX && mx <= s.toggleX + s.toggleW && my >= s.toggleY && my <= s.toggleY + s.toggleH) {
                AdminDataStore.getInstance().toggleSkill(s.model.name);
                dashboard.showToast((!s.model.active ? "Enabled" : "Disabled") + " skill: " + s.model.name);
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean changed = false;

        // Add Category button hover
        boolean pAddCat = addCatHovered;
        addCatHovered = (mx >= addCatBtnX && mx <= addCatBtnX + addCatBtnW && my >= addCatBtnY && my <= addCatBtnY + addCatBtnH);
        if (pAddCat != addCatHovered) changed = true;

        // Add Skill button hover
        boolean pAddSkill = addSkillHovered;
        addSkillHovered = (mx >= addSkillBtnX && mx <= addSkillBtnX + addSkillBtnW && my >= addSkillBtnY && my <= addSkillBtnY + addSkillBtnH);
        if (pAddSkill != addSkillHovered) changed = true;

        // Search clear hovers
        boolean pClrCat = catClearHovered;
        catClearHovered = (!catSearch.isEmpty() && mx >= catClearX && mx <= catClearX + catClearSize && my >= catClearY && my <= catClearY + catClearSize);
        if (pClrCat != catClearHovered) changed = true;

        boolean pClrSkill = skillClearHovered;
        skillClearHovered = (!skillSearch.isEmpty() && mx >= skillClearX && mx <= skillClearX + skillClearSize && my >= skillClearY && my <= skillClearY + skillClearSize);
        if (pClrSkill != skillClearHovered) changed = true;

        // Reset search hovers
        boolean pRstCat = catResetHovered;
        catResetHovered = (catResetBtnX > 0 && mx >= catResetBtnX && mx <= catResetBtnX + catResetBtnW && my >= catResetBtnY && my <= catResetBtnY + catResetBtnH);
        if (pRstCat != catResetHovered) changed = true;

        boolean pRstSkill = skillResetHovered;
        skillResetHovered = (skillResetBtnX > 0 && mx >= skillResetBtnX && mx <= skillResetBtnX + skillResetBtnW && my >= skillResetBtnY && my <= skillResetBtnY + skillResetBtnH);
        if (pRstSkill != skillResetHovered) changed = true;

        // Category rows hovers
        for (CatHitbox c : catHitboxes) {
            boolean pE = c.editHovered;
            boolean pT = c.toggleHovered;
            c.editHovered = (mx >= c.editX && mx <= c.editX + c.editW && my >= c.editY && my <= c.editY + c.editH);
            c.toggleHovered = (mx >= c.toggleX && mx <= c.toggleX + c.toggleW && my >= c.toggleY && my <= c.toggleY + c.toggleH);
            if (pE != c.editHovered || pT != c.toggleHovered) changed = true;
        }

        // Skill rows hovers
        for (SkillHitbox s : skillHitboxes) {
            boolean pE = s.editHovered;
            boolean pT = s.toggleHovered;
            s.editHovered = (mx >= s.editX && mx <= s.editX + s.editW && my >= s.editY && my <= s.editY + s.editH);
            s.toggleHovered = (mx >= s.toggleX && mx <= s.toggleX + s.toggleW && my >= s.toggleY && my <= s.toggleY + s.toggleH);
            if (pE != s.editHovered || pT != s.toggleHovered) changed = true;
        }

        viewAllCatHovered = false;
        viewAllSkillHovered = false;

        return changed;
    }

    public boolean onKey(char ch, int keyCode, AdminDashboard dashboard) {
        if (catSearchFocused) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (catSearch.length() > 0) catSearch = catSearch.substring(0, catSearch.length() - 1);
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
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
            } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                skillSearchFocused = false;
                return true;
            } else if (ch >= 32 && ch <= 126) {
                skillSearch += ch;
                return true;
            }
        }
        return false;
    }

    // Getters and setters for testing & dynamic integration
    public boolean isShowAllCategories() {
        return showAllCategories;
    }

    public void setShowAllCategories(boolean show) {
        this.showAllCategories = show;
    }

    public boolean isShowAllSkills() {
        return showAllSkills;
    }

    public void setShowAllSkills(boolean show) {
        this.showAllSkills = show;
    }

    public List<CatHitbox> getCatHitboxes() {
        return catHitboxes;
    }

    public List<SkillHitbox> getSkillHitboxes() {
        return skillHitboxes;
    }

    public int getAddCatBtnX() { return addCatBtnX; }
    public int getAddCatBtnY() { return addCatBtnY; }
    public int getAddSkillBtnX() { return addSkillBtnX; }
    public int getAddSkillBtnY() { return addSkillBtnY; }

    public int getViewAllCatX() { return viewAllCatX; }
    public int getViewAllCatY() { return viewAllCatY; }
    public int getViewAllSkillX() { return viewAllSkillX; }
    public int getViewAllSkillY() { return viewAllSkillY; }

    public String getCatSearch() { return catSearch; }
    public void setCatSearch(String q) { this.catSearch = (q == null) ? "" : q; }

    public String getSkillSearch() { return skillSearch; }
    public void setSkillSearch(String q) { this.skillSearch = (q == null) ? "" : q; }

    public boolean isCatClearHovered() { return catClearHovered; }
    public boolean isSkillClearHovered() { return skillClearHovered; }
    public boolean isAddCatHovered() { return addCatHovered; }
    public boolean isAddSkillHovered() { return addSkillHovered; }

    public int getCatClearX() { return catClearX; }
    public int getCatClearY() { return catClearY; }
    public int getSkillClearX() { return skillClearX; }
    public int getSkillClearY() { return skillClearY; }

    public int getCatResetBtnX() { return catResetBtnX; }
    public int getCatResetBtnY() { return catResetBtnY; }
    public int getSkillResetBtnX() { return skillResetBtnX; }
    public int getSkillResetBtnY() { return skillResetBtnY; }
}
