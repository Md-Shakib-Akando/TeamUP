import java.awt.*;
import java.util.Map;

/**
 * Stat band — numbers count up when scrolled into view. Values come from the database
 * (GET /api/platform-stats) and refresh every 30 seconds; until the first answer arrives
 * (or if the backend is unreachable) the cells show "—" instead of made-up numbers.
 */
public class NumbersStrip extends LpSection {

    private static final long REFRESH_MS = 30_000L;

    private final String[] suffix = {"", "", "%", "%"};
    private final String[] label = {"Verified members", "Active teams", "Match accuracy", "Ghosting rate"};
    private final String[] sub = {"Loading live stats…", "Loading live stats…", "Loading live stats…", "Loading live stats…"};
    private final String[] icons = {"users", "layers", "spark", "bolt"};
    private final Color[] accents = {Fx.PINK, Fx.VIOLET, Fx.CYAN, Fx.MINT};
    /** -1 = not known yet (rendered as "—"). Written by the sync thread, read by the UI thread. */
    private final float[] value = {-1f, -1f, -1f, -1f};
    private volatile boolean everLoaded = false;

    public NumbersStrip() {
        startSync();
    }

    /** Updates the band; a negative value means "unknown". */
    public synchronized void setStats(float members, float teams, float match, float ghost) {
        value[0] = members;
        value[1] = teams;
        value[2] = match;
        value[3] = ghost;
    }

    private synchronized float valueAt(int i) {
        return value[i];
    }

    private void startSync() {
        Thread t = new Thread(() -> {
            int failures = 0;
            while (true) {
                boolean ok = false;
                try {
                    AuthStore.ensureServerRunning();
                    ApiClient.Response res = ApiClient.get("/platform-stats");
                    Map<String, Object> data = res != null && res.ok() ? Json.map(res.json, "data") : null;
                    if (data != null) {
                        apply(data);
                        ok = true;
                    }
                } catch (Exception ignored) {
                    // backend offline: keep the last known numbers
                }
                failures = ok ? 0 : failures + 1;
                if (!ok && !everLoaded && failures >= 3) {
                    for (int i = 0; i < sub.length; i++) sub[i] = "Live stats unavailable";
                }
                try {
                    Thread.sleep(ok ? REFRESH_MS : (failures < 4 ? 1_500L : REFRESH_MS));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "TeamUp-NumbersStrip-Sync");
        t.setDaemon(true);
        t.start();
    }

    private void apply(Map<String, Object> data) {
        int members = Json.integer(data, "verifiedMembers", -1);
        int teams = Json.integer(data, "activeTeams", -1);
        Object acc = data.get("matchAccuracy");
        float match = acc instanceof Number ? ((Number) acc).floatValue() : -1f;
        float ghost = (float) Json.num(data, "ghostingRate", -1);
        setStats(members, teams, match, ghost);

        String uni = Json.str(data, "subUniversities", null);
        sub[0] = uni != null && !uni.trim().isEmpty() ? uni : "Active student accounts";
        sub[1] = "Ongoing projects with 2+ members";
        sub[2] = match >= 0 ? "Average peer rating, out of 5 stars" : "No peer reviews yet";
        sub[3] = "Members reported for no-shows";
        everLoaded = true;
    }

    @Override public String getId() { return "numbers"; }

    @Override
    protected int render(Graphics2D g, int y, int width, int viewportY, int viewportH) {
        int top = y;
        int cur = y + (width < 700 ? 60 : 100);
        int[] c = Fx.container(width);
        int cols = width >= 980 ? 4 : (width >= 600 ? 2 : 1);
        int rows = (4 + cols - 1) / cols;
        int cellH = 150;
        int bh = rows * cellH;
        if (measuring) return cur + bh - top + 20;

        Graphics2D b = Fx.fade(g, rv.p(0f, 0.9f), 30);
        Fx.glowOval(b, width / 2f, cur + bh / 2f, c[1] * 0.5f, bh * 0.8f, Fx.VIOLET, Fx.dark() ? 0.14f : 0.08f);
        Fx.card(b, c[0], cur, c[1], bh, 26, 0f, Fx.PINK);

        int cw = c[1] / cols;
        for (int i = 0; i < 4; i++) {
            int col = i % cols, row = i / cols;
            int x = c[0] + col * cw;
            int cy = cur + row * cellH;
            if (col > 0) {
                b.setPaint(new GradientPaint(0, cy + 20, Fx.a(Fx.border(), 0), 0, cy + cellH / 2f, Fx.borderHi(), true));
                b.fillRect(x, cy + 24, 1, cellH - 48);
            }
            if (row > 0 && col == 0) {
                b.setColor(Fx.border());
                b.drawLine(c[0] + 24, cy, c[0] + c[1] - 24, cy);
            }

            float v = valueAt(i);
            String num;
            if (v < 0) {
                num = "—";
            } else {
                float p = rv.lin(0.2f + i * 0.12f, 1.6f);
                float e = Fx.easeOutExpo(p);
                float shown = v * e;
                boolean fractional = v < 10 && Math.abs(v - Math.round(v)) > 0.01f;
                String formatted = fractional
                        ? String.format(java.util.Locale.US, "%.1f", shown)
                        : String.format(java.util.Locale.US, "%,d", Math.round(shown));
                num = formatted + suffix[i];
            }

            int px = x + 32;
            Fx.iconTile(b, px, cy + 32, 40, accents[i], icons[i].equals("layers") ? "task" : icons[i], 0f);
            float fontSize = 40f;
            int maxNumW = Math.max(80, cw - 110);
            b.setFont(Fx.display(fontSize));
            while (fontSize > 24f && b.getFontMetrics().stringWidth(num) > maxNumW) {
                fontSize -= 2f;
                b.setFont(Fx.display(fontSize));
            }
            Fx.gradientText(b, num, px + 56, cy + 32 + 34, new Color[]{Fx.text(), Fx.mix(Fx.text(), accents[i], 0.6f)}, 0f);
            b.setFont(Fx.bold(15));
            b.setColor(Fx.text());
            b.drawString(label[i], px, cy + 98);
            b.setFont(Fx.body(13));
            b.setColor(Fx.text3());
            b.drawString(sub[i], px, cy + 119);
        }
        b.dispose();
        return cur + bh - top + 20;
    }
}
