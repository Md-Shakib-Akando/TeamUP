/**
 * Anim manages animation easing formulas, cursor lag,
 * page-load intro, and motion spec implementations.
 */
public class Anim {

    public static float globalTime = 0f;
    public static float mouseX = 0f;
    public static float mouseY = 0f;
    public static float targetMouseX = 0f;
    public static float targetMouseY = 0f;

    // Motion Spec #4: Soft pink glow follows mouse with lag (lerp 0.12 per frame)
    public static float lagCursorX = 0f;
    public static float lagCursorY = 0f;

    // Motion Spec #1 & #3: Page-load intro and squiggle draw
    public static float pageLoadTime = 0f;

    public static void update(float dt) {
        if (Theme.motionEnabled) {
            globalTime += dt;
            pageLoadTime += dt;

            // Direct mouse update
            mouseX = approach(mouseX, targetMouseX, dt, 14f);
            mouseY = approach(mouseY, targetMouseY, dt, 14f);

            // Spec #4: lerp 0.12 per frame for soft pink cursor glow lag
            lagCursorX = lerp(lagCursorX, targetMouseX, 0.12f);
            lagCursorY = lerp(lagCursorY, targetMouseY, 0.12f);
        } else {
            pageLoadTime = 10f;
            lagCursorX = targetMouseX;
            lagCursorY = targetMouseY;
        }
    }

    public static void setMouseTarget(float mx, float my) {
        targetMouseX = mx;
        targetMouseY = my;
    }

    public static float easeOutCubic(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float f = 1f - t;
        return 1f - f * f * f;
    }

    public static float easeInOutQuad(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }

    public static float easeOutBack(float t) {
        t = Math.max(0f, Math.min(1f, t));
        final float c1 = 1.70158f;
        final float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.max(0f, Math.min(1f, t));
    }

    public static float approach(float current, float target, float dt, float speed) {
        if (!Theme.motionEnabled) return target;
        float diff = target - current;
        if (Math.abs(diff) < 0.0005f) return target;
        return current + diff * Math.min(1f, dt * speed);
    }

    public static float loop(float period, float offset) {
        if (!Theme.motionEnabled) return 0f;
        return ((globalTime + offset) % period) / period;
    }

    public static float pulse(float min, float max, float speed) {
        if (!Theme.motionEnabled) return (min + max) * 0.5f;
        float s = (float) (Math.sin(globalTime * speed) * 0.5 + 0.5);
        return min + s * (max - min);
    }

    /**
     * Motion Spec #8: Magnetic button offset calculator
     * Button leans up to 8px toward cursor when within 80px distance.
     */
    public static float[] calculateMagneticOffset(float btnCenterX, float btnCenterY, float maxDist, float maxPull) {
        float dx = mouseX - btnCenterX;
        float dy = mouseY - btnCenterY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist < maxDist && dist > 1f && Theme.motionEnabled) {
            float strength = (1f - dist / maxDist);
            float pull = strength * maxPull;
            return new float[]{(dx / dist) * pull, (dy / dist) * pull};
        }
        return new float[]{0f, 0f};
    }

    public static class RevealState {
        public float progress = 0f;
        private boolean triggered = false;

        public void trigger() {
            triggered = true;
        }

        public void update(float dt) {
            if (!Theme.motionEnabled) {
                progress = triggered ? 1f : 0f;
                return;
            }
            if (triggered && progress < 1f) {
                progress += dt / 0.5f;
                if (progress > 1f) progress = 1f;
            }
        }

        public float getAlpha() {
            return easeOutCubic(progress);
        }

        public int getYOffset() {
            return Math.round(24 * (1f - easeOutCubic(progress)));
        }
    }
}
