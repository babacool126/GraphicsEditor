import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// Base class
abstract class Figure {
    int left, top, width, height;
    
    Figure(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }
    
    boolean contains(int x, int y) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
    
    abstract void draw(Graphics g);
}

// Rectangle
class RectangleFigure extends Figure {
    RectangleFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(left, top, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(left, top, width, height);
    }
}

// Ellipse
class EllipseFigure extends Figure {
    EllipseFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        g.setColor(Color.PINK);
        g.fillOval(left, top, width, height);
        g.setColor(Color.BLACK);
        g.drawOval(left, top, width, height);
    }
}

// Canvas
class DrawingPanel extends JPanel {
    ArrayList<Figure> figures = new ArrayList<>();
    String mode = "rectangle";
    int startX, startY;
    Figure currentFigure;
    
    DrawingPanel() {
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                
                if (mode.equals("rectangle")) {
                    currentFigure = new RectangleFigure(startX, startY, 1, 1);
                } else {
                    currentFigure = new EllipseFigure(startX, startY, 1, 1);
                }
                figures.add(currentFigure);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (currentFigure != null) {
                    int x = e.getX();
                    int y = e.getY();
                    currentFigure.left = Math.min(startX, x);
                    currentFigure.top = Math.min(startY, y);
                    currentFigure.width = Math.abs(x - startX);
                    currentFigure.height = Math.abs(y - startY);
                    repaint();
                }
            }
        });
    }
    
    void setMode(String mode) {
        this.mode = mode;
    }
    
    void clear() {
        figures.clear();
        repaint();
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Figure f : figures) {
            f.draw(g);
        }
    }
}

// Main
public class GraphicsEditor extends JFrame {
    DrawingPanel canvas;
    
    GraphicsEditor() {
        setTitle("Graphics Editor - Simple");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        canvas = new DrawingPanel();
        add(canvas, BorderLayout.CENTER);
        
        JPanel toolbar = new JPanel();
        
        JButton rectBtn = new JButton("Rectangle");
        rectBtn.addActionListener(e -> canvas.setMode("rectangle"));
        toolbar.add(rectBtn);
        
        JButton ellipseBtn = new JButton("Ellipse");
        ellipseBtn.addActionListener(e -> canvas.setMode("ellipse"));
        toolbar.add(ellipseBtn);
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> canvas.clear());
        toolbar.add(clearBtn);
        
        add(toolbar, BorderLayout.NORTH);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphicsEditor().setVisible(true));
    }
}
