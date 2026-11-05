import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Pixel-perfect Student Workspace & Explore Projects Platform for TeamUp.
 * Matches the reference design with 100% interactive controls:
 * - Search by keyword/skill with live filtering
 * - Category quick-filter pills (All, Campus life, Education, Sustainability, Research, Social impact)
 * - Filter dropdowns (Category, Skills, Experience, Recruiting)
 * - Sort dropdown (Recommended, Most Popular, Newest, Most Members, Fewest Members)
 * - Grid and List view switcher
 * - Interactive Project Cards with bookmarks, skill tags, creator avatars, and "View details ->"
 * - Interactive Project Details modal with full info and "Apply to Join" action
 * - Interactive "+ Create project" modal with full form to add real-time new projects
 * - Interactive Notifications dropdown with mark-all-read
 * - Interactive User Profile popup
 * - Complete pagination (Previous, 1, 2, 3, Next)
 * - Full Sidebar navigation with active highlight and Back to Landing Page
 */
public class StudentDashboard {

    public interface DashboardListener {
        void returnToLanding();
        void openModal(String title, String message, boolean isRegister);
    }

    private final DashboardListener listener;

    // Viewport & scrolling
    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private int totalContentHeight = 1100;

    // Navigation State
    public enum NavItem {
        OVERVIEW("Overview", 0),
        DISCOVER_PROJECTS("Discover Projects", 0),
        FIND_TEAMMATES("Find Teammates", 0),
        MY_PROJECTS("My Projects", 0),
        MY_TEAMS("My Teams", 0),
        MESSAGES("Messages", 3),
        MY_PROFILE("My Profile", 0),
        SETTINGS("Settings", 0);

        final String label;
        final int badge;
        NavItem(String label, int badge) {
            this.label = label;
            this.badge = badge;
        }
    }

    private NavItem activeNav = NavItem.FIND_TEAMMATES;
    private NavItem hoveredNav = null;

    // Color Palette matching design tokens and reference image
    private static final Color BG_CANVAS = new Color(0xF8, 0xF9, 0xFD);
    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xEA, 0xEF, 0xF5);
    private static final Color BORDER_HOVER = new Color(0xCB, 0xD5, 0xE1);
    private static final Color BRAND_PURPLE = new Color(0x4F, 0x46, 0xE5);
    private static final Color BRAND_PURPLE_HOVER = new Color(0x43, 0x38, 0xCA);
    private static final Color PURPLE_TINT_BG = new Color(0xEE, 0xF2, 0xFF);
    private static final Color PURPLE_BORDER = new Color(0xC7, 0xD2, 0xFE);
    private static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT_LIGHT = new Color(0x94, 0xA3, 0xB8);
    private static final Color RECRUIT_BG = new Color(0xDC, 0xFC, 0xE7);
    private static final Color RECRUIT_TEXT = new Color(0x15, 0x80, 0x3D);

    // Project Model
    public static class Project {
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
        public boolean applied = false;

        public Project(int id, String title, String category, String experience, String description,
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

    private final List<Project> allProjects = new ArrayList<>();
    private final List<Project> filteredProjects = new ArrayList<>();

    // Category Filter Chips
    private final String[] categoryChips = {
            "All projects", "Campus life", "Education", "Sustainability", "Research", "Social impact"
    };
    private String selectedCategory = "All projects";

    // Dropdown Filters
    public enum DropdownType { NONE, CATEGORY, SKILLS, EXPERIENCE, RECRUITING, SORT, NOTIFICATIONS, PROFILE,
        TM_SKILLS, TM_EXPERIENCE, TM_AVAILABILITY, TM_RATING, TM_SORT }
    private DropdownType activeDropdown = DropdownType.NONE;

    private String filterCategory = "All";
    private String filterSkill = "All";
    private String filterExperience = "All";
    private String filterRecruiting = "All";
    private String selectedSort = "Recommended";

    private final String[] skillsList = {"All", "React", "TypeScript", "Node.js", "Python", "Figma", "Maps API", "PostgreSQL"};
    private final String[] expList = {"All", "Beginner", "Intermediate", "Advanced"};
    private final String[] recruitList = {"All", "Recruiting", "Team Full"};
    private final String[] sortList = {"Recommended", "Most Popular", "Newest", "Most Members", "Fewest Members"};

    // Teammate Filters
    private String filterTmSkill = "All";
    private String filterTmExperience = "All";
    private String filterTmAvailability = "All";
    private String filterTmRating = "All";
    private String selectedTmSort = "Best sample match";
    private boolean filterOnlyAvailable = true;

    private final String[] tmSkillsList = {"All", "Figma", "React", "Node.js", "Python", "PostgreSQL", "TypeScript", "Testing", "Docker"};
    private final String[] tmExpList = {"All", "Beginner", "Intermediate", "Advanced"};
    private final String[] tmAvailList = {"All", "5-8 hrs / week", "8-12 hrs / week", "10-15 hrs / week"};
    private final String[] tmRatingList = {"All", "4.7+", "4.8+", "4.9+"};
    private final String[] tmSortList = {"Best sample match", "Highest rating", "Most reviews", "Most available"};

    // Teammate Model
    public static class Teammate {
        public int id;
        public String name;
        public String role;
        public String university;
        public String initials;
        public Color avatarColor;
        public String availability;
        public double rating;
        public int reviews;
        public List<String> skills = new ArrayList<>();
        public String matchPct;
        public String matchDesc;
        public String portfolio;
        public String experience;
        public boolean invited = false;
        public boolean requested = false;

        public Teammate(int id, String name, String role, String university, String initials, Color avatarColor,
                        String availability, double rating, int reviews, String[] skillsArr,
                        String matchPct, String matchDesc, String portfolio, String experience) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.university = university;
            this.initials = initials;
            this.avatarColor = avatarColor;
            this.availability = availability;
            this.rating = rating;
            this.reviews = reviews;
            Collections.addAll(this.skills, skillsArr);
            this.matchPct = matchPct;
            this.matchDesc = matchDesc;
            this.portfolio = portfolio;
            this.experience = experience;
        }
    }

    private final List<Teammate> allTeammates = new ArrayList<>();
    private final List<Teammate> filteredTeammates = new ArrayList<>();
    private Teammate teammateModalUser = null;

    // Teammate Pagination & Search
    private String teammateSearchText = "";
    private boolean teammateSearchFocused = false;
    private int teammateCurrentPage = 1;
    private final int teammatesPerPage = 6;

    // Layout View
    private boolean isGridView = true;

    // Pagination
    private int currentPage = 1;
    private final int itemsPerPage = 6;

    // Interactive Searches
    private String topSearchText = "";
    private boolean topSearchFocused = false;
    private String contentSearchText = "";
    private boolean contentSearchFocused = false;

    // Notification Items
    public static class NotificationItem {
        String title;
        String time;
        boolean unread;
        NotificationItem(String title, String time, boolean unread) {
            this.title = title;
            this.time = time;
            this.unread = unread;
        }
    }
    private final List<NotificationItem> notifications = new ArrayList<>();

    // Overview Page Models
    public static class TaskItem {
        public String title;
        public String deadline;
        public boolean overdue;
        public String project;
        public boolean completed;
        public TaskItem(String title, String deadline, boolean overdue, String project) {
            this.title = title;
            this.deadline = deadline;
            this.overdue = overdue;
            this.project = project;
            this.completed = false;
        }
    }
    private final List<TaskItem> upcomingTasks = new ArrayList<>();

    public static class TeammateMatch {
        public String name;
        public String initials;
        public Color avatarColor;
        public String role;
        public String matchPct;
        public boolean invited;
        public TeammateMatch(String name, String initials, Color avatarColor, String role, String matchPct) {
            this.name = name;
            this.initials = initials;
            this.avatarColor = avatarColor;
            this.role = role;
            this.matchPct = matchPct;
            this.invited = false;
        }
    }
    private final List<TeammateMatch> suggestedTeammates = new ArrayList<>();

    public static class ActivityItem {
        public String name;
        public String initials;
        public Color avatarColor;
        public String detail;
        public String badge;
        public Color badgeBg;
        public Color badgeText;
        public ActivityItem(String name, String initials, Color avatarColor, String detail, String badge, Color badgeBg, Color badgeText) {
            this.name = name;
            this.initials = initials;
            this.avatarColor = avatarColor;
            this.detail = detail;
            this.badge = badge;
            this.badgeBg = badgeBg;
            this.badgeText = badgeText;
        }
    }
    private final List<ActivityItem> recentActivities = new ArrayList<>();

    // Modals
    private Project detailModalProject = null;
    private boolean showCreateModal = false;
    private String newProjTitle = "";
    private String newProjCategory = "Campus life";
    private String newProjSkills = "React, Node.js";
    private String newProjRoles = "Frontend developer · UI Designer";
    private String newProjDesc = "";
    private int newProjMembers = 4;
    private int createModalActiveField = 0; // 0=Title, 1=Skills, 2=Roles, 3=Desc

    // Toasts
    private String toastMessage = "";
    private float toastTimer = 0f;

    // Hitbox Rectangles for Mouse Interaction
    private final Map<String, Rectangle> hitboxes = new HashMap<>();
    private final Map<String, Object> hitboxPayloads = new HashMap<>();
    private String hoveredHitbox = null;

    // Caret blink
    private float caretTimer = 0f;

    public StudentDashboard(DashboardListener listener) {
        this.listener = listener;
        initDemoData();
        applyFilters();
        applyTeammateFilters();
    }

    private void initDemoData() {
        // 1. CampusConnect
        allProjects.add(new Project(
                1, "CampusConnect", "Campus life", "Intermediate",
                "One place to discover university events, meet people and make campus feel connected.",
                new String[]{"React", "TypeScript", "Node.js"},
                true, 5, 7, "Open: Frontend developer · QA engineer",
                "Priya Shah", "PS", new Color(0x8B, 0x5C, 0xF6),
                0, new Color(0xEE, 0xF2, 0xFF), new Color(0x63, 0x66, 0xF1)
        ));

        // 2. GreenRoute
        allProjects.add(new Project(
                2, "GreenRoute", "Sustainability", "Intermediate",
                "Make low-carbon campus travel easier with shared routes and real-time mobility insights.",
                new String[]{"React", "Maps API", "Node.js"},
                true, 4, 6, "Open: Frontend developer · Data analyst",
                "Maya Patel", "MP", new Color(0x3B, 0x82, 0xF6),
                1, new Color(0xEC, 0xFD, 0xF5), new Color(0x10, 0xB9, 0x81)
        ));

        // 3. OpenLab
        allProjects.add(new Project(
                3, "OpenLab", "Research", "Intermediate",
                "Help students discover research opportunities and collaborate across university labs.",
                new String[]{"TypeScript", "PostgreSQL", "Figma"},
                true, 3, 5, "Open: Product designer · Backend developer",
                "Noah Williams", "NW", new Color(0x63, 0x66, 0xF1),
                2, new Color(0xF3, 0xE8, 0xFF), new Color(0x8B, 0x5C, 0xF6)
        ));

        // 4. AccessMap
        allProjects.add(new Project(
                4, "AccessMap", "Social impact", "Intermediate",
                "Build a community-powered map of accessible spaces and routes around campus.",
                new String[]{"React Native", "Maps API"},
                true, 3, 5, "Open: Mobile developer · UX researcher",
                "Leila Hassan", "LH", new Color(0xEC, 0x48, 0x99),
                3, new Color(0xE0, 0xF2, 0xFE), new Color(0x0E, 0xA5, 0xE9)
        ));

        // 5. PeerPrep
        allProjects.add(new Project(
                5, "PeerPrep", "Education", "Intermediate",
                "Turn interview practice into a shared learning experience with peer feedback and goals.",
                new String[]{"Next.js", "Prisma", "Tailwind"},
                true, 2, 4, "Open: Full-stack developer · Product designer",
                "Oliver Brooks", "OB", new Color(0x8B, 0x5C, 0xF6),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)
        ));

        // 6. FoodLoop
        allProjects.add(new Project(
                6, "FoodLoop", "Sustainability", "Intermediate",
                "Connect campus cafés with students to reduce food waste, one shared meal at a time.",
                new String[]{"React", "Node.js", "Figma"},
                true, 4, 6, "Open: Frontend developer · QA engineer",
                "Aisha Rahman", "AR", new Color(0x14, 0xB8, 0xA6),
                5, new Color(0xF0, 0xFD, 0xFA), new Color(0x0D, 0x94, 0x88)
        ));

        // Page 2 Projects (6 realistic demo projects)
        allProjects.add(new Project(
                7, "StudySync", "Education", "Beginner",
                "Collaborative note-sharing and AI-powered study group matcher for STEM classes.",
                new String[]{"Python", "FastAPI", "React"},
                true, 3, 5, "Open: Python backend · UI designer",
                "Marcus Chen", "MC", new Color(0x10, 0xB9, 0x81),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x4F, 0x46, 0xE5)
        ));
        allProjects.add(new Project(
                8, "SkillSwap", "Campus life", "Intermediate",
                "Peer tutoring exchange platform where students trade coding tips for languages.",
                new String[]{"Vue.js", "Firebase", "Tailwind"},
                true, 2, 4, "Open: Full-stack engineer · Community lead",
                "Sarah Jenkins", "SJ", new Color(0xF5, 0x9E, 0x0B),
                0, new Color(0xFE, 0xF3, 0xC7), new Color(0xD9, 0x77, 0x06)
        ));
        allProjects.add(new Project(
                9, "SolarCampus", "Sustainability", "Advanced",
                "IoT sensor monitoring of rooftop solar panels across engineering facilities.",
                new String[]{"Python", "C++", "PostgreSQL"},
                true, 4, 5, "Open: Embedded systems · Data pipeline",
                "Daniel Kim", "DK", new Color(0xEF, 0x44, 0x44),
                1, new Color(0xEC, 0xFD, 0xF5), new Color(0x10, 0xB9, 0x81)
        ));
        allProjects.add(new Project(
                10, "MentalWell", "Social impact", "Intermediate",
                "Anonymous peer support chat and mental wellness reflection tool for students.",
                new String[]{"React Native", "Node.js"},
                true, 3, 6, "Open: React Native · Content strategist",
                "Fatima Zahra", "FZ", new Color(0x8B, 0x5C, 0xF6),
                3, new Color(0xFA, 0xE8, 0xFF), new Color(0xA8, 0x55, 0xF7)
        ));
        allProjects.add(new Project(
                11, "QuantumLearn", "Research", "Advanced",
                "Interactive visualizer for introductory quantum computing circuits and gates.",
                new String[]{"Python", "TypeScript", "Figma"},
                true, 2, 3, "Open: Algorithm researcher · Frontend dev",
                "Lucas Vance", "LV", new Color(0x3B, 0x82, 0xF6),
                2, new Color(0xEE, 0xF2, 0xFF), new Color(0x25, 0x63, 0xEB)
        ));
        allProjects.add(new Project(
                12, "BookCycle", "Campus life", "Beginner",
                "Student textbook exchange & lending marketplace with locker pickup.",
                new String[]{"React", "TypeScript"},
                true, 3, 4, "Open: Frontend engineer · Mobile dev",
                "Chloe Bennett", "CB", new Color(0x06, 0xB6, 0xD4),
                0, new Color(0xCF, 0xFA, 0xFE), new Color(0x08, 0x91, 0xB2)
        ));

        // Page 3 Projects (6 realistic demo projects)
        allProjects.add(new Project(
                13, "RoboRescue", "Research", "Advanced",
                "Autonomous drone navigation algorithms for indoor obstacle avoidance.",
                new String[]{"C++", "Python"},
                true, 4, 6, "Open: Robotics researcher · Systems dev",
                "Ethan Hunt", "EH", new Color(0x4F, 0x46, 0xE5),
                2, new Color(0xF3, 0xE8, 0xFF), new Color(0x7C, 0x3A, 0xED)
        ));
        allProjects.add(new Project(
                14, "CivicVoice", "Social impact", "Intermediate",
                "Local government policy tracker and student voting information assistant.",
                new String[]{"React", "Python"},
                true, 2, 4, "Open: Full-stack developer · Policy researcher",
                "Zoe Martinez", "ZM", new Color(0x10, 0xB9, 0x81),
                3, new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69)
        ));
        allProjects.add(new Project(
                15, "DormChef", "Campus life", "Beginner",
                "Quick and healthy student recipe builder using common dormitory ingredients.",
                new String[]{"React", "Figma"},
                true, 3, 5, "Open: UI/UX Designer · Frontend dev",
                "Ryan Cooper", "RC", new Color(0xF9, 0x73, 0x16),
                5, new Color(0xFF, 0xED, 0xD5), new Color(0xEA, 0x58, 0x0C)
        ));
        allProjects.add(new Project(
                16, "EcoTrack", "Sustainability", "Intermediate",
                "Gamified personal carbon footprint tracking challenge with leaderboards.",
                new String[]{"TypeScript", "Node.js", "PostgreSQL"},
                true, 4, 5, "Open: Backend architect · QA engineer",
                "Emma Wilson", "EW", new Color(0x14, 0xB8, 0xA6),
                1, new Color(0xCC, 0xFB, 0xF1), new Color(0x0F, 0x76, 0x6E)
        ));
        allProjects.add(new Project(
                17, "CodeMentors", "Education", "Intermediate",
                "Peer-to-peer programming mentorship and weekly code review matchmaker.",
                new String[]{"TypeScript", "React"},
                true, 2, 4, "Open: Frontend engineer · Community manager",
                "Liam Davis", "LD", new Color(0x63, 0x66, 0xF1),
                4, new Color(0xEE, 0xF2, 0xFF), new Color(0x43, 0x38, 0xCA)
        ));
        allProjects.add(new Project(
                18, "BioHub", "Research", "Advanced",
                "Genomics data visualization and bioinformatics pipeline runner for capstone lab.",
                new String[]{"Python", "PostgreSQL", "Figma"},
                true, 3, 5, "Open: Bioinformatics dev · UX designer",
                "Maya Lin", "ML", new Color(0x8B, 0x5C, 0xF6),
                2, new Color(0xF3, 0xE8, 0xFF), new Color(0x6D, 0x28, 0xD9)
        ));

        // Notifications
        notifications.add(new NotificationItem("Priya Shah accepted your invite to CampusConnect", "10m ago", true));
        notifications.add(new NotificationItem("Maya Patel sent a message regarding GreenRoute", "1h ago", true));
        notifications.add(new NotificationItem("New project matching your skills: OpenLab", "3h ago", true));

        // Overview Demo Data: Activities
        recentActivities.add(new ActivityItem("Priya Shah", "PS", new Color(0x8B, 0x5C, 0xF6), "", "Design", new Color(0xEE, 0xF2, 0xFF), BRAND_PURPLE));
        recentActivities.add(new ActivityItem("Daniel Kim", "DK", new Color(0x10, 0xB9, 0x81), "Completed the event API schema · 42 min ago", "Completed", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)));
        recentActivities.add(new ActivityItem("Sofia Martinez", "SM", new Color(0xEC, 0x48, 0x99), "Mentioned you in \"Build event cards\" · 1 hour ago", "Task", new Color(0xF1, 0xF5, 0xF9), new Color(0x47, 0x55, 0x69)));

        // Overview Demo Data: Suggested Teammates
        suggestedTeammates.add(new TeammateMatch("Priya Shah", "PS", new Color(0x8B, 0x5C, 0xF6), "UX/UI designer · UW", "96% sample"));
        suggestedTeammates.add(new TeammateMatch("Daniel Kim", "DK", new Color(0x10, 0xB9, 0x81), "Backend developer · UW", "92% sample"));

        // Overview Demo Data: Upcoming Deadlines
        upcomingTasks.add(new TaskItem("Finalize event API contract", "Overdue · Oct 15 · CampusConnect", true, "CampusConnect"));
        upcomingTasks.add(new TaskItem("Build event discovery cards", "Today · Oct 16 · CampusConnect", false, "CampusConnect"));
        upcomingTasks.add(new TaskItem("Accessibility review", "Mon, Oct 19 · CampusConnect", false, "CampusConnect"));

        // Teammates Demo Data (24 students matching screenshot & platform)
        allTeammates.add(new Teammate(
                1, "Priya Shah", "UX/UI designer", "University of Washington", "PS", new Color(0x8B, 0x5C, 0xF6),
                "Available · 10-15 hrs / week", 4.9, 12, new String[]{"Figma", "UX research", "Design systems"},
                "96% skill match · Sample", "Pairs your full-stack skills with user research and accessible product design.",
                "priyashah.design", "Intermediate"
        ));
        allTeammates.add(new Teammate(
                2, "Daniel Kim", "Backend developer", "University of Washington", "DK", new Color(0x10, 0xB9, 0x81),
                "Available · 8-12 hrs / week", 4.8, 9, new String[]{"Node.js", "PostgreSQL", "Docker"},
                "92% skill match · Sample", "Adds API architecture and database expertise to your React projects.",
                "github.com/danielkim", "Advanced"
        ));
        allTeammates.add(new Teammate(
                3, "Sofia Martinez", "Frontend developer", "University of Oregon", "SM", new Color(0xEC, 0x48, 0x99),
                "Available · 10 hrs / week", 4.9, 8, new String[]{"React", "TypeScript", "Accessibility"},
                "89% skill match · Sample", "Shares your TypeScript interests and brings a strong accessibility focus.",
                "sofiam.dev", "Intermediate"
        ));
        allTeammates.add(new Teammate(
                4, "Ethan Chen", "QA engineer", "University of Washington", "EC", new Color(0xF5, 0x9E, 0x0B),
                "Available · 6-10 hrs / week", 4.7, 6, new String[]{"Playwright", "Testing", "CI/CD"},
                "88% skill match · Sample", "Complements your development work with automated tests and release quality.",
                "github.com/ethanchen", "Intermediate"
        ));
        allTeammates.add(new Teammate(
                5, "Maya Patel", "Full-stack developer", "UC Berkeley", "MP", new Color(0x3B, 0x82, 0xF6),
                "Available · 8 hrs / week", 4.8, 11, new String[]{"React", "Python", "Maps API"},
                "85% skill match · Sample", "A shared interest in sustainability, with geospatial skills for new ideas.",
                "mayapatel.dev", "Advanced"
        ));
        allTeammates.add(new Teammate(
                6, "Noah Williams", "Data engineer", "Stanford University", "NW", new Color(0x63, 0x66, 0xF1),
                "Available · 5-8 hrs / week", 4.8, 7, new String[]{"Python", "PostgreSQL", "Data science"},
                "83% skill match · Sample", "Brings data modeling and research experience to your product thinking.",
                "github.com/noahw", "Advanced"
        ));

        // Page 2
        allTeammates.add(new Teammate(7, "Leila Hassan", "Mobile developer", "University of Washington", "LH", new Color(0xEC, 0x48, 0x99), "Available · 10-12 hrs / week", 4.9, 14, new String[]{"React Native", "Swift", "iOS"}, "82% skill match · Sample", "Expertise in native iOS & mobile performance for student campus apps.", "github.com/leilah", "Intermediate"));
        allTeammates.add(new Teammate(8, "Oliver Brooks", "Full-stack developer", "University of Washington", "OB", new Color(0x8B, 0x5C, 0xF6), "Available · 12-15 hrs / week", 4.8, 10, new String[]{"Next.js", "Prisma", "Tailwind"}, "80% skill match · Sample", "Rapid full-stack prototyping for student community platforms.", "oliverb.codes", "Intermediate"));
        allTeammates.add(new Teammate(9, "Aisha Rahman", "Product Designer", "University of British Columbia", "AR", new Color(0x14, 0xB8, 0xA6), "Available · 8-10 hrs / week", 4.9, 15, new String[]{"Figma", "Design systems"}, "79% skill match · Sample", "Specializes in clean user flows and accessibility audits.", "aisharahman.io", "Advanced"));
        allTeammates.add(new Teammate(10, "Marcus Chen", "AI & ML Engineer", "UC Berkeley", "MC", new Color(0x10, 0xB9, 0x81), "Available · 10 hrs / week", 4.7, 8, new String[]{"Python", "FastAPI", "PyTorch"}, "78% skill match · Sample", "Experience integrating LLMs and embeddings into student web tools.", "github.com/marcusc", "Advanced"));
        allTeammates.add(new Teammate(11, "Sarah Jenkins", "Frontend developer", "University of Washington", "SJ", new Color(0xF5, 0x9E, 0x0B), "Available · 6-8 hrs / week", 4.8, 9, new String[]{"Vue.js", "Tailwind", "CSS"}, "76% skill match · Sample", "Component-driven frontend design with responsive micro-animations.", "sarahj.dev", "Beginner"));
        allTeammates.add(new Teammate(12, "Chloe Bennett", "UX Researcher", "Stanford University", "CB", new Color(0x06, 0xB6, 0xD4), "Available · 8-12 hrs / week", 4.9, 11, new String[]{"UX research", "Figma", "User Testing"}, "75% skill match · Sample", "Conducts student user interviews and synthesizes qualitative insights.", "chloeb.design", "Intermediate"));

        // Page 3
        allTeammates.add(new Teammate(13, "Lucas Vance", "Systems Engineer", "University of Washington", "LV", new Color(0x3B, 0x82, 0xF6), "Available · 5-8 hrs / week", 4.8, 6, new String[]{"C++", "Rust", "Linux"}, "74% skill match · Sample", "Low-level systems architecture and high-throughput backend services.", "github.com/lucasv", "Advanced"));
        allTeammates.add(new Teammate(14, "Fatima Zahra", "Mobile Developer", "University of Washington", "FZ", new Color(0x8B, 0x5C, 0xF6), "Available · 10 hrs / week", 4.9, 13, new String[]{"Flutter", "Firebase", "Dart"}, "73% skill match · Sample", "Cross-platform mobile apps for Android and iOS student workflows.", "fatimaz.dev", "Intermediate"));
        allTeammates.add(new Teammate(15, "Ryan Cooper", "DevOps & Cloud", "UCLA", "RC", new Color(0xF9, 0x73, 0x16), "Available · 6-10 hrs / week", 4.7, 7, new String[]{"Docker", "CI/CD", "AWS"}, "72% skill match · Sample", "CI/CD pipeline automation and cloud container deployments.", "github.com/ryancooper", "Intermediate"));
        allTeammates.add(new Teammate(16, "Emma Wilson", "Data Scientist", "University of Washington", "EW", new Color(0x14, 0xB8, 0xA6), "Available · 8-10 hrs / week", 4.8, 12, new String[]{"Python", "PostgreSQL", "Data science"}, "70% skill match · Sample", "Statistical analysis and predictive modeling for academic research.", "emmawilson.data", "Intermediate"));
        allTeammates.add(new Teammate(17, "Liam Davis", "Security & QA", "University of Oregon", "LD", new Color(0x63, 0x66, 0xF1), "Available · 6-8 hrs / week", 4.7, 5, new String[]{"Testing", "Python", "Security"}, "69% skill match · Sample", "Web application penetration testing and secure authentication review.", "liamdavis.security", "Intermediate"));
        allTeammates.add(new Teammate(18, "Zoe Martinez", "Content & UI", "University of Washington", "ZM", new Color(0x10, 0xB9, 0x81), "Available · 8-12 hrs / week", 4.8, 9, new String[]{"Figma", "UI/UX", "Copywriting"}, "68% skill match · Sample", "Brand voice, onboarding flows, and polished design systems.", "zoemartinez.design", "Beginner"));

        // Page 4
        allTeammates.add(new Teammate(19, "Ethan Hunt", "Robotics Engineer", "Stanford University", "EH", new Color(0x4F, 0x46, 0xE5), "Available · 10-15 hrs / week", 4.9, 16, new String[]{"C++", "ROS", "Python"}, "67% skill match · Sample", "Sensor fusion and robotics algorithms for capstone hardware.", "github.com/ethanhunt", "Advanced"));
        allTeammates.add(new Teammate(20, "Maya Lin", "Bioinformatics", "UC Berkeley", "ML", new Color(0x8B, 0x5C, 0xF6), "Available · 8 hrs / week", 4.8, 8, new String[]{"Python", "PostgreSQL", "Data science"}, "66% skill match · Sample", "Genomic data pipelines and molecular modeling simulations.", "mayalin.bio", "Advanced"));
        allTeammates.add(new Teammate(21, "David Park", "Full-stack developer", "University of Washington", "DP", new Color(0x3B, 0x82, 0xF6), "Available · 10-14 hrs / week", 4.9, 11, new String[]{"React", "TypeScript", "Node.js"}, "65% skill match · Sample", "High performance microservices with modern React frontends.", "davidpark.codes", "Advanced"));
        allTeammates.add(new Teammate(22, "Grace Hopper", "Backend engineer", "University of Washington", "GH", new Color(0x10, 0xB9, 0x81), "Available · 8-10 hrs / week", 5.0, 20, new String[]{"Java", "PostgreSQL", "Docker"}, "64% skill match · Sample", "Robust Java backend services and concurrent database schemas.", "gracehopper.dev", "Advanced"));
        allTeammates.add(new Teammate(23, "James Wilson", "Frontend developer", "University of Washington", "JW", new Color(0xF5, 0x9E, 0x0B), "Available · 6-8 hrs / week", 4.6, 5, new String[]{"React", "JavaScript", "CSS"}, "62% skill match · Sample", "Clean semantic markup and accessible UI components.", "github.com/jwilson", "Beginner"));
        allTeammates.add(new Teammate(24, "Elena Rostova", "AI Research", "Stanford University", "ER", new Color(0xEC, 0x48, 0x99), "Available · 8-12 hrs / week", 4.9, 12, new String[]{"Python", "FastAPI", "Data science"}, "60% skill match · Sample", "Natural language processing and sentiment analysis pipelines.", "elenarostova.ai", "Advanced"));
    }

    private void applyTeammateFilters() {
        filteredTeammates.clear();
        String query = (teammateSearchText.isEmpty() ? topSearchText : teammateSearchText).trim().toLowerCase();

        for (Teammate t : allTeammates) {
            // Skill dropdown
            if (!filterTmSkill.equals("All")) {
                boolean hasSk = false;
                for (String s : t.skills) {
                    if (s.equalsIgnoreCase(filterTmSkill)) { hasSk = true; break; }
                }
                if (!hasSk) continue;
            }
            // Experience dropdown
            if (!filterTmExperience.equals("All") && !t.experience.equalsIgnoreCase(filterTmExperience)) {
                continue;
            }
            // Availability dropdown
            if (!filterTmAvailability.equals("All")) {
                String sub = filterTmAvailability.split(" ")[0];
                if (!t.availability.toLowerCase().contains(sub.toLowerCase())) continue;
            }
            // Rating dropdown
            if (!filterTmRating.equals("All")) {
                double minR = Double.parseDouble(filterTmRating.replace("+", ""));
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
                boolean matchPort = t.portfolio.toLowerCase().contains(query);
                boolean matchDesc = t.matchDesc.toLowerCase().contains(query);
                boolean matchSk = false;
                for (String s : t.skills) {
                    if (s.toLowerCase().contains(query)) { matchSk = true; break; }
                }
                if (!matchName && !matchRole && !matchUni && !matchPort && !matchDesc && !matchSk) {
                    continue;
                }
            }
            filteredTeammates.add(t);
        }

        // Sorting
        if ("Highest rating".equals(selectedTmSort)) {
            filteredTeammates.sort((a, b) -> Double.compare(b.rating, a.rating));
        } else if ("Most reviews".equals(selectedTmSort)) {
            filteredTeammates.sort((a, b) -> Integer.compare(b.reviews, a.reviews));
        }

        int maxP = Math.max(1, (int) Math.ceil((double) filteredTeammates.size() / teammatesPerPage));
        if (teammateCurrentPage > maxP) teammateCurrentPage = maxP;
        if (teammateCurrentPage < 1) teammateCurrentPage = 1;
    }

    public void showToast(String message) {
        this.toastMessage = message;
        this.toastTimer = 3.2f;
    }

    public void update(float dt) {
        scrollY = Anim.approach(scrollY, targetScrollY, dt, 14f);
        if (toastTimer > 0f) {
            toastTimer -= dt;
            if (toastTimer <= 0f) toastMessage = "";
        }
        caretTimer += dt;
        if (caretTimer > 1.0f) caretTimer = 0f;
    }

    private void applyFilters() {
        filteredProjects.clear();
        String query = (contentSearchText.isEmpty() ? topSearchText : contentSearchText).trim().toLowerCase();

        for (Project p : allProjects) {
            // Category Chip Filter
            if (!selectedCategory.equals("All projects") && !p.category.equalsIgnoreCase(selectedCategory)) {
                continue;
            }
            // Category Dropdown Filter
            if (!filterCategory.equals("All") && !p.category.equalsIgnoreCase(filterCategory)) {
                continue;
            }
            // Skill Dropdown Filter
            if (!filterSkill.equals("All")) {
                boolean hasSkill = false;
                for (String s : p.skills) {
                    if (s.equalsIgnoreCase(filterSkill)) { hasSkill = true; break; }
                }
                if (!hasSkill) continue;
            }
            // Experience Filter
            if (!filterExperience.equals("All") && !p.experience.equalsIgnoreCase(filterExperience)) {
                continue;
            }
            // Recruiting Filter
            if (filterRecruiting.equals("Recruiting") && !p.recruiting) continue;
            if (filterRecruiting.equals("Team Full") && p.recruiting) continue;

            // Text Search
            if (!query.isEmpty()) {
                boolean matchTitle = p.title.toLowerCase().contains(query);
                boolean matchCat = p.category.toLowerCase().contains(query);
                boolean matchDesc = p.description.toLowerCase().contains(query);
                boolean matchLead = p.creatorName.toLowerCase().contains(query);
                boolean matchSkill = false;
                for (String s : p.skills) {
                    if (s.toLowerCase().contains(query)) { matchSkill = true; break; }
                }
                if (!matchTitle && !matchCat && !matchDesc && !matchLead && !matchSkill) {
                    continue;
                }
            }

            filteredProjects.add(p);
        }

        // Sorting
        if ("Most Members".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(b.currentMembers, a.currentMembers));
        } else if ("Fewest Members".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(a.currentMembers, b.currentMembers));
        } else if ("Newest".equals(selectedSort)) {
            filteredProjects.sort((a, b) -> Integer.compare(b.id, a.id));
        }

        // Clamp page
        int maxPages = Math.max(1, (int) Math.ceil((double) filteredProjects.size() / itemsPerPage));
        if (currentPage > maxPages) currentPage = maxPages;
        if (currentPage < 1) currentPage = 1;
    }

    // =========================================================================
    // MAIN PAINT ROUTINE
    // =========================================================================
    public void paint(Graphics2D g, int width, int height) {
        hitboxes.clear();
        hitboxPayloads.clear();

        Graphics2D g2 = (Graphics2D) g.create();
        Draw.setQualityHints(g2);

        // 1. Overall Background Canvas
        g2.setColor(BG_CANVAS);
        g2.fillRect(0, 0, width, height);

        int sideW = 230;
        int headerH = 60;

        // 2. Main Scrollable Content Area (Clipped under header & right of sidebar)
        Shape oldClip = g2.getClip();
        g2.setClip(sideW, headerH, width - sideW, height - headerH);

        Graphics2D cg = (Graphics2D) g2.create();
        int contentY = headerH - Math.round(scrollY);

        if (activeNav == NavItem.FIND_TEAMMATES) {
            paintFindTeammatesContent(cg, sideW, contentY, width - sideW, height);
        } else if (activeNav == NavItem.DISCOVER_PROJECTS) {
            paintExploreProjectsContent(cg, sideW, contentY, width - sideW, height);
        } else {
            paintOverviewContent(cg, sideW, contentY, width - sideW, height);
        }
        cg.dispose();

        g2.setClip(oldClip);

        // 3. Left Fixed Sidebar
        paintSidebar(g2, 0, 0, sideW, height);

        // 4. Top Fixed Header
        paintHeader(g2, sideW, 0, width - sideW, headerH);

        // 5. Scrollbar for Content
        paintContentScrollbar(g2, width - 8, headerH, 6, height - headerH);

        // 6. Overlays & Dropdowns (Top Z-Index)
        paintDropdownOverlays(g2, sideW, headerH, width - sideW, height);

        // 7. Modals (Detail modal, Teammate modal, or Create project modal)
        if (detailModalProject != null) {
            paintProjectDetailModal(g2, width, height);
        } else if (teammateModalUser != null) {
            paintTeammateProfileModal(g2, width, height);
        } else if (showCreateModal) {
            paintCreateProjectModal(g2, width, height);
        }

        // 8. Toast Notification (if active)
        if (!toastMessage.isEmpty()) {
            paintToast(g2, width, height);
        }

        g2.dispose();
    }

    // =========================================================================
    // EXPLORE PROJECTS PAGE RENDERING (Matches Reference Image)
    // =========================================================================
    private void paintExploreProjectsContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Page Title & Subtitle + "+ Create project" Primary Button
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString("Explore projects", startX + padX, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("Find a meaningful problem. Bring your skills. Build something that matters.", startX + padX, curY + 46);

        // "+ Create project" Primary Button
        int primBtnW = 138;
        int primBtnH = 38;
        int primBtnX = startX + padX + contentW - primBtnW;
        int primBtnY = curY;
        registerHitbox("MAIN_CREATE_PROJECT", primBtnX, primBtnY, primBtnW, primBtnH, null);

        boolean primHov = "MAIN_CREATE_PROJECT".equals(hoveredHitbox);
        RoundRectangle2D primBtnShape = new RoundRectangle2D.Float(primBtnX, primBtnY, primBtnW, primBtnH, 10, 10);
        g.setColor(primHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        g.fill(primBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("+  Create project", primBtnX + 16, primBtnY + 24);

        curY += 72;

        // 2. Search Box + Filter Dropdowns Row
        int searchBoxW = Math.max(260, contentW - 440);
        int searchBoxH = 40;
        int searchBoxX = startX + padX;
        int searchBoxY = curY;
        registerHitbox("CONTENT_SEARCH", searchBoxX, searchBoxY, searchBoxW, searchBoxH, null);

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchBoxX, searchBoxY, searchBoxW, searchBoxH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(contentSearchFocused ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(contentSearchFocused ? 1.5f : 1.0f));
        g.draw(sBox);

        // Magnifier
        g.setColor(TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchBoxX + 14, searchBoxY + 13, 11, 11);
        g.drawLine(searchBoxX + 23, searchBoxY + 22, searchBoxX + 27, searchBoxY + 26);

        // Text or Placeholder
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (contentSearchText.isEmpty() && !contentSearchFocused) {
            g.setColor(TEXT_LIGHT);
            g.drawString("Search projects by keyword or skill...", searchBoxX + 38, searchBoxY + 25);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(contentSearchText, searchBoxX + 38, searchBoxY + 25);
            if (contentSearchFocused && caretTimer < 0.5f) {
                int tw = g.getFontMetrics().stringWidth(contentSearchText);
                g.setColor(BRAND_PURPLE);
                g.drawLine(searchBoxX + 38 + tw, searchBoxY + 12, searchBoxX + 38 + tw, searchBoxY + 28);
            }
        }

        // Clear Search Button (if text entered)
        if (!contentSearchText.isEmpty()) {
            int clrX = searchBoxX + searchBoxW - 28;
            int clrY = searchBoxY + 12;
            registerHitbox("CLEAR_SEARCH", clrX, clrY, 16, 16, null);
            g.setColor(TEXT_LIGHT);
            g.fillOval(clrX, clrY, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.drawString("×", clrX + 4, clrY + 12);
        }

        // Filter Dropdowns Buttons
        int dropX = searchBoxX + searchBoxW + 12;
        dropX = paintFilterButton(g, dropX, curY, "Category", filterCategory, "FILTER_CATEGORY");
        dropX = paintFilterButton(g, dropX, curY, "Skills", filterSkill, "FILTER_SKILLS");
        dropX = paintFilterButton(g, dropX, curY, "Experience", filterExperience, "FILTER_EXPERIENCE");
        paintFilterButton(g, dropX, curY, "Recruiting", filterRecruiting, "FILTER_RECRUITING");

        curY += searchBoxH + 18;

        // 3. Category Filter Chips Row
        int chipX = startX + padX;
        for (String cat : categoryChips) {
            boolean isSel = cat.equalsIgnoreCase(selectedCategory);
            boolean isHov = ("CHIP_" + cat).equals(hoveredHitbox);

            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 12));
            int strW = g.getFontMetrics().stringWidth(cat);
            int chipW = strW + 24;
            int chipH = 32;

            registerHitbox("CHIP_" + cat, chipX, curY, chipW, chipH, cat);

            RoundRectangle2D chipShape = new RoundRectangle2D.Float(chipX, curY, chipW, chipH, 8, 8);
            if (isSel) {
                g.setColor(PURPLE_TINT_BG);
                g.fill(chipShape);
                g.setColor(PURPLE_BORDER);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(chipShape);
                g.setColor(BRAND_PURPLE);
            } else {
                g.setColor(isHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
                g.fill(chipShape);
                g.setColor(BORDER_COLOR);
                g.setStroke(new BasicStroke(1.0f));
                g.draw(chipShape);
                g.setColor(TEXT_MUTED);
            }

            g.drawString(cat, chipX + 12, curY + 20);
            chipX += chipW + 8;
        }

        curY += 32 + 24;

        // 4. Header Bar: Count + Recommended Sort + View Switcher
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT_DARK);
        String countStr = filteredProjects.size() + " projects looking for people like you";
        g.drawString(countStr, startX + padX, curY + 22);

        // Right side: Sort + Grid/List
        int rightCurX = startX + padX + contentW;

        // View Mode: List Button
        int viewBtnSize = 34;
        rightCurX -= viewBtnSize;
        registerHitbox("VIEW_LIST", rightCurX, curY, viewBtnSize, viewBtnSize, null);
        paintViewToggleBtn(g, rightCurX, curY, viewBtnSize, !isGridView, false);

        // View Mode: Grid Button
        rightCurX -= (viewBtnSize + 6);
        registerHitbox("VIEW_GRID", rightCurX, curY, viewBtnSize, viewBtnSize, null);
        paintViewToggleBtn(g, rightCurX, curY, viewBtnSize, isGridView, true);

        // Sort Dropdown
        int sortBtnW = 140;
        rightCurX -= (sortBtnW + 12);
        registerHitbox("SORT_DROPDOWN", rightCurX, curY, sortBtnW, 34, null);
        boolean sortHov = "SORT_DROPDOWN".equals(hoveredHitbox) || activeDropdown == DropdownType.SORT;

        RoundRectangle2D sortShape = new RoundRectangle2D.Float(rightCurX, curY, sortBtnW, 34, 8, 8);
        g.setColor(sortHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(sortShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(sortShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString(selectedSort, rightCurX + 12, curY + 21);
        Draw.drawChevronDown(g, rightCurX + sortBtnW - 20, curY + 12, 8, TEXT_MUTED, 0f);

        curY += 34 + 18;

        // 5. Projects Listing (Grid or List view)
        int startIdx = (currentPage - 1) * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, filteredProjects.size());

        if (filteredProjects.isEmpty()) {
            paintEmptyState(g, startX + padX, curY, contentW, 280);
            curY += 300;
        } else if (isGridView) {
            curY = paintProjectGrid(g, startX + padX, curY, contentW, startIdx, endIdx);
        } else {
            curY = paintProjectList(g, startX + padX, curY, contentW, startIdx, endIdx);
        }

        curY += 24;

        // 6. Pagination Row
        paintPagination(g, startX + padX, curY, contentW, filteredProjects.size());
        curY += 60;

        totalContentHeight = curY - startY + 40;
    }

    // =========================================================================
    // PROJECT CARDS GRID (3 Columns x 2 Rows)
    // =========================================================================
    private int paintProjectGrid(Graphics2D g, int x, int y, int w, int startIdx, int endIdx) {
        int cols = (w >= 1080) ? 3 : 2;
        int gap = 20;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 264;

        int curX = x;
        int curY = y;
        int colCount = 0;

        for (int i = startIdx; i < endIdx; i++) {
            Project p = filteredProjects.get(i);
            paintSingleProjectCard(g, curX, curY, cardW, cardH, p);

            colCount++;
            if (colCount % cols == 0) {
                colCount = 0;
                curX = x;
                curY += cardH + gap;
            } else {
                curX += cardW + gap;
            }
        }

        if (colCount != 0) {
            curY += cardH + gap;
        }
        return curY;
    }

    private void paintSingleProjectCard(Graphics2D g, int cx, int cy, int cw, int ch, Project p) {
        String cardKey = "CARD_" + p.id;
        boolean isHov = cardKey.equals(hoveredHitbox);

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(CARD_BG);
        g.fill(cardShape);

        // Border & Hover Shadow
        g.setColor(isHov ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(isHov ? 1.5f : 1.0f));
        g.draw(cardShape);

        int pad = 20;

        // 1. Icon Box
        int iconSize = 40;
        int iconX = cx + pad;
        int iconY = cy + pad;
        RoundRectangle2D iconShape = new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, 10, 10);
        g.setColor(p.iconBg);
        g.fill(iconShape);
        paintVectorIcon(g, p.iconType, iconX + 10, iconY + 10, 20, p.iconColor);

        // 2. Title & Category Subtitle
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString(p.title, iconX + iconSize + 12, iconY + 18);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(p.category + " · " + p.experience, iconX + iconSize + 12, iconY + 34);

        // 3. Bookmark Ribbon (top right)
        int bmSize = 22;
        int bmX = cx + cw - pad - bmSize;
        int bmY = cy + pad + 4;
        String bmKey = "BOOKMARK_" + p.id;
        registerHitbox(bmKey, bmX - 4, bmY - 4, bmSize + 8, bmSize + 8, p);
        paintBookmarkIcon(g, bmX, bmY, 18, p.bookmarked);

        // 4. Description (2 lines)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(0x33, 0x41, 0x55));
        int descY = cy + pad + iconSize + 16;
        drawWrappedText(g, p.description, cx + pad, descY, cw - pad * 2, 17, 2);

        // 5. Skill Chips Row
        int tagY = descY + 40;
        int tagX = cx + pad;
        for (String s : p.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            int sw = g.getFontMetrics().stringWidth(s);
            int tw = sw + 16;
            int th = 22;

            RoundRectangle2D tagShape = new RoundRectangle2D.Float(tagX, tagY, tw, th, 6, 6);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(tagShape);
            g.setColor(new Color(0x47, 0x55, 0x69));
            g.drawString(s, tagX + 8, tagY + 15);

            String tagKey = "TAG_" + p.id + "_" + s;
            registerHitbox(tagKey, tagX, tagY, tw, th, s);

            tagX += tw + 6;
        }

        // 6. Recruiting status badge & Members count
        int statusY = tagY + 34;
        RoundRectangle2D recBadge = new RoundRectangle2D.Float(cx + pad, statusY, 68, 20, 6, 6);
        g.setColor(p.recruiting ? RECRUIT_BG : new Color(0xF1, 0xF5, 0xF9));
        g.fill(recBadge);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(p.recruiting ? RECRUIT_TEXT : TEXT_MUTED);
        g.drawString(p.recruiting ? "Recruiting" : "Team Full", cx + pad + 8, statusY + 14);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        String memStr = p.currentMembers + " / " + p.maxMembers + " members";
        int memW = g.getFontMetrics().stringWidth(memStr);
        g.drawString(memStr, cx + cw - pad - memW, statusY + 14);

        // Open roles line
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(p.openRoles, cx + pad, statusY + 36);

        // 7. Bottom Card Divider
        int divY = cy + ch - 48;
        g.setColor(BORDER_COLOR);
        g.drawLine(cx + pad, divY, cx + cw - pad, divY);

        // 8. Creator Avatar & Name
        int avSize = 26;
        int avX = cx + pad;
        int avY = divY + 11;
        g.setColor(p.creatorColor);
        g.fillOval(avX, avY, avSize, avSize);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(Color.WHITE);
        g.drawString(p.creatorInitials, avX + 6, avY + 17);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString(p.creatorName, avX + 34, avY + 18);

        // 9. "View details ->" Button
        String vdKey = "VIEW_DETAILS_" + p.id;
        boolean vdHov = vdKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(vdHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String vdText = "View details →";
        int vdW = g.getFontMetrics().stringWidth(vdText);
        int vdX = cx + cw - pad - vdW;
        int vdY = avY + 18;
        g.drawString(vdText, vdX, vdY);

        registerHitbox(vdKey, vdX - 4, avY, vdW + 8, 26, p);
    }

    // =========================================================================
    // PROJECT CARDS LIST VIEW (Horizontal Cards)
    // =========================================================================
    private int paintProjectList(Graphics2D g, int x, int y, int w, int startIdx, int endIdx) {
        int cardH = 132;
        int gap = 16;
        int curY = y;

        for (int i = startIdx; i < endIdx; i++) {
            Project p = filteredProjects.get(i);
            paintSingleProjectListCard(g, x, curY, w, cardH, p);
            curY += cardH + gap;
        }
        return curY;
    }

    private void paintSingleProjectListCard(Graphics2D g, int cx, int cy, int cw, int ch, Project p) {
        String cardKey = "CARD_" + p.id;
        boolean isHov = cardKey.equals(hoveredHitbox);

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(CARD_BG);
        g.fill(cardShape);
        g.setColor(isHov ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(isHov ? 1.5f : 1.0f));
        g.draw(cardShape);

        int pad = 20;

        // Icon
        int iconSize = 44;
        RoundRectangle2D iconShape = new RoundRectangle2D.Float(cx + pad, cy + 20, iconSize, iconSize, 10, 10);
        g.setColor(p.iconBg);
        g.fill(iconShape);
        paintVectorIcon(g, p.iconType, cx + pad + 12, cy + 32, 20, p.iconColor);

        // Title & Description
        int infoX = cx + pad + iconSize + 16;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        g.drawString(p.title, infoX, cy + 32);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(p.category + " · " + p.experience + " · Lead: " + p.creatorName, infoX + g.getFontMetrics(new Font(Font.SANS_SERIF, Font.BOLD, 16)).stringWidth(p.title) + 16, cy + 32);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(new Color(0x33, 0x41, 0x55));
        g.drawString(p.description, infoX, cy + 54);

        // Skills Tags
        int tagX = infoX;
        int tagY = cy + 72;
        for (String s : p.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            int tw = g.getFontMetrics().stringWidth(s) + 16;
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fillRoundRect(tagX, tagY, tw, 22, 6, 6);
            g.setColor(new Color(0x47, 0x55, 0x69));
            g.drawString(s, tagX + 8, tagY + 15);
            tagX += tw + 8;
        }

        // Open roles
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(p.openRoles, tagX + 16, tagY + 15);

        // Right side: Recruiting, Members, Bookmark, Details
        int rightX = cx + cw - pad;

        // Bookmark
        int bmSize = 20;
        int bmX = rightX - bmSize;
        int bmY = cy + 20;
        registerHitbox("BOOKMARK_" + p.id, bmX - 4, bmY - 4, bmSize + 8, bmSize + 8, p);
        paintBookmarkIcon(g, bmX, bmY, 18, p.bookmarked);

        // Recruiting
        RoundRectangle2D recBadge = new RoundRectangle2D.Float(rightX - 100, cy + 52, 68, 20, 6, 6);
        g.setColor(p.recruiting ? RECRUIT_BG : new Color(0xF1, 0xF5, 0xF9));
        g.fill(recBadge);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(p.recruiting ? RECRUIT_TEXT : TEXT_MUTED);
        g.drawString(p.recruiting ? "Recruiting" : "Team Full", rightX - 92, cy + 66);

        // View Details Button
        String vdKey = "VIEW_DETAILS_" + p.id;
        boolean vdHov = vdKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(vdHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String vdText = "View details →";
        int vdW = g.getFontMetrics().stringWidth(vdText);
        g.drawString(vdText, rightX - vdW, cy + 98);
        registerHitbox(vdKey, rightX - vdW - 4, cy + 82, vdW + 8, 26, p);
    }

    // =========================================================================
    // PAGINATION BAR
    // =========================================================================
    private void paintPagination(Graphics2D g, int x, int y, int w, int totalItems) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        int startItem = totalItems == 0 ? 0 : (currentPage - 1) * itemsPerPage + 1;
        int endItem = Math.min(currentPage * itemsPerPage, totalItems);

        // Left: "Showing 1-6 of 18 recruiting projects"
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        String info = "Showing " + startItem + "-" + endItem + " of " + totalItems + " recruiting projects";
        g.drawString(info, x, y + 22);

        // Right: Pagination Buttons
        int curX = x + w;

        // "Next ->" Button
        boolean nextDisabled = (currentPage >= totalPages);
        int nextW = 76;
        int btnH = 34;
        curX -= nextW;
        registerHitbox("PAGE_NEXT", curX, y, nextW, btnH, null);

        boolean nextHov = "PAGE_NEXT".equals(hoveredHitbox) && !nextDisabled;
        paintPageBtn(g, curX, y, nextW, btnH, "Next →", false, nextDisabled, nextHov);

        // Numeric Page Buttons (1, 2, 3...)
        for (int p = totalPages; p >= 1; p--) {
            int numW = 34;
            curX -= (numW + 6);
            String pKey = "PAGE_NUM_" + p;
            registerHitbox(pKey, curX, y, numW, btnH, p);
            boolean isCur = (p == currentPage);
            boolean isHov = pKey.equals(hoveredHitbox);
            paintPageBtn(g, curX, y, numW, btnH, String.valueOf(p), isCur, false, isHov);
        }

        // "<- Previous" Button
        boolean prevDisabled = (currentPage <= 1);
        int prevW = 94;
        curX -= (prevW + 6);
        registerHitbox("PAGE_PREV", curX, y, prevW, btnH, null);
        boolean prevHov = "PAGE_PREV".equals(hoveredHitbox) && !prevDisabled;
        paintPageBtn(g, curX, y, prevW, btnH, "← Previous", false, prevDisabled, prevHov);
    }

    private void paintPageBtn(Graphics2D g, int x, int y, int w, int h, String label, boolean active, boolean disabled, boolean hovered) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        if (active) {
            g.setColor(PURPLE_TINT_BG);
            g.fill(shape);
            g.setColor(PURPLE_BORDER);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
            g.setColor(BRAND_PURPLE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        } else {
            g.setColor(hovered && !disabled ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(shape);
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(shape);
            g.setColor(disabled ? TEXT_LIGHT : TEXT_DARK);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }

        int sw = g.getFontMetrics().stringWidth(label);
        g.drawString(label, x + (w - sw) / 2, y + 21);
    }

    // =========================================================================
    // FILTER BUTTON RENDERER
    // =========================================================================
    private int paintFilterButton(Graphics2D g, int x, int y, String title, String activeVal, String key) {
        boolean isFiltered = !activeVal.equals("All");
        String label = isFiltered ? (title + ": " + activeVal) : title;

        g.setFont(new Font(Font.SANS_SERIF, isFiltered ? Font.BOLD : Font.PLAIN, 12));
        int strW = g.getFontMetrics().stringWidth(label);
        int btnW = strW + 36;
        int btnH = 40;

        registerHitbox(key, x, y, btnW, btnH, null);

        boolean isHov = key.equals(hoveredHitbox) || (key.equals("FILTER_CATEGORY") && activeDropdown == DropdownType.CATEGORY)
                || (key.equals("FILTER_SKILLS") && activeDropdown == DropdownType.SKILLS)
                || (key.equals("FILTER_EXPERIENCE") && activeDropdown == DropdownType.EXPERIENCE)
                || (key.equals("FILTER_RECRUITING") && activeDropdown == DropdownType.RECRUITING);

        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, btnW, btnH, 10, 10);
        g.setColor(isHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(shape);
        g.setColor(isFiltered ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(isFiltered ? 1.2f : 1.0f));
        g.draw(shape);

        g.setColor(isFiltered ? BRAND_PURPLE : TEXT_DARK);
        g.drawString(label, x + 12, y + 25);
        Draw.drawChevronDown(g, x + btnW - 16, y + 16, 8, isFiltered ? BRAND_PURPLE : TEXT_MUTED, 0f);

        return x + btnW + 10;
    }

    private void paintViewToggleBtn(Graphics2D g, int x, int y, int size, boolean active, boolean isGrid) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, size, size, 8, 8);
        g.setColor(active ? PURPLE_TINT_BG : Color.WHITE);
        g.fill(shape);
        g.setColor(active ? PURPLE_BORDER : BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(shape);

        Color iconCol = active ? BRAND_PURPLE : TEXT_MUTED;
        g.setColor(iconCol);

        if (isGrid) {
            // 4 squares
            int s = 4;
            g.fillRect(x + 9, y + 9, s, s);
            g.fillRect(x + 17, y + 9, s, s);
            g.fillRect(x + 9, y + 17, s, s);
            g.fillRect(x + 17, y + 17, s, s);
        } else {
            // 3 lines
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + 8, y + 11, x + 24, y + 11);
            g.drawLine(x + 8, y + 17, x + 24, y + 17);
            g.drawLine(x + 8, y + 23, x + 24, y + 23);
        }
    }

    // =========================================================================
    // EMPTY STATE (When search/filter has 0 results)
    // =========================================================================
    private void paintEmptyState(Graphics2D g, int x, int y, int w, int h) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(BORDER_COLOR);
        g.draw(shape);

        int cx = x + w / 2;
        int cy = y + 70;

        // Magnifier with exclamation
        g.setColor(PURPLE_TINT_BG);
        g.fillOval(cx - 30, cy - 30, 60, 60);
        g.setColor(BRAND_PURPLE);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(cx - 14, cy - 14, 20, 20);
        g.drawLine(cx + 2, cy + 2, cx + 12, cy + 12);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(TEXT_DARK);
        String t = "No matching projects found";
        g.drawString(t, cx - g.getFontMetrics().stringWidth(t) / 2, cy + 54);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        String sub = "Try adjusting your filters, clearing the search query, or create your own project!";
        g.drawString(sub, cx - g.getFontMetrics().stringWidth(sub) / 2, cy + 76);

        // Reset Filters Button
        int btnW = 120;
        int btnH = 36;
        int btnX = cx - btnW / 2;
        int btnY = cy + 96;
        registerHitbox("RESET_FILTERS", btnX, btnY, btnW, btnH, null);

        RoundRectangle2D rBtn = new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 8, 8);
        g.setColor(PURPLE_TINT_BG);
        g.fill(rBtn);
        g.setColor(BRAND_PURPLE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        String rText = "Reset Filters";
        g.drawString(rText, btnX + (btnW - g.getFontMetrics().stringWidth(rText)) / 2, btnY + 22);
    }

    // =========================================================================
    // SIDEBAR RENDERING
    // =========================================================================
    private void paintSidebar(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(BORDER_COLOR);
        g.drawLine(x + w - 1, y, x + w - 1, y + h);

        int curY = y + 20;
        int padX = 20;

        // Logo + Brand Name
        paintLogo(g, x + padX, curY);
        registerHitbox("LOGO_HOME", x + padX, curY, 120, 32, null);
        curY += 46;

        // "YOUR WORKSPACE" Card
        int wsCardW = w - padX * 2;
        int wsCardH = 68;
        RoundRectangle2D wsCard = new RoundRectangle2D.Float(x + padX, curY, wsCardW, wsCardH, 12, 12);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(wsCard);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(TEXT_LIGHT);
        g.drawString("YOUR WORKSPACE", x + padX + 12, curY + 20);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Student workspace", x + padX + 12, curY + 38);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("University of Washington", x + padX + 12, curY + 54);

        registerHitbox("WORKSPACE_CARD", x + padX, curY, wsCardW, wsCardH, null);
        curY += wsCardH + 16;

        // Nav Menu Items
        int itemH = 38;
        int itemW = w - padX * 2;
        NavItem[] items = NavItem.values();

        for (NavItem item : items) {
            boolean isSel = (item == activeNav);
            boolean isHov = (item == hoveredNav) || ("NAV_" + item.name()).equals(hoveredHitbox);
            int itemX = x + padX;
            int itemY = curY;

            registerHitbox("NAV_" + item.name(), itemX, itemY, itemW, itemH, item);

            RoundRectangle2D itemShape = new RoundRectangle2D.Float(itemX, itemY, itemW, itemH, 10, 10);
            if (isSel) {
                g.setColor(PURPLE_TINT_BG);
                g.fill(itemShape);
            } else if (isHov) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fill(itemShape);
            }

            // Icon
            Color iconCol = isSel ? BRAND_PURPLE : TEXT_MUTED;
            paintNavIcon(g, item, itemX + 12, itemY + 11, 16, iconCol);

            // Label
            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 13));
            g.setColor(isSel ? BRAND_PURPLE : TEXT_MUTED);
            g.drawString(item.label, itemX + 38, itemY + 24);

            // Badge (for Messages)
            if (item.badge > 0) {
                int bw = 20;
                int bh = 18;
                int bx = itemX + itemW - bw - 10;
                int by = itemY + (itemH - bh) / 2;
                RoundRectangle2D bShape = new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh);
                g.setColor(new Color(0xEE, 0xF2, 0xFF));
                g.fill(bShape);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                g.setColor(BRAND_PURPLE);
                g.drawString(String.valueOf(item.badge), bx + 7, by + 13);
            }

            curY += itemH + 2;
        }

        // Return to Landing Page Pill (Helper navigation)
        curY += 12;
        int returnBtnX = x + padX;
        int returnBtnY = curY;
        int returnBtnW = w - padX * 2;
        int returnBtnH = 34;
        registerHitbox("BACK_TO_LANDING", returnBtnX, returnBtnY, returnBtnW, returnBtnH, null);

        boolean retHov = "BACK_TO_LANDING".equals(hoveredHitbox);
        RoundRectangle2D retShape = new RoundRectangle2D.Float(returnBtnX, returnBtnY, returnBtnW, returnBtnH, 10, 10);
        g.setColor(retHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(retShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(retShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("← Back to Landing Page", returnBtnX + 16, returnBtnY + 21);

        // Bottom User Profile Card
        int bottomH = 64;
        int bottomY = h - bottomH;
        g.setColor(BORDER_COLOR);
        g.drawLine(x, bottomY, x + w, bottomY);

        registerHitbox("BOTTOM_PROFILE", x, bottomY, w, bottomH, null);
        boolean profHov = "BOTTOM_PROFILE".equals(hoveredHitbox);
        if (profHov) {
            g.setColor(new Color(0xF8, 0xFA, 0xFC));
            g.fillRect(x, bottomY, w, bottomH);
        }

        // Avatar "AM"
        int avX = x + padX;
        int avY = bottomY + 14;
        int avSize = 36;
        g.setColor(new Color(0xED, 0xE9, 0xFE));
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        g.drawString("AM", avX + 9, avY + 23);

        // User info
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Alex Morgan", avX + 44, avY + 16);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString("Student · Free plan", avX + 44, avY + 31);

        // Chevron
        Draw.drawChevronDown(g, x + w - padX - 10, avY + 14, 10, TEXT_LIGHT, 0f);
    }

    private void paintLogo(Graphics2D g, int x, int y) {
        int iconSize = 28;
        RoundRectangle2D logoBg = new RoundRectangle2D.Float(x, y, iconSize, iconSize, 8, 8);
        g.setColor(BRAND_PURPLE);
        g.fill(logoBg);

        // 4 dots/squares
        g.setColor(Color.WHITE);
        int d = 6;
        g.fillRoundRect(x + 6, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 6, d, d, 2, 2);
        g.fillRoundRect(x + 6, y + 16, d, d, 2, 2);
        g.fillRoundRect(x + 16, y + 16, d, d, 2, 2);

        // Brand Name
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(TEXT_DARK);
        g.drawString("TeamUp", x + 38, y + 21);
    }

    private void paintNavIcon(Graphics2D g, NavItem item, int x, int y, int size, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (item) {
            case OVERVIEW:
                int s = 5;
                g.drawRoundRect(x, y, s, s, 2, 2);
                g.drawRoundRect(x + 9, y, s, s, 2, 2);
                g.drawRoundRect(x, y + 9, s, s, 2, 2);
                g.drawRoundRect(x + 9, y + 9, s, s, 2, 2);
                break;
            case DISCOVER_PROJECTS:
                g.drawOval(x, y, size, size);
                g.drawLine(x + 4, y + 12, x + 12, y + 4);
                break;
            case FIND_TEAMMATES:
                g.drawOval(x + 3, y, 6, 6);
                g.drawArc(x, y + 8, 12, 8, 0, 180);
                g.drawOval(x + 10, y + 2, 4, 4);
                break;
            case MY_PROJECTS:
                g.drawRoundRect(x, y + 3, size - 2, size - 5, 3, 3);
                g.drawLine(x, y + 6, x + 6, y + 6);
                break;
            case MY_TEAMS:
                g.drawOval(x + 5, y + 1, 6, 6);
                g.drawArc(x + 2, y + 9, 12, 7, 0, 180);
                break;
            case MESSAGES:
                g.drawRoundRect(x, y + 1, size - 2, size - 4, 4, 4);
                g.drawLine(x + 4, y + size - 3, x + 2, y + size);
                break;
            case MY_PROFILE:
                g.drawOval(x + 4, y + 1, 8, 8);
                g.drawArc(x + 1, y + 10, 14, 6, 0, 180);
                break;
            case SETTINGS:
                g.drawOval(x + 3, y + 3, size - 6, size - 6);
                g.drawOval(x + 6, y + 6, size - 12, size - 12);
                break;
        }
    }

    // =========================================================================
    // TOP HEADER RENDERING
    // =========================================================================
    private void paintHeader(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, y, w, h);
        g.setColor(BORDER_COLOR);
        g.drawLine(x, y + h - 1, x + w, y + h - 1);

        int padX = 28;
        int curX = x + padX;

        // Breadcrumb: "Workspace >"
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("Workspace", curX, y + 35);
        registerHitbox("BREADCRUMB_WS", curX - 4, y + 15, 75, 30, null);
        curX += 75;

        g.setColor(TEXT_LIGHT);
        g.drawString("›", curX, y + 35);
        curX += 20;

        // Search Pill Input
        int searchX = curX;
        int searchY = y + 12;
        int searchW = Math.min(320, w - 460);
        int searchH = 36;
        registerHitbox("TOP_SEARCH", searchX, searchY, searchW, searchH, null);

        RoundRectangle2D searchPill = new RoundRectangle2D.Float(searchX, searchY, searchW, searchH, searchH, searchH);
        g.setColor(topSearchFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(searchPill);
        g.setColor(topSearchFocused ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(searchPill);

        // Search Magnifier Icon
        g.setColor(TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchX + 14, searchY + 11, 10, 10);
        g.drawLine(searchX + 22, searchY + 19, searchX + 26, searchY + 23);

        // Placeholder or entered text
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (topSearchText.isEmpty() && !topSearchFocused) {
            g.setColor(TEXT_LIGHT);
            g.drawString("Search projects, people and teams...", searchX + 34, searchY + 22);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(topSearchText, searchX + 34, searchY + 22);
            if (topSearchFocused && caretTimer < 0.5f) {
                int tw = g.getFontMetrics().stringWidth(topSearchText);
                g.setColor(BRAND_PURPLE);
                g.drawLine(searchX + 34 + tw, searchY + 10, searchX + 34 + tw, searchY + 26);
            }
        }

        // Right side controls
        int rightX = x + w - padX;

        // Profile Avatar + Chevron
        int avSize = 34;
        rightX -= (avSize + 16);
        registerHitbox("TOP_PROFILE", rightX, y + 12, avSize + 16, 36, null);

        g.setColor(new Color(0xED, 0xE9, 0xFE));
        g.fillOval(rightX, y + 13, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(BRAND_PURPLE);
        g.drawString("AM", rightX + 8, y + 35);
        Draw.drawChevronDown(g, rightX + avSize + 4, y + 25, 8, TEXT_MUTED, 0f);

        // Bell Icon (Notifications)
        rightX -= 40;
        int bellX = rightX;
        int bellY = y + 14;
        registerHitbox("TOP_BELL", bellX - 4, bellY - 2, 34, 34, null);

        boolean bellHov = "TOP_BELL".equals(hoveredHitbox) || activeDropdown == DropdownType.NOTIFICATIONS;
        if (bellHov) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fillOval(bellX - 2, bellY, 30, 30);
        }

        g.setColor(TEXT_MUTED);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(bellX + 6, bellY + 6, 14, 14, 0, 180);
        g.drawLine(bellX + 4, bellY + 20, bellX + 22, bellY + 20);
        g.drawOval(bellX + 11, bellY + 20, 4, 3);

        // Notification dot (if any unread)
        boolean hasUnread = notifications.stream().anyMatch(n -> n.unread);
        if (hasUnread) {
            g.setColor(BRAND_PURPLE);
            g.fillOval(bellX + 18, bellY + 5, 6, 6);
        }

        // "+ Create project" Ghost Pill Button
        int topCreateBtnW = 126;
        int topCreateBtnH = 34;
        rightX -= (topCreateBtnW + 16);
        int topCreateBtnX = rightX;
        int topCreateBtnY = y + 13;
        registerHitbox("TOP_CREATE_PROJECT", topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH, null);

        boolean topBtnHov = "TOP_CREATE_PROJECT".equals(hoveredHitbox);
        RoundRectangle2D topBtnShape = new RoundRectangle2D.Float(topCreateBtnX, topCreateBtnY, topCreateBtnW, topCreateBtnH, topCreateBtnH, topCreateBtnH);
        g.setColor(topBtnHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(topBtnShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(topBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("+ Create project", topCreateBtnX + 14, topCreateBtnY + 21);
    }

    // =========================================================================
    // DROPDOWN MENUS OVERLAYS
    // =========================================================================
    private void paintDropdownOverlays(Graphics2D g, int sideW, int headerH, int contentW, int height) {
        if (activeDropdown == DropdownType.NONE) return;

        // Dim background outside dropdown
        switch (activeDropdown) {
            case CATEGORY:
                paintGenericMenu(g, getHitboxX("FILTER_CATEGORY"), headerH + 112 - Math.round(scrollY), 160,
                        categoryChips, selectedCategory, "SELECT_CAT_DROP");
                break;
            case SKILLS:
                paintGenericMenu(g, getHitboxX("FILTER_SKILLS"), headerH + 112 - Math.round(scrollY), 150,
                        skillsList, filterSkill, "SELECT_SKILL_DROP");
                break;
            case EXPERIENCE:
                paintGenericMenu(g, getHitboxX("FILTER_EXPERIENCE"), headerH + 112 - Math.round(scrollY), 140,
                        expList, filterExperience, "SELECT_EXP_DROP");
                break;
            case RECRUITING:
                paintGenericMenu(g, getHitboxX("FILTER_RECRUITING"), headerH + 112 - Math.round(scrollY), 140,
                        recruitList, filterRecruiting, "SELECT_REC_DROP");
                break;
            case SORT:
                paintGenericMenu(g, getHitboxX("SORT_DROPDOWN"), headerH + 172 - Math.round(scrollY), 160,
                        sortList, selectedSort, "SELECT_SORT_DROP");
                break;
            case TM_SKILLS:
                paintGenericMenu(g, getHitboxX("TM_FILTER_SKILLS"), headerH + 112 - Math.round(scrollY), 150,
                        tmSkillsList, filterTmSkill, "TM_SELECT_SKILL");
                break;
            case TM_EXPERIENCE:
                paintGenericMenu(g, getHitboxX("TM_FILTER_EXP"), headerH + 112 - Math.round(scrollY), 140,
                        tmExpList, filterTmExperience, "TM_SELECT_EXP");
                break;
            case TM_AVAILABILITY:
                paintGenericMenu(g, getHitboxX("TM_FILTER_AVAIL"), headerH + 112 - Math.round(scrollY), 160,
                        tmAvailList, filterTmAvailability, "TM_SELECT_AVAIL");
                break;
            case TM_RATING:
                paintGenericMenu(g, getHitboxX("TM_FILTER_RATING"), headerH + 112 - Math.round(scrollY), 140,
                        tmRatingList, filterTmRating, "TM_SELECT_RATING");
                break;
            case TM_SORT:
                paintGenericMenu(g, getHitboxX("TM_SORT_DROPDOWN"), headerH + 158 - Math.round(scrollY), 180,
                        tmSortList, selectedTmSort, "TM_SELECT_SORT");
                break;
            case NOTIFICATIONS:
                paintNotificationsPopup(g, sideW + contentW - 320, headerH + 6);
                break;
            case PROFILE:
                paintProfileMenu(g, sideW + contentW - 200, headerH + 6);
                break;
            default:
                break;
        }
    }

    private void paintGenericMenu(Graphics2D g, int x, int y, int w, String[] items, String selected, String prefix) {
        int itemH = 32;
        int totalH = items.length * itemH + 12;

        RoundRectangle2D menuBg = new RoundRectangle2D.Float(x, y, w, totalH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(menuBg);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(menuBg);

        int curY = y + 6;
        for (String it : items) {
            boolean isSel = it.equalsIgnoreCase(selected);
            String itemKey = prefix + "_" + it;
            registerHitbox(itemKey, x, curY, w, itemH, it);

            boolean isHov = itemKey.equals(hoveredHitbox);
            if (isHov || isSel) {
                g.setColor(isSel ? PURPLE_TINT_BG : new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(x + 4, curY, w - 8, itemH, 6, 6);
            }

            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(isSel ? BRAND_PURPLE : TEXT_DARK);
            g.drawString(it, x + 16, curY + 20);

            if (isSel) {
                g.drawString("✓", x + w - 24, curY + 20);
            }
            curY += itemH;
        }
    }

    private void paintNotificationsPopup(Graphics2D g, int x, int y) {
        int w = 310;
        int h = 250;

        RoundRectangle2D popup = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(popup);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(popup);

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Notifications", x + 16, y + 26);

        // Mark All Read Button
        String mKey = "NOTIF_MARK_READ";
        registerHitbox(mKey, x + w - 100, y + 10, 90, 24, null);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(BRAND_PURPLE);
        g.drawString("Mark all read", x + w - 88, y + 26);

        g.setColor(BORDER_COLOR);
        g.drawLine(x, y + 40, x + w, y + 40);

        int curY = y + 48;
        for (NotificationItem n : notifications) {
            RoundRectangle2D itBg = new RoundRectangle2D.Float(x + 8, curY, w - 16, 54, 8, 8);
            if (n.unread) {
                g.setColor(PURPLE_TINT_BG);
                g.fill(itBg);
            }

            g.setColor(n.unread ? BRAND_PURPLE : TEXT_MUTED);
            g.fillOval(x + 18, curY + 14, 8, 8);

            g.setFont(new Font(Font.SANS_SERIF, n.unread ? Font.BOLD : Font.PLAIN, 11));
            g.setColor(TEXT_DARK);
            drawWrappedText(g, n.title, x + 34, curY + 18, w - 50, 14, 2);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(TEXT_LIGHT);
            g.drawString(n.time, x + 34, curY + 44);

            curY += 60;
        }
    }

    private void paintProfileMenu(Graphics2D g, int x, int y) {
        String[] pItems = {"Alex Morgan", "My Profile", "Account Settings", "Help Center", "Log Out"};
        int w = 180;
        int itemH = 34;
        int h = pItems.length * itemH + 12;

        RoundRectangle2D popup = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(popup);
        g.setColor(BORDER_COLOR);
        g.draw(popup);

        int curY = y + 6;
        for (int i = 0; i < pItems.length; i++) {
            String it = pItems[i];
            String pKey = "PROF_ITEM_" + i;
            registerHitbox(pKey, x, curY, w, itemH, it);

            boolean isHov = pKey.equals(hoveredHitbox);
            if (isHov) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(x + 4, curY, w - 8, itemH, 6, 6);
            }

            g.setFont(new Font(Font.SANS_SERIF, i == 0 ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(i == pItems.length - 1 ? new Color(0xEF, 0x44, 0x44) : TEXT_DARK);
            g.drawString(it, x + 16, curY + 22);

            if (i == 0 || i == 3) {
                g.setColor(BORDER_COLOR);
                g.drawLine(x, curY + itemH, x + w, curY + itemH);
            }
            curY += itemH;
        }
    }

    // =========================================================================
    // PROJECT DETAILS MODAL
    // =========================================================================
    private void paintProjectDetailModal(Graphics2D g, int sw, int sh) {
        Project p = detailModalProject;
        if (p == null) return;

        // Dark dim backdrop
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, sw, sh);

        int mw = Math.min(640, sw - 80);
        int mh = Math.min(560, sh - 80);
        int mx = (sw - mw) / 2;
        int my = (sh - mh) / 2;

        registerHitbox("MODAL_CONTAINER", mx, my, mw, mh, null);

        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(BORDER_COLOR);
        g.draw(mShape);

        int pad = 28;
        int curY = my + pad;

        // Top Row: Icon + Title + Close Button
        int iconSize = 48;
        RoundRectangle2D iconShape = new RoundRectangle2D.Float(mx + pad, curY, iconSize, iconSize, 12, 12);
        g.setColor(p.iconBg);
        g.fill(iconShape);
        paintVectorIcon(g, p.iconType, mx + pad + 12, curY + 12, 24, p.iconColor);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(TEXT_DARK);
        g.drawString(p.title, mx + pad + iconSize + 16, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(p.category + " · " + p.experience + " · Created by " + p.creatorName, mx + pad + iconSize + 16, curY + 42);

        // Close X button
        int closeX = mx + mw - pad - 20;
        int closeY = curY + 4;
        registerHitbox("MODAL_CLOSE", closeX - 6, closeY - 6, 32, 32, null);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_LIGHT);
        g.drawString("✕", closeX, closeY + 16);

        curY += iconSize + 22;

        // Divider
        g.setColor(BORDER_COLOR);
        g.drawLine(mx + pad, curY, mx + mw - pad, curY);
        curY += 20;

        // Section: About Project
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT_DARK);
        g.drawString("About this project", mx + pad, curY);
        curY += 18;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(new Color(0x33, 0x41, 0x55));
        drawWrappedText(g, p.description + " This project is currently recruiting students at University of Washington to build an impactful end-to-end prototype.", mx + pad, curY, mw - pad * 2, 19, 3);
        curY += 60;

        // Section: Required Skills
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Target Skills & Stack", mx + pad, curY);
        curY += 14;

        int tagX = mx + pad;
        for (String s : p.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            int tw = g.getFontMetrics().stringWidth(s) + 20;
            g.setColor(PURPLE_TINT_BG);
            g.fillRoundRect(tagX, curY, tw, 26, 8, 8);
            g.setColor(BRAND_PURPLE);
            g.drawString(s, tagX + 10, curY + 18);
            tagX += tw + 8;
        }
        curY += 40;

        // Section: Team Status & Roles
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Team & Open Roles", mx + pad, curY);
        curY += 14;

        // Open roles card
        RoundRectangle2D roleCard = new RoundRectangle2D.Float(mx + pad, curY, mw - pad * 2, 60, 10, 10);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(roleCard);
        g.setColor(BORDER_COLOR);
        g.draw(roleCard);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Status: " + (p.recruiting ? "Actively Recruiting" : "Full"), mx + pad + 16, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(p.openRoles + " (" + p.currentMembers + " of " + p.maxMembers + " spots filled)", mx + pad + 16, curY + 44);

        curY += 76;

        // Action Buttons Row (Apply to Join + Bookmark)
        int btnH = 42;
        int applyW = 160;
        int applyX = mx + mw - pad - applyW;
        int applyY = my + mh - pad - btnH;
        registerHitbox("MODAL_APPLY", applyX, applyY, applyW, btnH, p);

        RoundRectangle2D applyBtn = new RoundRectangle2D.Float(applyX, applyY, applyW, btnH, 10, 10);
        g.setColor(p.applied ? new Color(0x10, 0xB9, 0x81) : BRAND_PURPLE);
        g.fill(applyBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        String apText = p.applied ? "✓ Applied" : "Apply to Join Team";
        g.drawString(apText, applyX + (applyW - g.getFontMetrics().stringWidth(apText)) / 2, applyY + 26);

        // Bookmark in Modal
        int bmMBtnW = 140;
        int bmMBtnX = applyX - bmMBtnW - 12;
        registerHitbox("MODAL_BOOKMARK", bmMBtnX, applyY, bmMBtnW, btnH, p);
        RoundRectangle2D bmBtn = new RoundRectangle2D.Float(bmMBtnX, applyY, bmMBtnW, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(bmBtn);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(bmBtn);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        String bmText = p.bookmarked ? "★ Bookmarked" : "☆ Save Project";
        g.drawString(bmText, bmMBtnX + (bmMBtnW - g.getFontMetrics().stringWidth(bmText)) / 2, applyY + 26);
    }

    // =========================================================================
    // CREATE PROJECT MODAL
    // =========================================================================
    private void paintCreateProjectModal(Graphics2D g, int sw, int sh) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, sw, sh);

        int mw = Math.min(580, sw - 80);
        int mh = Math.min(540, sh - 80);
        int mx = (sw - mw) / 2;
        int my = (sh - mh) / 2;

        registerHitbox("CREATE_MODAL_CONTAINER", mx, my, mw, mh, null);

        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(BORDER_COLOR);
        g.draw(mShape);

        int pad = 28;
        int curY = my + pad;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(TEXT_DARK);
        g.drawString("Create a New Project", mx + pad, curY + 16);

        // Close X button
        int closeX = mx + mw - pad - 20;
        registerHitbox("CREATE_MODAL_CLOSE", closeX - 6, curY, 32, 32, null);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_LIGHT);
        g.drawString("✕", closeX, curY + 16);

        curY += 40;

        // Field 1: Project Name
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Project Title", mx + pad, curY);
        curY += 8;

        int fW = mw - pad * 2;
        int fH = 36;
        registerHitbox("INPUT_TITLE", mx + pad, curY, fW, fH, null);
        paintFormInput(g, mx + pad, curY, fW, fH, newProjTitle, "e.g. EcoRoute / CampusEvents", createModalActiveField == 0);
        curY += fH + 16;

        // Field 2: Category Selector
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Category", mx + pad, curY);
        curY += 8;

        int catBtnW = (fW - 20) / 3;
        String[] mCats = {"Campus life", "Education", "Sustainability"};
        int cx = mx + pad;
        for (String mc : mCats) {
            boolean isSel = mc.equals(newProjCategory);
            registerHitbox("CREATE_CAT_" + mc, cx, curY, catBtnW, 32, mc);
            RoundRectangle2D catR = new RoundRectangle2D.Float(cx, curY, catBtnW, 32, 8, 8);
            g.setColor(isSel ? PURPLE_TINT_BG : new Color(0xF8, 0xFA, 0xFC));
            g.fill(catR);
            g.setColor(isSel ? BRAND_PURPLE : BORDER_COLOR);
            g.draw(catR);
            g.setFont(new Font(Font.SANS_SERIF, isSel ? Font.BOLD : Font.PLAIN, 11));
            g.setColor(isSel ? BRAND_PURPLE : TEXT_DARK);
            g.drawString(mc, cx + 12, curY + 20);
            cx += catBtnW + 10;
        }
        curY += 32 + 16;

        // Field 3: Required Skills
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Required Skills (comma separated)", mx + pad, curY);
        curY += 8;
        registerHitbox("INPUT_SKILLS", mx + pad, curY, fW, fH, null);
        paintFormInput(g, mx + pad, curY, fW, fH, newProjSkills, "React, TypeScript, Figma", createModalActiveField == 1);
        curY += fH + 16;

        // Field 4: Open Roles
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Open Roles Needed", mx + pad, curY);
        curY += 8;
        registerHitbox("INPUT_ROLES", mx + pad, curY, fW, fH, null);
        paintFormInput(g, mx + pad, curY, fW, fH, newProjRoles, "Frontend developer · UI Designer", createModalActiveField == 2);
        curY += fH + 16;

        // Field 5: Description
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Short Description", mx + pad, curY);
        curY += 8;
        registerHitbox("INPUT_DESC", mx + pad, curY, fW, 54, null);
        paintFormInput(g, mx + pad, curY, fW, 54, newProjDesc, "Describe the problem and what you are building...", createModalActiveField == 3);
        curY += 54 + 24;

        // Submit Button
        int btnW = 150;
        int btnH = 40;
        int sBtnX = mx + mw - pad - btnW;
        registerHitbox("SUBMIT_NEW_PROJECT", sBtnX, curY, btnW, btnH, null);
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(sBtnX, curY, btnW, btnH, 10, 10);
        g.setColor(BRAND_PURPLE);
        g.fill(sBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        String pubText = "Publish Project";
        g.drawString(pubText, sBtnX + (btnW - g.getFontMetrics().stringWidth(pubText)) / 2, curY + 25);
    }

    private void paintFormInput(Graphics2D g, int x, int y, int w, int h, String val, String placeholder, boolean focused) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(focused ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
        g.draw(shape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (val.isEmpty()) {
            g.setColor(TEXT_LIGHT);
            g.drawString(placeholder, x + 12, y + 22);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(val, x + 12, y + 22);
            if (focused && caretTimer < 0.5f) {
                int tw = g.getFontMetrics().stringWidth(val);
                g.setColor(BRAND_PURPLE);
                g.drawLine(x + 12 + tw, y + 10, x + 12 + tw, y + 26);
            }
        }
    }

    // =========================================================================
    // OVERVIEW VIEW (Matches Welcome Back, Alex Reference Design)
    // =========================================================================
    private void paintOverviewContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Header: "Welcome back, Alex!" + subtitle + "+ Create Project" button
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString("Welcome back, Alex!", startX + padX, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("A little progress, a great team. Here's what's happening today.", startX + padX, curY + 46);

        // "+ Create Project" Primary Pill Button
        int primBtnW = 142;
        int primBtnH = 38;
        int primBtnX = startX + padX + contentW - primBtnW;
        int primBtnY = curY;
        registerHitbox("OVERVIEW_CREATE_PROJECT", primBtnX, primBtnY, primBtnW, primBtnH, null);

        boolean primHov = "OVERVIEW_CREATE_PROJECT".equals(hoveredHitbox);
        RoundRectangle2D primBtnShape = new RoundRectangle2D.Float(primBtnX, primBtnY, primBtnW, primBtnH, 10, 10);
        g.setColor(primHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        g.fill(primBtnShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("+  Create Project", primBtnX + 16, primBtnY + 24);

        curY += 72;

        // 2. Profile Completion Banner
        curY = paintProfileBanner(g, startX + padX, curY, contentW);
        curY += 20;

        // 3. 4 Stat Cards Row
        curY = paintStatCardsRow(g, startX + padX, curY, contentW);
        curY += 24;

        // 4. Two-Column Layout (Left Column 62%, Right Column 38%)
        int colGap = 20;
        int leftColW = (int) Math.round((contentW - colGap) * 0.62f);
        int rightColW = contentW - leftColW - colGap;
        int leftX = startX + padX;
        int rightX = leftX + leftColW + colGap;

        // LEFT COLUMN:
        int leftCurY = curY;
        leftCurY = paintRecommendedProjectsCard(g, leftX, leftCurY, leftColW);
        leftCurY += 20;
        leftCurY = paintRecentActivityCard(g, leftX, leftCurY, leftColW);

        // RIGHT COLUMN:
        int rightCurY = curY;
        rightCurY = paintSuggestedTeammatesCard(g, rightX, rightCurY, rightColW);
        rightCurY += 20;
        rightCurY = paintUpcomingDeadlinesCard(g, rightX, rightCurY, rightColW);

        totalContentHeight = Math.max(leftCurY, rightCurY) - startY + 40;
    }

    private int paintProfileBanner(Graphics2D g, int x, int y, int w) {
        int h = 76;
        RoundRectangle2D banner = new RoundRectangle2D.Float(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(banner);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(banner);

        int pad = 18;

        // Sparkle icon in soft purple circle
        int spSize = 38;
        int spX = x + pad;
        int spY = y + (h - spSize) / 2;
        g.setColor(PURPLE_TINT_BG);
        g.fillOval(spX, spY, spSize, spSize);
        drawSparkle(g, spX + spSize / 2, spY + spSize / 2, 7, BRAND_PURPLE);

        // Text
        int textX = spX + spSize + 14;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Great teammates start with a great profile.", textX, y + 32);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Add your portfolio to help people see what you can build.", textX, y + 52);

        // Right side: Progress + "Complete profile ->" button
        int rightCurX = x + w - pad;

        // "Complete profile ->" button
        int cpBtnW = 142;
        int cpBtnH = 34;
        rightCurX -= cpBtnW;
        int cpBtnX = rightCurX;
        int cpBtnY = y + (h - cpBtnH) / 2;
        registerHitbox("OVERVIEW_COMPLETE_PROFILE", cpBtnX, cpBtnY, cpBtnW, cpBtnH, null);

        boolean cpHov = "OVERVIEW_COMPLETE_PROFILE".equals(hoveredHitbox);
        RoundRectangle2D cpShape = new RoundRectangle2D.Float(cpBtnX, cpBtnY, cpBtnW, cpBtnH, 8, 8);
        g.setColor(cpHov ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(cpShape);
        g.setColor(new Color(0xD1, 0xD5, 0xDB));
        g.setStroke(new BasicStroke(1.0f));
        g.draw(cpShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Complete profile →", cpBtnX + 14, cpBtnY + 21);

        // Progress bar and text
        int progW = 110;
        int progH = 6;
        rightCurX -= (progW + 24);
        int progX = rightCurX;
        int progY = y + 42;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(BRAND_PURPLE);
        g.drawString("80% complete", progX, y + 30);

        RoundRectangle2D progTrack = new RoundRectangle2D.Float(progX, progY, progW, progH, progH, progH);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.fill(progTrack);

        RoundRectangle2D progFill = new RoundRectangle2D.Float(progX, progY, Math.round(progW * 0.8f), progH, progH, progH);
        g.setColor(BRAND_PURPLE);
        g.fill(progFill);

        return y + h;
    }

    private int paintStatCardsRow(Graphics2D g, int x, int y, int w) {
        int gap = 16;
        int cardW = (w - gap * 3) / 4;
        int cardH = 118;

        String[] titles = {"Active Projects", "My Teams", "Pending Requests", "Tasks Due Soon"};
        String[] values = {"3", "4", "5", "6"};
        String[] subs = {"Across 3 collaborative teams", "3 active · 1 completed", "3 received · 2 sent", "Next 7 days · 1 overdue"};

        for (int i = 0; i < 4; i++) {
            int cx = x + i * (cardW + gap);
            String statKey = "STAT_" + i;
            registerHitbox(statKey, cx, y, cardW, cardH, i);

            boolean isHov = statKey.equals(hoveredHitbox);
            RoundRectangle2D shape = new RoundRectangle2D.Float(cx, y, cardW, cardH, 14, 14);
            g.setColor(isHov ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
            g.fill(shape);
            g.setColor(isHov ? BRAND_PURPLE : BORDER_COLOR);
            g.setStroke(new BasicStroke(isHov ? 1.2f : 1.0f));
            g.draw(shape);

            int pad = 16;
            // Title
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.setColor(TEXT_MUTED);
            g.drawString(titles[i], cx + pad, y + 26);

            // Icon on top right
            int iconX = cx + cardW - pad - 16;
            int iconY = y + 14;
            if (i == 0) drawFolderIcon(g, iconX, iconY, 16, new Color(0x63, 0x66, 0xF1));
            else if (i == 1) drawUsersIcon(g, iconX, iconY, 16, new Color(0x10, 0xB9, 0x81));
            else if (i == 2) drawUserPlusIcon(g, iconX, iconY, 16, new Color(0x8B, 0x5C, 0xF6));
            else drawCalendarAlertIcon(g, iconX, iconY, 16, new Color(0xF5, 0x9E, 0x0B));

            // Value
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
            g.setColor(TEXT_DARK);
            g.drawString(values[i], cx + pad, y + 68);

            // Subtitle
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(subs[i], cx + pad, y + 96);
        }

        return y + cardH;
    }

    private int paintRecommendedProjectsCard(Graphics2D g, int x, int y, int w) {
        int cardH = 276;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(container);

        int pad = 20;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Recommended projects", x + pad, y + 26);

        // "Explore all ->"
        String expKey = "OVERVIEW_EXPLORE_ALL";
        boolean expHov = expKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(expHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String expText = "Explore all →";
        int expW = g.getFontMetrics().stringWidth(expText);
        int expX = x + w - pad - expW;
        g.drawString(expText, expX, y + 26);
        registerHitbox(expKey, expX - 4, y + 10, expW + 8, 24, null);

        // Subcards: 2 side by side
        int subGap = 16;
        int subW = (w - pad * 2 - subGap) / 2;
        int subH = 208;
        int subY = y + 44;

        // Subcard 1: GreenRoute
        Project p1 = getProjectByTitle("GreenRoute");
        if (p1 != null) {
            paintRecommendedSubCard(g, x + pad, subY, subW, subH, p1, "OVERVIEW_REC_1", "OVERVIEW_BM_GREENROUTE", "OVERVIEW_VIEW_GREENROUTE");
        }

        // Subcard 2: OpenLab
        Project p2 = getProjectByTitle("OpenLab");
        if (p2 != null) {
            paintRecommendedSubCard(g, x + pad + subW + subGap, subY, subW, subH, p2, "OVERVIEW_REC_2", "OVERVIEW_BM_OPENLAB", "OVERVIEW_VIEW_OPENLAB");
        }

        return y + cardH;
    }

    private void paintRecommendedSubCard(Graphics2D g, int cx, int cy, int cw, int ch, Project p, String cardKey, String bmKey, String viewKey) {
        boolean isHov = cardKey.equals(hoveredHitbox);
        RoundRectangle2D shape = new RoundRectangle2D.Float(cx, cy, cw, ch, 12, 12);
        g.setColor(isHov ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(shape);
        g.setColor(isHov ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(isHov ? 1.2f : 1.0f));
        g.draw(shape);

        registerHitbox(cardKey, cx, cy, cw, ch, p);

        int pad = 14;

        // Icon
        int iconSize = 34;
        int iconX = cx + pad;
        int iconY = cy + pad;
        RoundRectangle2D iconShape = new RoundRectangle2D.Float(iconX, iconY, iconSize, iconSize, 8, 8);
        g.setColor(p.iconBg);
        g.fill(iconShape);
        paintVectorIcon(g, p.iconType, iconX + 8, iconY + 8, 18, p.iconColor);

        // Title + Subtitle
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT_DARK);
        g.drawString(p.title, iconX + iconSize + 10, iconY + 16);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(p.creatorName + " · " + p.category, iconX + iconSize + 10, iconY + 30);

        // Bookmark icon top right
        int bmSize = 18;
        int bmX = cx + cw - pad - bmSize;
        int bmY = cy + pad + 2;
        registerHitbox(bmKey, bmX - 4, bmY - 4, bmSize + 8, bmSize + 8, p);
        paintBookmarkIcon(g, bmX, bmY, 15, p.bookmarked);

        // Description
        int descY = iconY + iconSize + 14;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(new Color(0x33, 0x41, 0x55));
        drawWrappedText(g, p.description, cx + pad, descY, cw - pad * 2, 15, 2);

        // Skill pills
        int tagY = descY + 34;
        int tagX = cx + pad;
        for (String s : p.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            int tw = g.getFontMetrics().stringWidth(s) + 12;
            int th = 20;
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fillRoundRect(tagX, tagY, tw, th, 4, 4);
            g.setColor(new Color(0x47, 0x55, 0x69));
            g.drawString(s, tagX + 6, tagY + 14);

            String tagKey = "OVERVIEW_TAG_" + p.id + "_" + s;
            registerHitbox(tagKey, tagX, tagY, tw, th, s);

            tagX += tw + 4;
        }

        // Roles & members line
        int statY = tagY + 28;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("2 open roles · " + p.currentMembers + " / " + p.maxMembers + " members", cx + pad, statY);

        // "View project ->" Link
        boolean vHov = viewKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(vHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String vText = "View project →";
        int vW = g.getFontMetrics().stringWidth(vText);
        int vX = cx + pad;
        int vY = cy + ch - 12;
        g.drawString(vText, vX, vY);
        registerHitbox(viewKey, vX - 4, vY - 14, vW + 8, 20, p);
    }

    private int paintRecentActivityCard(Graphics2D g, int x, int y, int w) {
        int cardH = 224;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(container);

        int pad = 20;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Recent activity", x + pad, y + 26);

        // "View all ->"
        String vaKey = "OVERVIEW_VIEW_ALL_ACTIVITY";
        boolean vaHov = vaKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(vaHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String vaText = "View all →";
        int vaW = g.getFontMetrics().stringWidth(vaText);
        int vaX = x + w - pad - vaW;
        g.drawString(vaText, vaX, y + 26);
        registerHitbox(vaKey, vaX - 4, y + 10, vaW + 8, 24, null);

        int curY = y + 46;
        int rowH = 54;

        for (int i = 0; i < recentActivities.size(); i++) {
            ActivityItem act = recentActivities.get(i);
            String actKey = "OVERVIEW_ACT_" + i;
            registerHitbox(actKey, x + pad, curY, w - pad * 2, rowH, act);

            boolean isHov = actKey.equals(hoveredHitbox);
            if (isHov) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(x + pad - 6, curY - 2, w - pad * 2 + 12, rowH + 4, 8, 8);
            }

            // Avatar
            int avSize = 32;
            int avX = x + pad;
            int avY = curY + (rowH - avSize) / 2;
            g.setColor(act.avatarColor);
            g.fillOval(avX, avY, avSize, avSize);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(Color.WHITE);
            int initW = g.getFontMetrics().stringWidth(act.initials);
            g.drawString(act.initials, avX + (avSize - initW) / 2, avY + 21);

            // Name & Subtext
            int textX = avX + avSize + 12;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            if (act.detail.isEmpty()) {
                g.drawString(act.name, textX, curY + 32);
            } else {
                g.drawString(act.name, textX, curY + 22);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                g.setColor(TEXT_MUTED);
                g.drawString(act.detail, textX, curY + 40);
            }

            // Badge
            if (!act.badge.isEmpty()) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                int bw = g.getFontMetrics().stringWidth(act.badge) + 18;
                int bh = 22;
                int bx = x + w - pad - bw;
                int by = curY + (rowH - bh) / 2;

                RoundRectangle2D badgeShape = new RoundRectangle2D.Float(bx, by, bw, bh, 6, 6);
                g.setColor(act.badgeBg);
                g.fill(badgeShape);
                g.setColor(act.badgeText);
                g.drawString(act.badge, bx + 9, by + 15);
            }

            curY += rowH;
            if (i < recentActivities.size() - 1) {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.drawLine(x + pad, curY, x + w - pad, curY);
            }
        }

        return y + cardH;
    }

    private int paintSuggestedTeammatesCard(Graphics2D g, int x, int y, int w) {
        int cardH = 268;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(container);

        int pad = 20;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Suggested teammates", x + pad, y + 26);

        // "Find more ->"
        String fmKey = "OVERVIEW_FIND_MORE_TEAMMATES";
        boolean fmHov = fmKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(fmHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String fmText = "Find more →";
        int fmW = g.getFontMetrics().stringWidth(fmText);
        int fmX = x + w - pad - fmW;
        g.drawString(fmText, fmX, y + 26);
        registerHitbox(fmKey, fmX - 4, y + 10, fmW + 8, 24, null);

        int curY = y + 46;
        int rowH = 50;

        for (int i = 0; i < suggestedTeammates.size(); i++) {
            TeammateMatch tm = suggestedTeammates.get(i);
            String tmKey = "OVERVIEW_TM_" + i;
            registerHitbox(tmKey, x + pad, curY, w - pad * 2, rowH, tm);

            boolean isHov = tmKey.equals(hoveredHitbox);
            if (isHov) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(x + pad - 6, curY - 2, w - pad * 2 + 12, rowH + 4, 8, 8);
            }

            // Avatar
            int avSize = 32;
            int avX = x + pad;
            int avY = curY + (rowH - avSize) / 2;
            g.setColor(tm.avatarColor);
            g.fillOval(avX, avY, avSize, avSize);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(Color.WHITE);
            int initW = g.getFontMetrics().stringWidth(tm.initials);
            g.drawString(tm.initials, avX + (avSize - initW) / 2, avY + 21);

            // Name & Role
            int textX = avX + avSize + 12;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g.setColor(TEXT_DARK);
            g.drawString(tm.name, textX, curY + 20);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(TEXT_MUTED);
            g.drawString(tm.role, textX, curY + 38);

            // Match badge
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            int bw = g.getFontMetrics().stringWidth(tm.matchPct) + 16;
            int bh = 20;
            int bx = x + w - pad - bw;
            int by = curY + (rowH - bh) / 2;

            RoundRectangle2D badgeShape = new RoundRectangle2D.Float(bx, by, bw, bh, 6, 6);
            g.setColor(tm.invited ? new Color(0xEE, 0xF2, 0xFF) : RECRUIT_BG);
            g.fill(badgeShape);
            g.setColor(tm.invited ? BRAND_PURPLE : RECRUIT_TEXT);
            g.drawString(tm.invited ? "Invited ✓" : tm.matchPct, bx + 8, by + 14);

            curY += rowH + 4;
        }

        // Subtitle: "Illustrative matches based on complementary skills."
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_LIGHT);
        g.drawString("Illustrative matches based on complementary skills.", x + pad, curY + 12);

        // "Find Teammates" Pill Button
        int btnW = w - pad * 2;
        int btnH = 36;
        int btnY = curY + 24;
        String ftKey = "OVERVIEW_FIND_TEAMMATES_BTN";
        registerHitbox(ftKey, x + pad, btnY, btnW, btnH, null);

        boolean ftHov = ftKey.equals(hoveredHitbox);
        RoundRectangle2D ftShape = new RoundRectangle2D.Float(x + pad, btnY, btnW, btnH, 10, 10);
        g.setColor(ftHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(ftShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(ftShape);

        // Icon + Label
        int iconX = x + pad + btnW / 2 - 58;
        paintNavIcon(g, NavItem.FIND_TEAMMATES, iconX, btnY + 10, 16, TEXT_DARK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Find Teammates", iconX + 24, btnY + 23);

        return y + cardH;
    }

    private int paintUpcomingDeadlinesCard(Graphics2D g, int x, int y, int w) {
        int cardH = 224;
        RoundRectangle2D container = new RoundRectangle2D.Float(x, y, w, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(container);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(container);

        int pad = 20;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString("Upcoming deadlines", x + pad, y + 26);

        // "All tasks ->"
        String atKey = "OVERVIEW_ALL_TASKS";
        boolean atHov = atKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(atHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String atText = "All tasks →";
        int atW = g.getFontMetrics().stringWidth(atText);
        int atX = x + w - pad - atW;
        g.drawString(atText, atX, y + 26);
        registerHitbox(atKey, atX - 4, y + 10, atW + 8, 24, null);

        int curY = y + 44;
        int rowH = 40;

        for (int i = 0; i < upcomingTasks.size(); i++) {
            TaskItem t = upcomingTasks.get(i);
            String taskKey = "OVERVIEW_TASK_" + i;
            registerHitbox(taskKey, x + pad, curY, w - pad * 2, rowH, t);

            boolean isHov = taskKey.equals(hoveredHitbox);
            if (isHov) {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fillRoundRect(x + pad - 4, curY - 2, w - pad * 2 + 8, rowH + 4, 6, 6);
            }

            // Calendar icon
            int iconX = x + pad;
            int iconY = curY + 6;
            drawCalendarAlertIcon(g, iconX, iconY, 14, t.overdue ? new Color(0xEF, 0x44, 0x44) : TEXT_LIGHT);

            // Title & Subtitle
            int textX = iconX + 22;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g.setColor(t.completed ? TEXT_LIGHT : TEXT_DARK);
            g.drawString(t.title + (t.completed ? " (Completed)" : ""), textX, curY + 16);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(t.overdue && !t.completed ? new Color(0xEF, 0x44, 0x44) : TEXT_MUTED);
            g.drawString(t.deadline, textX, curY + 32);

            curY += rowH + 4;
        }

        // Bottom divider & Unread conversations line
        int divY = y + cardH - 42;
        g.setColor(BORDER_COLOR);
        g.drawLine(x + pad, divY, x + w - pad, divY);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString("3 unread conversations", x + pad, divY + 26);

        // "View Messages ->" button
        String vmKey = "OVERVIEW_VIEW_MESSAGES";
        boolean vmHov = vmKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(vmHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        String vmText = "View Messages →";
        int vmW = g.getFontMetrics().stringWidth(vmText);
        int vmX = x + w - pad - vmW;
        g.drawString(vmText, vmX, divY + 26);
        registerHitbox(vmKey, vmX - 4, divY + 12, vmW + 8, 22, null);

        return y + cardH;
    }

    private Project getProjectByTitle(String title) {
        for (Project p : allProjects) {
            if (p.title.equalsIgnoreCase(title)) return p;
        }
        return null;
    }

    private void drawFolderIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x, y + 2, x + 5, y + 2);
        g2.drawLine(x + 5, y + 2, x + 8, y + 5);
        g2.drawLine(x + 8, y + 5, x + size, y + 5);
        g2.drawRoundRect(x, y + 5, size, size - 5, 3, 3);
        g2.dispose();
    }

    private void drawUsersIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(x + 4, y, 6, 6);
        g2.drawArc(x, y + 7, 14, 8, 0, 180);
        g2.dispose();
    }

    private void drawUserPlusIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(x + 2, y, 6, 6);
        g2.drawArc(x - 2, y + 7, 14, 8, 0, 180);
        g2.drawLine(x + 13, y + 4, x + 13, y + 10);
        g2.drawLine(x + 10, y + 7, x + 16, y + 7);
        g2.dispose();
    }

    private void drawCalendarAlertIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(x, y + 2, size - 2, size - 2, 3, 3);
        g2.drawLine(x, y + 6, x + size - 2, y + 6);
        g2.drawLine(x + 3, y, x + 3, y + 3);
        g2.drawLine(x + size - 5, y, x + size - 5, y + 3);
        g2.dispose();
    }

    private void drawSparkle(Graphics2D g, int cx, int cy, int r, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx, cy - r);
        p.quadTo(cx, cy, cx + r, cy);
        p.quadTo(cx, cy, cx, cy + r);
        p.quadTo(cx, cy, cx - r, cy);
        p.quadTo(cx, cy, cx, cy - r);
        p.closePath();
        g2.fill(p);
        g2.dispose();
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxW, int lineH, int maxLines) {
        if (text == null || text.isEmpty()) return;
        List<String> lines = Draw.wrapText(text, g.getFontMetrics(), maxW);
        int curY = y;
        int count = 0;
        for (String line : lines) {
            if (count >= maxLines) break;
            g.drawString(line, x, curY);
            curY += lineH;
            count++;
        }
    }

    // =========================================================================
    // FIND TEAMMATES PAGE RENDERING (Matches Reference Design)
    // =========================================================================
    private void paintFindTeammatesContent(Graphics2D g, int startX, int startY, int width, int height) {
        int padX = 36;
        int padY = 28;
        int contentW = width - padX * 2;
        int curY = startY + padY;

        // 1. Header: "Find your next teammate"
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        g.setColor(TEXT_DARK);
        g.drawString("Find your next teammate", startX + padX, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(TEXT_MUTED);
        g.drawString("Different strengths. Shared ambition. Discover people you can build with.", startX + padX, curY + 46);

        curY += 72;

        // 2. Search Box + Filter Dropdowns Row
        int searchBoxW = Math.max(260, contentW - 440);
        int searchBoxH = 40;
        int searchBoxX = startX + padX;
        int searchBoxY = curY;
        registerHitbox("TM_SEARCH", searchBoxX, searchBoxY, searchBoxW, searchBoxH, null);

        RoundRectangle2D sBox = new RoundRectangle2D.Float(searchBoxX, searchBoxY, searchBoxW, searchBoxH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(sBox);
        g.setColor(teammateSearchFocused ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(teammateSearchFocused ? 1.5f : 1.0f));
        g.draw(sBox);

        // Magnifier
        g.setColor(TEXT_LIGHT);
        g.setStroke(new BasicStroke(1.4f));
        g.drawOval(searchBoxX + 14, searchBoxY + 13, 11, 11);
        g.drawLine(searchBoxX + 23, searchBoxY + 22, searchBoxX + 27, searchBoxY + 26);

        // Text or Placeholder
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        if (teammateSearchText.isEmpty() && !teammateSearchFocused) {
            g.setColor(TEXT_LIGHT);
            g.drawString("Search by name, skill, role or interests...", searchBoxX + 38, searchBoxY + 25);
        } else {
            g.setColor(TEXT_DARK);
            g.drawString(teammateSearchText, searchBoxX + 38, searchBoxY + 25);
            if (teammateSearchFocused && caretTimer < 0.5f) {
                int tw = g.getFontMetrics().stringWidth(teammateSearchText);
                g.setColor(BRAND_PURPLE);
                g.drawLine(searchBoxX + 38 + tw, searchBoxY + 12, searchBoxX + 38 + tw, searchBoxY + 28);
            }
        }

        // Clear Search Button
        if (!teammateSearchText.isEmpty()) {
            int clrX = searchBoxX + searchBoxW - 28;
            int clrY = searchBoxY + 12;
            registerHitbox("CLEAR_TM_SEARCH", clrX, clrY, 16, 16, null);
            g.setColor(TEXT_LIGHT);
            g.fillOval(clrX, clrY, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.drawString("×", clrX + 4, clrY + 12);
        }

        // Dropdown Buttons: Skills ˅, Experience ˅, Availability ˅, Rating 4+ ˅
        int dropX = searchBoxX + searchBoxW + 12;
        dropX = paintFilterButton(g, dropX, curY, "Skills", filterTmSkill, "TM_FILTER_SKILLS");
        dropX = paintFilterButton(g, dropX, curY, "Experience", filterTmExperience, "TM_FILTER_EXP");
        dropX = paintFilterButton(g, dropX, curY, "Availability", filterTmAvailability, "TM_FILTER_AVAIL");
        paintFilterButton(g, dropX, curY, "Rating 4+", filterTmRating, "TM_FILTER_RATING");

        curY += searchBoxH + 18;

        // 3. Stats & Active Filters Row
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT_DARK);
        String countStr = filteredTeammates.size() + " teammates";
        g.drawString(countStr, startX + padX, curY + 20);

        int countW = g.getFontMetrics().stringWidth(countStr);
        int chipX = startX + padX + countW + 16;

        // "Available to collaborate" Pill
        String availKey = "TM_AVAIL_CHIP";
        registerHitbox(availKey, chipX, curY + 2, 164, 28, null);
        boolean availHov = availKey.equals(hoveredHitbox);
        RoundRectangle2D availShape = new RoundRectangle2D.Float(chipX, curY + 2, 164, 28, 8, 8);
        g.setColor(filterOnlyAvailable ? PURPLE_TINT_BG : (availHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE));
        g.fill(availShape);
        g.setColor(filterOnlyAvailable ? PURPLE_BORDER : BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(availShape);

        g.setFont(new Font(Font.SANS_SERIF, filterOnlyAvailable ? Font.BOLD : Font.PLAIN, 12));
        g.setColor(filterOnlyAvailable ? BRAND_PURPLE : TEXT_MUTED);
        g.drawString("Available to collaborate", chipX + 12, curY + 20);

        chipX += 164 + 14;

        // "Clear filters" Link
        String clrKey = "TM_CLEAR_FILTERS";
        registerHitbox(clrKey, chipX, curY + 5, 80, 20, null);
        boolean clrHov = clrKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(clrHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE);
        g.drawString("Clear filters", chipX, curY + 20);

        // Right side: Sort Dropdown
        int sortBtnW = 180;
        int sortX = startX + padX + contentW - sortBtnW;
        registerHitbox("TM_SORT_DROPDOWN", sortX, curY, sortBtnW, 34, null);
        boolean sortHov = "TM_SORT_DROPDOWN".equals(hoveredHitbox) || activeDropdown == DropdownType.TM_SORT;

        RoundRectangle2D sortShape = new RoundRectangle2D.Float(sortX, curY, sortBtnW, 34, 8, 8);
        g.setColor(sortHov ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(sortShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(sortShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_DARK);
        g.drawString("Sort: " + selectedTmSort, sortX + 12, curY + 21);
        Draw.drawChevronDown(g, sortX + sortBtnW - 20, curY + 12, 8, TEXT_MUTED, 0f);

        curY += 34 + 18;

        // 4. Teammates Grid (3 Columns x 2 Rows)
        int startIdx = (teammateCurrentPage - 1) * teammatesPerPage;
        int endIdx = Math.min(startIdx + teammatesPerPage, filteredTeammates.size());

        if (filteredTeammates.isEmpty()) {
            paintEmptyState(g, startX + padX, curY, contentW, 260);
            curY += 280;
        } else {
            curY = paintTeammateGrid(g, startX + padX, curY, contentW, startIdx, endIdx);
        }

        curY += 24;

        // 5. Pagination Row
        paintTeammatePagination(g, startX + padX, curY, contentW, filteredTeammates.size());
        curY += 60;

        totalContentHeight = curY - startY + 40;
    }

    private int paintTeammateGrid(Graphics2D g, int x, int y, int w, int startIdx, int endIdx) {
        int cols = (w >= 1080) ? 3 : 2;
        int gap = 20;
        int cardW = (w - gap * (cols - 1)) / cols;
        int cardH = 296;

        int curX = x;
        int curY = y;
        int colCount = 0;

        for (int i = startIdx; i < endIdx; i++) {
            Teammate tm = filteredTeammates.get(i);
            paintSingleTeammateCard(g, curX, curY, cardW, cardH, tm);

            colCount++;
            if (colCount % cols == 0) {
                colCount = 0;
                curX = x;
                curY += cardH + gap;
            } else {
                curX += cardW + gap;
            }
        }

        if (colCount != 0) {
            curY += cardH + gap;
        }
        return curY;
    }

    private void paintSingleTeammateCard(Graphics2D g, int cx, int cy, int cw, int ch, Teammate tm) {
        String cardKey = "TM_CARD_" + tm.id;
        boolean isHov = cardKey.equals(hoveredHitbox);

        RoundRectangle2D cardShape = new RoundRectangle2D.Float(cx, cy, cw, ch, 14, 14);
        g.setColor(CARD_BG);
        g.fill(cardShape);
        g.setColor(isHov ? BRAND_PURPLE : BORDER_COLOR);
        g.setStroke(new BasicStroke(isHov ? 1.5f : 1.0f));
        g.draw(cardShape);

        int pad = 20;

        // 1. Avatar Circle
        int avSize = 40;
        int avX = cx + pad;
        int avY = cy + pad;
        g.setColor(tm.avatarColor);
        g.fillOval(avX, avY, avSize, avSize);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.WHITE);
        int initW = g.getFontMetrics().stringWidth(tm.initials);
        g.drawString(tm.initials, avX + (avSize - initW) / 2, avY + 25);

        // 2. Name & Role · University
        int textX = avX + avSize + 12;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT_DARK);
        g.drawString(tm.name, textX, avY + 18);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(tm.role + " · " + tm.university, textX, avY + 34);

        // 3. Status & Rating Row
        int statY = avY + avSize + 16;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(new Color(0x15, 0x80, 0x3D)); // Green
        g.drawString(tm.availability, cx + pad, statY);

        // Rating
        String rateStr = String.format(java.util.Locale.US, "★ %.1f (%d reviews)", tm.rating, tm.reviews);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int rateW = g.getFontMetrics().stringWidth(rateStr);
        int rateX = cx + cw - pad - rateW;
        g.setColor(new Color(0xD9, 0x77, 0x06));
        g.drawString("★", rateX, statY);
        g.setColor(TEXT_MUTED);
        g.drawString(rateStr.substring(1), rateX + 12, statY);

        // 4. Skills Pills Row
        int tagY = statY + 12;
        int tagX = cx + pad;
        for (String s : tm.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            int tw = g.getFontMetrics().stringWidth(s) + 16;
            int th = 22;

            RoundRectangle2D tagShape = new RoundRectangle2D.Float(tagX, tagY, tw, th, 6, 6);
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(tagShape);
            g.setColor(new Color(0x47, 0x55, 0x69));
            g.drawString(s, tagX + 8, tagY + 15);

            String tagKey = "TM_TAG_" + tm.id + "_" + s;
            registerHitbox(tagKey, tagX, tagY, tw, th, s);

            tagX += tw + 6;
        }

        // 5. Match Box Banner
        int matchY = tagY + 32;
        int matchH = 56;
        RoundRectangle2D matchShape = new RoundRectangle2D.Float(cx + pad, matchY, cw - pad * 2, matchH, 8, 8);
        g.setColor(new Color(0xEC, 0xFD, 0xF5)); // Soft emerald
        g.fill(matchShape);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(new Color(0x0F, 0x76, 0x6E));
        g.drawString(tm.matchPct, cx + pad + 10, matchY + 18);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(new Color(0x33, 0x41, 0x55));
        drawWrappedText(g, tm.matchDesc, cx + pad + 10, matchY + 34, cw - pad * 2 - 20, 15, 2);

        // 6. Portfolio Link
        int portY = matchY + matchH + 18;
        drawLinkIcon(g, cx + pad, portY - 9, 12, TEXT_MUTED);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        g.drawString(tm.portfolio, cx + pad + 18, portY + 2);
        registerHitbox("TM_PORTFOLIO_" + tm.id, cx + pad, portY - 10, 160, 18, tm.portfolio);

        // 7. Bottom Action Buttons: [View Profile] [+ Invite] [Request]
        int btnY = cy + ch - pad - 30;
        int curBtnX = cx + pad;

        // "View Profile" Button
        int vpW = 96;
        int bH = 30;
        String vpKey = "TM_VIEW_PROFILE_" + tm.id;
        registerHitbox(vpKey, curBtnX, btnY, vpW, bH, tm);
        boolean vpHov = vpKey.equals(hoveredHitbox);
        RoundRectangle2D vpShape = new RoundRectangle2D.Float(curBtnX, btnY, vpW, bH, 8, 8);
        g.setColor(vpHov ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
        g.fill(vpShape);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(vpShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(TEXT_DARK);
        g.drawString("View Profile", curBtnX + 14, btnY + 19);

        curBtnX += vpW + 8;

        // "+ Invite" Button
        int invW = 84;
        String invKey = "TM_INVITE_" + tm.id;
        registerHitbox(invKey, curBtnX, btnY, invW, bH, tm);
        boolean invHov = invKey.equals(hoveredHitbox);
        RoundRectangle2D invShape = new RoundRectangle2D.Float(curBtnX, btnY, invW, bH, 8, 8);
        g.setColor(tm.invited ? new Color(0x10, 0xB9, 0x81) : (invHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE));
        g.fill(invShape);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.setColor(Color.WHITE);
        String invText = tm.invited ? "✓ Invited" : "+ Invite";
        int itW = g.getFontMetrics().stringWidth(invText);
        g.drawString(invText, curBtnX + (invW - itW) / 2, btnY + 19);

        curBtnX += invW + 12;

        // "Request" Button / Link
        String reqKey = "TM_REQUEST_" + tm.id;
        registerHitbox(reqKey, curBtnX, btnY + 4, 60, 22, tm);
        boolean reqHov = reqKey.equals(hoveredHitbox);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(tm.requested ? TEXT_LIGHT : (reqHov ? BRAND_PURPLE_HOVER : BRAND_PURPLE));
        g.drawString(tm.requested ? "Sent ✓" : "Request", curBtnX, btnY + 20);
    }

    private void paintTeammatePagination(Graphics2D g, int x, int y, int w, int totalItems) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / teammatesPerPage));
        int startItem = totalItems == 0 ? 0 : (teammateCurrentPage - 1) * teammatesPerPage + 1;
        int endItem = Math.min(teammateCurrentPage * teammatesPerPage, totalItems);

        // Left info
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        String info = "Showing " + startItem + "-" + endItem + " of " + totalItems + " teammates · Matches are illustrative, not computed";
        g.drawString(info, x, y + 22);

        // Right buttons
        int curX = x + w;

        // "Next ->" Button
        boolean nextDisabled = (teammateCurrentPage >= totalPages);
        int nextW = 76;
        int btnH = 34;
        curX -= nextW;
        registerHitbox("TM_PAGE_NEXT", curX, y, nextW, btnH, null);
        boolean nextHov = "TM_PAGE_NEXT".equals(hoveredHitbox) && !nextDisabled;
        paintPageBtn(g, curX, y, nextW, btnH, "Next →", false, nextDisabled, nextHov);

        // Numbers
        for (int p = totalPages; p >= 1; p--) {
            int numW = 34;
            curX -= (numW + 6);
            String pKey = "TM_PAGE_NUM_" + p;
            registerHitbox(pKey, curX, y, numW, btnH, p);
            boolean isCur = (p == teammateCurrentPage);
            boolean isHov = pKey.equals(hoveredHitbox);
            paintPageBtn(g, curX, y, numW, btnH, String.valueOf(p), isCur, false, isHov);
        }

        // "<- Previous" Button
        boolean prevDisabled = (teammateCurrentPage <= 1);
        int prevW = 94;
        curX -= (prevW + 6);
        registerHitbox("TM_PAGE_PREV", curX, y, prevW, btnH, null);
        boolean prevHov = "TM_PAGE_PREV".equals(hoveredHitbox) && !prevDisabled;
        paintPageBtn(g, curX, y, prevW, btnH, "← Previous", false, prevDisabled, prevHov);
    }

    private void paintTeammateProfileModal(Graphics2D g, int sw, int sh) {
        Teammate tm = teammateModalUser;
        if (tm == null) return;

        // Dark backdrop
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, sw, sh);

        int mw = Math.min(600, sw - 80);
        int mh = Math.min(520, sh - 80);
        int mx = (sw - mw) / 2;
        int my = (sh - mh) / 2;

        registerHitbox("TM_MODAL_CONTAINER", mx, my, mw, mh, null);

        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(BORDER_COLOR);
        g.draw(mShape);

        int pad = 28;
        int curY = my + pad;

        // Avatar + Name + Subtitle
        int avSize = 48;
        g.setColor(tm.avatarColor);
        g.fillOval(mx + pad, curY, avSize, avSize);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(Color.WHITE);
        int initW = g.getFontMetrics().stringWidth(tm.initials);
        g.drawString(tm.initials, mx + pad + (avSize - initW) / 2, curY + 30);

        int textX = mx + pad + avSize + 16;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(TEXT_DARK);
        g.drawString(tm.name, textX, curY + 22);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString(tm.role + " · " + tm.university, textX, curY + 42);

        // Close ✕ Button
        int closeX = mx + mw - pad - 20;
        registerHitbox("TM_MODAL_CLOSE", closeX - 6, curY, 32, 32, null);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(TEXT_LIGHT);
        g.drawString("✕", closeX, curY + 16);

        curY += avSize + 22;

        // Divider
        g.setColor(BORDER_COLOR);
        g.drawLine(mx + pad, curY, mx + mw - pad, curY);
        curY += 20;

        // Bio section
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("About " + tm.name, mx + pad, curY);
        curY += 16;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(new Color(0x33, 0x41, 0x55));
        drawWrappedText(g, tm.matchDesc + " Actively studying at " + tm.university + " and ready to collaborate on innovative student hackathons and capstone projects.", mx + pad, curY, mw - pad * 2, 18, 3);
        curY += 60;

        // Verified Skills
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(TEXT_DARK);
        g.drawString("Verified Skills", mx + pad, curY);
        curY += 14;

        int tagX = mx + pad;
        for (String s : tm.skills) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            int tw = g.getFontMetrics().stringWidth(s) + 20;
            g.setColor(PURPLE_TINT_BG);
            g.fillRoundRect(tagX, curY, tw, 26, 8, 8);
            g.setColor(BRAND_PURPLE);
            g.drawString(s, tagX + 10, curY + 18);
            tagX += tw + 8;
        }
        curY += 42;

        // Status Card
        RoundRectangle2D statBox = new RoundRectangle2D.Float(mx + pad, curY, mw - pad * 2, 60, 10, 10);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(statBox);
        g.setColor(BORDER_COLOR);
        g.draw(statBox);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(new Color(0x15, 0x80, 0x3D));
        g.drawString(tm.availability, mx + pad + 16, curY + 24);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setColor(TEXT_MUTED);
        g.drawString("Rating: ★ " + tm.rating + " (" + tm.reviews + " reviews) · Portfolio: " + tm.portfolio, mx + pad + 16, curY + 44);

        curY += 76;

        // Action Buttons Row
        int btnH = 42;
        int invW = 160;
        int invX = mx + mw - pad - invW;
        int actY = my + mh - pad - btnH;
        registerHitbox("TM_MODAL_INVITE", invX, actY, invW, btnH, tm);

        RoundRectangle2D invBtn = new RoundRectangle2D.Float(invX, actY, invW, btnH, 10, 10);
        g.setColor(tm.invited ? new Color(0x10, 0xB9, 0x81) : BRAND_PURPLE);
        g.fill(invBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(Color.WHITE);
        String inText = tm.invited ? "✓ Invited" : "+ Invite to Team";
        int inW = g.getFontMetrics().stringWidth(inText);
        g.drawString(inText, invX + (invW - inW) / 2, actY + 26);

        // "Send Message" Button
        int msgW = 140;
        int msgX = invX - msgW - 12;
        registerHitbox("TM_MODAL_MSG", msgX, actY, msgW, btnH, tm);
        RoundRectangle2D msgBtn = new RoundRectangle2D.Float(msgX, actY, msgW, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(msgBtn);
        g.setColor(BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(msgBtn);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(TEXT_DARK);
        String msText = "Send Message";
        int msW = g.getFontMetrics().stringWidth(msText);
        g.drawString(msText, msgX + (msgW - msW) / 2, actY + 26);
    }

    private void drawLinkIcon(Graphics2D g, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(x, y + 2, size - 3, size - 3, 2, 2);
        g2.drawLine(x + size - 5, y, x + size, y);
        g2.drawLine(x + size, y + 5, x + size, y);
        g2.drawLine(x + size / 2, y + size / 2, x + size, y);
        g2.dispose();
    }

    // =========================================================================
    // ICONS DRAWING HELPER
    // =========================================================================
    private void paintVectorIcon(Graphics2D g, int type, int x, int y, int size, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case 0: // Calendar
                g2.drawRoundRect(x, y + 2, size, size - 2, 4, 4);
                g2.drawLine(x, y + 8, x + size, y + 8);
                g2.drawLine(x + 5, y, x + 5, y + 4);
                g2.drawLine(x + size - 5, y, x + size - 5, y + 4);
                // dots
                g2.fillRect(x + 5, y + 12, 2, 2);
                g2.fillRect(x + 10, y + 12, 2, 2);
                g2.fillRect(x + 15, y + 12, 2, 2);
                break;
            case 1: // Leaf / Sprout
                g2.drawArc(x, y, size, size, 0, 90);
                g2.drawArc(x, y, size, size, 180, 90);
                g2.drawLine(x, y + size, x + size, y);
                break;
            case 2: // Flask / Beaker
                g2.drawLine(x + 7, y, x + size - 7, y);
                g2.drawLine(x + 8, y, x + 8, y + 6);
                g2.drawLine(x + size - 8, y, x + size - 8, y + 6);
                g2.drawLine(x + 8, y + 6, x + 2, y + size);
                g2.drawLine(x + size - 8, y + 6, x + size - 2, y + size);
                g2.drawLine(x + 2, y + size, x + size - 2, y + size);
                g2.drawLine(x + 5, y + size - 5, x + size - 5, y + size - 5);
                break;
            case 3: // Map Pin
                g2.drawOval(x + 4, y, size - 8, size - 8);
                g2.drawLine(x + 4, y + 6, x + size / 2, y + size);
                g2.drawLine(x + size - 4, y + 6, x + size / 2, y + size);
                g2.drawOval(x + size / 2 - 2, y + 4, 4, 4);
                break;
            case 4: // Open Book
                g2.drawArc(x, y + 2, size / 2, 6, 180, 180);
                g2.drawArc(x + size / 2, y + 2, size / 2, 6, 180, 180);
                g2.drawLine(x, y + 5, x, y + size);
                g2.drawLine(x + size / 2, y + 8, x + size / 2, y + size + 2);
                g2.drawLine(x + size, y + 5, x + size, y + size);
                g2.drawArc(x, y + size - 3, size / 2, 6, 180, 180);
                g2.drawArc(x + size / 2, y + size - 3, size / 2, 6, 180, 180);
                break;
            case 5: // Food (Fork & Spoon)
                // Fork
                g2.drawLine(x + 4, y, x + 4, y + 7);
                g2.drawLine(x + 8, y, x + 8, y + 7);
                g2.drawLine(x + 6, y + 7, x + 6, y + size);
                // Spoon
                g2.drawOval(x + 12, y, 6, 8);
                g2.drawLine(x + 15, y + 8, x + 15, y + size);
                break;
            default:
                g2.drawRect(x + 2, y + 2, size - 4, size - 4);
                break;
        }
        g2.dispose();
    }

    private void paintBookmarkIcon(Graphics2D g, int x, int y, int size, boolean filled) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(filled ? BRAND_PURPLE : TEXT_LIGHT);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D.Float path = new Path2D.Float();
        path.moveTo(x, y);
        path.lineTo(x + size, y);
        path.lineTo(x + size, y + size + 4);
        path.lineTo(x + size / 2f, y + size - 2);
        path.lineTo(x, y + size + 4);
        path.closePath();

        if (filled) {
            g2.fill(path);
        } else {
            g2.draw(path);
        }
        g2.dispose();
    }

    private void paintContentScrollbar(Graphics2D g, int x, int y, int w, int h) {
        int maxScroll = Math.max(0, totalContentHeight - h);
        if (maxScroll <= 0) return;

        float ratio = (float) h / (float) totalContentHeight;
        int thumbH = Math.max(36, Math.round(h * ratio));
        float scrollPct = scrollY / (float) maxScroll;
        int thumbY = y + Math.round((h - thumbH) * scrollPct);

        g.setColor(new Color(0, 0, 0, 20));
        g.fillRoundRect(x, thumbY, w, thumbH, w, w);
    }

    private void paintToast(Graphics2D g, int width, int height) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        int strW = g.getFontMetrics().stringWidth(toastMessage);
        int tw = strW + 36;
        int th = 38;
        int tx = (width - tw) / 2;
        int ty = height - 60;

        RoundRectangle2D toastShape = new RoundRectangle2D.Float(tx, ty, tw, th, 19, 19);
        g.setColor(new Color(0x0F, 0x17, 0x2A));
        g.fill(toastShape);

        g.setColor(Color.WHITE);
        g.drawString(toastMessage, tx + 18, ty + 24);
    }

    // =========================================================================
    // HITBOX REGISTRATION & COORDINATE HELPERS
    // =========================================================================
    private void registerHitbox(String key, int x, int y, int w, int h, Object payload) {
        hitboxes.put(key, new Rectangle(x, y, w, h));
        if (payload != null) {
            hitboxPayloads.put(key, payload);
        }
    }

    private int getHitboxX(String key) {
        Rectangle r = hitboxes.get(key);
        return r != null ? r.x : 0;
    }

    // =========================================================================
    // EVENT HANDLING (Making 100% of the buttons work!)
    // =========================================================================
    public boolean onClick(int mx, int my) {
        // 1. Modals check first
        if (detailModalProject != null) {
            Rectangle mBox = hitboxes.get("MODAL_CONTAINER");
            if (mBox != null && !mBox.contains(mx, my)) {
                detailModalProject = null;
                return true;
            }
            if (isHit("MODAL_CLOSE", mx, my)) {
                detailModalProject = null;
                return true;
            }
            if (isHit("MODAL_APPLY", mx, my)) {
                detailModalProject.applied = !detailModalProject.applied;
                showToast(detailModalProject.applied ?
                        "Application sent to " + detailModalProject.title + "!" :
                        "Application withdrawn.");
                return true;
            }
            if (isHit("MODAL_BOOKMARK", mx, my)) {
                detailModalProject.bookmarked = !detailModalProject.bookmarked;
                showToast(detailModalProject.bookmarked ? "Project bookmarked!" : "Bookmark removed.");
                return true;
            }
            return true;
        }

        if (teammateModalUser != null) {
            Rectangle mBox = hitboxes.get("TM_MODAL_CONTAINER");
            if (mBox != null && !mBox.contains(mx, my)) {
                teammateModalUser = null;
                return true;
            }
            if (isHit("TM_MODAL_CLOSE", mx, my)) {
                teammateModalUser = null;
                return true;
            }
            if (isHit("TM_MODAL_INVITE", mx, my)) {
                teammateModalUser.invited = !teammateModalUser.invited;
                showToast(teammateModalUser.invited ?
                        ("Invitation sent to " + teammateModalUser.name + "!") :
                        ("Invitation withdrawn."));
                return true;
            }
            if (isHit("TM_MODAL_MSG", mx, my)) {
                showToast("Opening conversation with " + teammateModalUser.name + "...");
                teammateModalUser = null;
                activeNav = NavItem.MESSAGES;
                return true;
            }
            return true;
        }

        if (showCreateModal) {
            Rectangle mBox = hitboxes.get("CREATE_MODAL_CONTAINER");
            if (mBox != null && !mBox.contains(mx, my)) {
                showCreateModal = false;
                return true;
            }
            if (isHit("CREATE_MODAL_CLOSE", mx, my)) {
                showCreateModal = false;
                return true;
            }
            if (isHit("INPUT_TITLE", mx, my)) { createModalActiveField = 0; return true; }
            if (isHit("INPUT_SKILLS", mx, my)) { createModalActiveField = 1; return true; }
            if (isHit("INPUT_ROLES", mx, my)) { createModalActiveField = 2; return true; }
            if (isHit("INPUT_DESC", mx, my)) { createModalActiveField = 3; return true; }

            for (String mc : new String[]{"Campus life", "Education", "Sustainability"}) {
                if (isHit("CREATE_CAT_" + mc, mx, my)) {
                    newProjCategory = mc;
                    return true;
                }
            }

            if (isHit("SUBMIT_NEW_PROJECT", mx, my)) {
                String title = newProjTitle.trim().isEmpty() ? "My Student Initiative" : newProjTitle.trim();
                String[] skArr = newProjSkills.split(",");
                for (int i = 0; i < skArr.length; i++) skArr[i] = skArr[i].trim();

                Project newP = new Project(
                        allProjects.size() + 1, title, newProjCategory, "Intermediate",
                        newProjDesc.trim().isEmpty() ? "Collaborative student project built for university innovation." : newProjDesc.trim(),
                        skArr, true, 1, 4, "Open: " + newProjRoles,
                        "Alex Morgan", "AM", BRAND_PURPLE,
                        0, PURPLE_TINT_BG, BRAND_PURPLE
                );
                allProjects.add(0, newP);
                applyFilters();
                currentPage = 1;
                showCreateModal = false;
                showToast("Project '" + title + "' published successfully!");
                return true;
            }
            return true;
        }

        // 2. Dropdowns selection & Dismissal
        if (activeDropdown != DropdownType.NONE) {
            // Check Dropdown items
            for (Map.Entry<String, Rectangle> entry : hitboxes.entrySet()) {
                if (entry.getKey().startsWith("SELECT_") || entry.getKey().startsWith("PROF_ITEM_") || entry.getKey().equals("NOTIF_MARK_READ")) {
                    if (entry.getValue().contains(mx, my)) {
                        handleDropdownItemClick(entry.getKey());
                        activeDropdown = DropdownType.NONE;
                        return true;
                    }
                }
            }
            activeDropdown = DropdownType.NONE;
            return true;
        }

        // 3. Top Header Actions
        if (isHit("TOP_CREATE_PROJECT", mx, my) || isHit("MAIN_CREATE_PROJECT", mx, my)) {
            showCreateModal = true;
            newProjTitle = "";
            newProjDesc = "";
            createModalActiveField = 0;
            return true;
        }

        if (isHit("TOP_BELL", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.NOTIFICATIONS) ? DropdownType.NONE : DropdownType.NOTIFICATIONS;
            return true;
        }

        if (isHit("TOP_PROFILE", mx, my) || isHit("BOTTOM_PROFILE", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.PROFILE) ? DropdownType.NONE : DropdownType.PROFILE;
            return true;
        }

        if (isHit("TOP_SEARCH", mx, my)) {
            topSearchFocused = true;
            contentSearchFocused = false;
            return true;
        }

        if (isHit("CONTENT_SEARCH", mx, my)) {
            contentSearchFocused = true;
            topSearchFocused = false;
            return true;
        } else {
            contentSearchFocused = false;
            topSearchFocused = false;
        }

        if (isHit("CLEAR_SEARCH", mx, my)) {
            contentSearchText = "";
            topSearchText = "";
            applyFilters();
            return true;
        }

        // Overview Page Interactive Actions
        if (activeNav == NavItem.OVERVIEW) {
            if (isHit("OVERVIEW_CREATE_PROJECT", mx, my)) {
                showCreateModal = true;
                newProjTitle = "";
                newProjDesc = "";
                createModalActiveField = 0;
                return true;
            }
            if (isHit("OVERVIEW_COMPLETE_PROFILE", mx, my)) {
                showToast("Opening profile editor (80% complete)...");
                return true;
            }
            if (isHit("STAT_0", mx, my)) {
                activeNav = NavItem.MY_PROJECTS;
                showToast("Viewing 3 active collaborative projects");
                return true;
            }
            if (isHit("STAT_1", mx, my)) {
                activeNav = NavItem.MY_TEAMS;
                showToast("Viewing 4 teams (3 active, 1 completed)");
                return true;
            }
            if (isHit("STAT_2", mx, my)) {
                showToast("5 Pending Requests: 3 team invites, 2 applications");
                return true;
            }
            if (isHit("STAT_3", mx, my)) {
                showToast("6 Tasks due: 1 task overdue on CampusConnect");
                return true;
            }
            if (isHit("OVERVIEW_EXPLORE_ALL", mx, my)) {
                activeNav = NavItem.DISCOVER_PROJECTS;
                showToast("Browsing verified student projects...");
                return true;
            }
            if (isHit("OVERVIEW_VIEW_GREENROUTE", mx, my)) {
                detailModalProject = getProjectByTitle("GreenRoute");
                return true;
            }
            if (isHit("OVERVIEW_VIEW_OPENLAB", mx, my)) {
                detailModalProject = getProjectByTitle("OpenLab");
                return true;
            }
            if (isHit("OVERVIEW_BM_GREENROUTE", mx, my)) {
                Project p = getProjectByTitle("GreenRoute");
                if (p != null) {
                    p.bookmarked = !p.bookmarked;
                    showToast(p.bookmarked ? "GreenRoute added to Bookmarks ★" : "GreenRoute removed from Bookmarks");
                    return true;
                }
            }
            if (isHit("OVERVIEW_BM_OPENLAB", mx, my)) {
                Project p = getProjectByTitle("OpenLab");
                if (p != null) {
                    p.bookmarked = !p.bookmarked;
                    showToast(p.bookmarked ? "OpenLab added to Bookmarks ★" : "OpenLab removed from Bookmarks");
                    return true;
                }
            }
            if (isHit("OVERVIEW_VIEW_ALL_ACTIVITY", mx, my)) {
                showToast("Showing complete workspace team activity timeline.");
                return true;
            }
            for (int i = 0; i < recentActivities.size(); i++) {
                if (isHit("OVERVIEW_ACT_" + i, mx, my)) {
                    showToast("Activity update: " + recentActivities.get(i).name);
                    return true;
                }
            }
            if (isHit("OVERVIEW_FIND_MORE_TEAMMATES", mx, my) || isHit("OVERVIEW_FIND_TEAMMATES_BTN", mx, my)) {
                activeNav = NavItem.FIND_TEAMMATES;
                showToast("Opening Find Teammates discovery...");
                return true;
            }
            for (int i = 0; i < suggestedTeammates.size(); i++) {
                if (isHit("OVERVIEW_TM_" + i, mx, my)) {
                    TeammateMatch tm = suggestedTeammates.get(i);
                    tm.invited = !tm.invited;
                    showToast(tm.invited ? ("Invitation sent to " + tm.name + "!") : ("Invitation withdrawn for " + tm.name));
                    return true;
                }
            }
            if (isHit("OVERVIEW_ALL_TASKS", mx, my)) {
                showToast("Opening upcoming sprint tasks...");
                return true;
            }
            for (int i = 0; i < upcomingTasks.size(); i++) {
                if (isHit("OVERVIEW_TASK_" + i, mx, my)) {
                    TaskItem t = upcomingTasks.get(i);
                    t.completed = !t.completed;
                    showToast(t.completed ? ("Completed: " + t.title) : ("Reopened: " + t.title));
                    return true;
                }
            }
            if (isHit("OVERVIEW_VIEW_MESSAGES", mx, my)) {
                activeNav = NavItem.MESSAGES;
                showToast("Opening 3 unread messages in workspace...");
                return true;
            }
        }

        // Find Teammates Page Interactive Actions
        if (activeNav == NavItem.FIND_TEAMMATES) {
            if (isHit("TM_SEARCH", mx, my)) {
                teammateSearchFocused = true;
                topSearchFocused = false;
                return true;
            } else {
                teammateSearchFocused = false;
            }

            if (isHit("CLEAR_TM_SEARCH", mx, my)) {
                teammateSearchText = "";
                applyTeammateFilters();
                return true;
            }

            if (isHit("TM_FILTER_SKILLS", mx, my)) {
                activeDropdown = (activeDropdown == DropdownType.TM_SKILLS) ? DropdownType.NONE : DropdownType.TM_SKILLS;
                return true;
            }
            if (isHit("TM_FILTER_EXP", mx, my)) {
                activeDropdown = (activeDropdown == DropdownType.TM_EXPERIENCE) ? DropdownType.NONE : DropdownType.TM_EXPERIENCE;
                return true;
            }
            if (isHit("TM_FILTER_AVAIL", mx, my)) {
                activeDropdown = (activeDropdown == DropdownType.TM_AVAILABILITY) ? DropdownType.NONE : DropdownType.TM_AVAILABILITY;
                return true;
            }
            if (isHit("TM_FILTER_RATING", mx, my)) {
                activeDropdown = (activeDropdown == DropdownType.TM_RATING) ? DropdownType.NONE : DropdownType.TM_RATING;
                return true;
            }
            if (isHit("TM_SORT_DROPDOWN", mx, my)) {
                activeDropdown = (activeDropdown == DropdownType.TM_SORT) ? DropdownType.NONE : DropdownType.TM_SORT;
                return true;
            }

            if (isHit("TM_AVAIL_CHIP", mx, my)) {
                filterOnlyAvailable = !filterOnlyAvailable;
                applyTeammateFilters();
                showToast(filterOnlyAvailable ? "Showing only currently available peers" : "Showing all peers");
                return true;
            }

            if (isHit("TM_CLEAR_FILTERS", mx, my)) {
                teammateSearchText = "";
                filterTmSkill = "All";
                filterTmExperience = "All";
                filterTmAvailability = "All";
                filterTmRating = "All";
                filterOnlyAvailable = true;
                applyTeammateFilters();
                showToast("Teammate filters reset.");
                return true;
            }

            // Teammate Card Actions
            for (Map.Entry<String, Rectangle> entry : hitboxes.entrySet()) {
                String k = entry.getKey();
                if (k.startsWith("TM_VIEW_PROFILE_") && entry.getValue().contains(mx, my)) {
                    teammateModalUser = (Teammate) hitboxPayloads.get(k);
                    return true;
                }
                if (k.startsWith("TM_INVITE_") && entry.getValue().contains(mx, my)) {
                    Teammate tm = (Teammate) hitboxPayloads.get(k);
                    if (tm != null) {
                        tm.invited = !tm.invited;
                        showToast(tm.invited ? ("Invitation sent to " + tm.name + "!") : ("Invitation withdrawn for " + tm.name));
                        return true;
                    }
                }
                if (k.startsWith("TM_REQUEST_") && entry.getValue().contains(mx, my)) {
                    Teammate tm = (Teammate) hitboxPayloads.get(k);
                    if (tm != null) {
                        tm.requested = !tm.requested;
                        showToast(tm.requested ? ("Collaboration request sent to " + tm.name + "!") : ("Request withdrawn for " + tm.name));
                        return true;
                    }
                }
                if (k.startsWith("TM_PORTFOLIO_") && entry.getValue().contains(mx, my)) {
                    String url = (String) hitboxPayloads.get(k);
                    if (url != null) {
                        showToast("Opening student portfolio: " + url);
                        return true;
                    }
                }
                if (k.startsWith("TM_TAG_") && entry.getValue().contains(mx, my)) {
                    String sk = (String) hitboxPayloads.get(k);
                    if (sk != null) {
                        teammateSearchText = sk;
                        applyTeammateFilters();
                        showToast("Filtered teammates by skill: " + sk);
                        return true;
                    }
                }
            }

            // Teammate Pagination
            if (isHit("TM_PAGE_PREV", mx, my) && teammateCurrentPage > 1) {
                teammateCurrentPage--;
                targetScrollY = 0f;
                return true;
            }
            if (isHit("TM_PAGE_NEXT", mx, my)) {
                int maxP = Math.max(1, (int) Math.ceil((double) filteredTeammates.size() / teammatesPerPage));
                if (teammateCurrentPage < maxP) {
                    teammateCurrentPage++;
                    targetScrollY = 0f;
                    return true;
                }
            }
            for (int p = 1; p <= 6; p++) {
                if (isHit("TM_PAGE_NUM_" + p, mx, my)) {
                    teammateCurrentPage = p;
                    targetScrollY = 0f;
                    return true;
                }
            }
        }

        // 4. Filter Dropdown Toggles
        if (isHit("FILTER_CATEGORY", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.CATEGORY) ? DropdownType.NONE : DropdownType.CATEGORY;
            return true;
        }
        if (isHit("FILTER_SKILLS", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.SKILLS) ? DropdownType.NONE : DropdownType.SKILLS;
            return true;
        }
        if (isHit("FILTER_EXPERIENCE", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.EXPERIENCE) ? DropdownType.NONE : DropdownType.EXPERIENCE;
            return true;
        }
        if (isHit("FILTER_RECRUITING", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.RECRUITING) ? DropdownType.NONE : DropdownType.RECRUITING;
            return true;
        }
        if (isHit("SORT_DROPDOWN", mx, my)) {
            activeDropdown = (activeDropdown == DropdownType.SORT) ? DropdownType.NONE : DropdownType.SORT;
            return true;
        }

        // 5. Category Chips Click
        for (String cat : categoryChips) {
            if (isHit("CHIP_" + cat, mx, my)) {
                selectedCategory = cat;
                currentPage = 1;
                applyFilters();
                return true;
            }
        }

        // 6. View Mode Toggles
        if (isHit("VIEW_GRID", mx, my)) {
            isGridView = true;
            return true;
        }
        if (isHit("VIEW_LIST", mx, my)) {
            isGridView = false;
            return true;
        }

        // 7. Reset Filters Button (Empty state)
        if (isHit("RESET_FILTERS", mx, my)) {
            selectedCategory = "All projects";
            filterCategory = "All";
            filterSkill = "All";
            filterExperience = "All";
            filterRecruiting = "All";
            contentSearchText = "";
            topSearchText = "";
            applyFilters();
            showToast("All filters reset.");
            return true;
        }

        // 8. Project Card Buttons (Bookmark, View Details, Skill Tags)
        for (Map.Entry<String, Rectangle> entry : hitboxes.entrySet()) {
            if (entry.getKey().startsWith("BOOKMARK_") && entry.getValue().contains(mx, my)) {
                Project p = (Project) hitboxPayloads.get(entry.getKey());
                if (p != null) {
                    p.bookmarked = !p.bookmarked;
                    showToast(p.bookmarked ? (p.title + " added to Bookmarks ★") : (p.title + " removed from Bookmarks"));
                    return true;
                }
            }
            if (entry.getKey().startsWith("VIEW_DETAILS_") && entry.getValue().contains(mx, my)) {
                detailModalProject = (Project) hitboxPayloads.get(entry.getKey());
                return true;
            }
            if (entry.getKey().startsWith("TAG_") && entry.getValue().contains(mx, my)) {
                String sk = (String) hitboxPayloads.get(entry.getKey());
                if (sk != null) {
                    contentSearchText = sk;
                    applyFilters();
                    showToast("Filtered by skill: " + sk);
                    return true;
                }
            }
        }

        // 9. Pagination Clicks
        if (isHit("PAGE_PREV", mx, my) && currentPage > 1) {
            currentPage--;
            targetScrollY = 0f;
            return true;
        }
        if (isHit("PAGE_NEXT", mx, my)) {
            int maxP = Math.max(1, (int) Math.ceil((double) filteredProjects.size() / itemsPerPage));
            if (currentPage < maxP) {
                currentPage++;
                targetScrollY = 0f;
                return true;
            }
        }
        for (int p = 1; p <= 5; p++) {
            if (isHit("PAGE_NUM_" + p, mx, my)) {
                currentPage = p;
                targetScrollY = 0f;
                return true;
            }
        }

        // 10. Sidebar Navigation Items
        for (NavItem item : NavItem.values()) {
            if (isHit("NAV_" + item.name(), mx, my)) {
                activeNav = item;
                if (item == NavItem.DISCOVER_PROJECTS) {
                    showToast("Browsing projects looking for teammates...");
                } else if (item == NavItem.OVERVIEW) {
                    showToast("Opening Alex's Workspace Overview...");
                } else {
                    showToast("Navigated to " + item.label + " (Demo view active)");
                }
                return true;
            }
        }

        // Return to Landing Page
        if (isHit("BACK_TO_LANDING", mx, my)) {
            if (listener != null) listener.returnToLanding();
            return true;
        }

        if (isHit("GO_TO_EXPLORE", mx, my)) {
            activeNav = NavItem.DISCOVER_PROJECTS;
            return true;
        }

        return false;
    }

    private void handleDropdownItemClick(String key) {
        Object payload = hitboxPayloads.get(key);
        String val = payload != null ? payload.toString() : "";

        if (key.startsWith("SELECT_CAT_DROP_")) {
            filterCategory = val;
            applyFilters();
        } else if (key.startsWith("SELECT_SKILL_DROP_")) {
            filterSkill = val;
            applyFilters();
        } else if (key.startsWith("SELECT_EXP_DROP_")) {
            filterExperience = val;
            applyFilters();
        } else if (key.startsWith("SELECT_REC_DROP_")) {
            filterRecruiting = val;
            applyFilters();
        } else if (key.startsWith("SELECT_SORT_DROP_")) {
            selectedSort = val;
            applyFilters();
        } else if (key.startsWith("TM_SELECT_SKILL_")) {
            filterTmSkill = val;
            applyTeammateFilters();
        } else if (key.startsWith("TM_SELECT_EXP_")) {
            filterTmExperience = val;
            applyTeammateFilters();
        } else if (key.startsWith("TM_SELECT_AVAIL_")) {
            filterTmAvailability = val;
            applyTeammateFilters();
        } else if (key.startsWith("TM_SELECT_RATING_")) {
            filterTmRating = val;
            applyTeammateFilters();
        } else if (key.startsWith("TM_SELECT_SORT_")) {
            selectedTmSort = val;
            applyTeammateFilters();
        } else if (key.equals("NOTIF_MARK_READ")) {
            notifications.forEach(n -> n.unread = false);
            showToast("All notifications marked as read.");
        } else if (key.startsWith("PROF_ITEM_")) {
            showToast("Option: " + val);
            if ("Log Out".equals(val) && listener != null) {
                listener.returnToLanding();
            }
        }
    }

    private boolean isHit(String key, int mx, int my) {
        Rectangle r = hitboxes.get(key);
        return r != null && r.contains(mx, my);
    }

    public boolean onMouseMove(int mx, int my) {
        String oldHover = hoveredHitbox;
        hoveredHitbox = null;

        for (Map.Entry<String, Rectangle> entry : hitboxes.entrySet()) {
            if (entry.getValue().contains(mx, my)) {
                hoveredHitbox = entry.getKey();
                break;
            }
        }

        // Sidebar hover
        NavItem oldNavHov = hoveredNav;
        hoveredNav = null;
        for (NavItem it : NavItem.values()) {
            if (("NAV_" + it.name()).equals(hoveredHitbox)) {
                hoveredNav = it;
                break;
            }
        }

        return hoveredHitbox != null || (oldHover != null);
    }

    public void onMouseWheel(int wheelRotation, int screenH) {
        int maxScroll = Math.max(0, totalContentHeight - (screenH - 60));
        targetScrollY = Math.max(0, Math.min(targetScrollY + wheelRotation * 55, maxScroll));
    }

    public boolean onKey(KeyEvent e) {
        int code = e.getKeyCode();

        // Modals close on Escape
        if (code == KeyEvent.VK_ESCAPE) {
            if (teammateModalUser != null) { teammateModalUser = null; return true; }
            if (detailModalProject != null) { detailModalProject = null; return true; }
            if (showCreateModal) { showCreateModal = false; return true; }
            if (activeDropdown != DropdownType.NONE) { activeDropdown = DropdownType.NONE; return true; }
            if (topSearchFocused) { topSearchFocused = false; return true; }
            if (contentSearchFocused) { contentSearchFocused = false; return true; }
            if (teammateSearchFocused) { teammateSearchFocused = false; return true; }
        }

        // Typing in Create Project Modal
        if (showCreateModal) {
            if (code == KeyEvent.VK_TAB) {
                createModalActiveField = (createModalActiveField + 1) % 4;
                return true;
            }
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (createModalActiveField == 0 && newProjTitle.length() > 0) newProjTitle = newProjTitle.substring(0, newProjTitle.length() - 1);
                if (createModalActiveField == 1 && newProjSkills.length() > 0) newProjSkills = newProjSkills.substring(0, newProjSkills.length() - 1);
                if (createModalActiveField == 2 && newProjRoles.length() > 0) newProjRoles = newProjRoles.substring(0, newProjRoles.length() - 1);
                if (createModalActiveField == 3 && newProjDesc.length() > 0) newProjDesc = newProjDesc.substring(0, newProjDesc.length() - 1);
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126) {
                if (createModalActiveField == 0 && newProjTitle.length() < 40) newProjTitle += c;
                if (createModalActiveField == 1 && newProjSkills.length() < 50) newProjSkills += c;
                if (createModalActiveField == 2 && newProjRoles.length() < 50) newProjRoles += c;
                if (createModalActiveField == 3 && newProjDesc.length() < 120) newProjDesc += c;
                return true;
            }
            return true;
        }

        // Typing in Search boxes
        if (teammateSearchFocused) {
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (teammateSearchText.length() > 0) {
                    teammateSearchText = teammateSearchText.substring(0, teammateSearchText.length() - 1);
                    applyTeammateFilters();
                }
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && teammateSearchText.length() < 50) {
                teammateSearchText += c;
                applyTeammateFilters();
                return true;
            }
            return true;
        }

        if (contentSearchFocused) {
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (contentSearchText.length() > 0) {
                    contentSearchText = contentSearchText.substring(0, contentSearchText.length() - 1);
                    applyFilters();
                }
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && contentSearchText.length() < 40) {
                contentSearchText += c;
                applyFilters();
                return true;
            }
            return true;
        }

        if (topSearchFocused) {
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (topSearchText.length() > 0) {
                    topSearchText = topSearchText.substring(0, topSearchText.length() - 1);
                    applyFilters();
                    applyTeammateFilters();
                }
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && topSearchText.length() < 40) {
                topSearchText += c;
                applyFilters();
                applyTeammateFilters();
                return true;
            }
            return true;
        }

        return false;
    }
}
