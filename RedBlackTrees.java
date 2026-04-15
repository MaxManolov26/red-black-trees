/**
    Container class for red-black tree code.

    The outer type only names the implementation and prevents accidental instantiation.
*/
public final class RedBlackTrees {
    /**
        Prevents construction of the utility container.
    */
    private RedBlackTrees() {
    }

    /**
        Minimal red-black tree skeleton keyed by comparable values.

        This version only models the tree structure and supports read-only lookup methods.
        Mutation logic is intentionally absent for now, and empty child or parent references are
        represented with {@code null}.

        @param <K> key type used to order nodes in the tree
        @param <V> value type stored alongside each key
    */
    public static final class RedBlackTree<K extends Comparable<K>, V> {
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
            and a color. Missing links are represented with {@code null}.
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
            Root of the tree.

            When the tree is empty, the root is {@code null}.
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
            root = null;
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
            return findNode(key) != null;
        }

        /**
            Returns the value associated with a key.

            @param key key to search for
            @return stored value for the key, or {@code null} when the key is absent
        */
        public V get(K key) {
            Node locatedNode = findNode(key);
            return locatedNode == null ? null : locatedNode.value;
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
                throw new NullPointerException("key");
            }

            Node current = root;
            while (current != null) {
                int comparison = key.compareTo(current.key);
                if (comparison == 0) {
                    return current;
                }
                current = comparison < 0 ? current.left : current.right;
            }
            return null;
        }
    }
}
