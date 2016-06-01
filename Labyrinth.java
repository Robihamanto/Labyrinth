/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ProjectAkhir;

import Learn.MineFront;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;

/**
 *
 * @author Robi
 */
public class Labyrinth {

    public static final int DISPLAY_HEIGHT = 480;
    public static final int DISPLAY_WIDTH = 640;
    public static final Logger LOGGER = Logger.getLogger(Field.class.getName());

    /**
     * Defines if the application is resizable.
     */
    private static final boolean resizable = true;

    /**
     * The position of the player as a 3D vector (xyz).
     */
    private static Vector3f position = new Vector3f(0, 0, 0);
    /**
     * The rotation of the axis (where to the player looks). The X component
     * stands for the rotation along the x-axis, where 0 is dead ahead, 180 is
     * backwards, and 360 is automically set to 0 (dead ahead). The value must
     * be between (including) 0 and 360. The Y component stands for the rotation
     * along the y-axis, where 0 is looking straight ahead, -90 is straight up,
     * and 90 is straight down. The value must be between (including) -90 and
     * 90.
     */
    private static Vector3f rotation = new Vector3f(0, 0, 0);
    /**
     * The minimal distance from the camera where objects are rendered.
     */
    private static final float zNear = 0.3f;
    /**
     * The width and length of the floor and ceiling. Don't put anything above
     * 1000, or OpenGL will start to freak out, though.
     */
    private static final int gridSize = 10;
    /**
     * The size of tiles, where 0.5 is the standard size. Increasing the size by
     * results in smaller tiles, and vice versa.
     */

    private static final float tileSize = 0.20f;
    /**
     * The maximal distance from the camera where objects are rendered.
     */
    private static final float zFar = 20f;
    /**
     * The distance where fog starts appearing.
     */
    private static final float fogNear = 9f;
    /**
     * The distance where the fog stops appearing (fully black here)
     */
    private static final float fogFar = 13f;
    /**
     * The color of the fog in rgba.
     */
    private static final Color fogColor = new Color(0f, 0f, 0f, 1f);
    /**
     * Defines if the application utilizes full-screen.
     */
    private static final boolean fullscreen = false;
    /**
     * Defines the walking speed, where 10 is the standard.
     */
    private static int walkingSpeed = 10;
    /**
     * Defines the mouse speed.
     */
    private static int mouseSpeed = 2;
    /**
     * Defines if the application utilizes vertical synchronization (eliminates
     * screen tearing; caps fps to 60fps)
     */
    private static final boolean vsync = true;
    /**
     * Defines if the applications prints its frames-per-second to the console.
     */
    private static final boolean printFPS = false;
    /**
     * Defines the maximum angle at which the player can look up.
     */
    private static final int maxLookUp = 85;
    /**
     * Defines the minimum angle at which the player can look down.
     */
    private static final int maxLookDown = -85;
    /**
     * The height of the ceiling.
     */
    private static final float ceilingHeight = 10;
    /**
     * The height of the floor.
     */
    private static final float floorHeight = -1;
    /**
     * Defines the field of view.
     */
    private static final int fov = 68;

    private static int fps;
    private static int delta;
    private static long lastFPS;
    private static long lastFrame;

    public static void main(String[] args) {
        Field main = null;
        try {
            main = new Field();
            main.create();
            main.run();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        } finally {
            if (main != null) {
                main.destroy();
            }
        }
    }

    public void create() throws LWJGLException {

        Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
        Display.setResizable(resizable);
        Display.setTitle("Field");
        Display.create();

        //Create Keyboard
        Keyboard.create();

        //Create Mouse
        Mouse.setGrabbed(false);
        Mouse.create();

        initGL();
        resizeGL();

    }

    public void resizeGL() {
        //2D Scene
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnable(GL_FOG);
    }

    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private static int getDelta() {
        long currentTime = getTime();
        int delta = (int) (currentTime - lastFrame);
        lastFrame = getTime();
        return delta;
    }

    private static void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            if (printFPS) {
                System.out.println("FPS: " + fps);
            }
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    void destroy() {
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy();
    }

    public void initGL() {
        //2D Initialization
        glColor3b((byte) 255, (byte) 0, (byte) 0);
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    public void processKeyboard() {
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        boolean moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean moveSlower = Keyboard.isKeyDown(Keyboard.KEY_TAB);

        if (moveFaster && !moveSlower) {
            walkingSpeed *= 4f;
        }
        if (moveSlower && !moveFaster) {
            walkingSpeed /= 10f;
        }

        if (keyUp && keyRight && !keyLeft && !keyDown) {
            float angle = rotation.y + 45;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            float angle = rotation.y - 45;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            float angle = rotation.y;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            float angle = rotation.y - 135;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            float angle = rotation.y + 135;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            float angle = rotation.y;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = -(walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            float angle = rotation.y - 90;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            float angle = rotation.y + 90;
            Vector3f newPosition = new Vector3f(position);
            float hypotenuse = (walkingSpeed * 0.0002f) * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            newPosition.z += adjacent;
            newPosition.x -= opposite;
            position.z = newPosition.z;
            position.x = newPosition.x;
        }
        if (flyUp && !flyDown) {
            double newPositionY = (walkingSpeed * 0.0002) * delta;
            position.y -= newPositionY;
        }
        if (flyDown && !flyUp) {
            double newPositionY = (walkingSpeed * 0.0002) * delta;
            position.y += newPositionY;
        }
        if (moveFaster && !moveSlower) {
            walkingSpeed /= 4f;
        }
        if (moveSlower && !moveFaster) {
            walkingSpeed *= 10f;
        }

        while (Keyboard.next()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                position = new Vector3f(0, 0, 0);
                rotation = new Vector3f(0, 0, 0);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                mouseSpeed += 1;
                System.out.println("Mouse speed changed to " + mouseSpeed + ".");
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                if (mouseSpeed - 1 > 0) {
                    mouseSpeed -= 1;
                    System.out.println("Mouse speed changed to " + mouseSpeed + ".");
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                System.out.println("Walking speed changed to " + walkingSpeed + ".");
                walkingSpeed += 1;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                System.out.println("Walking speed changed to " + walkingSpeed + ".");
                walkingSpeed -= 1;
            }
        }
    }

    public void processMouse() {
        if (Mouse.isGrabbed()) {
            float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
            float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
            if (rotation.y + mouseDX >= 360) {
                rotation.y = rotation.y + mouseDX - 360;
            } else if (rotation.y + mouseDX < 0) {
                rotation.y = 360 - rotation.y + mouseDX;
            } else {
                rotation.y += mouseDX;
            }
            if (rotation.x - mouseDY >= maxLookDown && rotation.x - mouseDY <= maxLookUp) {
                rotation.x += -mouseDY;
            } else if (rotation.x - mouseDY < maxLookDown) {
                rotation.x = maxLookDown;
            } else if (rotation.x - mouseDY > maxLookUp) {
                rotation.x = maxLookUp;
            }
        }

        while (Mouse.next()) {
            if (Mouse.isButtonDown(0)) {
                Mouse.setGrabbed(true);
            }
            if (Mouse.isButtonDown(1)) {
                Mouse.setGrabbed(false);
            }
        }
    }

    void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        delta = getDelta();

//        glEnable(GL_CULL_FACE);
//        glDisable(GL_DEPTH_TEST);
//        glEnable(GL_DEPTH_TEST);
//        glDisable(GL_CULL_FACE);
//        glBindTexture(GL_TEXTURE_2D, 0);
//
//        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_TEXTURE_2D);
//        glEnable(GL_BLEND);
//        glEnable(GL_ALPHA_TEST);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
//        glEnable(GL_FOG);

        glLoadIdentity();
        glRotatef(rotation.x, 1, 0, 0);
        glRotatef(rotation.y, 0, 1, 0);
        glRotatef(rotation.z, 0, 0, 1);
        glTranslatef(position.x, position.y, position.z);

        if (resizable) {
            if (Display.wasResized()) {
                glViewport(0, 0, Display.getWidth(), Display.getHeight());
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                gluPerspective(fov, (float) Display.getWidth() / (float) Display.getHeight(), zNear, zFar);
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();
            }
        }

        //Draw Sky
//        drawSky();
        //Draw Floor
            glColor3f(1.0f, 1.0f, 1.0f);
            glBegin(GL_QUADS);
            glVertex3f(-100f,-0.5f,100f);
            glVertex3f(100f,-0.5f,100f);
            glVertex3f(100f,-0.5f,-100f);
            glVertex3f(-100f,-0.5f,-100f);
            glEnd();
        //Draw wall
        glColor3f(0.0f, 1.0f, 0.0f);
        glBegin(GL_QUADS);

//            Outer front left
        drawWall(-1.0f, -12.0f, -1.5f, -2.5f);

//            Outer front right
        drawWall(0.0f, 13.0f, -1.5f, -2.5f);

//          Outer Back
        drawWall(12.5f, -11.0f, -24.5f, -25.5f);

//          Outer Left
        drawWall(-11.5f, -12.0f, -2.0f, -25.5f);

//          Outer Right
        drawWall(12.5f, 12.0f, -2.0f, -25.5f);

//          Vertical left to right
        //Line 1
        drawWall(-9.5f, -10.0f, -3.5f, -5.5f);
        drawWall(-9.5f, -10.0f, -6.5f, -8.5f);
        drawWall(-9.5f, -11.5f, -10.5f, -11.5f);
        drawWall(-9.5f, -10.0f, -12.5f, -23.5f);
        drawWall(-6.5f, -10.0f, -22.5f, -23.5f);

        //Line 2
        drawWall(-7.5f, -8.0f, -3.5f, -7.5f);
        drawWall(-7.5f, -8.0f, -8.5f, -12.5f);
        drawWall(-7.5f, -8.0f, -12.5f, -13.5f);
        drawWall(-7.5f, -8.0f, -17.5f, -21.5f);
        drawWall(-4.5f, -8.0f, -20.5f, -21.5f);

        //Line 3
        drawWall(-5.5f, -6.0f, -5.5f, -9.5f);
        drawWall(-3.5f, -6.0f, -10.5f, -11.5f);
        drawWall(-5.5f, -8.0f, -14.0f, -15.0f);
        drawWall(-5.5f, -6.0f, -12.5f, -19.5f);

//          Line 4
        drawWall(-3.5f, -4.0f, -3.5f, -6.5f);
        drawWall(-2.5f, -3.0f, -7.5f, -8.5f);
        drawWall(-3.5f, -4.0f, -7.5f, -13.5f);
        drawWall(-3.5f, -4.0f, -14.5f, -23.5f);
        drawWall(0.5f, -3.0f, -15.5f, -16.5f);
        drawWall(-2.5f, -3.0f, -19.5f, -20.5f);
        drawWall(-2.5f, -3.0f, -22.5f, -23.5f);

//          Line 5
        drawWall(-1.5f, -2.0f, -5.5f, -8.5f);
        drawWall(-1.5f, -2.0f, -9.5f, -16.5f);
        drawWall(5.5f, -2.0f, -11.5f, -12.5f);
        drawWall(-1.5f, -2.0f, -17.5f, -21.5f);
        drawWall(3.5f, -2.0f, -21.0f, -22.0f);
        drawWall(-0.5f, -1.0f, -22.5f, -25.5f);

//          Line 6
        drawWall(0.5f, 0.0f, -3.5f, -10.5f);
        drawWall(0.5f, 0.0f, -16.5f, -18f);
        drawWall(2.5f, 0.0f, -19.0f, -20.0f);
        drawWall(0.5f, 2.0f, -21.5f, -23.5f);

//          Line 7
        drawWall(2.5f, 2.0f, -5.5f, -11.5f);
        drawWall(3.5f, 2.0f, -15.5f, -16.5f);
        drawWall(1.5f, 3.0f, -15.5f, -19.0f);
        drawWall(2.0f, 3.f, -15.5f, -19.0f);

//          Line 8
        drawWall(3.5f, 5.0f, -3.5f, -10.5f);
        drawWall(3.5f, 5.0f, -11.5f, -16.5f);
        drawWall(3.5f, 5.0f, -17.5f, -22.0f);
        drawWall(3.0f, 6.0f, -22.5f, -23.5f);

//          Line 9
        drawWall(5.5f, 9.0f, -5.5f, -6.5f);
        drawWall(5.5f, 7.0f, -5.5f, -12.5f);
        drawWall(6.0f, 8.0f, -9.25f, -11.0f);
        drawWall(5.0f, 7.25f, -13.25f, -14.5f);
        drawWall(6.0f, 8.5f, -15.0f, -16.0f);
        drawWall(4.0f, 7.5f, -17.5f, -18.5f);
        drawWall(5.0f, 6.5f, -19.0f, -23.5f);
        drawWall(6.0f, 7.5f, -15.5f, -20.0f);
        drawWall(7.0f, 9.0f, -18.0f, -19.0f);

//          Line 9
        drawWall(7.0f, 9.5f, -7.25f, -8.5f);
        drawWall(7.0f, 9.5f, -11.5f, -12.5f);
        drawWall(8.0f, 9.5f, -5.5f, -12.5f);
        drawWall(6.5f, 9.0f, -21.0f, -22.0f);
        drawWall(7.5f, 9.0f, -18.0f, -21.0f);
        drawWall(6.5f, 8.0f, -22.5f, -25.5f);

//          Line 10
        drawWall(9.75f, 11.25f, -4.5f, -14.5f);
        drawWall(8.25f, 11.25f, -13.5f, -14.5f);
        drawWall(8.5f, 10.0f, -13.5f, -17.5f);
        drawWall(10.5f, 12.0f, -15.25f, -16.0f);
        drawWall(8.0f, 11.0f, -16.5f, -17.5f);
        drawWall(9.5f, 12.0f, -17.5f, -19.0f);
        drawWall(9.5f, 11.0f, -17.5f, -19.5f);
        drawWall(9.5f, 11.0f, -20.5f, -23.5f);
        drawWall(8.0f, 11.0f, -22.5f, -23.5f);

//            Horizontal from down to up
        drawWall(10.75f, -9.5f, -3.5f, -4.5f);
        drawWall(-5.5f, -7.5f, -6.5f, -7.5f);
        drawWall(-7.5f, -10.0f, -8.5f, -9.5f);
        drawWall(-7.5f, -10.0f, -12.5f, -13.5f);
        drawWall(-7.5f, -10.0f, -15.5f, -16.5f);

        glEnd();
    }

    public void drawSky() {
//        int floorTexture = glGenTextures();
//        {
//            InputStream in = null;
//            try {
//                in = new FileInputStream("C:/libs/LWJGL/res/images/floor.png");
//                PNGDecoder decoder = new PNGDecoder(in);
//                ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
//                decoder.decode(buffer, decoder.getWidth() * 4, Format.RGBA);
//                buffer.flip();
//                glBindTexture(GL_TEXTURE_2D, floorTexture);
//                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA,
//                        GL_UNSIGNED_BYTE, buffer);
//                glBindTexture(GL_TEXTURE_2D, 0);
//            } catch (FileNotFoundException ex) {
//                System.err.println("Failed to find the texture files.");
//                ex.printStackTrace();
//                Display.destroy();
//                System.exit(1);
//            } catch (IOException ex) {
//                System.err.println("Failed to load the texture files.");
//                ex.printStackTrace();
//                Display.destroy();
//                System.exit(1);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        int ceilingDisplayList = glGenLists(1);
//        glNewList(ceilingDisplayList, GL_COMPILE);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex3f(-gridSize, ceilingHeight, -gridSize);
        glTexCoord2f(gridSize * 10 * tileSize, 0);
        glVertex3f(gridSize, ceilingHeight, -gridSize);
        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
        glVertex3f(gridSize, ceilingHeight, gridSize);
        glTexCoord2f(0, gridSize * 10 * tileSize);
        glVertex3f(-gridSize, ceilingHeight, gridSize);
        glEnd();
//        glEndList();

        int wallDisplayList = glGenLists(1);
        glNewList(wallDisplayList, GL_COMPILE);

        glBegin(GL_QUADS);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex3f(-100f, -0.5f, 100f);
        glTexCoord2f(0, 1);
        glVertex3f(100f, -0.5f, 100f);
        glTexCoord2f(1, 0);
        glVertex3f(100f, -0.5f, -100f);
        glTexCoord2f(0, 0);
        glVertex3f(-100f, -0.5f, -100f);
        glEnd();

        // North wall
//        glTexCoord2f(0, 0);
//        glVertex3f(-gridSize, floorHeight, -gridSize);
//        glTexCoord2f(0, gridSize * 10 * tileSize);
//        glVertex3f(gridSize, floorHeight, -gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
//        glVertex3f(gridSize, ceilingHeight, -gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, 0);
//        glVertex3f(-gridSize, ceilingHeight, -gridSize);
//
//        // West wall
//
//        glTexCoord2f(0, 0);
//        glVertex3f(-gridSize, floorHeight, -gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, 0);
//        glVertex3f(-gridSize, ceilingHeight, -gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
//        glVertex3f(-gridSize, ceilingHeight, +gridSize);
//        glTexCoord2f(0, gridSize * 10 * tileSize);
//        glVertex3f(-gridSize, floorHeight, +gridSize);
//
//        // East wall
//
//        glTexCoord2f(0, 0);
//        glVertex3f(+gridSize, floorHeight, -gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, 0);
//        glVertex3f(+gridSize, floorHeight, +gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
//        glVertex3f(+gridSize, ceilingHeight, +gridSize);
//        glTexCoord2f(0, gridSize * 10 * tileSize);
//        glVertex3f(+gridSize, ceilingHeight, -gridSize);
//
//        // South wall
//
//        glTexCoord2f(0, 0);
//        glVertex3f(-gridSize, floorHeight, +gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, 0);
//        glVertex3f(-gridSize, ceilingHeight, +gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
//        glVertex3f(+gridSize, ceilingHeight, +gridSize);
//        glTexCoord2f(0, gridSize * 10 * tileSize);
//        glVertex3f(+gridSize, floorHeight, +gridSize);
//
//        glEnd();
//
//        glEndList();
//
//        int floorDisplayList = glGenLists(1);
//        glNewList(floorDisplayList, GL_COMPILE);
//        glBegin(GL_QUADS);
//        glTexCoord2f(0, 0);
//        glVertex3f(-gridSize, floorHeight, -gridSize);
//        glTexCoord2f(0, gridSize * 10 * tileSize);
//        glVertex3f(-gridSize, floorHeight, gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, gridSize * 10 * tileSize);
//        glVertex3f(gridSize, floorHeight, gridSize);
//        glTexCoord2f(gridSize * 10 * tileSize, 0);
//        glVertex3f(gridSize, floorHeight, -gridSize);
//        glEnd();
//        glEndList();
    }

    public void drawWall(float xKiri, float xKanan, float zDepan, float zBelakang) {

        //Kanan
        glVertex3f(xKanan, -0.5f, zDepan);
        glVertex3f(xKanan, -0.5f, zBelakang);
        glVertex3f(xKanan, 0.5f, zBelakang);
        glVertex3f(xKanan, 0.5f, zDepan);

//        //Kiri
        glVertex3f(xKiri + 0.5f, -0.5f, zDepan);
        glVertex3f(xKiri + 0.5f, -0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, 0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, 0.5f, zDepan);

        //Belakang
        glVertex3f(xKanan, -0.5f, zBelakang);
        glVertex3f(xKanan, 0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, 0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, -0.5f, zBelakang);

//        Depan
//        glTexCoord2f(0, 0);
        glVertex3f(xKanan, -0.5f, zDepan);
//        glTexCoord2f(xKanan, 1.0f);
        glVertex3f(xKanan, 0.5f, zDepan);
//        glTexCoord2f(1.0f, xKanan);
        glVertex3f(xKiri + 0.5f, 0.5f, zDepan);
//        glTexCoord2f(0, -xKiri);
        glVertex3f(xKiri + 0.5f, -0.5f, zDepan);

        //Atas
        glVertex3f(xKanan, 0.5f, zDepan);
        glVertex3f(xKanan, 0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, 0.5f, zBelakang);
        glVertex3f(xKiri + 0.5f, 0.5f, zDepan);

        glEndList();

    }

    static void texture() {
        int floorTexture = glGenTextures();
        {
            InputStream in = null;
            try {
                in = new FileInputStream("C:/libs/LWJGL/res/images/floor.png");
                PNGDecoder decoder = new PNGDecoder(in);
                ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
                buffer.flip();
                glBindTexture(GL_TEXTURE_2D, floorTexture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA,
                        GL_UNSIGNED_BYTE, buffer);
                glBindTexture(GL_TEXTURE_2D, 0);
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to find the texture files.");
                ex.printStackTrace();
                Display.destroy();
                System.exit(1);
            } catch (IOException ex) {
                System.err.println("Failed to load the texture files.");
                ex.printStackTrace();
                Display.destroy();
                System.exit(1);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    void run() {
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            if (Display.isVisible()) {
                processKeyboard();
                processMouse();
                render();
            }
            Display.update();
            Display.sync(100);
        }
    }

}
