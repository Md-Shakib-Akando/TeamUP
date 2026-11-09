import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Handles interactive dialog modals for TeamUp Admin Dashboard.
 * Includes Moderation Review Modal, Edit User Role Modal, Add Category/Skill Modal, and Instant 0ms Dismissal.
 * Pure core Java - zero third-party dependencies.
 */
public class AdminModals {

    public enum ActiveModal {
        NONE,
        REVIEW_REPORT,
        ALL_REPORTS,
        EDIT_ROLE,
        ADD_CATEGORY,
        ADD_SKILL,
        USER_DETAILS,
        ADD_USER
    }

    private ActiveModal activeModal = ActiveModal.NONE;
    private AdminOverview.ModerationItem currentItem = null;

    // Edit Role State
    private String editRoleUser = "";
    private String selectedRole = "Member";
    private int roleMemberBtnX, roleModBtnX, roleAdminBtnX, roleChoiceY, roleChoiceW = 100, roleChoiceH = 34;
    private int saveRoleBtnX, saveRoleBtnY, saveRoleBtnW = 120, saveRoleBtnH = 36;
    private boolean saveRoleHovered = false;

    // Add Category / Skill State
    private String inputName = "";
    private int saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW = 120, saveNewItemBtnH = 36;
    private boolean saveNewItemHovered = false;

    // User Details State
    private AdminDataStore.UserModel selectedUser = null;
    private int userModalSuspendBtnX, userModalSuspendBtnY, userModalSuspendBtnW = 140, userModalSuspendBtnH = 36;
    private int userModalRoleBtnX, userModalRoleBtnY, userModalRoleBtnW = 120, userModalRoleBtnH = 36;
    private int userModalDeleteBtnX, userModalDeleteBtnY, userModalDeleteBtnW = 120, userModalDeleteBtnH = 36;
    private int userModalCloseBtnX, userModalCloseBtnY, userModalCloseBtnW = 90, userModalCloseBtnH = 36;
    private boolean userModalSuspendHovered = false;
    private boolean userModalRoleHovered = false;
    private boolean userModalDeleteHovered = false;
    private boolean userModalCloseHovered = false;

    // Add User State
    private String newUserName = "";
    private String newUserEmail = "";
    private String newUserRole = "Member";
    private int activeUserField = 0; // 0 = Name, 1 = Email
    private int saveNewUserBtnX, saveNewUserBtnY, saveNewUserBtnW = 130, saveNewUserBtnH = 36;
    private int cancelNewUserBtnX, cancelNewUserBtnY, cancelNewUserBtnW = 90, cancelNewUserBtnH = 36;
    private boolean saveNewUserHovered = false;
    private boolean cancelNewUserHovered = false;
    private int newUserMemberBtnX, newUserModBtnX, newUserAdminBtnX, newUserRoleY, newUserRoleW = 96, newUserRoleH = 32;
    private int newUserNameInputX, newUserNameInputY, newUserNameInputW, newUserNameInputH = 36;
    private int newUserEmailInputX, newUserEmailInputY, newUserEmailInputW, newUserEmailInputH = 36;

    // Modal card bounds
    private int modalX, modalY, modalW, modalH;
    private int closeBtnX, closeBtnY, closeBtnSize = 28;
    private boolean closeBtnHovered = false;

    // Action buttons inside Review Modal
    private int dismissBtnX, dismissBtnY, dismissBtnW = 120, dismissBtnH = 36;
    private int warnBtnX, warnBtnY, warnBtnW = 120, warnBtnH = 36;
    private int suspendBtnX, suspendBtnY, suspendBtnW = 140, suspendBtnH = 36;

    private boolean dismissHovered = false;
    private boolean warnHovered = false;
    private boolean suspendHovered = false;

    public ActiveModal getActiveModal() { return activeModal; }

    public void openReview(AdminOverview.ModerationItem item) {
        this.currentItem = item;
        this.activeModal = ActiveModal.REVIEW_REPORT;
    }

    public void openAllReports() {
        this.activeModal = ActiveModal.ALL_REPORTS;
    }

    public void openEditRole(String userName, String role) {
        this.editRoleUser = userName;
        this.selectedRole = (role != null) ? role : "Member";
        this.activeModal = ActiveModal.EDIT_ROLE;
    }

    public void openAddCategory() {
        this.inputName = "";
        this.activeModal = ActiveModal.ADD_CATEGORY;
    }

    public void openAddSkill() {
        this.inputName = "";
        this.activeModal = ActiveModal.ADD_SKILL;
    }

    public void openUserDetails(AdminDataStore.UserModel user) {
        this.selectedUser = user;
        this.activeModal = ActiveModal.USER_DETAILS;
    }

    public void openAddUser() {
        this.newUserName = "";
        this.newUserEmail = "";
        this.newUserRole = "Member";
        this.activeUserField = 0;
        this.activeModal = ActiveModal.ADD_USER;
    }

    public void closeModal() {
        this.activeModal = ActiveModal.NONE;
        this.currentItem = null;
        this.editRoleUser = "";
        this.inputName = "";
        this.selectedUser = null;
        this.newUserName = "";
        this.newUserEmail = "";
    }

    public void paint(Graphics2D g, int viewW, int viewH) {
        if (activeModal == ActiveModal.NONE) return;

        // Dark translucent backdrop
        g.setColor(new Color(15, 23, 42, 140));
        g.fillRect(0, 0, viewW, viewH);

        switch (activeModal) {
            case REVIEW_REPORT:
                if (currentItem != null) paintReviewModal(g, viewW, viewH);
                break;
            case ALL_REPORTS:
                paintAllReportsModal(g, viewW, viewH);
                break;
            case EDIT_ROLE:
                paintEditRoleModal(g, viewW, viewH);
                break;
            case ADD_CATEGORY:
                paintAddCategoryModal(g, viewW, viewH);
                break;
            case ADD_SKILL:
                paintAddSkillModal(g, viewW, viewH);
                break;
            case USER_DETAILS:
                if (selectedUser != null) paintUserDetailsModal(g, viewW, viewH);
                break;
            case ADD_USER:
                paintAddUserModal(g, viewW, viewH);
                break;
            default:
                break;
        }
    }

    private void paintModalHeader(Graphics2D g, String title) {
        // Header
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(title, modalX + 28, modalY + 38);

        // Close button (X)
        closeBtnX = modalX + modalW - closeBtnSize - 20;
        closeBtnY = modalY + 18;
        if (closeBtnHovered) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fillOval(closeBtnX, closeBtnY, closeBtnSize, closeBtnSize);
        }
        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("✕", closeBtnX + 9, closeBtnY + 19);

        // Divider
        g.setColor(AdminTheme.BORDER_COLOR);
        g.drawLine(modalX, modalY + 60, modalX + modalW, modalY + 60);
    }

    private void paintReviewModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(540, viewW - 40);
        modalH = 400;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Review Moderation Report");

        int curY = modalY + 90;
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("REPORT ID & TYPE", modalX + 28, curY);
        g.drawString("REPORTER", modalX + 280, curY);

        curY += 20;
        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(currentItem.id + " (" + currentItem.type + ")", modalX + 28, curY);
        g.drawString(currentItem.reporter + " · " + currentItem.date, modalX + 280, curY);

        curY += 32;
        g.setFont(AdminTheme.FONT_SEMIBOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("FLAGGED ITEM SUMMARY", modalX + 28, curY);

        curY += 20;
        g.setFont(AdminTheme.FONT_BOLD_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(currentItem.title, modalX + 28, curY);

        curY += 20;
        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Reported for policy violation or spam invite. Current status: " + currentItem.status + ".", modalX + 28, curY);

        // Action Buttons at bottom
        int btnY = modalY + modalH - 58;

        // 1. Dismiss button
        dismissBtnX = modalX + 28;
        dismissBtnY = btnY;
        RoundRectangle2D disShape = new RoundRectangle2D.Float(dismissBtnX, dismissBtnY, dismissBtnW, dismissBtnH, 8, 8);
        g.setColor(dismissHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(disShape);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(disShape);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Dismiss Report", dismissBtnX + 16, dismissBtnY + 23);

        // 2. Warn User button
        warnBtnX = dismissBtnX + dismissBtnW + 12;
        warnBtnY = btnY;
        RoundRectangle2D warnShape = new RoundRectangle2D.Float(warnBtnX, warnBtnY, warnBtnW, warnBtnH, 8, 8);
        g.setColor(warnHovered ? new Color(0xFE, 0xF3, 0xC7) : Color.WHITE);
        g.fill(warnShape);
        g.setColor(new Color(0xF5, 0x9E, 0x0B));
        g.draw(warnShape);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(new Color(0xD9, 0x77, 0x06));
        g.drawString("Warn User", warnBtnX + 28, warnBtnY + 23);

        // 3. Suspend button
        suspendBtnX = warnBtnX + warnBtnW + 12;
        suspendBtnY = btnY;
        RoundRectangle2D susShape = new RoundRectangle2D.Float(suspendBtnX, suspendBtnY, suspendBtnW, suspendBtnH, 8, 8);
        g.setColor(suspendHovered ? new Color(0xDC, 0x26, 0x26) : AdminTheme.STATUS_RED_TEXT);
        g.fill(susShape);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("Suspend Account", suspendBtnX + 20, suspendBtnY + 23);
    }

    private void paintEditRoleModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(460, viewW - 40);
        modalH = 300;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Edit Member Role");

        int curY = modalY + 90;
        g.setFont(AdminTheme.FONT_BOLD_14);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(editRoleUser, modalX + 28, curY);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Select platform permissions and responsibilities for this user:", modalX + 28, curY + 20);

        roleChoiceY = curY + 40;
        roleMemberBtnX = modalX + 28;
        roleModBtnX = roleMemberBtnX + roleChoiceW + 12;
        roleAdminBtnX = roleModBtnX + roleChoiceW + 12;

        paintRoleOption(g, roleMemberBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Member", "Member".equals(selectedRole));
        paintRoleOption(g, roleModBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Moderator", "Moderator".equals(selectedRole));
        paintRoleOption(g, roleAdminBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Admin", "Admin".equals(selectedRole));

        // Save Button
        saveRoleBtnX = modalX + modalW - saveRoleBtnW - 28;
        saveRoleBtnY = modalY + modalH - 56;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveRoleBtnX, saveRoleBtnY, saveRoleBtnW, saveRoleBtnH, 8, 8);
        g.setColor(saveRoleHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("Save Changes", saveRoleBtnX + 18, saveRoleBtnY + 22);
    }

    private void paintRoleOption(Graphics2D g, int x, int y, int w, int h, String roleName, boolean sel) {
        RoundRectangle2D b = new RoundRectangle2D.Float(x, y, w, h, 8, 8);
        if (sel) {
            g.setColor(AdminTheme.BRAND_GREEN_TINT);
            g.fill(b);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(b);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
        } else {
            g.setColor(Color.WHITE);
            g.fill(b);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(b);
            g.setColor(AdminTheme.TEXT_MAIN);
        }
        g.setFont(AdminTheme.FONT_BOLD_12);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(roleName);
        g.drawString(roleName, x + (w - tw) / 2, y + 21);
    }

    private void paintAddCategoryModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(460, viewW - 40);
        modalH = 260;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Add Project Category");

        int curY = modalY + 90;
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("CATEGORY NAME", modalX + 28, curY);

        RoundRectangle2D input = new RoundRectangle2D.Float(modalX + 28, curY + 10, modalW - 56, 36, 8, 8);
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(input);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(input);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(inputName.isEmpty() ? "e.g. Mobile Apps, Bioengineering..." : inputName, modalX + 40, curY + 34);

        // Save
        saveNewItemBtnX = modalX + modalW - saveNewItemBtnW - 28;
        saveNewItemBtnY = modalY + modalH - 56;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW, saveNewItemBtnH, 8, 8);
        g.setColor(saveNewItemHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("Create Category", saveNewItemBtnX + 14, saveNewItemBtnY + 22);
    }

    private void paintAddSkillModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(460, viewW - 40);
        modalH = 260;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Add Skill Tag");

        int curY = modalY + 90;
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("SKILL NAME", modalX + 28, curY);

        RoundRectangle2D input = new RoundRectangle2D.Float(modalX + 28, curY + 10, modalW - 56, 36, 8, 8);
        g.setColor(AdminTheme.BG_CANVAS);
        g.fill(input);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(input);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(inputName.isEmpty() ? "e.g. Python, Figma, Docker..." : inputName, modalX + 40, curY + 34);

        // Save
        saveNewItemBtnX = modalX + modalW - saveNewItemBtnW - 28;
        saveNewItemBtnY = modalY + modalH - 56;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW, saveNewItemBtnH, 8, 8);
        g.setColor(saveNewItemHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("Create Skill", saveNewItemBtnX + 24, saveNewItemBtnY + 22);
    }

    private void paintAllReportsModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(640, viewW - 40);
        modalH = 460;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Full Moderation Queue (18 Reports)");

        int curY = modalY + 84;
        String[] sampleReports = {
                "R-1048 · Message: StudyBuddy AI conversation (Harassment) · Pending",
                "R-1047 · Project: SkillSwap Marketplace (Misleading content) · In review",
                "R-1046 · Message: Unsolicited project invite (Spam) · Pending",
                "R-1045 · Profile: User profile biography (Inappropriate) · Pending",
                "R-1044 · Comment: Research Commons (Off-topic) · Resolved",
                "R-1043 · Message: Direct message harassment · Pending"
        };

        for (String r : sampleReports) {
            RoundRectangle2D row = new RoundRectangle2D.Float(modalX + 24, curY, modalW - 48, 42, 8, 8);
            g.setColor(AdminTheme.BG_CANVAS);
            g.fill(row);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.draw(row);

            g.setFont(AdminTheme.FONT_REGULAR_12);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(r, modalX + 38, curY + 26);
            curY += 50;
        }
    }

    private void paintUserDetailsModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(540, viewW - 40);
        modalH = 460;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Member Profile & Access Control");

        int curY = modalY + 80;

        // User Header Card
        RoundRectangle2D uCard = new RoundRectangle2D.Float(modalX + 24, curY, modalW - 48, 76, 12, 12);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(uCard);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(uCard);

        // Avatar circle
        int avSize = 48;
        int avX = modalX + 38;
        int avY = curY + 14;
        g.setColor(new Color(0xE2, 0xEE, 0xE7));
        g.fillOval(avX, avY, avSize, avSize);
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.BRAND_GREEN_DARK);
        FontMetrics afm = g.getFontMetrics();
        g.drawString(selectedUser.initials, avX + (avSize - afm.stringWidth(selectedUser.initials)) / 2, avY + 31);

        // Name & Email
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(selectedUser.name, avX + avSize + 16, curY + 32);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString(selectedUser.email, avX + avSize + 16, curY + 52);

        // Role & Status Badges
        int badgeRight = modalX + modalW - 44;
        boolean isActive = "Active".equalsIgnoreCase(selectedUser.status);

        // Status pill
        int statusW = isActive ? 58 : 78;
        badgeRight -= statusW;
        RoundRectangle2D sPill = new RoundRectangle2D.Float(badgeRight, curY + 28, statusW, 22, 11, 11);
        g.setColor(isActive ? new Color(0xE6, 0xF4, 0xEA) : new Color(0xFE, 0xE2, 0xE2));
        g.fill(sPill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(isActive ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
        g.drawString(selectedUser.status, badgeRight + 10, curY + 43);

        // Role pill
        int roleW = "Admin".equalsIgnoreCase(selectedUser.role) ? 54 : ("Moderator".equalsIgnoreCase(selectedUser.role) ? 72 : 60);
        badgeRight -= (roleW + 8);
        RoundRectangle2D rPill = new RoundRectangle2D.Float(badgeRight, curY + 28, roleW, 22, 11, 11);
        g.setColor(new Color(0xE0, 0xF2, 0xFE));
        g.fill(rPill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(new Color(0x03, 0x69, 0xA1));
        g.drawString(selectedUser.role, badgeRight + 8, curY + 43);

        curY += 92;

        // 4 Details Grid Cards (2x2)
        int cardW = (modalW - 48 - 14) / 2;
        int cardH = 54;

        // Card 1: Member ID
        paintDetailMiniCard(g, modalX + 24, curY, cardW, cardH, "MEMBER ID", selectedUser.id);
        // Card 2: Joined Date
        paintDetailMiniCard(g, modalX + 24 + cardW + 14, curY, cardW, cardH, "JOINED DATE", selectedUser.joined);

        curY += cardH + 12;

        // Card 3: University
        paintDetailMiniCard(g, modalX + 24, curY, cardW, cardH, "ORGANIZATION", "University of Washington");
        // Card 4: Trust
        paintDetailMiniCard(g, modalX + 24 + cardW + 14, curY, cardW, cardH, "SECURITY ACCESS", "✓ Verified NetID · 2FA Active");

        curY += cardH + 14;

        // Activity banner
        RoundRectangle2D actCard = new RoundRectangle2D.Float(modalX + 24, curY, modalW - 48, 40, 8, 8);
        g.setColor(new Color(0xFA, 0xFA, 0xFA));
        g.fill(actCard);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(actCard);

        g.setFont(AdminTheme.FONT_REGULAR_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Engagement: 3 Projects Active · 28 Sprint Tasks Completed · 0 Policy Flags", modalX + 38, curY + 25);

        // Bottom Action buttons
        int btnY = modalY + modalH - 56;
        int btnSpacing = 10;
        int curBtnX = modalX + 24;

        // Suspend / Reactivate
        userModalSuspendBtnX = curBtnX;
        userModalSuspendBtnY = btnY;
        userModalSuspendBtnW = isActive ? 130 : 140;
        RoundRectangle2D susB = new RoundRectangle2D.Float(userModalSuspendBtnX, userModalSuspendBtnY, userModalSuspendBtnW, userModalSuspendBtnH, 8, 8);
        g.setColor(userModalSuspendHovered ? (isActive ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xDC, 0xFC, 0xE7)) : Color.WHITE);
        g.fill(susB);
        g.setColor(isActive ? new Color(0xEF, 0x44, 0x44) : AdminTheme.BRAND_GREEN_DARK);
        g.draw(susB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(isActive ? "Suspend User" : "Reactivate User", userModalSuspendBtnX + 16, userModalSuspendBtnY + 22);

        curBtnX += userModalSuspendBtnW + btnSpacing;

        // Edit Role
        userModalRoleBtnX = curBtnX;
        userModalRoleBtnY = btnY;
        userModalRoleBtnW = 100;
        RoundRectangle2D rolB = new RoundRectangle2D.Float(userModalRoleBtnX, userModalRoleBtnY, userModalRoleBtnW, userModalRoleBtnH, 8, 8);
        g.setColor(userModalRoleHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(rolB);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(rolB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("Edit Role", userModalRoleBtnX + 20, userModalRoleBtnY + 22);

        curBtnX += userModalRoleBtnW + btnSpacing;

        // Delete User
        userModalDeleteBtnX = curBtnX;
        userModalDeleteBtnY = btnY;
        userModalDeleteBtnW = 100;
        RoundRectangle2D delB = new RoundRectangle2D.Float(userModalDeleteBtnX, userModalDeleteBtnY, userModalDeleteBtnW, userModalDeleteBtnH, 8, 8);
        g.setColor(userModalDeleteHovered ? new Color(0xFE, 0xF2, 0xF2) : Color.WHITE);
        g.fill(delB);
        g.setColor(new Color(0xFC, 0xA5, 0xA5));
        g.draw(delB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(new Color(0xDC, 0x26, 0x26));
        g.drawString("Delete", userModalDeleteBtnX + 28, userModalDeleteBtnY + 22);

        // Close on far right
        userModalCloseBtnX = modalX + modalW - 24 - userModalCloseBtnW;
        userModalCloseBtnY = btnY;
        RoundRectangle2D clsB = new RoundRectangle2D.Float(userModalCloseBtnX, userModalCloseBtnY, userModalCloseBtnW, userModalCloseBtnH, 8, 8);
        g.setColor(userModalCloseHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(clsB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Close", userModalCloseBtnX + 26, userModalCloseBtnY + 22);
    }

    private void paintDetailMiniCard(Graphics2D g, int cx, int cy, int cw, int ch, String label, String val) {
        RoundRectangle2D r = new RoundRectangle2D.Float(cx, cy, cw, ch, 8, 8);
        g.setColor(new Color(0xFA, 0xFA, 0xFA));
        g.fill(r);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(r);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString(label, cx + 12, cy + 19);

        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(val, cx + 12, cy + 39);
    }

    private void paintAddUserModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(500, viewW - 40);
        modalH = 390;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Add New Community Member");

        int curY = modalY + 84;

        // 1. Full Name
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("FULL NAME", modalX + 28, curY);

        newUserNameInputX = modalX + 28;
        newUserNameInputY = curY + 8;
        newUserNameInputW = modalW - 56;
        RoundRectangle2D nameInput = new RoundRectangle2D.Float(newUserNameInputX, newUserNameInputY, newUserNameInputW, newUserNameInputH, 8, 8);
        g.setColor(activeUserField == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(nameInput);
        g.setColor(activeUserField == 0 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(nameInput);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        if (newUserName.isEmpty() && activeUserField != 0) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("e.g. Maya Lin", newUserNameInputX + 12, newUserNameInputY + 23);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(newUserName + (activeUserField == 0 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""), newUserNameInputX + 12, newUserNameInputY + 23);
        }

        curY += 66;

        // 2. Email Address
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("EMAIL ADDRESS", modalX + 28, curY);

        newUserEmailInputX = modalX + 28;
        newUserEmailInputY = curY + 8;
        newUserEmailInputW = modalW - 56;
        RoundRectangle2D emailInput = new RoundRectangle2D.Float(newUserEmailInputX, newUserEmailInputY, newUserEmailInputW, newUserEmailInputH, 8, 8);
        g.setColor(activeUserField == 1 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(emailInput);
        g.setColor(activeUserField == 1 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(emailInput);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        if (newUserEmail.isEmpty() && activeUserField != 1) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("e.g. maya.lin@uw.edu", newUserEmailInputX + 12, newUserEmailInputY + 23);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(newUserEmail + (activeUserField == 1 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""), newUserEmailInputX + 12, newUserEmailInputY + 23);
        }

        curY += 66;

        // 3. Platform Role
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("PLATFORM ROLE", modalX + 28, curY);

        newUserRoleY = curY + 8;
        newUserMemberBtnX = modalX + 28;
        newUserModBtnX = newUserMemberBtnX + newUserRoleW + 10;
        newUserAdminBtnX = newUserModBtnX + newUserRoleW + 10;

        paintRoleOption(g, newUserMemberBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Member", "Member".equalsIgnoreCase(newUserRole));
        paintRoleOption(g, newUserModBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Moderator", "Moderator".equalsIgnoreCase(newUserRole));
        paintRoleOption(g, newUserAdminBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Admin", "Admin".equalsIgnoreCase(newUserRole));

        // Bottom buttons
        int btnY = modalY + modalH - 56;

        // Cancel button
        cancelNewUserBtnX = modalX + 28;
        cancelNewUserBtnY = btnY;
        RoundRectangle2D cBtn = new RoundRectangle2D.Float(cancelNewUserBtnX, cancelNewUserBtnY, cancelNewUserBtnW, cancelNewUserBtnH, 8, 8);
        g.setColor(cancelNewUserHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cBtn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(cBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Cancel", cancelNewUserBtnX + 24, cancelNewUserBtnY + 22);

        // Create Member button
        saveNewUserBtnX = modalX + modalW - 28 - saveNewUserBtnW;
        saveNewUserBtnY = btnY;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveNewUserBtnX, saveNewUserBtnY, saveNewUserBtnW, saveNewUserBtnH, 8, 8);
        g.setColor(saveNewUserHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Create Member", saveNewUserBtnX + 18, saveNewUserBtnY + 22);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        if (activeModal == ActiveModal.NONE) return false;

        // Click outside modal card -> close modal (0ms instant)
        if (mx < modalX || mx > modalX + modalW || my < modalY || my > modalY + modalH) {
            closeModal();
            return true;
        }

        // Close button (X) -> close modal (0ms instant)
        if (mx >= closeBtnX && mx <= closeBtnX + closeBtnSize && my >= closeBtnY && my <= closeBtnY + closeBtnSize) {
            closeModal();
            return true;
        }

        // Review Modal Actions
        if (activeModal == ActiveModal.REVIEW_REPORT) {
            if (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY && my <= dismissBtnY + dismissBtnH) {
                if (currentItem != null) {
                    currentItem.status = "Dismissed";
                    AdminDataStore.getInstance().resolveReport(currentItem.id, "Dismissed via review modal");
                }
                dashboard.showToast("Report " + (currentItem != null ? currentItem.id : "") + " dismissed");
                closeModal();
                return true;
            }
            if (mx >= warnBtnX && mx <= warnBtnX + warnBtnW && my >= warnBtnY && my <= warnBtnY + warnBtnH) {
                if (currentItem != null) {
                    currentItem.status = "Warned";
                    AdminDataStore.getInstance().warnUserReport(currentItem.id, "Warning issued via review modal");
                }
                dashboard.showToast("Warning issued to reported user");
                closeModal();
                return true;
            }
            if (mx >= suspendBtnX && mx <= suspendBtnX + suspendBtnW && my >= suspendBtnY && my <= suspendBtnY + suspendBtnH) {
                if (currentItem != null) {
                    currentItem.status = "Suspended";
                    AdminDataStore.getInstance().suspendUser("Evan Reed");
                }
                dashboard.showToast("Account suspended for policy violation");
                closeModal();
                return true;
            }
        }

        // Edit Role Modal Actions
        if (activeModal == ActiveModal.EDIT_ROLE) {
            if (mx >= roleMemberBtnX && mx <= roleMemberBtnX + roleChoiceW && my >= roleChoiceY && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Member";
                return true;
            }
            if (mx >= roleModBtnX && mx <= roleModBtnX + roleChoiceW && my >= roleChoiceY && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Moderator";
                return true;
            }
            if (mx >= roleAdminBtnX && mx <= roleAdminBtnX + roleChoiceW && my >= roleChoiceY && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Admin";
                return true;
            }
            if (mx >= saveRoleBtnX && mx <= saveRoleBtnX + saveRoleBtnW && my >= saveRoleBtnY && my <= saveRoleBtnY + saveRoleBtnH) {
                AdminDataStore.getInstance().updateUserRole(editRoleUser, selectedRole);
                dashboard.showToast("Role updated to " + selectedRole + " for " + editRoleUser);
                closeModal();
                return true;
            }
        }

        // Add Category / Skill
        if (activeModal == ActiveModal.ADD_CATEGORY) {
            if (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY && my <= saveNewItemBtnY + saveNewItemBtnH) {
                String catName = inputName.trim().isEmpty() ? "Mobile Development" : inputName.trim();
                AdminDataStore.getInstance().addCategory(catName);
                dashboard.showToast("Category '" + catName + "' created successfully");
                closeModal();
                return true;
            }
        }
        if (activeModal == ActiveModal.ADD_SKILL) {
            if (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY && my <= saveNewItemBtnY + saveNewItemBtnH) {
                String skillName = inputName.trim().isEmpty() ? "Python" : inputName.trim();
                AdminDataStore.getInstance().addSkill(skillName);
                dashboard.showToast("Skill tag '" + skillName + "' added successfully");
                closeModal();
                return true;
            }
        }

        // User Details Modal Actions
        if (activeModal == ActiveModal.USER_DETAILS && selectedUser != null) {
            if (mx >= userModalSuspendBtnX && mx <= userModalSuspendBtnX + userModalSuspendBtnW &&
                    my >= userModalSuspendBtnY && my <= userModalSuspendBtnY + userModalSuspendBtnH) {
                if ("Active".equalsIgnoreCase(selectedUser.status)) {
                    AdminDataStore.getInstance().suspendUser(selectedUser.name);
                    dashboard.showToast("Account suspended for " + selectedUser.name);
                } else {
                    AdminDataStore.getInstance().reactivateUser(selectedUser.name);
                    dashboard.showToast("Account reactivated for " + selectedUser.name);
                }
                selectedUser = AdminDataStore.getInstance().getUserByName(selectedUser.name);
                return true;
            }
            if (mx >= userModalRoleBtnX && mx <= userModalRoleBtnX + userModalRoleBtnW &&
                    my >= userModalRoleBtnY && my <= userModalRoleBtnY + userModalRoleBtnH) {
                openEditRole(selectedUser.name, selectedUser.role);
                return true;
            }
            if (mx >= userModalDeleteBtnX && mx <= userModalDeleteBtnX + userModalDeleteBtnW &&
                    my >= userModalDeleteBtnY && my <= userModalDeleteBtnY + userModalDeleteBtnH) {
                String dName = selectedUser.name;
                AdminDataStore.getInstance().deleteUser(dName);
                dashboard.showToast("Deleted user record: " + dName);
                closeModal();
                return true;
            }
            if (mx >= userModalCloseBtnX && mx <= userModalCloseBtnX + userModalCloseBtnW &&
                    my >= userModalCloseBtnY && my <= userModalCloseBtnY + userModalCloseBtnH) {
                closeModal();
                return true;
            }
        }

        // Add User Modal Actions
        if (activeModal == ActiveModal.ADD_USER) {
            if (mx >= newUserNameInputX && mx <= newUserNameInputX + newUserNameInputW &&
                    my >= newUserNameInputY && my <= newUserNameInputY + newUserNameInputH) {
                activeUserField = 0;
                return true;
            }
            if (mx >= newUserEmailInputX && mx <= newUserEmailInputX + newUserEmailInputW &&
                    my >= newUserEmailInputY && my <= newUserEmailInputY + newUserEmailInputH) {
                activeUserField = 1;
                return true;
            }
            if (mx >= newUserMemberBtnX && mx <= newUserMemberBtnX + newUserRoleW &&
                    my >= newUserRoleY && my <= newUserRoleY + newUserRoleH) {
                newUserRole = "Member";
                return true;
            }
            if (mx >= newUserModBtnX && mx <= newUserModBtnX + newUserRoleW &&
                    my >= newUserRoleY && my <= newUserRoleY + newUserRoleH) {
                newUserRole = "Moderator";
                return true;
            }
            if (mx >= newUserAdminBtnX && mx <= newUserAdminBtnX + newUserRoleW &&
                    my >= newUserRoleY && my <= newUserRoleY + newUserRoleH) {
                newUserRole = "Admin";
                return true;
            }
            if (mx >= cancelNewUserBtnX && mx <= cancelNewUserBtnX + cancelNewUserBtnW &&
                    my >= cancelNewUserBtnY && my <= cancelNewUserBtnY + cancelNewUserBtnH) {
                closeModal();
                return true;
            }
            if (mx >= saveNewUserBtnX && mx <= saveNewUserBtnX + saveNewUserBtnW &&
                    my >= saveNewUserBtnY && my <= saveNewUserBtnY + saveNewUserBtnH) {
                if (newUserName.trim().isEmpty()) {
                    dashboard.showToast("Please enter a user name");
                    return true;
                }
                AdminDataStore.getInstance().addUser(newUserName, newUserEmail, newUserRole);
                dashboard.showToast("User '" + newUserName + "' added successfully");
                closeModal();
                return true;
            }
        }

        return true;
    }

    public boolean onKey(char ch, int keyCode) {
        if (activeModal == ActiveModal.ADD_CATEGORY || activeModal == ActiveModal.ADD_SKILL) {
            if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (inputName.length() > 0) {
                    inputName = inputName.substring(0, inputName.length() - 1);
                    return true;
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                // Submit modal
                if (activeModal == ActiveModal.ADD_CATEGORY) {
                    String catName = inputName.trim().isEmpty() ? "Mobile Development" : inputName.trim();
                    AdminDataStore.getInstance().addCategory(catName);
                } else {
                    String skillName = inputName.trim().isEmpty() ? "Python" : inputName.trim();
                    AdminDataStore.getInstance().addSkill(skillName);
                }
                closeModal();
                return true;
            } else if (ch >= 32 && ch <= 126) {
                inputName += ch;
                return true;
            }
        } else if (activeModal == ActiveModal.ADD_USER) {
            if (keyCode == java.awt.event.KeyEvent.VK_TAB) {
                activeUserField = (activeUserField + 1) % 2;
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                if (activeUserField == 0 && newUserName.trim().length() > 0) {
                    activeUserField = 1;
                    return true;
                } else {
                    if (!newUserName.trim().isEmpty()) {
                        AdminDataStore.getInstance().addUser(newUserName, newUserEmail, newUserRole);
                        closeModal();
                        return true;
                    }
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (activeUserField == 0) {
                    if (newUserName.length() > 0) {
                        newUserName = newUserName.substring(0, newUserName.length() - 1);
                        return true;
                    }
                } else {
                    if (newUserEmail.length() > 0) {
                        newUserEmail = newUserEmail.substring(0, newUserEmail.length() - 1);
                        return true;
                    }
                }
            } else if (ch >= 32 && ch <= 126) {
                if (activeUserField == 0) {
                    newUserName += ch;
                } else {
                    newUserEmail += ch;
                }
                return true;
            }
        }
        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        if (activeModal == ActiveModal.NONE) return false;

        boolean pClose = closeBtnHovered;
        closeBtnHovered = (mx >= closeBtnX && mx <= closeBtnX + closeBtnSize && my >= closeBtnY && my <= closeBtnY + closeBtnSize);

        boolean pDis = dismissHovered;
        dismissHovered = (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY && my <= dismissBtnY + dismissBtnH);

        boolean pWarn = warnHovered;
        warnHovered = (mx >= warnBtnX && mx <= warnBtnX + warnBtnW && my >= warnBtnY && my <= warnBtnY + warnBtnH);

        boolean pSus = suspendHovered;
        suspendHovered = (mx >= suspendBtnX && mx <= suspendBtnX + suspendBtnW && my >= suspendBtnY && my <= suspendBtnY + suspendBtnH);

        boolean pSaveR = saveRoleHovered;
        saveRoleHovered = (mx >= saveRoleBtnX && mx <= saveRoleBtnX + saveRoleBtnW && my >= saveRoleBtnY && my <= saveRoleBtnY + saveRoleBtnH);

        boolean pSaveNew = saveNewItemHovered;
        saveNewItemHovered = (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY && my <= saveNewItemBtnY + saveNewItemBtnH);

        boolean pUsus = userModalSuspendHovered;
        userModalSuspendHovered = (mx >= userModalSuspendBtnX && mx <= userModalSuspendBtnX + userModalSuspendBtnW && my >= userModalSuspendBtnY && my <= userModalSuspendBtnY + userModalSuspendBtnH);

        boolean pUrole = userModalRoleHovered;
        userModalRoleHovered = (mx >= userModalRoleBtnX && mx <= userModalRoleBtnX + userModalRoleBtnW && my >= userModalRoleBtnY && my <= userModalRoleBtnY + userModalRoleBtnH);

        boolean pUdel = userModalDeleteHovered;
        userModalDeleteHovered = (mx >= userModalDeleteBtnX && mx <= userModalDeleteBtnX + userModalDeleteBtnW && my >= userModalDeleteBtnY && my <= userModalDeleteBtnY + userModalDeleteBtnH);

        boolean pUclose = userModalCloseHovered;
        userModalCloseHovered = (mx >= userModalCloseBtnX && mx <= userModalCloseBtnX + userModalCloseBtnW && my >= userModalCloseBtnY && my <= userModalCloseBtnY + userModalCloseBtnH);

        boolean pSnu = saveNewUserHovered;
        saveNewUserHovered = (mx >= saveNewUserBtnX && mx <= saveNewUserBtnX + saveNewUserBtnW && my >= saveNewUserBtnY && my <= saveNewUserBtnY + saveNewUserBtnH);

        boolean pCnu = cancelNewUserHovered;
        cancelNewUserHovered = (mx >= cancelNewUserBtnX && mx <= cancelNewUserBtnX + cancelNewUserBtnW && my >= cancelNewUserBtnY && my <= cancelNewUserBtnY + cancelNewUserBtnH);

        return pClose != closeBtnHovered || pDis != dismissHovered || pWarn != warnHovered || pSus != suspendHovered ||
                pSaveR != saveRoleHovered || pSaveNew != saveNewItemHovered || pUsus != userModalSuspendHovered ||
                pUrole != userModalRoleHovered || pUdel != userModalDeleteHovered || pUclose != userModalCloseHovered ||
                pSnu != saveNewUserHovered || pCnu != cancelNewUserHovered;
    }
}
