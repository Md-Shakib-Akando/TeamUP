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
        int cardH = 200;
        p.hitX = cx; p.hitY = cy; p.hitW = cw; p.hitH = cardH;

        RoundRectangle2D cShape = new RoundRectangle2D.Float(cx, cy, cw, cardH, 14, 14);
        g.setColor(Color.WHITE);
        g.fill(cShape);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(cShape);

        // Header: Title + Role pill + Category + Status Badge
        int curX = cx + 20;
        int curY = cy + 28;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(p.title, curX, curY);
        int tw = g.getFontMetrics().stringWidth(p.title);
        curX += tw + 12;

        // Role Tag
        RoundRectangle2D roleTag = new RoundRectangle2D.Float(curX, curY - 14, p.isLead ? 92 : 70, 20, 6, 6);
        g.setColor(p.isLead ? DashboardTheme.PURPLE_TINT_BG : new Color(0xF4, 0xF2, 0xEE));
        g.fill(roleTag);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(p.isLead ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
        g.drawString(p.isLead ? "PROJECT LEAD" : "JOINED", curX + 8, curY);
        curX += (p.isLead ? 92 : 70) + 8;

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
        g.setColor(p.statusBg != null ? p.statusBg : new Color(0xDC, 0xFC, 0xE7));
        g.fill(stBadge);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(p.statusText != null ? p.statusText : new Color(0x15, 0x80, 0x3D));
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

        // Bottom Footer: Team Members Avatars + Action Buttons (No "+ open roles" text!)
        curY += 14;
        int avX = cx + 20;
        int avSize = 28;
        for (MemberAvatar m : p.members) {
            g.setColor(m.bg != null ? m.bg : DashboardTheme.PURPLE_TINT_BG);
            g.fillOval(avX, curY, avSize, avSize);
            g.setFont(DashboardTheme.FONT_BOLD_10);
            g.setColor(m.text != null ? m.text : DashboardTheme.BRAND_PURPLE);
            g.drawString(m.initials, avX + 6, curY + 18);
            avX += avSize - 6; // overlapping effect
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
            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(m.name, mx + 78, curY + 20);

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

            // Action Buttons: [★ Review]  [⚑ Report]  [✕ Remove]
            int btnRight = mx + mw - 36;
            int btnY = curY + 14;

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
            g.setColor(task.done ? new Color(0xF0, 0xFD, 0xF4) : Color.WHITE);
            g.fill(tRow);
            g.setColor(task.done ? new Color(0xBB, 0xF7, 0xD0) : DashboardTheme.BORDER_COLOR);
            g.draw(tRow);

            // Checkbox
            int cbX = task.hitX + 12;
            int cbY = task.hitY + 11;
            int cbS = 18;
            RoundRectangle2D cb = new RoundRectangle2D.Float(cbX, cbY, cbS, cbS, 4, 4);
            g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : Color.WHITE);
            g.fill(cb);
            g.setColor(task.done ? new Color(0x10, 0xB9, 0x81) : new Color(0xD1, 0xD5, 0xDB));
            g.draw(cb);
            if (task.done) {
                g.setColor(Color.WHITE);
                g.setFont(DashboardTheme.FONT_BOLD_11);
                g.drawString("✓", cbX + 4, cbY + 14);
            }

            // Title
            g.setFont(task.done ? DashboardTheme.FONT_REG_13 : DashboardTheme.FONT_BOLD_13);
            g.setColor(task.done ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
            g.drawString(task.title, task.hitX + 40, task.hitY + 25);

            // Due Date
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_LIGHT);
            int dw = g.getFontMetrics().stringWidth(task.dueDate);
            g.drawString(task.dueDate, task.hitX + task.hitW - dw - 14, task.hitY + 25);

            curY += 46;
        }

        // New Task Input Form
        curY += 10;
        newTaskInputX = mx + 26;
        newTaskInputY = curY;
        RoundRectangle2D inputShape = new RoundRectangle2D.Float(newTaskInputX, newTaskInputY, newTaskInputW, newTaskInputH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(inputShape);
        g.setColor(newTaskFocused ? DashboardTheme.BRAND_PURPLE : DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(newTaskFocused ? 1.4f : 1.0f));
        g.draw(inputShape);

        g.setFont(DashboardTheme.FONT_REG_12);
        if (newTaskTitle.isEmpty() && !newTaskFocused) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("Enter a new task title and press Enter...", newTaskInputX + 12, newTaskInputY + 23);
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(newTaskTitle + (newTaskFocused ? "|" : ""), newTaskInputX + 12, newTaskInputY + 23);
        }

        addTaskBtnX = newTaskInputX + newTaskInputW + 10;
        addTaskBtnY = curY;
        RoundRectangle2D addBtn = new RoundRectangle2D.Float(addTaskBtnX, addTaskBtnY, addTaskBtnW, addTaskBtnH, 8, 8);
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fill(addBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString("+ Add Task", addTaskBtnX + 16, addTaskBtnY + 23);
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
                for (ProjectTask task : modalProject.tasks) {
                    if (mx >= task.hitX && mx <= task.hitX + task.hitW && my >= task.hitY && my <= task.hitY + task.hitH) {
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
                return true;
            }
        }

        return false;
    }

    private void handleReviewClick(StudentDashboard dashboard, MemberAvatar m, MyProjectCard proj) {
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
        return createBtnHovered != prevHov;
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
