import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
  Fully implemented ordered map backed by a red-black tree.
  This class keeps key-value pairs in sorted order while guaranteeing
  logarithmic-time search, insertion, and deletion in the worst case.
  The implementation uses a single shared black sentinel leaf instead of
  null child references. That design removes a large amount of edge-case
  branching from the balancing logic because every branch always ends at a real
  node object with a well-defined color.
  Ordering can come from either: anatural ordering, used by the zero-argument
  constructor or a caller-supplied Comparator, used by the comparator constructor.
  
  Null keys are always rejected because an ordered tree needs every key
  to participate in a total ordering. Null values are allowed. As with
  Java's java.util.Map, get returning null can mean either a missing key or
  stored null value, so containsKey is provided to disambiguate those cases.
  
  @param <K> key type used to order nodes in the tree
  @param <V> value type stored alongside each key
 */
 
public final class RedBlackTree<K, V> {
    /**
      Colors used by the balancing rules.
      Red-black trees encode structural balance in node colors rather than in an
      explicit height or priority field.
     */
    private enum NodeColor {
        RED,
        BLACK
    }
    /**
      Single node in the tree.
      The same class represents both ordinary data-bearing nodes and the shared
      sentinel leaf. For regular nodes, all fields are meaningful. For the
      sentinel leaf, key and value remain null.
     */
    private final class Node {
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
      Search result used by insertion-style operations.
      A single traversal can either find an existing node or identify the parent
      under which a new node should be attached.
     */
    private final class SearchResult {
        final Node matchingNode;
        final Node parentNode;
        final int comparisonToParent;
        /**
          Stores the outcome of a tree search.
          @param matchingNode matching node, or sentinelLeaf when absent
          @param parentNode last real node visited, or sentinelLeaf when the tree is empty
          @param comparisonToParent last comparison result against the parent node
         */
        SearchResult(Node matchingNode, Node parentNode, int comparisonToParent) {
            this.matchingNode = matchingNode;
            this.parentNode = parentNode;
            this.comparisonToParent = comparisonToParent;
        }
    }
    /**
      Immutable key-value snapshot used by entry-returning helpers.
      This keeps the tree self-contained while still providing a small immutable
      entry representation.
     */
    private static final class SnapshotEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;
        /**
          Stores one immutable key-value pair.
          @param key entry key
          @param value entry value
         */
        SnapshotEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        /**
          Returns the entry key.
          @return stored key
         */
        @Override
        public K getKey() {
            return key;
        }
        /**
          Returns the entry value.
          @return stored value
         */
        @Override
        public V getValue() {
            return value;
        }
        /**
          Immutable entries do not support mutation.
          @param value ignored replacement value
          @return never returns normally
         */
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Snapshot entries are immutable.");
        }
        /**
          Compares this entry to another entry by key and value.
          @param otherObject object being compared
          @return true when both key and value match
         */
        @Override
        public boolean equals(Object otherObject) {
            if (!(otherObject instanceof Map.Entry<?, ?> otherEntry)) {
                return false;
            }
            return Objects.equals(key, otherEntry.getKey())
                && Objects.equals(value, otherEntry.getValue());
        }
        /**
          Returns a hash code consistent with equals.
          @return entry hash code
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }
        /**
          Returns a readable key-value representation.
          @return entry text
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
    /**
      Comparator used to order keys, or null when natural ordering is in use.
      Returning null from comparator matches the established
      Java convention used by java.util.TreeMap.
     */
    private final Comparator<? super K> keyComparator;
    /**
      Shared black leaf used at the end of every branch.
      A sentinel removes many special cases because code never has to ask
      whether a child reference is null before checking its color.
     */
    private final Node sentinelLeaf;
    /**
      Root node of the tree, or sentinelLeaf when the tree is empty.
     */
    private Node rootNode;
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
        sentinelLeaf.leftChild = sentinelLeaf;
        sentinelLeaf.rightChild = sentinelLeaf;
        sentinelLeaf.parentNode = sentinelLeaf;
        rootNode = sentinelLeaf;
        size = 0;
    }
    /**
      Returns the comparator used to order keys.
      A null result means the tree currently uses natural ordering.
      @return active comparator, or null for natural ordering
     */
    public Comparator<? super K> comparator() {
        return keyComparator;
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
      Tests whether any stored entry currently maps to the supplied value.
      This method scans the whole tree because the ordering is based on keys,
      not values.
      @param value value to search for
      @return true when at least one key maps to the supplied value
     */
    public boolean containsValue(Object value) {
        return subtreeContainsValue(rootNode, value);
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
      Returns the value associated with a key, or a default value when absent.
      Unlike a naive "return get(key) unless it is null" implementation,
      this method distinguishes a missing key from a key explicitly mapped to a
      stored null value.
      @param key key to search for
      @param defaultValue fallback value returned only when the key is absent
      @return stored value when present; otherwise defaultValue
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V getOrDefault(Object key, V defaultValue) {
        Node locatedNode = findNode(key);
        return locatedNode == sentinelLeaf ? defaultValue : locatedNode.value;
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
        SearchResult searchResult = searchForNodeOrParent(validatedKey);
        if (searchResult.matchingNode != sentinelLeaf) {
            V previousValue = searchResult.matchingNode.value;
            searchResult.matchingNode.value = value;
            return previousValue;
        }
        Node insertedNode = new Node(
            validatedKey,
            value,
            NodeColor.RED,
            sentinelLeaf,
            sentinelLeaf,
            searchResult.parentNode
        );
        if (searchResult.parentNode == sentinelLeaf) {
            rootNode = insertedNode;
        } else if (searchResult.comparisonToParent < 0) {
            searchResult.parentNode.leftChild = insertedNode;
        } else {
            searchResult.parentNode.rightChild = insertedNode;
        }
        size++;
        restoreAfterInsertion(insertedNode);
        return null;
    }
    /**
      Inserts a key-value pair only when the key is absent or mapped to null.
      This follows the same convention as java.util.Map#putIfAbsent:
      a present key storing null is treated the same as an absent key.
      @param key key to insert
      @param value value to associate with the key when insertion occurs
      @return the previously stored value, or null when no non-null value existed
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V putIfAbsent(K key, V value) {
        K validatedKey = requireCompatibleKey(key);
        SearchResult searchResult = searchForNodeOrParent(validatedKey);
        if (searchResult.matchingNode == sentinelLeaf) {
            Node insertedNode = new Node(
                validatedKey,
                value,
                NodeColor.RED,
                sentinelLeaf,
                sentinelLeaf,
                searchResult.parentNode
            );
            if (searchResult.parentNode == sentinelLeaf) {
                rootNode = insertedNode;
            } else if (searchResult.comparisonToParent < 0) {
                searchResult.parentNode.leftChild = insertedNode;
            } else {
                searchResult.parentNode.rightChild = insertedNode;
            }
            size++;
            restoreAfterInsertion(insertedNode);
            return null;
        }
        V previousValue = searchResult.matchingNode.value;
        if (previousValue == null) {
            searchResult.matchingNode.value = value;
        }
        return previousValue;
    }
    /**
      Inserts every entry from another map.
      Entries are inserted one at a time so the usual key validation and update
      rules still apply.
      @param entries entries to copy into this tree
      @throws NullPointerException when entries is null or contains a null key
      @throws ClassCastException when a key cannot be ordered by this tree
     */
    public void putAll(Map<? extends K, ? extends V> entries) {
        Objects.requireNonNull(entries, "entries");
        for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
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
      Removes a key only when it is currently associated with an expected value.
      The value comparison uses Objects.equals, so
      null values are handled naturally.
      @param key key to remove
      @param expectedValue value that must currently be associated with the key
      @return true when the entry was removed; otherwise false
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public boolean remove(Object key, Object expectedValue) {
        Node nodeToRemove = findNode(key);
        if (nodeToRemove == sentinelLeaf || !Objects.equals(nodeToRemove.value, expectedValue)) {
            return false;
        }
        deleteNode(nodeToRemove);
        size--;
        return true;
    }
    /**
      Replaces the value associated with a key only when the key already exists.
      @param key key whose value should change
      @param newValue replacement value
      @return previous value, or null when the key was absent
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public V replace(K key, V newValue) {
        Node existingNode = findNode(key);
        if (existingNode == sentinelLeaf) {
            return null;
        }
        V previousValue = existingNode.value;
        existingNode.value = newValue;
        return previousValue;
    }
    /**
      Replaces the value for a key only when both the key and the current value match.
      @param key key whose value may change
      @param expectedOldValue value that must currently be stored
      @param newValue replacement value
      @return true when the replacement occurred; otherwise false
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public boolean replace(K key, V expectedOldValue, V newValue) {
        Node existingNode = findNode(key);
        if (existingNode == sentinelLeaf || !Objects.equals(existingNode.value, expectedOldValue)) {
            return false;
        }
        existingNode.value = newValue;
        return true;
    }
    /**
      Returns the smallest key currently stored.
      Use firstEntry when you prefer a nullable boundary lookup.
      @return smallest key currently stored
      @throws java.util.NoSuchElementException when the tree is empty
     */
    public K firstKey() {
        if (rootNode == sentinelLeaf) {
            throw new java.util.NoSuchElementException("The tree is empty.");
        }
        return minimumNode(rootNode).key;
    }
    /**
      Returns the largest key currently stored.
      Use lastEntry when you prefer a nullable boundary lookup.
      @return largest key currently stored
      @throws java.util.NoSuchElementException when the tree is empty
     */
    public K lastKey() {
        if (rootNode == sentinelLeaf) {
            throw new java.util.NoSuchElementException("The tree is empty.");
        }
        return maximumNode(rootNode).key;
    }
    /**
      Returns the smallest entry currently stored.
      The returned entry is an immutable snapshot, not a live view into the tree.
      @return smallest entry, or null when the tree is empty
     */
    public Map.Entry<K, V> firstEntry() {
        return snapshotEntry(rootNode == sentinelLeaf ? sentinelLeaf : minimumNode(rootNode));
    }
    /**
      Returns the largest entry currently stored.
      The returned entry is an immutable snapshot, not a live view into the tree.
      @return largest entry, or null when the tree is empty
     */
    public Map.Entry<K, V> lastEntry() {
        return snapshotEntry(rootNode == sentinelLeaf ? sentinelLeaf : maximumNode(rootNode));
    }
    /**
      Returns the greatest key strictly less than the supplied key.
      @param key reference key
      @return greatest stored key below key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public K lowerKey(K key) {
        Node candidateNode = findLowerNode(key);
        return candidateNode == sentinelLeaf ? null : candidateNode.key;
    }
    /**
      Returns the greatest key less than or equal to the supplied key.
      @param key reference key
      @return greatest stored key at or below key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public K floorKey(K key) {
        Node candidateNode = findFloorNode(key);
        return candidateNode == sentinelLeaf ? null : candidateNode.key;
    }
    /**
      Returns the smallest key greater than or equal to the supplied key.
      @param key reference key
      @return smallest stored key at or above key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public K ceilingKey(K key) {
        Node candidateNode = findCeilingNode(key);
        return candidateNode == sentinelLeaf ? null : candidateNode.key;
    }
    /**
      Returns the smallest key strictly greater than the supplied key.
      @param key reference key
      @return smallest stored key above key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public K higherKey(K key) {
        Node candidateNode = findHigherNode(key);
        return candidateNode == sentinelLeaf ? null : candidateNode.key;
    }
    /**
      Returns the entry strictly below the supplied key.
      @param key reference key
      @return greatest entry below key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public Map.Entry<K, V> lowerEntry(K key) {
        return snapshotEntry(findLowerNode(key));
    }
    /**
      Returns the entry at or below the supplied key.
      @param key reference key
      @return greatest entry at or below key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public Map.Entry<K, V> floorEntry(K key) {
        return snapshotEntry(findFloorNode(key));
    }
    /**
      Returns the entry at or above the supplied key.
      @param key reference key
      @return smallest entry at or above key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public Map.Entry<K, V> ceilingEntry(K key) {
        return snapshotEntry(findCeilingNode(key));
    }
    /**
      Returns the entry strictly above the supplied key.
      @param key reference key
      @return smallest entry above key, or null when none exists
      @throws NullPointerException when key is null
      @throws ClassCastException when the key cannot be ordered by this tree
     */
    public Map.Entry<K, V> higherEntry(K key) {
        return snapshotEntry(findHigherNode(key));
    }
    /**
      Returns every key in sorted order.
      The returned list is a snapshot. Mutating it does not affect the tree.
      @return sorted list of keys
     */
    public List<K> keysInOrder() {
        List<K> orderedKeys = new ArrayList<>();
        collectKeysInOrder(rootNode, orderedKeys);
        return orderedKeys;
    }
    /**
      Returns values in the order produced by an in-order traversal.
      Because an in-order traversal visits keys from smallest to largest, the
      returned values line up with keysInOrder.
      @return values ordered by their keys
     */
    public List<V> valuesInKeyOrder() {
        List<V> orderedValues = new ArrayList<>();
        collectValuesInOrder(rootNode, orderedValues);
        return orderedValues;
    }
    /**
      Returns immutable key-value snapshots in ascending key order.
      Returning immutable snapshots rather than live node views keeps callers
      from mutating tree state behind the balancing code's back.
      @return entry snapshots ordered by key
     */
    public List<Map.Entry<K, V>> entriesInOrder() {
        List<Map.Entry<K, V>> orderedEntries = new ArrayList<>();
        collectEntriesInOrder(rootNode, orderedEntries);
        return orderedEntries;
    }
    /**
      Verifies the major red-black tree invariants.
      This method is mainly intended for testing and debugging. It checks:
      - root color
      - parent links
      - search-tree ordering
      - red-parent violations
      - black-height consistency
      - recorded size correctness
      @return true when the structure is internally consistent
     */
    public boolean validateInvariants() {
        if (sentinelLeaf.color != NodeColor.BLACK) {
            return false;
        }
        if (rootNode == sentinelLeaf) {
            return size == 0;
        }
        if (rootNode.parentNode != sentinelLeaf || rootNode.color != NodeColor.BLACK) {
            return false;
        }
        int expectedBlackHeight = countBlackNodesOnLeftEdge(rootNode);
        return validateSubtree(rootNode, null, null, 0, expectedBlackHeight)
            && countNodes(rootNode) == size;
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
        while (currentNode.parentNode.color == NodeColor.RED) {
            if (currentNode.parentNode == currentNode.parentNode.parentNode.leftChild) {
                Node uncleNode = currentNode.parentNode.parentNode.rightChild;
                // Case 1: the uncle is red, so recoloring fixes the local violation.
                if (uncleNode.color == NodeColor.RED) {
                    currentNode.parentNode.color = NodeColor.BLACK;
                    uncleNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode.parentNode;
                } else {
                    // Case 2: turn an inner bend into a straight line first.
                    if (currentNode == currentNode.parentNode.rightChild) {
                        currentNode = currentNode.parentNode;
                        rotateLeft(currentNode);
                    }
                    // Case 3: recolor and rotate to move the red link upward.
                    currentNode.parentNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    rotateRight(currentNode.parentNode.parentNode);
                }
            } else {
                Node uncleNode = currentNode.parentNode.parentNode.leftChild;
                // Mirror of case 1.
                if (uncleNode.color == NodeColor.RED) {
                    currentNode.parentNode.color = NodeColor.BLACK;
                    uncleNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode.parentNode;
                } else {
                    // Mirror of case 2.
                    if (currentNode == currentNode.parentNode.leftChild) {
                        currentNode = currentNode.parentNode;
                        rotateRight(currentNode);
                    }
                    // Mirror of case 3.
                    currentNode.parentNode.color = NodeColor.BLACK;
                    currentNode.parentNode.parentNode.color = NodeColor.RED;
                    rotateLeft(currentNode.parentNode.parentNode);
                }
            }
        }
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
            fixupStartNode = nodeToRemove.rightChild;
            replaceSubtree(nodeToRemove, nodeToRemove.rightChild);
        } else if (nodeToRemove.rightChild == sentinelLeaf) {
            fixupStartNode = nodeToRemove.leftChild;
            replaceSubtree(nodeToRemove, nodeToRemove.leftChild);
        } else {
            nodeActuallyRemoved = minimumNode(nodeToRemove.rightChild);
            removedNodeColor = nodeActuallyRemoved.color;
            fixupStartNode = nodeActuallyRemoved.rightChild;
            if (nodeActuallyRemoved.parentNode == nodeToRemove) {
                fixupStartNode.parentNode = nodeActuallyRemoved;
            } else {
                replaceSubtree(nodeActuallyRemoved, nodeActuallyRemoved.rightChild);
                nodeActuallyRemoved.rightChild = nodeToRemove.rightChild;
                nodeActuallyRemoved.rightChild.parentNode = nodeActuallyRemoved;
            }
            replaceSubtree(nodeToRemove, nodeActuallyRemoved);
            nodeActuallyRemoved.leftChild = nodeToRemove.leftChild;
            nodeActuallyRemoved.leftChild.parentNode = nodeActuallyRemoved;
            nodeActuallyRemoved.color = nodeToRemove.color;
        }
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
        while (currentNode != rootNode && currentNode.color == NodeColor.BLACK) {
            if (currentNode == currentNode.parentNode.leftChild) {
                Node siblingNode = currentNode.parentNode.rightChild;
                // Case 1: convert a red sibling into a black sibling configuration.
                if (siblingNode.color == NodeColor.RED) {
                    siblingNode.color = NodeColor.BLACK;
                    currentNode.parentNode.color = NodeColor.RED;
                    rotateLeft(currentNode.parentNode);
                    siblingNode = currentNode.parentNode.rightChild;
                }
                // Case 2: both sibling children are black, so push the deficit upward.
                if (
                    siblingNode.leftChild.color == NodeColor.BLACK
                    && siblingNode.rightChild.color == NodeColor.BLACK
                ) {
                    siblingNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode;
                } else {
                    // Case 3: reshape so case 4 can finish the repair.
                    if (siblingNode.rightChild.color == NodeColor.BLACK) {
                        siblingNode.leftChild.color = NodeColor.BLACK;
                        siblingNode.color = NodeColor.RED;
                        rotateRight(siblingNode);
                        siblingNode = currentNode.parentNode.rightChild;
                    }
                    // Case 4: recolor and rotate once to absorb the deficit.
                    siblingNode.color = currentNode.parentNode.color;
                    currentNode.parentNode.color = NodeColor.BLACK;
                    siblingNode.rightChild.color = NodeColor.BLACK;
                    rotateLeft(currentNode.parentNode);
                    currentNode = rootNode;
                }
            } else {
                Node siblingNode = currentNode.parentNode.leftChild;
                // Mirror of case 1.
                if (siblingNode.color == NodeColor.RED) {
                    siblingNode.color = NodeColor.BLACK;
                    currentNode.parentNode.color = NodeColor.RED;
                    rotateRight(currentNode.parentNode);
                    siblingNode = currentNode.parentNode.leftChild;
                }
                // Mirror of case 2.
                if (
                    siblingNode.rightChild.color == NodeColor.BLACK
                    && siblingNode.leftChild.color == NodeColor.BLACK
                ) {
                    siblingNode.color = NodeColor.RED;
                    currentNode = currentNode.parentNode;
                } else {
                    // Mirror of case 3.
                    if (siblingNode.leftChild.color == NodeColor.BLACK) {
                        siblingNode.rightChild.color = NodeColor.BLACK;
                        siblingNode.color = NodeColor.RED;
                        rotateLeft(siblingNode);
                        siblingNode = currentNode.parentNode.leftChild;
                    }
                    // Mirror of case 4.
                    siblingNode.color = currentNode.parentNode.color;
                    currentNode.parentNode.color = NodeColor.BLACK;
                    siblingNode.leftChild.color = NodeColor.BLACK;
                    rotateRight(currentNode.parentNode);
                    currentNode = rootNode;
                }
            }
        }
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
      Searches for a key and records where a new node should attach when absent.
      @param key validated key being searched
      @return matching node when present, or the insertion parent and comparison result when absent
     */
    private SearchResult searchForNodeOrParent(K key) {
        Node parentNode = sentinelLeaf;
        Node currentNode = rootNode;
        int comparisonToParent = 0;
        while (currentNode != sentinelLeaf) {
            parentNode = currentNode;
            comparisonToParent = compareKeys(key, currentNode.key);
            if (comparisonToParent < 0) {
                currentNode = currentNode.leftChild;
            } else if (comparisonToParent > 0) {
                currentNode = currentNode.rightChild;
            } else {
                return new SearchResult(currentNode, parentNode, 0);
            }
        }
        return new SearchResult(sentinelLeaf, parentNode, comparisonToParent);
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
      Finds the greatest node strictly below a key.
      @param key reference key
      @return best candidate node, or sentinelLeaf when none exists
     */
    private Node findLowerNode(K key) {
        K validatedKey = requireCompatibleKey(key);
        Node currentNode = rootNode;
        Node candidateNode = sentinelLeaf;
        while (currentNode != sentinelLeaf) {
            int comparison = compareKeys(validatedKey, currentNode.key);
            if (comparison <= 0) {
                currentNode = currentNode.leftChild;
            } else {
                candidateNode = currentNode;
                currentNode = currentNode.rightChild;
            }
        }
        return candidateNode;
    }
    /**
      Finds the greatest node at or below a key.
      @param key reference key
      @return best candidate node, or sentinelLeaf when none exists
     */
    private Node findFloorNode(K key) {
        K validatedKey = requireCompatibleKey(key);
        Node currentNode = rootNode;
        Node candidateNode = sentinelLeaf;
        while (currentNode != sentinelLeaf) {
            int comparison = compareKeys(validatedKey, currentNode.key);
            if (comparison < 0) {
                currentNode = currentNode.leftChild;
            } else if (comparison > 0) {
                candidateNode = currentNode;
                currentNode = currentNode.rightChild;
            } else {
                return currentNode;
            }
        }
        return candidateNode;
    }
    /**
      Finds the smallest node at or above a key.
      @param key reference key
      @return best candidate node, or sentinelLeaf when none exists
     */
    private Node findCeilingNode(K key) {
        K validatedKey = requireCompatibleKey(key);
        Node currentNode = rootNode;
        Node candidateNode = sentinelLeaf;
        while (currentNode != sentinelLeaf) {
            int comparison = compareKeys(validatedKey, currentNode.key);
            if (comparison < 0) {
                candidateNode = currentNode;
                currentNode = currentNode.leftChild;
            } else if (comparison > 0) {
                currentNode = currentNode.rightChild;
            } else {
                return currentNode;
            }
        }
        return candidateNode;
    }
    /**
      Finds the smallest node strictly above a key.
      @param key reference key
      @return best candidate node, or sentinelLeaf when none exists
     */
    private Node findHigherNode(K key) {
        K validatedKey = requireCompatibleKey(key);
        Node currentNode = rootNode;
        Node candidateNode = sentinelLeaf;
        while (currentNode != sentinelLeaf) {
            int comparison = compareKeys(validatedKey, currentNode.key);
            if (comparison < 0) {
                candidateNode = currentNode;
                currentNode = currentNode.leftChild;
            } else {
                currentNode = currentNode.rightChild;
            }
        }
        return candidateNode;
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
      Returns the largest node reachable from a starting node.
      @param startNode subtree root to search
      @return rightmost node in the subtree
     */
    private Node maximumNode(Node startNode) {
        Node currentNode = startNode;
        while (currentNode.rightChild != sentinelLeaf) {
            currentNode = currentNode.rightChild;
        }
        return currentNode;
    }
    /**
      Traverses the tree in ascending key order and collects keys.
      @param currentNode current subtree root
      @param orderedKeys output list receiving keys
     */
    private void collectKeysInOrder(Node currentNode, List<K> orderedKeys) {
        if (currentNode == sentinelLeaf) {
            return;
        }
        collectKeysInOrder(currentNode.leftChild, orderedKeys);
        orderedKeys.add(currentNode.key);
        collectKeysInOrder(currentNode.rightChild, orderedKeys);
    }
    /**
      Traverses the tree in ascending key order and collects values.
      @param currentNode current subtree root
      @param orderedValues output list receiving values
     */
    private void collectValuesInOrder(Node currentNode, List<V> orderedValues) {
        if (currentNode == sentinelLeaf) {
            return;
        }
        collectValuesInOrder(currentNode.leftChild, orderedValues);
        orderedValues.add(currentNode.value);
        collectValuesInOrder(currentNode.rightChild, orderedValues);
    }
    /**
      Traverses the tree in ascending key order and collects immutable entries.
      @param currentNode current subtree root
      @param orderedEntries output list receiving entries
     */
    private void collectEntriesInOrder(
        Node currentNode,
        List<Map.Entry<K, V>> orderedEntries
    ) {
        if (currentNode == sentinelLeaf) {
            return;
        }
        collectEntriesInOrder(currentNode.leftChild, orderedEntries);
        orderedEntries.add(snapshotEntry(currentNode));
        collectEntriesInOrder(currentNode.rightChild, orderedEntries);
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
      Returns whether a subtree contains a value.
      @param currentNode subtree root being searched
      @param targetValue value being searched for
      @return true when the subtree contains the value
     */
    private boolean subtreeContainsValue(Node currentNode, Object targetValue) {
        if (currentNode == sentinelLeaf) {
            return false;
        }
        return Objects.equals(currentNode.value, targetValue)
            || subtreeContainsValue(currentNode.leftChild, targetValue)
            || subtreeContainsValue(currentNode.rightChild, targetValue);
    }
    /**
      Creates an immutable entry snapshot from a node.
      Returning snapshots prevents callers from mutating internal node state.
      @param node source node
      @return immutable entry snapshot, or null for the sentinel leaf
     */
    private Map.Entry<K, V> snapshotEntry(Node node) {
        if (node == sentinelLeaf) {
            return null;
        }
        return new SnapshotEntry<>(node.key, node.value);
    }
    /**
      Counts black nodes on the leftmost path from a subtree root to a leaf.
      Every root-to-leaf path should have this same count in a valid red-black
      tree, so it gives the validator a target black height.
      @param startNode subtree root
      @return number of black nodes including the sentinel leaf
     */
    private int countBlackNodesOnLeftEdge(Node startNode) {
        int blackNodeCount = 1;
        Node currentNode = startNode;
        while (currentNode != sentinelLeaf) {
            if (currentNode.color == NodeColor.BLACK) {
                blackNodeCount++;
            }
            currentNode = currentNode.leftChild;
        }
        return blackNodeCount;
    }
    /**
      Recursively checks ordering, coloring, and parent-link invariants.
      @param currentNode subtree root being validated
      @param lowerExclusiveBound smallest legal key for this subtree
      @param upperExclusiveBound largest legal key for this subtree
      @param blackNodesSeen number of black nodes encountered so far
      @param expectedBlackHeight required black height for every leaf path
      @return true when the subtree is structurally valid
     */
    private boolean validateSubtree(
        Node currentNode,
        K lowerExclusiveBound,
        K upperExclusiveBound,
        int blackNodesSeen,
        int expectedBlackHeight
    ) {
        if (currentNode == sentinelLeaf) {
            return blackNodesSeen + 1 == expectedBlackHeight;
        }
        if (
            lowerExclusiveBound != null
            && compareKeys(currentNode.key, lowerExclusiveBound) <= 0
        ) {
            return false;
        }
        if (
            upperExclusiveBound != null
            && compareKeys(currentNode.key, upperExclusiveBound) >= 0
        ) {
            return false;
        }
        if (currentNode.leftChild != sentinelLeaf && currentNode.leftChild.parentNode != currentNode) {
            return false;
        }
        if (
            currentNode.rightChild != sentinelLeaf
            && currentNode.rightChild.parentNode != currentNode
        ) {
            return false;
        }
        if (
            currentNode.color == NodeColor.RED
            && (
                currentNode.leftChild.color == NodeColor.RED
                || currentNode.rightChild.color == NodeColor.RED
            )
        ) {
            return false;
        }
        int updatedBlackNodeCount =
            blackNodesSeen + (currentNode.color == NodeColor.BLACK ? 1 : 0);
        return validateSubtree(
                currentNode.leftChild,
                lowerExclusiveBound,
                currentNode.key,
                updatedBlackNodeCount,
                expectedBlackHeight
            )
            && validateSubtree(
                currentNode.rightChild,
                currentNode.key,
                upperExclusiveBound,
                updatedBlackNodeCount,
                expectedBlackHeight
            );
    }
    /**
      Counts real nodes in a subtree.
      This is used by the validator to confirm that the recorded size matches
      the actual structure.
      @param currentNode subtree root
      @return number of real nodes beneath the subtree root
     */
    private int countNodes(Node currentNode) {
        if (currentNode == sentinelLeaf) {
            return 0;
        }
        return 1 + countNodes(currentNode.leftChild) + countNodes(currentNode.rightChild);
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
            } else if (keyComparator != null) {
                compareKeys(key, key);
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
