import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

// ==================== STRATEGY PATTERN (SINGLETON) ====================

/**
 * DrawStrategy - Strategy pattern interface
 * Defines the drawing behavior for different figure types
 */
interface DrawStrategy {
    void draw(Graphics g, int left, int top, int width, int height, boolean selected);
    String getName();
}

/**
 * RectangleStrategy - Singleton strategy for drawing rectangles
 */
class RectangleStrategy implements DrawStrategy {
    private static RectangleStrategy instance;
    
    private RectangleStrategy() {
        // Private constructor for singleton
    }
    
    public static RectangleStrategy getInstance() {
        if (instance == null) {
            instance = new RectangleStrategy();
        }
        return instance;
    }
    
    @Override
    public void draw(Graphics g, int left, int top, int width, int height, boolean selected) {
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
    
    @Override
    public String getName() {
        return "rectangle";
    }
}

/**
 * EllipseStrategy - Singleton strategy for drawing ellipses
 */
class EllipseStrategy implements DrawStrategy {
    private static EllipseStrategy instance;
    
    private EllipseStrategy() {
        // Private constructor for singleton
    }
    
    public static EllipseStrategy getInstance() {
        if (instance == null) {
            instance = new EllipseStrategy();
        }
        return instance;
    }
    
    @Override
    public void draw(Graphics g, int left, int top, int width, int height, boolean selected) {
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
    
    @Override
    public String getName() {
        return "ellipse";
    }
}

// ==================== VISITOR PATTERN ====================

/**
 * FigureVisitor - Visitor pattern interface
 */
interface FigureVisitor {
    void visitBaseFigure(BaseFigure figure);
    void visitGroup(FigureGroup group);
}

/**
 * MoveVisitor - Moves figures by a given delta
 */
class MoveVisitor implements FigureVisitor {
    private int dx, dy;
    
    MoveVisitor(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    @Override
    public void visitBaseFigure(BaseFigure figure) {
        figure.left += dx;
        figure.top += dy;
    }
    
    @Override
    public void visitGroup(FigureGroup group) {
        // Move all children
        for (Figure child : group.getChildren()) {
            child.accept(this);
        }
        group.updateBounds();
    }
}

/**
 * ResizeVisitor - Resizes figures to a new width and height
 */
class ResizeVisitor implements FigureVisitor {
    private int newWidth, newHeight;
    private Figure targetFigure;
    
    // For groups: store original bounds for proportional scaling
    private int originalLeft, originalTop, originalWidth, originalHeight;
    
    ResizeVisitor(Figure target, int newWidth, int newHeight) {
        this.targetFigure = target;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
        this.originalLeft = target.left;
        this.originalTop = target.top;
        this.originalWidth = target.width;
        this.originalHeight = target.height;
    }
    
    @Override
    public void visitBaseFigure(BaseFigure figure) {
        figure.width = newWidth;
        figure.height = newHeight;
    }
    
    @Override
    public void visitGroup(FigureGroup group) {
        if (originalWidth == 0 || originalHeight == 0) return;
        
        double scaleX = (double) newWidth / originalWidth;
        double scaleY = (double) newHeight / originalHeight;
        
        // Resize each child proportionally
        for (Figure child : group.getChildren()) {
            int relativeLeft = child.left - originalLeft;
            int relativeTop = child.top - originalTop;
            
            int childNewLeft = originalLeft + (int) Math.round(relativeLeft * scaleX);
            int childNewTop = originalTop + (int) Math.round(relativeTop * scaleY);
            int childNewWidth = (int) Math.round(child.width * scaleX);
            int childNewHeight = (int) Math.round(child.height * scaleY);
            
            child.left = childNewLeft;
            child.top = childNewTop;
            
            // Recursively resize child
            ResizeVisitor childResizer = new ResizeVisitor(child, childNewWidth, childNewHeight);
            child.accept(childResizer);
        }
        
        group.updateBounds();
    }
}

/**
 * FileWriterVisitor - Writes figures to file format
 */
class FileWriterVisitor implements FigureVisitor {
    private StringBuilder output = new StringBuilder();
    private int indentLevel;
    
    FileWriterVisitor(int indentLevel) {
        this.indentLevel = indentLevel;
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    @Override
    public void visitBaseFigure(BaseFigure figure) {
        output.append(getIndent())
              .append(figure.getStrategy().getName()).append(" ")
              .append(figure.left).append(" ")
              .append(figure.top).append(" ")
              .append(figure.width).append(" ")
              .append(figure.height);
    }
    
    @Override
    public void visitGroup(FigureGroup group) {
        output.append(getIndent())
              .append("group ")
              .append(group.getChildCount())
              .append("\n");
        
        for (Figure child : group.getChildren()) {
            FileWriterVisitor childVisitor = new FileWriterVisitor(indentLevel + 1);
            child.accept(childVisitor);
            output.append(childVisitor.getOutput()).append("\n");
        }
        
        // Remove last newline
        if (output.length() > 0 && output.charAt(output.length() - 1) == '\n') {
            output.setLength(output.length() - 1);
        }
    }
    
    private String getIndent() {
        return " ".repeat(indentLevel);
    }
}

// ==================== FIGURE CLASSES ====================

/**
 * Figure - Abstract base class for all figures
 */
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
    
    abstract void accept(FigureVisitor visitor);
    abstract void draw(Graphics g);
    public abstract Figure clone();
}

/**
 * BaseFigure - Unified figure class using Strategy pattern
 * Replaces separate RectangleFigure and EllipseFigure classes
 */
class BaseFigure extends Figure {
    private DrawStrategy strategy;
    
    BaseFigure(int left, int top, int width, int height, DrawStrategy strategy) {
        super(left, top, width, height);
        this.strategy = strategy;
    }
    
    public DrawStrategy getStrategy() {
        return strategy;
    }
    
    @Override
    void accept(FigureVisitor visitor) {
        visitor.visitBaseFigure(this);
    }
    
    @Override
    void draw(Graphics g) {
        strategy.draw(g, left, top, width, height, selected);
    }
    
    @Override
    public Figure clone() {
        return new BaseFigure(left, top, width, height, strategy);
    }
}

// ==================== GROUP (COMPOSITE PATTERN) ====================

/**
 * FigureGroup - Composite pattern with visitor support
 */
class FigureGroup extends Figure {
    private ArrayList<Figure> children = new ArrayList<>();
    
    FigureGroup() {
        super(0, 0, 0, 0);
        updateBounds();
    }
    
    @Override
    void accept(FigureVisitor visitor) {
        visitor.visitGroup(this);
    }
    
    void add(Figure figure) {
        children.add(figure);
        updateBounds();
    }
    
    void remove(Figure figure) {
        children.remove(figure);
        updateBounds();
    }
    
    ArrayList<Figure> getChildren() {
        return new ArrayList<>(children);
    }
    
    int getChildCount() {
        return children.size();
    }
    
    /**
     * Update bounds to encompass all children
     */
    public void updateBounds() {
        if (children.isEmpty()) {
            left = top = width = height = 0;
            return;
        }
        
        int minLeft = Integer.MAX_VALUE;
        int minTop = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxBottom = Integer.MIN_VALUE;
        
        for (Figure child : children) {
            minLeft = Math.min(minLeft, child.left);
            minTop = Math.min(minTop, child.top);
            maxRight = Math.max(maxRight, child.left + child.width);
            maxBottom = Math.max(maxBottom, child.top + child.height);
        }
        
        left = minLeft;
        top = minTop;
        width = maxRight - minLeft;
        height = maxBottom - minTop;
    }
    
    @Override
    void draw(Graphics g) {
        // Draw all children
        for (Figure child : children) {
            child.draw(g);
        }
        
        // If selected, draw selection box around entire group
        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLUE);
            Stroke oldStroke = g2.getStroke();
            float[] dashPattern = {5, 5};
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10, dashPattern, 0));
            g2.drawRect(left, top, width, height);
            g2.setStroke(oldStroke);
        }
    }
    
    @Override
    boolean contains(int x, int y) {
        // Check if any child contains the point
        for (Figure child : children) {
            if (child.contains(x, y)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Figure clone() {
        FigureGroup clone = new FigureGroup();
        for (Figure child : children) {
            clone.add(child.clone());
        }
        return clone;
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
        MoveVisitor visitor = new MoveVisitor(dx, dy);
        figure.accept(visitor);
    }
    
    public void undo() {
        MoveVisitor visitor = new MoveVisitor(-dx, -dy);
        figure.accept(visitor);
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
        ResizeVisitor visitor = new ResizeVisitor(figure, newWidth, newHeight);
        figure.accept(visitor);
    }
    
    public void undo() {
        ResizeVisitor visitor = new ResizeVisitor(figure, oldWidth, oldHeight);
        figure.accept(visitor);
    }
}

class GroupCommand implements Command {
    private ArrayList<Figure> figures;
    private ArrayList<Figure> figuresToGroup;
    private FigureGroup createdGroup;
    private int[] indices;
    
    GroupCommand(ArrayList<Figure> figures, ArrayList<Figure> figuresToGroup) {
        this.figures = figures;
        this.figuresToGroup = new ArrayList<>(figuresToGroup);
        this.indices = new int[figuresToGroup.size()];
    }
    
    public void execute() {
        // Store indices
        for (int i = 0; i < figuresToGroup.size(); i++) {
            indices[i] = figures.indexOf(figuresToGroup.get(i));
        }
        
        // Create group
        createdGroup = new FigureGroup();
        for (Figure fig : figuresToGroup) {
            createdGroup.add(fig);
            figures.remove(fig);
        }
        
        // Add group at position of first figure
        int insertPos = indices[0];
        if (insertPos >= 0 && insertPos <= figures.size()) {
            figures.add(insertPos, createdGroup);
        } else {
            figures.add(createdGroup);
        }
    }
    
    public void undo() {
        // Remove group
        figures.remove(createdGroup);
        
        // Add back original figures
        for (int i = 0; i < figuresToGroup.size(); i++) {
            int idx = indices[i];
            if (idx >= 0 && idx <= figures.size()) {
                figures.add(idx, figuresToGroup.get(i));
            } else {
                figures.add(figuresToGroup.get(i));
            }
        }
    }
}

class UngroupCommand implements Command {
    private ArrayList<Figure> figures;
    private FigureGroup group;
    private ArrayList<Figure> extractedFigures;
    private int groupIndex;
    
    UngroupCommand(ArrayList<Figure> figures, FigureGroup group) {
        this.figures = figures;
        this.group = group;
        this.extractedFigures = new ArrayList<>();
    }
    
    public void execute() {
        groupIndex = figures.indexOf(group);
        extractedFigures = group.getChildren();
        
        figures.remove(group);
        
        // Add extracted figures
        for (int i = 0; i < extractedFigures.size(); i++) {
            int pos = groupIndex + i;
            if (pos >= 0 && pos <= figures.size()) {
                figures.add(pos, extractedFigures.get(i));
            } else {
                figures.add(extractedFigures.get(i));
            }
        }
    }
    
    public void undo() {
        // Remove extracted figures
        for (Figure fig : extractedFigures) {
            figures.remove(fig);
        }
        
        // Add back group
        if (groupIndex >= 0 && groupIndex <= figures.size()) {
            figures.add(groupIndex, group);
        } else {
            figures.add(group);
        }
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
    /**
     * Save figures to file using FileWriterVisitor
     */
    static void save(ArrayList<Figure> figures, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write as a group containing all figures
            writer.println("group " + figures.size());
            for (Figure f : figures) {
                FileWriterVisitor visitor = new FileWriterVisitor(1);
                f.accept(visitor);
                writer.println(visitor.getOutput());
            }
        }
    }
    
    static ArrayList<Figure> load(File file) throws IOException {
        ArrayList<Figure> figures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && line.trim().startsWith("group")) {
                FigureGroup rootGroup = parseGroup(reader, line.trim(), 0);
                figures.addAll(rootGroup.getChildren());
            }
        }
        return figures;
    }
    
    private static FigureGroup parseGroup(BufferedReader reader, String groupLine, int expectedIndent) throws IOException {
        String[] parts = groupLine.trim().split("\\s+");
        int count = Integer.parseInt(parts[1]);
        
        FigureGroup group = new FigureGroup();
        
        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) break;
            
            int indent = countIndent(line);
            String trimmed = line.trim();
            
            Figure fig = parseFigure(reader, trimmed, indent);
            if (fig != null) {
                group.add(fig);
            }
        }
        
        return group;
    }
    
    private static Figure parseFigure(BufferedReader reader, String line, int indent) throws IOException {
        String[] parts = line.split("\\s+");
        
        if (parts[0].equals("group")) {
            return parseGroup(reader, line, indent);
        } else if (parts.length >= 5) {
            String type = parts[0];
            int left = Integer.parseInt(parts[1]);
            int top = Integer.parseInt(parts[2]);
            int width = Integer.parseInt(parts[3]);
            int height = Integer.parseInt(parts[4]);
            
            DrawStrategy strategy = null;
            if (type.equals("rectangle")) {
                strategy = RectangleStrategy.getInstance();
            } else if (type.equals("ellipse")) {
                strategy = EllipseStrategy.getInstance();
            }
            
            if (strategy != null) {
                return new BaseFigure(left, top, width, height, strategy);
            }
        }
        return null;
    }
    
    private static int countIndent(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else break;
        }
        return count;
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
    
    int originalX, originalY;
    int originalWidth, originalHeight;
    int totalDx, totalDy;
    
    DrawingPanel(CommandManager commandManager) {
        this.commandManager = commandManager;
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                
                if (mode.equals("rectangle") || mode.equals("ellipse")) {
                    DrawStrategy strategy = mode.equals("rectangle") 
                        ? RectangleStrategy.getInstance() 
                        : EllipseStrategy.getInstance();
                    currentFigure = new BaseFigure(startX, startY, 1, 1, strategy);
                } else if (mode.equals("select")) {
                    // Select figure - support Ctrl+Click for multi-select
                    boolean addToSelection = e.isControlDown();
                    selectFigureAt(e.getX(), e.getY(), addToSelection);
                } else if (mode.equals("move")) {
                    selectedFigure = findFigureAt(e.getX(), e.getY());
                    if (selectedFigure != null) {
                        originalX = e.getX();
                        originalY = e.getY();
                        totalDx = 0;
                        totalDy = 0;
                    }
                } else if (mode.equals("resize")) {
                    selectedFigure = findFigureAt(e.getX(), e.getY());
                    if (selectedFigure != null) {
                        originalWidth = selectedFigure.width;
                        originalHeight = selectedFigure.height;
                    }
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                if (currentFigure != null && (mode.equals("rectangle") || mode.equals("ellipse"))) {
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
                    
                    selectedFigure.left = selectedFigure.left - totalDx + dx;
                    selectedFigure.top = selectedFigure.top - totalDy + dy;
                    
                    totalDx = dx;
                    totalDy = dy;
                    
                    repaint();
                } else if (mode.equals("resize") && selectedFigure != null) {
                    int newWidth = Math.max(10, Math.abs(e.getX() - selectedFigure.left));
                    int newHeight = Math.max(10, Math.abs(e.getY() - selectedFigure.top));
                    
                    // Use visitor to resize
                    ResizeVisitor visitor = new ResizeVisitor(selectedFigure, newWidth, newHeight);
                    selectedFigure.accept(visitor);
                    
                    repaint();
                }
            }
        });
    }
    
    void setMode(String mode) {
        this.mode = mode;
        for (Figure f : figures) {
            f.selected = false;
        }
        repaint();
    }
    
    void clear() {
        commandManager.executeCommand(new ClearCommand(figures));
        repaint();
    }
    
    void groupSelected() {
        ArrayList<Figure> selected = getSelectedFigures();
        if (selected.size() >= 2) {
            commandManager.executeCommand(new GroupCommand(figures, selected));
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Select at least 2 figures to group", 
                "Group", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    void ungroupSelected() {
        ArrayList<Figure> selected = getSelectedFigures();
        if (selected.size() == 1 && selected.get(0) instanceof FigureGroup) {
            commandManager.executeCommand(new UngroupCommand(figures, (FigureGroup) selected.get(0)));
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Select exactly one group to ungroup", 
                "Ungroup", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    ArrayList<Figure> getSelectedFigures() {
        ArrayList<Figure> selected = new ArrayList<>();
        for (Figure f : figures) {
            if (f.selected) {
                selected.add(f);
            }
        }
        return selected;
    }
    
    void selectFigureAt(int x, int y, boolean addToSelection) {
        if (!addToSelection) {
            // Clear all selections if not adding
            for (Figure f : figures) {
                f.selected = false;
            }
        }
        
        // Find and toggle/select the clicked figure
        for (int i = figures.size() - 1; i >= 0; i--) {
            if (figures.get(i).contains(x, y)) {
                if (addToSelection) {
                    // Toggle selection if Ctrl is held
                    figures.get(i).selected = !figures.get(i).selected;
                } else {
                    // Just select it
                    figures.get(i).selected = true;
                }
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

public class GraphicsEditorStep5 extends JFrame {
    DrawingPanel canvas;
    CommandManager commandManager;
    JButton undoBtn, redoBtn;
    
    GraphicsEditorStep5() {
        setTitle("Graphics Editor - Step 5 (Strategy + Singleton Pattern)");
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
        
        JButton groupBtn = new JButton("Group");
        groupBtn.addActionListener(e -> {
            canvas.groupSelected();
            updateUndoRedoButtons();
        });
        toolbar.add(groupBtn);
        
        JButton ungroupBtn = new JButton("Ungroup");
        ungroupBtn.addActionListener(e -> {
            canvas.ungroupSelected();
            updateUndoRedoButtons();
        });
        toolbar.add(ungroupBtn);
        
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
        SwingUtilities.invokeLater(() -> new GraphicsEditorStep5().setVisible(true));
    }
}
