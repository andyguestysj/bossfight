package bossfight;

public class TileMap {

  private final int cols, rows;
  private final int tileSize;
  private final int[][] tiles;

  private final Texture tileset;
  private final int tilesetCols, tilesetRows;

  public TileMap(Texture tileset, int cols, int rows,
      int tileSize, int tilesetCols, int tilesetRows) {
    this.tileset = tileset;
    this.cols = cols;
    this.rows = rows;
    this.tileSize = tileSize;
    this.tilesetCols = tilesetCols;
    this.tilesetRows = tilesetRows;

    tiles = new int[rows][cols];

    // Border walls
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        boolean border = (x == 0 || y == 0 || x == cols - 1 || y == rows - 1);
        tiles[y][x] = border ? 1 : 0;
      }
    }
  }

  public void render() {

    float tu = 1f / tilesetCols;
    float tv = 1f / tilesetRows;

    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {

        int index = tiles[y][x];
        int tx = index % tilesetCols;
        int ty = index / tilesetCols;

        float u0 = tx * tu;
        float v0 = ty * tv;
        float u1 = u0 + tu;
        float v1 = v0 + tv;

        float cx = x * tileSize + tileSize / 2f;
        float cy = y * tileSize + tileSize / 2f;

        SpriteRenderer.drawSubSprite(
            tileset, cx, cy, tileSize, tileSize,
            u0, v0, u1, v1);
      }
    }
  }
}
