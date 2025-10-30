import java.util.ArrayList;

/**
 * Test program to verify MoveVisitor functionality
 * Tests the exact scenario from the implementation guide
 */
public class TestMoveVisitor {
    
    public static void main(String[] args) {
        System.out.println("=== Testing MoveVisitor ===\n");
        
        // Test 1: Move a single rectangle
        testMoveRectangle();
        
        // Test 2: Move a group
        testMoveGroup();
        
        // Test 3: Undo move operation
        testUndoMove();
        
        System.out.println("\n=== All tests passed! ===");
    }
    
    static void testMoveRectangle() {
        System.out.println("Test 1: Move a Rectangle");
        System.out.println("-".repeat(40));
        
        // Step 1: Create a rectangle at (100, 100) with size 50x50
        RectangleFigure rect = new RectangleFigure(100, 100, 50, 50);
        System.out.println("1. Created rectangle at position: (" + rect.left + ", " + rect.top + ")");
        System.out.println("   Size: " + rect.width + "x" + rect.height);
        
        // Step 2: Note its position
        int originalLeft = rect.left;
        int originalTop = rect.top;
        System.out.println("\n2. Original position noted: (" + originalLeft + ", " + originalTop + ")");
        
        // Step 3: Move it by (50, 30)
        int dx = 50;
        int dy = 30;
        System.out.println("\n3. Moving by dx=" + dx + ", dy=" + dy);
        
        MoveVisitor moveVisitor = new MoveVisitor(dx, dy);
        rect.accept(moveVisitor);
        
        System.out.println("   New position: (" + rect.left + ", " + rect.top + ")");
        
        // Step 4: Verify the position changed by exactly (dx, dy)
        int expectedLeft = originalLeft + dx;
        int expectedTop = originalTop + dy;
        
        System.out.println("\n4. Verifying position:");
        System.out.println("   Expected: (" + expectedLeft + ", " + expectedTop + ")");
        System.out.println("   Actual:   (" + rect.left + ", " + rect.top + ")");
        
        if (rect.left == expectedLeft && rect.top == expectedTop) {
            System.out.println("   ✓ Position changed correctly!");
        } else {
            System.out.println("   ✗ ERROR: Position is incorrect!");
            System.exit(1);
        }
        
        // Step 5: Undo (move by -dx, -dy)
        System.out.println("\n5. Undoing move...");
        MoveVisitor undoVisitor = new MoveVisitor(-dx, -dy);
        rect.accept(undoVisitor);
        
        System.out.println("   Position after undo: (" + rect.left + ", " + rect.top + ")");
        
        if (rect.left == originalLeft && rect.top == originalTop) {
            System.out.println("   ✓ Position restored correctly!");
        } else {
            System.out.println("   ✗ ERROR: Position not restored!");
            System.exit(1);
        }
        
        System.out.println("\n✓ Test 1 passed!\n");
    }
    
    static void testMoveGroup() {
        System.out.println("Test 2: Move a Group");
        System.out.println("-".repeat(40));
        
        // Create a group with 3 rectangles
        FigureGroup group = new FigureGroup();
        RectangleFigure rect1 = new RectangleFigure(10, 10, 20, 20);
        RectangleFigure rect2 = new RectangleFigure(40, 10, 20, 20);
        RectangleFigure rect3 = new RectangleFigure(25, 40, 20, 20);
        
        group.add(rect1);
        group.add(rect2);
        group.add(rect3);
        
        System.out.println("1. Created group with 3 rectangles:");
        System.out.println("   Rect1: (" + rect1.left + ", " + rect1.top + ")");
        System.out.println("   Rect2: (" + rect2.left + ", " + rect2.top + ")");
        System.out.println("   Rect3: (" + rect3.left + ", " + rect3.top + ")");
        System.out.println("   Group bounds: (" + group.left + ", " + group.top + ") " + 
                          group.width + "x" + group.height);
        
        // Store original positions
        int[] originals = {
            rect1.left, rect1.top,
            rect2.left, rect2.top,
            rect3.left, rect3.top,
            group.left, group.top
        };
        
        // Move the group
        int dx = 100;
        int dy = 50;
        System.out.println("\n2. Moving group by dx=" + dx + ", dy=" + dy);
        
        MoveVisitor moveVisitor = new MoveVisitor(dx, dy);
        group.accept(moveVisitor);
        
        System.out.println("\n3. After move:");
        System.out.println("   Rect1: (" + rect1.left + ", " + rect1.top + ")");
        System.out.println("   Rect2: (" + rect2.left + ", " + rect2.top + ")");
        System.out.println("   Rect3: (" + rect3.left + ", " + rect3.top + ")");
        System.out.println("   Group bounds: (" + group.left + ", " + group.top + ") " + 
                          group.width + "x" + group.height);
        
        // Verify all children moved correctly
        boolean allCorrect = true;
        allCorrect &= (rect1.left == originals[0] + dx) && (rect1.top == originals[1] + dy);
        allCorrect &= (rect2.left == originals[2] + dx) && (rect2.top == originals[3] + dy);
        allCorrect &= (rect3.left == originals[4] + dx) && (rect3.top == originals[5] + dy);
        allCorrect &= (group.left == originals[6] + dx) && (group.top == originals[7] + dy);
        
        if (allCorrect) {
            System.out.println("   ✓ All figures moved correctly!");
        } else {
            System.out.println("   ✗ ERROR: Some figures moved incorrectly!");
            System.exit(1);
        }
        
        System.out.println("\n✓ Test 2 passed!\n");
    }
    
    static void testUndoMove() {
        System.out.println("Test 3: Undo Move with Command Pattern");
        System.out.println("-".repeat(40));
        
        // Create figure and command manager
        ArrayList<Figure> figures = new ArrayList<>();
        RectangleFigure rect = new RectangleFigure(200, 150, 60, 40);
        figures.add(rect);
        
        CommandManager cmdManager = new CommandManager();
        
        System.out.println("1. Initial rectangle at: (" + rect.left + ", " + rect.top + ")");
        
        int originalLeft = rect.left;
        int originalTop = rect.top;
        
        // Execute move command
        int dx = 75;
        int dy = -25;
        MoveFigureCommand moveCmd = new MoveFigureCommand(rect, dx, dy);
        
        System.out.println("\n2. Executing move command (dx=" + dx + ", dy=" + dy + ")");
        cmdManager.executeCommand(moveCmd);
        
        System.out.println("   Position after move: (" + rect.left + ", " + rect.top + ")");
        
        // Verify move
        if (rect.left == originalLeft + dx && rect.top == originalTop + dy) {
            System.out.println("   ✓ Move executed correctly!");
        } else {
            System.out.println("   ✗ ERROR: Move failed!");
            System.exit(1);
        }
        
        // Test undo
        System.out.println("\n3. Executing undo...");
        cmdManager.undo();
        
        System.out.println("   Position after undo: (" + rect.left + ", " + rect.top + ")");
        
        // Verify undo
        if (rect.left == originalLeft && rect.top == originalTop) {
            System.out.println("   ✓ Undo restored position correctly!");
        } else {
            System.out.println("   ✗ ERROR: Undo failed!");
            System.exit(1);
        }
        
        // Test redo
        System.out.println("\n4. Executing redo...");
        cmdManager.redo();
        
        System.out.println("   Position after redo: (" + rect.left + ", " + rect.top + ")");
        
        // Verify redo
        if (rect.left == originalLeft + dx && rect.top == originalTop + dy) {
            System.out.println("   ✓ Redo restored moved position correctly!");
        } else {
            System.out.println("   ✗ ERROR: Redo failed!");
            System.exit(1);
        }
        
        System.out.println("\n✓ Test 3 passed!\n");
    }
}
