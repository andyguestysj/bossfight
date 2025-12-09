package bossfight;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import org.lwjgl.stb.STBImage;

public class Texture {

  public final int id;
  public final int width;
  public final int height;

  public Texture(String path) {

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer comp = BufferUtils.createIntBuffer(1);

    STBImage.stbi_set_flip_vertically_on_load(true);
    ByteBuffer image = STBImage.stbi_load(path, w, h, comp, 4);

    if (image == null)
      throw new RuntimeException("Failed to load texture: " + path);

    width = w.get(0);
    height = h.get(0);

    id = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, id);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8,
        width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

    STBImage.stbi_image_free(image);
  }

  public void bind() {
    glBindTexture(GL_TEXTURE_2D, id);
  }

  public void dispose() {
    glDeleteTextures(id);
  }
}
