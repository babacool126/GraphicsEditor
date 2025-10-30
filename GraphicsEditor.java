import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// ==================== BASE FIGURE CLASS ====================

abstract class Figure {
    int left, top, width, height;
    boolean selected = false;
    
    Figure(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }
    
    // Check if point is inside figure
    boolean contains(int x, int y) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
    
    // Move figure by delta
    void move(int dx, int dy) {
        left += dx;
        top += dy;
    }
    
    // Resize figure
    void resize(int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
    }
    
    // Draw figure (to be implemented by subclasses)
    abstract void draw(Graphics g);
}

// ==================== RECTANGLE FIGURE ====================

class RectangleFigure extends Figure {
    RectangleFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        // Fill the rectangle
        g.setColor(Color.CYAN);
        g.fillRect(left, top, width, height);
        
        // Draw border (red if selected, black otherwise)
        if (selected) {
            g.setColor(Color.RED);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(left, top, width, height);
            g2.setStroke(new BasicStroke(1));
            
            // Draw resize handles
            drawResizeHandles(g);
        } else {
            g.setColor(Color.BLACK);
            g.drawRect(left, top, width, height);
        }
    }
    
    private void drawResizeHandles(Graphics g) {
        int handleSize = 8;
        g.setColor(Color.RED);
        // Top-left
        g.fillRect(left - handleSize/2, top - handleSize/2, handleSize, handleSize);
        // Top-right
        g.fillRect(left + width - handleSize/2, top - handleSize/2, handleSize, handleSize);
        // Bottom-left
        g.fillRect(left - handleSize/2, top + height - handleSize/2, handleSize, handleSize);
        // Bottom-right
        g.fillRect(left + width - handleSize/2, top + height - handleSize/2, handleSize, handleSize);
    }
}

// ==================== ELLIPSE FIGURE ====================

class EllipseFigure extends Figure {
    EllipseFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        // Fill the ellipse
        g.setColor(Color.PINK);
        g.fillOval(left, top, width, height);
        
        // Draw border (red if selected, black otherwise)
        if (selected) {
            g.setColor(Color.RED);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(left, top, width, height);
            g2.setStroke(new BasicStroke(1));
            
            // Draw resize handles
            drawResizeHandles(g);
        } else {
            g.setColor(Color.BLACK);
            g.drawOval(left, top, width, height);
        }
    }
    
    private void drawResizeHandles(Graphics g) {
        int handleSize = 8;
        g.setColor(Color.RED);
        // Top-left
        g.fillRect(left - handleSize/2, top - handleSize/2, handleSize, handleSize);
        // Top-right
        g.fillRect(left + width - handleSize/2, top - handleSize/2, handleSize, handleSize);
        // Bottom-left
        g.fillRect(left - handleSize/2, top + height - handleSize/2, handleSize, handleSize);
        // Bottom-right
        g.fillRect(left + width - handleSize/2, top + height - handleSize/2, handleSize, handleSize);
    }
}

// ==================== DRAWING PANEL ====================

class DrawingPanel extends JPanel {
    ArrayList<Figure> figures = new ArrayList<>();
    
    // Drawing state
    String mode = "rectangle"; // rectangle, ellipse, select, move, resize
    Figure currentFigure = null;
    Figure selectedFigure = null;
    
    // Mouse tracking
    int startX, startY;
    int lastMouseX, lastMouseY;
    
    // Resize state
    boolean isResizing = false;
    
    DrawingPanel() {
        setBackground(Color.WHITE);
        
        // Mouse listener for press and release
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                lastMouseX = startX;
                lastMouseY = startY;
                
                handleMousePressed(e.getX(), e.getY());
            }
            
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e.getX(), e.getY());
            }
        });
        
        // Mouse motion listener for dragging
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e.getX(), e.getY());
            }
        });
    }
    
    void handleMousePressed(int x, int y) {
        if (mode.equals("rectangle")) {
            // Start drawing a rectangle
            currentFigure = new RectangleFigure(x, y, 1, 1);
            figures.add(currentFigure);
            
        } else if (mode.equals("ellipse")) {
            // Start drawing an ellipse
            currentFigure = new EllipseFigure(x, y, 1, 1);
            figures.add(currentFigure);
            
        } else if (mode.equals("select")) {
            // Deselect all figures first
            for (Figure f : figures) {
                f.selected = false;
            }
            
            // Select the topmost figure at this position
            for (int i = figures.size() - 1; i >= 0; i--) {
                if (figures.get(i).contains(x, y)) {
                    figures.get(i).selected = true;
                    selectedFigure = figures.get(i);
                    break;
                }
            }
            repaint();
            
        } else if (mode.equals("move")) {
            // Find figure to move
            selectedFigure = null;
            for (int i = figures.size() - 1; i >= 0; i--) {
                if (figures.get(i).contains(x, y)) {
                    selectedFigure = figures.get(i);
                    break;
                }
            }
            
        } else if (mode.equals("resize")) {
            // Find figure to resize
            selectedFigure = null;
            for (int i = figures.size() - 1; i >= 0; i--) {
                if (figures.get(i).contains(x, y)) {
                    selectedFigure = figures.get(i);
                    isResizing = true;
                    break;
                }
            }
        }
    }
    
    void handleMouseDragged(int x, int y) {
        if (mode.equals("rectangle") || mode.equals("ellipse")) {
            // Update the figure being drawn
            if (currentFigure != null) {
                currentFigure.left = Math.min(startX, x);
                currentFigure.top = Math.min(startY, y);
                currentFigure.width = Math.abs(x - startX);
                currentFigure.height = Math.abs(y - startY);
                repaint();
            }
            
        } else if (mode.equals("move")) {
            // Move the selected figure
            if (selectedFigure != null) {
                int dx = x - lastMouseX;
                int dy = y - lastMouseY;
                selectedFigure.move(dx, dy);
                lastMouseX = x;
                lastMouseY = y;
                repaint();
            }
            
        } else if (mode.equals("resize")) {
            // Resize the selected figure
            if (selectedFigure != null && isResizing) {
                int newWidth = Math.max(10, Math.abs(x - selectedFigure.left));
                int newHeight = Math.max(10, Math.abs(y - selectedFigure.top));
                selectedFigure.resize(newWidth, newHeight);
                repaint();
            }
        }
    }
    
    void handleMouseReleased(int x, int y) {
        if (mode.equals("rectangle") || mode.equals("ellipse")) {
            // Finalize the drawn figure
            if (currentFigure != null) {
                // Remove figures that are too small
                if (currentFigure.width < 5 || currentFigure.height < 5) {
                    figures.remove(currentFigure);
                }
                currentFigure = null;
                repaint();
            }
            
        } else if (mode.equals("move")) {
            // Finalize move
            selectedFigure = null;
            
        } else if (mode.equals("resize")) {
            // Finalize resize
            selectedFigure = null;
            isResizing = false;
        }
    }
    
    void setMode(String mode) {
        this.mode = mode;
        
        // Deselect all figures when changing modes
        for (Figure f : figures) {
            f.selected = false;
        }
        selectedFigure = null;
        currentFigure = null;
        repaint();
    }
    
    void clear() {
        figures.clear();
        selectedFigure = null;
        currentFigure = null;
        repaint();
    }
    
    void deleteSelected() {
        // Remove all selected figures
        figures.removeIf(f -> f.selected);
        selectedFigure = null;
        repaint();
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw all figures
        for (Figure f : figures) {
            f.draw(g);
        }
        
        // Draw the figure currently being created
        if (currentFigure != null) {
            currentFigure.draw(g);
        }
    }
}

// ==================== MAIN APPLICATION ====================

public class GraphicsEditorStep1 extends JFrame {
    DrawingPanel canvas;
    
    // UI components
    JButton rectBtn, ellipseBtn, selectBtn, moveBtn, resizeBtn, clearBtn, deleteBtn;
    JLabel statusLabel;
    
    GraphicsEditorStep1() {
        setTitle("Graphics Editor - Step 1");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Create canvas
        canvas = new DrawingPanel();
        add(canvas, BorderLayout.CENTER);
        
        // Create toolbar
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // Drawing buttons
        rectBtn = new JButton("Rectangle");
        rectBtn.addActionListener(e -> {
            canvas.setMode("rectangle");
            updateStatus("Drawing mode: Rectangle");
            highlightButton(rectBtn);
        });
        toolbar.add(rectBtn);
        
        ellipseBtn = new JButton("Ellipse");
        ellipseBtn.addActionListener(e -> {
            canvas.setMode("ellipse");
            updateStatus("Drawing mode: Ellipse");
            highlightButton(ellipseBtn);
        });
        toolbar.add(ellipseBtn);
        
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Selection and manipulation buttons
        selectBtn = new JButton("Select");
        selectBtn.addActionListener(e -> {
            canvas.setMode("select");
            updateStatus("Select mode: Click on a figure to select it");
            highlightButton(selectBtn);
        });
        toolbar.add(selectBtn);
        
        moveBtn = new JButton("Move");
        moveBtn.addActionListener(e -> {
            canvas.setMode("move");
            updateStatus("Move mode: Drag a figure to move it");
            highlightButton(moveBtn);
        });
        toolbar.add(moveBtn);
        
        resizeBtn = new JButton("Resize");
        resizeBtn.addActionListener(e -> {
            canvas.setMode("resize");
            updateStatus("Resize mode: Drag from top-left to resize");
            highlightButton(resizeBtn);
        });
        toolbar.add(resizeBtn);
        
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Utility buttons
        deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            canvas.deleteSelected();
            updateStatus("Selected figures deleted");
        });
        toolbar.add(deleteBtn);
        
        clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            canvas.clear();
            updateStatus("Canvas cleared");
        });
        toolbar.add(clearBtn);
        
        add(toolbar, BorderLayout.NORTH);
        
        // Create status bar
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready - Select a drawing mode");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
        
        // Initial button highlight
        highlightButton(rectBtn);
    }
    
    void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    void highlightButton(JButton activeButton) {
        // Reset all buttons
        rectBtn.setBackground(null);
        ellipseBtn.setBackground(null);
        selectBtn.setBackground(null);
        moveBtn.setBackground(null);
        resizeBtn.setBackground(null);
        
        // Highlight active button
        activeButton.setBackground(new Color(200, 230, 255));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphicsEditorStep1 editor = new GraphicsEditorStep1();
            editor.setVisible(true);
        });
    }
}
