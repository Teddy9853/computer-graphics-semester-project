/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package semester_project;

/**
 *
 * @author user
 */

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import java.nio.FloatBuffer;

public class Semester_project {

    private static FloatBuffer lightPosition;
    private static FloatBuffer whiteLight;
    private static FloatBuffer dimLight;
    private static FloatBuffer noAmbient;
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

            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                camera.walkForward(0.3f);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                camera.walkBackwards(0.3f);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                camera.strafeLeft(0.3f);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                camera.strafeRight(0.3f);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                camera.moveUp(0.3f);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                camera.moveDown(0.3f);
            }

            camera.lookThrough();

            // Light position must be set after the camera transform
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);

            chunk.render();

            Display.update();
            Display.sync(60);
        }

        Display.destroy();
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

        // Make texture color combine with lighting
        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        // Small global ambient so dark side is still visible
        glLightModel(GL_LIGHT_MODEL_AMBIENT, dimLight);

        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, noAmbient);

        // Stronger falloff so one side looks brighter than the other
        glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 1.0f);
        glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 0.02f);
        glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.001f);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
    }

    private static void initLightArrays() {
        // Point light placed far to the left and above the world
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(-120.0f).put(80.0f).put(-20.0f).put(1.0f).flip();

        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();

        dimLight = BufferUtils.createFloatBuffer(4);
        dimLight.put(0.03f).put(0.03f).put(0.03f).put(1.0f).flip();

        noAmbient = BufferUtils.createFloatBuffer(4);
        noAmbient.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
    }
}