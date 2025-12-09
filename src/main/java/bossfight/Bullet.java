package bossfight;

public class Bullet extends Entity {

    float vx, vy;
    boolean friendly;
    boolean alive = true;

    public Bullet(float x, float y, float vx, float vy,
            boolean friendly, Texture tex) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.friendly = friendly;
        this.texture = tex;
        this.width = 10;
        this.height = 10;
    }

    public void update(float dt) {
        x += vx * dt;
        y += vy * dt;
        if (x < -50 || x > 2000 || y < -50 || y > 2000)
            alive = false;
    }

    public void render() {
        // Angle the bullet in the direction it’s moving
        float angleRad = (float) Math.atan2(vy, vx);
        float angleDeg = (float) Math.toDegrees(angleRad);

        SpriteRenderer.drawRotatedSprite(texture, x, y, width, height, angleDeg);
    }
}
