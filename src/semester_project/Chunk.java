package semester_project;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author Victor, Tai Ji Chen
 * GROUP: 404 Bug Not Found
 * CS4450 Spring 2026
 *
 */

public class Chunk {
    private int VBOTextureHandle;
    private int VBOVertexHandle;
    private int VBONormalHandle;
    private Texture texture;

    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    static final int WATER_LEVEL = 4;

    private Block[][][] Blocks;
    private int vertexCount;

    private int StartX, StartY, StartZ;

    public Chunk(int startX, int startY, int startZ) {
        try {
            texture = TextureLoader.getTexture("PNG",
                    ResourceLoader.getResourceAsStream("terrain.png"));
            System.out.println("terrain.png loaded successfully");
        } catch (Exception e) {
            System.out.println("Could not load terrain.png");
            e.printStackTrace();
        }

        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

        StartX = startX;
        StartY = startY;
        StartZ = startZ;

        generateTerrain();
        rebuildMesh(startX, startY, startZ);
    }

    private void generateTerrain() {
        int seed = new Random().nextInt();
        NoiseGenerate noise = new NoiseGenerate(32, 0.5, seed);

        int minHeight = 2;
        int maxHeight = 12;
        int[][] heights = noise.generateChunkHeights(0, 0, minHeight, maxHeight);

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Default);
                    Blocks[x][y][z].setActive(false);
                }
            }
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int height = heights[x][z];

                for (int y = 0; y <= height && y < CHUNK_SIZE; y++) {
                    Block block;

                    if (y == 0) {
                        block = new Block(Block.BlockType.BlockType_Bedrock);
                    } else if (y == height) {
                        if (height <= WATER_LEVEL) {
                            block = new Block(Block.BlockType.BlockType_Sand);
                        } else {
                            block = new Block(Block.BlockType.BlockType_Grass);
                        }
                    } else if (y >= height - 2) {
                        block = new Block(Block.BlockType.BlockType_Dirt);
                    } else {
                        block = new Block(Block.BlockType.BlockType_Stone);
                    }

                    block.setActive(true);
                    Blocks[x][y][z] = block;
                }

                for (int y = height + 1; y <= WATER_LEVEL && y < CHUNK_SIZE; y++) {
                    Block water = new Block(Block.BlockType.BlockType_Water);
                    water.setActive(true);
                    Blocks[x][y][z] = water;
                }
            }
        }
    }

    public void render() {
        glPushMatrix();

        glEnable(GL_TEXTURE_2D);

        if (texture != null) {
            texture.bind();
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, VBONormalHandle);
        glNormalPointer(GL_FLOAT, 0, 0L);

        glDrawArrays(GL_QUADS, 0, vertexCount);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glPopMatrix();
    }

    public void rebuildMesh(float startX, float startY, float startZ) {
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        VBONormalHandle = glGenBuffers();

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 72);
        FloatBuffer textureData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 48);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 72);

        int activeBlockCount = 0;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    Block block = Blocks[x][y][z];

                    if (block == null || !block.isActive()) {
                        continue;
                    }

                    vertexData.put(createCube(
                            startX + x * CUBE_LENGTH,
                            startY + y * CUBE_LENGTH,
                            startZ + z * CUBE_LENGTH));

                    textureData.put(createCubeTexCoords(block));
                    normalData.put(createCubeNormals());

                    activeBlockCount++;
                }
            }
        }

        vertexData.flip();
        textureData.flip();
        normalData.flip();

        vertexCount = activeBlockCount * 24;

        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, textureData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, VBONormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;

        return new float[] {
            // TOP
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,

            // BOTTOM
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,

            // FRONT
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,

            // BACK
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,

            // LEFT
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,

            // RIGHT
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z
        };
    }

    public static float[] createCubeNormals() {
        return new float[] {
            // TOP
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // BOTTOM
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,

            // FRONT
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // BACK
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // LEFT
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // RIGHT
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };
    }

    private float[] createCubeTexCoords(Block block) {
        float tileSize = 1.0f / 16.0f;

        int topCol = 0, topRow = 0;
        int bottomCol = 0, bottomRow = 0;
        int sideCol = 0, sideRow = 0;

        switch (block.GetID()) {
            case 0: // Grass
                topCol = 0;    topRow = 0;
                bottomCol = 2; bottomRow = 0;
                sideCol = 3;   sideRow = 0;
                break;

            case 1: // Sand
                topCol = bottomCol = sideCol = 1;
                topRow = bottomRow = sideRow = 1;
                break;

            case 2: // Water
                topCol = bottomCol = sideCol = 13;
                topRow = bottomRow = sideRow = 12;
                break;

            case 3: // Dirt
                topCol = bottomCol = sideCol = 2;
                topRow = bottomRow = sideRow = 0;
                break;

            case 4: // Stone
                topCol = bottomCol = sideCol = 1;
                topRow = bottomRow = sideRow = 0;
                break;

            case 5: // Bedrock
                topCol = bottomCol = sideCol = 1;
                topRow = bottomRow = sideRow = 1;
                break;

            default:
                topCol = bottomCol = sideCol = 0;
                topRow = bottomRow = sideRow = 0;
                break;
        }

        float topU = topCol * tileSize;
        float topV = topRow * tileSize;
        float topU2 = topU + tileSize;
        float topV2 = topV + tileSize;

        float bottomU = bottomCol * tileSize;
        float bottomV = bottomRow * tileSize;
        float bottomU2 = bottomU + tileSize;
        float bottomV2 = bottomV + tileSize;

        float sideU = sideCol * tileSize;
        float sideV = sideRow * tileSize;
        float sideU2 = sideU + tileSize;
        float sideV2 = sideV + tileSize;

        return new float[] {
            // TOP
            topU2, topV,   topU, topV,   topU, topV2,   topU2, topV2,

            // BOTTOM
            bottomU2, bottomV,   bottomU, bottomV,   bottomU, bottomV2,   bottomU2, bottomV2,

            // FRONT
            sideU2, sideV,   sideU, sideV,   sideU, sideV2,   sideU2, sideV2,

            // BACK
            sideU2, sideV,   sideU, sideV,   sideU, sideV2,   sideU2, sideV2,

            // LEFT
            sideU2, sideV,   sideU, sideV,   sideU, sideV2,   sideU2, sideV2,

            // RIGHT
            sideU2, sideV,   sideU, sideV,   sideU, sideV2,   sideU2, sideV2
        };
    }
}