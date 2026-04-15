package redblacktrees;

public final class RedBlackTrees {
    private RedBlackTrees() {
    }

    public static final class RedBlackTree<K extends Comparable<K>, V> {
        private enum NodeColor {
            RED,
            BLACK
        }

        private final class Node {
            K key;
            V value;
            Node left;
            Node right;
            Node parent;
            NodeColor color;

            Node(K key, V value, NodeColor color) {
                this.key = key;
                this.value = value;
                this.color = color;
            }

            boolean isNil() {
                return this == nil;
            }
        }

        private final Node nil;
        private Node root;
        private int size;

        public RedBlackTree() {
            nil = new Node(null, null, NodeColor.BLACK);
            nil.left = nil;
            nil.right = nil;
            nil.parent = nil;

            root = nil;
            size = 0;
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }
}
