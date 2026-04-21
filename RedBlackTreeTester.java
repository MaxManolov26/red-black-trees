import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
  Simple demo runner for RedBlackTree.

  This class does not use random data or assertion helpers. It just exercises
  the tree with a few deterministic scenarios and prints the results so you can
  inspect the behavior directly.
 */
public final class RedBlackTreeTester {
    /**
      Utility class; no instances are needed.
     */
    private RedBlackTreeTester() {
    }

    /**
      Runs the demo scenarios.

      @param args unused command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("RedBlackTree demo");
        showEmptyTreeBehavior();
        showBasicOperations();
        showDeletionAndNavigation();
        showBulkLoadAndClear();
        showComparatorOrdering();
        showCompatibilityErrors();
    }

    /**
      Demonstrates the behavior of a newly constructed tree.
     */
    private static void showEmptyTreeBehavior() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();

        printSection("Empty tree");
        printTreeState(tree);
        System.out.println("getOrDefault(10, fallback): " + tree.getOrDefault(10, "fallback"));
        System.out.println("firstEntry(): " + tree.firstEntry());
        System.out.println("lastEntry(): " + tree.lastEntry());

        try {
            System.out.println("firstKey(): " + tree.firstKey());
        } catch (NoSuchElementException exception) {
            System.out.println("firstKey() threw: " + exception.getMessage());
        }

        try {
            System.out.println("lastKey(): " + tree.lastKey());
        } catch (NoSuchElementException exception) {
            System.out.println("lastKey() threw: " + exception.getMessage());
        }
    }

    /**
      Demonstrates insertion, update, replacement, and lookup operations.
     */
    private static void showBasicOperations() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();

        printSection("Basic operations");
        System.out.println("put(10, ten) -> " + tree.put(10, "ten"));
        System.out.println("put(20, twenty) -> " + tree.put(20, "twenty"));
        System.out.println("put(5, five) -> " + tree.put(5, "five"));
        System.out.println("put(15, fifteen) -> " + tree.put(15, "fifteen"));
        System.out.println("put(20, TWENTY) -> " + tree.put(20, "TWENTY"));
        System.out.println("putIfAbsent(20, ignored) -> " + tree.putIfAbsent(20, "ignored"));
        System.out.println("putIfAbsent(25, twenty-five) -> " + tree.putIfAbsent(25, "twenty-five"));
        System.out.println("replace(10, TEN) -> " + tree.replace(10, "TEN"));
        System.out.println("replace(5, five, FIVE) -> " + tree.replace(5, "five", "FIVE"));
        System.out.println("containsKey(15): " + tree.containsKey(15));
        System.out.println("containsValue(TWENTY): " + tree.containsValue("TWENTY"));
        System.out.println("get(25): " + tree.get(25));
        System.out.println("remove(99): " + tree.remove(99));
        printTreeState(tree);
    }

    /**
      Demonstrates navigation helpers and deletion behavior.
     */
    private static void showDeletionAndNavigation() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        int[] keys = {20, 10, 30, 5, 15, 25, 35, 1, 6, 14, 16};

        for (int key : keys) {
            tree.put(key, "value-" + key);
        }

        printSection("Deletion and navigation");
        printNavigation(tree, 15);
        System.out.println("remove(6) -> " + tree.remove(6));
        System.out.println("remove(5) -> " + tree.remove(5));
        System.out.println("remove(10) -> " + tree.remove(10));
        System.out.println("remove(20) -> " + tree.remove(20));
        printNavigation(tree, 15);
        printTreeState(tree);
    }

    /**
      Demonstrates bulk loading, clearing, and rebuilding the tree.
     */
    private static void showBulkLoadAndClear() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        Map<Integer, String> entries = new LinkedHashMap<>();

        entries.put(8, "eight");
        entries.put(3, "three");
        entries.put(10, "ten");
        entries.put(1, "one");
        entries.put(6, "six");
        entries.put(14, "fourteen");

        printSection("Bulk load and clear");
        tree.putAll(entries);
        printTreeState(tree);
        tree.clear();
        System.out.println("After clear:");
        printTreeState(tree);
        tree.put(9, null);
        tree.put(7, "seven");
        tree.put(11, "eleven");
        System.out.println("After rebuilding:");
        printTreeState(tree);
    }

    /**
      Demonstrates custom comparator ordering with a non-Comparable key type.
     */
    private static void showComparatorOrdering() {
        Comparator<CourseKey> courseComparator =
            Comparator.comparing((CourseKey key) -> key.department)
                .thenComparingInt(key -> key.catalogNumber);
        RedBlackTree<CourseKey, String> tree = new RedBlackTree<>(courseComparator);

        printSection("Comparator ordering");
        tree.put(new CourseKey("CS", 61), "data structures");
        tree.put(new CourseKey("CS", 62), "computer architecture");
        tree.put(new CourseKey("MATH", 53), "multivariable calculus");
        tree.put(new CourseKey("PHYS", 43), "electricity and magnetism");
        tree.put(new CourseKey("CS", 70), "discrete mathematics");
        printTreeState(tree);
    }

    /**
      Demonstrates the tree's null-key and incompatible-key errors.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void showCompatibilityErrors() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        RedBlackTree rawTree = tree;

        tree.put(1, "one");

        printSection("Compatibility errors");

        try {
            tree.containsKey(null);
        } catch (NullPointerException exception) {
            System.out.println("containsKey(null) threw: " + exception.getMessage());
        }

        try {
            tree.containsKey("bad");
        } catch (ClassCastException exception) {
            System.out.println("containsKey(\"bad\") threw: " + exception.getMessage());
        }

        try {
            rawTree.put("bad", "value");
        } catch (ClassCastException exception) {
            System.out.println("put(\"bad\", value) threw: " + exception.getMessage());
        }
    }

    /**
      Prints a section title.

      @param title section name
     */
    private static void printSection(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    /**
      Prints the main observable state of a tree.

      @param tree tree being displayed
     */
    private static <K, V> void printTreeState(RedBlackTree<K, V> tree) {
        System.out.println("size: " + tree.size());
        System.out.println("isEmpty: " + tree.isEmpty());
        System.out.println("entriesInOrder: " + tree.entriesInOrder());
        System.out.println("keysInOrder: " + tree.keysInOrder());
        System.out.println("valuesInKeyOrder: " + tree.valuesInKeyOrder());
        System.out.println("firstEntry: " + tree.firstEntry());
        System.out.println("lastEntry: " + tree.lastEntry());
        System.out.println("validateInvariants: " + tree.validateInvariants());
        System.out.println("toString: " + tree);
    }

    /**
      Prints the navigation helpers for one probe key.

      @param tree tree being queried
      @param key probe key
     */
    private static <K, V> void printNavigation(RedBlackTree<K, V> tree, K key) {
        System.out.println("lowerKey(" + key + "): " + tree.lowerKey(key));
        System.out.println("floorKey(" + key + "): " + tree.floorKey(key));
        System.out.println("ceilingKey(" + key + "): " + tree.ceilingKey(key));
        System.out.println("higherKey(" + key + "): " + tree.higherKey(key));
        System.out.println("lowerEntry(" + key + "): " + tree.lowerEntry(key));
        System.out.println("floorEntry(" + key + "): " + tree.floorEntry(key));
        System.out.println("ceilingEntry(" + key + "): " + tree.ceilingEntry(key));
        System.out.println("higherEntry(" + key + "): " + tree.higherEntry(key));
    }

    /**
      Non-Comparable key type used to demonstrate comparator-backed ordering.
     */
    private static final class CourseKey {
        private final String department;
        private final int catalogNumber;

        /**
          Creates a key describing a course.

          @param department department prefix
          @param catalogNumber course number
         */
        private CourseKey(String department, int catalogNumber) {
            this.department = department;
            this.catalogNumber = catalogNumber;
        }

        /**
          Returns a readable label for console output.

          @return formatted course identifier
         */
        @Override
        public String toString() {
            return department + "-" + catalogNumber;
        }
    }
}
