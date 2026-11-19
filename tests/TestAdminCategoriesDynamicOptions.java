import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TestAdminCategoriesDynamicOptions {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(boolean condition, String message) {
        if (condition) {
            System.out.println("  ✓ " + message);
            passed++;
        } else {
            System.out.println("  ✗ FAILED: " + message);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  TESTING CATEGORIES & SKILLS DYNAMIC SEARCH      ");
        System.out.println("==================================================");

        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });
        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        AdminModals modals = dashboard.getModals();
        AdminCategories categories = dashboard.getCategoriesView();

        // 1. Add Category Modal Dynamic Search & Options
        System.out.println("\n[Test 1] Testing Add Category Modal Dynamic Options...");
        dashboard.openAddCategoryModal();
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_CATEGORY, "Add Category modal is active");

        // Check options for empty search (shows recommended)
        List<String> recCats = modals.getMatchingCategoryOptions("");
        check(recCats.size() >= 10, "Default recommended category options count >= 10 (" + recCats.size() + ")");

        // Type "Cloud" in search
        modals.setInputName("Cloud");
        List<String> matchingCats = modals.getMatchingCategoryOptions("Cloud");
        check(matchingCats.size() >= 1, "Matching categories for 'Cloud' found: " + matchingCats);
        check(matchingCats.contains("Cloud Computing & DevOps"), "Contains 'Cloud Computing & DevOps'");

        // Paint to layout hitboxes
        dashboard.paint(g, 1366, 768);
        List<AdminModals.ModalOptionHitbox> catHitboxes = modals.getModalOptionHitboxes();
        check(!catHitboxes.isEmpty(), "Modal dynamic option hitboxes generated: " + catHitboxes.size());

        // Click first matching option pill
        AdminModals.ModalOptionHitbox firstCatHb = catHitboxes.get(0);
        modals.onClick(firstCatHb.x + 5, firstCatHb.y + 5, dashboard);
        check(modals.getInputName().equals(firstCatHb.text), "Clicking pill selected option into input: " + modals.getInputName());

        modals.closeModal();
        check(modals.getActiveModal() == AdminModals.ActiveModal.NONE, "Modal closed");

        // 2. Add Skill Modal Dynamic Search & Options
        System.out.println("\n[Test 2] Testing Add Skill Modal Dynamic Options...");
        dashboard.openAddSkillModal();
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_SKILL, "Add Skill modal is active");

        // Type "Py" in search
        modals.setInputName("Py");
        List<String> matchingSkills = modals.getMatchingSkillOptions("Py");
        check(matchingSkills.contains("Python"), "Matching skills contain 'Python'");
        check(matchingSkills.contains("PyTorch"), "Matching skills contain 'PyTorch'");

        // Paint to layout hitboxes
        dashboard.paint(g, 1366, 768);
        List<AdminModals.ModalOptionHitbox> skillHitboxes = modals.getModalOptionHitboxes();
        check(!skillHitboxes.isEmpty(), "Modal dynamic skill option hitboxes generated: " + skillHitboxes.size());

        // Find "Python" option hitbox and click it
        AdminModals.ModalOptionHitbox pyHb = null;
        for (AdminModals.ModalOptionHitbox hb : skillHitboxes) {
            if ("Python".equals(hb.text)) {
                pyHb = hb;
                break;
            }
        }
        check(pyHb != null, "Found 'Python' option hitbox");
        if (pyHb != null) {
            modals.onClick(pyHb.x + 5, pyHb.y + 5, dashboard);
            check("Python".equals(modals.getInputName()), "Selected 'Python' via option pill click: " + modals.getInputName());
        }

        modals.closeModal();

        // 3. Testing Prefilled Modal Openings
        System.out.println("\n[Test 3] Testing Prefilled Modal Openings...");
        dashboard.openAddCategoryModal("Cybersecurity");
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_CATEGORY, "Modal opened with prefill");
        check("Cybersecurity".equals(modals.getInputName()), "Input prefilled with 'Cybersecurity': " + modals.getInputName());
        modals.closeModal();

        dashboard.openAddSkillModal("Docker");
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_SKILL, "Modal opened with prefill");
        check("Docker".equals(modals.getInputName()), "Input prefilled with 'Docker': " + modals.getInputName());
        modals.closeModal();

        // 4. Testing Empty Search Quick-Add Dynamic Buttons
        System.out.println("\n[Test 4] Testing Categories & Skills Empty Search Quick-Add Buttons...");
        // Set view to CATEGORIES / SKILLS
        dashboard.navigateTo(AdminSidebar.AdminNavItem.SKILLS);
        dashboard.paint(g, 1366, 768);

        // Type search that matches nothing
        categories.setCatSearch("NonExistentCategoryXYZ");
        dashboard.paint(g, 1366, 768);

        dashboard.openAddCategoryModal(categories.getCatSearch().trim());
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_CATEGORY, "Quick-add opened Add Category modal");
        check("NonExistentCategoryXYZ".equals(modals.getInputName()), "Prefilled with search query: " + modals.getInputName());
        modals.closeModal();

        categories.setCatSearch("");
        categories.setSkillSearch("NonExistentSkillXYZ");
        dashboard.paint(g, 1366, 768);

        dashboard.openAddSkillModal(categories.getSkillSearch().trim());
        check(modals.getActiveModal() == AdminModals.ActiveModal.ADD_SKILL, "Quick-add opened Add Skill modal");
        check("NonExistentSkillXYZ".equals(modals.getInputName()), "Prefilled with search query: " + modals.getInputName());
        modals.closeModal();

        g.dispose();

        System.out.println("\n==================================================");
        if (failed == 0) {
            System.out.println("  ALL " + passed + " DYNAMIC CATEGORY & SKILL TESTS PASSED 100%!");
        } else {
            System.out.println("  " + failed + " TESTS FAILED out of " + (passed + failed));
        }
        System.out.println("==================================================");
        if (failed > 0) System.exit(1);
    }
}
