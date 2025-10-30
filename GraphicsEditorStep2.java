import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

// ==================== FIGURE CLASSES ====================

abstract class Figure {
    int left, top, width, height;
    boolean selected = false;
    
    Figure(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }
    
    boolean contains(int x, int y) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }
    
    void move(int dx, int dy) {
        left += dx;
        top += dy;
    }
    
    void resize(int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
    }
    
    abstract void draw(Graphics g);
    abstract String toFileFormat(int indent);
    public abstract Figure clone();
}

class RectangleFigure extends Figure {
    RectangleFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(left, top, width, height);
        g.setColor(selected ? Color.RED : Color.BLACK);
        g.drawRect(left, top, width, height);
        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(left, top, width, height);
            g2.setStroke(new BasicStroke(1));
        }
    }
    
    String toFileFormat(int indent) {
        return getIndent(indent) + "rectangle " + left + " " + top + " " + width + " " + height;
    }
    
    public Figure clone() {
        return new RectangleFigure(left, top, width, height);
    }
    
    private String getIndent(int level) {
        return " ".repeat(level);
    }
}

class EllipseFigure extends Figure {
    EllipseFigure(int left, int top, int width, int height) {
        super(left, top, width, height);
    }
    
    void draw(Graphics g) {
        g.setColor(Color.PINK);
        g.fillOval(left, top, width, height);
        g.setColor(selected ? Color.RED : Color.BLACK);
        g.drawOval(left, top, width, height);
        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(left, top, width, height);
            g2.setStroke(new BasicStroke(1));
        }
    }
    
    String toFileFormat(int indent) {
        return getIndent(indent) + "ellipse " + left + " " + top + " " + width + " " + height;
    }
    
    public Figure clone() {
        return new EllipseFigure(left, top, width, height);
    }
    
    private String getIndent(int level) {
        return " ".repeat(level);
    }
}

// ==================== COMMAND PATTERN ====================

interface Command {
    void execute();
    void undo();
}

class AddFigureCommand implements Command {
    private ArrayList<Figure> figures;
    private Figure figure;
    
    AddFigureCommand(ArrayList<Figure> figures, Figure figure) {
        this.figures = figures;
        this.figure = figure;
    }
    
    public void execute() {
        figures.add(figure);
    }
    
    public void undo() {
        figures.remove(figure);
    }
}

class RemoveFigureCommand implements Command {
    private ArrayList<Figure> figures;
    private Figure figure;
    private int index;
    
    RemoveFigureCommand(ArrayList<Figure> figures, Figure figure) {
        this.figures = figures;
        this.figure = figure;
        this.index = figures.indexOf(figure);
    }
    
    public void execute() {
        index = figures.indexOf(figure);
        figures.remove(figure);
    }
    
    public void undo() {
        figures.add(index, figure);
    }
}

class MoveFigureCommand implements Command {
    private Figure figure;
    private int dx, dy;
    
    MoveFigureCommand(Figure figure, int dx, int dy) {
        this.figure = figure;
        this.dx = dx;
        this.dy = dy;
    }
    
    public void execute() {
        figure.move(dx, dy);
    }
    
    public void undo() {
        figure.move(-dx, -dy);
    }
}

class ResizeFigureCommand implements Command {
    private Figure figure;
    private int oldWidth, oldHeight;
    private int newWidth, newHeight;
    
    ResizeFigureCommand(Figure figure, int newWidth, int newHeight) {
        this.figure = figure;
        this.oldWidth = figure.width;
        this.oldHeight = figure.height;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }
    
    public void execute() {
        figure.resize(newWidth, newHeight);
    }
    
    public void undo() {
        figure.resize(oldWidth, oldHeight);
    }
}

class ClearCommand implements Command {
    private ArrayList<Figure> figures;
    private ArrayList<Figure> backup;
    
    ClearCommand(ArrayList<Figure> figures) {
        this.figures = figures;
        this.backup = new ArrayList<>();
        for (Figure f : figures) {
            backup.add(f.clone());
        }
    }
    
    public void execute() {
        figures.clear();
    }
    
    public void undo() {
        figures.clear();
        figures.addAll(backup);
    }
}

class LoadFileCommand implements Command {
    private ArrayList<Figure> figures;
    private ArrayList<Figure> oldFigures;
    private ArrayList<Figure> newFigures;
    
    LoadFileCommand(ArrayList<Figure> figures, ArrayList<Figure> newFigures) {
        this.figures = figures;
        this.newFigures = newFigures;
        this.oldFigures = new ArrayList<>();
        for (Figure f : figures) {
            oldFigures.add(f.clone());
        }
    }
    
    public void execute() {
        figures.clear();
        figures.addAll(newFigures);
    }
    
    public void undo() {
        figures.clear();
        figures.addAll(oldFigures);
    }
}

// ==================== COMMAND MANAGER ====================

class CommandManager {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();
    
    void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }
    
    void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }
    
    void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
    
    boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    boolean canRedo() {
        return !redoStack.isEmpty();
    }
}

// ==================== FILE I/O ====================

class FileIO {
    static void save(ArrayList<Figure> figures, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write as a group containing all figures
            writer.println("group " + figures.size());
            for (Figure f : figures) {
                writer.println(f.toFileFormat(1));
            }
        }
    }
    
    static ArrayList<Figure> load(File file) throws IOException {
        ArrayList<Figure> figures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && line.trim().startsWith("group")) {
                String[] parts = line.trim().split("\\s+");
                int count = Integer.parseInt(parts[1]);
                for (int i = 0; i < count; i++) {
                    line = reader.readLine();
                    if (line != null) {
                        Figure fig = parseFigure(line.trim());
                        if (fig != null) {
                            figures.add(fig);
                        }
                    }
                }
            }
        }
        return figures;
    }
    
    private static Figure parseFigure(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 5) return null;
        
        String type = parts[0];
        int left = Integer.parseInt(parts[1]);
        int top = Integer.parseInt(parts[2]);
        int width = Integer.parseInt(parts[3]);
        int height = Integer.parseInt(parts[4]);
        
        if (type.equals("rectangle")) {
            return new RectangleFigure(left, top, width, height);
        } else if (type.equals("ellipse")) {
            return new EllipseFigure(left, top, width, height);
        }
        return null;
    }
}

// ==================== CANVAS ====================

class DrawingPanel extends JPanel {
    ArrayList<Figure> figures = new ArrayList<>();
    CommandManager commandManager;
    
    String mode = "rectangle"; // rectangle, ellipse, select, move, resize
    int startX, startY;
    Figure currentFigure;
    Figure selectedFigure;
    
    // FIXED: Track original position for move/resize
    int originalX, originalY;
    int originalWidth, originalHeight;
    int totalDx, totalDy;  // Track total movement for command
    
    DrawingPanel(CommandManager commandManager) {
        this.commandManager = commandManager;
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                
                if (mode.equals("rectangle") || mode.equals("ellipse")) {
                    // Create new figure
                    if (mode.equals("rectangle")) {
                        currentFigure = new RectangleFigure(startX, startY, 1, 1);
                    } else {
                        currentFigure = new EllipseFigure(startX, startY, 1, 1);
                    }
                } else if (mode.equals("select")) {
                    // Select figure
                    selectFigureAt(e.getX(), e.getY());
                } else if (mode.equals("move")) {
                    // Find figure to move
                    selectedFigure = findFigureAt(e.getX(), e.getY());
                    if (selectedFigure != null) {
                        originalX = e.getX();
                        originalY = e.getY();
                        totalDx = 0;
                        totalDy = 0;
                    }
                } else if (mode.equals("resize")) {
                    // Find figure to resize
                    selectedFigure = findFigureAt(e.getX(), e.getY());
                    if (selectedFigure != null) {
                        originalWidth = selectedFigure.width;
                        originalHeight = selectedFigure.height;
                    }
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                if (currentFigure != null && (mode.equals("rectangle") || mode.equals("ellipse"))) {
                    // Add figure via command
                    if (currentFigure.width > 5 && currentFigure.height > 5) {
                        commandManager.executeCommand(new AddFigureCommand(figures, currentFigure));
                    }
                    currentFigure = null;
                    repaint();
                } else if (mode.equals("move") && selectedFigure != null) {
                    if (totalDx != 0 || totalDy != 0) {
                        commandManager.executeCommand(new MoveFigureCommand(selectedFigure, totalDx, totalDy));
                    }
                    selectedFigure = null;
                    repaint();
                } else if (mode.equals("resize") && selectedFigure != null) {
                    if (selectedFigure.width != originalWidth || selectedFigure.height != originalHeight) {
                        commandManager.executeCommand(new ResizeFigureCommand(selectedFigure, selectedFigure.width, selectedFigure.height));
                    }
                    selectedFigure = null;
                    repaint();
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (currentFigure != null && (mode.equals("rectangle") || mode.equals("ellipse"))) {
                    int x = e.getX();
                    int y = e.getY();
                    currentFigure.left = Math.min(startX, x);
                    currentFigure.top = Math.min(startY, y);
                    currentFigure.width = Math.abs(x - startX);
                    currentFigure.height = Math.abs(y - startY);
                    repaint();
                } else if (mode.equals("move") && selectedFigure != null) {
                    int currentX = e.getX();
                    int currentY = e.getY();
                    int dx = currentX - originalX;
                    int dy = currentY - originalY;
                    
                    // Move figure to new position based on total delta
                    selectedFigure.left = selectedFigure.left - totalDx + dx;
                    selectedFigure.top = selectedFigure.top - totalDy + dy;
                    
                    // Update total delta
                    totalDx = dx;
                    totalDy = dy;
                    
                    repaint();
                } else if (mode.equals("resize") && selectedFigure != null) {
                    int newWidth = Math.max(10, Math.abs(e.getX() - selectedFigure.left));
                    int newHeight = Math.max(10, Math.abs(e.getY() - selectedFigure.top));
                    selectedFigure.resize(newWidth, newHeight);
                    repaint();
                }
            }
        });
    }
    
    void setMode(String mode) {
        this.mode = mode;
        // Deselect all when changing mode
        for (Figure f : figures) {
            f.selected = false;
        }
        repaint();
    }
    
    void clear() {
        commandManager.executeCommand(new ClearCommand(figures));
        repaint();
    }
    
    void selectFigureAt(int x, int y) {
        // Deselect all first
        for (Figure f : figures) {
            f.selected = false;
        }
        // Select the top-most figure at position
        for (int i = figures.size() - 1; i >= 0; i--) {
            if (figures.get(i).contains(x, y)) {
                figures.get(i).selected = true;
                break;
            }
        }
        repaint();
    }
    
    Figure findFigureAt(int x, int y) {
        for (int i = figures.size() - 1; i >= 0; i--) {
            if (figures.get(i).contains(x, y)) {
                return figures.get(i);
            }
        }
        return null;
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Figure f : figures) {
            f.draw(g);
        }
        if (currentFigure != null) {
            currentFigure.draw(g);
        }
    }
}

// ==================== MAIN APPLICATION ====================

public class GraphicsEditorStep2 extends JFrame {
    DrawingPanel canvas;
    CommandManager commandManager;
    JButton undoBtn, redoBtn;
    
    GraphicsEditorStep2() {
        setTitle("Graphics Editor - Step 2 (Command Pattern + File I/O)");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        commandManager = new CommandManager();
        canvas = new DrawingPanel(commandManager);
        add(canvas, BorderLayout.CENTER);
        
        // Create toolbar
        JPanel toolbar = new JPanel();
        
        JButton rectBtn = new JButton("Rectangle");
        rectBtn.addActionListener(e -> canvas.setMode("rectangle"));
        toolbar.add(rectBtn);
        
        JButton ellipseBtn = new JButton("Ellipse");
        ellipseBtn.addActionListener(e -> canvas.setMode("ellipse"));
        toolbar.add(ellipseBtn);
        
        JButton selectBtn = new JButton("Select");
        selectBtn.addActionListener(e -> canvas.setMode("select"));
        toolbar.add(selectBtn);
        
        JButton moveBtn = new JButton("Move");
        moveBtn.addActionListener(e -> canvas.setMode("move"));
        toolbar.add(moveBtn);
        
        JButton resizeBtn = new JButton("Resize");
        resizeBtn.addActionListener(e -> canvas.setMode("resize"));
        toolbar.add(resizeBtn);
        
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> {
            commandManager.undo();
            canvas.repaint();
            updateUndoRedoButtons();
        });
        toolbar.add(undoBtn);
        
        redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> {
            commandManager.redo();
            canvas.repaint();
            updateUndoRedoButtons();
        });
        toolbar.add(redoBtn);
        
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveFile());
        toolbar.add(saveBtn);
        
        JButton loadBtn = new JButton("Load");
        loadBtn.addActionListener(e -> loadFile());
        toolbar.add(loadBtn);
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            canvas.clear();
            updateUndoRedoButtons();
        });
        toolbar.add(clearBtn);
        
        add(toolbar, BorderLayout.NORTH);
        
        updateUndoRedoButtons();
    }
    
    void updateUndoRedoButtons() {
        undoBtn.setEnabled(commandManager.canUndo());
        redoBtn.setEnabled(commandManager.canRedo());
    }
    
    void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try {
                FileIO.save(canvas.figures, file);
                JOptionPane.showMessageDialog(this, "File saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ArrayList<Figure> newFigures = FileIO.load(fileChooser.getSelectedFile());
                commandManager.executeCommand(new LoadFileCommand(canvas.figures, newFigures));
                canvas.repaint();
                updateUndoRedoButtons();
                JOptionPane.showMessageDialog(this, "File loaded successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphicsEditorStep2().setVisible(true));
    }
}
