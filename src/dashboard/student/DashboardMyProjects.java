import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Handles the "My Projects" page for TeamUp Student Dashboard.
 * Displays user's active, lead, and joined collaborative projects.
 * All project and invitation data loaded live from MongoDB Atlas backend.
 * Features:
 *  - Real database projects (Created by me, Joined, Archived)
 *  - Received Invitations tab with live Accept / Decline actions
 *  - Manage Team & Applications modal:
 *      * Review applicants with "View Profile" details, Accept, and Decline buttons
 *      * Manage team members with individual Task Progress %, Delete, Report, and Review buttons
 *  - Task sprint board with real-time toggle and progress recalculation
 *  - Modern, polished card design without clutter.
 */
public class DashboardMyProjects {

    public static class MemberAvatar {
        public String initials;
        public String name;
        public String role;
        public Color bg;
        public Color text;
        public int progress = 0; // task progress percentage
        public String id = "";   // userId

        // Hitboxes in Manage Team modal
        public int delBtnX, delBtnY, delBtnW = 68, delBtnH = 26;
        public int repBtnX, repBtnY, repBtnW = 68, repBtnH = 26;
        public int revBtnX, revBtnY, revBtnW = 68, revBtnH = 26;

        public MemberAvatar(String initials, String name, String role, Color bg, Color text, int progress, String id) {
            this.initials = initials;
            this.name = name;
            this.role = role;
            this.bg = bg;
            this.text = text;
            this.progress = progress;
            this.id = id;
        }

        public MemberAvatar(String initials, String name, String role, Color bg, Color text) {
            this(initials, name, role, bg, text, 0, "");
        }
    }

    public static class ProjectTask {
        public volatile String remoteId;
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
        public String remoteId; // JoinRequest id
        public String userId = "";
        public String headline = "";
        public String university = "";
        public String department = "";
        public String bio = "";
        public String portfolio = "";
        public String github = "";
        public List<String> skills = new ArrayList<>();
        public String message = "";
        public boolean accepted = false;
        public boolean declined = false;

        public int viewProfX, viewProfY, viewProfW = 86, viewProfH = 28;
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

    public static class ProjectInvitation {
        public String id;
        public String projectId;
        public String projectTitle;
        public String category;
        public String projectDesc;
        public String fromId;
        public String fromName;
        public String fromHeadline;
        public String fromUniversity;
        public Color fromBg;
        public Color fromFg;
        public String date;
        public boolean accepted = false;
        public boolean declined = false;

        public int hitX, hitY, hitW, hitH;
        public int acceptBtnX, acceptBtnY, acceptBtnW = 110, acceptBtnH = 32;
        public int declineBtnX, declineBtnY, declineBtnW = 85, declineBtnH = 32;

        public ProjectInvitation(String id, String projectId, String projectTitle, String category,
                                 String projectDesc, String fromId, String fromName, String fromHeadline,
                                 String fromUniversity, Color fromBg, Color fromFg, String date) {
            this.id = id;
            this.projectId = projectId;
            this.projectTitle = projectTitle;
            this.category = category;
            this.projectDesc = projectDesc;
            this.fromId = fromId;
            this.fromName = fromName;
            this.fromHeadline = fromHeadline;
            this.fromUniversity = fromUniversity;
            this.fromBg = fromBg;
            this.fromFg = fromFg;
            this.date = date;
        }
    }

    public static class MyProjectCard {
        public int id;
        public String remoteId; // MongoDB project id
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
            if (skillArr != null) Collections.addAll(this.skills, skillArr);
            this.nextMilestone = nextMilestone;
            this.totalCapacity = capacity;
        }
    }

    private final List<MyProjectCard> allProjects = new ArrayList<>();
    private final List<MyProjectCard> filteredProjects = new ArrayList<>();
    private final List<ProjectInvitation> invitations = new ArrayList<>();

    // State & Filters
    // 0 = All Projects, 1 = Created by me, 2 = Joined, 3 = Invitations, 4 = Archived
    private int selectedTab = 0;
    private String searchKeyword = "";
    private boolean searchFocused = false;
    private String filterStatus = "All statuses";
    private String filterRole = "All roles";
    private String selectedSort = "Recently updated";

    private int totalContentHeight = 1200;
    private int lastScreenWidth = 1200;
    private int lastScreenHeight = 800;

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
    public enum SubModal { NONE, MANAGE_TEAM, PROJECT_BOARD, VIEW_CANDIDATE_PROFILE }
    private SubModal activeSubModal = SubModal.NONE;
    private MyProjectCard modalProject = null;
    private Candidate selectedCandidate = null;

    private int subModalCloseX, subModalCloseY, subModalCloseSize = 28;
    private int newTaskInputX, newTaskInputY, newTaskInputW = 320, newTaskInputH = 36;
    private int addTaskBtnX, addTaskBtnY, addTaskBtnW = 95, addTaskBtnH = 36;
    private String newTaskTitle = "";
    private boolean newTaskFocused = false;

    private StudentDashboard dashboardRef = null;

    public DashboardMyProjects() {
        // Starts empty - all data loaded live from MongoDB Atlas database!
        applyFilters();
    }

    /** Replaces the project list with data loaded live from the backend. */
    public void setProjects(List<MyProjectCard> projects) {
        allProjects.clear();
        if (projects != null) {
            allProjects.addAll(projects);
        }
        applyFilters();
    }

    /** Sets the received project invitations from the backend. */
    public void setInvitations(List<ProjectInvitation> invs) {
        invitations.clear();
        if (invs != null) {
            invitations.addAll(invs);
        }
    }

    /** Returns all projects owned / led by the user for the teammate invitation picker. */
    public List<MyProjectCard> getLeadProjects() {
        List<MyProjectCard> leads = new ArrayList<>();
        for (MyProjectCard p : allProjects) {
            if (p.isLead && !p.isArchived) leads.add(p);
        }
        return leads;
    }

    public void applyFilters() {
        filteredProjects.clear();
        String query = searchKeyword.trim().toLowerCase();

        for (MyProjectCard p : allProjects) {
            // Tab filtering
            if (selectedTab == 1 && !p.isLead) continue;
            if (selectedTab == 2 && p.isLead) continue;
            if (selectedTab == 4 && !p.isArchived) continue;
            if (selectedTab != 4 && p.isArchived) continue; // Non-archived tabs hide archived

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

    /** All of the user's projects (read-only) — used by the header search. */
    public List<MyProjectCard> getAllProjects() {
        return Collections.unmodifiableList(allProjects);
    }

    /** Filters My Projects by a keyword (shows the "All projects" tab). */
    public void setSearchKeyword(String kw) {
        searchKeyword = kw == null ? "" : kw;
        selectedTab = 0;
        applyFilters();
    }

    /** True while a full-screen overlay (Manage team / task board / candidate profile) covers the page. */
    public boolean hasOverlay() {
        return activeSubModal != SubModal.NONE;
    }

    // =========================================================================
    // PAINTING METHODS
    // =========================================================================

    public void paint(Graphics2D g, int startX, int startY, int width, int height, StudentDashboard dashboard) {
        this.dashboardRef = dashboard;
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
        g.drawString("Manage your active builds, review applicants, and collaborate with your team.", startX + padX, curY + 46);

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

        // 2. Metrics Strip (4 stat summary cards calculated live)
        int statGap = 16;
        int statW = (contentW - statGap * 3) / 4;
        int statH = 88;

        int totalProjCount = allProjects.size();
        int leadProjCount = 0;
        int joinedProjCount = 0;
        int totalPendingTasks = 0;
        int totalPendingApplicants = 0;
        int totalFilledSlots = 0;
        int totalMaxSlots = 0;

        for (MyProjectCard p : allProjects) {
            if (p.isLead && !p.isArchived) leadProjCount++;
            if (!p.isLead && !p.isArchived) joinedProjCount++;
            for (ProjectTask t : p.tasks) {
                if (!t.done) totalPendingTasks++;
            }
            if (p.isLead) {
                totalPendingApplicants += p.candidates.size();
            }
            totalFilledSlots += p.members.size();
            totalMaxSlots += p.totalCapacity;
        }

        String capPct = totalMaxSlots > 0 ? (Math.round((totalFilledSlots * 100f) / totalMaxSlots) + "%") : "100%";

        paintStatCard(g, startX + padX, curY, statW, statH, "Total Projects",
                String.valueOf(totalProjCount), leadProjCount + " created · " + joinedProjCount + " joined", 0);
        paintStatCard(g, startX + padX + (statW + statGap), curY, statW, statH, "Active Tasks",
                String.valueOf(totalPendingTasks), "Pending completion", 1);
        paintStatCard(g, startX + padX + (statW + statGap) * 2, curY, statW, statH, "Pending Applicants",
                String.valueOf(totalPendingApplicants), "Awaiting your review", 2);
        paintStatCard(g, startX + padX + (statW + statGap) * 3, curY, statW, statH, "Invitations",
                String.valueOf(invitations.size()), invitations.size() > 0 ? "Pending invitations" : "No pending invites", 3);

        curY += statH + 24;

        // 3. Tab Navigation Bar
        tabs.clear();
        int allActiveCount = 0;
        int archivedCount = 0;
        for (MyProjectCard p : allProjects) {
            if (p.isArchived) archivedCount++;
            else allActiveCount++;
        }

        String[] tabNames = {"All projects", "Created by me", "Joined", "Invitations", "Archived"};
        int[] tabCounts = {allActiveCount, leadProjCount, joinedProjCount, invitations.size(), archivedCount};
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

        // Tab 3: INVITATIONS TAB VIEW
        if (selectedTab == 3) {
            curY = paintInvitationsTab(g, startX + padX, curY, contentW, dashboard);
            totalContentHeight = curY - startY + 60;
            return;
        }

        // 4. Search & Filters Bar (for project tabs)
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

        // Search Icon & text
        Draw.drawSearchIcon(g, searchX + 12, searchY + 11, 14, DashboardTheme.TEXT_LIGHT);
        g.setFont(DashboardTheme.FONT_REG_13);
        if (searchKeyword.isEmpty() && !searchFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Search projects by title, skill, or role...", searchX + 36, searchY + 24);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(searchKeyword + (searchFocused ? "|" : ""), searchX + 36, searchY + 24);
        }

        // Dropdowns on the right
        int dropX = searchX + searchW + 12;

        // Status Dropdown
        statusDropX = dropX; statusDropY = curY;
        paintDropdown(g, statusDropX, statusDropY, statusDropW, statusDropH, filterStatus, activeDropdown == DropdownType.STATUS);
        dropX += statusDropW + 10;

        // Role Dropdown
        roleDropX = dropX; roleDropY = curY;
        paintDropdown(g, roleDropX, roleDropY, roleDropW, roleDropH, filterRole, activeDropdown == DropdownType.ROLE);
        dropX += roleDropW + 10;

        // Sort Dropdown
        sortDropX = dropX; sortDropY = curY;
        paintDropdown(g, sortDropX, sortDropY, sortDropW, sortDropH, selectedSort, activeDropdown == DropdownType.SORT);

        curY += searchH + 20;

        // 5. Projects Cards List
        if (filteredProjects.isEmpty()) {
            curY = paintEmptyState(g, startX + padX, curY, contentW);
        } else {
            for (MyProjectCard card : filteredProjects) {
                int cardH = paintProjectCard(g, startX + padX, curY, contentW, card);
                curY += cardH + 16;
            }
        }

        totalContentHeight = curY - startY + 60;
    }

    private int paintInvitationsTab(Graphics2D g, int cx, int cy, int cw, StudentDashboard dashboard) {
        int curY = cy;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Project Invitations (" + invitations.size() + ")", cx, curY + 20);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Invitations from team leads to join their collaborative projects. Accept to become a project member.", cx, curY + 38);
        curY += 54;

        if (invitations.isEmpty()) {
            RoundRectangle2D emptyBox = new RoundRectangle2D.Float(cx, curY, cw, 180, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(emptyBox);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(emptyBox);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString("No pending invitations", cx + 30, curY + 70);

            g.setFont(DashboardTheme.FONT_REG_13);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString("When other project leads invite you to their team, the invitation will appear here.", cx + 30, curY + 98);
            return curY + 200;
        }

        for (ProjectInvitation inv : invitations) {
            int cardH = 130;
            inv.hitX = cx; inv.hitY = curY; inv.hitW = cw; inv.hitH = cardH;

            RoundRectangle2D iShape = new RoundRectangle2D.Float(cx, curY, cw, cardH, 12, 12);
            g.setColor(Color.WHITE);
            g.fill(iShape);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(iShape);

            // Project Title + Category
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(inv.projectTitle, cx + 24, curY + 30);
            int tw = g.getFontMetrics().stringWidth(inv.projectTitle);

            RoundRectangle2D catBadge = new RoundRectangle2D.Float(cx + 24 + tw + 12, curY + 14, 88, 22, 6, 6);
            g.setColor(DashboardTheme.PURPLE_TINT_BG);
            g.fill(catBadge);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawString(inv.category, cx + 24 + tw + 20, curY + 29);

            // Invited by
            int avSize = 28;
            g.setColor(inv.fromBg != null ? inv.fromBg : DashboardTheme.PURPLE_TINT_BG);
            g.fillOval(cx + 24, curY + 44, avSize, avSize);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.setColor(inv.fromFg != null ? inv.fromFg : DashboardTheme.BRAND_PURPLE);
            String ini = inv.fromName != null && inv.fromName.length() >= 2 ? inv.fromName.substring(0, 2).toUpperCase() : "TU";
            g.drawString(ini, cx + 31, curY + 62);

            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString("Invited by " + inv.fromName + (inv.fromHeadline != null && !inv.fromHeadline.isEmpty() ? " · " + inv.fromHeadline : ""), cx + 60, curY + 56);

            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Received " + inv.date, cx + 60, curY + 70);

            // Description
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_MUTED);
            String desc = inv.projectDesc;
            if (desc != null && desc.length() > 100) desc = desc.substring(0, 97) + "...";
            g.drawString(desc != null ? desc : "", cx + 24, curY + 98);

            // Action Buttons on Right
            int btnY = curY + 45;
            if (inv.accepted) {
                g.setFont(DashboardTheme.FONT_BOLD_13);
                g.setColor(new Color(0x15, 0x80, 0x3D));
                g.drawString("✓ Accepted — You are a member!", cx + cw - 260, btnY + 22);
            } else if (inv.declined) {
                g.setFont(DashboardTheme.FONT_BOLD_13);
                g.setColor(DashboardTheme.TEXT_LIGHT);
                g.drawString("Invitation Declined", cx + cw - 180, btnY + 22);
            } else {
                // Accept Button
                inv.acceptBtnX = cx + cw - 24 - inv.acceptBtnW - inv.declineBtnW - 10;
                inv.acceptBtnY = btnY;
                RoundRectangle2D accB = new RoundRectangle2D.Float(inv.acceptBtnX, inv.acceptBtnY, inv.acceptBtnW, inv.acceptBtnH, 8, 8);
                g.setColor(new Color(0xDC, 0xFC, 0xE7));
                g.fill(accB);
                g.setFont(DashboardTheme.FONT_BOLD_12);
                g.setColor(new Color(0x15, 0x80, 0x3D));
                g.drawString("✓ Accept Invite", inv.acceptBtnX + 12, inv.acceptBtnY + 21);

                // Decline Button
                inv.declineBtnX = cx + cw - 24 - inv.declineBtnW;
                inv.declineBtnY = btnY;
                RoundRectangle2D decB = new RoundRectangle2D.Float(inv.declineBtnX, inv.declineBtnY, inv.declineBtnW, inv.declineBtnH, 8, 8);
                g.setColor(Color.WHITE);
                g.fill(decB);
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(decB);
                g.setFont(DashboardTheme.FONT_BOLD_12);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString("✕ Decline", inv.declineBtnX + 14, inv.declineBtnY + 21);
            }

            curY += cardH + 14;
        }

        return curY;
    }

    private int paintEmptyState(Graphics2D g, int cx, int cy, int cw) {
        int h = 200;
        RoundRectangle2D bg = new RoundRectangle2D.Float(cx, cy, cw, h, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(bg);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(bg);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("No projects found", cx + 36, cy + 70);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Create a project with '+ New Project' or discover peer projects in Discover Projects to collaborate!", cx + 36, cy + 98);

        return cy + h + 16;
    }

    private int paintProjectCard(Graphics2D g, int cx, int cy, int cw, MyProjectCard p) {
        int pad = 22;
        int innerW = cw - pad * 2;

        // --- measure: description lines decide the card height
        FontMetrics dfm = g.getFontMetrics(DashboardTheme.FONT_REG_13);
        List<String> descLines = wrapLimited(p.description, dfm, innerW, 2);
        int cardH = 22 + 44 + 14 + Math.max(1, descLines.size()) * 20 + 16 + 30 + 14 + 24 + 20 + 1 + 16 + 34 + 18;
        p.hitX = cx; p.hitY = cy; p.hitW = cw; p.hitH = cardH;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, cardH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        int x = cx + pad;
        int y = cy + 22;

        // --- header: monogram, title, role/category meta, status badge
        Color accent = p.statusText != null ? p.statusText : DashboardTheme.BRAND_PURPLE;
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fill(new RoundRectangle2D.Float(x, y, 44, 44, 12, 12));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.BRAND_PURPLE);
        String mono = (p.title == null || p.title.trim().isEmpty()) ? "P" : p.title.trim().substring(0, 1).toUpperCase();
        FontMetrics mfm = g.getFontMetrics();
        g.drawString(mono, x + (44 - mfm.stringWidth(mono)) / 2, y + 29);

        String status = p.status == null ? "" : p.status;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics bfm = g.getFontMetrics();
        int badgeW = bfm.stringWidth(status) + 26;
        int badgeX = cx + cw - pad - badgeW;
        RoundRectangle2D stBadge = new RoundRectangle2D.Float(badgeX, y + 2, badgeW, 24, 24, 24);
        g.setColor(p.statusBg != null ? p.statusBg : new Color(0xDC, 0xFC, 0xE7));
        g.fill(stBadge);
        g.setColor(accent);
        g.fillOval(badgeX + 10, y + 11, 6, 6);
        g.drawString(status, badgeX + 20, y + 18);

        int textX = x + 58;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(fitText(p.title, g.getFontMetrics(), badgeX - 14 - textX), textX, y + 19);

        // role pill + category + my role
        int metaY = y + 26;
        String rolePill = p.isLead ? "PROJECT LEAD" : "MEMBER";
        g.setFont(DashboardTheme.FONT_BOLD_10);
        int rpW = g.getFontMetrics().stringWidth(rolePill) + 14;
        g.setColor(p.isLead ? DashboardTheme.PURPLE_TINT_BG : new Color(0xF4, 0xF2, 0xEE));
        g.fill(new RoundRectangle2D.Float(textX, metaY, rpW, 18, 6, 6));
        g.setColor(p.isLead ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
        g.drawString(rolePill, textX + 7, metaY + 13);
        List<String> meta = new ArrayList<>();
        if (p.category != null && !p.category.trim().isEmpty()) meta.add(p.category.trim());
        if (p.myRole != null && !p.myRole.trim().isEmpty()) meta.add("You: " + p.myRole.trim());
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(fitText(String.join("  ·  ", meta), g.getFontMetrics(), cx + cw - pad - (textX + rpW + 10)), textX + rpW + 10, metaY + 13);

        // --- description (max 2 lines, ellipsized)
        y += 44 + 14;
        g.setFont(DashboardTheme.FONT_REG_13);
        if (descLines.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("No description yet.", x, y + 14);
            y += 20;
        } else {
            g.setColor(DashboardTheme.TEXT_MUTED);
            for (String line : descLines) {
                g.drawString(line, x, y + 14);
                y += 20;
            }
        }

        // --- progress (left) + next milestone (right)
        y += 16;
        int progW = Math.min(300, Math.max(140, innerW / 2 - 40));
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("PROGRESS", x, y + 10);
        String pct = p.progressPct + "%";
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(pct, x + progW - g.getFontMetrics().stringWidth(pct), y + 10);
        g.setColor(new Color(0xEE, 0xEC, 0xE7));
        g.fill(new RoundRectangle2D.Float(x, y + 18, progW, 8, 8, 8));
        int fillW = Math.round(progW * Math.max(0, Math.min(100, p.progressPct)) / 100f);
        if (fillW > 0) {
            g.setColor(p.progressPct >= 100 ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.BRAND_PURPLE);
            g.fill(new RoundRectangle2D.Float(x, y + 18, Math.max(8, fillW), 8, 8, 8));
        }

        int msX = x + progW + 32;
        int msW = cx + cw - pad - msX;
        if (msW > 120 && p.nextMilestone != null && !p.nextMilestone.trim().isEmpty()) {
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString("NEXT MILESTONE", msX, y + 10);
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(fitText(p.nextMilestone.trim(), g.getFontMetrics(), msW), msX, y + 27);
        }
        y += 30 + 14;

        // --- skills: one row, "+N" when they don't fit
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics sfm = g.getFontMetrics();
        int skX = x;
        int limit = cx + cw - pad;
        for (int i = 0; i < p.skills.size(); i++) {
            String s = p.skills.get(i);
            int sw = sfm.stringWidth(s) + 16;
            int remaining = p.skills.size() - i - 1;
            int moreW = remaining > 0 ? sfm.stringWidth("+" + remaining) + 16 + 6 : 0;
            if (skX + sw + moreW > limit) {
                String more = "+" + (p.skills.size() - i);
                int mw = sfm.stringWidth(more) + 16;
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Float(skX, y, mw, 24, 8, 8));
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(new RoundRectangle2D.Float(skX, y, mw, 24, 8, 8));
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString(more, skX + 8, y + 16);
                break;
            }
            RoundRectangle2D sk = new RoundRectangle2D.Float(skX, y, sw, 24, 8, 8);
            g.setColor(new Color(0xF8, 0xF7, 0xF4));
            g.fill(sk);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(sk);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 8, y + 16);
            skX += sw + 6;
        }
        y += 24 + 20;

        // --- divider + footer (members on the left, actions on the right)
        g.setColor(new Color(0xEE, 0xEB, 0xE5));
        g.drawLine(cx + pad, y, cx + cw - pad, y);
        y += 1 + 16;

        int btnH = 34;
        int btnRowX = cx + cw - pad;
        g.setFont(DashboardTheme.FONT_BOLD_12);
        FontMetrics btnFm = g.getFontMetrics();
        if (p.isLead && !p.isArchived) {
            String mLabel = "Manage team" + (p.candidates.size() > 0 ? "  " + p.candidates.size() : "");
            p.manageBtnW = btnFm.stringWidth(mLabel) + 32;
            p.manageBtnH = btnH;
            btnRowX -= p.manageBtnW;
            p.manageBtnX = btnRowX;
            p.manageBtnY = y;
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.fill(new RoundRectangle2D.Float(p.manageBtnX, p.manageBtnY, p.manageBtnW, btnH, 10, 10));
            g.setColor(Color.WHITE);
            g.drawString(mLabel, p.manageBtnX + 16, p.manageBtnY + 22);
            btnRowX -= 10;
        } else {
            p.manageBtnW = 0;
            p.manageBtnH = 0;
        }

        String tLabel = "Tasks  " + p.tasks.size();
        p.boardBtnW = btnFm.stringWidth(tLabel) + 46;
        p.boardBtnH = btnH;
        btnRowX -= p.boardBtnW;
        p.boardBtnX = btnRowX;
        p.boardBtnY = y;
        RoundRectangle2D bBtn = new RoundRectangle2D.Float(p.boardBtnX, p.boardBtnY, p.boardBtnW, btnH, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(bBtn);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(bBtn);
        // checklist glyph
        int gx = p.boardBtnX + 14;
        int gy = p.boardBtnY + 11;
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect(gx, gy, 12, 12, 3, 3);
        g.drawLine(gx + 3, gy + 6, gx + 5, gy + 8);
        g.drawLine(gx + 5, gy + 8, gx + 9, gy + 4);
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(tLabel, p.boardBtnX + 34, p.boardBtnY + 22);

        // member avatars (max 5) + "n / cap members"
        int avSize = 30;
        int avX = x;
        int shown = Math.min(5, p.members.size());
        for (int i = 0; i < shown; i++) {
            MemberAvatar m = p.members.get(i);
            g.setColor(Color.WHITE);
            g.fillOval(avX - 2, y + 0, avSize + 4, avSize + 4);
            g.setColor(m.bg != null ? m.bg : DashboardTheme.PURPLE_TINT_BG);
            g.fillOval(avX, y + 2, avSize, avSize);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(m.text != null ? m.text : DashboardTheme.BRAND_PURPLE);
            String ini = m.initials == null ? "" : m.initials;
            FontMetrics afm = g.getFontMetrics();
            g.drawString(ini, avX + (avSize - afm.stringWidth(ini)) / 2, y + 21);
            avX += avSize - 4;
        }
        if (p.members.size() > shown) {
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fillOval(avX, y + 2, avSize, avSize);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(DashboardTheme.TEXT_MUTED);
            String more = "+" + (p.members.size() - shown);
            g.drawString(more, avX + (avSize - g.getFontMetrics().stringWidth(more)) / 2, y + 21);
            avX += avSize - 4;
        }
        String cap = p.members.size() + (p.totalCapacity > 0 ? " / " + p.totalCapacity : "") + " members";
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        int capX = avX + (shown > 0 ? 18 : 0);
        if (capX + g.getFontMetrics().stringWidth(cap) < btnRowX - 12) {
            g.drawString(cap, capX, y + 22);
        }
        return cardH;
    }

    private void paintDropdown(Graphics2D g, int x, int y, int w, int h, String text, boolean open) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(open ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(open ? 1.4f : 1.0f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(text, x + 14, y + 24);

        Draw.drawChevronDown(g, x + w - 20, y + 15, 8, DashboardTheme.TEXT_MUTED, 0f);
    }

    private void paintStatCard(Graphics2D g, int x, int y, int w, int h, String title, String val, String sub, int type) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(shape);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(title, x + 18, y + 26);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(val, x + 18, y + 54);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(sub, x + 18, y + 74);
    }

    // =========================================================================
    // OVERLAYS & MODALS
    // =========================================================================

    public void paintOverlays(Graphics2D g, int screenW, int screenH) {
        lastScreenWidth = screenW;
        lastScreenHeight = screenH;
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
        } else if (activeSubModal == SubModal.VIEW_CANDIDATE_PROFILE && selectedCandidate != null) {
            paintCandidateProfileModal(g, screenW, screenH);
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

        int mw = 680;
        int mh = 560;
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
        g.drawString("Review join requests, manage active members, task progress, report, and leave peer reviews.", mx + 26, my + 58);

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
                RoundRectangle2D cRow = new RoundRectangle2D.Float(mx + 26, curY, mw - 52, 48, 8, 8);
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fill(cRow);
                g.setColor(DashboardTheme.BORDER_COLOR);
                g.draw(cRow);

                // Avatar
                g.setColor(c.bg != null ? c.bg : DashboardTheme.PURPLE_TINT_BG);
                g.fillOval(mx + 36, curY + 9, 30, 30);
                g.setFont(DashboardTheme.FONT_BOLD_11);
                g.setColor(c.text != null ? c.text : DashboardTheme.BRAND_PURPLE);
                g.drawString(c.initials, mx + 42, curY + 28);

                // Name & Role
                g.setFont(DashboardTheme.FONT_BOLD_12);
                g.setColor(DashboardTheme.TEXT_DARK);
                g.drawString(c.name, mx + 76, curY + 20);

                g.setFont(DashboardTheme.FONT_REG_11);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString(c.role + " · " + c.matchPct + " skill match", mx + 76, curY + 36);

                // Buttons: [View Profile]  [Accept]  [Decline]
                if (c.accepted) {
                    g.setFont(DashboardTheme.FONT_BOLD_12);
                    g.setColor(new Color(0x15, 0x80, 0x3D));
                    g.drawString("✓ Accepted", mx + mw - 140, curY + 29);
                } else if (c.declined) {
                    g.setFont(DashboardTheme.FONT_BOLD_12);
                    g.setColor(DashboardTheme.TEXT_LIGHT);
                    g.drawString("Declined", mx + mw - 120, curY + 29);
                } else {
                    int btnRowRight = mx + mw - 36;

                    // Decline Button
                    c.declineX = btnRowRight - c.declineW;
                    c.declineY = curY + 10;
                    RoundRectangle2D decBtn = new RoundRectangle2D.Float(c.declineX, c.declineY, c.declineW, c.declineH, 6, 6);
                    g.setColor(Color.WHITE);
                    g.fill(decBtn);
                    g.setColor(DashboardTheme.BORDER_COLOR);
                    g.draw(decBtn);
                    g.setFont(DashboardTheme.FONT_BOLD_11);
                    g.setColor(DashboardTheme.TEXT_MUTED);
                    g.drawString("Decline", c.declineX + 16, c.declineY + 18);

                    // Accept Button
                    c.acceptX = c.declineX - c.acceptW - 8;
                    c.acceptY = curY + 10;
                    RoundRectangle2D accBtn = new RoundRectangle2D.Float(c.acceptX, c.acceptY, c.acceptW, c.acceptH, 6, 6);
                    g.setColor(new Color(0xDC, 0xFC, 0xE7));
                    g.fill(accBtn);
                    g.setFont(DashboardTheme.FONT_BOLD_11);
                    g.setColor(new Color(0x15, 0x80, 0x3D));
                    g.drawString("Accept", c.acceptX + 18, c.acceptY + 18);

                    // View Profile Button
                    c.viewProfX = c.acceptX - c.viewProfW - 8;
                    c.viewProfY = curY + 10;
                    RoundRectangle2D vpBtn = new RoundRectangle2D.Float(c.viewProfX, c.viewProfY, c.viewProfW, c.viewProfH, 6, 6);
                    g.setColor(Color.WHITE);
                    g.fill(vpBtn);
                    g.setColor(DashboardTheme.BORDER_COLOR);
                    g.draw(vpBtn);
                    g.setFont(DashboardTheme.FONT_BOLD_11);
                    g.setColor(DashboardTheme.TEXT_DARK);
                    g.drawString("View Profile", c.viewProfX + 10, c.viewProfY + 18);
                }

                curY += 56;
            }
        }

        // Section 2: Active Members with Progress %, Delete, Report, Review
        curY += 12;
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("CURRENT TEAM MEMBERS (" + modalProject.members.size() + "/" + modalProject.totalCapacity + ")", mx + 26, curY);

        curY += 12;
        for (int i = 0; i < modalProject.members.size(); i++) {
            MemberAvatar m = modalProject.members.get(i);
            RoundRectangle2D mRow = new RoundRectangle2D.Float(mx + 26, curY, mw - 52, 54, 8, 8);
            g.setColor(Color.WHITE);
            g.fill(mRow);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(mRow);

            g.setColor(m.bg != null ? m.bg : DashboardTheme.PURPLE_TINT_BG);
            g.fillOval(mx + 36, curY + 11, 32, 32);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.setColor(m.text != null ? m.text : DashboardTheme.BRAND_PURPLE);
            g.drawString(m.initials, mx + 43, curY + 31);

            // Name & Role
            boolean self = isSelf(m);
            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(m.name, mx + 78, curY + 20);
            if (self) {
                int nw = g.getFontMetrics().stringWidth(m.name);
                g.setFont(DashboardTheme.FONT_REG_11);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString("(you)", mx + 78 + nw + 6, curY + 20);
            }

            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(m.role, mx + 78, curY + 34);

            // Progress bar under name
            int pTrackW = 110;
            int pTrackH = 5;
            int pTrackX = mx + 78;
            int pTrackY = curY + 41;
            RoundRectangle2D mTrack = new RoundRectangle2D.Float(pTrackX, pTrackY, pTrackW, pTrackH, 3, 3);
            g.setColor(new Color(0xEE, 0xF2, 0xF6));
            g.fill(mTrack);

            int mFillW = Math.round((pTrackW * Math.min(100, Math.max(0, m.progress))) / 100f);
            RoundRectangle2D mFill = new RoundRectangle2D.Float(pTrackX, pTrackY, mFillW, pTrackH, 3, 3);
            g.setColor(m.progress >= 100 ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.BRAND_PURPLE);
            g.fill(mFill);

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString("Progress: " + m.progress + "%", pTrackX + pTrackW + 8, pTrackY + 6);

            // Action Buttons: [★ Review]  [⚑ Report]  [✕ Remove] — never on your own row
            int btnRight = mx + mw - 36;
            int btnY = curY + 14;
            if (self) {
                m.revBtnX = m.repBtnX = m.delBtnX = -9999; // no stale hitboxes
                String you = "You";
                g.setFont(DashboardTheme.FONT_BOLD_11);
                int yw = g.getFontMetrics().stringWidth(you) + 24;
                RoundRectangle2D pill = new RoundRectangle2D.Float(btnRight - yw, btnY, yw, 26, 26, 26);
                g.setColor(DashboardTheme.PURPLE_TINT_BG);
                g.fill(pill);
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawString(you, btnRight - yw + 12, btnY + 17);
                curY += 62;
                continue;
            }

            // Remove Button (Not shown for Project Lead / Owner)
            boolean isLeadMember = (i == 0 || "Project Lead".equalsIgnoreCase(m.role));
            if (!isLeadMember) {
                m.delBtnX = btnRight - m.delBtnW;
                m.delBtnY = btnY;
                RoundRectangle2D delBtn = new RoundRectangle2D.Float(m.delBtnX, m.delBtnY, m.delBtnW, m.delBtnH, 6, 6);
                g.setColor(new Color(0xFE, 0xF2, 0xF2));
                g.fill(delBtn);
                g.setColor(new Color(0xEF, 0x44, 0x44));
                g.setFont(DashboardTheme.FONT_BOLD_11);
                g.drawString("✕ Remove", m.delBtnX + 8, m.delBtnY + 17);
                btnRight -= m.delBtnW + 8;
            }

            // Report Button
            m.repBtnX = btnRight - m.repBtnW;
            m.repBtnY = btnY;
            RoundRectangle2D repBtn = new RoundRectangle2D.Float(m.repBtnX, m.repBtnY, m.repBtnW, m.repBtnH, 6, 6);
            g.setColor(Color.WHITE);
            g.fill(repBtn);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(repBtn);
            g.setColor(new Color(0xD9, 0x77, 0x06));
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.drawString("⚑ Report", m.repBtnX + 11, m.repBtnY + 17);
            btnRight -= m.repBtnW + 8;

            // Review Button
            m.revBtnX = btnRight - m.revBtnW;
            m.revBtnY = btnY;
            RoundRectangle2D revBtn = new RoundRectangle2D.Float(m.revBtnX, m.revBtnY, m.revBtnW, m.revBtnH, 6, 6);
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(revBtn);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.drawString("★ Review", m.revBtnX + 10, m.revBtnY + 17);

            curY += 62;
        }
    }

    private void paintCandidateProfileModal(Graphics2D g, int sw, int sh) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, sw, sh);

        int mw = 520;
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

        // Candidate Avatar & Header
        int curY = my + 32;
        int avD = 54;
        g.setColor(selectedCandidate.bg != null ? selectedCandidate.bg : DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(mx + 28, curY, avD, avD);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(selectedCandidate.text != null ? selectedCandidate.text : DashboardTheme.BRAND_PURPLE);
        g.drawString(selectedCandidate.initials, mx + 42, curY + 34);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(selectedCandidate.name, mx + 94, curY + 24);

        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString(selectedCandidate.role + " · " + selectedCandidate.matchPct + " skill match", mx + 94, curY + 42);

        curY += 66;

        // Headline & University
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String sub = "";
        if (!selectedCandidate.headline.isEmpty()) sub += selectedCandidate.headline;
        if (!selectedCandidate.university.isEmpty()) sub += (sub.isEmpty() ? "" : " · ") + selectedCandidate.university;
        g.drawString(sub.isEmpty() ? "Student Collaborator" : sub, mx + 28, curY);

        curY += 20;

        // Bio Section
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("ABOUT & BIO", mx + 28, curY);

        curY += 12;
        RoundRectangle2D bioBox = new RoundRectangle2D.Float(mx + 28, curY, mw - 56, 75, 8, 8);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(bioBox);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(bioBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String bio = selectedCandidate.bio.isEmpty() ? "No personal bio provided yet." : selectedCandidate.bio;
        if (bio.length() > 140) bio = bio.substring(0, 137) + "...";
        g.drawString(bio, mx + 38, curY + 28);

        curY += 92;

        // Skills Pills
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("VERIFIED SKILLS", mx + 28, curY);

        curY += 12;
        int skX = mx + 28;
        List<String> sks = selectedCandidate.skills.isEmpty() ? Arrays.asList("Collaboration", "Teamwork", "Development") : selectedCandidate.skills;
        for (String s : sks) {
            int pillW = g.getFontMetrics(DashboardTheme.FONT_REG_11).stringWidth(s) + 16;
            RoundRectangle2D sk = new RoundRectangle2D.Float(skX, curY, pillW, 22, 6, 6);
            g.setColor(new Color(0xF4, 0xF2, 0xEE));
            g.fill(sk);
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.draw(sk);
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(s, skX + 8, curY + 15);
            skX += pillW + 6;
            if (skX > mx + mw - 100) break;
        }

        curY += 46;

        // External links
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        String links = "Portfolio: " + (selectedCandidate.portfolio.isEmpty() ? "None" : selectedCandidate.portfolio)
                + "   |   GitHub: " + (selectedCandidate.github.isEmpty() ? "None" : selectedCandidate.github);
        g.drawString(links, mx + 28, curY);

        curY += 34;

        // Bottom Button: Close and Accept
        RoundRectangle2D closeBtn = new RoundRectangle2D.Float(mx + 28, curY, 110, 34, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(closeBtn);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.draw(closeBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("← Back", mx + 58, curY + 22);

        RoundRectangle2D accBtn = new RoundRectangle2D.Float(mx + mw - 28 - 140, curY, 140, 34, 8, 8);
        g.setColor(new Color(0xDC, 0xFC, 0xE7));
        g.fill(accBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x15, 0x80, 0x3D));
        g.drawString("✓ Accept Member", mx + mw - 28 - 128, curY + 22);
    }

    // Task board scroll state: the task list scrolls inside its own viewport so the
    // "add task" row is always pinned to the bottom of the modal.
    private int boardScroll = 0;
    private int boardMaxScroll = 0;
    private boolean boardScrollToEnd = false;
    private int boardListX, boardListY, boardListW, boardListH;
    private boolean addTaskBtnHovered = false;

    /** Scrolls the task list when the board is open; returns true when the wheel was consumed. */
    public boolean onMouseWheel(int rotation) {
        if (activeSubModal != SubModal.PROJECT_BOARD || modalProject == null) return false;
        boardScroll = Math.max(0, Math.min(boardMaxScroll, boardScroll + rotation * 48));
        return true;
    }

    private void paintProjectBoardModal(Graphics2D g, int sw, int sh) {
        g.setColor(new Color(15, 23, 42, 150));
        g.fillRect(0, 0, sw, sh);

        int rowH = 44;
        int rowGap = 8;
        int headerH = 112;
        int footerH = 84;
        int n = modalProject.tasks.size();
        int contentH = n == 0 ? 120 : n * (rowH + rowGap) - rowGap;
        int maxListH = Math.max(rowH, sh - 64 - headerH - footerH);
        int listH = Math.min(contentH, maxListH);

        int mw = Math.min(600, sw - 48);
        int mh = headerH + listH + footerH;
        int mx = (sw - mw) / 2;
        int my = Math.max(24, (sh - mh) / 2);

        // shadow + surface
        for (int i = 0; i < 4; i++) {
            int off = (i + 1) * 3;
            g.setColor(new Color(15, 23, 42, Math.max(4, 18 - i * 4)));
            g.fill(new RoundRectangle2D.Float(mx - off / 2f, my + off, mw + off, mh + off / 2f, 22, 22));
        }
        RoundRectangle2D mShape = new RoundRectangle2D.Float(mx, my, mw, mh, 18, 18);
        g.setColor(Color.WHITE);
        g.fill(mShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(mShape);

        int padX = 26;
        int innerW = mw - padX * 2;

        // Close button (vector ✕ in a soft square)
        subModalCloseSize = 32;
        subModalCloseX = mx + mw - subModalCloseSize - 18;
        subModalCloseY = my + 18;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(subModalCloseX, subModalCloseY, subModalCloseSize, subModalCloseSize, 10, 10));
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(subModalCloseX, subModalCloseY, subModalCloseSize, subModalCloseSize, 10, 10));
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(subModalCloseX + 11, subModalCloseY + 11, subModalCloseX + 21, subModalCloseY + 21);
        g.drawLine(subModalCloseX + 21, subModalCloseY + 11, subModalCloseX + 11, subModalCloseY + 21);

        // Header: title, project name, progress summary
        int textMaxW = innerW - subModalCloseSize - 12;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Tasks & sprints", mx + padX, my + 40);
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(fitText(modalProject.title, g.getFontMetrics(), textMaxW), mx + padX, my + 59);

        int done = 0;
        for (ProjectTask t : modalProject.tasks) if (t.done) done++;
        String summary = n == 0 ? "No tasks yet" : done + " of " + n + " done";
        int barY = my + 80;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        int sumW = g.getFontMetrics().stringWidth(summary);
        int barW = innerW - sumW - 14;
        g.setColor(new Color(0xF1, 0xEF, 0xEA));
        g.fill(new RoundRectangle2D.Float(mx + padX, barY, barW, 6, 6, 6));
        if (n > 0 && done > 0) {
            g.setColor(done == n ? new Color(0x10, 0xB9, 0x81) : DashboardTheme.BRAND_PURPLE);
            g.fill(new RoundRectangle2D.Float(mx + padX, barY, Math.max(6, barW * done / n), 6, 6, 6));
        }
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(summary, mx + padX + barW + 14, barY + 7);

        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(mx, my + headerH - 12, mx + mw, my + headerH - 12);

        // Scrollable task list
        boardListX = mx + padX;
        boardListY = my + headerH;
        boardListW = innerW;
        boardListH = listH;
        boardMaxScroll = Math.max(0, contentH - listH);
        if (boardScrollToEnd) {
            boardScroll = boardMaxScroll;
            boardScrollToEnd = false;
        }
        boardScroll = Math.max(0, Math.min(boardMaxScroll, boardScroll));

        boolean scrollable = boardMaxScroll > 0;
        int rowW = innerW - (scrollable ? 12 : 0);
        Shape oldClip = g.getClip();
        g.clipRect(boardListX - 2, boardListY - 2, boardListW + 4, boardListH + 4);

        if (n == 0) {
            int ey = boardListY + 10;
            float[] dash = { 5f, 4f };
            g.setColor(DashboardTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, 0f));
            g.draw(new RoundRectangle2D.Float(boardListX, ey, innerW, 100, 12, 12));
            g.setStroke(new BasicStroke(1f));
            g.setFont(DashboardTheme.FONT_BOLD_13);
            g.setColor(DashboardTheme.TEXT_DARK);
            String t1 = "No tasks yet";
            g.drawString(t1, boardListX + (innerW - g.getFontMetrics().stringWidth(t1)) / 2, ey + 44);
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_MUTED);
            String t2 = "Add the first milestone for your team below.";
            g.drawString(t2, boardListX + (innerW - g.getFontMetrics().stringWidth(t2)) / 2, ey + 66);
        }

        int curY = boardListY - boardScroll;
        for (ProjectTask task : modalProject.tasks) {
            boolean visible = curY + rowH > boardListY && curY < boardListY + boardListH;
            task.hitX = boardListX;
            task.hitY = curY;
            task.hitW = visible ? rowW : 0;
            task.hitH = visible ? rowH : 0;
            if (visible) paintBoardTaskRow(g, task, boardListX, curY, rowW, rowH);
            curY += rowH + rowGap;
        }
        g.setClip(oldClip);

        // soft fade hints when more tasks are hidden above / below
        if (scrollable) {
            if (boardScroll > 0) {
                g.setPaint(new GradientPaint(0, boardListY, new Color(255, 255, 255, 230), 0, boardListY + 18, new Color(255, 255, 255, 0)));
                g.fillRect(boardListX, boardListY, rowW, 18);
            }
            if (boardScroll < boardMaxScroll) {
                int by = boardListY + boardListH;
                g.setPaint(new GradientPaint(0, by - 18, new Color(255, 255, 255, 0), 0, by, new Color(255, 255, 255, 230)));
                g.fillRect(boardListX, by - 18, rowW, 18);
            }
            // scrollbar
            int trackX = boardListX + innerW - 5;
            g.setColor(new Color(0xF1, 0xEF, 0xEA));
            g.fill(new RoundRectangle2D.Float(trackX, boardListY, 5, boardListH, 5, 5));
            int thumbH = Math.max(28, boardListH * boardListH / contentH);
            int thumbY = boardListY + Math.round((boardListH - thumbH) * (boardScroll / (float) boardMaxScroll));
            g.setColor(new Color(0xCB, 0xD5, 0xE1));
            g.fill(new RoundRectangle2D.Float(trackX, thumbY, 5, thumbH, 5, 5));
        }

        // Pinned footer: new task input + add button
        int footY = my + mh - footerH;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(mx, footY + 8, mx + mw, footY + 8);

        addTaskBtnW = 112;
        addTaskBtnH = 40;
        newTaskInputH = 40;
        newTaskInputX = mx + padX;
        newTaskInputY = footY + 26;
        newTaskInputW = innerW - addTaskBtnW - 10;
        RoundRectangle2D inputShape = new RoundRectangle2D.Float(newTaskInputX, newTaskInputY, newTaskInputW, newTaskInputH, 10, 10);
        g.setColor(newTaskFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(inputShape);
        g.setColor(newTaskFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(newTaskFocused ? 1.6f : 1.1f));
        g.draw(inputShape);

        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics ifm = g.getFontMetrics();
        int textX = newTaskInputX + 14;
        int textMax = newTaskInputW - 28;
        if (newTaskTitle.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString(fitText("Add a task and press Enter…", ifm, textMax), textX, newTaskInputY + 25);
            if (newTaskFocused) {
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(textX, newTaskInputY + 11, textX, newTaskInputY + 29);
            }
        } else {
            // keep the end of the text (where the caret is) visible
            String shown = newTaskTitle;
            while (shown.length() > 1 && ifm.stringWidth(shown) > textMax - 4) shown = shown.substring(1);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(shown, textX, newTaskInputY + 25);
            if (newTaskFocused) {
                int cx = textX + ifm.stringWidth(shown) + 2;
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(cx, newTaskInputY + 11, cx, newTaskInputY + 29);
            }
        }

        addTaskBtnX = newTaskInputX + newTaskInputW + 10;
        addTaskBtnY = newTaskInputY;
        boolean canAdd = !newTaskTitle.trim().isEmpty();
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addTaskBtnX, addTaskBtnY, addTaskBtnW, addTaskBtnH, 10, 10);
        g.setColor(canAdd ? (addTaskBtnHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE)
                : DashboardTheme.PURPLE_TINT_BG);
        g.fill(addBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(canAdd ? Color.WHITE : DashboardTheme.BRAND_PURPLE);
        String addLabel = "+  Add task";
        g.drawString(addLabel, addTaskBtnX + (addTaskBtnW - g.getFontMetrics().stringWidth(addLabel)) / 2, addTaskBtnY + 25);
    }

    private void paintBoardTaskRow(Graphics2D g, ProjectTask task, int x, int y, int w, int h) {
        RoundRectangle2D tRow = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g.setColor(task.done ? new Color(0xF0, 0xFD, 0xF4) : Color.WHITE);
        g.fill(tRow);
        g.setColor(task.done ? new Color(0xBB, 0xF7, 0xD0) : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.draw(tRow);

        int cbS = 18;
        int cbX = x + 14;
        int cbY = y + (h - cbS) / 2;
        RoundRectangle2D cb = new RoundRectangle2D.Float(cbX, cbY, cbS, cbS, 5, 5);
        g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : Color.WHITE);
        g.fill(cb);
        g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1.3f));
        g.draw(cb);
        if (task.done) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cbX + 4, cbY + 9, cbX + 8, cbY + 13);
            g.drawLine(cbX + 8, cbY + 13, cbX + 14, cbY + 5);
        }

        String due = task.dueDate == null ? "" : task.dueDate;
        g.setFont(DashboardTheme.FONT_REG_11);
        FontMetrics dfm = g.getFontMetrics();
        int dueW = due.isEmpty() ? 0 : dfm.stringWidth(due) + 16;
        if (!due.isEmpty()) {
            int px = x + w - 12 - dueW;
            g.setColor(task.done ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xF4, 0xF2, 0xEE));
            g.fill(new RoundRectangle2D.Float(px, y + (h - 22) / 2, dueW, 22, 22, 22));
            g.setColor(task.done ? new Color(0x15, 0x80, 0x3D) : DashboardTheme.TEXT_MUTED);
            g.drawString(due, px + 8, y + h / 2 + 4);
        }

        g.setFont(task.done ? DashboardTheme.FONT_REG_13 : DashboardTheme.FONT_BOLD_13);
        FontMetrics tfm = g.getFontMetrics();
        int titleX = cbX + cbS + 12;
        String title = fitText(task.title, tfm, x + w - 12 - dueW - 12 - titleX);
        g.setColor(task.done ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
        int ty = y + h / 2 + 5;
        g.drawString(title, titleX, ty);
        if (task.done) {
            g.setColor(new Color(0x9E, 0x9E, 0xAE, 160));
            g.drawLine(titleX, ty - 4, titleX + tfm.stringWidth(title), ty - 4);
        }
    }

    /** Ellipsizes text to fit maxW pixels. */
    private static String fitText(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    /** Word-wraps text into at most maxLines lines; the last line is ellipsized when text remains. */
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

    // =========================================================================
    // EVENT HANDLING
    // =========================================================================

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        // 1. Sub-Modal Clicks
        if (activeSubModal != SubModal.NONE) {
            // Close (X) button
            if (mx >= subModalCloseX - 6 && mx <= subModalCloseX + subModalCloseSize &&
                    my >= subModalCloseY - 6 && my <= subModalCloseY + subModalCloseSize) {
                if (activeSubModal == SubModal.VIEW_CANDIDATE_PROFILE) {
                    activeSubModal = SubModal.MANAGE_TEAM;
                } else {
                    activeSubModal = SubModal.NONE;
                    modalProject = null;
                }
                return true;
            }

            // Candidate Profile Modal Actions
            if (activeSubModal == SubModal.VIEW_CANDIDATE_PROFILE && selectedCandidate != null) {
                int mw = 520; int mh = 480;
                int smx = (lastScreenWidth - mw) / 2;
                int smy = (lastScreenHeight - mh) / 2;

                // Back button
                if (mx >= smx + 28 && mx <= smx + 138 && my >= smy + 400 && my <= smy + 440) {
                    activeSubModal = SubModal.MANAGE_TEAM;
                    return true;
                }
                // Accept Member button
                if (mx >= smx + mw - 28 - 140 && mx <= smx + mw - 28 && my >= smy + 400 && my <= smy + 440) {
                    selectedCandidate.accepted = true;
                    selectedCandidate.declined = false;
                    DashboardSync.decideRequest(dashboard, selectedCandidate.remoteId, true);
                    modalProject.members.add(new MemberAvatar(
                            selectedCandidate.initials, selectedCandidate.name, selectedCandidate.role,
                            selectedCandidate.bg, selectedCandidate.text, 0, selectedCandidate.userId));
                    dashboard.showToast("Accepted " + selectedCandidate.name + " into " + modalProject.title + "! 🎉");
                    activeSubModal = SubModal.MANAGE_TEAM;
                    return true;
                }
                return true; // Click inside modal swallowed
            }

            // Manage Team Actions: Review applicants & Manage members
            if (activeSubModal == SubModal.MANAGE_TEAM && modalProject != null) {
                // Applicants actions
                for (Candidate c : modalProject.candidates) {
                    if (!c.accepted && !c.declined) {
                        // View Profile
                        if (mx >= c.viewProfX && mx <= c.viewProfX + c.viewProfW && my >= c.viewProfY && my <= c.viewProfY + c.viewProfH) {
                            selectedCandidate = c;
                            activeSubModal = SubModal.VIEW_CANDIDATE_PROFILE;
                            return true;
                        }
                        // Accept
                        if (mx >= c.acceptX && mx <= c.acceptX + c.acceptW && my >= c.acceptY && my <= c.acceptY + c.acceptH) {
                            c.accepted = true;
                            c.declined = false;
                            DashboardSync.decideRequest(dashboard, c.remoteId, true);
                            modalProject.members.add(new MemberAvatar(c.initials, c.name, c.role, c.bg, c.text, 0, c.userId));
                            dashboard.showToast(c.name + " accepted into " + modalProject.title + "! 🎉");
                            return true;
                        }
                        // Decline
                        if (mx >= c.declineX && mx <= c.declineX + c.declineW && my >= c.declineY && my <= c.declineY + c.declineH) {
                            c.declined = true;
                            c.accepted = false;
                            DashboardSync.decideRequest(dashboard, c.remoteId, false);
                            dashboard.showToast("Declined " + c.name + "'s request.");
                            return true;
                        }
                    }
                }

                // Member actions: Delete, Report, Review
                for (int i = 0; i < modalProject.members.size(); i++) {
                    MemberAvatar m = modalProject.members.get(i);
                    boolean isLead = (i == 0 || "Project Lead".equalsIgnoreCase(m.role));
                    if (isSelf(m)) continue; // you can't review, report or remove yourself

                    // Review button (★ Review)
                    if (mx >= m.revBtnX && mx <= m.revBtnX + m.revBtnW && my >= m.revBtnY && my <= m.revBtnY + m.revBtnH) {
                        handleReviewClick(dashboard, m, modalProject);
                        return true;
                    }

                    // Report button (⚑ Report)
                    if (mx >= m.repBtnX && mx <= m.repBtnX + m.repBtnW && my >= m.repBtnY && my <= m.repBtnY + m.repBtnH) {
                        handleReportClick(dashboard, m);
                        return true;
                    }

                    // Delete button (✕ Remove) - not available for project lead
                    if (!isLead && mx >= m.delBtnX && mx <= m.delBtnX + m.delBtnW && my >= m.delBtnY && my <= m.delBtnY + m.delBtnH) {
                        int opt = JOptionPane.showConfirmDialog(
                                null,
                                "Are you sure you want to remove " + m.name + " from " + modalProject.title + "?",
                                "Remove Team Member",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (opt == JOptionPane.YES_OPTION) {
                            DashboardSync.removeMember(dashboard, modalProject.remoteId, m.id);
                            modalProject.members.remove(i);
                            dashboard.showToast("Removed " + m.name + " from team.");
                        }
                        return true;
                    }
                }
            }

            // Project Board Actions: Toggle task, Add task
            if (activeSubModal == SubModal.PROJECT_BOARD && modalProject != null) {
                // Task toggle
                boolean inList = my >= boardListY && my <= boardListY + boardListH;
                for (ProjectTask task : modalProject.tasks) {
                    if (inList && task.hitW > 0 && mx >= task.hitX && mx <= task.hitX + task.hitW && my >= task.hitY && my <= task.hitY + task.hitH) {
                        task.done = !task.done;
                        DashboardSync.setTaskDone(dashboard, task, task.done);
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
                        boardScrollToEnd = true;
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

        // 5. Invitations Tab actions (Accept / Decline)
        if (selectedTab == 3) {
            for (ProjectInvitation inv : invitations) {
                if (!inv.accepted && !inv.declined) {
                    // Accept Invite
                    if (mx >= inv.acceptBtnX && mx <= inv.acceptBtnX + inv.acceptBtnW &&
                            my >= inv.acceptBtnY && my <= inv.acceptBtnY + inv.acceptBtnH) {
                        inv.accepted = true;
                        DashboardSync.acceptInvitation(dashboard, inv.id);
                        dashboard.showToast("Accepted invitation to " + inv.projectTitle + "! 🎉");
                        return true;
                    }
                    // Decline Invite
                    if (mx >= inv.declineBtnX && mx <= inv.declineBtnX + inv.declineBtnW &&
                            my >= inv.declineBtnY && my <= inv.declineBtnY + inv.declineBtnH) {
                        inv.declined = true;
                        DashboardSync.declineInvitation(dashboard, inv.id);
                        dashboard.showToast("Declined invitation to " + inv.projectTitle);
                        return true;
                    }
                }
            }
            return false;
        }

        // 6. Search Box focus
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // 7. Dropdown Toggles
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

        // 8. Project Card Buttons
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
                boardScroll = 0;
                return true;
            }
        }

        return false;
    }

    /** True when this member row is the logged-in user (matched by id, or by name when offline). */
    private boolean isSelf(MemberAvatar m) {
        if (m == null || dashboardRef == null) return false;
        String myId = dashboardRef.getProfileUserId();
        if (m.id != null && !m.id.isEmpty() && myId != null && !myId.isEmpty()) return m.id.equals(myId);
        String myName = dashboardRef.getProfileName();
        return (m.id == null || m.id.isEmpty()) && myName != null && m.name != null
                && m.name.trim().equalsIgnoreCase(myName.trim());
    }

    private void handleReviewClick(StudentDashboard dashboard, MemberAvatar m, MyProjectCard proj) {
        if (isSelf(m)) {
            dashboard.showToast("You can't review yourself.");
            return;
        }
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.add(new JLabel("Leave a Peer Review for " + m.name + ":"));

        String[] ratings = {"5 - Exceptional teammate ⭐⭐⭐⭐⭐", "4 - Very good contribution ⭐⭐⭐⭐", "3 - Solid teamwork ⭐⭐⭐", "2 - Needs improvement ⭐⭐", "1 - Unsatisfactory ⭐"};
        JComboBox<String> ratingBox = new JComboBox<>(ratings);
        panel.add(ratingBox);

        panel.add(new JLabel("Feedback & collaboration notes:"));
        JTextField feedbackField = new JTextField("Great teammate, clear communicator and reliably delivered tasks.");
        panel.add(feedbackField);

        int res = JOptionPane.showConfirmDialog(null, panel, "Peer Review: " + m.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            int ratingNum = 5 - ratingBox.getSelectedIndex();
            String text = feedbackField.getText().trim();
            if (text.isEmpty()) text = "Great collaboration on " + proj.title + "!";
            DashboardSync.submitReview(dashboard, m.id, ratingNum, text, proj.remoteId);
            dashboard.showToast("Review published for " + m.name + "! ⭐");
        }
    }

    private void handleReportClick(StudentDashboard dashboard, MemberAvatar m) {
        if (isSelf(m)) {
            dashboard.showToast("You can't report yourself.");
            return;
        }
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.add(new JLabel("Report Conduct Issue for " + m.name + ":"));

        String[] categories = {"Harassment / Inappropriate behavior", "Spam / Commercial advertising", "Non-responsive / Abandoned project", "Academic dishonesty", "Other conduct violation"};
        JComboBox<String> catBox = new JComboBox<>(categories);
        panel.add(catBox);

        panel.add(new JLabel("Detailed explanation for Admin Moderation Team:"));
        JTextField descField = new JTextField();
        panel.add(descField);

        int res = JOptionPane.showConfirmDialog(null, panel, "Submit Report to Admin: " + m.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String cat = (String) catBox.getSelectedItem();
            String desc = descField.getText().trim();
            if (desc.isEmpty()) desc = "Report filed regarding " + m.name;
            DashboardSync.submitReport(dashboard, "Report against " + m.name, cat, desc, "Medium", m.id);
            dashboard.showToast("Report submitted to Admin Moderation Team. 🛡️");
        }
    }

    public boolean onMouseMove(int mx, int my) {
        boolean prevHov = createBtnHovered;
        createBtnHovered = (mx >= createBtnX && mx <= createBtnX + createBtnW && my >= createBtnY && my <= createBtnY + createBtnH);
        boolean changed = createBtnHovered != prevHov;
        if (activeSubModal == SubModal.PROJECT_BOARD && modalProject != null) {
            boolean prevAdd = addTaskBtnHovered;
            addTaskBtnHovered = mx >= addTaskBtnX && mx <= addTaskBtnX + addTaskBtnW && my >= addTaskBtnY && my <= addTaskBtnY + addTaskBtnH;
            changed |= addTaskBtnHovered != prevAdd;
        }
        return changed;
    }

    public boolean onKey(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            if (activeSubModal == SubModal.VIEW_CANDIDATE_PROFILE) {
                activeSubModal = SubModal.MANAGE_TEAM;
                return true;
            }
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
                    boardScrollToEnd = true;
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
