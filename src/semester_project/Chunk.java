
package semester_project;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

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
        } catch (Exception e) {
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

        int[][] heights = noise.generateChunkHeights(0, 0, 2, 12);

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
                        block = (height <= WATER_LEVEL)
                                ? new Block(Block.BlockType.BlockType_Sand)
                                : new Block(Block.BlockType.BlockType_Grass);
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
        int o = CUBE_LENGTH / 2;

        return new float[] {
            // TOP
            x + o, y + o, z,
            x - o, y + o, z,
            x - o, y + o, z - CUBE_LENGTH,
            x + o, y + o, z - CUBE_LENGTH,

            // BOTTOM
            x + o, y - o, z - CUBE_LENGTH,
            x - o, y - o, z - CUBE_LENGTH,
            x - o, y - o, z,
            x + o, y - o, z,

            // FRONT
            x + o, y + o, z - CUBE_LENGTH,
            x - o, y + o, z - CUBE_LENGTH,
            x - o, y - o, z - CUBE_LENGTH,
            x + o, y - o, z - CUBE_LENGTH,

            // BACK
            x + o, y - o, z,
            x - o, y - o, z,
            x - o, y + o, z,
            x + o, y + o, z,

            // LEFT
            x - o, y + o, z - CUBE_LENGTH,
            x - o, y + o, z,
            x - o, y - o, z,
            x - o, y - o, z - CUBE_LENGTH,

            // RIGHT
            x + o, y + o, z,
            x + o, y + o, z - CUBE_LENGTH,
            x + o, y - o, z - CUBE_LENGTH,
            x + o, y - o, z
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
        float ts = 1.0f / 16.0f;

        int tc = 0, tr = 0;
        int bc = 0, br = 0;
        int sc = 0, sr = 0;

        switch (block.GetID()) {
            case 0: // Grass: top = green cotton
                tc = 2; tr = 9;
                bc = 2; br = 0;
                sc = 3; sr = 0;
                break;

            case 1: // Sand
                tc = bc = sc = 1;
                tr = br = sr = 1;
                break;

            case 2: // Water
                tc = bc = sc = 13;
                tr = br = sr = 12;
                break;

            case 3: // Dirt
                tc = bc = sc = 2;
                tr = br = sr = 0;
                break;

            case 4: // Stone
                tc = bc = sc = 1;
                tr = br = sr = 0;
                break;

            case 5: // Bedrock
                tc = bc = sc = 1;
                tr = br = sr = 1;
                break;

            default:
                tc = bc = sc = 0;
                tr = br = sr = 0;
                break;
        }

        float topU = tc * ts;
        float topV = tr * ts;
        float topU2 = topU + ts;
        float topV2 = topV + ts;

        float bottomU = bc * ts;
        float bottomV = br * ts;
        float bottomU2 = bottomU + ts;
        float bottomV2 = bottomV + ts;

        float sideU = sc * ts;
        float sideV = sr * ts;
        float sideU2 = sideU + ts;
        float sideV2 = sideV + ts;

        return new float[] {
            // TOP
            topU2, topV,
            topU,  topV,
            topU,  topV2,
            topU2, topV2,

            // BOTTOM
            bottomU2, bottomV,
            bottomU,  bottomV,
            bottomU,  bottomV2,
            bottomU2, bottomV2,

            // FRONT
            sideU2, sideV,
            sideU,  sideV,
            sideU,  sideV2,
            sideU2, sideV2,

            // BACK
            sideU2, sideV2,
            sideU,  sideV2,
            sideU,  sideV,
            sideU2, sideV,

            // LEFT
            sideU2, sideV,
            sideU,  sideV,
            sideU,  sideV2,
            sideU2, sideV2,

            // RIGHT
            sideU2, sideV,
            sideU,  sideV,
            sideU,  sideV2,
            sideU2, sideV2
        };
    }
}