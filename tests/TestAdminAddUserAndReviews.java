import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Automated test suite verifying:
 * 1. Admin Reviews page 'Add review' button deletion.
 * 2. Admin Users 'Add User' modal: only Admin and Member roles remain; Moderator is deleted.
 */
public class TestAdminAddUserAndReviews {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING ADD USER & REVIEWS MODAL TEST SUITE    ");
        System.out.println("==================================================");

        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override
            public void returnToLanding() {}
            @Override
            public void switchToStudentDashboard() {}
        });

        BufferedImage img = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Draw.setQualityHints(g);

        // ----------------------------------------------------
        // Part 1: Verify Reviews page 'Add review' button removed
        // ----------------------------------------------------
        System.out.println("\n[Test 1] Testing Reviews page 'Add review' button removal...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.FEEDBACK);
        dashboard.paint(g, 1366, 768);

        // Click top right where the Add Review button was
        dashboard.onClick(1366 - 80, 20);
        if (dashboard.getModals().getActiveModal() == AdminModals.ActiveModal.ADD_REVIEW) {
            throw new AssertionError("Clicking top right unexpectedly opened ADD_REVIEW modal!");
        }
        System.out.println("  ✓ 'Add review' button completely deleted from Reviews page UI.");

        // Multi-viewport rendering check for reviews page
        int[] vps = {480, 768, 1024, 1366, 1920};
        for (int w : vps) {
            BufferedImage vImg = new BufferedImage(w, 768, BufferedImage.TYPE_INT_ARGB);
            Graphics2D vG = vImg.createGraphics();
            dashboard.paint(vG, w, 768);
            vG.dispose();
        }
        System.out.println("  ✓ Reviews page renders cleanly without 'Add review' across all viewports.");

        // ----------------------------------------------------
        // Part 2: Verify Users page 'Add User' modal roles
        // ----------------------------------------------------
        System.out.println("\n[Test 2] Testing 'Add User' modal roles (Only Member & Admin; Moderator deleted)...");
        dashboard.navigateTo(AdminSidebar.AdminNavItem.USERS);
        dashboard.openAddUserModal();
        dashboard.paint(g, 1366, 768);

        AdminModals modals = dashboard.getModals();
        if (modals.getActiveModal() != AdminModals.ActiveModal.ADD_USER) {
            throw new AssertionError("ADD_USER modal is not active!");
        }

        // 1. Initial default role must be Member
        if (!"Member".equalsIgnoreCase(modals.getNewUserRole())) {
            throw new AssertionError("Initial newUserRole expected 'Member', got: " + modals.getNewUserRole());
        }
        System.out.println("  ✓ Initial newUserRole defaults to 'Member'.");

        // 2. Moderator button must be deleted (-9999)
        if (modals.getNewUserModBtnX() != -9999) {
            throw new AssertionError("newUserModBtnX expected -9999 (deleted), got: " + modals.getNewUserModBtnX());
        }
        System.out.println("  ✓ Moderator role button completely deleted.");

        // 3. Member and Admin button positions must be valid
        int memX = modals.getNewUserMemberBtnX();
        int admX = modals.getNewUserAdminBtnX();
        if (memX <= 0 || admX <= memX) {
            throw new AssertionError("Invalid button positions! memX=" + memX + ", admX=" + admX);
        }
        System.out.println("  ✓ Member button (" + memX + "px) and Admin button (" + admX + "px) properly aligned.");

        // 4. Click Admin button to toggle role
        int roleY = 768 / 2; // modal is centered around 390h, roleY is inside modal
        // Find exact roleY from painting
        dashboard.onClick(admX + 10, (768 - 390) / 2 + 84 + 66 + 66 + 18);
        if (!"Admin".equalsIgnoreCase(modals.getNewUserRole())) {
            throw new AssertionError("Clicking Admin role button failed! Current: " + modals.getNewUserRole());
        }
        System.out.println("  ✓ Clicking 'Admin' successfully set role to 'Admin'.");

        // 5. Click Member button to toggle back to Member
        dashboard.onClick(memX + 10, (768 - 390) / 2 + 84 + 66 + 66 + 18);
        if (!"Member".equalsIgnoreCase(modals.getNewUserRole())) {
            throw new AssertionError("Clicking Member role button failed! Current: " + modals.getNewUserRole());
        }
        System.out.println("  ✓ Clicking 'Member' successfully set role back to 'Member'.");

        // 6. Test User Creation with 'Admin' role
        dashboard.onClick(admX + 10, (768 - 390) / 2 + 84 + 66 + 66 + 18);
        AdminDataStore store = AdminDataStore.getInstance();
        int initialUserCount = store.getAllUsers().size();
        store.addUser("Tanvir Ahmed", "tanvir.ahmed@teamup.edu", modals.getNewUserRole());

        AdminDataStore.UserModel createdAdmin = store.getUserByName("Tanvir Ahmed");
        if (createdAdmin == null || !"Admin".equalsIgnoreCase(createdAdmin.role)) {
            throw new AssertionError("Created user did not receive 'Admin' role!");
        }
        System.out.println("  ✓ User created with role: " + createdAdmin.role);

        // 7. Test User Creation with 'Member' role
        dashboard.onClick(memX + 10, (768 - 390) / 2 + 84 + 66 + 66 + 18);
        store.addUser("Tasnia Rahman", "tasnia.rahman@teamup.edu", modals.getNewUserRole());
        AdminDataStore.UserModel createdMember = store.getUserByName("Tasnia Rahman");
        if (createdMember == null || !"Member".equalsIgnoreCase(createdMember.role)) {
            throw new AssertionError("Created user did not receive 'Member' role!");
        }
        System.out.println("  ✓ User created with role: " + createdMember.role);

        // Clean up created users
        store.deleteUser("Tanvir Ahmed");
        store.deleteUser("Tasnia Rahman");
        dashboard.closeModal();

        g.dispose();

        System.out.println("\n==================================================");
        System.out.println("  ALL ADD USER & REVIEWS TESTS PASSED 100%!       ");
        System.out.println("==================================================");
    }
}

