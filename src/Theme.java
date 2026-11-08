import java.awt.Color;
import java.awt.Font;

/**
 * Theme holds Dribbble-inspired design tokens:
 * Light-first canvas (#F8F7F4), ink (#0D0C22), brand pink (#EA4C89),
 * pastel tints (violet, mint, sunny, sky), and high-contrast dark CTA/footer.
 */
public class Theme {

    // Light-first as mandated by PRD Section 5
    public static boolean isDark = false;
    public static float themeTransition = 1f; // 0 = dark, 1 = light

    public static float fontScale = 1.0f;
    public static boolean motionEnabled = true;

    // Dribbble Brand Palette
    public static final Color BRAND_PINK       = new Color(0xEA, 0x4C, 0x89);
    public static final Color BRAND_PINK_HOVER = new Color(0xF0, 0x62, 0x9A);
    public static final Color INK_DARK         = new Color(0x0D, 0x0C, 0x22);
    public static final Color CANVAS_LIGHT  = new Color(0xF8, 0xF7, 0xF4);

    // Pastel Card Tints (PRD Section 5)
    public static final Color PASTEL_VIOLET = new Color(0xF3, 0xF0, 0xFF);
    public static final Color PASTEL_MINT   = new Color(0xEB, 0xFB, 0xF5);
    public static final Color PASTEL_SUNNY  = new Color(0xFF, 0xFA, 0xEC);
    public static final Color PASTEL_SKY    = new Color(0xF0, 0xF8, 0xFF);
    public static final Color PASTEL_PINK   = new Color(0xFF, 0xF0, 0xF5);

    // Accent saturated colors
    public static final Color ACCENT_VIOLET = new Color(0x7C, 0x5C, 0xFF);
    public static final Color ACCENT_CYAN   = new Color(0x22, 0xD3, 0xEE);
    public static final Color HIGHLIGHT_WARM = new Color(0xFF, 0x8A, 0x5B);
    public static final Color ACCENT_MINT   = new Color(0x3D, 0xDB, 0xB0);
    public static final Color ACCENT_SUNNY  = new Color(0xFF, 0xD1, 0x66);
    public static final Color ACCENT_SKY    = new Color(0x6E, 0xC1, 0xFF);
    public static final Color SUCCESS_GREEN = new Color(0x10, 0xB9, 0x81);
    public static final Color ERROR_RED     = new Color(0xEF, 0x44, 0x44);

    // Dark mode fallback colors
    private static final Color DARK_BG           = new Color(0x0B, 0x10, 0x20);
    private static final Color DARK_CARD_BG      = new Color(255, 255, 255, 15);
    private static final Color DARK_CARD_BORDER  = new Color(255, 255, 255, 30);
    private static final Color DARK_TEXT_PRIMARY   = new Color(0xF8, 0xFA, 0xFC);
    private static final Color DARK_TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
    private static final Color DARK_TEXT_MUTED     = new Color(0x64, 0x74, 0x8B);
    private static final Color DARK_NAV_BG         = new Color(0x0B, 0x10, 0x20, 220);

    // Light mode colors
    private static final Color LIGHT_BG           = CANVAS_LIGHT;
    private static final Color LIGHT_CARD_BG      = Color.WHITE;
    private static final Color LIGHT_CARD_BORDER  = new Color(0, 0, 0, 18);
    private static final Color LIGHT_TEXT_PRIMARY   = INK_DARK;
    private static final Color LIGHT_TEXT_SECONDARY = new Color(0x56, 0x55, 0x66);
    private static final Color LIGHT_TEXT_MUTED     = new Color(0x9E, 0x9E, 0xAE);
    private static final Color LIGHT_NAV_BG         = new Color(248, 247, 244, 230);

    public static void toggleTheme() {
        isDark = !isDark;
    }

    public static void update(float dt) {
        float target = isDark ? 0f : 1f;
        if (Math.abs(themeTransition - target) > 0.001f) {
            float speed = 6.0f;
            themeTransition += (target - themeTransition) * Math.min(1.0f, dt * speed);
            if (Math.abs(themeTransition - target) < 0.005f) {
                themeTransition = target;
            }
        }
    }

    public static Color lerp(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int red   = (int) (a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int green = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int blue  = (int) (a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        int alpha = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(red, green, blue, alpha);
    }

    public static Color bg() {
        return lerp(DARK_BG, LIGHT_BG, themeTransition);
    }

    public static Color cardBg() {
        return lerp(DARK_CARD_BG, LIGHT_CARD_BG, themeTransition);
    }

    public static Color cardBorder() {
        return lerp(DARK_CARD_BORDER, LIGHT_CARD_BORDER, themeTransition);
    }

    public static Color textPrimary() {
        return lerp(DARK_TEXT_PRIMARY, LIGHT_TEXT_PRIMARY, themeTransition);
    }

    public static Color textSecondary() {
        return lerp(DARK_TEXT_SECONDARY, LIGHT_TEXT_SECONDARY, themeTransition);
    }

    public static Color textMuted() {
        return lerp(DARK_TEXT_MUTED, LIGHT_TEXT_MUTED, themeTransition);
    }

    public static Color navBg() {
        return lerp(DARK_NAV_BG, LIGHT_NAV_BG, themeTransition);
    }

    public static Color inputBg() {
        if (isDark) {
            return new Color(255, 255, 255, 20);
        } else {
            return Color.WHITE;
        }
    }

    public static Color inputBorder() {
        if (isDark) {
            return new Color(255, 255, 255, 45);
        } else {
            return new Color(0, 0, 0, 30);
        }
    }

    // Typography: bold, huge contrast
    public static Font fontHero() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(72 * fontScale));
    }

    public static Font fontHeroMobile() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(42 * fontScale));
    }

    public static Font fontTitle() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(44 * fontScale));
    }

    public static Font fontSectionHeading() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(32 * fontScale));
    }

    public static Font fontCardTitle() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(20 * fontScale));
    }

    public static Font fontSubtitle() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(18 * fontScale));
    }

    public static Font fontBody() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(16 * fontScale));
    }

    public static Font fontBodyBold() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(16 * fontScale));
    }

    public static Font fontSmall() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(13 * fontScale));
    }

    public static Font fontSmallBold() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(13 * fontScale));
    }

    public static Font fontButton() {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(15 * fontScale));
    }

    public static void zoomIn() {
        if (fontScale < 1.6f) fontScale += 0.1f;
    }

    public static void zoomOut() {
        if (fontScale > 0.8f) fontScale -= 0.1f;
    }

    public static void resetZoom() {
        fontScale = 1.0f;
    }
}
