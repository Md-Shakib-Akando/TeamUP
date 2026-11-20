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
    private String editName = "";
    private String editHeadline = "";
    private String editUniversity = "";
    private String editDepartment = "";
    private String editAvailability = "";
    private String editPortfolio = "";
    private String editBio = "";
    private final List<String> editSkills = new ArrayList<>();
    private int profileFocusField = 0; // 0: Name, 1: Headline, 2: Uni, 3: Dept, 4: Avail, 5: Port, 6: Bio
    private int profNameX, profNameY, profNameW, profNameH;
    private int profHeadX, profHeadY, profHeadW, profHeadH;
    private int profUniX, profUniY, profUniW, profUniH;
    private int profDeptX, profDeptY, profDeptW, profDeptH;
    private int profAvailX, profAvailY, profAvailW, profAvailH;
    private int profPortX, profPortY, profPortW, profPortH;
    private int profBioX, profBioY, profBioW, profBioH;
    private int profSaveBtnX, profSaveBtnY, profSaveBtnW, profSaveBtnH;
    private int profCancelBtnX, profCancelBtnY, profCancelBtnW, profCancelBtnH;
    private boolean profSaveHovered = false;
    private boolean profCancelHovered = false;
    private final List<StudentDashboard.PillHitbox> profSkillChips = new ArrayList<>();

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

    /** Where the Edit profile "Save changes" button was last painted — used by UI tests. */
    public Rectangle getProfileSaveBounds() {
        return new Rectangle(profSaveBtnX, profSaveBtnY, profSaveBtnW, profSaveBtnH);
    }

    public void open(StudentDashboard.ActiveModal modal) {
        this.activeModal = modal;
        this.modalAnim = 1.0f; // Instant open
    }

    public void initProfileForm(StudentDashboard dashboard) {
        ProfileData pd = dashboard.getProfileData();
        this.editName = dashboard.getProfileName() != null ? dashboard.getProfileName() : "";
        this.editHeadline = pd.headline != null ? pd.headline : "";
        this.editUniversity = (pd.university != null && !pd.university.equals("Add your university")) ? pd.university : "";
        this.editDepartment = pd.department != null ? pd.department : "";
        this.editAvailability = pd.availabilityStatus != null ? pd.availabilityStatus : "Available for new teams (10–15 hrs/wk)";
        this.editPortfolio = pd.portfolio != null ? pd.portfolio : (dashboard.getProfilePortfolio() != null ? dashboard.getProfilePortfolio() : "");
        this.editBio = pd.bio != null ? pd.bio : (dashboard.getProfileBio() != null ? dashboard.getProfileBio() : "");
        this.editSkills.clear();
        for (ProfileData.ProfileSkill s : pd.skills) {
            this.editSkills.add(s.name);
        }
        if (this.editSkills.isEmpty()) {
            this.editSkills.addAll(Arrays.asList("React", "Java", "Node.js"));
        }
        this.profileFocusField = 0;
        this.caretBlinkTimer = 0f;
        this.caretVisible = true;
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
        if (activeModal == StudentDashboard.ActiveModal.CREATE_PROJECT) {
            modalW = 620;
            modalH = CREATE_PROJECT_H;
        } else if (activeModal == StudentDashboard.ActiveModal.COMPLETE_PROFILE) {
            modalW = EDIT_PROFILE_W;
            modalH = EDIT_PROFILE_H;
        } else if (activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS
                || activeModal == StudentDashboard.ActiveModal.TEAMMATE_DETAILS) {
            modalW = Math.min(activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS ? 640 : 580, width - 32);
            modalH = measureDetailsHeight(g, modalW, dashboard); // sized to content, clamped below
        } else if (activeModal == StudentDashboard.ActiveModal.ALL_TASKS) {
            modalW = 520;
            modalH = 480;
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
            default:
                break;
        }
    }

    private static final int PROJ_TITLE_MAX = 40;
    private static final int PROJ_DESC_MAX = 120;
    private static final Color FIELD_LABEL = new Color(0x33, 0x41, 0x55);
    private static final Color FIELD_BG = new Color(0xF8, 0xFA, 0xFC);
    private static final Color FIELD_BORDER = new Color(0xE2, 0xE8, 0xF0);

    /** Natural height of the Create Project form (the modal is clamped to the screen). */
    private static final int CREATE_PROJECT_H = 640;

    private void paintCreateProjectModal(Graphics2D g, int x, int y, int w, int h) {
        int padX = 30;
        int innerW = w - padX * 2;

        // ---------- header
        int badge = 42;
        RoundRectangle2D badgeShape = new RoundRectangle2D.Float(x + padX, y + 22, badge, badge, 12, 12);
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fill(badgeShape);
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int bxMid = x + padX + badge / 2;
        int byMid = y + 22 + badge / 2;
        g.drawLine(bxMid - 7, byMid, bxMid + 7, byMid);
        g.drawLine(bxMid, byMid - 7, bxMid, byMid + 7);

        int headX = x + padX + badge + 14;
        int headMax = modalCloseX - 12 - headX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Create a new project", headX, y + 41);
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(fitText("Publish it so students with matching skills can find and join your team.",
                g.getFontMetrics(), headMax), headX, y + 60);

        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y + 84, x + w, y + 84);

        int curY = y + 108;

        // ---------- 1. title
        curY = paintFieldLabel(g, x + padX, curY, innerW, "Project title", true,
                newProjTitle.length() + " / " + PROJ_TITLE_MAX, newProjTitle.length() >= PROJ_TITLE_MAX);
        projTitleX = x + padX;
        projTitleY = curY;
        projTitleW = innerW;
        projTitleH = 44;
        boolean titleFocused = (formFocusField == 0);
        paintInputBox(g, projTitleX, projTitleY, projTitleW, projTitleH, titleFocused);
        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics tfm = g.getFontMetrics();
        int tx = projTitleX + 14;
        int tMax = projTitleW - 28;
        if (newProjTitle.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString(fitText("e.g. StudyBuddy AI, Campus Shuttle Tracker", tfm, tMax), tx, projTitleY + 27);
            if (titleFocused && caretVisible) drawCaret(g, tx, projTitleY + 13, projTitleY + 31);
        } else {
            String shown = newProjTitle;
            while (shown.length() > 1 && tfm.stringWidth(shown) > tMax - 4) shown = shown.substring(1);
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(shown, tx, projTitleY + 27);
            if (titleFocused && caretVisible) drawCaret(g, tx + tfm.stringWidth(shown) + 2, projTitleY + 13, projTitleY + 31);
        }
        curY += projTitleH + 22;

        // ---------- 2. category
        curY = paintFieldLabel(g, x + padX, curY, innerW, "Category", false, null, false);
        categoryPills.clear();
        String[] cats = {"Sustainability", "AI & Research", "Web & Mobile", "Design & UX"};
        int pillX = x + padX;
        int pillY = curY;
        int pillH = 34;
        g.setFont(DashboardTheme.FONT_BOLD_12);
        FontMetrics pfm = g.getFontMetrics();
        for (String cat : cats) {
            boolean sel = cat.equalsIgnoreCase(newProjCategory);
            int pillW = pfm.stringWidth(cat) + (sel ? 44 : 28);
            if (pillX + pillW > x + padX + innerW) {
                pillX = x + padX;
                pillY += pillH + 8;
            }
            categoryPills.add(new StudentDashboard.PillHitbox(cat, pillX, pillY, pillW, pillH));
            RoundRectangle2D pill = new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, pillH, pillH);
            g.setColor(sel ? DashboardTheme.PURPLE_TINT_BG : Color.WHITE);
            g.fill(pill);
            g.setColor(sel ? DashboardTheme.BRAND_PURPLE : FIELD_BORDER);
            g.setStroke(new BasicStroke(sel ? 1.5f : 1.1f));
            g.draw(pill);
            int lx = pillX + 14;
            if (sel) {
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(lx, pillY + 17, lx + 4, pillY + 21);
                g.drawLine(lx + 4, pillY + 21, lx + 11, pillY + 13);
                lx += 16;
            }
            g.setColor(sel ? DashboardTheme.BRAND_PURPLE : FIELD_LABEL);
            g.drawString(cat, lx, pillY + 22);
            pillX += pillW + 8;
        }
        curY = pillY + pillH + 22;

        // ---------- 3. description (wrapping textarea)
        curY = paintFieldLabel(g, x + padX, curY, innerW, "Short description", true,
                newProjDesc.length() + " / " + PROJ_DESC_MAX, newProjDesc.length() >= PROJ_DESC_MAX - 10);
        projDescX = x + padX;
        projDescY = curY;
        projDescW = innerW;
        projDescH = 100;
        boolean descFocused = (formFocusField == 1);
        paintInputBox(g, projDescX, projDescY, projDescW, projDescH, descFocused);

        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics dfm = g.getFontMetrics();
        int lineH = 20;
        int maxLines = (projDescH - 20) / lineH;
        int dx = projDescX + 14;
        int dMax = projDescW - 28;
        if (newProjDesc.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            int py = projDescY + 26;
            for (String line : hardWrap("What problem does it solve, what will you build, and who are you looking for?", dfm, dMax)) {
                g.drawString(line, dx, py);
                py += lineH;
            }
            if (descFocused && caretVisible) drawCaret(g, dx, projDescY + 12, projDescY + 30);
        } else {
            List<String> lines = hardWrap(newProjDesc, dfm, dMax);
            if (newProjDesc.endsWith(" ") && !lines.isEmpty()) {
                int last = lines.size() - 1;
                lines.set(last, lines.get(last) + " ");
            }
            int first = Math.max(0, lines.size() - maxLines); // keep the caret line visible
            int py = projDescY + 26;
            g.setColor(DashboardTheme.TEXT_DARK);
            for (int i = first; i < lines.size(); i++) {
                g.drawString(lines.get(i), dx, py);
                py += lineH;
            }
            if (descFocused && caretVisible) {
                String lastLine = lines.isEmpty() ? "" : lines.get(lines.size() - 1);
                int cy = projDescY + 26 + (lines.size() - 1 - first) * lineH;
                drawCaret(g, dx + dfm.stringWidth(lastLine) + 2, cy - 14, cy + 4);
            }
        }
        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("Tip: mention the problem, your plan, and the roles you need.", projDescX + 2, projDescY + projDescH + 18);
        curY += projDescH + 18 + 22;

        // ---------- 4. skills
        curY = paintFieldLabel(g, x + padX, curY, innerW, "Skills you need", false,
                newProjSkills.size() + " selected", false);
        skillChips.clear();
        String[] availableSkills = {"React", "Node.js", "Python", "UI/UX", "PostgreSQL", "Flutter", "AI / ML", "Figma"};
        int chipX = x + padX;
        int chipY = curY;
        int chipH = 30;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics cfm = g.getFontMetrics();
        for (String sk : availableSkills) {
            boolean selected = newProjSkills.contains(sk);
            String label = (selected ? "✓  " : "+  ") + sk;
            int chipW = cfm.stringWidth(label) + 24;
            if (chipX + chipW > x + padX + innerW) {
                chipX = x + padX;
                chipY += chipH + 8;
            }
            skillChips.add(new StudentDashboard.PillHitbox(sk, chipX, chipY, chipW, chipH));
            RoundRectangle2D chip = new RoundRectangle2D.Float(chipX, chipY, chipW, chipH, 10, 10);
            g.setColor(selected ? DashboardTheme.PURPLE_TINT_BG : FIELD_BG);
            g.fill(chip);
            g.setColor(selected ? DashboardTheme.BRAND_PURPLE : FIELD_BORDER);
            g.setStroke(new BasicStroke(selected ? 1.3f : 1f));
            g.draw(chip);
            g.setColor(selected ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
            g.drawString(label, chipX + 12, chipY + 19);
            chipX += chipW + 8;
        }

        // ---------- footer (pinned)
        int footY = y + h - 80;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(x + 1, footY, w - 2, 79, 20, 20));
        g.fillRect(x + 1, footY, w - 2, 30);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, footY, x + w, footY);

        int btnH = 42;
        int btnY = footY + (80 - btnH) / 2;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        FontMetrics bfm = g.getFontMetrics();
        String subText = "Publish project";
        projSubmitBtnW = bfm.stringWidth(subText) + 64;
        projSubmitBtnH = btnH;
        projSubmitBtnX = x + w - padX - projSubmitBtnW;
        projSubmitBtnY = btnY;
        projCancelBtnW = bfm.stringWidth("Cancel") + 44;
        projCancelBtnH = btnH;
        projCancelBtnX = projSubmitBtnX - 10 - projCancelBtnW;
        projCancelBtnY = btnY;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(projCancelBtnX, projCancelBtnY, projCancelBtnW, btnH, 10, 10);
        g.setColor(projCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1f));
        g.draw(cancelShape);
        g.setColor(FIELD_LABEL);
        g.drawString("Cancel", projCancelBtnX + (projCancelBtnW - bfm.stringWidth("Cancel")) / 2, btnY + 26);

        RoundRectangle2D submitShape = new RoundRectangle2D.Float(projSubmitBtnX, projSubmitBtnY, projSubmitBtnW, btnH, 10, 10);
        g.setColor(projSubmitHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(submitShape);
        g.setColor(Color.WHITE);
        int stx = projSubmitBtnX + (projSubmitBtnW - bfm.stringWidth(subText) - 18) / 2;
        g.drawString(subText, stx, btnY + 26);
        int ax = stx + bfm.stringWidth(subText) + 10;
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(ax, btnY + 21, ax + 9, btnY + 21);
        g.drawLine(ax + 5, btnY + 17, ax + 9, btnY + 21);
        g.drawLine(ax + 5, btnY + 25, ax + 9, btnY + 21);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        String hint = "Tab to switch fields · Enter to publish";
        if (x + padX + g.getFontMetrics().stringWidth(hint) < projCancelBtnX - 12) {
            g.drawString(hint, x + padX, btnY + 26);
        }
    }

    /** Field label (with optional required star) and a right-aligned helper/counter. Returns the y for the control. */
    private int paintFieldLabel(Graphics2D g, int x, int y, int w, String label, boolean required, String right, boolean warn) {
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(FIELD_LABEL);
        g.drawString(label, x, y + 12);
        if (required) {
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.drawString("*", x + g.getFontMetrics().stringWidth(label) + 4, y + 12);
        }
        if (right != null) {
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(warn ? new Color(0xD9, 0x77, 0x06) : DashboardTheme.TEXT_LIGHT);
            g.drawString(right, x + w - g.getFontMetrics().stringWidth(right), y + 12);
        }
        return y + 22;
    }

    private void paintInputBox(Graphics2D g, int x, int y, int w, int h, boolean focused) {
        if (focused) {
            g.setColor(new Color(0xEA, 0x4C, 0x89, 30));
            g.setStroke(new BasicStroke(4f));
            g.draw(new RoundRectangle2D.Float(x - 1, y - 1, w + 2, h + 2, 12, 12));
        }
        RoundRectangle2D box = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g.setColor(focused ? Color.WHITE : FIELD_BG);
        g.fill(box);
        g.setColor(focused ? DashboardTheme.BRAND_PURPLE : FIELD_BORDER);
        g.setStroke(new BasicStroke(focused ? 1.5f : 1.1f));
        g.draw(box);
    }

    private void drawCaret(Graphics2D g, int x, int y1, int y2) {
        g.setColor(DashboardTheme.BRAND_PURPLE);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(x, y1, x, y2);
    }

    private static String fitText(String s, FontMetrics fm, int maxW) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "…") > maxW) t = t.substring(0, t.length() - 1);
        return t.trim() + "…";
    }

    /** Word wrap that also breaks words longer than the line (e.g. URLs or keyboard mashing). */
    private static List<String> hardWrap(String text, FontMetrics fm, int maxW) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ", -1)) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxW) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }
            if (line.length() > 0) {
                out.add(line.toString());
                line.setLength(0);
            }
            String rest = word;
            while (fm.stringWidth(rest) > maxW && rest.length() > 1) {
                int cut = rest.length() - 1;
                while (cut > 1 && fm.stringWidth(rest.substring(0, cut)) > maxW) cut--;
                out.add(rest.substring(0, cut));
                rest = rest.substring(cut);
            }
            line.append(rest);
        }
        if (line.length() > 0 || out.isEmpty()) out.add(line.toString());
        return out;
    }

    /** Natural size of the Edit profile form (clamped to the screen by paint()). */
    private static final int EDIT_PROFILE_W = 700;
    private static final int EDIT_PROFILE_H = 680;
    private static final int[] PROFILE_FIELD_MAX = { 40, 60, 60, 60, 60, 60, 240 };

    /** Single-line input with placeholder, tail-visible text and caret. */
    private void paintTextField(Graphics2D g, int x, int y, int w, int h, String value, String placeholder, boolean focused) {
        paintInputBox(g, x, y, w, h, focused);
        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics fm = g.getFontMetrics();
        int tx = x + 14;
        int maxW = w - 28;
        int base = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        if (value == null || value.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            g.drawString(fitText(placeholder, fm, maxW), tx, base);
            if (focused && caretVisible) drawCaret(g, tx, y + 11, y + h - 11);
            return;
        }
        String shown = value;
        while (shown.length() > 1 && fm.stringWidth(shown) > maxW - 4) shown = shown.substring(1);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(shown, tx, base);
        if (focused && caretVisible) drawCaret(g, tx + fm.stringWidth(shown) + 2, y + 11, y + h - 11);
    }

    private void paintCompleteProfileModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        int padX = 30;
        int innerW = w - padX * 2;

        // ---------- header: live avatar preview that follows the name being typed
        String previewName = editName.trim().isEmpty() ? dashboard.getProfileName() : editName.trim();
        String ini = StudentDashboard.initialsFor(previewName);
        int av = 56;
        int avX = x + padX;
        int avY = y + 22;
        g.setColor(DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(avX, avY, av, av);
        g.setColor(new Color(0xF8, 0xBC, 0xD4));
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(avX, avY, av, av);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.BRAND_PURPLE);
        FontMetrics afm = g.getFontMetrics();
        g.drawString(ini, avX + (av - afm.stringWidth(ini)) / 2, avY + (av + afm.getAscent() - afm.getDescent()) / 2);

        int headX = avX + av + 16;
        int headMax = modalCloseX - 12 - headX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString("Edit profile", headX, y + 42);
        g.setFont(DashboardTheme.FONT_REG_12);
        g.setColor(DashboardTheme.TEXT_MUTED);
        String preview = previewName == null ? "" : previewName;
        if (!editHeadline.trim().isEmpty()) preview += "  ·  " + editHeadline.trim();
        g.drawString(fitText(preview, g.getFontMetrics(), headMax), headX, y + 62);

        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y + 96, x + w, y + 96);

        int colGap = 16;
        int colW = (innerW - colGap) / 2;
        int fieldH = 40;
        int curY = y + 116;
        int x1 = x + padX;
        int x2 = x1 + colW + colGap;

        // ---------- row 1: name + headline
        paintFieldLabel(g, x1, curY, colW, "Full name", true, editName.length() + " / " + PROFILE_FIELD_MAX[0], editName.length() >= PROFILE_FIELD_MAX[0]);
        paintFieldLabel(g, x2, curY, colW, "Headline", false, null, false);
        curY += 22;
        profNameX = x1; profNameY = curY; profNameW = colW; profNameH = fieldH;
        paintTextField(g, x1, curY, colW, fieldH, editName, "Your full name", profileFocusField == 0);
        profHeadX = x2; profHeadY = curY; profHeadW = colW; profHeadH = fieldH;
        paintTextField(g, x2, curY, colW, fieldH, editHeadline, "e.g. Full-stack developer", profileFocusField == 1);
        curY += fieldH + 18;

        // ---------- row 2: university + department
        paintFieldLabel(g, x1, curY, colW, "University", false, null, false);
        paintFieldLabel(g, x2, curY, colW, "Department / major", false, null, false);
        curY += 22;
        profUniX = x1; profUniY = curY; profUniW = colW; profUniH = fieldH;
        paintTextField(g, x1, curY, colW, fieldH, editUniversity, "e.g. University of Dhaka", profileFocusField == 2);
        profDeptX = x2; profDeptY = curY; profDeptW = colW; profDeptH = fieldH;
        paintTextField(g, x2, curY, colW, fieldH, editDepartment, "e.g. Computer Science", profileFocusField == 3);
        curY += fieldH + 18;

        // ---------- row 3: availability + portfolio
        paintFieldLabel(g, x1, curY, colW, "Availability", false, null, false);
        paintFieldLabel(g, x2, curY, colW, "Portfolio or GitHub", false, null, false);
        curY += 22;
        profAvailX = x1; profAvailY = curY; profAvailW = colW; profAvailH = fieldH;
        paintTextField(g, x1, curY, colW, fieldH, editAvailability, "e.g. Available · 10 hrs/week", profileFocusField == 4);
        profPortX = x2; profPortY = curY; profPortW = colW; profPortH = fieldH;
        paintTextField(g, x2, curY, colW, fieldH, editPortfolio, "github.com/username", profileFocusField == 5);
        curY += fieldH + 18;

        // ---------- bio (wrapping textarea, keeps the caret line visible)
        paintFieldLabel(g, x1, curY, innerW, "Bio", false, editBio.length() + " / " + PROFILE_FIELD_MAX[6], editBio.length() >= PROFILE_FIELD_MAX[6] - 20);
        curY += 22;
        profBioX = x1; profBioY = curY; profBioW = innerW; profBioH = 88;
        boolean bioFocused = profileFocusField == 6;
        paintInputBox(g, profBioX, profBioY, profBioW, profBioH, bioFocused);
        g.setFont(DashboardTheme.FONT_REG_13);
        FontMetrics bfm = g.getFontMetrics();
        int lineH = 20;
        int maxLines = (profBioH - 16) / lineH;
        int bx = profBioX + 14;
        if (editBio.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_LIGHT);
            int py = profBioY + 26;
            for (String line : hardWrap("A few lines about what you build, what you're learning, and the kind of team you want to join.", bfm, profBioW - 28)) {
                g.drawString(line, bx, py);
                py += lineH;
            }
            if (bioFocused && caretVisible) drawCaret(g, bx, profBioY + 12, profBioY + 30);
        } else {
            List<String> lines = hardWrap(editBio, bfm, profBioW - 28);
            if (editBio.endsWith(" ") && !lines.isEmpty()) {
                int last = lines.size() - 1;
                lines.set(last, lines.get(last) + " ");
            }
            int first = Math.max(0, lines.size() - maxLines);
            int py = profBioY + 26;
            g.setColor(DashboardTheme.TEXT_DARK);
            for (int i = first; i < lines.size(); i++) {
                g.drawString(lines.get(i), bx, py);
                py += lineH;
            }
            if (bioFocused && caretVisible) {
                String lastLine = lines.isEmpty() ? "" : lines.get(lines.size() - 1);
                int cy = profBioY + 26 + (lines.size() - 1 - first) * lineH;
                drawCaret(g, bx + bfm.stringWidth(lastLine) + 2, cy - 14, cy + 4);
            }
        }
        curY += profBioH + 18;

        // ---------- skills
        paintFieldLabel(g, x1, curY, innerW, "Skills", false, editSkills.size() + " selected", false);
        curY += 22;
        profSkillChips.clear();
        String[] availableSkills = {"Java", "React", "Python", "Node.js", "TypeScript", "UI/UX Design", "PostgreSQL", "Figma", "Spring Boot", "REST APIs"};
        int chipX = x1;
        int chipY = curY;
        int chipH = 30;
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics cfm = g.getFontMetrics();
        for (String sk : availableSkills) {
            boolean selected = editSkills.contains(sk);
            String label = (selected ? "✓  " : "+  ") + sk;
            int chipW = cfm.stringWidth(label) + 24;
            if (chipX + chipW > x1 + innerW) {
                chipX = x1;
                chipY += chipH + 8;
            }
            profSkillChips.add(new StudentDashboard.PillHitbox(sk, chipX, chipY, chipW, chipH));
            RoundRectangle2D chip = new RoundRectangle2D.Float(chipX, chipY, chipW, chipH, 10, 10);
            g.setColor(selected ? DashboardTheme.PURPLE_TINT_BG : FIELD_BG);
            g.fill(chip);
            g.setColor(selected ? DashboardTheme.BRAND_PURPLE : FIELD_BORDER);
            g.setStroke(new BasicStroke(selected ? 1.3f : 1f));
            g.draw(chip);
            g.setColor(selected ? DashboardTheme.BRAND_PURPLE : DashboardTheme.TEXT_MUTED);
            g.drawString(label, chipX + 12, chipY + 19);
            chipX += chipW + 8;
        }

        // ---------- footer (pinned)
        int footY = y + h - 80;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(x + 1, footY, w - 2, 79, 20, 20));
        g.fillRect(x + 1, footY, w - 2, 30);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.drawLine(x, footY, x + w, footY);

        int btnH = 42;
        int btnY = footY + (80 - btnH) / 2;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        FontMetrics btn = g.getFontMetrics();
        String saveText = "Save changes";
        profSaveBtnW = btn.stringWidth(saveText) + 56;
        profSaveBtnH = btnH;
        profSaveBtnX = x + w - padX - profSaveBtnW;
        profSaveBtnY = btnY;
        profCancelBtnW = btn.stringWidth("Cancel") + 44;
        profCancelBtnH = btnH;
        profCancelBtnX = profSaveBtnX - 10 - profCancelBtnW;
        profCancelBtnY = btnY;

        RoundRectangle2D cancelShape = new RoundRectangle2D.Float(profCancelBtnX, btnY, profCancelBtnW, btnH, 10, 10);
        g.setColor(profCancelHovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(cancelShape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1f));
        g.draw(cancelShape);
        g.setColor(FIELD_LABEL);
        g.drawString("Cancel", profCancelBtnX + (profCancelBtnW - btn.stringWidth("Cancel")) / 2, btnY + 26);

        RoundRectangle2D saveShape = new RoundRectangle2D.Float(profSaveBtnX, btnY, profSaveBtnW, btnH, 10, 10);
        g.setColor(profSaveHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
        g.fill(saveShape);
        g.setColor(Color.WHITE);
        g.drawString(saveText, profSaveBtnX + (profSaveBtnW - btn.stringWidth(saveText)) / 2, btnY + 26);

        g.setFont(DashboardTheme.FONT_REG_11);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        String hint = "Tab moves between fields · Enter saves from the bio";
        if (x + padX + g.getFontMetrics().stringWidth(hint) < profCancelBtnX - 12) {
            g.drawString(hint, x + padX, btnY + 26);
        }
    }

    // =========================================================================
    // PROJECT / TEAMMATE DETAILS
    // Both modals size themselves to their content: the body is laid out once on
    // a scratch canvas to measure it, then painted for real.
    // =========================================================================
    private static final int DETAILS_FOOTER_H = 80;

    /** Height a details modal needs for its content (clamped to the screen by the caller). */
    private int measureDetailsHeight(Graphics2D g, int w, StudentDashboard dashboard) {
        java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = scratch.createGraphics();
        mg.setRenderingHints(g.getRenderingHints());
        int bottom = activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS
                ? layoutProjectDetails(mg, 0, 0, w, dashboard.getSelectedProject(), 99)
                : layoutTeammateDetails(mg, 0, 0, w, dashboard.getSelectedTeammate(), 99);
        mg.dispose();
        return bottom + 8 + DETAILS_FOOTER_H;
    }

    /** Body lines allowed so the modal still fits on screen. */
    private int detailsBodyLines(Graphics2D g, int w, int maxH, StudentDashboard dashboard) {
        int lines = 12;
        while (lines > 3) {
            java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = scratch.createGraphics();
            mg.setRenderingHints(g.getRenderingHints());
            int bottom = activeModal == StudentDashboard.ActiveModal.PROJECT_DETAILS
                    ? layoutProjectDetails(mg, 0, 0, w, dashboard.getSelectedProject(), lines)
                    : layoutTeammateDetails(mg, 0, 0, w, dashboard.getSelectedTeammate(), lines);
            mg.dispose();
            if (bottom + 8 + DETAILS_FOOTER_H <= maxH) break;
            lines--;
        }
        return lines;
    }

    private void paintProjectDetailsModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        StudentDashboard.ProjectItem p = dashboard.getSelectedProject();
        if (p == null) return;
        layoutProjectDetails(g, x, y, w, p, detailsBodyLines(g, w, h, dashboard));

        // footer
        int footY = paintDetailsFooterBg(g, x, y, w, h);
        int padX = 30;
        int btnH = 42;
        int btnY = footY + (DETAILS_FOOTER_H - btnH) / 2;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        FontMetrics fm = g.getFontMetrics();
        String primary = p.isMember ? "✓  You're on this team" : (p.requested ? "✓  Request pending" : "Request to join");
        projJoinBtnW = fm.stringWidth(primary) + (p.isMember || p.requested ? 40 : 64);
        projJoinBtnH = btnH;
        projJoinBtnX = x + w - padX - projJoinBtnW;
        projJoinBtnY = btnY;
        projDetailCancelW = fm.stringWidth("Close") + 48;
        projDetailCancelH = btnH;
        projDetailCancelX = projJoinBtnX - 10 - projDetailCancelW;
        projDetailCancelY = btnY;
        paintSecondaryButton(g, projDetailCancelX, btnY, projDetailCancelW, btnH, "Close", projDetailCancelHovered);

        RoundRectangle2D join = new RoundRectangle2D.Float(projJoinBtnX, btnY, projJoinBtnW, btnH, 10, 10);
        if (p.isMember) {
            g.setColor(new Color(0xF1, 0xF5, 0xF9));
            g.fill(join);
            g.setColor(new Color(0x64, 0x74, 0x8B));
            g.drawString(primary, projJoinBtnX + (projJoinBtnW - fm.stringWidth(primary)) / 2, btnY + 26);
        } else if (p.requested) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(join);
            g.setColor(new Color(0x15, 0x80, 0x3D));
            g.drawString(primary, projJoinBtnX + (projJoinBtnW - fm.stringWidth(primary)) / 2, btnY + 26);
        } else {
            g.setColor(projJoinHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
            g.fill(join);
            g.setColor(Color.WHITE);
            int tx = projJoinBtnX + (projJoinBtnW - fm.stringWidth(primary) - 18) / 2;
            g.drawString(primary, tx, btnY + 26);
            drawArrow(g, tx + fm.stringWidth(primary) + 10, btnY + 21);
        }

        if (p.creatorName != null && !p.isMember && !p.requested) {
            g.setFont(DashboardTheme.FONT_REG_11);
            g.setColor(DashboardTheme.TEXT_LIGHT);
            String hint = fitText("The project lead reviews every request.", g.getFontMetrics(), projDetailCancelX - 12 - (x + padX));
            g.drawString(hint, x + padX, btnY + 26);
        }
    }

    /** Paints the project details body; returns the y where the body ends. */
    private int layoutProjectDetails(Graphics2D g, int x, int y, int w, StudentDashboard.ProjectItem p, int maxDescLines) {
        if (p == null) return y + 120;
        int padX = 30;
        int innerW = w - padX * 2;

        // Fallbacks for items opened from the overview (author = "Creator · Category")
        String creator = p.creatorName;
        String category = p.category;
        if ((creator == null || category == null) && p.author != null) {
            String[] parts = p.author.split("\\s*·\\s*", 2);
            if (creator == null) creator = parts[0].trim();
            if (category == null && parts.length > 1) category = parts[1].trim();
        }

        // ---- header: monogram tile, title (2 lines), pills
        int tile = 56;
        int tileX = x + padX;
        int tileY = y + 26;
        Color tileBg = p.iconBg != null ? p.iconBg : DashboardTheme.PURPLE_TINT_BG;
        Color tileFg = p.iconColor != null ? p.iconColor : DashboardTheme.BRAND_PURPLE;
        g.setColor(tileBg);
        g.fill(new RoundRectangle2D.Float(tileX, tileY, tile, tile, 14, 14));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(tileFg);
        String mono = p.title == null || p.title.trim().isEmpty() ? "P" : p.title.trim().substring(0, 1).toUpperCase();
        FontMetrics mfm = g.getFontMetrics();
        g.drawString(mono, tileX + (tile - mfm.stringWidth(mono)) / 2, tileY + (tile + mfm.getAscent() - mfm.getDescent()) / 2);

        int textX = tileX + tile + 16;
        int textW = x + w - padX - 44 - textX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.TEXT_DARK);
        int ty = y + 46;
        for (String line : wrapLimited(p.title, g.getFontMetrics(), textW, 2)) {
            g.drawString(line, textX, ty);
            ty += 24;
        }

        int pillY = ty - 8;
        int px = textX;
        boolean full = p.maxMembers > 0 && p.currentMembers >= p.maxMembers;
        boolean recruiting = p.isGreen && !full;
        px = paintPill(g, px, pillY, recruiting ? "● Recruiting" : "● Team full",
                recruiting ? new Color(0xDC, 0xFC, 0xE7) : new Color(0xF1, 0xF5, 0xF9),
                recruiting ? new Color(0x15, 0x80, 0x3D) : new Color(0x64, 0x74, 0x8B)) + 6;
        if (category != null && !category.isEmpty())
            px = paintPill(g, px, pillY, category, DashboardTheme.PURPLE_TINT_BG, DashboardTheme.BRAND_PURPLE) + 6;
        if (p.experience != null && !p.experience.isEmpty() && px + 90 < x + w - padX)
            paintPill(g, px, pillY, p.experience, new Color(0xF4, 0xF2, 0xEE), DashboardTheme.TEXT_MUTED);

        int curY = Math.max(tileY + tile, pillY + 24) + 22;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 22;

        // ---- stat tiles: team size, open spots, lead
        if (p.maxMembers > 0) {
            int gap = 12;
            int tileW = (innerW - gap * 2) / 3;
            int tileH = 74;
            int open = Math.max(0, p.maxMembers - p.currentMembers);
            paintStatTile(g, x + padX, curY, tileW, tileH, "TEAM", p.currentMembers + " / " + p.maxMembers,
                    Math.min(1f, p.currentMembers / (float) p.maxMembers));
            paintStatTile(g, x + padX + tileW + gap, curY, tileW, tileH, "OPEN SPOTS", open == 0 ? "None" : String.valueOf(open), -1f);
            paintLeadTile(g, x + padX + (tileW + gap) * 2, curY, tileW, tileH, creator, p.creatorInitials, p.creatorColor);
            curY += tileH + 24;
        } else if (p.footer != null && !p.footer.isEmpty()) {
            RoundRectangle2D box = new RoundRectangle2D.Float(x + padX, curY, innerW, 40, 10, 10);
            g.setColor(new Color(0xF8, 0xFA, 0xFC));
            g.fill(box);
            g.setColor(new Color(0xE2, 0xE8, 0xF0));
            g.draw(box);
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_MUTED);
            String info = (creator != null ? "Led by " + creator + "  ·  " : "") + p.footer;
            g.drawString(fitText(info, g.getFontMetrics(), innerW - 28), x + padX + 14, curY + 25);
            curY += 40 + 24;
        }

        // ---- about
        curY = paintSectionLabel(g, x + padX, curY, "About this project");
        g.setFont(DashboardTheme.FONT_REG_13);
        boolean hasDesc = p.desc != null && !p.desc.trim().isEmpty();
        g.setColor(hasDesc ? new Color(0x33, 0x41, 0x55) : DashboardTheme.TEXT_LIGHT);
        for (String line : wrapLimited(hasDesc ? p.desc : "The lead hasn't added a description yet.", g.getFontMetrics(), innerW, maxDescLines)) {
            g.drawString(line, x + padX, curY + 14);
            curY += 21;
        }
        curY += 20;

        // ---- roles the team is looking for ("3 open roles · Backend, Design")
        List<String> roles = new ArrayList<>();
        if (p.openRoles != null && p.openRoles.contains("·")) {
            for (String r : p.openRoles.substring(p.openRoles.indexOf('·') + 1).split(",")) {
                if (!r.trim().isEmpty()) roles.add(r.trim());
            }
        }
        if (!roles.isEmpty()) {
            curY = paintSectionLabel(g, x + padX, curY, "Looking for");
            curY = paintChipFlow(g, roles, x + padX, curY, innerW, new Color(0xFF, 0xFA, 0xEC), new Color(0xB4, 0x53, 0x09),
                    new Color(0xFF, 0xE8, 0xB3)) + 20;
        }

        // ---- skills
        if (p.skills != null && p.skills.length > 0) {
            curY = paintSectionLabel(g, x + padX, curY, "Skills needed");
            curY = paintChipFlow(g, Arrays.asList(p.skills), x + padX, curY, innerW, new Color(0xF8, 0xFA, 0xFC),
                    new Color(0x33, 0x41, 0x55), new Color(0xE2, 0xE8, 0xF0)) + 8;
        }
        return curY;
    }

    private void paintTeammateDetailsModal(Graphics2D g, int x, int y, int w, int h, StudentDashboard dashboard) {
        StudentDashboard.TeammateItem t = dashboard.getSelectedTeammate();
        if (t == null) return;
        layoutTeammateDetails(g, x, y, w, t, detailsBodyLines(g, w, h, dashboard));

        int footY = paintDetailsFooterBg(g, x, y, w, h);
        int padX = 30;
        int btnH = 42;
        int btnY = footY + (DETAILS_FOOTER_H - btnH) / 2;
        g.setFont(DashboardTheme.FONT_BOLD_13);
        FontMetrics fm = g.getFontMetrics();
        String primary = t.invited ? "✓  Invitation sent" : "Invite to team";
        teamInviteBtnW = fm.stringWidth(primary) + (t.invited ? 40 : 64);
        teamInviteBtnH = btnH;
        teamInviteBtnX = x + w - padX - teamInviteBtnW;
        teamInviteBtnY = btnY;
        teamDetailCancelW = fm.stringWidth("Close") + 48;
        teamDetailCancelH = btnH;
        teamDetailCancelX = teamInviteBtnX - 10 - teamDetailCancelW;
        teamDetailCancelY = btnY;
        paintSecondaryButton(g, teamDetailCancelX, btnY, teamDetailCancelW, btnH, "Close", teamDetailCancelHovered);

        RoundRectangle2D inv = new RoundRectangle2D.Float(teamInviteBtnX, btnY, teamInviteBtnW, btnH, 10, 10);
        if (t.invited) {
            g.setColor(new Color(0xDC, 0xFC, 0xE7));
            g.fill(inv);
            g.setColor(new Color(0x15, 0x80, 0x3D));
            g.drawString(primary, teamInviteBtnX + (teamInviteBtnW - fm.stringWidth(primary)) / 2, btnY + 26);
        } else {
            g.setColor(teamInviteHovered ? DashboardTheme.BRAND_PURPLE_HOVER : DashboardTheme.BRAND_PURPLE);
            g.fill(inv);
            g.setColor(Color.WHITE);
            int tx = teamInviteBtnX + (teamInviteBtnW - fm.stringWidth(primary) - 18) / 2;
            g.drawString(primary, tx, btnY + 26);
            drawArrow(g, tx + fm.stringWidth(primary) + 10, btnY + 21);
        }
    }

    /** Paints the teammate details body; returns the y where the body ends. */
    private int layoutTeammateDetails(Graphics2D g, int x, int y, int w, StudentDashboard.TeammateItem t, int maxBioLines) {
        if (t == null) return y + 120;
        int padX = 30;
        int innerW = w - padX * 2;

        int av = 68;
        int avX = x + padX;
        int avY = y + 26;
        g.setColor(t.avBg != null ? t.avBg : DashboardTheme.PURPLE_TINT_BG);
        g.fillOval(avX, avY, av, av);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(t.avText != null ? t.avText : DashboardTheme.BRAND_PURPLE);
        FontMetrics afm = g.getFontMetrics();
        String ini = t.initials == null ? "" : t.initials;
        g.drawString(ini, avX + (av - afm.stringWidth(ini)) / 2, avY + (av + afm.getAscent() - afm.getDescent()) / 2);

        int textX = avX + av + 18;
        int textW = x + w - padX - 44 - textX;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(fitText(t.name, g.getFontMetrics(), textW), textX, y + 50);

        int ty = y + 70;
        g.setFont(DashboardTheme.FONT_REG_13);
        if (t.role != null && !t.role.isEmpty()) {
            g.setColor(DashboardTheme.TEXT_DARK);
            g.drawString(fitText(t.role, g.getFontMetrics(), textW), textX, ty);
            ty += 19;
        }
        if (t.university != null && !t.university.isEmpty()) {
            g.setFont(DashboardTheme.FONT_REG_12);
            g.setColor(DashboardTheme.TEXT_MUTED);
            for (String line : wrapLimited(t.university, g.getFontMetrics(), textW, 2)) {
                g.drawString(line, textX, ty);
                ty += 17;
            }
        }
        int pillY = ty + 2;
        if (t.matchScore != null && !t.matchScore.isEmpty()) {
            String m = t.matchScore.toLowerCase().contains("match") ? t.matchScore : t.matchScore + " match";
            paintPill(g, textX, pillY, "✓ " + m, new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D));
            pillY += 24;
        }

        int curY = Math.max(avY + av, pillY) + 22;
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, curY, x + w, curY);
        curY += 22;

        curY = paintSectionLabel(g, x + padX, curY, "About");
        boolean hasBio = t.bio != null && !t.bio.trim().isEmpty();
        g.setFont(DashboardTheme.FONT_REG_13);
        g.setColor(hasBio ? new Color(0x33, 0x41, 0x55) : DashboardTheme.TEXT_LIGHT);
        for (String line : wrapLimited(hasBio ? t.bio : "This student hasn't written a bio yet.", g.getFontMetrics(), innerW, maxBioLines)) {
            g.drawString(line, x + padX, curY + 14);
            curY += 21;
        }
        return curY + 12;
    }

    // ---- shared pieces for the details modals

    private int paintDetailsFooterBg(Graphics2D g, int x, int y, int w, int h) {
        int footY = y + h - DETAILS_FOOTER_H;
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(new RoundRectangle2D.Float(x + 1, footY, w - 2, DETAILS_FOOTER_H - 1, 20, 20));
        g.fillRect(x + 1, footY, w - 2, 30);
        g.setColor(new Color(0xF1, 0xF5, 0xF9));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, footY, x + w, footY);
        return footY;
    }

    private void paintSecondaryButton(Graphics2D g, int x, int y, int w, int h, String label, boolean hovered) {
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g.setColor(hovered ? new Color(0xF1, 0xF5, 0xF9) : Color.WHITE);
        g.fill(shape);
        g.setColor(new Color(0xCB, 0xD5, 0xE1));
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        g.setFont(DashboardTheme.FONT_BOLD_13);
        g.setColor(FIELD_LABEL);
        g.drawString(label, x + (w - g.getFontMetrics().stringWidth(label)) / 2, y + 26);
    }

    private void drawArrow(Graphics2D g, int ax, int ay) {
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(ax, ay, ax + 9, ay);
        g.drawLine(ax + 5, ay - 4, ax + 9, ay);
        g.drawLine(ax + 5, ay + 4, ax + 9, ay);
        g.setStroke(new BasicStroke(1f));
    }

    /** Rounded pill; returns its right edge. */
    private int paintPill(Graphics2D g, int x, int y, String text, Color bg, Color fg) {
        g.setFont(DashboardTheme.FONT_BOLD_11);
        int pw = g.getFontMetrics().stringWidth(text) + 20;
        g.setColor(bg);
        g.fill(new RoundRectangle2D.Float(x, y, pw, 24, 24, 24));
        g.setColor(fg);
        g.drawString(text, x + 10, y + 16);
        return x + pw;
    }

    private int paintSectionLabel(Graphics2D g, int x, int y, String label) {
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(FIELD_LABEL);
        g.drawString(label, x, y + 12);
        return y + 24;
    }

    /** Wrapping chip layout; returns the bottom y. */
    private int paintChipFlow(Graphics2D g, List<String> items, int x, int y, int maxW, Color bg, Color fg, Color border) {
        g.setFont(DashboardTheme.FONT_BOLD_11);
        FontMetrics fm = g.getFontMetrics();
        int cx = x;
        int cy = y;
        int h = 28;
        for (String s : items) {
            String label = fitText(s, fm, maxW - 24);
            int cw = fm.stringWidth(label) + 24;
            if (cx > x && cx + cw > x + maxW) {
                cx = x;
                cy += h + 8;
            }
            RoundRectangle2D chip = new RoundRectangle2D.Float(cx, cy, cw, h, 10, 10);
            g.setColor(bg);
            g.fill(chip);
            g.setColor(border);
            g.setStroke(new BasicStroke(1f));
            g.draw(chip);
            g.setColor(fg);
            g.drawString(label, cx + 12, cy + 18);
            cx += cw + 8;
        }
        return cy + h;
    }

    private void paintStatTile(Graphics2D g, int x, int y, int w, int h, String label, String value, float fill) {
        RoundRectangle2D tile = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(tile);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(1f));
        g.draw(tile);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString(label, x + 14, y + 22);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(value, x + 14, y + 48);
        if (fill >= 0f) {
            int bw = w - 28;
            g.setColor(new Color(0xE2, 0xE8, 0xF0));
            g.fill(new RoundRectangle2D.Float(x + 14, y + 58, bw, 5, 5, 5));
            g.setColor(DashboardTheme.BRAND_PURPLE);
            g.fill(new RoundRectangle2D.Float(x + 14, y + 58, Math.max(5, Math.round(bw * fill)), 5, 5, 5));
        }
    }

    private void paintLeadTile(Graphics2D g, int x, int y, int w, int h, String name, String initials, Color color) {
        RoundRectangle2D tile = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(new Color(0xF8, 0xFA, 0xFC));
        g.fill(tile);
        g.setColor(new Color(0xE2, 0xE8, 0xF0));
        g.setStroke(new BasicStroke(1f));
        g.draw(tile);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(DashboardTheme.TEXT_LIGHT);
        g.drawString("PROJECT LEAD", x + 14, y + 22);
        int av = 26;
        g.setColor(color != null ? color : DashboardTheme.BRAND_PURPLE);
        g.fillOval(x + 14, y + 32, av, av);
        g.setFont(DashboardTheme.FONT_BOLD_10);
        g.setColor(Color.WHITE);
        String ini = initials != null ? initials : (name != null && name.length() > 0 ? name.substring(0, 1) : "?");
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ini, x + 14 + (av - fm.stringWidth(ini)) / 2, y + 32 + (av + fm.getAscent() - fm.getDescent()) / 2);
        g.setFont(DashboardTheme.FONT_BOLD_12);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(fitText(name == null ? "Unknown" : name, g.getFontMetrics(), w - 14 - av - 10 - 12), x + 14 + av + 10, y + 50);
    }

    /** Word wrap capped at maxLines; the last line is ellipsized when text remains. */
    private static List<String> wrapLimited(String text, FontMetrics fm, int maxW, int maxLines) {
        List<String> out = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return out;
        List<String> all = hardWrap(text.trim(), fm, maxW);
        for (int i = 0; i < all.size() && i < maxLines; i++) {
            String line = all.get(i);
            if (i == maxLines - 1 && all.size() > maxLines) line = line + " " + all.get(i + 1);
            out.add(fitText(line, fm, maxW));
        }
        return out;
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

    public void publishProjectFromForm(StudentDashboard dashboard) {
        String title = newProjTitle.trim().isEmpty() ? "AI Study Assistant" : newProjTitle.trim();
        String desc = newProjDesc.trim().isEmpty() ? "Collaborative student project building automated review tools." : newProjDesc.trim();
        String[] skills = newProjSkills.isEmpty() ? new String[]{"React", "Node.js"} : newProjSkills.toArray(new String[0]);
        String category = newProjCategory.isEmpty() ? "Sustainability" : newProjCategory;

        if (DashboardSync.isOnline()) {
            // Persist to MongoDB; the dashboard reloads when the server confirms
            DashboardSync.createProject(dashboard, title, desc, category, skills);
            closeModal();
            newProjTitle = "";
            newProjDesc = "";
            dashboard.showToast("Publishing '" + title + "'...");
            return;
        }

        dashboard.getProjects().add(0, new StudentDashboard.ProjectItem(
                title, dashboard.getProfileName() + " · " + category,
                desc, skills,
                "3 open roles · 1 / 4 members", true, 0
        ));
        dashboard.setActiveProjectsCount(dashboard.getActiveProjectsCount() + 1);
        closeModal(); // Instant close!
        newProjTitle = "";
        newProjDesc = "";
        dashboard.showToast("Project '" + title + "' published to student feed! 🎉");
    }

    /** Save button / Enter in the Complete Profile modal. */
    private void saveProfileForm(StudentDashboard dashboard) {
        String finalName = editName.trim().isEmpty() ? dashboard.getProfileName() : editName.trim();
        dashboard.setProfileName(finalName);
        dashboard.setProfileBio(editBio.trim());
        dashboard.setProfilePortfolio(editPortfolio.trim());

        ProfileData pd = dashboard.getProfileData();
        pd.name = finalName;
        pd.headline = editHeadline.trim();
        pd.university = editUniversity.trim();
        pd.department = editDepartment.trim();
        pd.availabilityStatus = editAvailability.trim();
        pd.portfolio = editPortfolio.trim();
        pd.bio = editBio.trim();

        // Sync skills
        List<ProfileData.ProfileSkill> updatedSkills = new ArrayList<>();
        for (String sk : editSkills) {
            boolean found = false;
            for (ProfileData.ProfileSkill existing : pd.skills) {
                if (existing.name.equalsIgnoreCase(sk)) {
                    updatedSkills.add(existing);
                    found = true;
                    break;
                }
            }
            if (!found) {
                updatedSkills.add(new ProfileData.ProfileSkill(sk, "Skills", 1, true));
            }
        }
        pd.skills.clear();
        pd.skills.addAll(updatedSkills);

        SettingsData sd = dashboard.getSettingsData();
        if (sd != null) {
            sd.fullName = finalName;
            sd.originalFullName = finalName;
        }

        dashboard.setProfileCompletePercent(100);
        closeModal();

        if (DashboardSync.isOnline()) {
            DashboardSync.saveProfile(dashboard, pd);
            dashboard.showToast("Profile saved & synced to MongoDB Atlas! 🚀");
        } else {
            dashboard.showToast("Profile updated successfully! (100% complete) 🚀");
        }
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
                saveProfileForm(dashboard);
                return true;
            }
            if (mx >= profNameX && mx <= profNameX + profNameW && my >= profNameY && my <= profNameY + profNameH) {
                profileFocusField = 0;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profHeadX && mx <= profHeadX + profHeadW && my >= profHeadY && my <= profHeadY + profHeadH) {
                profileFocusField = 1;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profUniX && mx <= profUniX + profUniW && my >= profUniY && my <= profUniY + profUniH) {
                profileFocusField = 2;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profDeptX && mx <= profDeptX + profDeptW && my >= profDeptY && my <= profDeptY + profDeptH) {
                profileFocusField = 3;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profAvailX && mx <= profAvailX + profAvailW && my >= profAvailY && my <= profAvailY + profAvailH) {
                profileFocusField = 4;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profPortX && mx <= profPortX + profPortW && my >= profPortY && my <= profPortY + profPortH) {
                profileFocusField = 5;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (mx >= profBioX && mx <= profBioX + profBioW && my >= profBioY && my <= profBioY + profBioH) {
                profileFocusField = 6;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            for (StudentDashboard.PillHitbox s : profSkillChips) {
                if (mx >= s.x && mx <= s.x + s.w && my >= s.y && my <= s.y + s.h) {
                    if (editSkills.contains(s.name)) {
                        editSkills.remove(s.name);
                    } else {
                        editSkills.add(s.name);
                    }
                    return true;
                }
            }
            return true;
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
                StudentDashboard.ProjectItem sel = dashboard.getSelectedProject();
                if (sel.isMember) {
                    closeModal();
                    dashboard.showToast("You're already a member of '" + sel.title + "'.");
                    return true;
                }
                if (sel.requested) {
                    closeModal();
                    dashboard.showToast("Your request to join '" + sel.title + "' is already pending.");
                    return true;
                }
                sel.requested = true;
                if (sel.sourceCard != null) sel.sourceCard.requested = true; // keep the Discover card in sync
                DashboardSync.requestJoin(dashboard, sel.remoteId);
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
                if (dashboard.getSelectedTeammate().invited) {
                    closeModal();
                    dashboard.showToast("You've already invited " + dashboard.getSelectedTeammate().name + ".");
                    return true;
                }
                dashboard.getSelectedTeammate().invited = true;
                DashboardSync.setInvite(dashboard, dashboard.getSelectedTeammate().remoteId, true);
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
                    DashboardSync.setTaskDone(dashboard, t.remoteId, t.completed);
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

        // 7. Outside click closes modal immediately
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

            boolean chipHov = false;
            for (StudentDashboard.PillHitbox s : profSkillChips) {
                if (mx >= s.x && mx <= s.x + s.w && my >= s.y && my <= s.y + s.h) { chipHov = true; break; }
            }

            return modalCloseHovered || profCancelHovered || profSaveHovered || chipHov || changed;
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
                profileFocusField = (profileFocusField + 1) % 7;
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (profileFocusField == 6) {
                    saveProfileForm(dashboard);
                } else {
                    profileFocusField = (profileFocusField + 1) % 7;
                    caretBlinkTimer = 0f;
                    caretVisible = true;
                }
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                switch (profileFocusField) {
                    case 0: if (editName.length() > 0) editName = editName.substring(0, editName.length() - 1); break;
                    case 1: if (editHeadline.length() > 0) editHeadline = editHeadline.substring(0, editHeadline.length() - 1); break;
                    case 2: if (editUniversity.length() > 0) editUniversity = editUniversity.substring(0, editUniversity.length() - 1); break;
                    case 3: if (editDepartment.length() > 0) editDepartment = editDepartment.substring(0, editDepartment.length() - 1); break;
                    case 4: if (editAvailability.length() > 0) editAvailability = editAvailability.substring(0, editAvailability.length() - 1); break;
                    case 5: if (editPortfolio.length() > 0) editPortfolio = editPortfolio.substring(0, editPortfolio.length() - 1); break;
                    case 6: if (editBio.length() > 0) editBio = editBio.substring(0, editBio.length() - 1); break;
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
            int kc = e.getKeyCode();
            if (kc == KeyEvent.VK_SHIFT || kc == KeyEvent.VK_CONTROL || kc == KeyEvent.VK_ALT ||
                kc == KeyEvent.VK_CAPS_LOCK || kc == KeyEvent.VK_META || kc == KeyEvent.VK_WINDOWS ||
                kc == KeyEvent.VK_LEFT || kc == KeyEvent.VK_RIGHT || kc == KeyEvent.VK_UP || kc == KeyEvent.VK_DOWN) {
                return false;
            }
            char c = e.getKeyChar();
            if (c >= 32 && c != 127) {
                switch (profileFocusField) {
                    case 0: if (editName.length() < 40) editName += c; break;
                    case 1: if (editHeadline.length() < 60) editHeadline += c; break;
                    case 2: if (editUniversity.length() < 60) editUniversity += c; break;
                    case 3: if (editDepartment.length() < 60) editDepartment += c; break;
                    case 4: if (editAvailability.length() < 60) editAvailability += c; break;
                    case 5: if (editPortfolio.length() < 60) editPortfolio += c; break;
                    case 6: if (editBio.length() < 240) editBio += c; break;
                }
                caretBlinkTimer = 0f;
                caretVisible = true;
                return true;
            }
        }

        return false;
    }
}

