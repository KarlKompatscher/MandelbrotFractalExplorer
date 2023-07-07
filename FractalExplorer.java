

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

public class FractalExplorer extends JFrame {

    static final int WIDTH = 1000;
    static final int HEIGHT = 1500;
    static final int MAX_INTERATION = 500;
    static final double DEFAULT_ZOOM = 200.0;
    static final double DEFAUT_TOPLEFT_X = -2.5;
    static final double DEFAUT_TOPLEFT_Y = 1.7;

    double zoomFactor = DEFAULT_ZOOM;
    double topLeftX = DEFAUT_TOPLEFT_X;
    double topLeftY = DEFAUT_TOPLEFT_Y;

    java.awt.Canvas canvass;

    Canvas canvas;
    BufferedImage fractalImage;

    // Implements GUI
    public FractalExplorer() {
        setInitialGUI_Properties();
        addCanvas();
        canvas.addKeyStrokeEvents();
        updateFractal();
    }

    public void setInitialGUI_Properties() {
        this.setTitle("Fractal Explorer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);
        this.setLocationRelativeTo(null); // GUI is located in the center of the screen
    }

    private void addCanvas() {
        canvas = new Canvas();
        fractalImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        canvas.setVisible(true);
        this.add(canvas, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public void updateFractal() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double c_r = getXPos(x);
                double c_i = getYPos(y);

                int iterCount = computeIterations(c_r, c_i);

                // Create a color based on the number ov Iteration
                int pixelColor = makeColor(iterCount);
                fractalImage.setRGB(x, y, pixelColor);
            }
        }

        canvas.repaint();
    }

    private double getXPos(double x) {
        return x / zoomFactor + topLeftX;
    }

    private double getYPos(double y) {
        return y / zoomFactor - topLeftY;
    }

    private int makeColor(int iterCount) {
        int color = 0b001011000101101101101000;
        int mask = 0b000000000000110001110101;
        int shiftMagnitude = iterCount / 13;    // 13 because of # of zeros in mask

        if (iterCount == MAX_INTERATION) {
            return Color.BLACK.getRGB();
        }
        return color | (mask << shiftMagnitude);
    }

    private int computeIterations(double c_r, double c_i) {
        /*
         * Let c = Re_c + Im_c
         * Let z = Re_z + Im_z
         * 
         * z' = z*z + c
         * = (Re_z + Im_z)*(Re_z + Im_z) + Re_c + Im_c
         * = Re_z² + 2*Re_z*Im_z - Im_z² + Re_c + Im_c
         * 
         * Re_z' = Re_z² -Im_z² + Re_c
         * Im_z' = 2*Re_z*Im_z + Im_c
         */

        double z_r = 0.0;
        double z_i = 0.0;

        int iterationCount = 0;

        // √(a² + b²) <= 2.0
        // (a² + b²) <= 4.0

        while (z_r * z_r + z_i * z_i <= 4.0) {
            double z_r_tmp = z_r;

            z_r = z_r * z_r - z_i * z_i + c_r;
            z_i = 2 * z_i * z_r_tmp + c_i;

            // Point was inside the Mandelbrot set
            if (iterationCount >= MAX_INTERATION) {
                return MAX_INTERATION;
            }

            iterationCount++;
        }

        // Complex point was outside the Mandelbrot-set
        return iterationCount;
    }

    public static void main(String[] args) {
        new FractalExplorer();
    }

    private class Canvas extends JPanel implements MouseListener {

        public Canvas() {
            addMouseListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WIDTH, HEIGHT);
        }

        @Override
        public void paintComponent(Graphics drawingObj) {
            drawingObj.drawImage(fractalImage, 0, 0, null);
        }

        @Override
        public void mousePressed(MouseEvent mouse) {
            double x = (double) mouse.getX();
            double y = (double) mouse.getY();

            switch (mouse.getButton()) {
                case MouseEvent.BUTTON1: // Left-click
                    adjustZoom(x, y, zoomFactor * 2); // zooming in
                    break;
                case MouseEvent.BUTTON3: // Right-click
                    adjustZoom(x, y, zoomFactor / 2); // zooming out
                    break;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }

        @Override
        public void mouseReleased(MouseEvent e) { }

        public void addKeyStrokeEvents() {
            KeyStroke wKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
            KeyStroke aKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
            KeyStroke sKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
            KeyStroke dKey = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);

            Action wPressed = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            };

            Action aPressed = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveLeft();
                }
            };

            Action sPressed = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            };

            Action dPressed = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveRight();
                }
            };

            this.getInputMap().put(wKey, "w_key");
            this.getInputMap().put(aKey, "a_key");
            this.getInputMap().put(sKey, "s_key");
            this.getInputMap().put(dKey, "d_key");

            this.getActionMap().put("w_key", wPressed);
            this.getActionMap().put("a_key", aPressed);
            this.getActionMap().put("s_key", sPressed);
            this.getActionMap().put("d_key", dPressed);

        }

    }

    private void adjustZoom(double newX, double newY, double newZoom) {
        // zooming into the fractal by shifting topLeftX and topLeftY one way
        topLeftX += newX / zoomFactor;
        topLeftY -= newY / zoomFactor;

        // recenter topLeftX and topLEftY
        zoomFactor = newZoom;
        topLeftX -= (WIDTH / 2) / zoomFactor;
        topLeftY += (HEIGHT / 2) / zoomFactor;

        updateFractal();
    }

    private void moveUp() {
        double currentHeight = HEIGHT / zoomFactor;
        topLeftY += currentHeight / 6;      // move 1/6 of the Height up
        updateFractal();
    }

    private void moveLeft() {
        double currentWidth = WIDTH / zoomFactor;
        topLeftX -= currentWidth / 6;      
        updateFractal();
    }

    private void moveDown() {
        double currentHeight = HEIGHT / zoomFactor;
        topLeftY -= currentHeight / 6;      
        updateFractal();
    }

    private void moveRight() {
        double currentWidth = WIDTH / zoomFactor;
        topLeftX += currentWidth / 6;     
        updateFractal();
    }

}