import java.util.Comparator;

/**
  Simple demo runner for RedBlackTree.

  This tester class just exercises the tree
  with a few deterministic scenarios and prints
  the results so you can inspect the behavior directly.
 */
public final class RedBlackTreeTester {

    /**
      Runs the demo scenarios.

      @param args unused command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("RedBlackTree demo");
        showEmptyTreeBehavior();
        showBasicOperations();
        showDeletionBehavior();
        showClearBehavior();
        showComparatorOrdering();
        showCompatibilityErrors();
    }

    /**
      Demonstrates the behavior of a newly constructed tree.
     */
    private static void showEmptyTreeBehavior() {
        // A freshly constructed tree has no entries and reports zero size.
        // containsKey returns false for any input, and get returns null.
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();

        printSection("EMPTY TREE");
        printTreeState(tree);
        System.out.println("get(10): " + tree.get(10));
        System.out.println("containsKey(10): " + tree.containsKey(10));
    }

    /**
      Demonstrates insertion, update, and lookup operations.
     */
    private static void showBasicOperations() {
        // Each put returns the previous value for that key, or null when the key
        // is new. Re-inserting an existing key updates its value without growing
        // the tree; notice that put(20, TWENTY) returns the old "twenty" value.
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();

        printSection("BASIC OPERATIONS");
        System.out.println("put(10, ten) -> " + tree.put(10, "ten"));
        System.out.println("put(20, twenty) -> " + tree.put(20, "twenty"));
        System.out.println("put(5, five) -> " + tree.put(5, "five"));
        System.out.println("put(15, fifteen) -> " + tree.put(15, "fifteen"));
        System.out.println("put(25, twenty-five) -> " + tree.put(25, "twenty-five"));
        System.out.println("put(20, TWENTY) -> " + tree.put(20, "TWENTY"));
        System.out.println("containsKey(15): " + tree.containsKey(15));
        System.out.println("get(25): " + tree.get(25));
        System.out.println("get(999): " + tree.get(999));
        printTreeState(tree);
    }

    /**
      Demonstrates deletion behavior against a small pre-populated tree.
     */
    private static void showDeletionBehavior() {
        // Build an 11-node tree, then remove several keys that exercise different
        // paths through the deletion fix-up (restoreAfterDeletion). The tree's
        // in-order output after each removal shows the ordered structure shifting.
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        int[] keys = {20, 10, 30, 5, 15, 25, 35, 1, 6, 14, 16};

        for (int key : keys) {
            tree.put(key, "value-" + key);
        }

        printSection("DELETION");
        System.out.println("Before removals: " + tree);
        System.out.println("remove(6) -> " + tree.remove(6));
        System.out.println("remove(5) -> " + tree.remove(5));
        System.out.println("remove(10) -> " + tree.remove(10));
        System.out.println("remove(20) -> " + tree.remove(20));
        System.out.println("remove(999) -> " + tree.remove(999));
        printTreeState(tree);
    }

    /**
      Demonstrates clearing a populated tree.
     */
    private static void showClearBehavior() {
        // clear resets the tree to an empty sentinel-only state in O(1) with no
        // node-by-node teardown.
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        tree.put(8, "eight");
        tree.put(3, "three");
        tree.put(10, "ten");

        printSection("CLEAR");
        System.out.println("Before clear: " + tree);
        tree.clear();
        printTreeState(tree);
    }

    /**
      Demonstrates custom comparator ordering with a non-Comparable key type.
     */
    private static void showComparatorOrdering() {
        // CourseKey does not implement Comparable, so it cannot be used with the
        // natural-ordering constructor. A Comparator supplied at construction time
        // defines the ordering instead. The entries printed in order should appear
        // sorted first by department (CS before MATH before PHYS) and then by
        // catalog number within each department.
        Comparator<CourseKey> courseComparator =
            Comparator.comparing((CourseKey key) -> key.department)
                .thenComparingInt(key -> key.catalogNumber);
        RedBlackTree<CourseKey, String> tree = new RedBlackTree<>(courseComparator);

        printSection("COMPARATOR ORDERING");
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

        printSection("COMPATIBILITY ERRORS");

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
        System.out.println(title);
    }

    /**
      Prints the main observable state of a tree.

      @param tree tree being displayed
     */
    private static <K, V> void printTreeState(RedBlackTree<K, V> tree) {
        System.out.println("size: " + tree.size());
        System.out.println("isEmpty: " + tree.isEmpty());
        System.out.println("toString: " + tree);
        verifyInvariants(tree);
    }

    /**
      Checks the three load-bearing red-black invariants on a tree.
      Walks the entire tree and throws AssertionError on any violation so the
      demo output makes a failed invariant obvious. The checks covered are:
        1) root is BLACK (an empty tree trivially satisfies this),
        2) no RED node has a RED child,
        3) every root-to-sentinel path has the same number of BLACK nodes.

      @param tree tree to inspect
     */
    private static <K, V> void verifyInvariants(RedBlackTree<K, V> tree) {
        if (tree.rootNode != tree.sentinelLeaf
            && tree.rootNode.color != RedBlackTree.NodeColor.BLACK) {
            throw new AssertionError("invariant violated: root is not BLACK");
        }
        if (tree.sentinelLeaf.color != RedBlackTree.NodeColor.BLACK) {
            throw new AssertionError("invariant violated: sentinel leaf is not BLACK");
        }
        verifyNoRedRed(tree, tree.rootNode);
        verifyEqualBlackHeight(tree, tree.rootNode);
        System.out.println("invariants OK: root BLACK, no red-red, equal black-height");
    }

    /**
      Recursively checks that no RED node has a RED child.
      Sentinel subtrees are trivially valid and terminate the recursion.

      @param tree tree being inspected
      @param node current subtree root
     */
    private static <K, V> void verifyNoRedRed(
        RedBlackTree<K, V> tree,
        RedBlackTree<K, V>.Node node
    ) {
        if (node == tree.sentinelLeaf) {
            return;
        }
        if (node.color == RedBlackTree.NodeColor.RED
            && (node.leftChild.color == RedBlackTree.NodeColor.RED
                || node.rightChild.color == RedBlackTree.NodeColor.RED)) {
            throw new AssertionError(
                "invariant violated: RED node has a RED child at key " + node.key);
        }
        verifyNoRedRed(tree, node.leftChild);
        verifyNoRedRed(tree, node.rightChild);
    }

    /**
      Recursively verifies that every root-to-sentinel path has the same
      number of BLACK nodes. Returns the black-height of the supplied subtree,
      counting the sentinel leaf itself as 1.

      @param tree tree being inspected
      @param node current subtree root
      @return black-height of the subtree rooted at node
     */
    private static <K, V> int verifyEqualBlackHeight(
        RedBlackTree<K, V> tree,
        RedBlackTree<K, V>.Node node
    ) {
        if (node == tree.sentinelLeaf) {
            return 1;
        }
        int leftHeight = verifyEqualBlackHeight(tree, node.leftChild);
        int rightHeight = verifyEqualBlackHeight(tree, node.rightChild);
        if (leftHeight != rightHeight) {
            throw new AssertionError(
                "invariant violated: unequal black-height at key " + node.key
                    + " (left=" + leftHeight + ", right=" + rightHeight + ")");
        }
        return leftHeight + (node.color == RedBlackTree.NodeColor.BLACK ? 1 : 0);
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

/**
  Ordered map backed by a red-black tree.
  This class keeps key-value pairs in sorted order while guaranteeing
  logarithmic-time search, insertion, and deletion in the worst case.
  The implementation uses a single shared black sentinel leaf instead of
  null child references. That design removes a large amount of edge-case
  branching from the balancing logic because every branch always ends at a real
  node object with a well-defined color.
  Ordering can come from either a natural ordering, used by the zero-argument
  constructor, or a caller-supplied Comparator, used by the comparator constructor.

  Null keys are always rejected because an ordered tree needs every key
  to participate in a total ordering. Null values are allowed. As with
  Java's java.util.Map, get returning null can mean either a missing key or
  stored null value, so containsKey is provided to disambiguate those cases.

  @param <K> key type used to order nodes in the tree
  @param <V> value type stored alongside each key
 */

final class RedBlackTree<K, V> {
    /**
      Colors used by the balancing rules.
      Red-black trees encode structural balance in node colors rather than in an
      explicit height or priority field.
     */
    enum NodeColor {
        RED,
        BLACK
    }
    /**
      Single node in the tree.
      The same class represents both ordinary data-bearing nodes and the shared
      sentinel leaf. For regular nodes, all fields are meaningful. For the
      sentinel leaf, key and value remain null.
     */
    final class Node {
        K key;
        V value;
        Node leftChild;
        Node rightChild;
        Node parentNode;
        NodeColor color;
        /**
          Creates a node with explicit structural links.
          The constructor is deliberately verbose so each call site makes the
          node's initial role obvious.
          @param key key stored in the node
          @param value value stored in the node
          @param color balancing color stored in the node
          @param leftChild node's initial left child
          @param rightChild node's initial right child
          @param parentNode node's initial parent
         */
        Node(
            K key,
            V value,
            NodeColor color,
            Node leftChild,
            Node rightChild,
            Node parentNode
        ) {
            this.key = key;
            this.value = value;
            this.color = color;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.parentNode = parentNode;
        }
    }
    /**
      Comparator used to order keys, or null when natural ordering is in use.
      Matches the convention used by java.util.TreeMap.
     */
    private final Comparator<? super K> keyComparator;
    /**
      Shared black leaf used at the end of every branch.
      A sentinel removes many special cases because code never has to ask
      whether a child reference is null before checking its color.
     */
    final Node sentinelLeaf;
    /**
      Root node of the tree, or sentinelLeaf when the tree is empty.
     */
    Node rootNode;
    /**
      Number of real key-value pairs currently stored in the tree.
     */
    private int size;
    /**
      Creates an empty tree that uses the keys' natural ordering.
      Keys inserted into this variant must implement Comparable.
     */
    public RedBlackTree() {
        this(null);
    }
    /**
      Creates an empty tree ordered by a caller-supplied comparator.
      Passing null explicitly is allowed and means "use natural
      ordering."
      @param keyComparator comparator that defines key ordering, or null
        for natural ordering
     */
    public RedBlackTree(Comparator<? super K> keyComparator) {
        this.keyComparator = keyComparator;
        sentinelLeaf = new Node(null, null, NodeColor.BLACK, null, null, null);
        // The sentinel's three structural links all point back to itself.
        // That means any traversal that walks off the end of a real node chain
        // (leftChild, rightChild, or parentNode) lands on the sentinel rather
        // than null. Fix-up loops can then read .color, .leftChild, etc. on the
        // result without a null-check guard before each access.
        sentinelLeaf.leftChild = sentinelLeaf;
        sentinelLeaf.rightChild = sentinelLeaf;
        sentinelLeaf.parentNode = sentinelLeaf;
        rootNode = sentinelLeaf;
        size = 0;
    }
    /**
      Returns the number of stored key-value pairs.
      @return current tree size
     */
    public int size() {
        return size;
    }
    /**
      Reports whether the tree currently stores any real nodes.
      @return true when the tree is empty; otherwise false
     */
    public boolean isEmpty() {
        return size == 0;
    }
    /**
      Removes every key-value pair from the tree.
      Resetting the root to the sentinel is enough because no external code can
      retain references to the private node objects.
     */
    public void clear() {
        sentinelLeaf.leftChild = sentinelLeaf;
        sentinelLeaf.rightChild = sentinelLeaf;
        sentinelLeaf.parentNode = sentinelLeaf;
        rootNode = sentinelLeaf;
        size = 0;
    }
    /**
      Tests whether a key is present in the tree.
      @param key key to search for
      @return true when the key is present; otherwise false
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public boolean containsKey(Object key) {
        return findNode(key) != sentinelLeaf;
    }
    /**
      Returns the value associated with a key.
      A null result is ambiguous in the same way it is for
      java.util.Map#get(Object): the key may be absent, or it may be
      present and explicitly mapped to null.
      @param key key to search for
      @return stored value for the key, or null when the key is absent
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V get(Object key) {
        Node locatedNode = findNode(key);
        return locatedNode == sentinelLeaf ? null : locatedNode.value;
    }
    /**
      Inserts or updates a key-value pair.
      If the key already exists, only the value changes and no balancing work is
      required because the tree shape stays the same.
      @param key key to insert or update
      @param value value to associate with the key
      @return previous value for the key, or null when the key was absent
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V put(K key, V value) {
        K validatedKey = requireCompatibleKey(key);
        // Walk the tree looking for either the matching key (update in place)
        // or the leaf position where the new node should attach. The last
        // comparison result tells the post-loop code which child slot to use.
        Node parentNode = sentinelLeaf;
        Node currentNode = rootNode;
        int comparisonToParent = 0;
        while (currentNode != sentinelLeaf) {
            parentNode = currentNode;
            comparisonToParent = compareKeys(validatedKey, currentNode.key);
            if (comparisonToParent < 0) {
                currentNode = currentNode.leftChild;
            } else if (comparisonToParent > 0) {
                currentNode = currentNode.rightChild;
            } else {
                V previousValue = currentNode.value;
                currentNode.value = value;
                return previousValue;
            }
        }
        Node insertedNode = new Node(
            validatedKey,
            value,
            NodeColor.RED,
            sentinelLeaf,
            sentinelLeaf,
            parentNode
        );
        if (parentNode == sentinelLeaf) {
            rootNode = insertedNode;
        } else if (comparisonToParent < 0) {
            parentNode.leftChild = insertedNode;
        } else {
            parentNode.rightChild = insertedNode;
        }
        size++;
        restoreAfterInsertion(insertedNode);
        return null;
    }
    /**
      Removes a key-value pair when present.
      @param key key to remove
      @return removed value, or null when the key was absent
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V remove(Object key) {
        Node nodeToRemove = findNode(key);
        if (nodeToRemove == sentinelLeaf) {
            return null;
        }
        V removedValue = nodeToRemove.value;
        deleteNode(nodeToRemove);
        size--;
        return removedValue;
    }
    /**
      Returns a map-like string representation in ascending key order.
      @return string containing key-value pairs
     */
    @Override
    public String toString() {
        StringBuilder descriptionBuilder = new StringBuilder("{");
        appendEntriesInOrder(rootNode, descriptionBuilder);
        descriptionBuilder.append('}');
        return descriptionBuilder.toString();
    }
    /**
      Restores red-black invariants after inserting a red node.
      A new red node can violate exactly one local rule: it may have a red
      parent. The fix-up climbs upward, resolving that violation with
      recoloring and rotations until the tree is valid again.
      @param currentNode newly inserted node, or the node currently being fixed
     */
    private void restoreAfterInsertion(Node currentNode) {
        // The loop runs only while there is a double-red violation: currentNode is red
        // and so is its parent. A red root or a black parent ends the loop immediately.
        while (currentNode.parentNode.color == NodeColor.RED) {
            if (currentNode.parentNode == currentNode.parentNode.parentNode.leftChild) {
                Node uncleNode = currentNode.parentNode.parentNode.rightChild;
                // Case 1: uncle is RED.
                // The grandparent's two red children (parent + uncle) can absorb the
                // violation by both turning BLACK. The grandparent then turns RED to
                // keep every root-to-leaf black count the same. The double-red problem
                // may now exist one level higher, so the loop continues at the grandparent.
                if (uncleNode.color == NodeColor.RED) {
                    currentNode.parentNode.color = NodeColor.BLACK;
                    uncleNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode.parentNode;
                } else {
                    // Case 2: uncle is BLACK and currentNode is an inner (zig-zag) grandchild.
                    // A left rotation around the parent straightens the zig-zag into a
                    // straight line so Case 3 can fix it. No recoloring here; the rotation
                    // alone does not change any black count.
                    if (currentNode == currentNode.parentNode.rightChild) {
                        currentNode = currentNode.parentNode;
                        rotateLeft(currentNode);
                    }
                    // Case 3: uncle is BLACK and the chain is straight (outer grandchild).
                    // The parent takes the grandparent's position via a right rotation and
                    // receives the grandparent's BLACK color. The grandparent moves down and
                    // becomes RED. Every root-to-leaf black count is restored, and no
                    // double-red remains in this subtree, so the loop ends.
                    currentNode.parentNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    rotateRight(currentNode.parentNode.parentNode);
                }
            } else {
                // Symmetric: parent is the grandparent's right child.
                Node uncleNode = currentNode.parentNode.parentNode.leftChild;
                // Mirror of case 1: uncle is RED, so recolor both children BLACK,
                // grandparent RED, and continue the loop at the grandparent.
                if (uncleNode.color == NodeColor.RED) {
                    currentNode.parentNode.color = NodeColor.BLACK;
                    uncleNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode.parentNode;
                } else {
                    // Mirror of case 2: currentNode is an inner (zig-zag) left grandchild;
                    // right-rotate to straighten the chain, then fall into case 3.
                    if (currentNode == currentNode.parentNode.leftChild) {
                        currentNode = currentNode.parentNode;
                        rotateRight(currentNode);
                    }
                    // Mirror of case 3: straight right-right chain, so recolor and left-rotate
                    // to lift the parent up and push the grandparent down as a RED child.
                    currentNode.parentNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    rotateLeft(currentNode.parentNode.parentNode);
                }
            }
        }
        // The root must always be BLACK. If the loop pushed a RED mark all the way to
        // the top, this line absorbs it without changing any black count (the root is
        // not on any path counted between two real nodes).
        rootNode.color = NodeColor.BLACK;
        rootNode.parentNode = sentinelLeaf;
    }
    /**
      Removes a node from the tree and restores balance when necessary.
      The deletion algorithm first performs the structural removal, then repairs
      any black-height imbalance created by removing a black node.
      @param nodeToRemove existing node that should be deleted
     */
    private void deleteNode(Node nodeToRemove) {
        Node nodeActuallyRemoved = nodeToRemove;
        NodeColor removedNodeColor = nodeActuallyRemoved.color;
        Node fixupStartNode;
        if (nodeToRemove.leftChild == sentinelLeaf) {
            // Node has at most a right child: splice it out by linking the right child
            // directly to nodeToRemove's parent. fixupStartNode is the child that moves
            // into the gap (may be the sentinel if the node was a leaf).
            fixupStartNode = nodeToRemove.rightChild;
            replaceSubtree(nodeToRemove, nodeToRemove.rightChild);
        } else if (nodeToRemove.rightChild == sentinelLeaf) {
            // Node has only a left child: same idea, splice the left child into place.
            fixupStartNode = nodeToRemove.leftChild;
            replaceSubtree(nodeToRemove, nodeToRemove.leftChild);
        } else {
            // Node has two children. We cannot remove it directly because it has two
            // subtrees. Instead we find its in-order successor (the leftmost node in
            // the right subtree), copy its key and value here, and physically remove
            // the successor. The successor has no left child by definition, so it falls
            // into one of the two cases above.
            nodeActuallyRemoved = minimumNode(nodeToRemove.rightChild);
            removedNodeColor = nodeActuallyRemoved.color;
            fixupStartNode = nodeActuallyRemoved.rightChild;
            if (nodeActuallyRemoved.parentNode == nodeToRemove) {
                // The successor is nodeToRemove's direct right child; its right
                // subtree already hangs in the right place, but the parent link of
                // fixupStartNode (possibly the sentinel) still needs to point at the
                // successor so the fix-up loop can walk upward correctly.
                fixupStartNode.parentNode = nodeActuallyRemoved;
            } else {
                replaceSubtree(nodeActuallyRemoved, nodeActuallyRemoved.rightChild);
                nodeActuallyRemoved.rightChild = nodeToRemove.rightChild;
                nodeActuallyRemoved.rightChild.parentNode = nodeActuallyRemoved;
            }
            replaceSubtree(nodeToRemove, nodeActuallyRemoved);
            nodeActuallyRemoved.leftChild = nodeToRemove.leftChild;
            nodeActuallyRemoved.leftChild.parentNode = nodeActuallyRemoved;
            // The successor inherits nodeToRemove's color so the black count on paths
            // through this position stays the same. The color we track for fix-up
            // purposes is the successor's *original* color (already saved above).
            nodeActuallyRemoved.color = nodeToRemove.color;
        }
        // A red node contributes nothing to black counts, so removing one never
        // creates an imbalance. Only the removal of a black node requires repair.
        if (removedNodeColor == NodeColor.BLACK) {
            restoreAfterDeletion(fixupStartNode);
        }
    }
    /**
      Restores red-black invariants after deletion.
      Removing a black node can leave one path effectively "short" a black
      node. The fix-up treats that deficit as moving upward until recoloring or
      rotation absorbs it.
      @param currentNode node carrying the temporary black-height deficit
     */
    private void restoreAfterDeletion(Node currentNode) {
        // currentNode carries a conceptual "extra black": it is one black short on
        // every path that runs through it. The loop moves that deficit upward or
        // resolves it with recoloring and rotation until the tree is balanced again.
        // The loop ends when currentNode is RED (coloring it BLACK absorbs the deficit)
        // or when it reaches the root (the root can simply be blackened).
        while (currentNode != rootNode && currentNode.color == NodeColor.BLACK) {
            if (currentNode == currentNode.parentNode.leftChild) {
                Node siblingNode = currentNode.parentNode.rightChild;
                // Case 1: sibling is RED.
                // A red sibling means the parent is BLACK. We recolor the sibling BLACK,
                // the parent RED, and rotate left. This does not yet fix the deficit, but
                // it gives currentNode a new BLACK sibling so one of cases 2-4 can finish
                // the repair.
                if (siblingNode.color == NodeColor.RED) {
                    siblingNode.color = NodeColor.BLACK;
                    currentNode.parentNode.color = NodeColor.RED;
                    rotateLeft(currentNode.parentNode);
                    siblingNode = currentNode.parentNode.rightChild;
                }
                // Case 2: sibling is BLACK with two BLACK children.
                // Neither of the sibling's subtrees has a spare black node to donate.
                // Recoloring the sibling RED reduces its black count by 1, equalizing it
                // with currentNode's deficient side. The deficit is now absorbed into the
                // parent, so we move the loop pointer upward and continue there.
                if (
                    siblingNode.leftChild.color == NodeColor.BLACK
                    && siblingNode.rightChild.color == NodeColor.BLACK
                ) {
                    siblingNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode;
                } else {
                    // Case 3: sibling is BLACK, its far (right) child is BLACK, near (left) is RED.
                    // We need the far child to be RED for Case 4. A right rotation around the
                    // sibling, plus a recolor, repositions the RED child to the far side without
                    // changing any black count, setting up Case 4 to finish the repair.
                    if (siblingNode.rightChild.color == NodeColor.BLACK) {
                        siblingNode.leftChild.color = NodeColor.BLACK;
                        siblingNode.color = NodeColor.RED;
                        rotateRight(siblingNode);
                        siblingNode = currentNode.parentNode.rightChild;
                    }
                    // Case 4: sibling is BLACK with a RED far (right) child.
                    // A left rotation lifts the sibling to the parent's position. The sibling
                    // takes the parent's color so the subtree root looks the same from above.
                    // The parent moves down and becomes BLACK (adding a black node to the
                    // deficient side). The far child also becomes BLACK (keeping the sibling's
                    // side balanced). The deficit is fully absorbed, so currentNode is set
                    // to root and the loop exits immediately.
                    siblingNode.color = currentNode.parentNode.color;
                    currentNode.parentNode.color = NodeColor.BLACK;
                    siblingNode.rightChild.color = NodeColor.BLACK;
                    rotateLeft(currentNode.parentNode);
                    currentNode = rootNode;
                }
            } else {
                // Symmetric: currentNode is the right child; sibling is on the left.
                Node siblingNode = currentNode.parentNode.leftChild;
                // Mirror of case 1: RED sibling, so recolor and right-rotate to get a BLACK
                // sibling, then continue with cases 2-4.
                if (siblingNode.color == NodeColor.RED) {
                    siblingNode.color = NodeColor.BLACK;
                    currentNode.parentNode.color = NodeColor.RED;
                    rotateRight(currentNode.parentNode);
                    siblingNode = currentNode.parentNode.leftChild;
                }
                // Mirror of case 2: sibling and both its children are BLACK, so recolor
                // the sibling RED and push the deficit to the parent.
                if (
                    siblingNode.rightChild.color == NodeColor.BLACK
                    && siblingNode.leftChild.color == NodeColor.BLACK
                ) {
                    siblingNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode;
                } else {
                    // Mirror of case 3: sibling's far (left) child is BLACK, near (right) is RED;
                    // left-rotate the sibling to move the RED child to the far side.
                    if (siblingNode.leftChild.color == NodeColor.BLACK) {
                        siblingNode.rightChild.color = NodeColor.BLACK;
                        siblingNode.color = NodeColor.RED;
                        rotateLeft(siblingNode);
                        siblingNode = currentNode.parentNode.leftChild;
                    }
                    // Mirror of case 4: sibling's far (left) child is RED, so right-rotate to lift
                    // the sibling, absorb the deficit, and exit the loop.
                    siblingNode.color = currentNode.parentNode.color;
                    currentNode.parentNode.color = NodeColor.BLACK;
                    siblingNode.leftChild.color = NodeColor.BLACK;
                    rotateRight(currentNode.parentNode);
                    currentNode = rootNode;
                }
            }
        }
        // If the loop ended because currentNode is RED, coloring it BLACK absorbs the
        // one-black deficit without changing any other path count. If the loop ended
        // because currentNode reached the root, blackening the root is always safe.
        currentNode.color = NodeColor.BLACK;
        rootNode.parentNode = sentinelLeaf;
    }
    /**
      Rotates a subtree left around the supplied pivot node.
      A left rotation moves the pivot's right child upward and the pivot itself
      downward to the left. The in-order key ordering does not change.
      @param pivotNode root of the subtree being rotated
     */
    private void rotateLeft(Node pivotNode) {
        // promoted's left subtree was ordered between pivot and promoted,
        // so it becomes pivot's right child. In-order key sequence is unchanged.
        Node promotedNode = pivotNode.rightChild;
        pivotNode.rightChild = promotedNode.leftChild;
        if (promotedNode.leftChild != sentinelLeaf) {
            promotedNode.leftChild.parentNode = pivotNode;
        }
        promotedNode.parentNode = pivotNode.parentNode;
        if (pivotNode.parentNode == sentinelLeaf) {
            rootNode = promotedNode;
        } else if (pivotNode == pivotNode.parentNode.leftChild) {
            pivotNode.parentNode.leftChild = promotedNode;
        } else {
            pivotNode.parentNode.rightChild = promotedNode;
        }
        promotedNode.leftChild = pivotNode;
        pivotNode.parentNode = promotedNode;
    }
    /**
      Rotates a subtree right around the supplied pivot node.
      A right rotation is the mirror image of rotateLeft.
      @param pivotNode root of the subtree being rotated
     */
    private void rotateRight(Node pivotNode) {
        // Mirror image of rotateLeft. promoted's right subtree was ordered between
        // promoted and pivot, so it becomes pivot's left child. In-order key sequence
        // is unchanged.
        Node promotedNode = pivotNode.leftChild;
        pivotNode.leftChild = promotedNode.rightChild;
        if (promotedNode.rightChild != sentinelLeaf) {
            promotedNode.rightChild.parentNode = pivotNode;
        }
        promotedNode.parentNode = pivotNode.parentNode;
        if (pivotNode.parentNode == sentinelLeaf) {
            rootNode = promotedNode;
        } else if (pivotNode == pivotNode.parentNode.rightChild) {
            pivotNode.parentNode.rightChild = promotedNode;
        } else {
            pivotNode.parentNode.leftChild = promotedNode;
        }
        promotedNode.rightChild = pivotNode;
        pivotNode.parentNode = promotedNode;
    }
    /**
      Replaces one subtree with another.
      This helper only rewires parent-child references. It does not rebalance
      the tree and does not change the child links beneath replacementNode.
      @param nodeBeingReplaced subtree root being removed from its current position
      @param replacementNode subtree root taking its place
     */
    private void replaceSubtree(Node nodeBeingReplaced, Node replacementNode) {
        if (nodeBeingReplaced.parentNode == sentinelLeaf) {
            rootNode = replacementNode;
        } else if (nodeBeingReplaced == nodeBeingReplaced.parentNode.leftChild) {
            nodeBeingReplaced.parentNode.leftChild = replacementNode;
        } else {
            nodeBeingReplaced.parentNode.rightChild = replacementNode;
        }
        replacementNode.parentNode = nodeBeingReplaced.parentNode;
    }
    /**
      Returns the node containing a key, or the sentinel leaf when absent.
      Accepting Object keeps the public API aligned with normal
      map-style lookup methods.
      @param key key to search for
      @return matching node or sentinelLeaf
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    private Node findNode(Object key) {
        K validatedKey = castAndValidateKey(key);
        Node currentNode = rootNode;
        while (currentNode != sentinelLeaf) {
            int comparison = compareKeys(validatedKey, currentNode.key);
            if (comparison == 0) {
                return currentNode;
            }
            currentNode = comparison < 0 ? currentNode.leftChild : currentNode.rightChild;
        }
        return sentinelLeaf;
    }
    /**
      Returns the smallest node reachable from a starting node.
      @param startNode subtree root to search
      @return leftmost node in the subtree
     */
    private Node minimumNode(Node startNode) {
        Node currentNode = startNode;
        while (currentNode.leftChild != sentinelLeaf) {
            currentNode = currentNode.leftChild;
        }
        return currentNode;
    }
    /**
      Appends entries in ascending key order for toString.
      @param currentNode current subtree root
      @param descriptionBuilder builder receiving textual entries
     */
    private void appendEntriesInOrder(Node currentNode, StringBuilder descriptionBuilder) {
        if (currentNode == sentinelLeaf) {
            return;
        }
        appendEntriesInOrder(currentNode.leftChild, descriptionBuilder);
        if (descriptionBuilder.length() > 1) {
            descriptionBuilder.append(", ");
        }
        descriptionBuilder
            .append(currentNode.key)
            .append('=')
            .append(currentNode.value);
        appendEntriesInOrder(currentNode.rightChild, descriptionBuilder);
    }
    /**
      Ensures a key is non-null and compatible with the active ordering strategy.
      For comparator-backed trees, the comparator defines compatibility. For
      natural-order trees, keys must implement Comparable.
      @param key user-supplied key
      @return the same key when it is valid
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    private K requireCompatibleKey(K key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (rootNode == sentinelLeaf && keyComparator == null && !(key instanceof Comparable<?>)) {
            throw incompatibleKeyException(key, null);
        }
        try {
            if (rootNode != sentinelLeaf) {
                compareKeys(key, rootNode.key);
            } else {
                compareKeys(key, key);
            }
        } catch (ClassCastException comparisonException) {
            throw incompatibleKeyException(key, comparisonException);
        }
        return key;
    }
    /**
      Casts an externally supplied lookup key to the tree's key type and validates it.
      The unchecked cast mirrors the way Java collections accept Object
      for lookup methods and then validate compatibility at runtime.
      @param key externally supplied lookup key
      @return validated key cast to K
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be treated as a K
     */
    @SuppressWarnings("unchecked")
    private K castAndValidateKey(Object key) {
        return requireCompatibleKey((K) key);
    }
    /**
      Creates a descriptive compatibility exception for an invalid key.
      @param key incompatible key
      @param cause underlying comparison failure, if any
      @return descriptive exception ready to be thrown
     */
    private ClassCastException incompatibleKeyException(Object key, ClassCastException cause) {
        StringBuilder messageBuilder =
            new StringBuilder("Key of type ")
                .append(key.getClass().getName())
                .append(" is incompatible with this tree");
        if (rootNode != sentinelLeaf) {
            messageBuilder
                .append(". Current root key type: ")
                .append(rootNode.key.getClass().getName());
        }
        messageBuilder.append('.');
        ClassCastException wrappedException = new ClassCastException(messageBuilder.toString());
        if (cause != null) {
            wrappedException.initCause(cause);
        }
        return wrappedException;
    }
    /**
      Compares two keys using the active ordering strategy.
      This method centralizes ordering so the rest of the tree code never has to
      care whether keys are using natural ordering or a custom comparator.
      @param firstKey left operand
      @param secondKey right operand
      @return negative when firstKey < secondKey, positive when greater, otherwise zero
      @throws ClassCastException when the keys cannot be compared
     */
    @SuppressWarnings("unchecked")
    private int compareKeys(K firstKey, K secondKey) {
        if (keyComparator != null) {
            return keyComparator.compare(firstKey, secondKey);
        }
        return ((Comparable<K>) firstKey).compareTo(secondKey);
    }
}
