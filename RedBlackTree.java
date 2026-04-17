/**
    Minimal red-black tree skeleton keyed by comparable values.

    This version only models the tree structure and supports read-only lookup methods.
    Mutation logic is intentionally absent for now, and empty child or parent references are
    represented with a shared black {@code NIL} sentinel.

    @param <K> key type used to order nodes in the tree
    @param <V> value type stored alongside each key
*/
public final class RedBlackTree<K extends Comparable<K>, V> {
    /**
        Color assigned to a tree node.

        Red-black trees encode balance information in node colors. The current barebones
        implementation does not rebalance yet, but the enum keeps the representation explicit.
    */
    private enum NodeColor {
        RED,
        BLACK
    }

    /**
        Single node within the tree.

        Each node carries a key, a value, links to both children, a link to the parent,
        and a color. Missing links point to the shared {@code NIL} sentinel.
    */
    private final class Node {
        K key;
        V value;
        Node left;
        Node right;
        Node parent;
        NodeColor color;

        /**
            Creates a tree node.

            @param key key stored in the node
            @param value value stored in the node
            @param color color assigned to the node
        */
        Node(K key, V value, NodeColor color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    /**
        Shared black sentinel used for all missing links.
    */
    private final Node NIL;

    /**
        Root of the tree.

        When the tree is empty, the root is {@code NIL}.
    */
    private Node root;

    /**
        Number of real key-value pairs currently stored in the tree.
    */
    private int size;

    /**
        Creates an empty tree.
    */
    public RedBlackTree() {
        NIL = new Node(null, null, NodeColor.BLACK);
        NIL.left = NIL;
        NIL.right = NIL;
        NIL.parent = NIL;

        root = NIL;
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
        Reports whether the tree currently contains any real nodes.

        @return {@code true} when the tree is empty; otherwise {@code false}
    */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
        Tests whether a key is present in the tree.

        Lookup walks the binary-search-tree path from the root toward a matching key.

        @param key key to search for
        @return {@code true} when the key is present; otherwise {@code false}
    */
    public boolean containsKey(K key) {
        return findNode(key) != NIL;
    }

    /**
        Returns the value associated with a key.

        @param key key to search for
        @return stored value for the key, or {@code null} when the key is absent
    */
    public V get(K key) {
        Node locatedNode = findNode(key);
        return locatedNode == NIL ? null : locatedNode.value;
    }

    /**
        Inserts a key-value pair into the tree.

        The binary-search-tree placement is handled here. Rebalancing is delegated to the
        separately maintained {@code insertFixup} method, which is expected to use the
        rotation helpers Max make.

        @param key key to insert
        @param value value to associate with the key
        @throws NullPointerException when {@code key} is {@code null}
    */
    public void insert(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }

        Node parent = NIL;
        Node current = root;

        while (current != NIL) {
            parent = current;
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                current.value = value;
                return;
            }
            current = comparison < 0 ? current.left : current.right;
        }

        // new nodes are always red, and their children are the black NIL sentinel
        Node insertedNode = new Node(key, value, NodeColor.RED);
        insertedNode.left = NIL;
        insertedNode.right = NIL;
        insertedNode.parent = parent;

        if (parent == NIL) {
            root = insertedNode;
        } else if (key.compareTo(parent.key) < 0) {
            parent.left = insertedNode;
        } else {
            parent.right = insertedNode;
        }

        size++;
        insertFixup(insertedNode);
    }

    /**
        Walks the binary-search-tree path for a key and returns the matching node when found.

        A missing key yields {@code null}.

        @param key key to search for
        @return matching node, or {@code null} when the key does not exist
        @throws NullPointerException when {@code key} is {@code null}
    */
    private Node findNode(K key) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }

        Node current = root;
        while (current != NIL) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                return current;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return NIL;
    }

    // this will implement rotateLeft and rotateRight
    private void insertFixup(Node insertedNode) {
        throw new UnsupportedOperationException("insertFixup is implemented by MAX");
    }
}
