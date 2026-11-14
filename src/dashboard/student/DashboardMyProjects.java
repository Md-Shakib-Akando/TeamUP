import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the "My Projects" page for TeamUp Student Dashboard.
 * Displays user's active, lead, and joined collaborative projects.
 * Fully interactive with live search, tabs (All, Created by me, Joined, Archived),
 * status & role filter dropdowns, team management modal, task board modal,
 * progress bars, member chips, and quick actions.
 * Matches project color palette and design language with pixel-perfect precision.
 */
public class DashboardMyProjects {

    public static class MemberAvatar {
        public String initials;
        public String name;
        public String role;
        public Color bg;
        public Color text;

        public MemberAvatar(String initials, String name, String role, Color bg, Color text) {
            this.initials = initials;
            this.name = name;
            this.role = role;
            this.bg = bg;
            this.text = text;
        }
    }

    public static class ProjectTask {
        public volatile String remoteId; // set from the IO thread once the backend creates the task
        public String title;
        public String dueDate;
        public boolean done;
        public int hitX, hitY, hitW, hitH;

        public ProjectTask(String title, String dueDate, boolean done) {
            this.title = title;
            this.dueDate = dueDate;
            this.done = done;
        }
    }

    public static class Candidate {
        public String name;
        public String role;
        public String initials;
        public Color bg;
        public Color text;
        public String matchPct;
        public String remoteId; // JoinRequest id when loaded from the backend
        public boolean accepted = false;
        public boolean declined = false;
        public int acceptX, acceptY, acceptW = 74, acceptH = 28;
        public int declineX, declineY, declineW = 74, declineH = 28;

        public Candidate(String name, String role, String initials, Color bg, Color text, String matchPct) {
            this.name = name;
            this.role = role;
            this.initials = initials;
            this.bg = bg;
            this.text = text;
            this.matchPct = matchPct;
        }
    }

    public static class MyProjectCard {
        public int id;
        public String remoteId; // MongoDB project id when loaded from the backend
        public String title;
        public String category;
        public String description;
        public String myRole;
        public boolean isLead; // true = created by me, false = joined
        public String status; // "Recruiting", "Active", "Completed"
        public Color statusBg;
        public Color statusText;
        public int progressPct;
        public List<String> skills = new ArrayList<>();
        public List<MemberAvatar> members = new ArrayList<>();
        public String nextMilestone;
        public int totalCapacity;
        public boolean isArchived = false;

        public List<ProjectTask> tasks = new ArrayList<>();
        public List<Candidate> candidates = new ArrayList<>();

        // Hitboxes per frame
        public int hitX, hitY, hitW, hitH;
        public int manageBtnX, manageBtnY, manageBtnW, manageBtnH;
        public int boardBtnX, boardBtnY, boardBtnW, boardBtnH;
        public int editBtnX, editBtnY, editBtnW, editBtnH;

        public MyProjectCard(int id, String title, String category, String description,
                             String myRole, boolean isLead, String status, Color statusBg, Color statusText,
                             int progressPct, String[] skillArr, String nextMilestone, int capacity) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.description = description;
            this.myRole = myRole;
            this.isLead = isLead;
            this.status = status;
            this.statusBg = statusBg;
            this.statusText = statusText;
            this.progressPct = progressPct;
            Collections.addAll(this.skills, skillArr);
            this.nextMilestone = nextMilestone;
            this.totalCapacity = capacity;
        }
    }

    private final List<MyProjectCard> allProjects = new ArrayList<>();
    private final List<MyProjectCard> filteredProjects = new ArrayList<>();

    // State & Filters
    private int selectedTab = 0; // 0=All, 1=Created by me, 2=Joined, 3=Archived
    private String searchKeyword = "";
    private boolean searchFocused = false;
    private String filterStatus = "All statuses";
    private String filterRole = "All roles";
    private String selectedSort = "Recently updated";

    private int totalContentHeight = 1200;

    // Dropdowns
    public enum DropdownType { NONE, STATUS, ROLE, SORT }
    private DropdownType activeDropdown = DropdownType.NONE;

    // Layout Hitboxes
    private int createBtnX, createBtnY, createBtnW = 142, createBtnH = 38;
    private boolean createBtnHovered = false;

    private static class TabHitbox {
        String label;
        int count;
        int x, y, w, h;
        TabHitbox(String label, int count, int x, int y, int w, int h) {
            this.label = label; this.count = count; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
    private final List<TabHitbox> tabs = new ArrayList<>();

    private int searchX, searchY, searchW, searchH = 38;
    private int statusDropX, statusDropY, statusDropW = 135, statusDropH = 38;
    private int roleDropX, roleDropY, roleDropW = 120, roleDropH = 38;
    private int sortDropX, sortDropY, sortDropW = 175, sortDropH = 38;

    // Dropdown Overlay bounds
    private int overlayX, overlayY, overlayW, overlayH;
    private String[] overlayItems = new String[0];

    // Sub-modal states
    public enum SubModal { NONE, MANAGE_TEAM, PROJECT_BOARD }
    private SubModal activeSubModal = SubModal.NONE;
    private MyProjectCard modalProject = null;
    private int subModalCloseX, subModalCloseY, subModalCloseSize = 28;
    private int newTaskInputX, newTaskInputY, newTaskInputW = 320, newTaskInputH = 36;
    private int addTaskBtnX, addTaskBtnY, addTaskBtnW = 95, addTaskBtnH = 36;
    private String newTaskTitle = "";
    private boolean newTaskFocused = false;

    public DashboardMyProjects() {
        initMyProjectsData();
        applyFilters();
    }

    /** Replaces the project list with data loaded from the backend, keeping tab & filters. */
    public void setProjects(List<MyProjectCard> projects) {
        allProjects.clear();
        allProjects.addAll(projects);
        applyFilters();
    }

    private void initMyProjectsData() {
        // Project 1: CampusConnect (Lead by Alex)
        MyProjectCard p1 = new MyProjectCard(
                1, "CampusConnect", "Campus life",
                "Centralized campus social hub discovering events, study hubs, and peer workshops in real-time.",
                "Project Lead & Full-Stack Developer", true, "Recruiting",
                new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09),
                65, new String[]{"React", "TypeScript", "Node.js", "Tailwind"},
                "Finalize event API contract (Due tomorrow)", 7
        );
        p1.members.add(new MemberAvatar("AM", "Alex Morgan", "Project Lead", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9)));
        p1.members.add(new MemberAvatar("PS", "Priya Shah", "UX Designer", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9)));
        p1.members.add(new MemberAvatar("DK", "Daniel Kim", "Backend Dev", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)));
        p1.members.add(new MemberAvatar("SM", "Sofia Martinez", "Frontend Dev", new Color(0xFF, 0xE4, 0xE6), new Color(0xBE, 0x12, 0x3C)));

        p1.tasks.add(new ProjectTask("Finalize event API contract with backend team", "Oct 17", false));
        p1.tasks.add(new ProjectTask("Build event discovery cards and filter chips", "Oct 18", false));
        p1.tasks.add(new ProjectTask("Accessibility and screen reader audit", "Oct 21", false));
        p1.tasks.add(new ProjectTask("Set up PostgreSQL Prisma migration", "Oct 12", true));
        p1.tasks.add(new ProjectTask("Design high-fidelity Figma mobile mockups", "Oct 10", true));

        p1.candidates.add(new Candidate("Ethan Chen", "QA Engineer", "EC", new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09), "88%"));
        p1.candidates.add(new Candidate("Aisha Rahman", "Product Designer", "AR", new Color(0xE0, 0xE7, 0xFF), new Color(0x37, 0x30, 0xA3), "79%"));

        allProjects.add(p1);

        // Project 2: GreenRoute (Joined by Alex, Lead: Maya Patel)
        MyProjectCard p2 = new MyProjectCard(
                2, "GreenRoute", "Sustainability",
                "Make low-carbon campus travel easier with shared routes, micro-mobility, and real-time transit insights.",
                "Frontend & Maps Developer", false, "Active",
                new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                80, new String[]{"React", "Maps API", "Node.js", "Express"},
                "Build event discovery cards (Today)", 6
        );
        p2.members.add(new MemberAvatar("MP", "Maya Patel", "Project Lead", new Color(0xEE, 0xF2, 0xFF), new Color(0x43, 0x38, 0xCA)));
        p2.members.add(new MemberAvatar("AM", "Alex Morgan", "Frontend Dev", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9)));
        p2.members.add(new MemberAvatar("NW", "Noah Williams", "Data Pipeline", new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)));
        p2.members.add(new MemberAvatar("CB", "Chloe Bennett", "UX Researcher", new Color(0xCF, 0xFA, 0xFE), new Color(0x0E, 0x74, 0x90)));

        p2.tasks.add(new ProjectTask("Integrate campus shuttle GPS tracking layer", "Oct 19", false));
        p2.tasks.add(new ProjectTask("Implement carbon reduction gamification badges", "Oct 24", false));
        p2.tasks.add(new ProjectTask("Route optimization algorithms in Mapbox", "Oct 14", true));
        p2.tasks.add(new ProjectTask("User testing session with 12 UW commuters", "Oct 08", true));

        allProjects.add(p2);

        // Project 3: AI PeerPrep (Lead by Alex)
        MyProjectCard p3 = new MyProjectCard(
                3, "AI PeerPrep", "Education",
                "Turn interview practice into a shared learning experience with peer feedback, mock interviews, and AI scoring.",
                "Project Lead & AI Engineer", true, "Active",
                new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                45, new String[]{"Next.js", "Prisma", "FastAPI", "Python"},
                "Integrate audio transcription & feedback engine (Oct 22)", 4
        );
        p3.members.add(new MemberAvatar("AM", "Alex Morgan", "Project Lead", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9)));
        p3.members.add(new MemberAvatar("OB", "Oliver Brooks", "Backend Dev", new Color(0xDC, 0xF8, 0xC6), new Color(0x16, 0x65, 0x34)));
        p3.members.add(new MemberAvatar("FZ", "Fatima Zahra", "Frontend Dev", new Color(0xF3, 0xE8, 0xFF), new Color(0x6B, 0x21, 0xA8)));

        p3.tasks.add(new ProjectTask("Set up Whisper speech-to-text API microservice", "Oct 20", false));
        p3.tasks.add(new ProjectTask("Build real-time peer video room using WebRTC", "Oct 25", false));
        p3.tasks.add(new ProjectTask("Create rubrics for behavioral question evaluations", "Oct 15", true));

        p3.candidates.add(new Candidate("Elena Rostova", "AI Research", "ER", new Color(0xFC, 0xE7, 0xF3), new Color(0x9D, 0x17, 0x4D), "94%"));

        allProjects.add(p3);

        // Project 4: SolarCampus IoT (Archived / Completed)
        MyProjectCard p4 = new MyProjectCard(
                4, "SolarCampus IoT", "Sustainability",
                "IoT sensor monitoring of rooftop solar panels across engineering facilities with automated yield forecasting.",
                "Data Pipeline Engineer", false, "Completed",
                new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8),
                100, new String[]{"Python", "PostgreSQL", "Grafana", "C++"},
                "Project completed & deployed to College of Engineering", 5
        );
        p4.isArchived = true;
        p4.members.add(new MemberAvatar("DK", "Daniel Kim", "Lead", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)));
        p4.members.add(new MemberAvatar("AM", "Alex Morgan", "Data Engineer", new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9)));
        p4.members.add(new MemberAvatar("LV", "Lucas Vance", "Embedded", new Color(0xDB, 0xEA, 0xFE), new Color(0x1E, 0x40, 0xAF)));

        p4.tasks.add(new ProjectTask("Publish capstone project report and open-source repo", "Sep 30", true));
        p4.tasks.add(new ProjectTask("Deploy 16 wireless sensor nodes across Wilcox Hall", "Sep 20", true));

        allProjects.add(p4);
    }

    public void applyFilters() {
        filteredProjects.clear();
        String query = searchKeyword.trim().toLowerCase();

        for (MyProjectCard p : allProjects) {
            // Tab filtering
            if (selectedTab == 1 && !p.isLead) continue;
            if (selectedTab == 2 && p.isLead) continue;
            if (selectedTab == 3 && !p.isArchived) continue;
            if (selectedTab != 3 && p.isArchived) continue; // Non-archived tabs hide archived

            // Status filter
            if (!filterStatus.equals("All statuses") && !p.status.equalsIgnoreCase(filterStatus)) {
                continue;
            }

            // Role filter
            if (!filterRole.equals("All roles")) {
                if (filterRole.equals("Project Lead") && !p.isLead) continue;
                if (filterRole.equals("Team Member") && p.isLead) continue;
            }

            // Search query
            if (!query.isEmpty()) {
                boolean matchTitle = p.title.toLowerCase().contains(query);
                boolean matchDesc = p.description.toLowerCase().contains(query);
                boolean matchRole = p.myRole.toLowerCase().contains(query);
                boolean matchSkill = false;
                for (String s : p.skills) {
                    if (s.toLowerCase().contains(query)) { matchSkill = true; break; }
                }
                if (!matchTitle && !matchDesc && !matchRole && !matchSkill) {
                    continue;
                }
            }

            filteredProjects.add(p);
        }

        // Sorting
        if ("Progress: High to Low".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(b.progressPct, a.progressPct));
        } else if ("Most members".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(b.members.size(), a.members.size()));
        } else if ("Alphabetical".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
        }
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

        // 1. Heading & Subtitle + "+ New Project" button
        g.setFont(DashboardTheme.FONT_HEADING);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("My Projects", startX + padX, curY + 24);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Manage your active builds, track recruitment status, and collaborate with your teams.", startX + padX, curY + 46);

        // "+ New Project" Button
        createBtnW = 142;
        createBtnH = 38;
        createBtnX = startX + padX + contentW - createBtnW;
        createBtnY = curY;
        RoundRectangle2D primBtn = new RoundRectangle2D.Float(createBtnX, createBtnY, createBtnW, createBtnH, 10, 10);
        g.setColor(createBtnHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(primBtn);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        g.drawString("+  New Project", createBtnX + 18, createBtnY + 24);

        curY += 68;

        // 2. Metrics Strip (4 stat summary cards)
        int statGap = 16;
        int statW = (contentW - statGap * 3) / 4;
        int statH = 88;

        paintStatCard(g, startX + padX, curY, statW, statH, "Total Projects", "3", "2 created by you · 1 joined", 0);
        paintStatCard(g, startX + padX + (statW + statGap), curY, statW, statH, "Active Tasks", "8", "5 in progress · 3 done", 1);
        paintStatCard(g, startX + padX + (statW + statGap) * 2, curY, statW, statH, "Pending Review", "3", "Candidates awaiting review", 2);
        paintStatCard(g, startX + padX + (statW + statGap) * 3, curY, statW, statH, "Team Capacity", "78%", "11 of 14 member slots filled", 3);

        curY += statH + 24;

        // 3. Tab Navigation Bar
        tabs.clear();
        String[] tabNames = {"All projects", "Created by me", "Joined", "Archived"};
        int[] tabCounts = {3, 2, 1, 1};
        int tabX = startX + padX;
        int tabH = 36;

        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics tfm = g.getFontMetrics();

        for (int i = 0; i < tabNames.length; i++) {
            String tName = tabNames[i] + " (" + tabCounts[i] + ")";
            int strW = tfm.stringWidth(tName);
            int tabW = strW + 28;
            boolean isSel = (i == selectedTab);

            RoundRectangle2D tShape = new RoundRectangle2D.Float(tabX, curY, tabW, tabH, 8, 8);
            if (isSel) {
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(tShape);
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.setFont(DashboardTheme.FONT_BOLD_13);
            } else {
                g.setColor(Color.WHITE);
                g.fill(tShape);
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(tShape);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.setFont(DashboardTheme.FONT_REG_13);
            }
            g.drawString(tName, tabX + 14, curY + 23);
            tabs.add(new TabHitbox(tabNames[i], tabCounts[i], tabX, curY, tabW, tabH));
            tabX += tabW + 8;
        }

        curY += tabH + 16;

        // 4. Search & Filters Bar
        searchW = Math.max(260, contentW - 460);
        searchH = 38;
        searchX = startX + padX;
        searchY = curY;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.4f : 1.0f));
        g.draw(sBox);

        // Magnifier
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 12, 10, 10);
        g.drawLine(searchX + 22, searchY + 20, searchX + 26, searchY + 24);

        g.setFont(DashboardTheme.FONT_REG_13);
        if (searchKeyword.isEmpty() && !searchFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Search my projects by title, role or tech stack...", searchX + 34, searchY + 24);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            String disp = searchKeyword + (searchFocused && (System.currentTimeMillis() % 1000 < 500) ? "|" : "");
            g.drawString(disp, searchX + 34, searchY + 24);
        }

        // Dropdowns on right
        int dropX = searchX + searchW + 12;
        statusDropX = dropX; statusDropY = curY; statusDropW = 135;
        paintFilterDropBtn(g, statusDropX, statusDropY, statusDropW, searchH, filterStatus, activeDropdown == DropdownType.STATUS);
        dropX += statusDropW + 8;

        roleDropX = dropX; roleDropY = curY; roleDropW = 120;
        paintFilterDropBtn(g, roleDropX, roleDropY, roleDropW, searchH, filterRole, activeDropdown == DropdownType.ROLE);
        dropX += roleDropW + 8;

        sortDropX = dropX; sortDropY = curY; sortDropW = 175;
        paintFilterDropBtn(g, sortDropX, sortDropY, sortDropW, searchH, "Sort: " + selectedSort, activeDropdown == DropdownType.SORT);

        curY += searchH + 20;

        // 5. Project Cards List
        if (filteredProjects.isEmpty()) {
            paintEmptyState(g, startX + padX, curY, contentW, 260);
            curY += 280;
        } else {
            int cardGap = 18;
            for (MyProjectCard card : filteredProjects) {
                int cardH = paintProjectCard(g, startX + padX, curY, contentW, card);
                curY += cardH + cardGap;
            }
        }

        totalContentHeight = curY - startY + 40;
    }

    private void paintStatCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub, int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);

        // Icon badge on top right
        int badgeSize = 28;
        int bx = x + w - badgeSize - 16;
        int by = y + 14;
        RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, badgeSize, badgeSize, 6, 6);

        Color badgeBg = DashboardTheme.PURPLE_TINT_BG;
        Color iconCol = DashboardTheme.BRAND_PURPLE;
        if (type == 1) { badgeBg = new Color(0xDC, 0xFC, 0xE7); iconCol = new Color(0x15, 0x80, 0x3D); }
        if (type == 2) { badgeBg = new Color(0xFE, 0xF3, 0xC7); iconCol = new Color(0xB4, 0x53, 0x09); }
        if (type == 3) { badgeBg = new Color(0xCF, 0xFA, 0xFE); iconCol = new Color(0x0E, 0x74, 0x90); }

        g.setColor(badgeBg);
        g.fill(bShape);
        g.setColor(iconCol);
        paintSmallStatIcon(g, type, bx + 7, by + 7);

        // Title
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(title, x + 16, y + 26);

        // Big Metric
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(val, x + 16, y + 54);

        // Subtitle
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(sub, x + 16, y + 74);
    }

    private void paintSmallStatIcon(Graphics2D g, int type, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (type == 0) {
            // Folder
            g2.drawRoundRect(x, y + 2, 14, 10, 2, 2);
            g2.drawLine(x, y + 5, x + 14, y + 5);
        } else if (type == 1) {
            // Checkmark
            g2.drawLine(x + 2, y + 7, x + 6, y + 11);
            g2.drawLine(x + 6, y + 11, x + 13, y + 3);
        } else if (type == 2) {
            // User alert
            g2.drawOval(x + 3, y, 7, 7);
            g2.drawArc(x, y + 8, 13, 6, 0, 180);
        } else {
            // Chart
            g2.drawLine(x + 2, y + 12, x + 2, y + 6);
            g2.drawLine(x + 7, y + 12, x + 7, y + 2);
            g2.drawLine(x + 12, y + 12, x + 12, y + 8);
        }
        g2.dispose();
    }

    private void paintFilterDropBtn(Graphics2D g, int x, int y, int w, int h, String label, boolean open) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(open ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(open ? 1.5f : 1.0f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(open ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
        String text = label;
        if (text.length() > 18) text = text.substring(0, 16) + "..";
        g.drawString(text, x + 12, y + (h / 2) + 5);

        Draw.drawChevronDown(g, x + w - 16, y + (h / 2) - 2, 8, DashboardTheme.TEXT_MUTED, open ? 1.0f : 0.0f);
    }

    private int paintProjectCard(Graphics2D g, int cx, int cy, int cw, MyProjectCard p) {
        int cardH = 205;
        p.hitX = cx; p.hitY = cy; p.hitW = cw; p.hitH = cardH;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        // Header: Title + Category pill + Status Badge
        int curX = cx + 20;
        int curY = cy + 28;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.title, curX, curY);
        int tw = g.getFontMetrics().stringWidth(p.title);
        curX += tw + 12;

        // Role Tag
        RoundRectangle2D roleTag = new RoundRectangle2D.Float(curX, curY - 14, p.isLead ? 88 : 68, 20, 6, 6);
        g.setColor(p.isLead ? DashboardTheme.PURPLE_TINT_BG : new Color(0xF4, 0xF2, 0xEE));
        g.fill(roleTag);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(p.isLead ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
        g.drawString(p.isLead ? "PROJECT LEAD" : "JOINED", curX + 8, curY);
        curX += (p.isLead ? 88 : 68) + 8;

        // Category Tag
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("·  " + p.category, curX, curY);

        // Status Badge (Right aligned)
        int badgeW = 90;
        int badgeH = 24;
        int badgeX = cx + cw - badgeW - 20;
        int badgeY = cy + 14;
        RoundRectangle2D stBadge = new RoundRectangle2D.Float(badgeX, badgeY, badgeW, badgeH, 6, 6);
        g.setColor(p.statusBg);
        g.fill(stBadge);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(p.statusText);
        int sbw = g.getFontMetrics().stringWidth(p.status);
        g.drawString(p.status, badgeX + (badgeW - sbw) / 2, badgeY + 16);

        // Description
        curY += 22;
        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String desc = p.description;
        if (desc.length() > 95) desc = desc.substring(0, 92) + "...";
        g.drawString(desc, cx + 20, curY);

        // Progress Bar & Percentage
        curY += 26;
        int progBarW = 280;
        int progBarH = 8;
        RoundRectangle2D pTrack = new RoundRectangle2D.Float(cx + 20, curY, progBarW, progBarH, 4, 4);
        g.setColor(new Color(0xEE, 0xF2, 0xF6));
        g.fill(pTrack);

        int fillW = Math.round((progBarW * p.progressPct) / 100f);
        RoundRectangle2D pFill = new RoundRectangle2D.Float(cx + 20, curY, fillW, progBarH, 4, 4);
        g.setColor(p.progressPct >= 100 ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.BRAND_PURPLE);
        g.fill(pFill);

        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.progressPct + "% complete", cx + 20 + progBarW + 12, curY + 8);

        // Next Milestone info
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Next: " + p.nextMilestone, cx + 20 + progBarW + 110, curY + 8);

        // Skills Pills
        curY += 24;
        int skX = cx + 20;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();
        for (String s : p.skills) {
            int sw = sfm.stringWidth(s) + 14;
            RoundRectangle2D sk = new RoundRectangle2D.Float(skX, curY, sw, 22, 6, 6);
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fill(sk);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(sk);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 7, curY + 15);
            skX += sw + 6;
        }

        // Divider line
        curY += 34;
        g.setColor(new Color(0xEE, 0xEB, 0xE5));
        g.drawLine(cx + 20, curY, cx + cw - 20, curY);

        // Bottom Footer: Team Members Avatars + Action Buttons
        curY += 14;
        int avX = cx + 20;
        int avSize = 28;
        for (MemberAvatar m : p.members) {
            g.setColor(m.bg);
            g.fillOval(avX, curY, avSize, avSize);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(m.text);
            g.drawString(m.initials, avX + 6, curY + 18);
            avX += avSize - 6; // overlapping effect
        }

        // Open slots indicator
        int openSlots = p.totalCapacity - p.members.size();
        if (openSlots > 0 && !p.isArchived) {
            RoundRectangle2D slotPill = new RoundRectangle2D.Float(avX + 12, curY + 3, 86, 22, 6, 6);
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(slotPill);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.drawString("+" + openSlots + " open roles", avX + 18, curY + 18);
        }

        // Action Buttons on Right
        int btnRowX = cx + cw - 20;
        int btnH = 32;

        if (p.isLead && !p.isArchived) {
            // "Manage Team" button (shows candidate count badge)
            p.manageBtnW = 120;
            p.manageBtnH = btnH;
            btnRowX -= p.manageBtnW;
            p.manageBtnX = btnRowX;
            p.manageBtnY = curY - 2;

            RoundRectangle2D mBtn = new RoundRectangle2D.Float(p.manageBtnX, p.manageBtnY, p.manageBtnW, p.manageBtnH, 8, 8);
            g.setColor(DashboardTheme.PURPLE_TINT_BG);
            g.fill(mBtn);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.setFont(DashboardTheme.FONT_BOLD_12);
            String mLabel = "Manage Team" + (p.candidates.size() > 0 ? " (" + p.candidates.size() + ")" : "");
            int mlw = g.getFontMetrics().stringWidth(mLabel);
            g.drawString(mLabel, p.manageBtnX + (p.manageBtnW - mlw) / 2, p.manageBtnY + 21);
            btnRowX -= 10;
        }

        // "Project Board" button
        p.boardBtnW = 105;
        p.boardBtnH = btnH;
        btnRowX -= p.boardBtnW;
        p.boardBtnX = btnRowX;
        p.boardBtnY = curY - 2;

        RoundRectangle2D bBtn = new RoundRectangle2D.Float(p.boardBtnX, p.boardBtnY, p.boardBtnW, p.boardBtnH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(bBtn);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(bBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Tasks (" + p.tasks.size() + ")", p.boardBtnX + 16, p.boardBtnY + 21);

        return cardH;
    }

    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        String msg = "No projects found in this view";
        int mw = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, x + (w - mw) / 2, y + 100);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String sub = "Create a new project or explore available projects to collaborate with peers.";
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, x + (w - sw) / 2, y + 130);
    }

    // =========================================================================
    // OVERLAYS & SUB-MODALS
    // =========================================================================

    public void paintOverlays(Graphics2D g, int screenW, int screenH) {
        // Dropdown Overlay
        if (activeDropdown != DropdownType.NONE) {
            switch (activeDropdown) {
                case STATUS:
                    overlayItems = new String[]{"All statuses", "Recruiting", "Active", "Completed"};
                    paintGenericMenu(g, statusDropX, statusDropY + statusDropH + 4, statusDropW, overlayItems, filterStatus);
                    break;
                case ROLE:
                    overlayItems = new String[]{"All roles", "Project Lead", "Team Member"};
                    paintGenericMenu(g, roleDropX, roleDropY + roleDropH + 4, roleDropW, overlayItems, filterRole);
                    break;
                case SORT:
                    overlayItems = new String[]{"Recently updated", "Progress: High to Low", "Most members", "Alphabetical"};
                    paintGenericMenu(g, sortDropX, sortDropY + sortDropH + 4, sortDropW, overlayItems, selectedSort);
                    break;
                default:
                    break;
            }
        }

        // Sub-Modals
        if (activeSubModal == SubModal.MANAGE_TEAM && modalProject != null) {
            paintManageTeamModal(g, screenW, screenH);
        } else if (activeSubModal == SubModal.PROJECT_BOARD && modalProject != null) {
            paintProjectBoardModal(g, screenW, screenH);
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

    private void paintManageTeamModal(Graphics2D g, int sw, int sh) {
        // Scrim
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, sw, sh);

        int mw = 560;
        int mh = 500;
        int mx = (sw - mw) / 2;
        int my = (sh - mh) / 2;

        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(mShape);

        // Close (X)
        subModalCloseX = mx + mw - 36;
        subModalCloseY = my + 18;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("✕", subModalCloseX, subModalCloseY + 16);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Team & Applicants: " + modalProject.title, mx + 26, my + 38);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Review pending join requests, manage active members, and assign roles.", mx + 26, my + 58);

        // Section 1: Pending Join Requests
        int curY = my + 82;
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("PENDING APPLICANTS (" + modalProject.candidates.size() + ")", mx + 26, curY);

        curY += 12;
        if (modalProject.candidates.isEmpty()) {
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("No pending join requests right now.", mx + 26, curY + 16);
            curY += 32;
        } else {
            for (Candidate c : modalProject.candidates) {
                RoundRectangle2D cRow = new RoundRectangle2D.Float(mx + 26, curY, mw - 52, 46, 8, 8);
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fill(cRow);
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(cRow);

                // Avatar
                g.setColor(c.bg);
                g.fillOval(mx + 36, curY + 8, 30, 30);
                g.setFont(DashboardTheme.FONT_BOLD_11);
                g.setColor(c.text);
                g.drawString(c.initials, mx + 42, curY + 27);

                // Name & Role
                g.setFont(DashboardTheme.FONT_BOLD_12);
                g.setColor(DashboardTheme.TEXT_DARK);
                g.drawString(c.name, mx + 76, curY + 20);

                g.setFont(DashboardTheme.FONT_REG_11);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString(c.role + " · " + c.matchPct + " skill match", mx + 76, curY + 35);

                // Accept / Decline Buttons
                if (c.accepted) {
                    g.setFont(DashboardTheme.FONT_BOLD_12);
                    g.setColor(new Color(0x15, 0x80, 0x3D));
                    g.drawString("✓ Accepted", mx + mw - 120, curY + 28);
                } else if (c.declined) {
                    g.setFont(DashboardTheme.FONT_BOLD_12);
                    g.setColor(DashboardTheme.TEXT_LIGHT);
                    g.drawString("Declined", mx + mw - 110, curY + 28);
                } else {
                    c.acceptX = mx + mw - 52 - 160;
                    c.acceptY = curY + 9;
                    RoundRectangle2D accBtn = new RoundRectangle2D.Float(c.acceptX, c.acceptY, c.acceptW, c.acceptH, 6, 6);
                    g.setColor(new Color(0xDC, 0xFC, 0xE7));
                    g.fill(accBtn);
                    g.setFont(DashboardTheme.FONT_BOLD_11);
                    g.setColor(new Color(0x15, 0x80, 0x3D));
                    g.drawString("Accept", c.acceptX + 18, c.acceptY + 18);

                    c.declineX = c.acceptX + c.acceptW + 8;
                    c.declineY = curY + 9;
                    RoundRectangle2D decBtn = new RoundRectangle2D.Float(c.declineX, c.declineY, c.declineW, c.declineH, 6, 6);
                    g.setColor(Color.WHITE);
                    g.fill(decBtn);
                    g.setColor(DashboardTheme.BORDER_COLOR);
                    g.draw(decBtn);
                    g.setColor(DashboardTheme.TEXT_MUTED);
                    g.drawString("Decline", c.declineX + 16, c.declineY + 18);
                }

                curY += 54;
            }
        }

        // Section 2: Active Members
        curY += 8;
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("CURRENT TEAM MEMBERS (" + modalProject.members.size() + "/" + modalProject.totalCapacity + ")", mx + 26, curY);

        curY += 12;
        for (MemberAvatar m : modalProject.members) {
            RoundRectangle2D mRow = new RoundRectangle2D.Float(mx + 26, curY, mw - 52, 42, 8, 8);
            g.setColor(Color.WHITE);
            g.fill(mRow);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(mRow);

            g.setColor(m.bg);
            g.fillOval(mx + 36, curY + 7, 28, 28);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(m.text);
            g.drawString(m.initials, mx + 42, curY + 25);

            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(m.name, mx + 76, curY + 18);

            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(m.role, mx + 76, curY + 32);

            curY += 48;
        }
    }

    private void paintProjectBoardModal(Graphics2D g, int sw, int sh) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, sw, sh);

        int mw = 560;
        int mh = 480;
        int mx = (sw - mw) / 2;
        int my = (sh - mh) / 2;

        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(mShape);

        // Close (X)
        subModalCloseX = mx + mw - 36;
        subModalCloseY = my + 18;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("✕", subModalCloseX, subModalCloseY + 16);

        // Title
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Tasks & Sprints: " + modalProject.title, mx + 26, my + 38);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Click any task to toggle complete or add new milestones for your team.", mx + 26, my + 58);

        // Tasks list
        int curY = my + 84;
        for (ProjectTask task : modalProject.tasks) {
            task.hitX = mx + 26;
            task.hitY = curY;
            task.hitW = mw - 52;
            task.hitH = 40;

            RoundRectangle2D tRow = new RoundRectangle2D.Float(task.hitX, task.hitY, task.hitW, task.hitH, 8, 8);
            g.setColor(task.done ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
            g.fill(tRow);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(tRow);

            // Checkbox
            int cbX = task.hitX + 14;
            int cbY = curY + 12;
            RoundRectangle2D cb = new RoundRectangle2D.Float(cbX, cbY, 16, 16, 4, 4);
            g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : Color.WHITE);
            g.fill(cb);
            g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.BORDER_COLOR);
            g.draw(cb);
            if (task.done) {
                g.setColor(Color.WHITE);
                g.setFont(DashboardTheme.FONT_BOLD_11);
                g.drawString("✓", cbX + 3, cbY + 13);
            }

            // Task title
            g.setFont(DashboardTheme.FONT_REG_13);
            g.setColor(task.done ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
            String titleText = task.title;
            if (task.done) titleText = "<s>" + titleText; // visual indicator
            g.drawString(task.title, cbX + 26, curY + 25);

            // Due Date
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            int dw = g.getFontMetrics().stringWidth(task.dueDate);
            g.drawString(task.dueDate, task.hitX + task.hitW - dw - 16, curY + 25);

            curY += 46;
        }

        // Add Task Row at Bottom
        int botY = my + mh - 60;
        newTaskInputX = mx + 26;
        newTaskInputY = botY;
        newTaskInputW = mw - 52 - 105;
        newTaskInputH = 36;

        RoundRectangle2D inBox = new RoundRectangle2D.Float(newTaskInputX, newTaskInputY, newTaskInputW, newTaskInputH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(inBox);
        g.setColor(newTaskFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(newTaskFocused ? 1.4f : 1f));
        g.draw(inBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        if (newTaskTitle.isEmpty() && !newTaskFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Type a new task and press Add...", newTaskInputX + 12, newTaskInputY + 23);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(newTaskTitle + (newTaskFocused && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""), newTaskInputX + 12, newTaskInputY + 23);
        }

        addTaskBtnX = newTaskInputX + newTaskInputW + 10;
        addTaskBtnY = botY;
        addTaskBtnW = 95;
        addTaskBtnH = 36;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addTaskBtnX, addTaskBtnY, addTaskBtnW, addTaskBtnH, 8, 8);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.fill(addBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("+ Add Task", addTaskBtnX + 14, addTaskBtnY + 23);
    }

    // =========================================================================
    // EVENT HANDLING
    // =========================================================================

    // Captured from onClick so keyboard-added tasks can be persisted too
    private StudentDashboard dashboardRef;

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        this.dashboardRef = dashboard;
        // 1. Sub-modal takes top priority
        if (activeSubModal != SubModal.NONE && modalProject != null) {
            // Close (X) button
            if (mx >= subModalCloseX - 6 && mx <= subModalCloseX + subModalCloseSize &&
                    my >= subModalCloseY - 6 && my <= subModalCloseY + subModalCloseSize) {
                activeSubModal = SubModal.NONE;
                modalProject = null;
                return true;
            }

            // Manage Team Actions: Accept / Decline candidate
            if (activeSubModal == SubModal.MANAGE_TEAM) {
                for (Candidate c : modalProject.candidates) {
                    if (!c.accepted && !c.declined) {
                        if (mx >= c.acceptX && mx <= c.acceptX + c.acceptW && my >= c.acceptY && my <= c.acceptY + c.acceptH) {
                            c.accepted = true;
                            DashboardSync.decideRequest(dashboard, c.remoteId, true);
                            modalProject.members.add(new MemberAvatar(c.initials, c.name, c.role, c.bg, c.text));
                            dashboard.showToast(c.name + " accepted into " + modalProject.title + "!");
                            return true;
                        }
                        if (mx >= c.declineX && mx <= c.declineX + c.declineW && my >= c.declineY && my <= c.declineY + c.declineH) {
                            c.declined = true;
                            DashboardSync.decideRequest(dashboard, c.remoteId, false);
                            dashboard.showToast(c.name + "'s request was declined.");
                            return true;
                        }
                    }
                }
            }

            // Project Board Actions: Toggle task, Add task
            if (activeSubModal == SubModal.PROJECT_BOARD) {
                // Task toggle
                for (ProjectTask task : modalProject.tasks) {
                    if (mx >= task.hitX && mx <= task.hitX + task.hitW && my >= task.hitY && my <= task.hitY + task.hitH) {
                        task.done = !task.done;
                        DashboardSync.setTaskDone(dashboard, task, task.done);
                        // update project progress pct
                        long doneCount = modalProject.tasks.stream().filter(t -> t.done).count();
                        modalProject.progressPct = (int) Math.round(((double) doneCount / modalProject.tasks.size()) * 100);
                        dashboard.showToast(task.done ? "Marked complete: " + task.title : "Task reopened");
                        return true;
                    }
                }

                // Focus new task input
                if (mx >= newTaskInputX && mx <= newTaskInputX + newTaskInputW && my >= newTaskInputY && my <= newTaskInputY + newTaskInputH) {
                    newTaskFocused = true;
                    return true;
                } else {
                    newTaskFocused = false;
                }

                // Add Task button
                if (mx >= addTaskBtnX && mx <= addTaskBtnX + addTaskBtnW && my >= addTaskBtnY && my <= addTaskBtnY + addTaskBtnH) {
                    if (!newTaskTitle.trim().isEmpty()) {
                        ProjectTask added = new ProjectTask(newTaskTitle.trim(), "Upcoming", false);
                        modalProject.tasks.add(added);
                        DashboardSync.addTask(dashboard, modalProject.remoteId, added);
                        long doneCount = modalProject.tasks.stream().filter(t -> t.done).count();
                        modalProject.progressPct = (int) Math.round(((double) doneCount / modalProject.tasks.size()) * 100);
                        dashboard.showToast("Task added to " + modalProject.title + "!");
                        newTaskTitle = "";
                    }
                    return true;
                }
            }

            return true; // Click inside modal swallowed
        }

        // 2. Dropdown Overlay click
        if (activeDropdown != DropdownType.NONE) {
            if (mx >= overlayX && mx <= overlayX + overlayW && my >= overlayY && my <= overlayY + overlayH) {
                int itemH = 34;
                int clickedIdx = (my - overlayY - 6) / itemH;
                if (clickedIdx >= 0 && clickedIdx < overlayItems.length) {
                    String chosen = overlayItems[clickedIdx];
                    if (activeDropdown == DropdownType.STATUS) {
                        filterStatus = chosen;
                    } else if (activeDropdown == DropdownType.ROLE) {
                        filterRole = chosen;
                    } else if (activeDropdown == DropdownType.SORT) {
                        selectedSort = chosen;
                    }
                    applyFilters();
                    dashboard.showToast("Filter: " + chosen);
                }
            }
            activeDropdown = DropdownType.NONE;
            return true;
        }

        // 3. "+ New Project" Button
        if (mx >= createBtnX && mx <= createBtnX + createBtnW && my >= createBtnY && my <= createBtnY + createBtnH) {
            dashboard.openModal(StudentDashboard.ActiveModal.CREATE_PROJECT);
            return true;
        }

        // 4. Tab Navigation
        for (int i = 0; i < tabs.size(); i++) {
            TabHitbox th = tabs.get(i);
            if (mx >= th.x && mx <= th.x + th.w && my >= th.y && my <= th.y + th.h) {
                selectedTab = i;
                applyFilters();
                dashboard.showToast("View: " + th.label);
                return true;
            }
        }

        // 5. Search Box focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 6. Dropdown Toggles
        if (mx >= statusDropX && mx <= statusDropX + statusDropW && my >= statusDropY && my <= statusDropY + statusDropH) {
            activeDropdown = (activeDropdown == DropdownType.STATUS) ? DropdownType.NONE : DropdownType.STATUS;
            return true;
        }
        if (mx >= roleDropX && mx <= roleDropX + roleDropW && my >= roleDropY && my <= roleDropY + roleDropH) {
            activeDropdown = (activeDropdown == DropdownType.ROLE) ? DropdownType.NONE : DropdownType.ROLE;
            return true;
        }
        if (mx >= sortDropX && mx <= sortDropX + sortDropW && my >= sortDropY && my <= sortDropY + sortDropH) {
            activeDropdown = (activeDropdown == DropdownType.SORT) ? DropdownType.NONE : DropdownType.SORT;
            return true;
        }

        // 7. Project Card Buttons
        for (MyProjectCard p : filteredProjects) {
            // Manage Team
            if (p.isLead && mx >= p.manageBtnX && mx <= p.manageBtnX + p.manageBtnW && my >= p.manageBtnY && my <= p.manageBtnY + p.manageBtnH) {
                modalProject = p;
                activeSubModal = SubModal.MANAGE_TEAM;
                return true;
            }

            // Tasks / Project Board
            if (mx >= p.boardBtnX && mx <= p.boardBtnX + p.boardBtnW && my >= p.boardBtnY && my <= p.boardBtnY + p.boardBtnH) {
                modalProject = p;
                activeSubModal = SubModal.PROJECT_BOARD;
                newTaskTitle = "";
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
            if (activeSubModal != SubModal.NONE) {
                activeSubModal = SubModal.NONE;
                modalProject = null;
                return true;
            }
            if (activeDropdown != DropdownType.NONE) {
                activeDropdown = DropdownType.NONE;
                return true;
            }
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
            if (newTaskFocused) {
                newTaskFocused = false;
                return true;
            }
        }

        // Typing in new task
        if (activeSubModal == SubModal.PROJECT_BOARD && newTaskFocused) {
            if (code == KeyEvent.VK_ENTER) {
                if (modalProject != null && !newTaskTitle.trim().isEmpty()) {
                    ProjectTask added = new ProjectTask(newTaskTitle.trim(), "Upcoming", false);
                    modalProject.tasks.add(added);
                    if (dashboardRef != null) DashboardSync.addTask(dashboardRef, modalProject.remoteId, added);
                    long doneCount = modalProject.tasks.stream().filter(t -> t.done).count();
                    modalProject.progressPct = (int) Math.round(((double) doneCount / modalProject.tasks.size()) * 100);
                    newTaskTitle = "";
                }
                return true;
            }
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (newTaskTitle.length() > 0) newTaskTitle = newTaskTitle.substring(0, newTaskTitle.length() - 1);
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && newTaskTitle.length() < 60) {
                newTaskTitle += c;
                return true;
            }
            return true;
        }

        // Search box typing
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
}

