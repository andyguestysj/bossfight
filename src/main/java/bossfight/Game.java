package bossfight;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import org.lwjgl.stb.STBEasyFont;

public class Game {

    int width, height;
    long window;

    Texture tilesetTexture;
    Texture playerTexture;
    Texture bossTexture;
    Texture bulletTexture;

    TileMap tileMap;

    Player player;
    Boss boss;
    List<Bullet> bullets = new ArrayList<>();

    float shootCooldown = 0.25f;
    float shootTimer = 0f;
    boolean gameOver = false;
    boolean restartRequested = false;

    public Game(int width, int height, long window) {
        this.width = width;
        this.height = height;
        this.window = window;

        tilesetTexture = new Texture("assets/tileset.png");
        playerTexture = new Texture("assets/player.png");
        bossTexture = new Texture("assets/boss.png");
        bulletTexture = new Texture("assets/bullet.png");

        GameAssets.bulletTexture = bulletTexture;

        int tileSize = 64;
        int cols = (int) Math.ceil(width / (float) tileSize);
        int rows = (int) Math.ceil(height / (float) tileSize);
        tileMap = new TileMap(tilesetTexture, cols, rows, tileSize, 8, 8);

        player = new Player(width * 0.25f, height * 0.5f, playerTexture);
        boss = new Boss(width * 0.75f, height * 0.5f, bossTexture);
    }

    public void update(float dt) {

        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);

        if (gameOver) {
            if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) {
                restartRequested = true;
            }

            if (restartRequested) {
                restartGame();
            }
            return;
        }

        player.update(dt, window, width, height);

        shootTimer = Math.max(0, shootTimer - dt);
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && shootTimer == 0) {
            bullets.add(player.shoot(bulletTexture));
            shootTimer = shootCooldown;
        }

        bullets.forEach(b -> b.update(dt));
        bullets.removeIf(b -> !b.alive);

        boss.update(dt, player, bullets, width, height);

        checkCollisions();

        // 🔹 If player out of lives, mark game over
        if (player.getLives() <= 0) {
            gameOver = true;
            System.out.println("GAME OVER");
        }
    }

    private void checkCollisions() {
        // Player bullets hit boss
        for (Bullet b : bullets) {
            if (b.friendly && b.alive &&
                    rectOverlap(b, boss)) {
                b.alive = false;
                boss.takeDamage(5f);
            }
        }

        // Boss bullets hit player
        for (Bullet b : bullets) {
            if (!b.friendly && b.alive &&
                    rectOverlap(b, player)) {
                b.alive = false;
                player.takeHit();
            }
        }
    }

    private boolean rectOverlap(Entity a, Entity b) {
        return Math.abs(a.x - b.x) * 2 < (a.width + b.width) &&
                Math.abs(a.y - b.y) * 2 < (a.height + b.height);
    }

    public void render() {

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        tileMap.render();

        player.render();
        boss.render();
        bullets.forEach(Bullet::render);

        drawBossHealthBar();
        drawPlayerHealthBar();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        if (gameOver) {
            // Dark panel
            glDisable(GL_TEXTURE_2D);
            glColor4f(0f, 0f, 0f, 0.6f);
            drawRect(width / 2f, height / 2f, 400, 120);
            glEnable(GL_TEXTURE_2D);

            // Text on top
            drawText(width / 2f - 120, height / 2f + 10, "GAME OVER");
            drawText(width / 2f - 180, height / 2f - 20, "Press R to restart");

        }

    }

    private void drawBossHealthBar() {
        float barWidth = width * 0.6f;
        float barHeight = 20f;
        float x = (width - barWidth) / 2f;
        float y = height - 50;

        glDisable(GL_TEXTURE_2D);
        glColor3f(0.2f, 0.2f, 0.2f);
        drawRect(x, y, barWidth, barHeight);

        float filled = boss.getHealth01() * barWidth;

        switch (boss.phase) {
            case PHASE1 -> glColor3f(0.2f, 0.7f, 0.2f);
            case PHASE2 -> glColor3f(0.9f, 0.7f, 0.0f);
            case ENRAGED -> glColor3f(0.9f, 0.2f, 0.2f);
        }

        drawRect(x, y, filled, barHeight);
        glColor3f(1f, 1f, 1f);
        glEnable(GL_TEXTURE_2D);
    }

    private void drawPlayerHealthBar() {
        // Simple lives-based bar (bottom-left)
        int lives = player.getLives();
        int maxLives = player.getMaxLives();

        float barWidth = 150f;
        float barHeight = 16f;
        float x = 40f;
        float y = 40f;

        float ratio = Math.max(0f, lives / (float) maxLives);
        float filled = barWidth * ratio;

        // Background
        glDisable(GL_TEXTURE_2D);
        glColor3f(0.2f, 0.2f, 0.2f);
        drawRect(x, y, barWidth, barHeight);

        // Fill colour – goes from green → yellow → red
        if (ratio > 0.66f) {
            glColor3f(0.2f, 0.8f, 0.2f);
        } else if (ratio > 0.33f) {
            glColor3f(0.9f, 0.8f, 0.2f);
        } else {
            glColor3f(0.9f, 0.2f, 0.2f);
        }

        drawRect(x, y, filled, barHeight);

        // Reset state
        glEnable(GL_TEXTURE_2D);
        glColor3f(1f, 1f, 1f);
    }

    private void restartGame() {
        System.out.println("Restarting game...");

        // Reset player
        player = new Player(width * 0.25f, height * 0.5f, playerTexture);

        // Reset boss
        boss = new Boss(width * 0.75f, height * 0.5f, bossTexture);

        // Clear bullets
        bullets.clear();

        // Reset timers
        shootTimer = 0f;

        // Reset game state flags
        gameOver = false;
        restartRequested = false;
    }

    public static void drawRect(float cx, float cy, float w, float h) {
        float hw = w / 2, hh = h / 2;
        glBegin(GL_QUADS);
        glVertex2f(cx - hw, cy - hh);
        glVertex2f(cx + hw, cy - hh);
        glVertex2f(cx + hw, cy + hh);
        glVertex2f(cx - hw, cy + hh);
        glEnd();
    }

    // Draw text at (x, y) in pixels. (0,0) is top-left of the window here.
    // Colour is (r, g, b) in 0..1.
    private void drawText(float x, float y, String text, float r, float g, float b) {
        if (text == null || text.isEmpty())
            return;

        // Allocate buffer for vertices (STBEasyFont uses 16 bytes per quad)
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);

        // -------- Save current matrices --------
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        // -------- Set up a clean 2D, Y-down projection for text --------
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // NOTE: height, 0 => Y goes downwards, top-left origin
        glOrtho(0, width, height, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Generate text geometry into the buffer
        int numQuads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);

        // Untextured, coloured text
        glDisable(GL_TEXTURE_2D);
        glColor3f(r, g, b);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, numQuads * 4);
        glDisableClientState(GL_VERTEX_ARRAY);

        // Restore state
        glColor3f(1f, 1f, 1f);
        glEnable(GL_TEXTURE_2D);

        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW); // leave it in modelview mode
    }

    // Convenience overload: white text
    private void drawText(float x, float y, String text) {
        drawText(x, y, text, 1f, 1f, 1f);
    }

    public void dispose() {
        tilesetTexture.dispose();
        playerTexture.dispose();
        bossTexture.dispose();
        bulletTexture.dispose();
    }
}
