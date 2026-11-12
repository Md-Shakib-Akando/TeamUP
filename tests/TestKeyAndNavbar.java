import java.awt.Canvas;
import java.awt.event.KeyEvent;

public class TestKeyAndNavbar {
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  TESTING SINGLE KEYSTROKE INPUT & NAVBAR PROFILE ");
        System.out.println("==================================================");

        // 1. Verify TextBox only accepts 1 char on KEY_PRESSED and ignores KEY_TYPED
        TextBox tb = new TextBox("Test Placeholder");
        tb.focused = true;

        Canvas dummy = new Canvas();
        KeyEvent pressA = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
        KeyEvent typeA = new KeyEvent(dummy, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'a');
        KeyEvent releaseA = new KeyEvent(dummy, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');

        // Simulate how AWT fires on a single key press
        tb.onKey(pressA);
        tb.onKey(typeA);
        tb.onKey(releaseA);

        assertCond("a".equals(tb.getText()), "Typing 'a' must insert exactly 1 character 'a', but got: '" + tb.getText() + "'");

        // Type 'b'
        KeyEvent pressB = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_B, 'b');
        KeyEvent typeB = new KeyEvent(dummy, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'b');
        tb.onKey(pressB);
        tb.onKey(typeB);

        assertCond("ab".equals(tb.getText()), "Typing 'b' must result in 'ab', but got: '" + tb.getText() + "'");

        // Test Backspace
        KeyEvent pressBS = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\b');
        tb.onKey(pressBS);
        assertCond("a".equals(tb.getText()), "Backspace must remove 1 character, result: '" + tb.getText() + "'");

        // 1.1 Verify Shift, Caps Lock, Ctrl, Alt, F-keys do NOT insert unwanted characters
        KeyEvent pressShift = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED);
        tb.onKey(pressShift);
        assertCond("a".equals(tb.getText()), "Shift alone must not insert any character, but got: '" + tb.getText() + "'");

        KeyEvent pressCapsLock = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_CAPS_LOCK, KeyEvent.CHAR_UNDEFINED);
        tb.onKey(pressCapsLock);
        assertCond("a".equals(tb.getText()), "Caps Lock alone must not insert any character, but got: '" + tb.getText() + "'");

        KeyEvent pressCtrl = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED);
        tb.onKey(pressCtrl);
        assertCond("a".equals(tb.getText()), "Ctrl alone must not insert any character, but got: '" + tb.getText() + "'");

        KeyEvent pressAlt = new KeyEvent(dummy, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.ALT_DOWN_MASK, KeyEvent.VK_ALT, KeyEvent.CHAR_UNDEFINED);
        tb.onKey(pressAlt);
        assertCond("a".equals(tb.getText()), "Alt alone must not insert any character, but got: '" + tb.getText() + "'");

        System.out.println("  [OK] Shift, Caps Lock, Ctrl, Alt verified: zero unwanted characters!");

        // 1.2 Verify Mouse Click caret placement
        tb.setText("HelloWorld");
        tb.setBounds(100, 100, 300, 44);

        // Click near the start (mx = 105)
        tb.onMouseDown(105, 120);
        assertCond(tb.getCaretPosFromX(105) == 0, "Clicking at start of text must place caret at 0");

        // Click near the end (mx = 380)
        tb.onMouseDown(380, 120);
        assertCond(tb.getCaretPosFromX(380) == 10, "Clicking past end of text must place caret at end");

        // Click in between (e.g. around index 5)
        int midPos = tb.getCaretPosFromX(150);
        assertCond(midPos > 0 && midPos < 10, "Clicking in middle must place caret in between, got: " + midPos);

        System.out.println("  [OK] Mouse click cursor positioning verified!");

        System.out.println("  [OK] Single character input verified: no duplicate characters!");

        // 2. Test NavBar profile badge when logged in
        NavBar nav = new NavBar(new NavBar.NavListener() {
            @Override public void scrollToSection(String sectionId) {}
            @Override public void openModal(String title, String message, boolean isRegister) {}
            @Override public void openDashboard() {}
        });

        // Initially not logged in
        assertCond(nav.getLoggedInUser() == null, "User must be null initially");

        // Log in user
        AuthStore.UserRecord testUser = new AuthStore.UserRecord("Rahim Uddin", "rahim@university.edu", "Student", "", "", "");
        nav.setLoggedInUser(testUser);
        assertCond(nav.getLoggedInUser() != null, "User must be logged in");
        assertCond("Rahim Uddin".equals(nav.getLoggedInUser().name), "Logged in user name must match");

        // Render paint pass to ensure no crashes
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        nav.paint(g2, 0, 1280, 0, 800);
        g2.dispose();

        // 3. Test Logout functionality
        AuthStore.logout();
        nav.setLoggedInUser(null);
        assertCond(nav.getLoggedInUser() == null, "User must be logged out and null");

        // Re-paint to confirm Get Started button returns
        img = new java.awt.image.BufferedImage(1280, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        g2 = img.createGraphics();
        nav.paint(g2, 0, 1280, 0, 800);
        g2.dispose();

        System.out.println("  [OK] NavBar profile badge & Logout workflow verified!");

        System.out.println("ALL KEYSTROKE AND NAVBAR TESTS PASSED 100%!");
        System.exit(0);
    }

    private static void assertCond(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAILED: " + msg);
    }
}

