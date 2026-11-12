import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the "Discover Projects" / "Explore projects" view for TeamUp Student Dashboard.
 * Matches Figma reference Image 1 with exact precision.
 * Fully interactive with live search, 5 filter dropdowns, category pills, bookmarking,
 * grid/list toggle, modal details viewer, and pagination.
 */
public class DashboardProjects {

    public static class ProjectCard {
        public int id;
        public String title;
        public String category; // Campus life, Sustainability, Research, Social impact, Education
        public String experience; // Beginner, Intermediate, Advanced
        public String description;
        public List<String> skills = new ArrayList<>();
        public boolean recruiting = true;
        public int currentMembers;
        public int maxMembers;
        public String openRoles;
        public String creatorName;
        public String creatorInitials;
        public Color creatorColor;
        public int iconType; // 0=Calendar, 1=Leaf, 2=Flask, 3=MapPin, 4=Book, 5=Food, 6=Code
        public Color iconBg;
        public Color iconColor;
        public boolean bookmarked = false;

        // Hitbox tracking per frame
        public int hitX, hitY, hitW, hitH;
        public int bmX, bmY, bmSize = 24;
        public int viewX, viewY, viewW, viewH;

        public ProjectCard(int id, String title, String category, String experience, String description,
                           String[] skillArr, boolean recruiting, int curM, int maxM,
                           String openRoles, String creatorName, String creatorInitials, Color creatorCol,
                           int iconType, Color iconBg, Color iconColor) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.experience = experience;
            this.description = description;
            Collections.addAll(this.skills, skillArr);
            this.recruiting = recruiting;
            this.currentMembers = curM;
            this.maxMembers = maxM;
            this.openRoles = openRoles;
            this.creatorName = creatorName;
            this.creatorInitials = creatorInitials;
            this.creatorColor = creatorCol;
            this.iconType = iconType;
            this.iconBg = iconBg;
            this.iconColor = iconColor;
        }
    }

    private final List<ProjectCard> allProjects = new ArrayList<>();
    private final List<ProjectCard> filteredProjects = new ArrayList<>();

    // State & Filters
    private String searchKeyword = "";
    private boolean searchFocused = false;
    private String filterCategory = "All projects";
    private String filterSkill = "All";
    private String filterExperience = "All";
    private String filterRecruiting = "All";
    private String selectedSort = "Recommended";
    private boolean isGridView = true;

    private int currentPage = 1;
    private final int itemsPerPage = 6;
    private int totalContentHeight = 1150;

    // Dropdowns
    public enum DropdownType { NONE, CATEGORY, SKILLS, EXPERIENCE, RECRUITING, SORT }
    private DropdownType activeDropdown = DropdownType.NONE;

    // Layout Hitboxes
    private int createBtnX, createBtnY, createBtnW = 138, createBtnH = 38;
    private boolean createBtnHovered = false;

    private int searchX, searchY, searchW, searchH = 40;
    private int catDropX, catDropY, catDropW = 110, catDropH = 40;
    private int skillDropX, skillDropY, skillDropW = 95, skillDropH = 40;
    private int expDropX, expDropY, expDropW = 115, expDropH = 40;
    private int recDropX, recDropY, recDropW = 115, recDropH = 40;
    private int sortDropX, sortDropY, sortDropW = 140, sortDropH = 34;

    private int gridBtnX, gridBtnY, listBtnX, listBtnY, toggleBtnSize = 34;

    private static class PillBox {
        String label;
        int x, y, w, h;
        PillBox(String label, int x, int y, int w, int h) {
            this.label = label; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
    private final List<PillBox> categoryPills = new ArrayList<>();

    private int prevBtnX, prevBtnY, prevBtnW = 90, prevBtnH = 32;
    private int nextBtnX, nextBtnY, nextBtnW = 75, nextBtnH = 32;
    private final List<PillBox> pageBoxes = new ArrayList<>();

    // Active dropdown overlay click bounds
    private int overlayX, overlayY, overlayW, overlayH;
    private String[] overlayItems = new String[0];

    public DashboardProjects() {
        initDemoProjects();
        applyFilters();
    }

    private void initDemoProjects() {
        allProjects.add(new ProjectCard(
                1, "CampusConnect", "Campus life", "Intermediate",
                "One place to discover university events, meet people and make campus feel connected.",
                new String[]{"React", "TypeScript", "Node.js"},
                true, 5, 7, "Open: Frontend developer · QA engineer",
                "Priya Shah", "PS", new Color(0xEA, 0x4C, 0x89),
                0, new Color(0xFD, 0xEE, 0xF4), new Color(0xEA, 0x4C, 0x89)
        ));
        allProjects.add(new ProjectCard(
                2, "GreenRoute", "Sustainability", "Intermediate",
                "Make low-carbon campus travel easier with shared routes and real-time mobility insights.",
                new String[]{"React", "Maps API", "Node.js"},
                true, 4, 6, "Open: Frontend developer · Data analyst",
                "Maya Patel", "MP", new Color(0x10, 0xB9, 0x81),
                1, new Color(0xEB, 0xFB, 0xF5), new Color(0x10, 0xB9, 0x81)
        ));
        allProjects.add(new ProjectCard(
                3, "OpenLab", "Research", "Intermediate",
                "Help students discover research opportunities and collaborate across university labs.",
                new String[]{"TypeScript", "PostgreSQL", "Figma"},
                true, 3, 5, "Open: Product designer · Backend developer",
                "Noah Williams", "NW", new Color(0x7C, 0x5C, 0xFF),
                2, new Color(0xF3, 0xF0, 0xFF), new Color(0x7C, 0x5C, 0xFF)
        ));
        allProjects.add(new ProjectCard(
                4, "AccessMap", "Social impact", "Intermediate",
                "Build a community-powered map of accessible spaces and routes around campus.",
                new String[]{"React Native", "Maps API"},
                true, 3, 5, "Open: Mobile developer · UX researcher",
                "Leila Hassan", "LH", new Color(0xEC, 0x48, 0x99),
                3, new Color(0xE0, 0xF2, 0xFE), new Color(0x0E, 0xA5, 0xE9)
        ));
        allProjects.add(new ProjectCard(
                5, "PeerPrep", "Education", "Intermediate",
                "Turn interview practice into a shared learning experience with peer feedback and goals.",
                new String[]{"Next.js", "Prisma", "Tailwind"},
                true, 2, 4, "Open: Full-stack developer · Product designer",
                "Oliver Brooks", "OB", new Color(0x8B, 0x5C, 0xF6),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)
        ));
        allProjects.add(new ProjectCard(
                6, "FoodLoop", "Sustainability", "Intermediate",
                "Connect campus cafés with students to reduce food waste, one shared meal at a time.",
                new String[]{"React", "Node.js", "Figma"},
                true, 4, 6, "Open: Frontend developer · QA engineer",
                "Aisha Rahman", "AR", new Color(0x14, 0xB8, 0xA6),
                5, new Color(0xF0, 0xFD, 0xFA), new Color(0x0D, 0x94, 0x88)
        ));

        // Page 2
        allProjects.add(new ProjectCard(
                7, "StudySync", "Education", "Beginner",
                "Collaborative note-sharing and AI-powered study group matcher for STEM classes.",
                new String[]{"Python", "FastAPI", "React"},
                true, 3, 5, "Open: Python backend · UI designer",
                "Marcus Chen", "MC", new Color(0x10, 0xB9, 0x81),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)
        ));
        allProjects.add(new ProjectCard(
                8, "SkillSwap", "Campus life", "Intermediate",
                "Peer tutoring exchange platform where students trade coding tips for languages.",
                new String[]{"Vue.js", "Firebase", "Tailwind"},
                true, 2, 4, "Open: Full-stack engineer · Community lead",
                "Sarah Jenkins", "SJ", new Color(0xF5, 0x9E, 0x0B),
                0, new Color(0xFE, 0xF3, 0xC7), new Color(0xD9, 0x77, 0x06)
        ));
        allProjects.add(new ProjectCard(
                9, "SolarCampus", "Sustainability", "Advanced",
                "IoT sensor monitoring of rooftop solar panels across engineering facilities.",
                new String[]{"Python", "C++", "PostgreSQL"},
                true, 4, 5, "Open: Embedded systems · Data pipeline",
                "Daniel Kim", "DK", new Color(0xEF, 0x44, 0x44),
                1, new Color(0xEC, 0xFD, 0xF5), new Color(0x10, 0xB9, 0x81)
        ));
        allProjects.add(new ProjectCard(
                10, "MentalWell", "Social impact", "Intermediate",
                "Anonymous peer mental health reflection journaling and licensed counselor triage.",
                new String[]{"Flutter", "Node.js", "MongoDB"},
                true, 2, 4, "Open: Mobile developer · UX researcher",
                "Elena Vance", "EV", new Color(0x8B, 0x5C, 0xF6),
                3, new Color(0xF3, 0xE8, 0xFF), new Color(0x8B, 0x5C, 0xF6)
        ));
        allProjects.add(new ProjectCard(
                11, "RoboNav", "Research", "Advanced",
                "Autonomous indoor navigation algorithms for assistive campus delivery robots.",
                new String[]{"ROS", "C++", "Python"},
                true, 3, 6, "Open: Robotics engineer · Computer vision",
                "Alex Rivera", "AR", new Color(0x3B, 0x82, 0xF6),
                2, new Color(0xEE, 0xF2, 0xFF), new Color(0x3B, 0x82, 0xF6)
        ));
        allProjects.add(new ProjectCard(
                12, "EcoDine", "Sustainability", "Beginner",
                "Gamified reusable container return system for university dining halls.",
                new String[]{"React", "Tailwind", "Supabase"},
                true, 3, 4, "Open: Frontend engineer · Content creator",
                "Chloe Bennett", "CB", new Color(0x10, 0xB9, 0x81),
                5, new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69)
        ));

        // Page 3
        allProjects.add(new ProjectCard(
                13, "CourseCritic", "Education", "Intermediate",
                "Syllabus breakdown and workload transparent reviews written by verified alumni.",
                new String[]{"Next.js", "PostgreSQL", "Prisma"},
                true, 4, 5, "Open: Full-stack engineer · Marketing",
                "Lucas Vance", "LV", new Color(0x63, 0x66, 0xF1),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)
        ));
        allProjects.add(new ProjectCard(
                14, "FairRide", "Campus life", "Intermediate",
                "Carpooling network connecting suburban commuters with campus parking permit holders.",
                new String[]{"React Native", "Firebase", "Maps API"},
                true, 3, 5, "Open: Mobile developer · UI/UX designer",
                "Fatima Zahra", "FZ", new Color(0xF9, 0x73, 0x16),
                0, new Color(0xFF, 0xED, 0xD5), new Color(0xEA, 0x58, 0x0C)
        ));
        allProjects.add(new ProjectCard(
                15, "QuantumSim", "Research", "Advanced",
                "Interactive web visualizer for quantum computing logic gates and entanglement.",
                new String[]{"TypeScript", "WebGL", "Python"},
                true, 2, 4, "Open: WebGL engineer · Quantum physics",
                "Noah Williams", "NW", new Color(0x8B, 0x5C, 0xF6),
                2, new Color(0xF3, 0xE8, 0xFF), new Color(0x7C, 0x3A, 0xED)
        ));
        allProjects.add(new ProjectCard(
                16, "SoundScape", "Social impact", "Intermediate",
                "Crowdsourced campus quiet study space decibel heatmaps and sensory sensory map.",
                new String[]{"Swift", "CoreAudio", "Node.js"},
                true, 3, 5, "Open: iOS developer · Audio engineer",
                "Ryan Cooper", "RC", new Color(0x06, 0xB6, 0xD4),
                3, new Color(0xEC, 0xFE, 0xFF), new Color(0x08, 0x91, 0xB2)
        ));
        allProjects.add(new ProjectCard(
                17, "BookLoop", "Campus life", "Beginner",
                "Textbook trade-in and lending library eliminating out-of-pocket course materials cost.",
                new String[]{"React", "Node.js", "Express"},
                true, 4, 6, "Open: Frontend engineer · Community lead",
                "Emma Wilson", "EW", new Color(0x10, 0xB9, 0x81),
                0, new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69)
        ));
        allProjects.add(new ProjectCard(
                18, "BioInsight", "Research", "Advanced",
                "Deep learning models for classifying microscope slide anomalies in biology labs.",
                new String[]{"PyTorch", "Python", "FastAPI"},
                true, 2, 4, "Open: ML engineer · Bioinformatics",
                "David Park", "DP", new Color(0xEC, 0x48, 0x99),
                2, new Color(0xFD, 0xE8, 0xE8), new Color(0xE0, 0x24, 0x24)
        ));
    }

    public void applyFilters() {
        filteredProjects.clear();
        String query = searchKeyword.trim().toLowerCase();

        for (ProjectCard p : allProjects) {
            // Category filter
            if (!filterCategory.equals("All projects") && !p.category.equalsIgnoreCase(filterCategory)) {
                continue;
            }
            // Skill dropdown
            if (!filterSkill.equals("All")) {
                boolean hasSkill = false;
                for (String s : p.skills) {
                    if (s.equalsIgnoreCase(filterSkill)) { hasSkill = true; break; }
                }
                if (!hasSkill) continue;
            }
            // Experience dropdown
            if (!filterExperience.equals("All") && !p.experience.equalsIgnoreCase(filterExperience)) {
                continue;
            }
            // Recruiting dropdown
            if (filterRecruiting.equals("Recruiting Only") && !p.recruiting) {
                continue;
            }
            if (filterRecruiting.equals("Full Teams") && p.recruiting) {
                continue;
            }
            // Search text
            if (!query.isEmpty()) {
                boolean matchTitle = p.title.toLowerCase().contains(query);
                boolean matchDesc = p.description.toLowerCase().contains(query);
                boolean matchCreator = p.creatorName.toLowerCase().contains(query);
                boolean matchSkill = false;
                for (String s : p.skills) {
                    if (s.toLowerCase().contains(query)) { matchSkill = true; break; }
                }
                if (!matchTitle && !matchDesc && !matchCreator && !matchSkill) {
                    continue;
                }
            }
            filteredProjects.add(p);
        }

        // Sorting
        if ("Most Recent".equals(selectedSort)) {
            Collections.reverse(filteredProjects);
        } else if ("Most Members".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(b.currentMembers, a.currentMembers));
        } else if ("Alphabetical".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
        }

        // Pagination clamp
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredProjects.size() / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
    }

    public int getTotalContentHeight() {
        return totalContentHeight;
    }

    // =========================================================================
    // PAINTING METHODS
    // =========================================================================

    public void paint(Graphics2D g, int startX, int startY, int width, int height, StudentDashboard dashboard) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Page Title & Subtitle + "+ Create project" Primary Button
        g.setFont(DashboardTheme.FONT_HEADING);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Explore projects", startX + padX, curY + 24);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Find a meaningful problem. Bring your skills. Build something that matters.", startX + padX, curY + 46);

        // "+ Create project" Primary Button
        createBtnW = 138;
        createBtnH = 38;
        createBtnX = startX + padX + contentW - createBtnW;
        createBtnY = curY;
        RoundRectangle2D primBtnShape = new RoundRectangle2D.Float(createBtnX, createBtnY, createBtnW, createBtnH, 10, 10);
        g.setColor(createBtnHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(primBtnShape);

        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        g.drawString("+  Create project", createBtnX + 16, createBtnY + 24);

        curY += 72;

        // 2. Search Box + Filter Dropdowns Row
        searchW = Math.max(260, contentW - 460);
        searchH = 40;
        searchX = startX + padX;
        searchY = curY;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1.0f));
        g.draw(sBox);

        // Magnifier icon
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 13, 11, 11);
        g.drawLine(searchX + 23, searchY + 22, searchX + 27, searchY + 26);

        // Search text or placeholder
        g.setFont(DashboardTheme.FONT_REG_13);
        if (searchKeyword.isEmpty() && !searchFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Search projects by keyword or skill...", searchX + 36, searchY + 25);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            String display = searchKeyword;
            if (searchFocused && (System.currentTimeMillis() % 1000 < 500)) display += "|";
            g.drawString(display, searchX + 36, searchY + 25);
        }

        // Dropdown Buttons Row
        int dropX = searchX + searchW + 12;
        catDropX = dropX; catDropY = curY; catDropW = 110;
        paintFilterDropBtn(g, catDropX, catDropY, catDropW, 40, "Category", filterCategory.equals("All projects") ? "" : filterCategory, activeDropdown == DropdownType.CATEGORY);
        dropX += catDropW + 8;

        skillDropX = dropX; skillDropY = curY; skillDropW = 95;
        paintFilterDropBtn(g, skillDropX, skillDropY, skillDropW, 40, "Skills", filterSkill.equals("All") ? "" : filterSkill, activeDropdown == DropdownType.SKILLS);
        dropX += skillDropW + 8;

        expDropX = dropX; expDropY = curY; expDropW = 115;
        paintFilterDropBtn(g, expDropX, expDropY, expDropW, 40, "Experience", filterExperience.equals("All") ? "" : filterExperience, activeDropdown == DropdownType.EXPERIENCE);
        dropX += expDropW + 8;

        recDropX = dropX; recDropY = curY; recDropW = 115;
        paintFilterDropBtn(g, recDropX, recDropY, recDropW, 40, "Recruiting", filterRecruiting.equals("All") ? "" : filterRecruiting, activeDropdown == DropdownType.RECRUITING);

        curY += 56;

        // 3. Category Filter Pills Row
        categoryPills.clear();
        String[] pillLabels = {"All projects", "Campus life", "Education", "Sustainability", "Research", "Social impact"};
        int pillX = startX + padX;
        int pillH = 32;

        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics fm = g.getFontMetrics();

        for (String pl : pillLabels) {
            int strW = fm.stringWidth(pl);
            int pillW = strW + 24;
            boolean isSel = pl.equalsIgnoreCase(filterCategory);

            RoundRectangle2D pShape = new RoundRectangle2D.Float(pillX, curY, pillW, pillH, 8, 8);
            if (isSel) {
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(pShape);
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.setFont(DashboardTheme.FONT_BOLD_13);
            } else {
                g.setColor(Color.WHITE);
                g.fill(pShape);
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.setStroke(new BasicStroke(1f));
                g.draw(pShape);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.setFont(DashboardTheme.FONT_REG_13);
            }
            g.drawString(pl, pillX + 12, curY + 21);

            categoryPills.add(new PillBox(pl, pillX, curY, pillW, pillH));
            pillX += pillW + 8;
        }

        curY += 48;

        // 4. Sub-Header: Count + Sort + Grid/List Toggle
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        String countStr = filteredProjects.size() + " projects looking for people like you";
        g.drawString(countStr, startX + padX, curY + 22);

        // Grid / List Toggle buttons (Right aligned)
        int toggleX = startX + padX + contentW - toggleBtnSize;
        listBtnX = toggleX; listBtnY = curY;
        paintViewToggleBtn(g, listBtnX, listBtnY, toggleBtnSize, !isGridView, false);

        toggleX -= (toggleBtnSize + 4);
        gridBtnX = toggleX; gridBtnY = curY;
        paintViewToggleBtn(g, gridBtnX, gridBtnY, toggleBtnSize, isGridView, true);

        // Sort Dropdown
        sortDropW = 145;
        sortDropH = 34;
        sortDropX = gridBtnX - sortDropW - 12;
        sortDropY = curY;
        paintFilterDropBtn(g, sortDropX, sortDropY, sortDropW, sortDropH, selectedSort, "", activeDropdown == DropdownType.SORT);

        curY += 46;

        // 5. Projects Grid or List View
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredProjects.size());

        if (filteredProjects.isEmpty()) {
            paintEmptyState(g, startX + padX, curY, contentW, 280);
            curY += 300;
        } else if (isGridView) {
            int gap = 20;
            int cardW = (contentW - gap) / 2;
            int cardH = 210;

            int rowCount = 0;
            for (int i = startIndex; i < endIndex; i++) {
                int col = (i - startIndex) % 2;
                int row = (i - startIndex) / 2;
                int cx = startX + padX + col * (cardW + gap);
                int cy = curY + row * (cardH + gap);

                paintSingleProjectCard(g, cx, cy, cardW, cardH, filteredProjects.get(i), dashboard);
                rowCount = Math.max(rowCount, row + 1);
            }
            curY += rowCount * (cardH + gap) + 10;
        } else {
            // List View
            int listCardH = 105;
            int gap = 12;
            for (int i = startIndex; i < endIndex; i++) {
                int cy = curY + (i - startIndex) * (listCardH + gap);
                paintSingleProjectListCard(g, startX + padX, cy, contentW, listCardH, filteredProjects.get(i), dashboard);
            }
            curY += (endIndex - startIndex) * (listCardH + gap) + 10;
        }

        // 6. Pagination Row
        curY += 10;
        paintPagination(g, startX + padX, curY, contentW, filteredProjects.size());
        curY += 80;

        totalContentHeight = curY - startY + 40;
    }

    private void paintFilterDropBtn(Graphics2D g, int x, int y, int w, int h, String label, String value, boolean open) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(open ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(open ? 1.5f : 1.0f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(value.isEmpty() ? DashboardTheme.TEXT_MUTED : DashboardTheme.BRAND_PURPLE);
        String text = value.isEmpty() ? label : value;
        if (text.length() > 14) text = text.substring(0, 12) + "..";
        g.drawString(text, x + 12, y + (h / 2) + 5);

        // Chevron
        Draw.drawChevronDown(g, x + w - 16, y + (h / 2) - 2, 8, DashboardTheme.TEXT_MUTED, open ? 1.0f : 0.0f);
    }

    private void paintViewToggleBtn(Graphics2D g, int x, int y, int size, boolean active, boolean isGrid) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, size, size, 8, 8);
        g.setColor(active ? DashboardTheme.PURPLE_TINT_BG : Color.WHITE);
        g.fill(shape);
        g.setColor(active ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);

        Color iconCol = active ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_LIGHT;
        g.setColor(iconCol);

        if (isGrid) {
            int d = 4;
            g.fillRect(x + 9, y + 9, d, d);
            g.fillRect(x + 17, y + 9, d, d);
            g.fillRect(x + 9, y + 17, d, d);
            g.fillRect(x + 17, y + 17, d, d);
        } else {
            g.setStroke(new BasicStroke(1.8f));
            g.drawLine(x + 8, y + 11, x + 24, y + 11);
            g.drawLine(x + 8, y + 17, x + 24, y + 17);
            g.drawLine(x + 8, y + 23, x + 24, y + 23);
        }
    }

    private void paintSingleProjectCard(Graphics2D g, int cx, int cy, int cw, int ch, ProjectCard p, StudentDashboard dashboard) {
        p.hitX = cx; p.hitY = cy; p.hitW = cw; p.hitH = ch;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        // Top Row: Icon + Title + Category + Bookmark
        int iconSize = 36;
        int iconX = cx + 18;
        int iconY = cy + 18;
        RoundRectangle2D iconBgShape = new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, 8, 8);
        g.setColor(p.iconBg);
        g.fill(iconBgShape);

        paintVectorIcon(g, p.iconType, iconX + 9, iconY + 9, 18, p.iconColor);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.title, iconX + iconSize + 12, cy + 30);

        // Category · Experience
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(p.category + " · " + p.experience, iconX + iconSize + 12, cy + 46);

        // Bookmark ribbon icon
        p.bmX = cx + cw - 38;
        p.bmY = cy + 20;
        paintBookmarkIcon(g, p.bmX, p.bmY, 16, p.bookmarked);

        // Description
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        FontMetrics dfm = g.getFontMetrics();
        List<String> lines = Draw.wrapText(p.description, dfm, cw - 36);
        int descY = cy + 74;
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            g.drawString(lines.get(i), cx + 18, descY);
            descY += 18;
        }

        // Skills Pills
        int skX = cx + 18;
        int skY = cy + 114;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();

        for (String s : p.skills) {
            int sw = sfm.stringWidth(s) + 14;
            RoundRectangle2D skShape = new RoundRectangle2D.Float(skX, skY, sw, 22, 6, 6);
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fill(skShape);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 7, skY + 15);
            skX += sw + 6;
        }

        // Status row: Green dot + Recruiting + Member count
        int statY = cy + 150;
        if (p.recruiting) {
            g.setColor(new Color(0x10, 0xB9, 0x81));
            g.fillOval(cx + 18, statY - 7, 6, 6);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.drawString("Recruiting", cx + 28, statY);
        }

        String memStr = p.currentMembers + " / " + p.maxMembers + " members";
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        int memW = g.getFontMetrics().stringWidth(memStr);
        g.drawString(memStr, cx + cw - 18 - memW, statY);

        // Open roles
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(p.openRoles, cx + 18, statY + 16);

        // Bottom Creator + View details link
        int botY = cy + 192;
        int crSize = 22;
        g.setColor(p.creatorColor);
        g.fillOval(cx + 18, botY - 14, crSize, crSize);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(Color.WHITE);
        g.drawString(p.creatorInitials, cx + 22, botY);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(p.creatorName, cx + 18 + crSize + 6, botY);

        // "View details →"
        String vd = "View details →";
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        int vdW = g.getFontMetrics().stringWidth(vd);
        p.viewX = cx + cw - 18 - vdW;
        p.viewY = botY - 12;
        p.viewW = vdW;
        p.viewH = 16;
        g.drawString(vd, p.viewX, botY);
    }

    private void paintSingleProjectListCard(Graphics2D g, int cx, int cy, int cw, int ch, ProjectCard p, StudentDashboard dashboard) {
        p.hitX = cx; p.hitY = cy; p.hitW = cw; p.hitH = ch;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        // Icon
        int iconSize = 40;
        int iconX = cx + 18;
        int iconY = cy + (ch - iconSize) / 2;
        RoundRectangle2D iconBg = new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, 8, 8);
        g.setColor(p.iconBg);
        g.fill(iconBg);
        paintVectorIcon(g, p.iconType, iconX + 11, iconY + 11, 18, p.iconColor);

        // Info
        int curX = iconX + iconSize + 16;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.title, curX, cy + 30);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(p.category + " · " + p.experience + " · Created by " + p.creatorName, curX, cy + 48);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String shortDesc = p.description;
        if (shortDesc.length() > 60) shortDesc = shortDesc.substring(0, 57) + "...";
        g.drawString(shortDesc, curX, cy + 72);

        // Skills tags
        int skX = cx + cw - 320;
        int skY = cy + 28;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();
        for (String s : p.skills) {
            int sw = sfm.stringWidth(s) + 12;
            RoundRectangle2D skShape = new RoundRectangle2D.Float(skX, skY, sw, 22, 6, 6);
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fill(skShape);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 6, skY + 15);
            skX += sw + 6;
        }

        // Bookmark
        p.bmX = cx + cw - 120;
        p.bmY = cy + 30;
        paintBookmarkIcon(g, p.bmX, p.bmY, 16, p.bookmarked);

        // View details
        String vd = "View details →";
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        int vdW = g.getFontMetrics().stringWidth(vd);
        p.viewX = cx + cw - 18 - vdW;
        p.viewY = cy + (ch / 2) - 8;
        p.viewW = vdW;
        p.viewH = 20;
        g.drawString(vd, p.viewX, cy + (ch / 2) + 5);
    }

    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        String msg = "No projects found matching your criteria";
        int mw = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, x + (w - mw) / 2, y + 110);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String sub = "Try adjusting your search terms or clearing some filters to explore more projects.";
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, x + (w - sw) / 2, y + 140);
    }

    private void paintPagination(Graphics2D g, int x, int y, int w, int totalItems) {
        pageBoxes.clear();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        int startItem = totalItems == 0 ? 0 : (currentPage - 1) * itemsPerPage + 1;
        int endItem = Math.min(currentPage * itemsPerPage, totalItems);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Showing " + startItem + "-" + endItem + " of " + totalItems + " recruiting projects", x, y + 21);

        // Pagination buttons right aligned
        int curX = x + w - nextBtnW;
        nextBtnX = curX; nextBtnY = y;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next →", false, currentPage >= totalPages);

        curX -= 40;
        for (int p = totalPages; p >= 1; p--) {
            int pageBtnX = curX;
            int pageBtnW = 32;
            paintPageBtn(g, pageBtnX, y, pageBtnW, nextBtnH, String.valueOf(p), p == currentPage, false);
            pageBoxes.add(new PillBox(String.valueOf(p), pageBtnX, y, pageBtnW, nextBtnH));
            curX -= 38;
        }

        prevBtnX = curX - prevBtnW + 30;
        prevBtnY = y;
        paintPageBtn(g, prevBtnX, prevBtnY, prevBtnW, prevBtnH, "← Previous", false, currentPage <= 1);
    }

    private void paintPageBtn(Graphics2D g, int x, int y, int w, int h, String label, boolean active, boolean disabled) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        if (active) {
            g.setColor(DashboardTheme.PURPLE_TINT_BG);
            g.fill(shape);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.draw(shape);
            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(DashboardTheme.BRAND_PURPLE);
        } else {
            g.setColor(Color.WHITE);
            g.fill(shape);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(shape);
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(disabled ? new Color(0xCB, 0xD5, 0xE1) : DashboardTheme.TEXT_DARK);
        }
        FontMetrics pfm = g.getFontMetrics();
        int lw = pfm.stringWidth(label);
        g.drawString(label, x + (w - lw) / 2, y + 20);
    }

    private void paintVectorIcon(Graphics2D g, int type, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case 0: // Calendar
                g2.drawRoundRect(x, y + 2, size, size - 2, 4, 4);
                g2.drawLine(x, y + 7, x + size, y + 7);
                g2.drawLine(x + 4, y, x + 4, y + 3);
                g2.drawLine(x + size - 4, y, x + size - 4, y + 3);
                break;
            case 1: // Leaf
                Path2D leaf = new Path2D.Float();
                leaf.moveTo(x, y + size);
                leaf.quadTo(x + size, y + size, x + size, y);
                leaf.quadTo(x, y, x, y + size);
                g2.draw(leaf);
                g2.drawLine(x, y + size, x + size - 4, y + 4);
                break;
            case 2: // Flask
                Path2D flask = new Path2D.Float();
                flask.moveTo(x + 5, y);
                flask.lineTo(x + size - 5, y);
                flask.lineTo(x + size - 5, y + 5);
                flask.lineTo(x + size, y + size);
                flask.lineTo(x, y + size);
                flask.lineTo(x + 5, y + 5);
                flask.closePath();
                g2.draw(flask);
                break;
            case 3: // MapPin
                g2.drawOval(x + 2, y, size - 4, size - 6);
                g2.drawLine(x + size / 2, y + size - 6, x + size / 2, y + size);
                break;
            case 4: // Book
                g2.drawRoundRect(x + 1, y + 2, size - 2, size - 3, 3, 3);
                g2.drawLine(x + 7, y + 2, x + 7, y + size - 1);
                break;
            case 5: // Food/Fork
                g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                g2.drawLine(x + 6, y + 6, x + size - 6, y + size - 6);
                break;
            default:
                g2.drawRect(x, y, size, size);
                break;
        }
        g2.dispose();
    }

    private void paintBookmarkIcon(Graphics2D g, int x, int y, int size, boolean filled) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D p = new Path2D.Float();
        p.moveTo(x, y);
        p.lineTo(x + size, y);
        p.lineTo(x + size, y + size + 4);
        p.lineTo(x + size / 2, y + size);
        p.lineTo(x, y + size + 4);
        p.closePath();

        if (filled) {
            g2.setColor(DashboardTheme.BRAND_PURPLE);
            g2.fill(p);
        } else {
            g2.setColor(DashboardTheme.TEXT_LIGHT);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(p);
        }
        g2.dispose();
    }

    // =========================================================================
    // DROPDOWN OVERLAYS (Highest Z-Index)
    // =========================================================================

    public void paintDropdownOverlay(Graphics2D g) {
        if (activeDropdown == DropdownType.NONE) return;

        switch (activeDropdown) {
            case CATEGORY:
                overlayItems = new String[]{"All projects", "Campus life", "Sustainability", "Research", "Social impact", "Education"};
                paintGenericMenu(g, catDropX, catDropY + catDropH + 4, 160, overlayItems, filterCategory);
                break;
            case SKILLS:
                overlayItems = new String[]{"All", "React", "TypeScript", "Node.js", "Python", "PostgreSQL", "Figma", "Maps API"};
                paintGenericMenu(g, skillDropX, skillDropY + skillDropH + 4, 140, overlayItems, filterSkill);
                break;
            case EXPERIENCE:
                overlayItems = new String[]{"All", "Beginner", "Intermediate", "Advanced"};
                paintGenericMenu(g, expDropX, expDropY + expDropH + 4, 140, overlayItems, filterExperience);
                break;
            case RECRUITING:
                overlayItems = new String[]{"All", "Recruiting Only", "Full Teams"};
                paintGenericMenu(g, recDropX, recDropY + recDropH + 4, 150, overlayItems, filterRecruiting);
                break;
            case SORT:
                overlayItems = new String[]{"Recommended", "Most Recent", "Most Members", "Alphabetical"};
                paintGenericMenu(g, sortDropX, sortDropY + sortDropH + 4, 160, overlayItems, selectedSort);
                break;
            default:
                break;
        }
    }

    private void paintGenericMenu(Graphics2D g, int x, int y, int w, String[] items, String selected) {
        int itemH = 34;
        int totalH = items.length * itemH + 12;
        overlayX = x; overlayY = y; overlayW = w; overlayH = totalH;

        RoundRectangle2D menuBg = new RoundRectangle2D.Float(x, y, w, totalH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(menuBg);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(menuBg);

        int curY = y + 6;
        for (String item : items) {
            boolean isSel = item.equalsIgnoreCase(selected);
            if (isSel) {
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fillRect(x + 4, curY, w - 8, itemH);
            }
            g.setFont(isSel ? DashboardTheme.FONT_BOLD_12 : DashboardTheme.FONT_REG_12);
            g.setColor(isSel ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_DARK);
            g.drawString(item, x + 16, curY + 22);
            curY += itemH;
        }
    }

    // =========================================================================
    // EVENT HANDLING
    // =========================================================================

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // 1. Dropdown Overlay click takes top priority
        if (activeDropdown != DropdownType.NONE) {
            if (mx >= overlayX && mx <= overlayX + overlayW && my >= overlayY && my <= overlayY + overlayH) {
                int itemH = 34;
                int clickedIdx = (my - overlayY - 6) / itemH;
                if (clickedIdx >= 0 && clickedIdx < overlayItems.length) {
                    String chosen = overlayItems[clickedIdx];
                    if (activeDropdown == DropdownType.CATEGORY) {
                        filterCategory = chosen;
                    } else if (activeDropdown == DropdownType.SKILLS) {
                        filterSkill = chosen;
                    } else if (activeDropdown == DropdownType.EXPERIENCE) {
                        filterExperience = chosen;
                    } else if (activeDropdown == DropdownType.RECRUITING) {
                        filterRecruiting = chosen;
                    } else if (activeDropdown == DropdownType.SORT) {
                        selectedSort = chosen;
                    }
                    applyFilters();
                    dashboard.showToast("Filter applied: " + chosen);
                }
            }
            activeDropdown = DropdownType.NONE;
            return true;
        }

        // 2. Primary "+ Create project" Button
        if (mx >= createBtnX && mx <= createBtnX + createBtnW && my >= createBtnY && my <= createBtnY + createBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.CREATE_PROJECT);
            return true;
        }

        // 3. Search Box Focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 4. Dropdown Toggle Buttons
        if (mx >= catDropX && mx <= catDropX + catDropW && my >= catDropY && my <= catDropY + catDropH) {
            activeDropdown = (activeDropdown == DropdownType.CATEGORY) ? DropdownType.NONE : DropdownType.CATEGORY;
            return true;
        }
        if (mx >= skillDropX && mx <= skillDropX + skillDropW && my >= skillDropY && my <= skillDropY + skillDropH) {
            activeDropdown = (activeDropdown == DropdownType.SKILLS) ? DropdownType.NONE : DropdownType.SKILLS;
            return true;
        }
        if (mx >= expDropX && mx <= expDropX + expDropW && my >= expDropY && my <= expDropY + expDropH) {
            activeDropdown = (activeDropdown == DropdownType.EXPERIENCE) ? DropdownType.NONE : DropdownType.EXPERIENCE;
            return true;
        }
        if (mx >= recDropX && mx <= recDropX + recDropW && my >= recDropY && my <= recDropY + recDropH) {
            activeDropdown = (activeDropdown == DropdownType.RECRUITING) ? DropdownType.NONE : DropdownType.RECRUITING;
            return true;
        }
        if (mx >= sortDropX && mx <= sortDropX + sortDropW && my >= sortDropY && my <= sortDropY + sortDropH) {
            activeDropdown = (activeDropdown == DropdownType.SORT) ? DropdownType.NONE : DropdownType.SORT;
            return true;
        }

        // 5. Category Pills Click
        for (PillBox pb : categoryPills) {
            if (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h) {
                filterCategory = pb.label;
                applyFilters();
                dashboard.showToast("Category: " + pb.label);
                return true;
            }
        }

        // 6. Grid / List View Toggle
        if (mx >= gridBtnX && mx <= gridBtnX + toggleBtnSize && my >= gridBtnY && my <= gridBtnY + toggleBtnSize) {
            isGridView = true;
            return true;
        }
        if (mx >= listBtnX && mx <= listBtnX + toggleBtnSize && my >= listBtnY && my <= listBtnY + toggleBtnSize) {
            isGridView = false;
            return true;
        }

        // 7. Project Card Actions: Bookmarks & View Details
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredProjects.size());

        for (int i = startIndex; i < endIndex; i++) {
            ProjectCard p = filteredProjects.get(i);

            // Bookmark icon
            if (mx >= p.bmX - 6 && mx <= p.bmX + p.bmSize + 6 && my >= p.bmY - 6 && my <= p.bmY + p.bmSize + 6) {
                p.bookmarked = !p.bookmarked;
                dashboard.showToast(p.bookmarked ? p.title + " added to Bookmarks ★" : p.title + " removed from Bookmarks");
                return true;
            }

            // View Details or Card Body
            if (mx >= p.viewX - 4 && mx <= p.viewX + p.viewW + 4 && my >= p.viewY - 4 && my <= p.viewY + p.viewH + 4) {
                StudentDashboard.ProjectItem pi = new StudentDashboard.ProjectItem(
                        p.title, p.creatorName + " · " + p.category, p.description,
                        p.skills.toArray(new String[0]), p.openRoles + " · " + p.currentMembers + "/" + p.maxMembers + " members",
                        p.recruiting, p.iconType
                );
                pi.bookmarked = p.bookmarked;
                dashboard.setSelectedProject(pi);
                dashboard.openModal(StudentDashboard.ActiveModal.PROJECT_DETAILS);
                return true;
            }

            if (mx >= p.hitX && mx <= p.hitX + p.hitW && my >= p.hitY && my <= p.hitY + p.hitH) {
                StudentDashboard.ProjectItem pi = new StudentDashboard.ProjectItem(
                        p.title, p.creatorName + " · " + p.category, p.description,
                        p.skills.toArray(new String[0]), p.openRoles + " · " + p.currentMembers + "/" + p.maxMembers + " members",
                        p.recruiting, p.iconType
                );
                pi.bookmarked = p.bookmarked;
                dashboard.setSelectedProject(pi);
                dashboard.openModal(StudentDashboard.ActiveModal.PROJECT_DETAILS);
                return true;
            }
        }

        // 8. Pagination Clicks
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredProjects.size() / itemsPerPage));
        if (mx >= prevBtnX && mx <= prevBtnX + prevBtnW && my >= prevBtnY && my <= prevBtnY + prevBtnH) {
            if (currentPage > 1) {
                currentPage--;
                dashboard.showToast("Page " + currentPage);
            }
            return true;
        }
        if (mx >= nextBtnX && mx <= nextBtnX + nextBtnW && my >= nextBtnY && my <= nextBtnY + nextBtnH) {
            if (currentPage < totalPages) {
                currentPage++;
                dashboard.showToast("Page " + currentPage);
            }
            return true;
        }
        for (PillBox pb : pageBoxes) {
            if (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h) {
                currentPage = Integer.parseInt(pb.label);
                dashboard.showToast("Page " + currentPage);
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        boolean prevHov = createBtnHovered;
        createBtnHovered = (mx >= createBtnX && mx <= createBtnX + createBtnW && my >= createBtnY && my <= createBtnY + createBtnH);
        return createBtnHovered != prevHov;
    }

    public boolean onKey(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            if (activeDropdown != DropdownType.NONE) {
                activeDropdown = DropdownType.NONE;
                return true;
            }
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
        }

        if (searchFocused) {
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (searchKeyword.length() > 0) {
                    searchKeyword = searchKeyword.substring(0, searchKeyword.length() - 1);
                    applyFilters();
                }
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && searchKeyword.length() < 40) {
                searchKeyword += c;
                applyFilters();
                return true;
            }
            return true;
        }

        return false;
    }

    public void setSearchKeyword(String kw) {
        this.searchKeyword = kw;
        applyFilters();
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }
}

