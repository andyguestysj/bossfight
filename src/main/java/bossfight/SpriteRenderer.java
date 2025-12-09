package bossfight;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

public class SpriteRenderer {

  // Default: no rotation
  public static void drawSprite(Texture tex, float cx, float cy, float w, float h) {
    drawRotatedSprite(tex, cx, cy, w, h, 0f);
  }

  // NEW: draw with rotation (angle in degrees, clockwise)
  public static void drawRotatedSprite(Texture tex,
      float cx, float cy,
      float w, float h,
      float angleDeg) {
    tex.bind();

    float hw = w / 2f;
    float hh = h / 2f;

    glPushMatrix();
    glTranslatef(cx, cy, 0f);
    glRotatef(angleDeg, 0f, 0f, 1f); // rotate around Z axis

    glBegin(GL_QUADS);
    glTexCoord2f(0f, 0f);
    glVertex2f(-hw, -hh);
    glTexCoord2f(1f, 0f);
    glVertex2f(hw, -hh);
    glTexCoord2f(1f, 1f);
    glVertex2f(hw, hh);
    glTexCoord2f(0f, 1f);
    glVertex2f(-hw, hh);
    glEnd();

    glPopMatrix();
  }

  public static void drawSubSprite(Texture tex,
      float cx, float cy, float w, float h,
      float u0, float v0, float u1, float v1) {
    tex.bind();

    float hw = w / 2f;
    float hh = h / 2f;

    glBegin(GL_QUADS);
    glTexCoord2f(u0, v0);
    glVertex2f(cx - hw, cy - hh);
    glTexCoord2f(u1, v0);
    glVertex2f(cx + hw, cy - hh);
    glTexCoord2f(u1, v1);
    glVertex2f(cx + hw, cy + hh);
    glTexCoord2f(u0, v1);
    glVertex2f(cx - hw, cy + hh);
    glEnd();
  }
}
