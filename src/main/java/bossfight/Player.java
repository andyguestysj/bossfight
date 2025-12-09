package bossfight;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class Player extends Entity {

    float speed = 400f;
    int lives = 3;
    float invuln = 0f;

    public Player(float x, float y, Texture tex) {
        this.x = x;
        this.y = y;
        this.texture = tex;
        this.width = 40;
        this.height = 40;
    }

    public void update(float dt, long win, int screenW, int screenH) {

        float vx = 0, vy = 0;

        if (glfwGetKey(win, GLFW_KEY_W) == GLFW_PRESS)
            vy++;
        if (glfwGetKey(win, GLFW_KEY_S) == GLFW_PRESS)
            vy--;
        if (glfwGetKey(win, GLFW_KEY_A) == GLFW_PRESS)
            vx--;
        if (glfwGetKey(win, GLFW_KEY_D) == GLFW_PRESS)
            vx++;

        float len = (float) Math.sqrt(vx * vx + vy * vy);
        if (len > 0) {
            vx /= len;
            vy /= len;
        }

        x += vx * speed * dt;
        y += vy * speed * dt;

        invuln = Math.max(0, invuln - dt);

        // world bounds
        float m = 64;
        x = Math.max(m, Math.min(x, screenW - m));
        y = Math.max(m, Math.min(y, screenH - m));
    }

    public Bullet shoot(Texture bulletTex) {
        return new Bullet(x + width / 2, y, 600, 0, true, bulletTex);
    }

    public void takeHit() {
        if (invuln > 0)
            return;
        lives--;
        invuln = 1f;
    }

    public void render() {
        SpriteRenderer.drawSprite(texture, x, y, width, height);
    }

    public int getLives() {
        return lives;
    }

    public int getMaxLives() {
        return 3; // or make this a field if you prefer
    }
}
