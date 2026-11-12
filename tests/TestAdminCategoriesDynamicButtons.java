import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Automated test suite verifying that all buttons on Admin Categories & Skills page are fully dynamic:
 * 1. Add Category & Add Skill primary buttons (hover & modal trigger).
 * 2. Category & Skill row 'Edit' buttons (hitbox, hover, toast feedback).
 * 3. Category & Skill row 'Disable' / 'Enable' toggle buttons (hitbox, hover, AdminDataStore persistence).
 * 4. Search bar Clear ('✕') buttons for both category and skill search bars.
 * 5. Empty state 'Reset search' buttons for both columns.
 * 6. 'View all' / 'Show less' expanding toggle buttons with dynamic counts.
 */
public class TestAdminCategoriesDynamicButtons {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING CATEGORIES & SKILLS DYNAMIC BUTTONS TEST");
        System.out.println("==================================================");

        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });

        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Draw.setQualityHints(g);

        dashboard.navigateTo(AdminSidebar.AdminNavItem.SKILLS);
        dashboard.paint(g, 1366, 768);

        AdminCategories view = dashboard.getCategoriesView();
        if (view == null) {
            throw new AssertionError("dashboard.getCategoriesView() returned null!");
        }

        // ----------------------------------------------------
        // Test 1: Add Category & Add Skill Primary Buttons
        // ----------------------------------------------------
        System.out.println("\n[Test 1] Testing '+ Add category' and '+ Add skill' dynamic buttons...");
        int addCatX = view.getAddCatBtnX();
        int addCatY = view.getAddCatBtnY();
        dashboard.onMouseMove(addCatX + 10, addCatY + 10);
        if (!view.isAddCatHovered()) {
            throw new AssertionError("Add category button failed to register hover!");
        }
        dashboard.onClick(addCatX + 10, addCatY + 10);
        if (dashboard.getModals().getActiveModal() != AdminModals.ActiveModal.ADD_CATEGORY) {
            throw new AssertionError("Clicking Add category did not open ADD_CATEGORY modal!");
        }
        dashboard.closeModal();
        System.out.println("  ✓ '+ Add category' button hover & click verified.");

        int addSkillX = view.getAddSkillBtnX();
        int addSkillY = view.getAddSkillBtnY();
        dashboard.onMouseMove(addSkillX + 10, addSkillY + 10);
        if (!view.isAddSkillHovered()) {
            throw new AssertionError("Add skill button failed to register hover!");
        }
        dashboard.onClick(addSkillX + 10, addSkillY + 10);
        if (dashboard.getModals().getActiveModal() != AdminModals.ActiveModal.ADD_SKILL) {
            throw new AssertionError("Clicking Add skill did not open ADD_SKILL modal!");
        }
        dashboard.closeModal();
        System.out.println("  ✓ '+ Add skill' button hover & click verified.");

        // ----------------------------------------------------
        // Test 2: Category Row Action Buttons (Edit & Disable/Enable)
        // ----------------------------------------------------
        System.out.println("\n[Test 2] Testing Category row action buttons (Edit & Disable/Enable)...");
        dashboard.paint(g, 1366, 768);
        List<AdminCategories.CatHitbox> catBoxes = view.getCatHitboxes();
        if (catBoxes.isEmpty()) {
            throw new AssertionError("Expected at least 1 category hitbox!");
        }
        AdminCategories.CatHitbox firstCat = catBoxes.get(0);
        String catName = firstCat.model.name;
        boolean initialCatActive = firstCat.model.active;

        // Edit button hover & click
        dashboard.onMouseMove(firstCat.editX + 5, firstCat.editY + 5);
        if (!firstCat.editHovered) {
            throw new AssertionError("Category Edit button failed to register hover!");
        }
        boolean editHandled = dashboard.onClick(firstCat.editX + 5, firstCat.editY + 5);
        if (!editHandled) {
            throw new AssertionError("Category Edit button click not handled!");
        }
        if (dashboard.getModals().getActiveModal() != AdminModals.ActiveModal.EDIT_CATEGORY) {
            throw new AssertionError("Category Edit button did not open EDIT_CATEGORY modal!");
        }
        dashboard.closeModal();
        System.out.println("  ✓ Category 'Edit' button hover & click verified for '" + catName + "'.");

        // Toggle button hover & click (Enable/Disable)
        dashboard.onMouseMove(firstCat.toggleX + 5, firstCat.toggleY + 5);
        if (!firstCat.toggleHovered) {
            throw new AssertionError("Category Toggle button failed to register hover!");
        }
        dashboard.onClick(firstCat.toggleX + 5, firstCat.toggleY + 5);
        if (firstCat.model.active == initialCatActive) {
            throw new AssertionError("Category toggle failed to mutate store active state!");
        }
        System.out.println("  ✓ Category 'Disable/Enable' button dynamically toggled active to " + firstCat.model.active);
        // Toggle back to initial
        dashboard.onClick(firstCat.toggleX + 5, firstCat.toggleY + 5);
        if (firstCat.model.active != initialCatActive) {
            throw new AssertionError("Category toggle failed to restore original active state!");
        }
        System.out.println("  ✓ Category active state restored to " + initialCatActive);

        // ----------------------------------------------------
        // Test 3: Skill Row Action Buttons (Edit & Disable/Enable)
        // ----------------------------------------------------
        System.out.println("\n[Test 3] Testing Skill row action buttons (Edit & Disable/Enable)...");
        dashboard.paint(g, 1366, 768);
        List<AdminCategories.SkillHitbox> skillBoxes = view.getSkillHitboxes();
        if (skillBoxes.isEmpty()) {
            throw new AssertionError("Expected at least 1 skill hitbox!");
        }
        AdminCategories.SkillHitbox firstSkill = skillBoxes.get(0);
        String skillName = firstSkill.model.name;
        boolean initialSkillActive = firstSkill.model.active;

        // Edit button hover & click
        dashboard.onMouseMove(firstSkill.editX + 5, firstSkill.editY + 5);
        if (!firstSkill.editHovered) {
            throw new AssertionError("Skill Edit button failed to register hover!");
        }
        boolean sEditHandled = dashboard.onClick(firstSkill.editX + 5, firstSkill.editY + 5);
        if (!sEditHandled) {
            throw new AssertionError("Skill Edit button click not handled!");
        }
        if (dashboard.getModals().getActiveModal() != AdminModals.ActiveModal.EDIT_SKILL) {
            throw new AssertionError("Skill Edit button did not open EDIT_SKILL modal!");
        }
        dashboard.closeModal();
        System.out.println("  ✓ Skill 'Edit' button hover & click verified for '" + skillName + "'.");

        // Toggle button hover & click
        dashboard.onMouseMove(firstSkill.toggleX + 5, firstSkill.toggleY + 5);
        if (!firstSkill.toggleHovered) {
            throw new AssertionError("Skill Toggle button failed to register hover!");
        }
        dashboard.onClick(firstSkill.toggleX + 5, firstSkill.toggleY + 5);
        if (firstSkill.model.active == initialSkillActive) {
            throw new AssertionError("Skill toggle failed to mutate store active state!");
        }
        System.out.println("  ✓ Skill 'Disable/Enable' button dynamically toggled active to " + firstSkill.model.active);
        // Toggle back
        dashboard.onClick(firstSkill.toggleX + 5, firstSkill.toggleY + 5);
        if (firstSkill.model.active != initialSkillActive) {
            throw new AssertionError("Skill toggle failed to restore original active state!");
        }
        System.out.println("  ✓ Skill active state restored to " + initialSkillActive);

        // ----------------------------------------------------
        // Test 4: Search Bar Clear Buttons ('✕')
        // ----------------------------------------------------
        System.out.println("\n[Test 4] Testing Search Bar Clear ('✕') dynamic buttons...");
        view.setCatSearch("Robotics");
        dashboard.paint(g, 1366, 768);

        // Click category search clear button
        dashboard.onMouseMove(view.getCatClearX() + 5, view.getCatClearY() + 5);
        if (!view.isCatClearHovered()) {
            throw new AssertionError("Category clear button hover failed!");
        }
        dashboard.onClick(view.getCatClearX() + 5, view.getCatClearY() + 5);
        if (!view.getCatSearch().isEmpty()) {
            throw new AssertionError("Clicking category search clear button failed to clear query! Was: " + view.getCatSearch());
        }
        System.out.println("  ✓ Category search bar clear button successfully cleared query.");

        view.setSkillSearch("React");
        dashboard.paint(g, 1366, 768);
        dashboard.onMouseMove(view.getSkillClearX() + 5, view.getSkillClearY() + 5);
        if (!view.isSkillClearHovered()) {
            throw new AssertionError("Skill clear button hover failed!");
        }
        dashboard.onClick(view.getSkillClearX() + 5, view.getSkillClearY() + 5);
        if (!view.getSkillSearch().isEmpty()) {
            throw new AssertionError("Clicking skill search clear button failed to clear query! Was: " + view.getSkillSearch());
        }
        System.out.println("  ✓ Skill search bar clear button successfully cleared query.");

        // ----------------------------------------------------
        // Test 5: Empty Search State Reset Buttons
        // ----------------------------------------------------
        System.out.println("\n[Test 5] Testing Empty Search State 'Reset search' dynamic buttons...");
        view.setCatSearch("NonExistentCategoryXYZ");
        dashboard.paint(g, 1366, 768);
        dashboard.onClick(view.getCatResetBtnX() + 10, view.getCatResetBtnY() + 10);
        if (!view.getCatSearch().isEmpty()) {
            throw new AssertionError("Clicking category empty state reset button failed!");
        }
        System.out.println("  ✓ Category empty state 'Reset search' button verified.");

        view.setSkillSearch("NonExistentSkillXYZ");
        dashboard.paint(g, 1366, 768);
        dashboard.onClick(view.getSkillResetBtnX() + 10, view.getSkillResetBtnY() + 10);
        if (!view.getSkillSearch().isEmpty()) {
            throw new AssertionError("Clicking skill empty state reset button failed!");
        }
        System.out.println("  ✓ Skill empty state 'Reset search' button verified.");

        // ----------------------------------------------------
        // Test 6: Verify 'View all' Footer Buttons are Deleted
        // ----------------------------------------------------
        System.out.println("\n[Test 6] Verifying 'View all' buttons are deleted from categories & skills footers...");
        dashboard.paint(g, 1366, 768);
        int vCatX = view.getViewAllCatX();
        int vSkillX = view.getViewAllSkillX();
        if (vCatX != -9999 || vSkillX != -9999) {
            throw new AssertionError("View all buttons must be deleted! Found vCatX=" + vCatX + ", vSkillX=" + vSkillX);
        }
        System.out.println("  ✓ Category & Skill 'View all' buttons successfully removed from UI.");

        // ----------------------------------------------------
        // Test 7: Dynamic Edit Category Modal
        // ----------------------------------------------------
        System.out.println("\n[Test 7] Testing Dynamic Edit Category Modal...");
        AdminDataStore dataStore = AdminDataStore.getInstance();
        AdminModals modals = dashboard.getModals();

        AdminCategories.CatHitbox targetCat = view.getCatHitboxes().get(0);
        String origCatName = targetCat.model.name;
        System.out.println("  Editing category: " + origCatName);

        // Click Edit button on the first category row
        dashboard.onClick(targetCat.editX + 5, targetCat.editY + 5);
        if (modals.getActiveModal() != AdminModals.ActiveModal.EDIT_CATEGORY) {
            throw new AssertionError("Clicking category Edit button did not open EDIT_CATEGORY modal! Active: " + modals.getActiveModal());
        }
        if (!origCatName.equals(modals.getInputName())) {
            throw new AssertionError("Edit category input not pre-filled with current name! Expected: " + origCatName + ", Got: " + modals.getInputName());
        }

        // Edit category name: add suffix " (Advanced)"
        for (char ch : " (Advanced)".toCharArray()) {
            dashboard.onKey(ch, (int) ch);
        }
        String updatedCatName = origCatName + " (Advanced)";

        // Submit via Save Changes button (or Enter key)
        dashboard.onKey('\n', java.awt.event.KeyEvent.VK_ENTER);
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            throw new AssertionError("EDIT_CATEGORY modal did not close after Enter submission!");
        }

        // Verify in AdminDataStore
        boolean foundUpdatedCat = dataStore.getAllCategories().stream().anyMatch(c -> c.name.equals(updatedCatName));
        if (!foundUpdatedCat) {
            throw new AssertionError("Category was not updated in AdminDataStore to: " + updatedCatName);
        }
        System.out.println("  ✓ Category successfully edited and updated to: " + updatedCatName);

        // ----------------------------------------------------
        // Test 8: Dynamic Edit Skill Modal
        // ----------------------------------------------------
        System.out.println("\n[Test 8] Testing Dynamic Edit Skill Modal...");
        dashboard.paint(g, 1366, 768);
        AdminCategories.SkillHitbox targetSkill = view.getSkillHitboxes().get(0);
        String origSkillName = targetSkill.model.name;
        System.out.println("  Editing skill: " + origSkillName);

        // Click Edit button on the first skill row
        dashboard.onClick(targetSkill.editX + 5, targetSkill.editY + 5);
        if (modals.getActiveModal() != AdminModals.ActiveModal.EDIT_SKILL) {
            throw new AssertionError("Clicking skill Edit button did not open EDIT_SKILL modal! Active: " + modals.getActiveModal());
        }
        if (!origSkillName.equals(modals.getInputName())) {
            throw new AssertionError("Edit skill input not pre-filled with current name! Expected: " + origSkillName + ", Got: " + modals.getInputName());
        }

        // Edit skill name: add suffix " v2"
        for (char ch : " v2".toCharArray()) {
            dashboard.onKey(ch, (int) ch);
        }
        String updatedSkillName = origSkillName + " v2";

        // Submit via Save Changes button
        dashboard.onKey('\n', java.awt.event.KeyEvent.VK_ENTER);
        if (modals.getActiveModal() != AdminModals.ActiveModal.NONE) {
            throw new AssertionError("EDIT_SKILL modal did not close after Enter submission!");
        }

        // Verify in AdminDataStore
        boolean foundUpdatedSkill = dataStore.getAllSkills().stream().anyMatch(s -> s.name.equals(updatedSkillName));
        if (!foundUpdatedSkill) {
            throw new AssertionError("Skill was not updated in AdminDataStore to: " + updatedSkillName);
        }
        System.out.println("  ✓ Skill successfully edited and updated to: " + updatedSkillName);

        g.dispose();

        System.out.println("\n==================================================");
        System.out.println("  ALL CATEGORIES & SKILLS DYNAMIC BUTTONS PASSED! ");
        System.out.println("==================================================");
    }
}
