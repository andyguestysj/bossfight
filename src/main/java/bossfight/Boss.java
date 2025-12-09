package bossfight;

import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.glColor3f;

public class Boss extends Entity {

    public enum Phase {
        PHASE1,
        PHASE2,
        ENRAGED
    }

    public enum State {
        IDLE,
        TELEGRAPH,
        ATTACK,
        COOLDOWN
    }

    float maxHp = 100f;
    float hp = 100f;

    public Phase phase = Phase.PHASE1;
    public State state = State.IDLE;

    // State machine timing
    private float stateTimer = 0f;
    private float idleDuration = 1.2f;
    private float telegraphDuration = 0.7f;
    private float cooldownDuration = 0.8f;

    // For radial pattern rotation
    private float radialOffset = 0f;

    // For alternating patterns in PHASE2
    private boolean lastPatternWasRadial = true;

    public Boss(float x, float y, Texture tex) {
        this.x = x;
        this.y = y;
        this.texture = tex;
        this.width = 80f;
        this.height = 80f;
    }

    public void update(float dt, Player player, List<Bullet> bullets,
            int screenW, int screenH) {

        // Update phase based on health
        updatePhase();

        // Tick timer
        stateTimer += dt;

        // State machine
        switch (state) {
            case IDLE -> updateIdleState();
            case TELEGRAPH -> updateTelegraphState();
            case ATTACK -> updateAttackState(player, bullets);
            case COOLDOWN -> updateCooldownState();
        }

        // Simple vertical bobbing so boss doesn’t feel static
        float t = (float) glfwGetTime();
        y = screenH * 0.5f + (float) Math.sin(t * 1.2f) * 100f;
    }

    // -----------------------------
    // PHASE LOGIC
    // -----------------------------
    private void updatePhase() {
        float h = hp / maxHp;

        if (h > 0.6f) {
            phase = Phase.PHASE1;
            idleDuration = 1.2f;
            telegraphDuration = 0.7f;
            cooldownDuration = 0.9f;
        } else if (h > 0.3f) {
            phase = Phase.PHASE2;
            idleDuration = 0.9f;
            telegraphDuration = 0.6f;
            cooldownDuration = 0.7f;
        } else {
            phase = Phase.ENRAGED;
            idleDuration = 0.6f;
            telegraphDuration = 0.5f;
            cooldownDuration = 0.6f;
        }
    }

    // -----------------------------
    // STATE MACHINE
    // -----------------------------

    private void updateIdleState() {
        // Boss is just moving/bobbing; no attacks
        if (stateTimer >= idleDuration) {
            stateTimer = 0f;
            state = State.TELEGRAPH;
        }
    }

    private void updateTelegraphState() {
        // Could flash sprite / change colour here if desired
        if (stateTimer >= telegraphDuration) {
            stateTimer = 0f;
            state = State.ATTACK;
        }
    }

    private void updateAttackState(Player player, List<Bullet> bullets) {
        // Fire one "volley" of bullets based on phase + pattern
        fireAttackPatterns(bullets, player);

        // Immediately go to cooldown after firing
        stateTimer = 0f;
        state = State.COOLDOWN;
    }

    private void updateCooldownState() {
        if (stateTimer >= cooldownDuration) {
            stateTimer = 0f;
            state = State.IDLE;
        }
    }

    // -----------------------------
    // ATTACK PATTERNS
    // -----------------------------

    private void fireAttackPatterns(List<Bullet> bullets, Player player) {
        switch (phase) {
            case PHASE1 -> {
                // Only radial pattern in early phase
                fireRadialPattern(bullets);
            }
            case PHASE2 -> {
                // Alternate radial <-> cone
                if (lastPatternWasRadial) {
                    fireConePattern(bullets, player);
                } else {
                    fireRadialPattern(bullets);
                }
                lastPatternWasRadial = !lastPatternWasRadial;
            }
            case ENRAGED -> {
                // Both patterns at once
                fireRadialPattern(bullets);
                fireConePattern(bullets, player);
            }
        }
    }

    private void fireRadialPattern(List<Bullet> bullets) {
        int bulletCount = switch (phase) {
            case PHASE1 -> 6;
            case PHASE2 -> 10;
            case ENRAGED -> 16;
        };

        float speed = switch (phase) {
            case PHASE1 -> 220f;
            case PHASE2 -> 300f;
            case ENRAGED -> 380f;
        };

        radialOffset += 12f; // rotate pattern slightly each time

        for (int i = 0; i < bulletCount; i++) {
            float angleDeg = radialOffset + (360f / bulletCount) * i;
            float angleRad = (float) Math.toRadians(angleDeg);

            float vx = (float) Math.cos(angleRad) * speed;
            float vy = (float) Math.sin(angleRad) * speed;

            bullets.add(new Bullet(
                    x - width / 2f,
                    y,
                    vx, vy,
                    false,
                    GameAssets.bulletTexture));
        }
    }

    private void fireConePattern(List<Bullet> bullets, Player player) {
        // Direction from boss to player
        float dx = player.x - x;
        float dy = player.y - y;
        float baseAngle = (float) Math.atan2(dy, dx);

        int count = 5; // number of bullets in the cone
        float spread = (float) Math.toRadians(35); // total angle spread

        float speed = switch (phase) {
            case PHASE1 -> 240f;
            case PHASE2 -> 340f;
            case ENRAGED -> 420f;
        };

        for (int i = 0; i < count; i++) {
            float t = (count == 1) ? 0f : (i / (float) (count - 1)); // 0..1
            float angle = baseAngle - spread / 2f + spread * t;

            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            bullets.add(new Bullet(
                    x - width / 2f,
                    y,
                    vx, vy,
                    false,
                    GameAssets.bulletTexture));
        }
    }

    // -----------------------------
    // HEALTH + RENDER
    // -----------------------------

    public void takeDamage(float amount) {
        hp -= amount;
        if (hp < 0f)
            hp = 0f;
    }

    public float getHealth01() {
        return hp / maxHp;
    }

    public void render() {
        // Choose tint based on state
        float r = 1f, g = 1f, b = 1f;

        switch (state) {
            case IDLE -> {
                // normal colour, maybe slight blue tint
                r = 0.9f;
                g = 0.9f;
                b = 1.0f;
            }
            case TELEGRAPH -> {
                // warning: flash red/orange
                r = 1.0f;
                g = 0.4f;
                b = 0.4f;
            }
            case ATTACK -> {
                // bright yellow while firing
                r = 1.0f;
                g = 1.0f;
                b = 0.4f;
            }
            case COOLDOWN -> {
                // desaturated / cooled-down colour
                r = 0.4f;
                g = 0.5f;
                b = 0.8f;
            }
        }

        // Optional: pulse the telegraph visually
        // (only if you want a flashing effect)
        if (state == State.TELEGRAPH) {
            float t = (float) (Math.sin(glfwGetTime() * 10.0) * 0.25 + 0.75);
            r *= t;
            g *= t;
            b *= t;
        }

        // Apply tint
        glColor3f(r, g, b);

        // Draw the boss sprite
        SpriteRenderer.drawSprite(texture, x, y, width, height);

        // IMPORTANT: reset colour so other sprites aren’t tinted
        glColor3f(1f, 1f, 1f);
    }

}
