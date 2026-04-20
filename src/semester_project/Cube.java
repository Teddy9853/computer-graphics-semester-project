import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Victor, Tai Ji Chen
 * GROUP: 404 Bug Not Found
 * CS4450 Spring 2026
 */
public class Cube {
    
    public void draw() {
        glBegin(GL_QUADS);
        //glEnable(GL_DEPTH_TEST);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        // Front face
        glColor3f(1, 0, 0); // Red
        glVertex3f(-1, -1,  1);
        glVertex3f( 1, -1,  1);
        glVertex3f( 1,  1,  1);
        glVertex3f(-1,  1,  1);

        // Back face
        glColor3f(0, 1, 0); // Green
        glVertex3f(-1, -1, -1);
        glVertex3f(-1,  1, -1);
        glVertex3f( 1,  1, -1);
        glVertex3f( 1, -1, -1);

        // Top face
        glColor3f(0, 0, 1); // Blue
        glVertex3f(-1,  1, -1);
        glVertex3f(-1,  1,  1);
        glVertex3f( 1,  1,  1);
        glVertex3f( 1,  1, -1);

        // Bottom face
        glColor3f(1, 1, 0); // Yellow
        glVertex3f(-1, -1, -1);
        glVertex3f( 1, -1, -1);
        glVertex3f( 1, -1,  1);
        glVertex3f(-1, -1,  1);

        // Right face
        glColor3f(1, 0, 1); // Purple
        glVertex3f( 1, -1, -1);
        glVertex3f( 1,  1, -1);
        glVertex3f( 1,  1,  1);
        glVertex3f( 1, -1,  1);

        // Left face
        glColor3f(0, 1, 1); // Cyan
        glVertex3f(-1, -1, -1);
        glVertex3f(-1, -1,  1);
        glVertex3f(-1,  1,  1);
        glVertex3f(-1,  1, -1);

        glEnd();
    }
}
