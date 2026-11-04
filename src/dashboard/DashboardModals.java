import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles all modal dialogs for TeamUp Student Dashboard.
 * Ensures instant, lag-free closing on cross button, cancel, outside clicks, or ESC key.
 * Provides rich, interactive forms with focused outlines, caret blinking, and tag selection.
 */
public class DashboardModals {

    private StudentDashboard.ActiveModal activeModal = StudentDashboard.ActiveModal.NONE;
    private float modalAnim = 0f;

    // Modal layout bounds
    private int activeModalX, activeModalY, activeModalW, activeModalH;
    private int modalCloseX, modalCloseY, modalCloseSize = 32;
    private boolean modalCloseHovered = false;

    // Caret blink
    private float caretBlinkTimer = 0f;
    private boolean caretVisible = true;

    // Create Project Form state & hitboxes
    private String newProjTitle = "";
    private String newProjCategory = "Sustainability";
    private String newProjDesc = "";
    private final List<String> newProjSkills = new ArrayList<>(Arrays.asList("React", "Node.js"));
    private int formFocusField = 0; // 0: Title, 1: Desc

    private int projTitleX, projTitleY, projTitleW, projTitleH;
    private int projDescX, projDescY, projDescW, projDescH;
    private int projSubmitBtnX, projSubmitBtnY, projSubmitBtnW, projSubmitBtnH;
    private int projCancelBtnX, projCancelBtnY, projCancelBtnW, projCancelBtnH;
    private boolean projSubmitHovered = false;
    private boolean projCancelHovered = false;

    private final List<StudentDashboard.PillHitbox> categoryPills = new ArrayList<>();
    private final List<StudentDashboard.PillHitbox> skillChips = new ArrayList<>();

    // Profile Form state & hitboxes
    private int profileFocusField = 0; // 0: Name, 1: Bio, 2: Portfolio
    private int profNameX, profNameY, profNameW, profNameH;
    private int profBioX, profBioY, profBioW, profBioH;
    private int profPortX, profPortY, profPortW, profPortH;
    private int profSaveBtnX, profSaveBtnY, profSaveBtnW, profSaveBtnH;
    private int profCancelBtnX, profCancelBtnY, profCancelBtnW, profCancelBtnH;
    private boolean profSaveHovered = false;
    private boolean profCancelHovered = false;

    // Project Details hitboxes
    private int projJoinBtnX, projJoinBtnY, projJoinBtnW, projJoinBtnH;
    private int projDetailCancelX, projDetailCancelY, projDetailCancelW, projDetailCancelH;
    private boolean projJoinHovered = false;
    private boolean projDetailCancelHovered = false;

    // Teammate Details hitboxes
    private int teamInviteBtnX, teamInviteBtnY, teamInviteBtnW, teamInviteBtnH;
    private int teamDetailCancelX, teamDetailCancelY, teamDetailCancelW, teamDetailCancelH;
    private boolean teamInviteHovered = false;
    private boolean teamDetailCancelHovered = false;

    // Tasks modal hitboxes
    private int tasksDoneBtnX, tasksDoneBtnY, tasksDoneBtnW, tasksDoneBtnH;
    private boolean tasksDoneHovered = false;

    // Messaging Form state & hitboxes
    private String newMessageText = "";
    private int msgInputX, msgInputY, msgInputW, msgInputH;
    private int msgSendBtnX, msgSendBtnY, msgSendBtnW, msgSendBtnH;
    private boolean msgSendHovered = false;

    public StudentDashboard.ActiveModal getActiveModal() {
        return activeModal;
    }

    public void open(StudentDashboard.ActiveModal modal) {
        this.activeModal = modal;
        this.modalAnim = 1.0f; // Instant open
    }

    /**
     * Closes the active modal instantly with zero lag.
     */
    public void closeModal() {
        this.activeModal = StudentDashboard.ActiveModal.NONE;
        this.modalAnim = 0f;
    }

    public void update(float dt) {
        if (activeModal == StudentDashboard.ActiveModal.NONE) {
            modalAnim = 0f;
            return;
        }

        // Fast smooth ease-in if opened
        if (modalAnim < 1f) {
            modalAnim = Math.min(1f, modalAnim + dt * 15f);
        }

        caretBlinkTimer += dt;
        if (caretBlinkTimer >= 0.5f) {
            caretBlinkTimer = 0f;
            caretVisible = !caretVisible;
        }
    }

    public void paint(Graphics2D g, int width, int height, StudentDashboard dashboard) {
        if (activeModal == StudentDashboard.ActiveModal.NONE) {
            return; // Instant disappear
        }

        // 1. Semi-transparent backdrop
        g.setColor(new Color(15, 23, 42, 180));
        g.fillRect(0, 0, width, height);

        // 2. Compute modal dimensions based on type
        int modalW = 560;
        int modalH = 590;
        if (activeModal == StudentDashboard.ActiveModal.COMPLETE_PROFILE) {
            modalW = 540;
            modalH = 510;
        } else if (activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS) {
            modalW = 540;
            modalH = 490;
        } else if (activeModal == StudentDashboard.ActiveModal.TEAMMATE_DETAILS) {
            modalW = 540;
            modalH = 480;
        } else if (activeModal == StudentDashboard.ActiveModal.ALL_TASKS) {
            modalW = 520;
            modalH = 480;
        } else if (activeModal == StudentDashboard.ActiveModal.MESSAGES) {
            modalW = 560;
            modalH = 530;
        }

        // Clamp to screen bounds
        modalW = Math.min(modalW, width - 32);
        modalH = Math.min(modalH, height - 32);

        int modalX = (width - modalW) / 2;
        int modalY = (height - modalH) / 2;

        this.activeModalX = modalX;
        this.activeModalY = modalY;
        this.activeModalW = modalW;
        this.activeModalH = modalH;

        this.modalCloseSize = 32;
        this.modalCloseX = modalX + modalW - modalCloseSize - 18;
        this.modalCloseY = modalY + 18;

        // 3. Drop shadows (layered for elevated 3D depth)
        for (int i = 0; i < 4; i++) {
            int off = (i + 1) * 3;
            int alpha = Math.max(4, 20 - i * 4);
            g.setColor(new Color(15, 23, 42, alpha));
            g.fill(new RoundRectangle2D.Float(modalX - off / 2f, modalY + off, modalW + off, modalH + off / 2f, 24, 24));
        }

        // 4. White modal container surface
        RoundRectangle2D box = new RoundRectangle2D.Float(modalX, modalY, modalW, modalH, 20, 20);
        g.setColor(Color.WHITE);
        g.fill(box);
        g.setColor(DashboardTheme.BORDER_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(box);

        // 5. Dedicated Close Button (✕)
        RoundRectangle2D closeBtnShape = new RoundRectangle2D.Float(modalCloseX, modalCloseY, modalCloseSize, modalCloseSize, 10, 10);
        if (modalCloseHovered) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(closeBtnShape);
            g.setColor(new Color(0xCB, 0xD5, 0xE1));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(closeBtnShape);
            g.setColor(DashboardTheme.TEXT_DARK);
        } else {
            g.setColor(new Color(0xF8, 0xFA, 0xFC));
            g.fill(closeBtnShape);
            g.setColor(new Color(0xE2, 0xE8, 0xF0));
            g.setStroke(new BasicStroke(1.0f));
            g.draw(closeBtnShape);
            g.setColor(DashboardTheme.TEXT_MUTED);
        }

        // Draw crisp vector ✕
        Graphics2D cg = (Graphics2D) g.create();
        cg.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pad = 10;
        cg.drawLine(modalCloseX + pad, modalCloseY + pad, modalCloseX + modalCloseSize - pad, modalCloseY + modalCloseSize - pad);
        cg.drawLine(modalCloseX + modalCloseSize - pad, modalCloseY + pad, modalCloseX + pad, modalCloseY + modalCloseSize - pad);
        cg.dispose();

        // 6. Paint modal content
        switch (activeModal) {
            case CREATE_PROJECT:
                paintCreateProjectModal(g, modalX, modalY, modalW, modalH);
                break;
            case COMPLETE_PROFILE:
                paintCompleteProfileModal(g, modalX, modalY, modalW, modalH, dashboard);
                break;
            case PROJECT_DETAILS:
                paintProjectDetailsModal(g, modalX, modalY, modalW, modalH, dashboard);
                break;
            case TEAMMATE_DETAILS:
                paintTeammateDetailsModal(g, modalX, modalY, modalW, modalH, dashboard);
                break;
            case ALL_TASKS:
                paintAllTasksModal(g, modalX, modalY, modalW, modalH, dashboard);
                break;
            case MESSAGES:
                paintMessagesModal(g, modalX, modalY, modalW, modalH, dashboard);
                break;
            default:
                break;
        }
    }

    private void paintCreateProjectModal(Graphics2D g, int x, int y, int w, int h) {
        int padX = 26;
        int innerW = w - padX * 2;

        // Header with icon badge
        int iconBadgeSize = 36;
        RoundRectangle2D badgeShape = new RoundRectangle2D.Float(x + padX, y + 18, iconBadgeSize, iconBadgeSize, 10, 10);
        g.setColor(new Color(0xEE, 0xF2, 0xFF));
        g.fill(badgeShape);
        g.setColor(new Color(0xC7, 0xD2, 0xFE));
        g.setStroke(new BasicStroke(1f));
        g.draw(badgeShape);

        // Vector '+' inside badge
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int bxMid = x + padX + iconBadgeSize / 2;
        int byMid = y + 18 + iconBadgeSize / 2;
        g.drawLine(bxMid - 6, byMid, bxMid + 6, byMid);
        g.drawLine(bxMid, byMid - 6, bxMid, byMid + 6);

        // Title & subtitle
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Create New Project", x + padX + iconBadgeSize + 12, y + 33);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Publish your project to recruit verified peers with matching skills.", x + padX + iconBadgeSize + 12, y + 50);

        // Separator
        int curY = y + 66;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 14;

        // 1. Project Title
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("PROJECT TITLE *", x + padX, curY);

        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(newProjTitle.length() + "/40", x + padX + innerW - 32, curY);
        curY += 7;

        projTitleX = x + padX;
        projTitleY = curY;
        projTitleW = innerW;
        projTitleH = 38;

        RoundRectangle2D titleBox = new RoundRectangle2D.Float(projTitleX, projTitleY, projTitleW, projTitleH, 8, 8);
        boolean titleFocused = (formFocusField == 0);
        g.setColor(titleFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(titleBox);
        g.setColor(titleFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(titleFocused ? 1.6f : 1.1f));
        g.draw(titleBox);

        g.setFont(DashboardTheme.FONT_REG_13);
        if (newProjTitle.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("e.g. StudyBuddy AI, Campus Shuttle Tracker...", projTitleX + 12, projTitleY + 24);
            if (titleFocused && caretVisible) {
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(projTitleX + 12, projTitleY + 10, projTitleX + 12, projTitleY + 28);
            }
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(newProjTitle, projTitleX + 12, projTitleY + 24);
            if (titleFocused && caretVisible) {
                int tw = g.getFontMetrics().stringWidth(newProjTitle);
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(projTitleX + 12 + tw + 2, projTitleY + 10, projTitleX + 12 + tw + 2, projTitleY + 28);
            }
        }

        curY += projTitleH + 13;

        // 2. Category selection pills
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("CATEGORY / DOMAIN", x + padX, curY);
        curY += 7;

        categoryPills.clear();
        String[] cats = {"Sustainability", "AI & Research", "Web & Mobile", "Design & UX"};
        int pillX = x + padX;
        int pillH = 28;
        for (String cat : cats) {
            g.setFont(DashboardTheme.FONT_BOLD_11);
            int pillW = g.getFontMetrics().stringWidth(cat) + 20;
            categoryPills.add(new StudentDashboard.PillHitbox(cat, pillX, curY, pillW, pillH));

            boolean isCatSel = cat.equalsIgnoreCase(newProjCategory);
            RoundRectangle2D pillShape = new RoundRectangle2D.Float(pillX, curY, pillW, pillH, 8, 8);
            if (isCatSel) {
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.fill(pillShape);
                g.setColor(Color.WHITE);
                g.drawString(cat, pillX + 10, curY + 18);
            } else {
                g.setColor(new Color(0xF1, 0xF5, 0xF9));
                g.fill(pillShape);
                g.setColor(new Color(0xE2, 0xE8, 0xF0));
                g.setStroke(new BasicStroke(1f));
                g.draw(pillShape);
                g.setColor(new Color(0x47, 0x55, 0x69));
                g.drawString(cat, pillX + 10, curY + 18);
            }
            pillX += pillW + 8;
        }

        curY += pillH + 13;

        // 3. Short Description
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("SHORT DESCRIPTION *", x + padX, curY);

        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(newProjDesc.length() + "/120", x + padX + innerW - 36, curY);
        curY += 7;

        projDescX = x + padX;
        projDescY = curY;
        projDescW = innerW;
        projDescH = 62;

        RoundRectangle2D descBox = new RoundRectangle2D.Float(projDescX, projDescY, projDescW, projDescH, 8, 8);
        boolean descFocused = (formFocusField == 1);
        g.setColor(descFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(descBox);
        g.setColor(descFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(descFocused ? 1.6f : 1.1f));
        g.draw(descBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        if (newProjDesc.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString("What does this project build and why are you excited to collaborate?", projDescX + 12, projDescY + 22);
            if (descFocused && caretVisible) {
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(projDescX + 12, projDescY + 10, projDescX + 12, projDescY + 26);
            }
        } else {
            g.setColor(DashboardTheme.TEXT_DARK);
            List<String> wrapped = Draw.wrapText(newProjDesc, g.getFontMetrics(), projDescW - 24);
            int textY = projDescY + 20;
            int lastLineIdx = 0;
            for (int i = 0; i < wrapped.size() && i < 3; i++) {
                g.drawString(wrapped.get(i), projDescX + 12, textY);
                lastLineIdx = i;
                textY += 18;
            }
            if (descFocused && caretVisible) {
                String lastLine = wrapped.isEmpty() ? "" : wrapped.get(lastLineIdx);
                int lw = g.getFontMetrics().stringWidth(lastLine);
                int cyPos = projDescY + 20 + lastLineIdx * 18;
                g.setColor(DashboardTheme.BRAND_PURPLE);
                g.drawLine(projDescX + 12 + lw + 2, cyPos - 12, projDescX + 12 + lw + 2, cyPos + 3);
            }
        }

        curY += projDescH + 13;

        // 4. Required Skills Chips (Interactive click to toggle)
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("REQUIRED SKILLS & TAGS (CLICK TO TOGGLE)", x + padX, curY);
        curY += 7;

        skillChips.clear();
        String[] availableSkills = {"React", "Node.js", "Python", "UI/UX", "PostgreSQL", "Flutter", "AI / ML", "Figma"};
        int chipX = x + padX;
        int chipY = curY;
        int chipH = 26;

        for (int i = 0; i < availableSkills.length; i++) {
            String sk = availableSkills[i];
            boolean selected = newProjSkills.contains(sk);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            String displayLabel = selected ? "✓ " + sk : "+ " + sk;
            int chipW = g.getFontMetrics().stringWidth(displayLabel) + 18;

            if (chipX + chipW > x + padX + innerW) {
                chipX = x + padX;
                chipY += chipH + 6;
            }

            skillChips.add(new StudentDashboard.PillHitbox(sk, chipX, chipY, chipW, chipH));

            RoundRectangle2D chipShape = new RoundRectangle2D.Float(chipX, chipY, chipW, chipH, 8, 8);
            if (selected) {
                g.setColor(new Color(0xEE, 0xF2, 0xFF));
                g.fill(chipShape);
                g.setColor(new Color(0x63, 0x66, 0xF1));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(chipShape);
                g.setColor(new Color(0x43, 0x38, 0xCA));
                g.drawString(displayLabel, chipX + 9, chipY + 17);
            } else {
                g.setColor(new Color(0xF8, 0xFA, 0xFC));
                g.fill(chipShape);
                g.setColor(new Color(0xE2, 0xE8, 0xF0));
                g.setStroke(new BasicStroke(1f));
                g.draw(chipShape);
                g.setColor(DashboardTheme.TEXT_MUTED);
                g.drawString(displayLabel, chipX + 9, chipY + 17);
            }

            chipX += chipW + 8;
        }

        // 5. Footer Actions (Cancel + Publish buttons)
        curY = y + h - 60;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 12;

        int btnH = 38;
        // Cancel Button
        projCancelBtnX = x + padX;
        projCancelBtnY = curY;
        projCancelBtnW = 95;
        projCancelBtnH = btnH;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(projCancelBtnX, projCancelBtnY, projCancelBtnW, projCancelBtnH, 8, 8);
        g.setColor(projCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1f));
        g.draw(cancelShape);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("Cancel", projCancelBtnX + (projCancelBtnW - g.getFontMetrics().stringWidth("Cancel")) / 2, projCancelBtnY + 23);

        // Submit Button
        projSubmitBtnX = x + padX + projCancelBtnW + 12;
        projSubmitBtnY = curY;
        projSubmitBtnW = innerW - projCancelBtnW - 12;
        projSubmitBtnH = btnH;

        RoundRectangle2D submitShape = new RoundRectangle2D.Float(projSubmitBtnX, projSubmitBtnY, projSubmitBtnW, projSubmitBtnH, 8, 8);
        g.setColor(projSubmitHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(submitShape);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        String subText = "Publish Project to Students  →";
        g.drawString(subText, projSubmitBtnX + (projSubmitBtnW - g.getFontMetrics().stringWidth(subText)) / 2, projSubmitBtnY + 23);
    }

    private void paintCompleteProfileModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        int padX = 26;
        int innerW = w - padX * 2;

        // Header with icon badge
        int iconBadgeSize = 36;
        RoundRectangle2D badgeShape = new RoundRectangle2D.Float(x + padX, y + 18, iconBadgeSize, iconBadgeSize, 10, 10);
        g.setColor(new Color(0xDC, 0xFC, 0xE7));
        g.fill(badgeShape);
        g.setColor(new Color(0x86, 0xEF, 0xAC));
        g.setStroke(new BasicStroke(1f));
        g.draw(badgeShape);

        // User icon / check inside badge
        g.setColor(new Color(0x16, 0xA3, 0x4A));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("★", x + padX + 11, y + 42);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Complete Student Profile", x + padX + iconBadgeSize + 12, y + 33);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Add your bio and portfolio link to reach 100% profile strength.", x + padX + iconBadgeSize + 12, y + 50);

        // Separator
        int curY = y + 66;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 16;

        // 1. Full Name
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("FULL NAME", x + padX, curY);
        curY += 7;

        profNameX = x + padX;
        profNameY = curY;
        profNameW = innerW;
        profNameH = 38;

        RoundRectangle2D nameBox = new RoundRectangle2D.Float(profNameX, profNameY, profNameW, profNameH, 8, 8);
        boolean nameFocused = (profileFocusField == 0);
        g.setColor(nameFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(nameBox);
        g.setColor(nameFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(nameFocused ? 1.6f : 1.1f));
        g.draw(nameBox);

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(dashboard.getProfileName(), profNameX + 12, profNameY + 24);
        if (nameFocused && caretVisible) {
            int tw = g.getFontMetrics().stringWidth(dashboard.getProfileName());
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawLine(profNameX + 12 + tw + 2, profNameY + 10, profNameX + 12 + tw + 2, profNameY + 28);
        }

        curY += profNameH + 14;

        // 2. Bio & Background
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("BIO & BACKGROUND", x + padX, curY);
        curY += 7;

        profBioX = x + padX;
        profBioY = curY;
        profBioW = innerW;
        profBioH = 54;

        RoundRectangle2D bioBox = new RoundRectangle2D.Float(profBioX, profBioY, profBioW, profBioH, 8, 8);
        boolean bioFocused = (profileFocusField == 1);
        g.setColor(bioFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(bioBox);
        g.setColor(bioFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(bioFocused ? 1.6f : 1.1f));
        g.draw(bioBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        List<String> wrappedBio = Draw.wrapText(dashboard.getProfileBio(), g.getFontMetrics(), profBioW - 24);
        int byPos = profBioY + 20;
        for (int i = 0; i < wrappedBio.size() && i < 2; i++) {
            g.drawString(wrappedBio.get(i), profBioX + 12, byPos);
            byPos += 18;
        }

        curY += profBioH + 14;

        // 3. Portfolio URL
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("PORTFOLIO / GITHUB URL", x + padX, curY);
        curY += 7;

        profPortX = x + padX;
        profPortY = curY;
        profPortW = innerW;
        profPortH = 38;

        RoundRectangle2D portBox = new RoundRectangle2D.Float(profPortX, profPortY, profPortW, profPortH, 8, 8);
        boolean portFocused = (profileFocusField == 2);
        g.setColor(portFocused ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
        g.fill(portBox);
        g.setColor(portFocused ? DashboardTheme.BRAND_PURPLE : new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(portFocused ? 1.6f : 1.1f));
        g.draw(portBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.drawString(dashboard.getProfilePortfolio(), profPortX + 12, profPortY + 24);
        if (portFocused && caretVisible) {
            int tw = g.getFontMetrics().stringWidth(dashboard.getProfilePortfolio());
            g.drawLine(profPortX + 12 + tw + 2, profPortY + 10, profPortX + 12 + tw + 2, profPortY + 28);
        }

        // Footer Actions
        curY = y + h - 60;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 12;

        int btnH = 38;
        // Cancel Button
        profCancelBtnX = x + padX;
        profCancelBtnY = curY;
        profCancelBtnW = 95;
        profCancelBtnH = btnH;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(profCancelBtnX, profCancelBtnY, profCancelBtnW, profCancelBtnH, 8, 8);
        g.setColor(profCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1f));
        g.draw(cancelShape);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("Cancel", profCancelBtnX + (profCancelBtnW - g.getFontMetrics().stringWidth("Cancel")) / 2, profCancelBtnY + 23);

        // Save Button
        profSaveBtnX = x + padX + profCancelBtnW + 12;
        profSaveBtnY = curY;
        profSaveBtnW = innerW - profCancelBtnW - 12;
        profSaveBtnH = btnH;

        RoundRectangle2D saveShape = new RoundRectangle2D.Float(profSaveBtnX, profSaveBtnY, profSaveBtnW, profSaveBtnH, 8, 8);
        g.setColor(profSaveHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(saveShape);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        String saveText = "Save Profile & Reach 100%  →";
        g.drawString(saveText, profSaveBtnX + (profSaveBtnW - g.getFontMetrics().stringWidth(saveText)) / 2, profSaveBtnY + 23);
    }

    private void paintProjectDetailsModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        StudentDashboard.ProjectItem selectedProject = dashboard.getSelectedProject();
        if (selectedProject == null) return;
        int padX = 26;
        int innerW = w - padX * 2;

        // Header
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(selectedProject.title, x + padX, y + 36);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(selectedProject.author, x + padX, y + 54);

        int curY = y + 74;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 16;

        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(new Color(0x33, 0x41, 0x55));
        FontMetrics dfm = g.getFontMetrics();
        List<String> lines = Draw.wrapText(selectedProject.desc, dfm, innerW);
        for (String l : lines) {
            g.drawString(l, x + padX, curY);
            curY += 20;
        }

        curY += 10;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("REQUIRED TECH STACK:", x + padX, curY);
        curY += 8;

        int sx = x + padX;
        for (String sk : selectedProject.skills) {
            int sw = g.getFontMetrics().stringWidth(sk) + 18;
            RoundRectangle2D sp = new RoundRectangle2D.Float(sx, curY, sw, 26, 6, 6);
            g.setColor(new Color(0xEE, 0xF2, 0xF6));
            g.fill(sp);
            g.setFont(DashboardTheme.FONT_BOLD_11);
            g.setColor(new Color(0x33, 0x41, 0x55));
            g.drawString(sk, sx + 9, curY + 17);
            sx += sw + 8;
        }

        curY += 44;
        RoundRectangle2D statusBox = new RoundRectangle2D.Float(x + padX, curY, innerW, 44, 8, 8);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(statusBox);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.draw(statusBox);

        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Status: " + selectedProject.footer, x + padX + 12, curY + 18);
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Actively recruiting students across University of Washington.", x + padX + 12, curY + 34);

        // Footer Actions
        curY = y + h - 60;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 12;

        int btnH = 38;
        projDetailCancelX = x + padX;
        projDetailCancelY = curY;
        projDetailCancelW = 95;
        projDetailCancelH = btnH;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(projDetailCancelX, projDetailCancelY, projDetailCancelW, projDetailCancelH, 8, 8);
        g.setColor(projDetailCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.draw(cancelShape);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("Close", projDetailCancelX + (projDetailCancelW - g.getFontMetrics().stringWidth("Close")) / 2, projDetailCancelY + 23);

        projJoinBtnX = x + padX + projDetailCancelW + 12;
        projJoinBtnY = curY;
        projJoinBtnW = innerW - projDetailCancelW - 12;
        projJoinBtnH = btnH;

        RoundRectangle2D joinShape = new RoundRectangle2D.Float(projJoinBtnX, projJoinBtnY, projJoinBtnW, projJoinBtnH, 8, 8);
        g.setColor(projJoinHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(joinShape);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        String jText = "Request to Join Team  →";
        g.drawString(jText, projJoinBtnX + (projJoinBtnW - g.getFontMetrics().stringWidth(jText)) / 2, projJoinBtnY + 23);
    }

    private void paintTeammateDetailsModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        StudentDashboard.TeammateItem selectedTeammate = dashboard.getSelectedTeammate();
        if (selectedTeammate == null) return;
        int padX = 26;
        int innerW = w - padX * 2;

        // Avatar
        int avSize = 44;
        g.setColor(selectedTeammate.avBg);
        g.fillOval(x + padX, y + 20, avSize, avSize);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(selectedTeammate.avText);
        g.drawString(selectedTeammate.initials, x + padX + 11, y + 48);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(selectedTeammate.name, x + padX + avSize + 14, y + 36);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(selectedTeammate.role + " · " + selectedTeammate.university, x + padX + avSize + 14, y + 54);

        int curY = y + 74;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 16;

        // Match Score Badge
        RoundRectangle2D matchBadge = new RoundRectangle2D.Float(x + padX, curY, 130, 26, 6, 6);
        g.setColor(new Color(0xDC, 0xFC, 0xE7));
        g.fill(matchBadge);
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x15, 0x80, 0x3D));
        g.drawString("✓ " + selectedTeammate.matchScore + " match", x + padX + 10, curY + 17);

        curY += 36;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("ABOUT THIS STUDENT:", x + padX, curY);
        curY += 8;

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(new Color(0x33, 0x41, 0x55));
        FontMetrics dfm = g.getFontMetrics();
        List<String> lines = Draw.wrapText(selectedTeammate.bio, dfm, innerW);
        for (String l : lines) {
            g.drawString(l, x + padX, curY);
            curY += 18;
        }

        // Footer Actions
        curY = y + h - 60;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 12;

        int btnH = 38;
        teamDetailCancelX = x + padX;
        teamDetailCancelY = curY;
        teamDetailCancelW = 95;
        teamDetailCancelH = btnH;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(teamDetailCancelX, teamDetailCancelY, teamDetailCancelW, teamDetailCancelH, 8, 8);
        g.setColor(teamDetailCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.draw(cancelShape);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(new Color(0x47, 0x55, 0x69));
        g.drawString("Close", teamDetailCancelX + (teamDetailCancelW - g.getFontMetrics().stringWidth("Close")) / 2, teamDetailCancelY + 23);

        teamInviteBtnX = x + padX + teamDetailCancelW + 12;
        teamInviteBtnY = curY;
        teamInviteBtnW = innerW - teamDetailCancelW - 12;
        teamInviteBtnH = btnH;

        RoundRectangle2D invShape = new RoundRectangle2D.Float(teamInviteBtnX, teamInviteBtnY, teamInviteBtnW, teamInviteBtnH, 8, 8);
        g.setColor(selectedTeammate.invited ? new Color(0x10, 0xB9, 0x81) : (teamInviteHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE));
        g.fill(invShape);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        String invText = selectedTeammate.invited ? "Invitation Sent ✓" : "Send Team Invite  →";
        g.drawString(invText, teamInviteBtnX + (teamInviteBtnW - g.getFontMetrics().stringWidth(invText)) / 2, teamInviteBtnY + 23);
    }

    private void paintAllTasksModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        int padX = 26;
        int innerW = w - padX * 2;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("All Upcoming Deadlines & Tasks", x + padX, y + 36);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Click any task card to toggle complete / pending status.", x + padX, y + 54);

        int curY = y + 74;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 14;

        for (StudentDashboard.TaskItem t : dashboard.getTasks()) {
            RoundRectangle2D card = new RoundRectangle2D.Float(x + padX, curY, innerW, 46, 8, 8);
            g.setColor(t.completed ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
            g.fill(card);
            g.setColor(t.completed ? new Color(0xE2, 0xE8, 0xF0) : new Color(0xCB, 0xD5, 0xE1));
            g.draw(card);

            int chkX = x + padX + 12;
            int chkY = curY + 14;
            g.setColor(t.completed ? new Color(0x10, 0xB9, 0x81) : new Color(0x94, 0xA3, 0xB8));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.drawString(t.completed ? "✓" : "○", chkX, chkY + 12);

            g.setFont(DashboardTheme.FONT_BOLD_12);
            g.setColor(t.completed ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
            g.drawString(t.title, chkX + 22, curY + 20);

            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(t.subtitle, chkX + 22, curY + 36);

            curY += 54;
        }

        // Close Button
        curY = y + h - 54;
        tasksDoneBtnX = x + padX;
        tasksDoneBtnY = curY;
        tasksDoneBtnW = innerW;
        tasksDoneBtnH = 38;

        RoundRectangle2D doneBtn = new RoundRectangle2D.Float(tasksDoneBtnX, tasksDoneBtnY, tasksDoneBtnW, tasksDoneBtnH, 8, 8);
        g.setColor(tasksDoneHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(doneBtn);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(Color.WHITE);
        g.drawString("Done", tasksDoneBtnX + (tasksDoneBtnW - g.getFontMetrics().stringWidth("Done")) / 2, tasksDoneBtnY + 23);
    }

    private void paintMessagesModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        int padX = 26;
        int innerW = w - padX * 2;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Team Messages & Chat", x + padX, y + 36);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString("Conversations with your project teammates at UW.", x + padX, y + 54);

        int curY = y + 74;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, curY, x + w, curY);
        curY += 14;

        for (String msg : dashboard.getChatHistory()) {
            RoundRectangle2D bubble = new RoundRectangle2D.Float(x + padX, curY, innerW, 40, 8, 8);
            boolean isAlex = msg.startsWith("Alex Morgan");
            g.setColor(isAlex ? new Color(0xEE, 0xF2, 0xFF) : new Color(0xF8, 0xFA, 0xFC));
            g.fill(bubble);
            g.setColor(new Color(0xE2, 0xE8, 0xF0));
            g.draw(bubble);

            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(msg, x + padX + 12, curY + 24);
            curY += 48;
        }

        // Input Box
        curY = y + h - 60;
        msgInputX = x + padX;
        msgInputY = curY;
        msgInputW = innerW - 80;
        msgInputH = 38;

        RoundRectangle2D inputBox = new RoundRectangle2D.Float(msgInputX, msgInputY, msgInputW, msgInputH, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(inputBox);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.draw(inputBox);

        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(newMessageText.isEmpty() ? DashboardTheme.TEXT_LIGHT : DashboardTheme.TEXT_DARK);
        g.drawString(newMessageText.isEmpty() ? "Type your message..." : newMessageText, msgInputX + 12, msgInputY + 23);
        if (caretVisible) {
            int tw = newMessageText.isEmpty() ? 0 : g.getFontMetrics().stringWidth(newMessageText);
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawLine(msgInputX + 12 + tw + 2, msgInputY + 10, msgInputX + 12 + tw + 2, msgInputY + 28);
        }

        // Send Button
        msgSendBtnX = msgInputX + msgInputW + 10;
        msgSendBtnY = curY;
        msgSendBtnW = 70;
        msgSendBtnH = 38;

        RoundRectangle2D sendBtn = new RoundRectangle2D.Float(msgSendBtnX, msgSendBtnY, msgSendBtnW, msgSendBtnH, 8, 8);
        g.setColor(msgSendHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(sendBtn);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(Color.WHITE);
        g.drawString("Send", msgSendBtnX + (msgSendBtnW - g.getFontMetrics().stringWidth("Send")) / 2, msgSendBtnY + 23);
    }

    public void publishProjectFromForm(StudentDashboard dashboard) {
        String title = newProjTitle.trim().isEmpty() ? "AI Study Assistant" : newProjTitle.trim();
        String desc = newProjDesc.trim().isEmpty() ? "Collaborative student project building automated review tools." : newProjDesc.trim();
        String[] skills = newProjSkills.isEmpty() ? new String[]{"React", "Node.js"} : newProjSkills.toArray(new String[0]);
        String category = newProjCategory.isEmpty() ? "Sustainability" : newProjCategory;

        dashboard.getProjects().add(0, new StudentDashboard.ProjectItem(
                title, "Alex Morgan · " + category,
                desc, skills,
                "3 open roles · 1 / 4 members", true, 0
        ));
        dashboard.setActiveProjectsCount(dashboard.getActiveProjectsCount() + 1);
        closeModal(); // Instant close!
        newProjTitle = "";
        newProjDesc = "";
        dashboard.showToast("Project '" + title + "' published to student feed! 🎉");
    }

    public boolean onClick(int mx, int my, StudentDashboard dashboard) {
        if (activeModal == StudentDashboard.ActiveModal.NONE) return false;

        // 1. Close button (✕) clicked -> Instant Close
        if (mx >= modalCloseX - 6 && mx <= modalCloseX + modalCloseSize + 6 &&
                my >= modalCloseY - 6 && my <= modalCloseY + modalCloseSize + 6) {
            closeModal();
            return true;
        }

        // 2. CREATE_PROJECT Form
        if (activeModal == StudentDashboard.ActiveModal.CREATE_PROJECT) {
            // Cancel button
            if (mx >= projCancelBtnX && mx <= projCancelBtnX + projCancelBtnW &&
                    my >= projCancelBtnY && my <= projCancelBtnY + projCancelBtnH) {
                closeModal();
                return true;
            }

            // Submit button
            if (mx >= projSubmitBtnX && mx <= projSubmitBtnX + projSubmitBtnW &&
                    my >= projSubmitBtnY && my <= projSubmitBtnY + projSubmitBtnH) {
                publishProjectFromForm(dashboard);
                return true;
            }

            // Title input box focus
            if (mx >= projTitleX && mx <= projTitleX + projTitleW &&
                    my >= projTitleY && my <= projTitleY + projTitleH) {
                formFocusField = 0;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }

            // Description input box focus
            if (mx >= projDescX && mx <= projDescX + projDescW &&
                    my >= projDescY && my <= projDescY + projDescH) {
                formFocusField = 1;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }

            // Category pills
            for (StudentDashboard.PillHitbox p : categoryPills) {
                if (mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + p.h) {
                    newProjCategory = p.name;
                    return true;
                }
            }

            // Skill tag chips
            for (StudentDashboard.PillHitbox s : skillChips) {
                if (mx >= s.x && mx <= s.x + s.w && my >= s.y && my <= s.y + s.h) {
                    if (newProjSkills.contains(s.name)) {
                        newProjSkills.remove(s.name);
                    } else {
                        newProjSkills.add(s.name);
                    }
                    return true;
                }
            }
        }

        // 3. COMPLETE_PROFILE Form
        if (activeModal == StudentDashboard.ActiveModal.COMPLETE_PROFILE) {
            if (mx >= profCancelBtnX && mx <= profCancelBtnX + profCancelBtnW &&
                    my >= profCancelBtnY && my <= profCancelBtnY + profCancelBtnH) {
                closeModal();
                return true;
            }
            if (mx >= profSaveBtnX && mx <= profSaveBtnX + profSaveBtnW &&
                    my >= profSaveBtnY && my <= profSaveBtnY + profSaveBtnH) {
                dashboard.setProfileCompletePercent(100);
                closeModal();
                dashboard.showToast("Profile reached 100%! Ready to get discovered. 🚀");
                return true;
            }
            if (mx >= profNameX && mx <= profNameX + profNameW && my >= profNameY && my <= profNameY + profNameH) {
                profileFocusField = 0;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profBioX && mx <= profBioX + profBioW && my >= profBioY && my <= profBioY + profBioH) {
                profileFocusField = 1;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profPortX && mx <= profPortX + profPortW && my >= profPortY && my <= profPortY + profPortH) {
                profileFocusField = 2;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
        }

        // 4. PROJECT_DETAILS
        if (activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS && dashboard.getSelectedProject() != null) {
            if (mx >= projDetailCancelX && mx <= projDetailCancelX + projDetailCancelW &&
                    my >= projDetailCancelY && my <= projDetailCancelY + projDetailCancelH) {
                closeModal();
                return true;
            }
            if (mx >= projJoinBtnX && mx <= projJoinBtnX + projJoinBtnW &&
                    my >= projJoinBtnY && my <= projJoinBtnY + projJoinBtnH) {
                dashboard.setPendingRequestsCount(dashboard.getPendingRequestsCount() + 1);
                closeModal();
                dashboard.showToast("Request to join '" + dashboard.getSelectedProject().title + "' submitted! Pending: " + dashboard.getPendingRequestsCount());
                return true;
            }
        }

        // 5. TEAMMATE_DETAILS
        if (activeModal == StudentDashboard.ActiveModal.TEAMMATE_DETAILS && dashboard.getSelectedTeammate() != null) {
            if (mx >= teamDetailCancelX && mx <= teamDetailCancelX + teamDetailCancelW &&
                    my >= teamDetailCancelY && my <= teamDetailCancelY + teamDetailCancelH) {
                closeModal();
                return true;
            }
            if (mx >= teamInviteBtnX && mx <= teamInviteBtnX + teamInviteBtnW &&
                    my >= teamInviteBtnY && my <= teamInviteBtnY + teamInviteBtnH) {
                dashboard.getSelectedTeammate().invited = true;
                dashboard.setPendingRequestsCount(dashboard.getPendingRequestsCount() + 1);
                closeModal();
                dashboard.showToast("Invitation sent to " + dashboard.getSelectedTeammate().name + "!");
                return true;
            }
        }

        // 6. ALL_TASKS
        if (activeModal == StudentDashboard.ActiveModal.ALL_TASKS) {
            if (mx >= tasksDoneBtnX && mx <= tasksDoneBtnX + tasksDoneBtnW &&
                    my >= tasksDoneBtnY && my <= tasksDoneBtnY + tasksDoneBtnH) {
                closeModal();
                return true;
            }
            int tY = activeModalY + 74 + 14;
            for (StudentDashboard.TaskItem t : dashboard.getTasks()) {
                if (mx >= activeModalX + 26 && mx <= activeModalX + activeModalW - 26 && my >= tY && my <= tY + 46) {
                    t.completed = !t.completed;
                    if (t.completed) {
                        dashboard.setTasksDueCount(Math.max(0, dashboard.getTasksDueCount() - 1));
                        dashboard.showToast("Task completed: " + t.title + " ✓");
                    } else {
                        dashboard.setTasksDueCount(dashboard.getTasksDueCount() + 1);
                    }
                    return true;
                }
                tY += 54;
            }
        }

        // 7. MESSAGES
        if (activeModal == StudentDashboard.ActiveModal.MESSAGES) {
            if (mx >= msgInputX && mx <= msgInputX + msgInputW && my >= msgInputY && my <= msgInputY + msgInputH) {
                return true;
            }
            if (mx >= msgSendBtnX && mx <= msgSendBtnX + msgSendBtnW && my >= msgSendBtnY && my <= msgSendBtnY + msgSendBtnH) {
                if (!newMessageText.trim().isEmpty()) {
                    dashboard.getChatHistory().add("Alex Morgan: " + newMessageText.trim());
                    newMessageText = "";
                    dashboard.showToast("Message sent to team channel!");
                    return true;
                }
            }
        }

        // 8. Outside click closes modal immediately
        if (mx < activeModalX || mx > activeModalX + activeModalW || my < activeModalY || my > activeModalY + activeModalH) {
            closeModal();
            return true;
        }

        // Consume any other click inside modal so background is not clicked
        return true;
    }

    public boolean onMouseMove(int mx, int my) {
        if (activeModal == StudentDashboard.ActiveModal.NONE) return false;

        boolean changed = false;
        boolean prevCloseHov = modalCloseHovered;
        modalCloseHovered = (mx >= modalCloseX - 6 && mx <= modalCloseX + modalCloseSize + 6 &&
                my >= modalCloseY - 6 && my <= modalCloseY + modalCloseSize + 6);
        if (modalCloseHovered != prevCloseHov) changed = true;

        if (activeModal == StudentDashboard.ActiveModal.CREATE_PROJECT) {
            boolean prevCan = projCancelHovered;
            projCancelHovered = (mx >= projCancelBtnX && mx <= projCancelBtnX + projCancelBtnW &&
                    my >= projCancelBtnY && my <= projCancelBtnY + projCancelBtnH);
            if (projCancelHovered != prevCan) changed = true;

            boolean prevSub = projSubmitHovered;
            projSubmitHovered = (mx >= projSubmitBtnX && mx <= projSubmitBtnX + projSubmitBtnW &&
                    my >= projSubmitBtnY && my <= projSubmitBtnY + projSubmitBtnH);
            if (projSubmitHovered != prevSub) changed = true;

            boolean chipHov = false;
            for (StudentDashboard.PillHitbox p : categoryPills) {
                if (mx >= p.x && mx <= p.x + p.w && my >= p.y && my <= p.y + p.h) { chipHov = true; break; }
            }
            if (!chipHov) {
                for (StudentDashboard.PillHitbox s : skillChips) {
                    if (mx >= s.x && mx <= s.x + s.w && my >= s.y && my <= s.y + s.h) { chipHov = true; break; }
                }
            }
            return modalCloseHovered || projCancelHovered || projSubmitHovered || chipHov || changed;
        }

        if (activeModal == StudentDashboard.ActiveModal.COMPLETE_PROFILE) {
            boolean prevCan = profCancelHovered;
            profCancelHovered = (mx >= profCancelBtnX && mx <= profCancelBtnX + profCancelBtnW &&
                    my >= profCancelBtnY && my <= profCancelBtnY + profCancelBtnH);
            if (profCancelHovered != prevCan) changed = true;

            boolean prevSave = profSaveHovered;
            profSaveHovered = (mx >= profSaveBtnX && mx <= profSaveBtnX + profSaveBtnW &&
                    my >= profSaveBtnY && my <= profSaveBtnY + profSaveBtnH);
            if (profSaveHovered != prevSave) changed = true;

            return modalCloseHovered || profCancelHovered || profSaveHovered || changed;
        }

        if (activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS) {
            boolean prevCan = projDetailCancelHovered;
            projDetailCancelHovered = (mx >= projDetailCancelX && mx <= projDetailCancelX + projDetailCancelW &&
                    my >= projDetailCancelY && my <= projDetailCancelY + projDetailCancelH);
            if (projDetailCancelHovered != prevCan) changed = true;

            boolean prevJoin = projJoinHovered;
            projJoinHovered = (mx >= projJoinBtnX && mx <= projJoinBtnX + projJoinBtnW &&
                    my >= projJoinBtnY && my <= projJoinBtnY + projJoinBtnH);
            if (projJoinHovered != prevJoin) changed = true;

            return modalCloseHovered || projDetailCancelHovered || projJoinHovered || changed;
        }

        if (activeModal == StudentDashboard.ActiveModal.TEAMMATE_DETAILS) {
            boolean prevCan = teamDetailCancelHovered;
            teamDetailCancelHovered = (mx >= teamDetailCancelX && mx <= teamDetailCancelX + teamDetailCancelW &&
                    my >= teamDetailCancelY && my <= teamDetailCancelY + teamDetailCancelH);
            if (teamDetailCancelHovered != prevCan) changed = true;

            boolean prevInv = teamInviteHovered;
            teamInviteHovered = (mx >= teamInviteBtnX && mx <= teamInviteBtnX + teamInviteBtnW &&
                    my >= teamInviteBtnY && my <= teamInviteBtnY + teamInviteBtnH);
            if (teamInviteHovered != prevInv) changed = true;

            return modalCloseHovered || teamDetailCancelHovered || teamInviteHovered || changed;
        }

        if (activeModal == StudentDashboard.ActiveModal.ALL_TASKS) {
            boolean prevDone = tasksDoneHovered;
            tasksDoneHovered = (mx >= tasksDoneBtnX && mx <= tasksDoneBtnX + tasksDoneBtnW &&
                    my >= tasksDoneBtnY && my <= tasksDoneBtnY + tasksDoneBtnH);
            if (tasksDoneHovered != prevDone) changed = true;
            return modalCloseHovered || tasksDoneHovered || changed;
        }

        if (activeModal == StudentDashboard.ActiveModal.MESSAGES) {
            boolean prevSend = msgSendHovered;
            msgSendHovered = (mx >= msgSendBtnX && mx <= msgSendBtnX + msgSendBtnW &&
                    my >= msgSendBtnY && my <= msgSendBtnY + msgSendBtnH);
            if (msgSendHovered != prevSend) changed = true;
            return modalCloseHovered || msgSendHovered || changed;
        }

        return modalCloseHovered || changed;
    }

    public boolean onKey(KeyEvent e, StudentDashboard dashboard) {
        if (activeModal == StudentDashboard.ActiveModal.NONE) return false;

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            closeModal();
            return true;
        }

        // Typing into Create Project Modal
        if (activeModal == StudentDashboard.ActiveModal.CREATE_PROJECT) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                formFocusField = (formFocusField + 1) % 2;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (formFocusField == 0) {
                    formFocusField = 1;
                    caretBlinkTimer = 0f;
                    caretVisible = true;
                } else {
                    publishProjectFromForm(dashboard);
                }
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (formFocusField == 0 && newProjTitle.length() > 0) {
                    newProjTitle = newProjTitle.substring(0, newProjTitle.length() - 1);
                } else if (formFocusField == 1 && newProjDesc.length() > 0) {
                    newProjDesc = newProjDesc.substring(0, newProjDesc.length() - 1);
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126) {
                if (formFocusField == 0 && newProjTitle.length() < 40) {
                    newProjTitle += c;
                } else if (formFocusField == 1 && newProjDesc.length() < 120) {
                    newProjDesc += c;
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
        }

        // Typing into Profile Modal
        if (activeModal == StudentDashboard.ActiveModal.COMPLETE_PROFILE) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                profileFocusField = (profileFocusField + 1) % 3;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                dashboard.setProfileCompletePercent(100);
                closeModal();
                dashboard.showToast("Profile reached 100%! Ready to get discovered. 🚀");
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (profileFocusField == 0 && dashboard.getProfileName().length() > 0) {
                    dashboard.setProfileName(dashboard.getProfileName().substring(0, dashboard.getProfileName().length() - 1));
                } else if (profileFocusField == 1 && dashboard.getProfileBio().length() > 0) {
                    dashboard.setProfileBio(dashboard.getProfileBio().substring(0, dashboard.getProfileBio().length() - 1));
                } else if (profileFocusField == 2 && dashboard.getProfilePortfolio().length() > 0) {
                    dashboard.setProfilePortfolio(dashboard.getProfilePortfolio().substring(0, dashboard.getProfilePortfolio().length() - 1));
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126) {
                if (profileFocusField == 0 && dashboard.getProfileName().length() < 30) {
                    dashboard.setProfileName(dashboard.getProfileName() + c);
                } else if (profileFocusField == 1 && dashboard.getProfileBio().length() < 100) {
                    dashboard.setProfileBio(dashboard.getProfileBio() + c);
                } else if (profileFocusField == 2 && dashboard.getProfilePortfolio().length() < 50) {
                    dashboard.setProfilePortfolio(dashboard.getProfilePortfolio() + c);
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
        }

        // Typing into Messages Modal
        if (activeModal == StudentDashboard.ActiveModal.MESSAGES) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (newMessageText.length() > 0) {
                    newMessageText = newMessageText.substring(0, newMessageText.length() - 1);
                }
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER && !newMessageText.trim().isEmpty()) {
                dashboard.getChatHistory().add("Alex Morgan: " + newMessageText.trim());
                newMessageText = "";
                dashboard.showToast("Message sent to team channel!");
                return true;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c <= 126 && newMessageText.length() < 60) {
                newMessageText += c;
                return true;
            }
        }

        return false;
    }
}

