import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Automated test suite for Admin Categories & Skills modals and functionality.
 */
public class TestAdminCategoriesPage {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(boolean condition, String desc) {
        if (condition) {
            System.out.println("  ✓ " + desc);
            passed++;
        } else {
            System.err.println("  ✗ FAILED: " + desc);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADMIN CATEGORIES & SKILLS MODAL TESTS  ");
        System.out.println("==================================================\n");

        AdminDataStore store = AdminDataStore.getInstance();
        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });

        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // 1. Navigate to Skills / Categories
        dashboard.navigateTo(AdminSidebar.AdminNavItem.SKILLS);
        dashboard.paint(g, 1366, 768);
        check(true, "Navigated and painted Categories & Skills page cleanly");

        // 2. Open Add Category Modal
        System.out.println("\n[Test 2] Testing Add Category Modal Interaction...");
        dashboard.openAddCategoryModal();
        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.ADD_CATEGORY, "ADD_CATEGORY modal is active");
        dashboard.paint(g, 1366, 768);

        // Type input via keys
        dashboard.onKey('D', 0);
        dashboard.onKey('e', 0);
        dashboard.onKey('v', 0);
        dashboard.onKey('O', 0);
        dashboard.onKey('p', 0);
        dashboard.onKey('s', 0);
        check(dashboard.getModals().getInputName().equals("DevOps"), "Typed 'DevOps' into modal input: " + dashboard.getModals().getInputName());

        // Test Backspace
        dashboard.onKey('\0', java.awt.event.KeyEvent.VK_BACK_SPACE);
        check(dashboard.getModals().getInputName().equals("DevOp"), "Backspace removed last char: " + dashboard.getModals().getInputName());
        dashboard.onKey('s', 0);

        // Click Cancel button
        int modalW = Math.min(480, 1366 - 40);
        int modalH = 280;
        int modalX = (1366 - modalW) / 2;
        int modalY = (768 - modalH) / 2;
        int cancelBtnX = modalX + 28;
        int cancelBtnY = modalY + modalH - 54;

        dashboard.onClick(cancelBtnX + 10, cancelBtnY + 10);
        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.NONE, "Cancel button closed modal");
        check(store.getAllCategories().stream().noneMatch(c -> c.name.equals("DevOps")), "DevOps was not added on cancel");

        // 3. Open Add Category Modal and Submit
        System.out.println("\n[Test 3] Testing Create Category Submission...");
        dashboard.openAddCategoryModal();
        dashboard.getModals().setInputName("Robotics & IoT");
        dashboard.paint(g, 1366, 768);

        int saveBtnX = modalX + modalW - 28 - 140;
        int saveBtnY = modalY + modalH - 54;
        dashboard.onClick(saveBtnX + 10, saveBtnY + 10);

        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.NONE, "Modal closed after creating category");
        check(store.getAllCategories().stream().anyMatch(c -> c.name.equals("Robotics & IoT")), "New category 'Robotics & IoT' exists in AdminDataStore");
        check(store.getAllCategories().get(0).name.equals("Robotics & IoT"), "Category added at the top of the list");

        // 4. Duplicate Category Validation
        System.out.println("\n[Test 4] Testing Duplicate Category Prevention...");
        dashboard.openAddCategoryModal();
        dashboard.getModals().setInputName("Robotics & IoT");
        dashboard.onClick(saveBtnX + 10, saveBtnY + 10);
        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.ADD_CATEGORY, "Modal remains open on duplicate category error");
        dashboard.getModals().closeModal();

        // 5. Open Add Skill Modal
        System.out.println("\n[Test 5] Testing Add Skill Modal Interaction...");
        dashboard.openAddSkillModal();
        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.ADD_SKILL, "ADD_SKILL modal is active");
        dashboard.paint(g, 1366, 768);

        // Type via keyboard
        dashboard.onKey('K', 0);
        dashboard.onKey('u', 0);
        dashboard.onKey('b', 0);
        dashboard.onKey('e', 0);
        dashboard.onKey('r', 0);
        dashboard.onKey('n', 0);
        dashboard.onKey('e', 0);
        dashboard.onKey('t', 0);
        dashboard.onKey('e', 0);
        dashboard.onKey('s', 0);
        check(dashboard.getModals().getInputName().equals("Kubernetes"), "Typed 'Kubernetes' into modal input: " + dashboard.getModals().getInputName());

        // Submit via ENTER key
        dashboard.onKey('\n', java.awt.event.KeyEvent.VK_ENTER);
        check(dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.NONE, "Modal closed on Enter key press");
        check(store.getAllSkills().stream().anyMatch(s -> s.name.equals("Kubernetes")), "New skill 'Kubernetes' exists in AdminDataStore");
        check(store.getAllSkills().get(0).name.equals("Kubernetes"), "Skill added at the top of the list");

        // 6. Test Multi-Viewport Rendering for Modals
        System.out.println("\n[Test 6] Testing Multi-Viewport Modal Painting...");
        int[] viewports = {480, 800, 1024, 1366, 1920};
        for (int vw : viewports) {
            BufferedImage testImg = new BufferedImage(vw, 768, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = testImg.createGraphics();
            dashboard.openAddCategoryModal();
            dashboard.paint(tg, vw, 768);
            dashboard.getModals().closeModal();

            dashboard.openAddSkillModal();
            dashboard.paint(tg, vw, 768);
            dashboard.getModals().closeModal();
            tg.dispose();
            check(true, "Add Category & Add Skill painted cleanly at " + vw + "x768");
        }

        // 7. Test Dynamic "View all categories" Button
        System.out.println("\n[Test 7] Testing Dynamic View All Categories Button...");
        AdminCategories categoriesView = dashboard.getCategoriesView();
        int initialH = categoriesView.getComputedHeight(1366 - 230);
        check(!categoriesView.isShowAllCategories(), "Initial showAllCategories is false (compact view)");

        categoriesView.setShowAllCategories(true);
        int expandedCatH = categoriesView.getComputedHeight(1366 - 230);
        check(categoriesView.isShowAllCategories(), "showAllCategories toggled to true");
        check(expandedCatH >= initialH, "Computed height dynamically increases when categories are expanded (" + expandedCatH + "px >= " + initialH + "px)");

        dashboard.paint(g, 1366, 768);
        categoriesView.setShowAllCategories(false);
        check(!categoriesView.isShowAllCategories(), "showAllCategories collapsed back to compact view");

        // 8. Test Dynamic "View all skills" Button
        System.out.println("\n[Test 8] Testing Dynamic View All Skills Button...");
        check(!categoriesView.isShowAllSkills(), "Initial showAllSkills is false (compact view)");

        categoriesView.setShowAllSkills(true);
        int expandedSkillH = categoriesView.getComputedHeight(1366 - 230);
        check(categoriesView.isShowAllSkills(), "showAllSkills toggled to true");
        check(expandedSkillH >= initialH, "Computed height dynamically increases when skills are expanded (" + expandedSkillH + "px >= " + initialH + "px)");

        dashboard.paint(g, 1366, 768);
        categoriesView.setShowAllSkills(false);
        check(!categoriesView.isShowAllSkills(), "showAllSkills collapsed back to compact view");

        g.dispose();

        System.out.println("\n==================================================");
        if (failed == 0) {
            System.out.println("  ALL " + passed + " CATEGORY & SKILL MODAL TESTS PASSED 100%! ");
        } else {
            System.err.println("  " + failed + " TESTS FAILED out of " + (passed + failed));
        }
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
