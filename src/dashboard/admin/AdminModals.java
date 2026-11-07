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
        ADD_SKILL
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

    public void closeModal() {
        this.activeModal = ActiveModal.NONE;
        this.currentItem = null;
        this.editRoleUser = "";
        this.inputName = "";
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

        return pClose != closeBtnHovered || pDis != dismissHovered || pWarn != warnHovered || pSus != suspendHovered || pSaveR != saveRoleHovered || pSaveNew != saveNewItemHovered;
    }
}
