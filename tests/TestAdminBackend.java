import java.util.List;
import java.util.Map;

public class TestAdminBackend {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  TESTING ADMIN DASHBOARD BACKEND & DATABASE SYNC ");
        System.out.println("==================================================");

        AdminDataStore store = AdminDataStore.getInstance();

        // 1. Verify Data Store Initial State
        assertCond(!store.getAllUsers().isEmpty(), "Users repository must not be empty");
        assertCond(!store.getAllProjects().isEmpty(), "Projects repository must not be empty");
        assertCond(!store.getAllReports().isEmpty(), "Reports repository must not be empty");
        assertCond(!store.getAllCategories().isEmpty(), "Categories repository must not be empty");
        assertCond(!store.getAllSkills().isEmpty(), "Skills repository must not be empty");
        assertCond(!store.getAllReviews().isEmpty(), "Reviews repository must not be empty");
        assertCond(!store.getAllAuditLogs().isEmpty(), "Audit logs repository must not be empty");
        System.out.println("  [OK] Initial in-memory data store verified.");

        // 2. Test User Operations & Sync
        int initialUsers = store.getAllUsers().size();
        String testUserName = "Test Admin User " + System.currentTimeMillis();
        boolean userAdded = store.addUser(testUserName, "test.admin@example.com", "Member");
        assertCond(userAdded, "Adding user should succeed");
        assertCond(store.getAllUsers().size() == initialUsers + 1, "User list count must increment");
        AdminDataStore.UserModel u = store.getUserByName(testUserName);
        assertCond(u != null, "Created user must be retrievable");

        boolean roleUpdated = store.updateUserRole(testUserName, "Moderator");
        assertCond(roleUpdated, "Updating user role must succeed");
        assertCond("Moderator".equals(u.role), "User role must be Moderator");

        boolean suspended = store.suspendUser(testUserName);
        assertCond(suspended, "Suspending user must succeed");
        assertCond("Suspended".equals(u.status), "User status must be Suspended");

        boolean reactivated = store.reactivateUser(testUserName);
        assertCond(reactivated, "Reactivating user must succeed");
        assertCond("Active".equals(u.status), "User status must be Active");

        boolean userDeleted = store.deleteUser(testUserName);
        assertCond(userDeleted, "Deleting user must succeed");
        assertCond(store.getUserByName(testUserName) == null, "Deleted user must not exist");
        System.out.println("  [OK] User CRUD and status operations verified.");

        // 3. Test Project Operations & Sync
        int initialProjects = store.getAllProjects().size();
        String testProjName = "Test AI Project " + System.currentTimeMillis();
        boolean projAdded = store.addProject(testProjName, "AI & Machine Learning", "Jordan Lee", "2 / 6", "Active", "Testing admin sync.");
        assertCond(projAdded, "Adding project must succeed");
        assertCond(store.getAllProjects().size() == initialProjects + 1, "Project list count must increment");
        AdminDataStore.ProjectModel p = store.getProjectByName(testProjName);
        assertCond(p != null, "Created project must be retrievable");

        boolean statusUpdated = store.updateProjectStatus(testProjName, "Completed");
        assertCond(statusUpdated, "Updating project status must succeed");
        assertCond("Completed".equals(p.status), "Project status must be Completed");

        boolean projDeleted = store.deleteProject(testProjName);
        assertCond(projDeleted, "Deleting project must succeed");
        assertCond(store.getProjectByName(testProjName) == null, "Deleted project must not exist");
        System.out.println("  [OK] Project CRUD and status operations verified.");

        // 4. Test Report Moderation Operations
        List<AdminDataStore.ReportModel> reports = store.getAllReports();
        if (!reports.isEmpty()) {
            AdminDataStore.ReportModel rep = reports.get(0);
            boolean resolved = store.resolveReport(rep.id, "Verified by automated admin test suite.");
            assertCond(resolved, "Resolving report must succeed");
            assertCond("Resolved".equals(rep.status), "Report status must be Resolved");
        }
        System.out.println("  [OK] Report moderation resolution verified.");

        // 5. Test Categories & Skills Operations
        String testCatName = "Cloud Systems " + System.currentTimeMillis();
        boolean catAdded = store.addCategory(testCatName);
        assertCond(catAdded, "Adding category must succeed");
        boolean catToggled = store.toggleCategory(testCatName);
        assertCond(catToggled, "Toggling category must succeed");

        String testSkillName = "GraphQL " + System.currentTimeMillis();
        boolean skillAdded = store.addSkill(testSkillName);
        assertCond(skillAdded, "Adding skill must succeed");
        boolean skillToggled = store.toggleSkill(testSkillName);
        assertCond(skillToggled, "Toggling skill must succeed");
        System.out.println("  [OK] Categories & Skills operations verified.");

        // 6. Test Reviews Operations
        String testReviewer = "Reviewer " + System.currentTimeMillis();
        boolean revAdded = store.addReview(testReviewer, "Alex Morgan", "Campus Carbon Tracker", "Contributor", 5, "Outstanding work on backend!", "Team Member Review");
        assertCond(revAdded, "Adding review must succeed");
        System.out.println("  [OK] Reviews & Feedback operations verified.");

        // 7. Test University Domains & Platform Settings
        String testDomain = "@oxford.ac.uk";
        boolean domainAdded = store.addUniversityDomain(testDomain);
        assertCond(domainAdded, "Adding university domain must succeed");
        assertCond(store.getUniversityDomainsList().contains(testDomain), "Domain list must contain new domain");

        boolean domainRemoved = store.removeUniversityDomain(testDomain);
        assertCond(domainRemoved, "Removing university domain must succeed");
        assertCond(!store.getUniversityDomainsList().contains(testDomain), "Domain list must not contain removed domain");
        System.out.println("  [OK] University domains configuration verified.");

        // 8. Test AdminDashboard UI container initialization & loadFromServer
        AdminDashboard dashboard = new AdminDashboard(new AdminDashboard.AdminListener() {
            @Override public void returnToLanding() {}
            @Override public void switchToStudentDashboard() {}
        });
        assertCond(dashboard.getOverviewView() != null, "Overview view must be initialized");
        assertCond(dashboard.getUsersView() != null, "Users view must be initialized");
        assertCond(dashboard.getProjectsView() != null, "Projects view must be initialized");
        assertCond(dashboard.getReportsView() != null, "Reports view must be initialized");
        assertCond(dashboard.getReviewsView() != null, "Reviews view must be initialized");
        assertCond(dashboard.getCategoriesView() != null, "Categories view must be initialized");
        assertCond(dashboard.getSettingsView() != null, "Settings view must be initialized");
        dashboard.loadFromServer();
        System.out.println("  [OK] AdminDashboard view container & loadFromServer verified.");

        System.out.println("ALL ADMIN DASHBOARD BACKEND & DATABASE TESTS PASSED 100%!");
        System.exit(0);
    }

    private static void assertCond(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAILED: " + msg);
    }
}

