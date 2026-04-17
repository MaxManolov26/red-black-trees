# Red-Black Trees (Java Implementation)

## Overview
This project implements a Red-Black Tree in Java. A Red-Black Tree is a self-balancing binary search tree that maintains logarithmic height through enforced coloring rules and rotations after insertions.

The implementation supports insertion using key–value pairs and uses a NIL sentinel node instead of null references to simplify tree operations.

---

## Features
- Generic key–value storage (map-like structure)
- Binary Search Tree insertion logic
- Self-balancing using Red-Black Tree rules
- NIL sentinel node (black leaf representation)
- Automatic rebalancing via insert fix-up
- Tree size tracking
- Duplicate key update (overwrites existing value)

---

## Red-Black Tree Properties Enforced
1. Every node is either red or black  
2. The root is always black  
3. No red node has a red child  
4. Every path from a node to its descendant NIL nodes has the same number of black nodes  
5. All NIL leaves are black  

---

## Core Design

### NIL Sentinel
- A single shared node represents all leaf children
- Replaces `null` references
- Always colored black
- Simplifies edge-case handling in insertion and balancing

---

### Node Structure
Each node stores:
- `key`
- `value`
- `color` (red or black)
- `left`, `right`, `parent` pointers

---

## Insertion Process

Insertion occurs in two phases:

### 1. BST Insert
- Traverse tree using key comparisons
- Insert new node at correct leaf position
- If key already exists, update its value

### 2. Fix-Up Phase
After insertion:
- Node is colored red
- `insertFixup` is called to restore Red-Black properties
- Uses:
  - recoloring
  - left rotations
  - right rotations

---

## Time Complexity
- Search: O(log n)
- Insert: O(log n)
- Fix-up: O(log n)

---

## Project Structure
- `RedBlackTree` → main tree implementation
- `Node` → internal node representation
- `insertFixup` → rebalancing logic after insert
- rotation helpers → structural adjustments

---

## Notes
- This implementation prioritizes correctness and clarity over optimization
- NIL sentinel is used instead of null to simplify logic and enforce invariants
- Only insertion is implemented; deletion can be added as an extension

---

## Future Improvements
- Implement deletion with fix-up cases
- Add search and traversal methods
- Add unit tests for rotation and balancing cases
- Make implementation generic (`<K, V>` fully type-safe)

---
