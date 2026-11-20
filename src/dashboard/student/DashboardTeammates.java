import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the "Find your next teammate" view for TeamUp Student Dashboard.
 * Matches Figma reference Image 3 with exact precision.
 * Fully interactive with live search, 5 filter dropdowns, availability toggle chip,
 * clear filters button, sort dropdown, teammate profile modal, + Invite toggle,
 * request button, portfolio link actions, skill tag filtering, and pagination.
 */
public class DashboardTeammates {

    public static class TeammateCard {
        public int id;
        public String remoteId; // MongoDB user id when loaded from the backend
        public String name;
        public String role;
        public String university;
        public String initials;
        public Color avatarBg;
        public Color avatarText;
        public String availability;
        public double rating;
        public int reviews;
        public List<String> skills = new ArrayList<>();
        public String matchPct;
        public String matchDesc;
        public String portfolio;
        public String experience; // Beginner, Intermediate, Advanced
        public String bio;
        public boolean invited = false;
        public boolean requested = false;

        // Hitbox tracking per frame
        public int hitX, hitY, hitW, hitH;
        public int viewProfX, viewProfY, viewProfW, viewProfH;
        public int inviteBtnX, inviteBtnY, inviteBtnW, inviteBtnH;
        public int requestBtnX, requestBtnY, requestBtnW, requestBtnH;
        public int linkX, linkY, linkW, linkH;

        public static class SkillHitbox {
            public String name;
            public int x, y, w, h;
            public SkillHitbox(String name, int x, int y, int w, int h) {
                this.name = name; this.x = x; this.y = y; this.w = w; this.h = h;
            }
        }
        public final List<SkillHitbox> skillHits = new ArrayList<>();

        public TeammateCard(int id, String name, String role, String university, String initials,
                            Color avatarBg, Color avatarText, String availability, double rating, int reviews,
                            String[] skillsArr, String matchPct, String matchDesc, String portfolio,
                            String experience, String bio) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.university = university;
            this.initials = initials;
            this.avatarBg = avatarBg;
            this.avatarText = avatarText;
            this.availability = availability;
            this.rating = rating;
            this.reviews = reviews;
            Collections.addAll(this.skills, skillsArr);
            this.matchPct = matchPct;
            this.matchDesc = matchDesc;
            this.portfolio = portfolio;
            this.experience = experience;
            this.bio = bio;
        }
    }

    private final List<TeammateCard> allTeammates = new ArrayList<>();
    private final List<TeammateCard> filteredTeammates = new ArrayList<>();

    // State & Filters
    private String searchKeyword = "";
    private boolean searchFocused = false;
    private String filterSkill = "All";
    private String filterExperience = "All";
    private String filterAvailability = "All";
    private String filterRating = "All";
    private String selectedSort = "Best sample match";
    private boolean filterOnlyAvailable = false;

    private int currentPage = 1;
    private final int itemsPerPage = 6;
    private int totalContentHeight = 1200;

    // Dropdowns
    public enum DropdownType { NONE, SKILLS, EXPERIENCE, AVAILABILITY, RATING, SORT }
    private DropdownType activeDropdown = DropdownType.NONE;

    // Hitboxes
    private int searchX, searchY, searchW, searchH = 40;
    private int skillDropX, skillDropY, skillDropW = 100, skillDropH = 40;
    private int expDropX, expDropY, expDropW = 115, expDropH = 40;
    private int availDropX, availDropY, availDropW = 120, availDropH = 40;
    private int ratingDropX, ratingDropY, ratingDropW = 110, ratingDropH = 40;

    private int availChipX, availChipY, availChipW, availChipH = 26;
    private int clearFiltersX, clearFiltersY, clearFiltersW, clearFiltersH = 26;
    private int sortDropX, sortDropY, sortDropW = 190, sortDropH = 34;

    private static class BoxHit {
        String label;
        int x, y, w, h;
        BoxHit(String label, int x, int y, int w, int h) {
            this.label = label; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
    private int prevBtnX, prevBtnY, prevBtnW = 90, prevBtnH = 32;
    private int nextBtnX, nextBtnY, nextBtnW = 75, nextBtnH = 32;
    private final List<BoxHit> pageBoxes = new ArrayList<>();

    // Overlay click bounds
    private int overlayX, overlayY, overlayW, overlayH;
    private String[] overlayItems = new String[0];

    // Detail Profile Modal state
    private TeammateCard activeProfileTeammate = null;
    private int modalCloseX, modalCloseY, modalCloseSize = 28;
    private int modalInviteBtnX, modalInviteBtnY, modalInviteBtnW = 140, modalInviteBtnH = 38;

    public DashboardTeammates() {
        initDemoTeammates();
        applyFilters();
    }

    /** Replaces the teammate list with data loaded from the backend, keeping filters & page. */
    public void setTeammates(List<TeammateCard> teammates) {
        allTeammates.clear();
        allTeammates.addAll(teammates);
        if (activeProfileTeammate != null) activeProfileTeammate = null;
        applyFilters();
    }

    private void initDemoTeammates() {
        boolean loadDemo = false;
        if (!loadDemo) return;
        // Page 1 (The exact 6 cards from screenshot 3)
        allTeammates.add(new TeammateCard(
                1, "Priya Shah", "UX/UI designer", "University of Washington", "PS",
                new Color(0xED, 0xE9, 0xFE), new Color(0x6D, 0x28, 0xD9),
                "Available · 10-15 hrs / week", 4.9, 12,
                new String[]{"Figma", "UX research", "Design systems"},
                "96% skill match · Sample", "Pairs your full-stack skills with user research and accessible product design.",
                "priyashah.design", "Advanced",
                "Senior studying HCDE at UW. Passionate about human-centered design, prototyping in Figma, and building inclusive digital experiences."
        ));
        allTeammates.add(new TeammateCard(
                2, "Daniel Kim", "Backend developer", "University of Washington", "DK",
                new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Available · 8-12 hrs / week", 4.8, 9,
                new String[]{"Node.js", "PostgreSQL", "Docker"},
                "92% skill match · Sample", "Adds API architecture and database expertise to your React projects.",
                "github.com/danielkim", "Advanced",
                "Junior CS student at UW. Focused on backend scalability, concurrent REST/GraphQL APIs, and Docker deployments."
        ));
        allTeammates.add(new TeammateCard(
                3, "Sofia Martinez", "Frontend developer", "University of Oregon", "SM",
                new Color(0xFF, 0xE4, 0xE6), new Color(0xBE, 0x12, 0x3C),
                "Available · 10 hrs / week", 4.9, 8,
                new String[]{"React", "TypeScript", "Accessibility"},
                "89% skill match · Sample", "Shares your TypeScript interests and brings a strong accessibility focus.",
                "sofiam.dev", "Intermediate",
                "Junior studying CS at UO. Loves crafting delightful micro-interactions, responsive CSS layouts, and WCAG AA accessibility."
        ));
        allTeammates.add(new TeammateCard(
                4, "Ethan Chen", "QA engineer", "University of Washington", "EC",
                new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09),
                "Available · 6-10 hrs / week", 4.7, 6,
                new String[]{"Playwright", "Testing", "CI/CD"},
                "88% skill match · Sample", "Complements your development work with automated tests and release quality.",
                "github.com/ethanchen", "Intermediate",
                "Senior Informatics major specializing in test-driven development, GitHub Actions pipelines, and automated end-to-end testing."
        ));
        allTeammates.add(new TeammateCard(
                5, "Maya Patel", "Full-stack developer", "UC Berkeley", "MP",
                new Color(0xEE, 0xF2, 0xFF), new Color(0x43, 0x38, 0xCA),
                "Available · 8 hrs / week", 4.8, 11,
                new String[]{"React", "Python", "Maps API"},
                "85% skill match · Sample", "A shared interest in sustainability, with geospatial skills for new ideas.",
                "mayapatel.dev", "Advanced",
                "Computer Science junior at UC Berkeley. Experienced in Python data pipelines, interactive mapping visualizations, and Next.js."
        ));
        allTeammates.add(new TeammateCard(
                6, "Noah Williams", "Data engineer", "Stanford University", "NW",
                new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5),
                "Available · 5-8 hrs / week", 4.8, 7,
                new String[]{"Python", "PostgreSQL", "Data science"},
                "83% skill match · Sample", "Brings data modeling and research experience to your product thinking.",
                "github.com/noahw", "Intermediate",
                "Data Science sophomore at Stanford with a strong interest in relational database architecture, ETL workflows, and research data."
        ));

        // Page 2
        allTeammates.add(new TeammateCard(
                7, "Leila Hassan", "Mobile Developer", "University of Washington", "LH",
                new Color(0xFC, 0xE7, 0xF3), new Color(0xBE, 0x18, 0x5D),
                "Available · 10-12 hrs / week", 4.9, 14,
                new String[]{"React Native", "Swift", "Firebase"},
                "82% skill match · Sample", "Mobile architecture specialist with published iOS and Android campus apps.",
                "leilahassan.io", "Advanced",
                "Passionate about cross-platform native app performance, push notification workflows, and intuitive gesture-driven interfaces."
        ));
        allTeammates.add(new TeammateCard(
                8, "Oliver Brooks", "Full-stack developer", "University of Washington", "OB",
                new Color(0xDC, 0xF8, 0xC6), new Color(0x16, 0x65, 0x34),
                "Available · 8-10 hrs / week", 4.8, 10,
                new String[]{"Next.js", "Prisma", "Tailwind"},
                "80% skill match · Sample", "Rapid prototyper who ships high quality full-stack web applications quickly.",
                "oliverbrooks.tech", "Intermediate",
                "Senior student who loves turning ideas into working SaaS projects overnight using modern serverless platforms and Prisma ORM."
        ));
        allTeammates.add(new TeammateCard(
                9, "Aisha Rahman", "Product Designer", "University of Washington", "AR",
                new Color(0xE0, 0xE7, 0xFF), new Color(0x37, 0x30, 0xA3),
                "Available · 12-15 hrs / week", 5.0, 18,
                new String[]{"Figma", "UI/UX", "User Testing"},
                "79% skill match · Sample", "Design lead with multiple university design competition winning entries.",
                "aisharahman.design", "Advanced",
                "Master's student in Interaction Design. Passionate about design systems, usability benchmarking, and high-fidelity clickable mockups."
        ));
        allTeammates.add(new TeammateCard(
                10, "Marcus Chen", "Cloud & DevOps", "UC Berkeley", "MC",
                new Color(0xFE, 0xF3, 0xC7), new Color(0x92, 0x40, 0x0E),
                "Available · 6-8 hrs / week", 4.7, 8,
                new String[]{"Docker", "Kubernetes", "AWS"},
                "78% skill match · Sample", "Automates container pipelines, CI deployments, and cloud infrastructure.",
                "marcuschen.cloud", "Advanced",
                "Specialized in AWS cloud solutions, Docker orchestration, and zero-downtime deployment pipelines for academic tech teams."
        ));
        allTeammates.add(new TeammateCard(
                11, "Sarah Jenkins", "Frontend engineer", "University of Washington", "SJ",
                new Color(0xFF, 0xED, 0xD5), new Color(0xC2, 0x41, 0x0C),
                "Available · 6-8 hrs / week", 4.8, 9,
                new String[]{"Vue.js", "Tailwind", "CSS"},
                "76% skill match · Sample", "Component-driven frontend design with responsive micro-animations.",
                "sarahj.dev", "Beginner",
                "Sophomore student passionate about semantic web technologies, modern reactive state management, and lightweight animations."
        ));
        allTeammates.add(new TeammateCard(
                12, "Chloe Bennett", "UX Researcher", "Stanford University", "CB",
                new Color(0xCF, 0xFA, 0xFE), new Color(0x0E, 0x74, 0x90),
                "Available · 8-12 hrs / week", 4.9, 11,
                new String[]{"UX research", "Figma", "User Testing"},
                "75% skill match · Sample", "Conducts student user interviews and synthesizes qualitative insights.",
                "chloeb.design", "Intermediate",
                "Enjoys user interviews, card sorting, wireframe usability tests, and turning student feedback into concrete feature roadmaps."
        ));

        // Page 3
        allTeammates.add(new TeammateCard(
                13, "Lucas Vance", "Systems Engineer", "University of Washington", "LV",
                new Color(0xDB, 0xEA, 0xFE), new Color(0x1E, 0x40, 0xAF),
                "Available · 5-8 hrs / week", 4.8, 6,
                new String[]{"C++", "Rust", "Linux"},
                "74% skill match · Sample", "Low-level systems architecture and high-throughput backend services.",
                "github.com/lucasv", "Advanced",
                "CS senior focused on performance profiling, multithreaded concurrency, and memory-safe systems software in Rust and C++."
        ));
        allTeammates.add(new TeammateCard(
                14, "Fatima Zahra", "Mobile Developer", "University of Washington", "FZ",
                new Color(0xF3, 0xE8, 0xFF), new Color(0x6B, 0x21, 0xA8),
                "Available · 10 hrs / week", 4.9, 13,
                new String[]{"Flutter", "Firebase", "Dart"},
                "73% skill match · Sample", "Cross-platform mobile apps for Android and iOS student workflows.",
                "fatimaz.dev", "Intermediate",
                "Experienced Flutter developer who enjoys creating clean state architecture with Bloc and real-time synchronization with Firestore."
        ));
        allTeammates.add(new TeammateCard(
                15, "Ryan Cooper", "DevOps & Cloud", "UCLA", "RC",
                new Color(0xFF, 0xED, 0xD5), new Color(0x9A, 0x34, 0x12),
                "Available · 6-10 hrs / week", 4.7, 7,
                new String[]{"Docker", "CI/CD", "AWS"},
                "72% skill match · Sample", "CI/CD pipeline automation and cloud container deployments.",
                "github.com/ryancooper", "Intermediate",
                "Focuses on developer experience, reproducible container builds, automated unit test runners, and monitoring alerts."
        ));
        allTeammates.add(new TeammateCard(
                16, "Emma Wilson", "Data Scientist", "University of Washington", "EW",
                new Color(0xCC, 0xFB, 0xF1), new Color(0x11, 0x5E, 0x59),
                "Available · 8-10 hrs / week", 4.8, 12,
                new String[]{"Python", "PostgreSQL", "Data science"},
                "70% skill match · Sample", "Statistical analysis and predictive modeling for academic research.",
                "emmawilson.data", "Intermediate",
                "Statistics & CS double major. Skilled in Pandas, Scikit-learn, exploratory data analysis, and dashboard communication."
        ));
        allTeammates.add(new TeammateCard(
                17, "Liam Davis", "Security & QA", "University of Oregon", "LD",
                new Color(0xEE, 0xF2, 0xFF), new Color(0x37, 0x30, 0xA3),
                "Available · 6-8 hrs / week", 4.7, 5,
                new String[]{"Testing", "Python", "Security"},
                "69% skill match · Sample", "Web application penetration testing and secure authentication review.",
                "liamdavis.security", "Intermediate",
                "Passionate about web app vulnerabilities (OWASP Top 10), cryptographic protocols, and secure credential handling in student teams."
        ));
        allTeammates.add(new TeammateCard(
                18, "Zoe Martinez", "Content & UI", "University of Washington", "ZM",
                new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0x65, 0x34),
                "Available · 8-12 hrs / week", 4.8, 9,
                new String[]{"Figma", "UI/UX", "Copywriting"},
                "68% skill match · Sample", "Brand voice, onboarding flows, and polished design systems.",
                "zoemartinez.design", "Beginner",
                "Communications and Design double major who writes clear product copy and crafts approachable user onboarding flows."
        ));

        // Page 4
        allTeammates.add(new TeammateCard(
                19, "Ethan Hunt", "Robotics Engineer", "Stanford University", "EH",
                new Color(0xEE, 0xF2, 0xFF), new Color(0x43, 0x38, 0xCA),
                "Available · 10-15 hrs / week", 4.9, 16,
                new String[]{"C++", "ROS", "Python"},
                "67% skill match · Sample", "Sensor fusion and robotics algorithms for capstone hardware.",
                "github.com/ethanhunt", "Advanced",
                "Robotics enthusiast experienced with LiDAR mapping, SLAM, motor controllers, and Linux embedded computing."
        ));
        allTeammates.add(new TeammateCard(
                20, "Maya Lin", "Bioinformatics", "UC Berkeley", "ML",
                new Color(0xF3, 0xE8, 0xFF), new Color(0x7E, 0x22, 0xCE),
                "Available · 8 hrs / week", 4.8, 8,
                new String[]{"Python", "PostgreSQL", "Data science"},
                "66% skill match · Sample", "Genomic data pipelines and molecular modeling simulations.",
                "mayalin.bio", "Advanced",
                "Computational Biology researcher with experience handling high-throughput DNA sequencing data with Python and biopython."
        ));
        allTeammates.add(new TeammateCard(
                21, "David Park", "Full-stack developer", "University of Washington", "DP",
                new Color(0xDB, 0xEA, 0xFE), new Color(0x1D, 0x4E, 0xD8),
                "Available · 10-14 hrs / week", 4.9, 11,
                new String[]{"React", "TypeScript", "Node.js"},
                "65% skill match · Sample", "High performance microservices with modern React frontends.",
                "davidpark.codes", "Advanced",
                "Software engineer with an emphasis on type safety, clean code architecture, and resilient API microservices."
        ));
        allTeammates.add(new TeammateCard(
                22, "Grace Hopper", "Backend engineer", "University of Washington", "GH",
                new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D),
                "Available · 8-10 hrs / week", 5.0, 20,
                new String[]{"Java", "PostgreSQL", "Docker"},
                "64% skill match · Sample", "Robust Java backend services and concurrent database schemas.",
                "gracehopper.dev", "Advanced",
                "Senior computer scientist who loves clean OOP design, enterprise Java frameworks, and optimized SQL indices."
        ));
        allTeammates.add(new TeammateCard(
                23, "James Wilson", "Frontend developer", "University of Washington", "JW",
                new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09),
                "Available · 6-8 hrs / week", 4.6, 5,
                new String[]{"React", "JavaScript", "CSS"},
                "62% skill match · Sample", "Clean semantic markup and accessible UI components.",
                "github.com/jwilson", "Beginner",
                "Sophomore student actively learning React, state management, and clean modern responsive layouts."
        ));
        allTeammates.add(new TeammateCard(
                24, "Elena Rostova", "AI Research", "Stanford University", "ER",
                new Color(0xFC, 0xE7, 0xF3), new Color(0x9D, 0x17, 0x4D),
                "Available · 8-12 hrs / week", 4.9, 12,
                new String[]{"Python", "FastAPI", "Data science"},
                "60% skill match · Sample", "Natural language processing and sentiment analysis pipelines.",
                "elenarostova.ai", "Advanced",
                "Graduate student working on Transformer models, LLM prompt orchestration, and ethical AI evaluation frameworks."
        ));
    }

    public void applyFilters() {
        filteredTeammates.clear();
        String query = searchKeyword.trim().toLowerCase();

        for (TeammateCard t : allTeammates) {
            // Skill dropdown
            if (!filterSkill.equals("All")) {
                boolean hasSk = false;
                for (String s : t.skills) {
                    if (s.equalsIgnoreCase(filterSkill)) { hasSk = true; break; }
                }
                if (!hasSk) continue;
            }
            // Experience dropdown
            if (!filterExperience.equals("All") && !t.experience.equalsIgnoreCase(filterExperience)) {
                continue;
            }
            // Availability dropdown
            if (!filterAvailability.equals("All")) {
                String sub = filterAvailability.split(" ")[0];
                if (!t.availability.toLowerCase().contains(sub.toLowerCase())) continue;
            }
            // Rating dropdown
            if (!filterRating.equals("All")) {
                double minR = Double.parseDouble(filterRating.replace("+", ""));
                if (t.rating < minR) continue;
            }
            // Only available
            if (filterOnlyAvailable && !t.availability.toLowerCase().contains("available")) {
                continue;
            }
            // Search text
            if (!query.isEmpty()) {
                boolean matchName = t.name.toLowerCase().contains(query);
                boolean matchRole = t.role.toLowerCase().contains(query);
                boolean matchUni = t.university.toLowerCase().contains(query);
                boolean matchSkill = false;
                for (String s : t.skills) {
                    if (s.toLowerCase().contains(query)) { matchSkill = true; break; }
                }
                if (!matchName && !matchRole && !matchUni && !matchSkill) {
                    continue;
                }
            }
            filteredTeammates.add(t);
        }

        // Sorting
        if ("Highest rating".equals(selectedSort)) {
            filteredTeammates.sort((a, b) -> Double.compare(b.rating, a.rating));
        } else if ("Most reviews".equals(selectedSort)) {
            filteredTeammates.sort((a, b) -> Integer.compare(b.reviews, a.reviews));
        } else if ("Most available".equals(selectedSort)) {
            filteredTeammates.sort((a, b) -> b.availability.compareToIgnoreCase(a.availability));
        }

        // Pagination clamp
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredTeammates.size() / itemsPerPage));
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

        // 1. Heading & Subtitle
        g.setFont(DashboardTheme.FONT_HEADING);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Find your next teammate", startX + padX, curY + 24);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Different strengths. Shared ambition. Discover people you can build with.", startX + padX, curY + 46);

        curY += 72;

        // 2. Search Box + Filter Dropdowns Row
        searchW = Math.max(260, contentW - 470);
        searchH = 40;
        searchX = startX + padX;
        searchY = curY;

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(searchFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(searchFocused ? 1.5f : 1.0f));
        g.draw(sBox);

        // Magnifier
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 13, 11, 11);
        g.drawLine(searchX + 23, searchY + 22, searchX + 27, searchY + 26);

        g.setFont(DashboardTheme.FONT_REG_13);
        if (searchKeyword.isEmpty() && !searchFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Search by name, skill, role or interests...", searchX + 36, searchY + 25);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            String display = searchKeyword;
            if (searchFocused && (System.currentTimeMillis() % 1000 < 500)) display += "|";
            g.drawString(display, searchX + 36, searchY + 25);
        }

        // Dropdowns row
        int dropX = searchX + searchW + 12;
        skillDropX = dropX; skillDropY = curY; skillDropW = 95;
        paintFilterDropBtn(g, skillDropX, skillDropY, skillDropW, 40, "Skills", filterSkill.equals("All") ? "" : filterSkill, activeDropdown == DropdownType.SKILLS);
        dropX += skillDropW + 8;

        expDropX = dropX; expDropY = curY; expDropW = 115;
        paintFilterDropBtn(g, expDropX, expDropY, expDropW, 40, "Experience", filterExperience.equals("All") ? "" : filterExperience, activeDropdown == DropdownType.EXPERIENCE);
        dropX += expDropW + 8;

        availDropX = dropX; availDropY = curY; availDropW = 120;
        paintFilterDropBtn(g, availDropX, availDropY, availDropW, 40, "Availability", filterAvailability.equals("All") ? "" : filterAvailability, activeDropdown == DropdownType.AVAILABILITY);
        dropX += availDropW + 8;

        ratingDropX = dropX; ratingDropY = curY; ratingDropW = 110;
        paintFilterDropBtn(g, ratingDropX, ratingDropY, ratingDropW, 40, "Rating 4+", filterRating.equals("All") ? "" : filterRating, activeDropdown == DropdownType.RATING);

        curY += 56;

        // 3. Subheader: Count + "Available to collaborate" chip + "Clear filters" + Sort dropdown
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        String countStr = filteredTeammates.size() + " teammates";
        g.drawString(countStr, startX + padX, curY + 18);
        int cw = g.getFontMetrics().stringWidth(countStr);

        // "Available to collaborate" Chip
        availChipX = startX + padX + cw + 14;
        availChipY = curY + 1;
        availChipW = 150;
        RoundRectangle2D chipShape = new RoundRectangle2D.Float(availChipX, availChipY, availChipW, availChipH, 6, 6);
        g.setColor(filterOnlyAvailable ? DashboardTheme.BRAND_PURPLE : DashboardTheme.PURPLE_TINT_BG);
        g.fill(chipShape);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(filterOnlyAvailable ? Color.WHITE : DashboardTheme.BRAND_PURPLE);
        g.drawString("Available to collaborate", availChipX + 10, availChipY + 17);

        // "Clear filters" text button
        clearFiltersX = availChipX + availChipW + 14;
        clearFiltersY = curY + 1;
        clearFiltersW = 75;
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Clear filters", clearFiltersX, clearFiltersY + 17);

        // Right side: "Sort: Best sample match ▾"
        sortDropW = 195;
        sortDropH = 34;
        sortDropX = startX + padX + contentW - sortDropW;
        sortDropY = curY - 4;
        paintFilterDropBtn(g, sortDropX, sortDropY, sortDropW, sortDropH, "Sort: " + selectedSort, "", activeDropdown == DropdownType.SORT);

        curY += 46;

        // 4. Teammates Grid (3 Columns × 2 Rows)
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredTeammates.size());

        if (filteredTeammates.isEmpty()) {
            paintEmptyState(g, startX + padX, curY, contentW, 280);
            curY += 300;
        } else {
            int gap = 20;
            int cardW = (contentW - gap * 2) / 3;
            int cardH = 285;

            int rowCount = 0;
            for (int i = startIndex; i < endIndex; i++) {
                int col = (i - startIndex) % 3;
                int row = (i - startIndex) / 3;
                int cx = startX + padX + col * (cardW + gap);
                int cy = curY + row * (cardH + gap);

                paintSingleTeammateCard(g, cx, cy, cardW, cardH, filteredTeammates.get(i), dashboard);
                rowCount = Math.max(rowCount, row + 1);
            }
            curY += rowCount * (cardH + gap) + 12;
        }

        // 5. Pagination Row
        paintPagination(g, startX + padX, curY, contentW, filteredTeammates.size());
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
        if (text.length() > 20) text = text.substring(0, 18) + "..";
        g.drawString(text, x + 12, y + (h / 2) + 5);

        Draw.drawChevronDown(g, x + w - 16, y + (h / 2) - 2, 8, DashboardTheme.TEXT_MUTED, open ? 1.0f : 0.0f);
    }

    private void paintSingleTeammateCard(Graphics2D g, int cx, int cy, int cw, int ch, TeammateCard tm, StudentDashboard dashboard) {
        tm.hitX = cx; tm.hitY = cy; tm.hitW = cw; tm.hitH = ch;
        tm.skillHits.clear();
        int pad = 18;
        int innerW = cw - pad * 2;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        // Avatar + name + role · university (all ellipsized to the card width)
        int avSize = 44;
        int avX = cx + pad;
        int avY = cy + 18;
        paintAvatar(g, avX, avY, avSize, tm, 15);
        int infoX = avX + avSize + 12;
        int infoW = cx + cw - pad - infoX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(fitText(tm.name, g.getFontMetrics(), infoW), infoX, cy + 36);
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(fitText(joinNonEmpty(" · ", tm.role, tm.university), g.getFontMetrics(), infoW), infoX, cy + 53);

        // Availability (left) + rating (right): rating keeps its width, availability takes the rest
        int statY = cy + 82;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics bfm = g.getFontMetrics();
        String ratStr = tm.reviews > 0 ? "★ " + String.format(java.util.Locale.US, "%.1f", tm.rating) + "  (" + tm.reviews + ")" : "No reviews yet";
        int ratW = bfm.stringWidth(ratStr);
        g.setColor(tm.reviews > 0 ? new Color(0xB4, 0x53, 0x09) : DashboardTheme.TEXT_LIGHT);
        g.drawString(ratStr, cx + cw - pad - ratW, statY);
        boolean available = tm.availability != null && tm.availability.toLowerCase().startsWith("available");
        g.setColor(available ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.TEXT_LIGHT);
        g.fillOval(cx + pad, statY - 8, 7, 7);
        g.setColor(available ? new Color(0x15, 0x80, 0x3D) : DashboardTheme.TEXT_MUTED);
        g.drawString(fitText(tm.availability, bfm, innerW - ratW - 24), cx + pad + 12, statY);

        // Skills: one row, the rest collapse into "+N" (fixes the overflow past the card edge)
        int skY = cy + 96;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();
        int skX = cx + pad;
        int limit = cx + cw - pad;
        for (int i = 0; i < tm.skills.size(); i++) {
            String s = tm.skills.get(i);
            int sw = sfm.stringWidth(s) + 16;
            int remaining = tm.skills.size() - i - 1;
            int moreW = remaining > 0 ? sfm.stringWidth("+" + remaining) + 16 + 6 : 0;
            if (skX + sw + moreW > limit) {
                String more = "+" + (tm.skills.size() - i);
                int mw = sfm.stringWidth(more) + 16;
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Float(skX, skY, mw, 24, 8, 8));
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(new RoundRectangle2D.Float(skX, skY, mw, 24, 8, 8));
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString(more, skX + 8, skY + 16);
                break;
            }
            RoundRectangle2D skShape = new RoundRectangle2D.Float(skX, skY, sw, 24, 8, 8);
            g.setColor(new Color(0xF8, 0xF7, 0xF4));
            g.fill(skShape);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(skShape);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 8, skY + 16);
            tm.skillHits.add(new TeammateCard.SkillHitbox(s, skX, skY, sw, 24));
            skX += sw + 6;
        }

        // Match box: percentage + 2-line reason
        int matchBoxY = cy + 132;
        int matchBoxH = 58;
        RoundRectangle2D mBox = new RoundRectangle2D.Float(cx + pad, matchBoxY, innerW, matchBoxH, 10, 10);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(mBox);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.draw(mBox);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString(fitText(tm.matchPct, g.getFontMetrics(), innerW - 24), cx + pad + 12, matchBoxY + 19);
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        int mLineY = matchBoxY + 35;
        for (String line : wrapLimited(tm.matchDesc, g.getFontMetrics(), innerW - 24, 2)) {
            g.drawString(line, cx + pad + 12, mLineY);
            mLineY += 15;
        }

        // Portfolio link (ellipsized)
        int linkY = matchBoxY + matchBoxH + 22;
        if (tm.portfolio != null && !tm.portfolio.trim().isEmpty()) {
            drawLinkIcon(g, cx + pad, linkY - 10, 12, DashboardTheme.TEXT_MUTED);
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            String link = fitText(tm.portfolio.trim(), g.getFontMetrics(), innerW - 18);
            g.drawString(link, cx + pad + 18, linkY);
            tm.linkX = cx + pad;
            tm.linkY = linkY - 12;
            tm.linkW = g.getFontMetrics().stringWidth(link) + 20;
            tm.linkH = 16;
        } else {
            tm.linkW = 0;
            tm.linkH = 0;
        }

        // Buttons share the full width: [View profile] [+ Invite]
        int btnH = 34;
        int btnY = cy + ch - pad - btnH;
        int gap = 10;
        int half = (innerW - gap) / 2;
        tm.viewProfX = cx + pad;
        tm.viewProfY = btnY;
        tm.viewProfW = half;
        tm.viewProfH = btnH;
        RoundRectangle2D vpShape = new RoundRectangle2D.Float(tm.viewProfX, btnY, half, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(vpShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(vpShape);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        FontMetrics btf = g.getFontMetrics();
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("View profile", tm.viewProfX + (half - btf.stringWidth("View profile")) / 2, btnY + 22);

        tm.inviteBtnX = tm.viewProfX + half + gap;
        tm.inviteBtnY = btnY;
        tm.inviteBtnW = innerW - half - gap;
        tm.inviteBtnH = btnH;
        RoundRectangle2D invShape = new RoundRectangle2D.Float(tm.inviteBtnX, btnY, tm.inviteBtnW, btnH, 10, 10);
        g.setColor(tm.invited ? new Color(0xDC, 0xFC, 0xE7) : DashboardTheme.BRAND_PURPLE);
        g.fill(invShape);
        g.setColor(tm.invited ? new Color(0x15, 0x80, 0x3D) : Color.WHITE);
        String invTxt = tm.invited ? "✓ Invited" : "+ Invite";
        g.drawString(invTxt, tm.inviteBtnX + (tm.inviteBtnW - btf.stringWidth(invTxt)) / 2, btnY + 22);

        tm.requestBtnW = 0;
        tm.requestBtnH = 0;
        tm.requestBtnX = -9999;
        tm.requestBtnY = -9999;
    }

    private void paintAvatar(Graphics2D g, int x, int y, int size, TeammateCard tm, int fontSize) {
        g.setColor(tm.avatarBg != null ? tm.avatarBg : DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(x, y, size, size);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        g.setColor(tm.avatarText != null ? tm.avatarText : DashboardTheme.BRAND_PURPLE);
        FontMetrics fm = g.getFontMetrics();
        String ini = tm.initials == null ? "" : tm.initials;
        g.drawString(ini, x + (size - fm.stringWidth(ini)) / 2, y + (size + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static String joinNonEmpty(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p.trim());
        }
        return sb.toString();
    }

    private static String fitText(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (maxW <= 0) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    private static List<String> wrapLimited(String text, FontMetrics fm, int maxW, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return lines;
        List<String> all = Draw.wrapText(text.trim(), fm, maxW);
        for (int i = 0; i < all.size() && i < maxLines; i++) {
            String line = all.get(i);
            if (i == maxLines - 1 && all.size() > maxLines) line = line + " " + all.get(i + 1);
            lines.add(fitText(line, fm, maxW));
        }
        return lines;
    }

    private void drawLinkIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRect(x, y + 3, size - 3, size - 3);
        g2.drawLine(x + 5, y + size - 2, x + size, y + 1);
        g2.drawLine(x + size - 4, y + 1, x + size, y + 1);
        g2.drawLine(x + size, y + 1, x + size, y + 5);
        g2.dispose();
    }

    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        String msg = "No teammates found matching your criteria";
        int mw = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, x + (w - mw) / 2, y + 110);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String sub = "Try clearing some filters or searching for broader skills or roles.";
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
        g.drawString("Showing " + startItem + "-" + endItem + " of " + totalItems + " teammates · Matches are illustrative, not computed", x, y + 21);

        int curX = x + w - nextBtnW;
        nextBtnX = curX; nextBtnY = y;
        paintPageBtn(g, nextBtnX, nextBtnY, nextBtnW, nextBtnH, "Next →", false, currentPage >= totalPages);

        curX -= 40;
        for (int p = totalPages; p >= 1; p--) {
            int pageBtnX = curX;
            int pageBtnW = 32;
            paintPageBtn(g, pageBtnX, y, pageBtnW, nextBtnH, String.valueOf(p), p == currentPage, false);
            pageBoxes.add(new BoxHit(String.valueOf(p), pageBtnX, y, pageBtnW, nextBtnH));
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

    // =========================================================================
    // DROPDOWNS & MODAL OVERLAYS (Highest Z-Index)
    // =========================================================================

    public void paintOverlays(Graphics2D g, int screenW, int screenH) {
        // Dropdown Overlay
        if (activeDropdown != DropdownType.NONE) {
            switch (activeDropdown) {
                case SKILLS:
                    overlayItems = new String[]{"All", "Figma", "React", "Node.js", "Python", "TypeScript", "PostgreSQL", "Docker"};
                    paintGenericMenu(g, skillDropX, skillDropY + skillDropH + 4, 150, overlayItems, filterSkill);
                    break;
                case EXPERIENCE:
                    overlayItems = new String[]{"All", "Beginner", "Intermediate", "Advanced"};
                    paintGenericMenu(g, expDropX, expDropY + expDropH + 4, 140, overlayItems, filterExperience);
                    break;
                case AVAILABILITY:
                    overlayItems = new String[]{"All", "5-8 hrs / week", "8-12 hrs / week", "10-15 hrs / week"};
                    paintGenericMenu(g, availDropX, availDropY + availDropH + 4, 170, overlayItems, filterAvailability);
                    break;
                case RATING:
                    overlayItems = new String[]{"All", "4.5+", "4.7+", "4.8+", "4.9+"};
                    paintGenericMenu(g, ratingDropX, ratingDropY + ratingDropH + 4, 120, overlayItems, filterRating);
                    break;
                case SORT:
                    overlayItems = new String[]{"Best sample match", "Highest rating", "Most reviews", "Most available"};
                    paintGenericMenu(g, sortDropX, sortDropY + sortDropH + 4, 195, overlayItems, selectedSort);
                    break;
                default:
                    break;
            }
        }

        // Profile Modal
        if (activeProfileTeammate != null) {
            paintTeammateProfileModal(g, screenW, screenH);
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

    // Profile modal geometry (for outside-click + footer close)
    private int profModalX, profModalY, profModalW, profModalH;
    private int modalCancelBtnX, modalCancelBtnY, modalCancelBtnW, modalCancelBtnH;

    private void paintTeammateProfileModal(Graphics2D g, int sw, int sh) {
        TeammateCard tm = activeProfileTeammate;
        if (tm == null) return;

        g.setColor(new Color(15, 23, 42, 150));
        g.fillRect(0, 0, sw, sh);

        int mw = Math.min(620, sw - 48);
        // measure on a scratch canvas, shrinking the bio until the modal fits the screen
        int bioLines = 10;
        int mh;
        while (true) {
            java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = scratch.createGraphics();
            mg.setRenderingHints(g.getRenderingHints());
            mh = layoutProfileBody(mg, 0, 0, mw, tm, bioLines) + 8 + 80;
            mg.dispose();
            if (mh <= sh - 48 || bioLines <= 2) break;
            bioLines--;
        }
        mh = Math.min(mh, sh - 24);
        int mx = (sw - mw) / 2;
        int my = Math.max(12, (sh - mh) / 2);
        profModalX = mx; profModalY = my; profModalW = mw; profModalH = mh;

        for (int i = 0; i < 4; i++) {
            int off = (i + 1) * 3;
            g.setColor(new Color(15, 23, 42, Math.max(4, 18 - i * 4)));
            g.fill(new RoundRectangle2D.Float(mx - off / 2f, my + off, mw + off, mh + off / 2f, 24, 24));
        }
        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 20, 20);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(mShape);

        // cover band
        Shape oldClip = g.getClip();
        g.clip(mShape);
        g.setColor(new Color(0xF3, 0xF0, 0xFF));
        g.fillRect(mx, my, mw, 72);
        g.setClip(oldClip);

        // close (vector ✕)
        modalCloseSize = 32;
        modalCloseX = mx + mw - modalCloseSize - 16;
        modalCloseY = my + 16;
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(modalCloseX, modalCloseY, modalCloseSize, modalCloseSize, 10, 10));
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(modalCloseX, modalCloseY, modalCloseSize, modalCloseSize, 10, 10));
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(modalCloseX + 11, modalCloseY + 11, modalCloseX + 21, modalCloseY + 21);
        g.drawLine(modalCloseX + 21, modalCloseY + 11, modalCloseX + 11, modalCloseY + 21);

        layoutProfileBody(g, mx, my, mw, tm, bioLines);

        // footer
        int footY = my + mh - 80;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(mx + 1, footY, mw - 2, 79, 20, 20));
        g.fillRect(mx + 1, footY, mw - 2, 30);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(mx, footY, mx + mw, footY);

        int btnH = 42;
        int btnY = footY + 19;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        FontMetrics bfm = g.getFontMetrics();
        String inv = tm.invited ? "✓  Invited to a project" : "+  Invite to a project";
        modalInviteBtnW = bfm.stringWidth(inv) + 40;
        modalInviteBtnH = btnH;
        modalInviteBtnX = mx + mw - 30 - modalInviteBtnW;
        modalInviteBtnY = btnY;
        RoundRectangle2D invB = new RoundRectangle2D.Float(modalInviteBtnX, btnY, modalInviteBtnW, btnH, 10, 10);
        g.setColor(tm.invited ? new Color(0xDC, 0xFC, 0xE7) : DashboardTheme.BRAND_PURPLE);
        g.fill(invB);
        g.setColor(tm.invited ? new Color(0x15, 0x80, 0x3D) : Color.WHITE);
        g.drawString(inv, modalInviteBtnX + (modalInviteBtnW - bfm.stringWidth(inv)) / 2, btnY + 26);

        modalCancelBtnW = bfm.stringWidth("Close") + 48;
        modalCancelBtnH = btnH;
        modalCancelBtnX = modalInviteBtnX - 10 - modalCancelBtnW;
        modalCancelBtnY = btnY;
        RoundRectangle2D cl = new RoundRectangle2D.Float(modalCancelBtnX, btnY, modalCancelBtnW, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(cl);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.draw(cl);
        g.setColor(new Color(0x33, 0x41, 0x55));
        g.drawString("Close", modalCancelBtnX + (modalCancelBtnW - bfm.stringWidth("Close")) / 2, btnY + 26);

        if (tm.invited) {
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_LIGHT);
            String hint = fitText("Click again to cancel the invite.", g.getFontMetrics(), modalCancelBtnX - 12 - (mx + 30));
            g.drawString(hint, mx + 30, btnY + 26);
        }
    }

    /** Profile modal body (header, match, about, skills, links); returns its bottom y. */
    private int layoutProfileBody(Graphics2D g, int mx, int my, int mw, TeammateCard tm, int bioLines) {
        int pad = 30;
        int innerW = mw - pad * 2;

        // avatar overlapping the cover band
        int av = 84;
        int avX = mx + pad;
        int avY = my + 30;
        g.setColor(Color.WHITE);
        g.fillOval(avX - 4, avY - 4, av + 8, av + 8);
        paintAvatar(g, avX, avY, av, tm, 26);

        int textX = avX + av + 18;
        int textW = mx + mw - pad - textX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(fitText(tm.name, g.getFontMetrics(), textW - 40), textX, my + 102);
        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        int ty = my + 122;
        for (String line : wrapLimited(joinNonEmpty("  ·  ", tm.role, tm.university), g.getFontMetrics(), textW, 2)) {
            g.drawString(line, textX, ty);
            ty += 18;
        }

        // pills: availability, rating, experience
        int py = Math.max(avY + av, ty - 4) + 16;
        int px = mx + pad;
        boolean available = tm.availability != null && tm.availability.toLowerCase().startsWith("available");
        px = pill(g, px, py, "● " + tm.availability, available ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xF1, 0xF5, 0xF9),
                available ? new Color(0x15, 0x80, 0x3D) : new Color(0x64, 0x74, 0x8B), mx + mw - pad) + 8;
        String rating = tm.reviews > 0 ? "★ " + String.format(java.util.Locale.US, "%.1f", tm.rating) + "  ·  " + tm.reviews
                + (tm.reviews == 1 ? " review" : " reviews") : "No reviews yet";
        px = pill(g, px, py, rating, new Color(0xFF, 0xFA, 0xEC), new Color(0xB4, 0x53, 0x09), mx + mw - pad) + 8;
        if (tm.experience != null && !tm.experience.isEmpty())
            pill(g, px, py, tm.experience, new Color(0xF3, 0xF0, 0xFF), new Color(0x7C, 0x5C, 0xFF), mx + mw - pad);
        int curY = py + 26 + 22;

        // match card
        if (tm.matchPct != null && !tm.matchPct.isEmpty()) {
            g.setFont(DashboardTheme.FONT_REG_12);
            List<String> why = wrapLimited(tm.matchDesc, g.getFontMetrics(), innerW - 28, 3);
            int boxH = 36 + why.size() * 17 + 6;
            RoundRectangle2D box = new RoundRectangle2D.Float(mx + pad, curY, innerW, boxH, 12, 12);
            g.setColor(DashboardTheme.PURPLE_TINT_BG);
            g.fill(box);
            g.setColor(new Color(0xF8, 0xBC, 0xD4));
            g.setStroke(new BasicStroke(1f));
            g.draw(box);
            g.setFont(DashboardTheme.FONT_BOLD_13);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawString(tm.matchPct, mx + pad + 14, curY + 24);
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(new Color(0x33, 0x41, 0x55));
            int ly = curY + 42;
            for (String line : why) {
                g.drawString(line, mx + pad + 14, ly);
                ly += 17;
            }
            curY += boxH + 22;
        }

        // about
        curY = sectionLabel(g, mx + pad, curY, "About");
        boolean hasBio = tm.bio != null && !tm.bio.trim().isEmpty();
        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(hasBio ? new Color(0x33, 0x41, 0x55) : DashboardTheme.TEXT_LIGHT);
        for (String line : wrapLimited(hasBio ? tm.bio : "This student hasn't written a bio yet.", g.getFontMetrics(), innerW, bioLines)) {
            g.drawString(line, mx + pad, curY + 14);
            curY += 21;
        }
        curY += 20;

        // skills (wrapping chips)
        if (!tm.skills.isEmpty()) {
            curY = sectionLabel(g, mx + pad, curY, "Skills");
            g.setFont(DashboardTheme.FONT_BOLD_11);
            FontMetrics fm = g.getFontMetrics();
            int cx = mx + pad;
            int cy = curY;
            for (String s : tm.skills) {
                String label = fitText(s, fm, innerW - 24);
                int cw = fm.stringWidth(label) + 24;
                if (cx > mx + pad && cx + cw > mx + pad + innerW) {
                    cx = mx + pad;
                    cy += 36;
                }
                RoundRectangle2D chip = new RoundRectangle2D.Float(cx, cy, cw, 28, 10, 10);
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fill(chip);
                g.setColor(new Color(0xE2, 0xE8, 0xF0));
                g.draw(chip);
                g.setColor(new Color(0x33, 0x41, 0x55));
                g.drawString(label, cx + 12, cy + 18);
                cx += cw + 8;
            }
            curY = cy + 28 + 20;
        }

        // portfolio
        if (tm.portfolio != null && !tm.portfolio.trim().isEmpty()) {
            curY = sectionLabel(g, mx + pad, curY, "Portfolio");
            drawLinkIcon(g, mx + pad, curY + 3, 12, DashboardTheme.BRAND_PURPLE);
            g.setFont(DashboardTheme.FONT_REG_13);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawString(fitText(tm.portfolio.trim(), g.getFontMetrics(), innerW - 20), mx + pad + 20, curY + 14);
            curY += 22;
        }
        return curY;
    }

    private int sectionLabel(Graphics2D g, int x, int y, String label) {
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x33, 0x41, 0x55));
        g.drawString(label, x, y + 12);
        return y + 24;
    }

    /** Rounded pill clipped to maxX; returns its right edge. */
    private int pill(Graphics2D g, int x, int y, String text, Color bg, Color fg, int maxX) {
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        String t = fitText(text, fm, maxX - x - 20);
        if (t.isEmpty()) return x;
        int w = fm.stringWidth(t) + 20;
        g.setColor(bg);
        g.fill(new RoundRectangle2D.Float(x, y, w, 26, 26, 26));
        g.setColor(fg);
        g.drawString(t, x + 10, y + 17);
        return x + w;
    }

    // =========================================================================
    // EVENT HANDLING
    // =========================================================================

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // 1. Profile Modal click
        if (activeProfileTeammate != null) {
            // Close button
            if (mx >= modalCloseX - 6 && mx <= modalCloseX + modalCloseSize && my >= modalCloseY - 6 && my <= modalCloseY + modalCloseSize) {
                activeProfileTeammate = null;
                return true;
            }
            // Invite button
            if (mx >= modalInviteBtnX && mx <= modalInviteBtnX + modalInviteBtnW && my >= modalInviteBtnY && my <= modalInviteBtnY + modalInviteBtnH) {
                handleInviteTeammate(dashboard, activeProfileTeammate);
                return true;
            }
            // Footer "Close" button, or a click outside the modal, closes it
            boolean onClose = mx >= modalCancelBtnX && mx <= modalCancelBtnX + modalCancelBtnW
                    && my >= modalCancelBtnY && my <= modalCancelBtnY + modalCancelBtnH;
            boolean outside = mx < profModalX || mx > profModalX + profModalW || my < profModalY || my > profModalY + profModalH;
            if (onClose || outside) {
                activeProfileTeammate = null;
            }
            return true;
        }

        // 2. Dropdown Overlay click
        if (activeDropdown != DropdownType.NONE) {
            if (mx >= overlayX && mx <= overlayX + overlayW && my >= overlayY && my <= overlayY + overlayH) {
                int itemH = 34;
                int clickedIdx = (my - overlayY - 6) / itemH;
                if (clickedIdx >= 0 && clickedIdx < overlayItems.length) {
                    String chosen = overlayItems[clickedIdx];
                    if (activeDropdown == DropdownType.SKILLS) {
                        filterSkill = chosen;
                    } else if (activeDropdown == DropdownType.EXPERIENCE) {
                        filterExperience = chosen;
                    } else if (activeDropdown == DropdownType.AVAILABILITY) {
                        filterAvailability = chosen;
                    } else if (activeDropdown == DropdownType.RATING) {
                        filterRating = chosen;
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

        // 3. Search Box Focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 4. Dropdowns Toggles
        if (mx >= skillDropX && mx <= skillDropX + skillDropW && my >= skillDropY && my <= skillDropY + skillDropH) {
            activeDropdown = (activeDropdown == DropdownType.SKILLS) ? DropdownType.NONE : DropdownType.SKILLS;
            return true;
        }
        if (mx >= expDropX && mx <= expDropX + expDropW && my >= expDropY && my <= expDropY + expDropH) {
            activeDropdown = (activeDropdown == DropdownType.EXPERIENCE) ? DropdownType.NONE : DropdownType.EXPERIENCE;
            return true;
        }
        if (mx >= availDropX && mx <= availDropX + availDropW && my >= availDropY && my <= availDropY + availDropH) {
            activeDropdown = (activeDropdown == DropdownType.AVAILABILITY) ? DropdownType.NONE : DropdownType.AVAILABILITY;
            return true;
        }
        if (mx >= ratingDropX && mx <= ratingDropX + ratingDropW && my >= ratingDropY && my <= ratingDropY + ratingDropH) {
            activeDropdown = (activeDropdown == DropdownType.RATING) ? DropdownType.NONE : DropdownType.RATING;
            return true;
        }
        if (mx >= sortDropX && mx <= sortDropX + sortDropW && my >= sortDropY && my <= sortDropY + sortDropH) {
            activeDropdown = (activeDropdown == DropdownType.SORT) ? DropdownType.NONE : DropdownType.SORT;
            return true;
        }

        // 5. "Available to collaborate" toggle chip
        if (mx >= availChipX && mx <= availChipX + availChipW && my >= availChipY && my <= availChipY + availChipH) {
            filterOnlyAvailable = !filterOnlyAvailable;
            applyFilters();
            dashboard.showToast(filterOnlyAvailable ? "Showing only available teammates" : "Showing all teammates");
            return true;
        }

        // 6. "Clear filters" button
        if (mx >= clearFiltersX && mx <= clearFiltersX + clearFiltersW && my >= clearFiltersY && my <= clearFiltersY + clearFiltersH) {
            searchKeyword = "";
            filterSkill = "All";
            filterExperience = "All";
            filterAvailability = "All";
            filterRating = "All";
            filterOnlyAvailable = false;
            selectedSort = "Best sample match";
            applyFilters();
            dashboard.showToast("All filters cleared.");
            return true;
        }

        // 7. Cards Interaction
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredTeammates.size());

        for (int i = startIndex; i < endIndex; i++) {
            TeammateCard tm = filteredTeammates.get(i);

            // View Profile button
            if (mx >= tm.viewProfX && mx <= tm.viewProfX + tm.viewProfW && my >= tm.viewProfY && my <= tm.viewProfY + tm.viewProfH) {
                activeProfileTeammate = tm;
                return true;
            }

            // + Invite button
            if (mx >= tm.inviteBtnX && mx <= tm.inviteBtnX + tm.inviteBtnW && my >= tm.inviteBtnY && my <= tm.inviteBtnY + tm.inviteBtnH) {
                handleInviteTeammate(dashboard, tm);
                return true;
            }

            // Portfolio Link
            if (mx >= tm.linkX && mx <= tm.linkX + tm.linkW && my >= tm.linkY && my <= tm.linkY + tm.linkH) {
                dashboard.showToast("Opening " + tm.portfolio + "...");
                return true;
            }

            // Skill tags click
            for (TeammateCard.SkillHitbox sk : tm.skillHits) {
                if (mx >= sk.x && mx <= sk.x + sk.w && my >= sk.y && my <= sk.y + sk.h) {
                    filterSkill = sk.name;
                    applyFilters();
                    dashboard.showToast("Filtered by skill: " + sk.name);
                    return true;
                }
            }

            // Card Body click -> opens View Profile
            if (mx >= tm.hitX && mx <= tm.hitX + tm.hitW && my >= tm.hitY && my <= tm.hitY + tm.hitH) {
                activeProfileTeammate = tm;
                return true;
            }
        }

        // 8. Pagination Clicks
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredTeammates.size() / itemsPerPage));
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
        for (BoxHit pb : pageBoxes) {
            if (mx >= pb.x && mx <= pb.x + pb.w && my >= pb.y && my <= pb.y + pb.h) {
                currentPage = Integer.parseInt(pb.label);
                dashboard.showToast("Page " + currentPage);
                return true;
            }
        }

        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        return false;
    }

    public boolean onKey(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            if (activeProfileTeammate != null) {
                activeProfileTeammate = null;
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
        this.currentPage = 1;
        applyFilters();
    }

    /** Every teammate loaded on Find Teammates (read-only) — used by the header search. */
    public List<TeammateCard> getAllTeammates() {
        return Collections.unmodifiableList(allTeammates);
    }

    /** Opens the profile modal for a teammate (same as clicking "View profile"). */
    public void openProfile(TeammateCard tm) {
        activeDropdown = DropdownType.NONE;
        activeProfileTeammate = tm;
    }

    /** True while a full-screen overlay (the profile modal) covers the page. */
    public boolean hasOverlay() {
        return activeProfileTeammate != null;
    }

    private void handleInviteTeammate(StudentDashboard dashboard, TeammateCard tm) {
        if (tm == null) return;
        if (tm.invited) {
            tm.invited = false;
            DashboardSync.setInvite(dashboard, tm.remoteId, false);
            dashboard.showToast("Invite cancelled for " + tm.name);
            return;
        }
        List<DashboardMyProjects.MyProjectCard> leadProjects = dashboard.getMyProjectsView().getLeadProjects();
        if (leadProjects.isEmpty()) {
            dashboard.showToast("You need to create a project before inviting teammates! Click '+ New Project' in My Projects.");
            return;
        }
        String[] options = new String[leadProjects.size()];
        for (int i = 0; i < leadProjects.size(); i++) {
            options[i] = leadProjects.get(i).title;
        }
        String selected = (String) javax.swing.JOptionPane.showInputDialog(
                null,
                "Select which of your projects to invite " + tm.name + " to:",
                "Invite to Project",
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (selected != null) {
            DashboardMyProjects.MyProjectCard chosen = null;
            for (DashboardMyProjects.MyProjectCard lp : leadProjects) {
                if (lp.title.equals(selected)) { chosen = lp; break; }
            }
            if (chosen != null) {
                tm.invited = true;
                DashboardSync.sendProjectInvite(dashboard, tm.remoteId, chosen.remoteId);
                dashboard.showToast("Invited " + tm.name + " to " + chosen.title + "! 🚀");
            }
        }
    }
}
