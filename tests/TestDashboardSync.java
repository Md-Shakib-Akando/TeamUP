import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Tests the student dashboard <-> backend bridge.
 *
 *   java -cp bin TestDashboardSync                 (offline parts only)
 *   java -Dteamup.api.url=http://localhost:5000/api -cp bin TestDashboardSync
 *       (also logs in as the seeded demo user and loads real data; run `npm run seed` first)
 */
public class TestDashboardSync {

    private static int failures = 0;

    private static void check(boolean ok, String label) {
        System.out.println((ok ? "  [PASS] " : "  [FAIL] ") + label);
        if (!ok) failures++;
    }

    private static StudentDashboard newDashboard() {
        return new StudentDashboard(new StudentDashboard.DashboardListener() {
            @Override public void returnToLanding() {}
            @Override public void openModal(String title, String message, boolean isRegister) {}
        });
    }

    /** Paint every sidebar view (and a couple of tab variants) to make sure nothing throws. */
    private static void paintAllViews(StudentDashboard d, String label) {
        BufferedImage img = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            for (StudentDashboard.NavItem nav : StudentDashboard.NavItem.values()) {
                d.setActiveNav(nav);
                d.update(0.016f);
                d.paint(g, 1280, 800);
                d.paint(g, 900, 700);
            }
            for (int i = 0; i < d.getTeamData().tabs.size(); i++) {
                d.getTeamData().activeTabIndex = i;
                d.setActiveNav(StudentDashboard.NavItem.MY_TEAMS);
                d.paint(g, 1280, 800);
            }
            d.getTeamData().activeTabIndex = 0;
            for (int i = 0; i < d.getProfileData().tabs.size(); i++) {
                d.getProfileData().activeTabIndex = i;
                d.setActiveNav(StudentDashboard.NavItem.MY_PROFILE);
                d.paint(g, 1280, 800);
            }
            d.getProfileData().activeTabIndex = 0;
            check(true, label + ": all views painted without errors");
        } catch (RuntimeException e) {
            e.printStackTrace();
            check(false, label + ": painting threw " + e);
        } finally {
            g.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        System.out.println("=== TestDashboardSync ===");

        // 1. JSON helper
        Object parsed = Json.parse("{\"a\":[1,2.5,\"x\\\"y\\u00e9\"],\"b\":{\"c\":true,\"d\":null}}");
        Map<String, Object> m = (Map<String, Object>) parsed;
        check(Json.list(m, "a").size() == 3, "Json parses arrays");
        check("x\"yé".equals(Json.list(m, "a").get(2)), "Json decodes escapes");
        check(Json.bool(Json.map(m, "b"), "c", false), "Json reads nested booleans");
        check(Json.integer(m, "missing", 7) == 7, "Json defaults for missing keys");
        String round = Json.stringify(Json.obj("t", "a\"b", "n", 3, "l", java.util.Arrays.asList("x", "y")));
        check(round.equals("{\"t\":\"a\\\"b\",\"n\":3,\"l\":[\"x\",\"y\"]}"), "Json stringify: " + round);

        // 2. Offline: demo data stays, sync calls are no-ops
        AuthStore.logout();
        StudentDashboard demo = newDashboard();
        check(!DashboardSync.isOnline(), "No session -> offline mode");
        demo.loadFromServer();
        demo.update(0.016f);
        check(!demo.isServerDataLoaded() && demo.getProjects().size() == 2, "Offline dashboard keeps demo data");
        paintAllViews(demo, "Demo data");

        // 3. Brand-new user: empty payload must render cleanly
        StudentDashboard empty = newDashboard();
        String emptyPayload = "{\"success\":true,\"me\":{\"name\":\"New Student\",\"initials\":\"NS\",\"role\":\"Student\",\"profileComplete\":40},"
                + "\"stats\":{\"activeProjects\":0,\"activeProjectsSub\":\"0 leading · 0 joined\",\"myTeams\":0,\"myTeamsSub\":\"0 active · 0 completed\","
                + "\"pendingRequests\":0,\"pendingRequestsSub\":\"0 received · 0 sent\",\"tasksDue\":0,\"tasksDueSub\":\"Next 7 days · 0 overdue\"},"
                + "\"overview\":{\"projects\":[],\"teammates\":[],\"tasks\":[],\"activities\":[]},\"notifications\":[],"
                + "\"discover\":[],\"teammates\":[],\"myProjects\":[],\"team\":null,"
                + "\"profile\":{\"name\":\"New Student\",\"handle\":\"@new\",\"projects\":[],\"skills\":[],\"reviews\":[],\"peerRating\":0,\"totalReviewsCount\":0},"
                + "\"settings\":{\"fullName\":\"New Student\",\"username\":\"new\",\"email\":\"new@example.com\",\"categories\":[],\"visibility\":\"PUBLIC\",\"collabMode\":\"REMOTE\"}}";
        DashboardSync.apply(empty, (Map<String, Object>) Json.parse(emptyPayload));
        check(empty.isServerDataLoaded(), "Empty payload applied");
        check("New".equals(empty.getFirstName()) && "NS".equals(empty.getProfileInitials()), "Greeting/initials follow the user");
        check("No team yet".equals(empty.getTeamData().workspaceName), "No team -> empty workspace state");
        check(empty.getNotifications().size() == 1, "Empty notifications -> friendly placeholder");
        paintAllViews(empty, "Empty new-user state");

        // 4. Live backend (only when a server is reachable)
        if (!AuthStore.isServerAlive()) {
            System.out.println("  [SKIP] Backend not reachable at " + AuthStore.getApiBaseUrl() + " — live checks skipped.");
        } else {
            AuthStore.AuthResult res = AuthStore.login("alex.morgan@example.com", "teamup123");
            check(res.success && AuthStore.getCurrentToken() != null, "Login as seeded demo user: " + res.message);

            StudentDashboard live = newDashboard();
            live.loadFromServer();
            long until = System.currentTimeMillis() + 20000;
            while (!live.isServerDataLoaded() && System.currentTimeMillis() < until) {
                live.update(0.016f);
                Thread.sleep(50);
            }
            check(live.isServerDataLoaded(), "Dashboard loaded from /api/dashboard");
            check("Alex".equals(live.getFirstName()), "Greeting uses logged-in user");
            check("CampusConnect workspace".equals(live.getTeamData().workspaceName),
                    "My Teams workspace = " + live.getTeamData().workspaceName);
            check(live.getTeamData().members.size() >= 5, "Workspace members loaded: " + live.getTeamData().members.size());
            check(live.getTasks().size() >= 3, "Upcoming deadlines loaded: " + live.getTasks().size());
            check(live.getProfileData().reviews.size() >= 3, "Peer reviews loaded");
            check(live.getStatSubtitle(0, null) != null, "Stat subtitles from server: " + live.getStatSubtitle(0, null));
            paintAllViews(live, "Live MongoDB data");

            // Action round-trip: add a task to the workspace project, then complete it (id arrives asynchronously)
            String projectId = live.getTeamData().projectId;
            DashboardMyProjects.ProjectTask t = new DashboardMyProjects.ProjectTask("Sync test task", "Upcoming", false);
            DashboardSync.addTask(live, projectId, t);
            DashboardSync.setTaskDone(live, t, true);
            until = System.currentTimeMillis() + 15000;
            while (t.remoteId == null && System.currentTimeMillis() < until) Thread.sleep(50);
            check(t.remoteId != null, "addTask returned backend id " + t.remoteId);
            Thread.sleep(1500);

            ApiClient.Response r = ApiClient.get("/dashboard");
            boolean foundDone = false;
            for (Map<String, Object> p : Json.maps(r.json, "myProjects")) {
                for (Map<String, Object> task : Json.maps(p, "tasks")) {
                    if (String.valueOf(t.remoteId).equals(Json.str(task, "id", "")) && Json.bool(task, "done", false)) {
                        foundDone = true;
                    }
                }
            }
            check(foundDone, "Task created and completed in MongoDB");
            AuthStore.logout();
        }

        System.out.println(failures == 0 ? "=== ALL DASHBOARD SYNC TESTS PASSED ===" : ("=== " + failures + " FAILURE(S) ==="));
        System.exit(failures == 0 ? 0 : 1);
    }
}
