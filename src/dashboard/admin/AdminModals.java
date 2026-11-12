
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Handles interactive dialog modals for TeamUp Admin Dashboard.
 * Includes Moderation Review Modal, Edit User Role Modal, Add Category/Skill
 * Modal, and Instant 0ms Dismissal.
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
        ADD_USER,
        PROJECT_DETAILS,
        ADD_PROJECT,
        REVIEW_DETAILS,
        ADD_REVIEW
    }

    private ActiveModal activeModal = ActiveModal.NONE;
    private AdminOverview.ModerationItem currentItem = null;

    // Review Details State
    private AdminDataStore.ReviewModel selectedReview = null;
    private int revModalDeleteBtnX, revModalDeleteBtnY, revModalDeleteBtnW = 140, revModalDeleteBtnH = 36;
    private int revModalApproveBtnX, revModalApproveBtnY, revModalApproveBtnW = 130, revModalApproveBtnH = 36;
    private int revModalHideBtnX, revModalHideBtnY, revModalHideBtnW = 90, revModalHideBtnH = 36;
    private int revModalWarnBtnX, revModalWarnBtnY, revModalWarnBtnW = 110, revModalWarnBtnH = 36;
    private int revModalCloseBtnX, revModalCloseBtnY, revModalCloseBtnW = 90, revModalCloseBtnH = 36;
    private boolean revModalDeleteHovered = false;
    private boolean revModalApproveHovered = false;
    private boolean revModalHideHovered = false;
    private boolean revModalWarnHovered = false;
    private boolean revModalCloseHovered = false;

    // Add Review State
    private String newRevReviewer = "";
    private String newRevReviewee = "Alex Morgan";
    private String newRevProject = "Campus Carbon Tracker";
    private String newRevRole = "Frontend Contributor";
    private int newRevRating = 5;
    private String newRevType = "Team Member Review";
    private String newRevText = "";
    private int activeReviewField = 0; // 0=Reviewer, 1=Reviewee, 2=Project, 3=Role, 4=Text
    private int saveNewRevBtnX, saveNewRevBtnY, saveNewRevBtnW = 130, saveNewRevBtnH = 36;
    private int cancelNewRevBtnX, cancelNewRevBtnY, cancelNewRevBtnW = 90, cancelNewRevBtnH = 36;
    private boolean saveNewRevHovered = false;
    private boolean cancelNewRevHovered = false;
    private int newRevReviewerInputX, newRevReviewerInputY, newRevReviewerInputW, newRevReviewerInputH = 34;
    private int newRevRevieweeInputX, newRevRevieweeInputY, newRevRevieweeInputW, newRevRevieweeInputH = 34;
    private int newRevProjectInputX, newRevProjectInputY, newRevProjectInputW, newRevProjectInputH = 34;
    private int newRevRoleInputX, newRevRoleInputY, newRevRoleInputW, newRevRoleInputH = 34;
    private int newRevTextInputX, newRevTextInputY, newRevTextInputW, newRevTextInputH = 46;
    private int newRevStarX, newRevStarY, newRevStarSize = 24;
    private int newRevTypeTeamX, newRevTypeUserX, newRevTypeCollabX, newRevTypeY, newRevTypeW = 136, newRevTypeH = 28;

    // Edit Role State
    private String editRoleUser = "";
    private String selectedRole = "Member";
    private int roleMemberBtnX, roleModBtnX, roleAdminBtnX, roleChoiceY, roleChoiceW = 100, roleChoiceH = 34;
    private int saveRoleBtnX, saveRoleBtnY, saveRoleBtnW = 120, saveRoleBtnH = 36;
    private boolean saveRoleHovered = false;

    // Add Category / Skill State
    private String inputName = "";
    private int newItemInputX, newItemInputY, newItemInputW, newItemInputH = 38;
    private boolean newItemInputFocused = true;
    private int saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW = 140, saveNewItemBtnH = 36;
    private int cancelNewItemBtnX, cancelNewItemBtnY, cancelNewItemBtnW = 96, cancelNewItemBtnH = 36;
    private boolean saveNewItemHovered = false;
    private boolean cancelNewItemHovered = false;

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

    // Project Details State
    private AdminDataStore.ProjectModel selectedProject = null;
    private int projModalApproveBtnX, projModalApproveBtnY, projModalApproveBtnW = 140, projModalApproveBtnH = 36;
    private int projModalStatusBtnX, projModalStatusBtnY, projModalStatusBtnW = 120, projModalStatusBtnH = 36;
    private int projModalDeleteBtnX, projModalDeleteBtnY, projModalDeleteBtnW = 100, projModalDeleteBtnH = 36;
    private int projModalCloseBtnX, projModalCloseBtnY, projModalCloseBtnW = 90, projModalCloseBtnH = 36;
    private boolean projModalApproveHovered = false;
    private boolean projModalStatusHovered = false;
    private boolean projModalDeleteHovered = false;
    private boolean projModalCloseHovered = false;

    // Add Project State
    public static class CatChoiceBox {
        public String name;
        public int x, y, w, h;
    }

    private final java.util.List<CatChoiceBox> addProjCatBoxes = new java.util.ArrayList<>();
    private String newProjectName = "";
    private String newProjectCategory = "Web Development";
    private String newProjectOwner = "Jordan Lee";
    private String newProjectTeamSize = "1 / 6";
    private String newProjectStatus = "Active";
    private String newProjectDesc = "";
    private int activeProjectField = 0; // 0 = Name, 1 = Owner, 2 = TeamSize, 3 = Description
    private int saveNewProjectBtnX, saveNewProjectBtnY, saveNewProjectBtnW = 130, saveNewProjectBtnH = 36;
    private int cancelNewProjectBtnX, cancelNewProjectBtnY, cancelNewProjectBtnW = 90, cancelNewProjectBtnH = 36;
    private boolean saveNewProjectHovered = false;
    private boolean cancelNewProjectHovered = false;
    private int newProjectNameInputX, newProjectNameInputY, newProjectNameInputW, newProjectNameInputH = 34;
    private int newProjectOwnerInputX, newProjectOwnerInputY, newProjectOwnerInputW, newProjectOwnerInputH = 34;
    private int newProjectSizeInputX, newProjectSizeInputY, newProjectSizeInputW, newProjectSizeInputH = 34;
    private int newProjectDescInputX, newProjectDescInputY, newProjectDescInputW, newProjectDescInputH = 46;
    private int addProjStatusActiveX, addProjStatusRecruitingX, addProjStatusReviewX, addProjStatusY,
            addProjStatusW = 94, addProjStatusH = 28;

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

    public ActiveModal getActiveModal() {
        return activeModal;
    }

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
        this.newItemInputFocused = true;
        this.activeModal = ActiveModal.ADD_CATEGORY;
    }

    public void openAddSkill() {
        this.inputName = "";
        this.newItemInputFocused = true;
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

    public void openProjectDetails(AdminDataStore.ProjectModel project) {
        this.selectedProject = project;
        this.activeModal = ActiveModal.PROJECT_DETAILS;
    }

    public void openAddProject() {
        this.newProjectName = "";
        this.newProjectCategory = "Web Development";
        this.newProjectOwner = "Jordan Lee";
        this.newProjectTeamSize = "1 / 6";
        this.newProjectStatus = "Active";
        this.newProjectDesc = "";
        this.activeProjectField = 0;
        this.activeModal = ActiveModal.ADD_PROJECT;
    }

    public void openReviewDetails(AdminDataStore.ReviewModel review) {
        this.selectedReview = review;
        this.activeModal = ActiveModal.REVIEW_DETAILS;
    }

    public void openAddReview() {
        this.newRevReviewer = "";
        this.newRevReviewee = "Alex Morgan";
        this.newRevProject = "Campus Carbon Tracker";
        this.newRevRole = "Frontend Contributor";
        this.newRevRating = 5;
        this.newRevType = "Team Member Review";
        this.newRevText = "";
        this.activeReviewField = 0;
        this.activeModal = ActiveModal.ADD_REVIEW;
    }

    public void closeModal() {
        this.activeModal = ActiveModal.NONE;
        this.currentItem = null;
        this.editRoleUser = "";
        this.inputName = "";
        this.selectedUser = null;
        this.newUserName = "";
        this.newUserEmail = "";
        this.selectedProject = null;
        this.newProjectName = "";
        this.newProjectDesc = "";
        this.selectedReview = null;
        this.newRevReviewer = "";
        this.newRevText = "";
    }

    public void paint(Graphics2D g, int viewW, int viewH) {
        if (activeModal == ActiveModal.NONE)
            return;

        // Dark translucent backdrop
        g.setColor(new Color(15, 23, 42, 140));
        g.fillRect(0, 0, viewW, viewH);

        switch (activeModal) {
            case REVIEW_REPORT:
                if (currentItem != null)
                    paintReviewModal(g, viewW, viewH);
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
                if (selectedUser != null)
                    paintUserDetailsModal(g, viewW, viewH);
                break;
            case ADD_USER:
                paintAddUserModal(g, viewW, viewH);
                break;
            case PROJECT_DETAILS:
                if (selectedProject != null)
                    paintProjectDetailsModal(g, viewW, viewH);
                break;
            case ADD_PROJECT:
                paintAddProjectModal(g, viewW, viewH);
                break;
            case REVIEW_DETAILS:
                if (selectedReview != null)
                    paintReviewDetailsModal(g, viewW, viewH);
                break;
            case ADD_REVIEW:
                paintAddReviewModal(g, viewW, viewH);
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
        g.drawString("Reported for policy violation or spam invite. Current status: " + currentItem.status + ".",
                modalX + 28, curY);

        // Action Buttons at bottom
        int btnY = modalY + modalH - 58;

        // 1. Dismiss button
        dismissBtnX = modalX + 28;
        dismissBtnY = btnY;
        RoundRectangle2D disShape = new RoundRectangle2D.Float(dismissBtnX, dismissBtnY, dismissBtnW, dismissBtnH, 8,
                8);
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
        RoundRectangle2D susShape = new RoundRectangle2D.Float(suspendBtnX, suspendBtnY, suspendBtnW, suspendBtnH, 8,
                8);
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

        paintRoleOption(g, roleMemberBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Member",
                "Member".equals(selectedRole));
        paintRoleOption(g, roleModBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Moderator",
                "Moderator".equals(selectedRole));
        paintRoleOption(g, roleAdminBtnX, roleChoiceY, roleChoiceW, roleChoiceH, "Admin", "Admin".equals(selectedRole));

        // Save Button
        saveRoleBtnX = modalX + modalW - saveRoleBtnW - 28;
        saveRoleBtnY = modalY + modalH - 56;
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveRoleBtnX, saveRoleBtnY, saveRoleBtnW, saveRoleBtnH, 8,
                8);
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
        modalW = Math.min(480, viewW - 40);
        modalH = 280;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Add Project Category");

        int curY = modalY + 84;
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("CATEGORY NAME *", modalX + 28, curY);

        newItemInputX = modalX + 28;
        newItemInputY = curY + 8;
        newItemInputW = modalW - 56;
        newItemInputH = 38;

        RoundRectangle2D input = new RoundRectangle2D.Float(newItemInputX, newItemInputY, newItemInputW, newItemInputH,
                8, 8);
        g.setColor(newItemInputFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(input);
        g.setColor(newItemInputFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(newItemInputFocused ? 1.6f : 1.0f));
        g.draw(input);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        if (inputName.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("e.g. Mobile Apps, Bioengineering, FinTech...", newItemInputX + 12, newItemInputY + 24);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(inputName, newItemInputX + 12, newItemInputY + 24);
        }

        // Blinking cursor if focused
        if (newItemInputFocused && (System.currentTimeMillis() % 1000 < 500)) {
            FontMetrics fm = g.getFontMetrics();
            int cursorX = newItemInputX + 12 + (inputName.isEmpty() ? 0 : fm.stringWidth(inputName));
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawLine(cursorX, newItemInputY + 9, cursorX, newItemInputY + 29);
        }

        // Helper guidance note
        curY += 56;
        g.setFont(AdminTheme.FONT_REGULAR_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("✓ New category will be active immediately for student projects and filters.", modalX + 28,
                curY + 6);
        g.drawString("✓ Press Enter or click 'Create Category' to save.", modalX + 28, curY + 22);

        // Bottom Action buttons
        int btnY = modalY + modalH - 54;

        cancelNewItemBtnW = 96;
        cancelNewItemBtnH = 36;
        cancelNewItemBtnX = modalX + 28;
        cancelNewItemBtnY = btnY;
        paintModalBtn(g, cancelNewItemBtnX, cancelNewItemBtnY, cancelNewItemBtnW, cancelNewItemBtnH, "Cancel",
                cancelNewItemHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE,
                AdminTheme.BORDER_COLOR, AdminTheme.TEXT_MUTED);

        saveNewItemBtnW = 140;
        saveNewItemBtnH = 36;
        saveNewItemBtnX = modalX + modalW - 28 - saveNewItemBtnW;
        saveNewItemBtnY = btnY;
        paintModalBtn(g, saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW, saveNewItemBtnH, "Create Category",
                saveNewItemHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK,
                AdminTheme.BRAND_GREEN_DARK, Color.WHITE);
    }

    private void paintAddSkillModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(480, viewW - 40);
        modalH = 280;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Add Skill Tag");

        int curY = modalY + 84;
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("SKILL NAME *", modalX + 28, curY);

        newItemInputX = modalX + 28;
        newItemInputY = curY + 8;
        newItemInputW = modalW - 56;
        newItemInputH = 38;

        RoundRectangle2D input = new RoundRectangle2D.Float(newItemInputX, newItemInputY, newItemInputW, newItemInputH,
                8, 8);
        g.setColor(newItemInputFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(input);
        g.setColor(newItemInputFocused ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(newItemInputFocused ? 1.6f : 1.0f));
        g.draw(input);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        if (inputName.isEmpty()) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("e.g. Python, Figma, Docker, PyTorch...", newItemInputX + 12, newItemInputY + 24);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(inputName, newItemInputX + 12, newItemInputY + 24);
        }

        // Blinking cursor if focused
        if (newItemInputFocused && (System.currentTimeMillis() % 1000 < 500)) {
            FontMetrics fm = g.getFontMetrics();
            int cursorX = newItemInputX + 12 + (inputName.isEmpty() ? 0 : fm.stringWidth(inputName));
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawLine(cursorX, newItemInputY + 9, cursorX, newItemInputY + 29);
        }

        // Helper guidance note
        curY += 56;
        g.setFont(AdminTheme.FONT_REGULAR_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("✓ Skill will be immediately available for student profiles and team matching.", modalX + 28,
                curY + 6);
        g.drawString("✓ Press Enter or click 'Create Skill' to save.", modalX + 28, curY + 22);

        // Bottom Action buttons
        int btnY = modalY + modalH - 54;

        cancelNewItemBtnW = 96;
        cancelNewItemBtnH = 36;
        cancelNewItemBtnX = modalX + 28;
        cancelNewItemBtnY = btnY;
        paintModalBtn(g, cancelNewItemBtnX, cancelNewItemBtnY, cancelNewItemBtnW, cancelNewItemBtnH, "Cancel",
                cancelNewItemHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE,
                AdminTheme.BORDER_COLOR, AdminTheme.TEXT_MUTED);

        saveNewItemBtnW = 126;
        saveNewItemBtnH = 36;
        saveNewItemBtnX = modalX + modalW - 28 - saveNewItemBtnW;
        saveNewItemBtnY = btnY;
        paintModalBtn(g, saveNewItemBtnX, saveNewItemBtnY, saveNewItemBtnW, saveNewItemBtnH, "Create Skill",
                saveNewItemHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK,
                AdminTheme.BRAND_GREEN_DARK, Color.WHITE);
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
        int roleW = "Admin".equalsIgnoreCase(selectedUser.role) ? 54
                : ("Moderator".equalsIgnoreCase(selectedUser.role) ? 72 : 60);
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
        paintDetailMiniCard(g, modalX + 24 + cardW + 14, curY, cardW, cardH, "SECURITY ACCESS",
                "✓ Verified NetID · 2FA Active");

        curY += cardH + 14;

        // Activity banner
        RoundRectangle2D actCard = new RoundRectangle2D.Float(modalX + 24, curY, modalW - 48, 40, 8, 8);
        g.setColor(new Color(0xFA, 0xFA, 0xFA));
        g.fill(actCard);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(actCard);

        g.setFont(AdminTheme.FONT_REGULAR_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Engagement: 3 Projects Active · 28 Sprint Tasks Completed · 0 Policy Flags", modalX + 38,
                curY + 25);

        // Bottom Action buttons
        int btnY = modalY + modalH - 56;
        int btnSpacing = 10;
        int curBtnX = modalX + 24;

        // Suspend / Reactivate
        userModalSuspendBtnX = curBtnX;
        userModalSuspendBtnY = btnY;
        userModalSuspendBtnW = isActive ? 130 : 140;
        RoundRectangle2D susB = new RoundRectangle2D.Float(userModalSuspendBtnX, userModalSuspendBtnY,
                userModalSuspendBtnW, userModalSuspendBtnH, 8, 8);
        g.setColor(userModalSuspendHovered ? (isActive ? new Color(0xFE, 0xE2, 0xE2) : new Color(0xDC, 0xFC, 0xE7))
                : Color.WHITE);
        g.fill(susB);
        g.setColor(isActive ? new Color(0xEF, 0x44, 0x44) : AdminTheme.BRAND_GREEN_DARK);
        g.draw(susB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.drawString(isActive ? "Suspend User" : "Reactivate User", userModalSuspendBtnX + 16,
                userModalSuspendBtnY + 22);

        curBtnX += userModalSuspendBtnW + btnSpacing;

        // Edit Role
        userModalRoleBtnX = curBtnX;
        userModalRoleBtnY = btnY;
        userModalRoleBtnW = 100;
        RoundRectangle2D rolB = new RoundRectangle2D.Float(userModalRoleBtnX, userModalRoleBtnY, userModalRoleBtnW,
                userModalRoleBtnH, 8, 8);
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
        RoundRectangle2D delB = new RoundRectangle2D.Float(userModalDeleteBtnX, userModalDeleteBtnY,
                userModalDeleteBtnW, userModalDeleteBtnH, 8, 8);
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
        RoundRectangle2D clsB = new RoundRectangle2D.Float(userModalCloseBtnX, userModalCloseBtnY, userModalCloseBtnW,
                userModalCloseBtnH, 8, 8);
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
        RoundRectangle2D nameInput = new RoundRectangle2D.Float(newUserNameInputX, newUserNameInputY, newUserNameInputW,
                newUserNameInputH, 8, 8);
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
            g.drawString(newUserName + (activeUserField == 0 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                    newUserNameInputX + 12, newUserNameInputY + 23);
        }

        curY += 66;

        // 2. Email Address
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("EMAIL ADDRESS", modalX + 28, curY);

        newUserEmailInputX = modalX + 28;
        newUserEmailInputY = curY + 8;
        newUserEmailInputW = modalW - 56;
        RoundRectangle2D emailInput = new RoundRectangle2D.Float(newUserEmailInputX, newUserEmailInputY,
                newUserEmailInputW, newUserEmailInputH, 8, 8);
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
            g.drawString(newUserEmail + (activeUserField == 1 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                    newUserEmailInputX + 12, newUserEmailInputY + 23);
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

        paintRoleOption(g, newUserMemberBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Member",
                "Member".equalsIgnoreCase(newUserRole));
        paintRoleOption(g, newUserModBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Moderator",
                "Moderator".equalsIgnoreCase(newUserRole));
        paintRoleOption(g, newUserAdminBtnX, newUserRoleY, newUserRoleW, newUserRoleH, "Admin",
                "Admin".equalsIgnoreCase(newUserRole));

        // Bottom buttons
        int btnY = modalY + modalH - 56;

        // Cancel button
        cancelNewUserBtnX = modalX + 28;
        cancelNewUserBtnY = btnY;
        RoundRectangle2D cBtn = new RoundRectangle2D.Float(cancelNewUserBtnX, cancelNewUserBtnY, cancelNewUserBtnW,
                cancelNewUserBtnH, 8, 8);
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
        RoundRectangle2D sBtn = new RoundRectangle2D.Float(saveNewUserBtnX, saveNewUserBtnY, saveNewUserBtnW,
                saveNewUserBtnH, 8, 8);
        g.setColor(saveNewUserHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(sBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Create Member", saveNewUserBtnX + 18, saveNewUserBtnY + 22);
    }

    private void paintProjectDetailsModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(560, viewW - 40);
        modalH = 460;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Project Details & Moderation");

        int curY = modalY + 76;

        // Project Title
        g.setFont(AdminTheme.FONT_BOLD_16);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(selectedProject.name, modalX + 28, curY + 20);

        int titleW = g.getFontMetrics().stringWidth(selectedProject.name);

        // Category pill
        int catPillX = modalX + 28 + titleW + 12;
        int catPillW = g.getFontMetrics(AdminTheme.FONT_BOLD_10).stringWidth(selectedProject.category) + 16;
        RoundRectangle2D cPill = new RoundRectangle2D.Float(catPillX, curY + 6, catPillW, 20, 10, 10);
        g.setColor(AdminTheme.BRAND_TINT);
        g.fill(cPill);
        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.drawString(selectedProject.category, catPillX + 8, curY + 19);

        // Status pill on right
        boolean isUnderRev = "Under review".equalsIgnoreCase(selectedProject.status);
        boolean isActive = "Active".equalsIgnoreCase(selectedProject.status);
        boolean isRecruiting = "Recruiting".equalsIgnoreCase(selectedProject.status);
        int statusW = 86;
        int statusX = modalX + modalW - 28 - statusW;
        RoundRectangle2D sPill = new RoundRectangle2D.Float(statusX, curY + 6, statusW, 22, 11, 11);
        if (isActive) {
            g.setColor(new Color(0xE6, 0xF4, 0xEA));
            g.fill(sPill);
            g.setColor(new Color(0x06, 0x5F, 0x46));
        } else if (isRecruiting) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(sPill);
            g.setColor(new Color(0x16, 0x65, 0x34));
        } else if (isUnderRev) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(sPill);
            g.setColor(new Color(0xB4, 0x53, 0x09));
        } else {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(sPill);
            g.setColor(AdminTheme.TEXT_MUTED);
        }
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics sfm = g.getFontMetrics();
        g.drawString(selectedProject.status, statusX + (statusW - sfm.stringWidth(selectedProject.status)) / 2,
                curY + 20);

        curY += 46;

        // 4 Details Grid Cards (2x2)
        int cardW = (modalW - 56 - 14) / 2;
        int cardH = 54;

        paintDetailMiniCard(g, modalX + 28, curY, cardW, cardH, "PROJECT ID", selectedProject.id);
        paintDetailMiniCard(g, modalX + 28 + cardW + 14, curY, cardW, cardH, "CREATED DATE", selectedProject.created);

        curY += cardH + 10;

        paintDetailMiniCard(g, modalX + 28, curY, cardW, cardH, "PROJECT OWNER", selectedProject.owner);
        paintDetailMiniCard(g, modalX + 28 + cardW + 14, curY, cardW, cardH, "TEAM FILL RATIO",
                selectedProject.teamSize + " members");

        curY += cardH + 12;

        // Scope / Description card
        RoundRectangle2D descCard = new RoundRectangle2D.Float(modalX + 28, curY, modalW - 56, 72, 8, 8);
        g.setColor(new Color(0xFA, 0xFA, 0xFA));
        g.fill(descCard);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(descCard);

        g.setFont(AdminTheme.FONT_BOLD_10);
        g.setColor(AdminTheme.TEXT_LIGHT);
        g.drawString("PROJECT SCOPE & OBJECTIVES", modalX + 40, curY + 20);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        String desc = (selectedProject.description == null || selectedProject.description.isEmpty())
                ? "No description provided for this project."
                : selectedProject.description;
        if (desc.length() > 75)
            desc = desc.substring(0, 72) + "...";
        g.drawString(desc, modalX + 40, curY + 44);

        // Bottom Action buttons
        int btnY = modalY + modalH - 56;
        int btnSpacing = 10;
        int curBtnX = modalX + 28;

        if (isUnderRev) {
            // Approve & Activate button
            projModalApproveBtnX = curBtnX;
            projModalApproveBtnY = btnY;
            projModalApproveBtnW = 148;
            RoundRectangle2D appB = new RoundRectangle2D.Float(projModalApproveBtnX, projModalApproveBtnY,
                    projModalApproveBtnW, projModalApproveBtnH, 8, 8);
            g.setColor(projModalApproveHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
            g.fill(appB);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(Color.WHITE);
            g.drawString("✓ Approve & Activate", projModalApproveBtnX + 16, projModalApproveBtnY + 22);

            curBtnX += projModalApproveBtnW + btnSpacing;
            projModalStatusBtnX = -100;
        } else {
            // Toggle Status Button
            projModalStatusBtnX = curBtnX;
            projModalStatusBtnY = btnY;
            projModalStatusBtnW = 136;
            RoundRectangle2D stB = new RoundRectangle2D.Float(projModalStatusBtnX, projModalStatusBtnY,
                    projModalStatusBtnW, projModalStatusBtnH, 8, 8);
            g.setColor(projModalStatusHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
            g.fill(stB);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.draw(stB);
            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString("↻ Change Status", projModalStatusBtnX + 18, projModalStatusBtnY + 22);

            curBtnX += projModalStatusBtnW + btnSpacing;
            projModalApproveBtnX = -100;
        }

        // Delete Project
        projModalDeleteBtnX = curBtnX;
        projModalDeleteBtnY = btnY;
        projModalDeleteBtnW = 106;
        RoundRectangle2D delB = new RoundRectangle2D.Float(projModalDeleteBtnX, projModalDeleteBtnY,
                projModalDeleteBtnW, projModalDeleteBtnH, 8, 8);
        g.setColor(projModalDeleteHovered ? new Color(0xFE, 0xF2, 0xF2) : Color.WHITE);
        g.fill(delB);
        g.setColor(new Color(0xFC, 0xA5, 0xA5));
        g.draw(delB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(new Color(0xDC, 0x26, 0x26));
        g.drawString("Delete Project", projModalDeleteBtnX + 14, projModalDeleteBtnY + 22);

        // Close on far right
        projModalCloseBtnX = modalX + modalW - 28 - projModalCloseBtnW;
        projModalCloseBtnY = btnY;
        RoundRectangle2D clsB = new RoundRectangle2D.Float(projModalCloseBtnX, projModalCloseBtnY, projModalCloseBtnW,
                projModalCloseBtnH, 8, 8);
        g.setColor(projModalCloseHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(clsB);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Close", projModalCloseBtnX + 26, projModalCloseBtnY + 22);
    }

    private void paintAddProjectModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(540, viewW - 40);
        modalH = 510;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Create New Platform Project");

        int curY = modalY + 76;

        // 1. Project Name
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("PROJECT NAME", modalX + 28, curY);

        newProjectNameInputX = modalX + 28;
        newProjectNameInputY = curY + 6;
        newProjectNameInputW = modalW - 56;
        RoundRectangle2D nameInput = new RoundRectangle2D.Float(newProjectNameInputX, newProjectNameInputY,
                newProjectNameInputW, newProjectNameInputH, 8, 8);
        g.setColor(activeProjectField == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(nameInput);
        g.setColor(activeProjectField == 0 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(nameInput);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        if (newProjectName.isEmpty() && activeProjectField != 0) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("e.g. Autonomous Campus Delivery Rover", newProjectNameInputX + 12, newProjectNameInputY + 22);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(
                    newProjectName + (activeProjectField == 0 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                    newProjectNameInputX + 12, newProjectNameInputY + 22);
        }

        curY += 56;

        // 2. Category selection pills
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("CATEGORY", modalX + 28, curY);

        curY += 6;
        addProjCatBoxes.clear();
        java.util.List<AdminDataStore.CategoryModel> cats = AdminDataStore.getInstance().getAllCategories();
        int catX = modalX + 28;
        int catPillH = 24;
        for (AdminDataStore.CategoryModel c : cats) {
            g.setFont(AdminTheme.FONT_BOLD_10);
            FontMetrics cfm = g.getFontMetrics();
            int cpW = cfm.stringWidth(c.name) + 18;
            if (catX + cpW > modalX + modalW - 28)
                break; // stay within modal

            CatChoiceBox cb = new CatChoiceBox();
            cb.name = c.name;
            cb.x = catX;
            cb.y = curY;
            cb.w = cpW;
            cb.h = catPillH;
            addProjCatBoxes.add(cb);

            boolean isSel = c.name.equalsIgnoreCase(newProjectCategory);
            RoundRectangle2D cp = new RoundRectangle2D.Float(catX, curY, cpW, catPillH, 8, 8);
            if (isSel) {
                g.setColor(AdminTheme.BRAND_GREEN_DARK);
                g.fill(cp);
                g.setColor(Color.WHITE);
            } else {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fill(cp);
                g.setColor(AdminTheme.BORDER_COLOR);
                g.draw(cp);
                g.setColor(AdminTheme.TEXT_MUTED);
            }
            g.drawString(c.name, catX + 9, curY + 16);
            catX += cpW + 8;
        }

        curY += 38;

        // 3. Owner & Team Size (2 columns)
        int colW = (modalW - 56 - 14) / 2;

        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("PROJECT OWNER", modalX + 28, curY);
        g.drawString("TEAM SIZE", modalX + 28 + colW + 14, curY);

        curY += 6;
        newProjectOwnerInputX = modalX + 28;
        newProjectOwnerInputY = curY;
        newProjectOwnerInputW = colW;
        RoundRectangle2D ownBox = new RoundRectangle2D.Float(newProjectOwnerInputX, newProjectOwnerInputY,
                newProjectOwnerInputW, newProjectOwnerInputH, 8, 8);
        g.setColor(activeProjectField == 1 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(ownBox);
        g.setColor(activeProjectField == 1 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(ownBox);

        g.setFont(AdminTheme.FONT_REGULAR_13);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(
                newProjectOwner + (activeProjectField == 1 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                newProjectOwnerInputX + 12, newProjectOwnerInputY + 22);

        newProjectSizeInputX = modalX + 28 + colW + 14;
        newProjectSizeInputY = curY;
        newProjectSizeInputW = colW;
        RoundRectangle2D sizeBox = new RoundRectangle2D.Float(newProjectSizeInputX, newProjectSizeInputY,
                newProjectSizeInputW, newProjectSizeInputH, 8, 8);
        g.setColor(activeProjectField == 2 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(sizeBox);
        g.setColor(activeProjectField == 2 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(sizeBox);

        g.drawString(
                newProjectTeamSize + (activeProjectField == 2 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                newProjectSizeInputX + 12, newProjectSizeInputY + 22);

        curY += 52;

        // 4. Initial Status pills
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("STATUS", modalX + 28, curY);

        curY += 6;
        addProjStatusY = curY;
        addProjStatusActiveX = modalX + 28;
        addProjStatusRecruitingX = addProjStatusActiveX + addProjStatusW + 8;
        addProjStatusReviewX = addProjStatusRecruitingX + addProjStatusW + 8;

        paintStatusChoice(g, addProjStatusActiveX, addProjStatusY, addProjStatusW, addProjStatusH, "Active",
                "Active".equalsIgnoreCase(newProjectStatus));
        paintStatusChoice(g, addProjStatusRecruitingX, addProjStatusY, addProjStatusW, addProjStatusH, "Recruiting",
                "Recruiting".equalsIgnoreCase(newProjectStatus));
        paintStatusChoice(g, addProjStatusReviewX, addProjStatusY, addProjStatusW, addProjStatusH, "Under review",
                "Under review".equalsIgnoreCase(newProjectStatus));

        curY += 46;

        // 5. Description
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("SHORT DESCRIPTION", modalX + 28, curY);

        curY += 6;
        newProjectDescInputX = modalX + 28;
        newProjectDescInputY = curY;
        newProjectDescInputW = modalW - 56;
        RoundRectangle2D descInput = new RoundRectangle2D.Float(newProjectDescInputX, newProjectDescInputY,
                newProjectDescInputW, newProjectDescInputH, 8, 8);
        g.setColor(activeProjectField == 3 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(descInput);
        g.setColor(activeProjectField == 3 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.draw(descInput);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        if (newProjectDesc.isEmpty() && activeProjectField != 3) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Summary of project goals, mission, and required qualifications...", newProjectDescInputX + 12,
                    newProjectDescInputY + 26);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(
                    newProjectDesc + (activeProjectField == 3 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                    newProjectDescInputX + 12, newProjectDescInputY + 26);
        }

        // Bottom buttons
        int btnY = modalY + modalH - 56;

        cancelNewProjectBtnX = modalX + 28;
        cancelNewProjectBtnY = btnY;
        RoundRectangle2D cBtn = new RoundRectangle2D.Float(cancelNewProjectBtnX, cancelNewProjectBtnY,
                cancelNewProjectBtnW, cancelNewProjectBtnH, 8, 8);
        g.setColor(cancelNewProjectHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cBtn);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.draw(cBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("Cancel", cancelNewProjectBtnX + 24, cancelNewProjectBtnY + 22);

        saveNewProjectBtnX = modalX + modalW - 28 - saveNewProjectBtnW;
        saveNewProjectBtnY = btnY;
        RoundRectangle2D svBtn = new RoundRectangle2D.Float(saveNewProjectBtnX, saveNewProjectBtnY, saveNewProjectBtnW,
                saveNewProjectBtnH, 8, 8);
        g.setColor(saveNewProjectHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK);
        g.fill(svBtn);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(Color.WHITE);
        g.drawString("Create Project", saveNewProjectBtnX + 22, saveNewProjectBtnY + 22);
    }

    private void paintReviewDetailsModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(560, viewW - 40);
        boolean isReported = "Reported".equalsIgnoreCase(selectedReview.status)
                || "Under review".equalsIgnoreCase(selectedReview.status);
        modalH = isReported ? 450 : 390;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "Feedback Moderation & Details");

        int curY = modalY + 76;

        // Top info row: ID, Star rating, Status Pill
        g.setFont(AdminTheme.FONT_BOLD_15);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(selectedReview.id, modalX + 28, curY + 16);

        // Stars
        int starX = modalX + 28 + g.getFontMetrics().stringWidth(selectedReview.id) + 14;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        for (int i = 1; i <= 5; i++) {
            g.setColor(i <= selectedReview.rating ? AdminTheme.BRAND_AMBER : new Color(0xE2, 0xE8, 0xF0));
            g.drawString("★", starX + (i - 1) * 16, curY + 16);
        }

        // Status Pill on top right
        int sW = 86;
        int sX = modalX + modalW - 28 - sW;
        RoundRectangle2D sp = new RoundRectangle2D.Float(sX, curY, sW, 22, 11, 11);
        if ("Published".equalsIgnoreCase(selectedReview.status)) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.drawString("✓ Published", sX + 12, curY + 15);
        } else if ("Reported".equalsIgnoreCase(selectedReview.status)) {
            g.setColor(new Color(0xFE, 0xE2, 0xE2));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.ACCENT_RED);
            g.drawString("⚠️ Reported", sX + 12, curY + 15);
        } else if ("Under review".equalsIgnoreCase(selectedReview.status)) {
            g.setColor(new Color(0xFE, 0xF3, 0xC7));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(new Color(0xB4, 0x53, 0x09));
            g.drawString("⏳ Under review", sX + 8, curY + 15);
        } else {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(sp);
            g.setFont(AdminTheme.FONT_BOLD_10);
            g.setColor(AdminTheme.TEXT_MUTED);
            g.drawString("👁️ Hidden", sX + 17, curY + 15);
        }

        curY += 36;

        // Two Mini Cards: Reviewer & Reviewee
        int miniCardW = (modalW - 56 - 14) / 2;
        int miniCardH = 56;

        paintDetailMiniCard(g, modalX + 28, curY, miniCardW, miniCardH, "REVIEWER", selectedReview.reviewerName + " ("
                + (selectedReview.roleOrContext != null ? selectedReview.roleOrContext : "Member") + ")");
        paintDetailMiniCard(g, modalX + 28 + miniCardW + 14, curY, miniCardW, miniCardH, "TARGET & PROJECT",
                selectedReview.revieweeName + " · " + selectedReview.projectName);

        curY += miniCardH + 14;

        // Review Content Blockquote Box
        int quoteH = 68;
        RoundRectangle2D qBox = new RoundRectangle2D.Float(modalX + 28, curY, modalW - 56, quoteH, 8, 8);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(qBox);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(qBox);

        // Left accent bar
        g.setColor(AdminTheme.BRAND_PRIMARY);
        g.fillRect(modalX + 28, curY, 4, quoteH);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        String txt = (selectedReview.reviewText != null) ? selectedReview.reviewText : "";
        if (txt.length() > 160)
            txt = txt.substring(0, 157) + "…";
        if (txt.length() > 80) {
            int breakIdx = txt.lastIndexOf(' ', 80);
            if (breakIdx == -1)
                breakIdx = 80;
            g.drawString("\"" + txt.substring(0, breakIdx), modalX + 42, curY + 26);
            g.drawString(txt.substring(breakIdx).trim() + "\"", modalX + 42, curY + 48);
        } else {
            g.drawString("\"" + txt + "\"", modalX + 42, curY + 38);
        }

        curY += quoteH + 14;

        // If Reported: Moderation Alert Banner
        if (isReported) {
            int alertH = 46;
            RoundRectangle2D repBox = new RoundRectangle2D.Float(modalX + 28, curY, modalW - 56, alertH, 8, 8);
            g.setColor(new Color(0xFF, 0xF1, 0xF2));
            g.fill(repBox);
            g.setColor(new Color(0xFE, 0xCD, 0xD3));
            g.setStroke(new BasicStroke(1.0f));
            g.draw(repBox);

            g.setFont(AdminTheme.FONT_BOLD_11);
            g.setColor(AdminTheme.ACCENT_RED);
            g.drawString("⚠️ Flagged for moderation review:", modalX + 38, curY + 18);

            g.setFont(AdminTheme.FONT_REG_11);
            g.setColor(new Color(0x9F, 0x12, 0x39));
            String repTxt = (selectedReview.reportReason != null ? selectedReview.reportReason
                    : "Inappropriate content") +
                    (selectedReview.reportedBy != null ? " · Flagged by " + selectedReview.reportedBy : "");
            g.drawString(repTxt, modalX + 38, curY + 34);

            curY += alertH + 14;
        }

        // Helpful meta info
        g.setFont(AdminTheme.FONT_REG_11);
        g.setColor(AdminTheme.TEXT_MUTED);
        String meta = "Submitted: " + selectedReview.date + " · Helpful score: 👍 " + selectedReview.helpfulVotes;
        g.drawString(meta, modalX + 28, curY + 12);

        // Bottom Action Buttons
        int btnY = modalY + modalH - 52;
        int rightX = modalX + modalW - 28;

        // "Close" Button (White outline)
        revModalCloseBtnW = 76;
        revModalCloseBtnH = 34;
        revModalCloseBtnX = rightX - revModalCloseBtnW;
        revModalCloseBtnY = btnY;
        paintModalBtn(g, revModalCloseBtnX, revModalCloseBtnY, revModalCloseBtnW, revModalCloseBtnH, "Close",
                revModalCloseHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE,
                AdminTheme.BORDER_COLOR, AdminTheme.TEXT_MUTED);

        rightX = revModalCloseBtnX - 10;

        // "Hide / Unhide" Button
        String hideLbl = "Hidden".equalsIgnoreCase(selectedReview.status) ? "Unhide" : "Hide";
        revModalHideBtnW = 76;
        revModalHideBtnH = 34;
        revModalHideBtnX = rightX - revModalHideBtnW;
        revModalHideBtnY = btnY;
        paintModalBtn(g, revModalHideBtnX, revModalHideBtnY, revModalHideBtnW, revModalHideBtnH, hideLbl,
                revModalHideHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE,
                AdminTheme.BORDER_COLOR, AdminTheme.TEXT_MUTED);

        rightX = revModalHideBtnX - 10;

        // If reported: "Approve & Keep" Button
        if (isReported) {
            revModalApproveBtnW = 120;
            revModalApproveBtnH = 34;
            revModalApproveBtnX = rightX - revModalApproveBtnW;
            revModalApproveBtnY = btnY;
            paintModalBtn(g, revModalApproveBtnX, revModalApproveBtnY, revModalApproveBtnW, revModalApproveBtnH,
                    "✓ Approve",
                    revModalApproveHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK,
                    AdminTheme.BRAND_GREEN_DARK, Color.WHITE);

            rightX = revModalApproveBtnX - 10;
        } else {
            revModalApproveBtnW = 0;
        }

        // "Delete Inappropriate Feedback" (Crimson Button)
        revModalDeleteBtnW = 148;
        revModalDeleteBtnH = 34;
        revModalDeleteBtnX = rightX - revModalDeleteBtnW;
        revModalDeleteBtnY = btnY;
        paintModalBtn(g, revModalDeleteBtnX, revModalDeleteBtnY, revModalDeleteBtnW, revModalDeleteBtnH,
                "🗑 Delete feedback",
                revModalDeleteHovered ? new Color(0xFE, 0xE2, 0xE2) : Color.WHITE,
                AdminTheme.ACCENT_RED, AdminTheme.ACCENT_RED);
    }

    private void paintAddReviewModal(Graphics2D g, int viewW, int viewH) {
        modalW = Math.min(520, viewW - 40);
        modalH = 490;
        modalX = (viewW - modalW) / 2;
        modalY = (viewH - modalH) / 2;

        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 16, 16);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        paintModalHeader(g, "+ Add Peer Review & Feedback");

        int curY = modalY + 76;

        // 1. Reviewer & Reviewee
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("REVIEWER NAME", modalX + 28, curY);
        g.drawString("REVIEWEE / TARGET", modalX + 28 + (modalW - 56) / 2 + 8, curY);

        curY += 6;
        int inputHalfW = (modalW - 56 - 16) / 2;

        newRevReviewerInputX = modalX + 28;
        newRevReviewerInputY = curY;
        newRevReviewerInputW = inputHalfW;

        RoundRectangle2D rvwBox = new RoundRectangle2D.Float(newRevReviewerInputX, newRevReviewerInputY,
                newRevReviewerInputW, newRevReviewerInputH, 8, 8);
        g.setColor(activeReviewField == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(rvwBox);
        g.setColor(activeReviewField == 0 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(activeReviewField == 0 ? 1.6f : 1.0f));
        g.draw(rvwBox);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString(newRevReviewer + (activeReviewField == 0 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                newRevReviewerInputX + 12, newRevReviewerInputY + 22);

        newRevRevieweeInputX = modalX + 28 + inputHalfW + 16;
        newRevRevieweeInputY = curY;
        newRevRevieweeInputW = inputHalfW;

        RoundRectangle2D rveBox = new RoundRectangle2D.Float(newRevRevieweeInputX, newRevRevieweeInputY,
                newRevRevieweeInputW, newRevRevieweeInputH, 8, 8);
        g.setColor(activeReviewField == 1 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(rveBox);
        g.setColor(activeReviewField == 1 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(activeReviewField == 1 ? 1.6f : 1.0f));
        g.draw(rveBox);

        g.drawString(newRevReviewee + (activeReviewField == 1 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                newRevRevieweeInputX + 12, newRevRevieweeInputY + 22);

        curY += 46;

        // 2. Project Name
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("PROJECT CONTEXT", modalX + 28, curY);

        curY += 6;
        newRevProjectInputX = modalX + 28;
        newRevProjectInputY = curY;
        newRevProjectInputW = modalW - 56;

        RoundRectangle2D pBox = new RoundRectangle2D.Float(newRevProjectInputX, newRevProjectInputY,
                newRevProjectInputW, newRevProjectInputH, 8, 8);
        g.setColor(activeReviewField == 2 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(pBox);
        g.setColor(activeReviewField == 2 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(activeReviewField == 2 ? 1.6f : 1.0f));
        g.draw(pBox);

        g.drawString(newRevProject + (activeReviewField == 2 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                newRevProjectInputX + 12, newRevProjectInputY + 22);

        curY += 46;

        // 3. Star Rating (Interactive 1-5 Stars)
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("STAR RATING", modalX + 28, curY);

        curY += 6;
        newRevStarX = modalX + 28;
        newRevStarY = curY;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        for (int i = 1; i <= 5; i++) {
            g.setColor(i <= newRevRating ? AdminTheme.BRAND_AMBER : new Color(0xCB, 0xD5, 0xE1));
            g.drawString("★", newRevStarX + (i - 1) * 32, curY + 20);
        }
        g.setFont(AdminTheme.FONT_BOLD_12);
        g.setColor(AdminTheme.TEXT_MUTED);
        g.drawString("(" + newRevRating + " out of 5 stars)", newRevStarX + 5 * 32 + 10, curY + 18);

        curY += 36;

        // 4. Review Type Pills
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("REVIEW TYPE", modalX + 28, curY);

        curY += 6;
        newRevTypeY = curY;
        newRevTypeW = 140;
        newRevTypeH = 28;
        newRevTypeTeamX = modalX + 28;
        newRevTypeUserX = newRevTypeTeamX + newRevTypeW + 8;
        newRevTypeCollabX = newRevTypeUserX + newRevTypeW + 8;

        paintStatusChoice(g, newRevTypeTeamX, newRevTypeY, newRevTypeW, newRevTypeH, "Team Member Review",
                "Team Member Review".equalsIgnoreCase(newRevType));
        paintStatusChoice(g, newRevTypeUserX, newRevTypeY, newRevTypeW, newRevTypeH, "User Rating",
                "User Rating".equalsIgnoreCase(newRevType));
        paintStatusChoice(g, newRevTypeCollabX, newRevTypeY, newRevTypeW, newRevTypeH, "Peer Collaboration",
                "Peer Collaboration".equalsIgnoreCase(newRevType));

        curY += 42;

        // 5. Review Feedback Text
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(AdminTheme.TEXT_MAIN);
        g.drawString("FEEDBACK COMMENTS", modalX + 28, curY);

        curY += 6;
        newRevTextInputX = modalX + 28;
        newRevTextInputY = curY;
        newRevTextInputW = modalW - 56;

        RoundRectangle2D tBox = new RoundRectangle2D.Float(newRevTextInputX, newRevTextInputY, newRevTextInputW,
                newRevTextInputH, 8, 8);
        g.setColor(activeReviewField == 4 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(tBox);
        g.setColor(activeReviewField == 4 ? AdminTheme.BRAND_GREEN_DARK : AdminTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(activeReviewField == 4 ? 1.6f : 1.0f));
        g.draw(tBox);

        g.setFont(AdminTheme.FONT_REGULAR_12);
        if (newRevText.isEmpty() && activeReviewField != 4) {
            g.setColor(AdminTheme.TEXT_LIGHT);
            g.drawString("Enter constructive feedback or notes regarding peer collaboration...", newRevTextInputX + 12,
                    newRevTextInputY + 28);
        } else {
            g.setColor(AdminTheme.TEXT_MAIN);
            g.drawString(newRevText + (activeReviewField == 4 && (System.currentTimeMillis() % 1000 < 500) ? "|" : ""),
                    newRevTextInputX + 12, newRevTextInputY + 28);
        }

        // Bottom Action buttons
        int btnY = modalY + modalH - 52;
        cancelNewRevBtnX = modalX + 28;
        cancelNewRevBtnY = btnY;
        paintModalBtn(g, cancelNewRevBtnX, cancelNewRevBtnY, cancelNewRevBtnW, cancelNewRevBtnH, "Cancel",
                cancelNewRevHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE,
                AdminTheme.BORDER_COLOR, AdminTheme.TEXT_MUTED);

        saveNewRevBtnX = modalX + modalW - 28 - saveNewRevBtnW;
        saveNewRevBtnY = btnY;
        paintModalBtn(g, saveNewRevBtnX, saveNewRevBtnY, saveNewRevBtnW, saveNewRevBtnH, "Post Review",
                saveNewRevHovered ? AdminTheme.BRAND_GREEN_MID : AdminTheme.BRAND_GREEN_DARK,
                AdminTheme.BRAND_GREEN_DARK, Color.WHITE);
    }

    private void paintModalBtn(Graphics2D g, int bx, int by, int bw, int bh, String text, Color bg, Color border,
            Color fg) {
        RoundRectangle2D b = new RoundRectangle2D.Float(bx, by, bw, bh, 8, 8);
        g.setColor(bg);
        g.fill(b);
        g.setColor(border);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(b);
        g.setFont(AdminTheme.FONT_BOLD_11);
        g.setColor(fg);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + (bh - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void paintStatusChoice(Graphics2D g, int bx, int by, int bw, int bh, String label, boolean isSel) {
        RoundRectangle2D b = new RoundRectangle2D.Float(bx, by, bw, bh, 8, 8);
        if (isSel) {
            g.setColor(AdminTheme.BRAND_GREEN_DARK);
            g.fill(b);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(b);
            g.setColor(AdminTheme.BORDER_COLOR);
            g.draw(b);
            g.setColor(AdminTheme.TEXT_MUTED);
        }
        g.setFont(AdminTheme.FONT_BOLD_10);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, bx + (bw - fm.stringWidth(label)) / 2, by + 18);
    }

    public boolean onClick(int mx, int my, AdminDashboard dashboard) {
        if (activeModal == ActiveModal.NONE)
            return false;

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
            if (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY
                    && my <= dismissBtnY + dismissBtnH) {
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
            if (mx >= suspendBtnX && mx <= suspendBtnX + suspendBtnW && my >= suspendBtnY
                    && my <= suspendBtnY + suspendBtnH) {
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
            if (mx >= roleMemberBtnX && mx <= roleMemberBtnX + roleChoiceW && my >= roleChoiceY
                    && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Member";
                return true;
            }
            if (mx >= roleModBtnX && mx <= roleModBtnX + roleChoiceW && my >= roleChoiceY
                    && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Moderator";
                return true;
            }
            if (mx >= roleAdminBtnX && mx <= roleAdminBtnX + roleChoiceW && my >= roleChoiceY
                    && my <= roleChoiceY + roleChoiceH) {
                selectedRole = "Admin";
                return true;
            }
            if (mx >= saveRoleBtnX && mx <= saveRoleBtnX + saveRoleBtnW && my >= saveRoleBtnY
                    && my <= saveRoleBtnY + saveRoleBtnH) {
                AdminDataStore.getInstance().updateUserRole(editRoleUser, selectedRole);
                dashboard.showToast("Role updated to " + selectedRole + " for " + editRoleUser);
                closeModal();
                return true;
            }
        }

        // Add Category / Skill
        if (activeModal == ActiveModal.ADD_CATEGORY || activeModal == ActiveModal.ADD_SKILL) {
            // Click input box focus
            if (mx >= newItemInputX && mx <= newItemInputX + newItemInputW && my >= newItemInputY
                    && my <= newItemInputY + newItemInputH) {
                newItemInputFocused = true;
                return true;
            }

            // Cancel button
            if (mx >= cancelNewItemBtnX && mx <= cancelNewItemBtnX + cancelNewItemBtnW && my >= cancelNewItemBtnY
                    && my <= cancelNewItemBtnY + cancelNewItemBtnH) {
                closeModal();
                return true;
            }

            if (activeModal == ActiveModal.ADD_CATEGORY) {
                if (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY
                        && my <= saveNewItemBtnY + saveNewItemBtnH) {
                    String catName = inputName.trim();
                    if (catName.isEmpty()) {
                        dashboard.showToast("Please enter a category name");
                        return true;
                    }
                    boolean added = AdminDataStore.getInstance().addCategory(catName);
                    if (added) {
                        dashboard.showToast("Category '" + catName + "' created successfully");
                        closeModal();
                    } else {
                        dashboard.showToast("Category '" + catName + "' already exists!");
                    }
                    return true;
                }
            }
            if (activeModal == ActiveModal.ADD_SKILL) {
                if (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY
                        && my <= saveNewItemBtnY + saveNewItemBtnH) {
                    String skillName = inputName.trim();
                    if (skillName.isEmpty()) {
                        dashboard.showToast("Please enter a skill name");
                        return true;
                    }
                    boolean added = AdminDataStore.getInstance().addSkill(skillName);
                    if (added) {
                        dashboard.showToast("Skill tag '" + skillName + "' added successfully");
                        closeModal();
                    } else {
                        dashboard.showToast("Skill tag '" + skillName + "' already exists!");
                    }
                    return true;
                }
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

        // Project Details Modal Actions
        if (activeModal == ActiveModal.PROJECT_DETAILS && selectedProject != null) {
            // Approve & Activate
            if ("Under review".equalsIgnoreCase(selectedProject.status) &&
                    mx >= projModalApproveBtnX && mx <= projModalApproveBtnX + projModalApproveBtnW &&
                    my >= projModalApproveBtnY && my <= projModalApproveBtnY + projModalApproveBtnH) {
                AdminDataStore.getInstance().updateProjectStatus(selectedProject.name, "Active");
                dashboard.showToast("Project approved and activated: " + selectedProject.name);
                selectedProject = AdminDataStore.getInstance().getProjectByName(selectedProject.name);
                return true;
            }
            // Toggle / Change Status
            if (!"Under review".equalsIgnoreCase(selectedProject.status) &&
                    mx >= projModalStatusBtnX && mx <= projModalStatusBtnX + projModalStatusBtnW &&
                    my >= projModalStatusBtnY && my <= projModalStatusBtnY + projModalStatusBtnH) {
                String curSt = selectedProject.status;
                String nextSt = "Active".equalsIgnoreCase(curSt) ? "Recruiting"
                        : ("Recruiting".equalsIgnoreCase(curSt) ? "Completed"
                                : ("Completed".equalsIgnoreCase(curSt) ? "Under review" : "Active"));
                AdminDataStore.getInstance().updateProjectStatus(selectedProject.name, nextSt);
                dashboard.showToast("Status updated to " + nextSt + " for " + selectedProject.name);
                selectedProject = AdminDataStore.getInstance().getProjectByName(selectedProject.name);
                return true;
            }
            // Delete Project
            if (mx >= projModalDeleteBtnX && mx <= projModalDeleteBtnX + projModalDeleteBtnW &&
                    my >= projModalDeleteBtnY && my <= projModalDeleteBtnY + projModalDeleteBtnH) {
                String dName = selectedProject.name;
                AdminDataStore.getInstance().deleteProject(dName);
                dashboard.showToast("Deleted project: " + dName);
                closeModal();
                return true;
            }
            // Close
            if (mx >= projModalCloseBtnX && mx <= projModalCloseBtnX + projModalCloseBtnW &&
                    my >= projModalCloseBtnY && my <= projModalCloseBtnY + projModalCloseBtnH) {
                closeModal();
                return true;
            }
        }

        // Add Project Modal Actions
        if (activeModal == ActiveModal.ADD_PROJECT) {
            // Field 0: Name
            if (mx >= newProjectNameInputX && mx <= newProjectNameInputX + newProjectNameInputW &&
                    my >= newProjectNameInputY && my <= newProjectNameInputY + newProjectNameInputH) {
                activeProjectField = 0;
                return true;
            }
            // Field 1: Owner
            if (mx >= newProjectOwnerInputX && mx <= newProjectOwnerInputX + newProjectOwnerInputW &&
                    my >= newProjectOwnerInputY && my <= newProjectOwnerInputY + newProjectOwnerInputH) {
                activeProjectField = 1;
                return true;
            }
            // Field 2: Team Size
            if (mx >= newProjectSizeInputX && mx <= newProjectSizeInputX + newProjectSizeInputW &&
                    my >= newProjectSizeInputY && my <= newProjectSizeInputY + newProjectSizeInputH) {
                activeProjectField = 2;
                return true;
            }
            // Field 3: Description
            if (mx >= newProjectDescInputX && mx <= newProjectDescInputX + newProjectDescInputW &&
                    my >= newProjectDescInputY && my <= newProjectDescInputY + newProjectDescInputH) {
                activeProjectField = 3;
                return true;
            }
            // Category pills
            for (CatChoiceBox cb : addProjCatBoxes) {
                if (mx >= cb.x && mx <= cb.x + cb.w && my >= cb.y && my <= cb.y + cb.h) {
                    newProjectCategory = cb.name;
                    return true;
                }
            }
            // Status pills
            if (mx >= addProjStatusActiveX && mx <= addProjStatusActiveX + addProjStatusW &&
                    my >= addProjStatusY && my <= addProjStatusY + addProjStatusH) {
                newProjectStatus = "Active";
                return true;
            }
            if (mx >= addProjStatusRecruitingX && mx <= addProjStatusRecruitingX + addProjStatusW &&
                    my >= addProjStatusY && my <= addProjStatusY + addProjStatusH) {
                newProjectStatus = "Recruiting";
                return true;
            }
            if (mx >= addProjStatusReviewX && mx <= addProjStatusReviewX + addProjStatusW &&
                    my >= addProjStatusY && my <= addProjStatusY + addProjStatusH) {
                newProjectStatus = "Under review";
                return true;
            }
            // Cancel
            if (mx >= cancelNewProjectBtnX && mx <= cancelNewProjectBtnX + cancelNewProjectBtnW &&
                    my >= cancelNewProjectBtnY && my <= cancelNewProjectBtnY + cancelNewProjectBtnH) {
                closeModal();
                return true;
            }
            // Create Project
            if (mx >= saveNewProjectBtnX && mx <= saveNewProjectBtnX + saveNewProjectBtnW &&
                    my >= saveNewProjectBtnY && my <= saveNewProjectBtnY + saveNewProjectBtnH) {
                if (newProjectName.trim().isEmpty()) {
                    dashboard.showToast("Please enter a project name");
                    return true;
                }
                AdminDataStore.getInstance().addProject(newProjectName, newProjectCategory, newProjectOwner,
                        newProjectTeamSize, newProjectStatus, newProjectDesc);
                dashboard.showToast("Project '" + newProjectName + "' created successfully");
                closeModal();
                return true;
            }
        }

        // Review Details Modal Actions
        if (activeModal == ActiveModal.REVIEW_DETAILS && selectedReview != null) {
            // Delete feedback
            if (mx >= revModalDeleteBtnX && mx <= revModalDeleteBtnX + revModalDeleteBtnW &&
                    my >= revModalDeleteBtnY && my <= revModalDeleteBtnY + revModalDeleteBtnH) {
                AdminDataStore.getInstance().deleteReview(selectedReview.id);
                dashboard.showToast("Feedback #" + selectedReview.id + " deleted permanently");
                closeModal();
                return true;
            }
            // Approve feedback
            if (revModalApproveBtnW > 0 && mx >= revModalApproveBtnX && mx <= revModalApproveBtnX + revModalApproveBtnW
                    &&
                    my >= revModalApproveBtnY && my <= revModalApproveBtnY + revModalApproveBtnH) {
                AdminDataStore.getInstance().approveReportedReview(selectedReview.id);
                dashboard.showToast("Feedback #" + selectedReview.id + " approved & published");
                closeModal();
                return true;
            }
            // Hide / Unhide
            if (mx >= revModalHideBtnX && mx <= revModalHideBtnX + revModalHideBtnW &&
                    my >= revModalHideBtnY && my <= revModalHideBtnY + revModalHideBtnH) {
                AdminDataStore.getInstance().hideReview(selectedReview.id);
                boolean isNowHidden = "Hidden".equalsIgnoreCase(selectedReview.status);
                dashboard.showToast(isNowHidden ? "Feedback #" + selectedReview.id + " hidden"
                        : "Feedback #" + selectedReview.id + " published");
                selectedReview = AdminDataStore.getInstance().getReviewById(selectedReview.id);
                return true;
            }
            // Close
            if (mx >= revModalCloseBtnX && mx <= revModalCloseBtnX + revModalCloseBtnW &&
                    my >= revModalCloseBtnY && my <= revModalCloseBtnY + revModalCloseBtnH) {
                closeModal();
                return true;
            }
        }

        // Add Review Modal Actions
        if (activeModal == ActiveModal.ADD_REVIEW) {
            // Field 0: Reviewer
            if (mx >= newRevReviewerInputX && mx <= newRevReviewerInputX + newRevReviewerInputW &&
                    my >= newRevReviewerInputY && my <= newRevReviewerInputY + newRevReviewerInputH) {
                activeReviewField = 0;
                return true;
            }
            // Field 1: Reviewee
            if (mx >= newRevRevieweeInputX && mx <= newRevRevieweeInputX + newRevRevieweeInputW &&
                    my >= newRevRevieweeInputY && my <= newRevRevieweeInputY + newRevRevieweeInputH) {
                activeReviewField = 1;
                return true;
            }
            // Field 2: Project
            if (mx >= newRevProjectInputX && mx <= newRevProjectInputX + newRevProjectInputW &&
                    my >= newRevProjectInputY && my <= newRevProjectInputY + newRevProjectInputH) {
                activeReviewField = 2;
                return true;
            }
            // Star clicks (1..5)
            if (my >= newRevStarY - 6 && my <= newRevStarY + 28) {
                for (int i = 1; i <= 5; i++) {
                    int sx = newRevStarX + (i - 1) * 32;
                    if (mx >= sx && mx <= sx + 30) {
                        newRevRating = i;
                        return true;
                    }
                }
            }
            // Type pills
            if (mx >= newRevTypeTeamX && mx <= newRevTypeTeamX + newRevTypeW &&
                    my >= newRevTypeY && my <= newRevTypeY + newRevTypeH) {
                newRevType = "Team Member Review";
                return true;
            }
            if (mx >= newRevTypeUserX && mx <= newRevTypeUserX + newRevTypeW &&
                    my >= newRevTypeY && my <= newRevTypeY + newRevTypeH) {
                newRevType = "User Rating";
                return true;
            }
            if (mx >= newRevTypeCollabX && mx <= newRevTypeCollabX + newRevTypeW &&
                    my >= newRevTypeY && my <= newRevTypeY + newRevTypeH) {
                newRevType = "Peer Collaboration";
                return true;
            }
            // Field 4: Text
            if (mx >= newRevTextInputX && mx <= newRevTextInputX + newRevTextInputW &&
                    my >= newRevTextInputY && my <= newRevTextInputY + newRevTextInputH) {
                activeReviewField = 4;
                return true;
            }
            // Cancel
            if (mx >= cancelNewRevBtnX && mx <= cancelNewRevBtnX + cancelNewRevBtnW &&
                    my >= cancelNewRevBtnY && my <= cancelNewRevBtnY + cancelNewRevBtnH) {
                closeModal();
                return true;
            }
            // Post Review
            if (mx >= saveNewRevBtnX && mx <= saveNewRevBtnX + saveNewRevBtnW &&
                    my >= saveNewRevBtnY && my <= saveNewRevBtnY + saveNewRevBtnH) {
                if (newRevReviewer.trim().isEmpty()) {
                    dashboard.showToast("Please enter a reviewer name");
                    return true;
                }
                if (newRevText.trim().isEmpty()) {
                    dashboard.showToast("Please enter feedback comments");
                    return true;
                }
                AdminDataStore.getInstance().addReview(
                        newRevReviewer.trim(),
                        newRevReviewee.trim(),
                        newRevProject.trim(),
                        newRevRole.trim(),
                        newRevRating,
                        newRevText.trim(),
                        newRevType);
                dashboard.showToast("Review submitted successfully");
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
                    String catName = inputName.trim();
                    if (!catName.isEmpty()) {
                        AdminDataStore.getInstance().addCategory(catName);
                        closeModal();
                    }
                } else {
                    String skillName = inputName.trim();
                    if (!skillName.isEmpty()) {
                        AdminDataStore.getInstance().addSkill(skillName);
                        closeModal();
                    }
                }
                return true;
            } else if (ch >= 32 && ch <= 126) {
                if (inputName.length() < 40) {
                    inputName += ch;
                }
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
        } else if (activeModal == ActiveModal.ADD_PROJECT) {
            if (keyCode == java.awt.event.KeyEvent.VK_TAB) {
                activeProjectField = (activeProjectField + 1) % 4;
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                if (activeProjectField < 3) {
                    activeProjectField++;
                    return true;
                } else {
                    if (!newProjectName.trim().isEmpty()) {
                        AdminDataStore.getInstance().addProject(newProjectName, newProjectCategory, newProjectOwner,
                                newProjectTeamSize, newProjectStatus, newProjectDesc);
                        closeModal();
                        return true;
                    }
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (activeProjectField == 0 && newProjectName.length() > 0) {
                    newProjectName = newProjectName.substring(0, newProjectName.length() - 1);
                    return true;
                } else if (activeProjectField == 1 && newProjectOwner.length() > 0) {
                    newProjectOwner = newProjectOwner.substring(0, newProjectOwner.length() - 1);
                    return true;
                } else if (activeProjectField == 2 && newProjectTeamSize.length() > 0) {
                    newProjectTeamSize = newProjectTeamSize.substring(0, newProjectTeamSize.length() - 1);
                    return true;
                } else if (activeProjectField == 3 && newProjectDesc.length() > 0) {
                    newProjectDesc = newProjectDesc.substring(0, newProjectDesc.length() - 1);
                    return true;
                }
            } else if (ch >= 32 && ch <= 126) {
                if (activeProjectField == 0)
                    newProjectName += ch;
                else if (activeProjectField == 1)
                    newProjectOwner += ch;
                else if (activeProjectField == 2)
                    newProjectTeamSize += ch;
                else if (activeProjectField == 3)
                    newProjectDesc += ch;
                return true;
            }
        } else if (activeModal == ActiveModal.ADD_REVIEW) {
            if (keyCode == java.awt.event.KeyEvent.VK_TAB) {
                activeReviewField = (activeReviewField + 1) % 5;
                if (activeReviewField == 3)
                    activeReviewField = 4;
                return true;
            } else if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                if (activeReviewField < 4) {
                    activeReviewField = (activeReviewField == 2) ? 4 : activeReviewField + 1;
                    return true;
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                if (activeReviewField == 0 && newRevReviewer.length() > 0) {
                    newRevReviewer = newRevReviewer.substring(0, newRevReviewer.length() - 1);
                    return true;
                } else if (activeReviewField == 1 && newRevReviewee.length() > 0) {
                    newRevReviewee = newRevReviewee.substring(0, newRevReviewee.length() - 1);
                    return true;
                } else if (activeReviewField == 2 && newRevProject.length() > 0) {
                    newRevProject = newRevProject.substring(0, newRevProject.length() - 1);
                    return true;
                } else if (activeReviewField == 4 && newRevText.length() > 0) {
                    newRevText = newRevText.substring(0, newRevText.length() - 1);
                    return true;
                }
            } else if (ch >= 32 && ch <= 126) {
                if (activeReviewField == 0)
                    newRevReviewer += ch;
                else if (activeReviewField == 1)
                    newRevReviewee += ch;
                else if (activeReviewField == 2)
                    newRevProject += ch;
                else if (activeReviewField == 4)
                    newRevText += ch;
                return true;
            }
        }
        return false;
    }

    public boolean onMouseMove(int mx, int my) {
        if (activeModal == ActiveModal.NONE)
            return false;

        boolean pClose = closeBtnHovered;
        closeBtnHovered = (mx >= closeBtnX && mx <= closeBtnX + closeBtnSize && my >= closeBtnY
                && my <= closeBtnY + closeBtnSize);

        boolean pDis = dismissHovered;
        dismissHovered = (mx >= dismissBtnX && mx <= dismissBtnX + dismissBtnW && my >= dismissBtnY
                && my <= dismissBtnY + dismissBtnH);

        boolean pWarn = warnHovered;
        warnHovered = (mx >= warnBtnX && mx <= warnBtnX + warnBtnW && my >= warnBtnY && my <= warnBtnY + warnBtnH);

        boolean pSus = suspendHovered;
        suspendHovered = (mx >= suspendBtnX && mx <= suspendBtnX + suspendBtnW && my >= suspendBtnY
                && my <= suspendBtnY + suspendBtnH);

        boolean pSaveR = saveRoleHovered;
        saveRoleHovered = (mx >= saveRoleBtnX && mx <= saveRoleBtnX + saveRoleBtnW && my >= saveRoleBtnY
                && my <= saveRoleBtnY + saveRoleBtnH);

        boolean pSaveNew = saveNewItemHovered;
        saveNewItemHovered = (mx >= saveNewItemBtnX && mx <= saveNewItemBtnX + saveNewItemBtnW && my >= saveNewItemBtnY
                && my <= saveNewItemBtnY + saveNewItemBtnH);

        boolean pCancelNew = cancelNewItemHovered;
        cancelNewItemHovered = (mx >= cancelNewItemBtnX && mx <= cancelNewItemBtnX + cancelNewItemBtnW
                && my >= cancelNewItemBtnY && my <= cancelNewItemBtnY + cancelNewItemBtnH);

        boolean pUsus = userModalSuspendHovered;
        userModalSuspendHovered = (mx >= userModalSuspendBtnX && mx <= userModalSuspendBtnX + userModalSuspendBtnW
                && my >= userModalSuspendBtnY && my <= userModalSuspendBtnY + userModalSuspendBtnH);

        boolean pUrole = userModalRoleHovered;
        userModalRoleHovered = (mx >= userModalRoleBtnX && mx <= userModalRoleBtnX + userModalRoleBtnW
                && my >= userModalRoleBtnY && my <= userModalRoleBtnY + userModalRoleBtnH);

        boolean pUdel = userModalDeleteHovered;
        userModalDeleteHovered = (mx >= userModalDeleteBtnX && mx <= userModalDeleteBtnX + userModalDeleteBtnW
                && my >= userModalDeleteBtnY && my <= userModalDeleteBtnY + userModalDeleteBtnH);

        boolean pUclose = userModalCloseHovered;
        userModalCloseHovered = (mx >= userModalCloseBtnX && mx <= userModalCloseBtnX + userModalCloseBtnW
                && my >= userModalCloseBtnY && my <= userModalCloseBtnY + userModalCloseBtnH);

        boolean pSnu = saveNewUserHovered;
        saveNewUserHovered = (mx >= saveNewUserBtnX && mx <= saveNewUserBtnX + saveNewUserBtnW && my >= saveNewUserBtnY
                && my <= saveNewUserBtnY + saveNewUserBtnH);

        boolean pCnu = cancelNewUserHovered;
        cancelNewUserHovered = (mx >= cancelNewUserBtnX && mx <= cancelNewUserBtnX + cancelNewUserBtnW
                && my >= cancelNewUserBtnY && my <= cancelNewUserBtnY + cancelNewUserBtnH);

        boolean pPapp = projModalApproveHovered;
        projModalApproveHovered = (mx >= projModalApproveBtnX && mx <= projModalApproveBtnX + projModalApproveBtnW
                && my >= projModalApproveBtnY && my <= projModalApproveBtnY + projModalApproveBtnH);

        boolean pPstat = projModalStatusHovered;
        projModalStatusHovered = (mx >= projModalStatusBtnX && mx <= projModalStatusBtnX + projModalStatusBtnW
                && my >= projModalStatusBtnY && my <= projModalStatusBtnY + projModalStatusBtnH);

        boolean pPdel = projModalDeleteHovered;
        projModalDeleteHovered = (mx >= projModalDeleteBtnX && mx <= projModalDeleteBtnX + projModalDeleteBtnW
                && my >= projModalDeleteBtnY && my <= projModalDeleteBtnY + projModalDeleteBtnH);

        boolean pPclose = projModalCloseHovered;
        projModalCloseHovered = (mx >= projModalCloseBtnX && mx <= projModalCloseBtnX + projModalCloseBtnW
                && my >= projModalCloseBtnY && my <= projModalCloseBtnY + projModalCloseBtnH);

        boolean pSnp = saveNewProjectHovered;
        saveNewProjectHovered = (mx >= saveNewProjectBtnX && mx <= saveNewProjectBtnX + saveNewProjectBtnW
                && my >= saveNewProjectBtnY && my <= saveNewProjectBtnY + saveNewProjectBtnH);

        boolean pCnp = cancelNewProjectHovered;
        cancelNewProjectHovered = (mx >= cancelNewProjectBtnX && mx <= cancelNewProjectBtnX + cancelNewProjectBtnW
                && my >= cancelNewProjectBtnY && my <= cancelNewProjectBtnY + cancelNewProjectBtnH);

        boolean pRdel = revModalDeleteHovered;
        revModalDeleteHovered = (mx >= revModalDeleteBtnX && mx <= revModalDeleteBtnX + revModalDeleteBtnW
                && my >= revModalDeleteBtnY && my <= revModalDeleteBtnY + revModalDeleteBtnH);

        boolean pRapp = revModalApproveHovered;
        revModalApproveHovered = (mx >= revModalApproveBtnX && mx <= revModalApproveBtnX + revModalApproveBtnW
                && my >= revModalApproveBtnY && my <= revModalApproveBtnY + revModalApproveBtnH);

        boolean pRhide = revModalHideHovered;
        revModalHideHovered = (mx >= revModalHideBtnX && mx <= revModalHideBtnX + revModalHideBtnW
                && my >= revModalHideBtnY && my <= revModalHideBtnY + revModalHideBtnH);

        boolean pRclose = revModalCloseHovered;
        revModalCloseHovered = (mx >= revModalCloseBtnX && mx <= revModalCloseBtnX + revModalCloseBtnW
                && my >= revModalCloseBtnY && my <= revModalCloseBtnY + revModalCloseBtnH);

        boolean pSnr = saveNewRevHovered;
        saveNewRevHovered = (mx >= saveNewRevBtnX && mx <= saveNewRevBtnX + saveNewRevBtnW && my >= saveNewRevBtnY
                && my <= saveNewRevBtnY + saveNewRevBtnH);

        boolean pCnr = cancelNewRevHovered;
        cancelNewRevHovered = (mx >= cancelNewRevBtnX && mx <= cancelNewRevBtnX + cancelNewRevBtnW
                && my >= cancelNewRevBtnY && my <= cancelNewRevBtnY + cancelNewRevBtnH);

        return pClose != closeBtnHovered || pDis != dismissHovered || pWarn != warnHovered || pSus != suspendHovered ||
                pSaveR != saveRoleHovered || pSaveNew != saveNewItemHovered || pCancelNew != cancelNewItemHovered ||
                pUsus != userModalSuspendHovered ||
                pUrole != userModalRoleHovered || pUdel != userModalDeleteHovered || pUclose != userModalCloseHovered ||
                pSnu != saveNewUserHovered || pCnu != cancelNewUserHovered ||
                pPapp != projModalApproveHovered || pPstat != projModalStatusHovered || pPdel != projModalDeleteHovered
                ||
                pPclose != projModalCloseHovered || pSnp != saveNewProjectHovered || pCnp != cancelNewProjectHovered ||
                pRdel != revModalDeleteHovered || pRapp != revModalApproveHovered || pRhide != revModalHideHovered ||
                pRclose != revModalCloseHovered || pSnr != saveNewRevHovered || pCnr != cancelNewRevHovered;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = (inputName != null) ? inputName : "";
    }
}
