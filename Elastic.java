/**
 * Elastic physics solver driving organic spring, hover, tilt,
 * and magnetic motion with stiffness and damping.
 */
public class Elastic {
    private float value;
    private float velocity;
    private float target;
    private float stiffness;
    private float damping;

    public Elastic(float initialValue, float stiffness, float damping) {
        this.value = initialValue;
        this.target = initialValue;
        this.velocity = 0f;
        this.stiffness = stiffness; // e.g. 180f
        this.damping = damping;     // e.g. 14f
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float v) {
        this.value = v;
        this.velocity = 0f;
    }

    public void update(float dt) {
        if (!Theme.motionEnabled) {
            value = target;
            velocity = 0f;
            return;
        }

        // Sub-stepping for numerical stability
        int steps = Math.max(1, (int) (dt / 0.005f));
        float subDt = dt / steps;

        for (int i = 0; i < steps; i++) {
            float force = -stiffness * (value - target) - damping * velocity;
            velocity += force * subDt;
            value += velocity * subDt;
        }

        if (Math.abs(value - target) < 0.001f && Math.abs(velocity) < 0.001f) {
            value = target;
            velocity = 0f;
        }
    }
}

