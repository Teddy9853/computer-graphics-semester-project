package semester_project;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;
import java.util.Random;

public class Semester_project {

    private static FloatBuffer lightPosition;
    private static FloatBuffer whiteLight;
    private static FloatBuffer globalAmbient;
    private static FloatBuffer lightAmbient;

    private static float lightAngle = 0.0f;

    private static Random random = new Random();

    private static float slimeX;
    private static float slimeZ;
    private static float slimeTargetX;
    private static float slimeTargetZ;
    private static boolean slimeInitialized = false;

    public static void main(String[] args) throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("CS4450 Final Project");
        Display.create();

        initGL();

        Chunk chunk = new Chunk(0, -10, -20);
        Camera camera = new Camera(0, 0, 5);

        Mouse.setGrabbed(true);

        while (!Display.isCloseRequested()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();

            if (Keyboard.isKeyDown(Keyboard.KEY_Q) ||
                Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                break;
            }

            camera.yaw(Mouse.getDX() * 0.1f);
            camera.pitch(Mouse.getDY() * 0.1f);

            if (Keyboard.isKeyDown(Keyboard.KEY_W)) camera.walkForward(0.3f);
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) camera.walkBackwards(0.3f);
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) camera.strafeLeft(0.3f);
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) camera.strafeRight(0.3f);
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) camera.moveUp(0.3f);
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) camera.moveDown(0.3f);

            camera.lookThrough();

            // ---- Orbiting light ----
            lightAngle += 0.01f;

            float radius = 40.0f;
            float lightX = (float) (radius * Math.cos(lightAngle));
            float lightZ = (float) (radius * Math.sin(lightAngle));
            float lightY = 35.0f + (float) (10.0f * Math.sin(lightAngle * 0.5f));

            lightPosition.clear();
            lightPosition.put(lightX).put(lightY).put(lightZ).put(1.0f);
            lightPosition.flip();

            glLight(GL_LIGHT0, GL_POSITION, lightPosition);

            // ---- Draw terrain ----
            chunk.render();

            // ---- Random moving slime ----
            updateAndDrawSlime(chunk);

            // ---- Yellow sun cube ----
            drawLightCube(lightX, lightY, lightZ);

            Display.update();
            Display.sync(60);
        }

        Display.destroy();
    }

    private static void updateAndDrawSlime(Chunk chunk) {
        if (!slimeInitialized) {
            slimeX = 5.0f + random.nextFloat() * 50.0f;
            slimeZ = -15.0f + random.nextFloat() * 50.0f;
            randomizeSlimeTarget();
            slimeInitialized = true;
        }

        float dx = slimeTargetX - slimeX;
        float dz = slimeTargetZ - slimeZ;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);

        if (distance < 1.0f) {
            randomizeSlimeTarget();
        } else {
            float speed = 0.03f;
            slimeX += (dx / distance) * speed;
            slimeZ += (dz / distance) * speed;
        }

        float surfaceY = chunk.getSurfaceY((int) slimeX, (int) slimeZ);
        float jumpHeight = Math.abs((float) Math.sin(lightAngle * 3.0f)) * 2.0f;

        drawSlimeCube(slimeX, surfaceY + jumpHeight, slimeZ);
    }

    private static void randomizeSlimeTarget() {
        slimeTargetX = 5.0f + random.nextFloat() * 50.0f;
        slimeTargetZ = -15.0f + random.nextFloat() * 50.0f;
    }

    private static void initGL() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float aspect = 640f / 480f;
        glFrustum(-aspect, aspect, -1, 1, 1, 1000);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);

        initLightArrays();

        glShadeModel(GL_SMOOTH);
        glEnable(GL_NORMALIZE);

        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        glLightModel(GL_LIGHT_MODEL_AMBIENT, globalAmbient);

        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, lightAmbient);

        glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 1.0f);
        glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 0.002f);
        glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.00002f);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
    }

    private static void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(40.0f).put(35.0f).put(0.0f).put(1.0f).flip();

        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();

        globalAmbient = BufferUtils.createFloatBuffer(4);
        globalAmbient.put(0.35f).put(0.35f).put(0.35f).put(1.0f).flip();

        lightAmbient = BufferUtils.createFloatBuffer(4);
        lightAmbient.put(0.15f).put(0.15f).put(0.15f).put(1.0f).flip();
    }

    private static void drawLightCube(float x, float y, float z) {
        float size = 6.0f;
        drawColoredCube(x, y, z, size, 1.0f, 1.0f, 0.0f, true);
    }

    private static void drawSlimeCube(float x, float y, float z) {
        float size = 1.0f;
        float s = size / 2.0f;

        glPushMatrix();
        glTranslatef(x, y + s, z);

        drawColoredCubeAtOrigin(size, 1.0f, 0.0f, 0.0f, false);

        glPopMatrix();
    }

    private static void drawColoredCube(float x, float y, float z,
                                        float size,
                                        float r, float g, float b,
                                        boolean disableLighting) {
        glPushMatrix();
        glTranslatef(x, y, z);

        drawColoredCubeAtOrigin(size, r, g, b, disableLighting);

        glPopMatrix();
    }

    private static void drawColoredCubeAtOrigin(float size,
                                                float r, float g, float b,
                                                boolean disableLighting) {
        float s = size / 2.0f;

        if (disableLighting) {
            glDisable(GL_LIGHTING);
        }

        glDisable(GL_TEXTURE_2D);
        glColor3f(r, g, b);

        glBegin(GL_QUADS);

        // TOP
        glVertex3f( s,  s, -s);
        glVertex3f(-s,  s, -s);
        glVertex3f(-s,  s,  s);
        glVertex3f( s,  s,  s);

        // BOTTOM
        glVertex3f( s, -s,  s);
        glVertex3f(-s, -s,  s);
        glVertex3f(-s, -s, -s);
        glVertex3f( s, -s, -s);

        // FRONT
        glVertex3f( s,  s,  s);
        glVertex3f(-s,  s,  s);
        glVertex3f(-s, -s,  s);
        glVertex3f( s, -s,  s);

        // BACK
        glVertex3f( s, -s, -s);
        glVertex3f(-s, -s, -s);
        glVertex3f(-s,  s, -s);
        glVertex3f( s,  s, -s);

        // LEFT
        glVertex3f(-s,  s,  s);
        glVertex3f(-s,  s, -s);
        glVertex3f(-s, -s, -s);
        glVertex3f(-s, -s,  s);

        // RIGHT
        glVertex3f( s,  s, -s);
        glVertex3f( s,  s,  s);
        glVertex3f( s, -s,  s);
        glVertex3f( s, -s, -s);

        glEnd();

        glColor3f(1.0f, 1.0f, 1.0f);
        glEnable(GL_TEXTURE_2D);

        if (disableLighting) {
            glEnable(GL_LIGHTING);
        }
    }
}