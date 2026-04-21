# Red-Black Trees in Java

## Overview
This is our Java implementation of a red-black tree. We really tried to make it correct, readable, and easy
to follow.

Our tree acts like a small ordered map:
- it stores key-value pairs
- it keeps keys in sorted order
- it supports search, insertion, and deletion in `O(log n)` worst-case time

We used a shared black sentinel leaf instead of `null` child references. That
made the balancing code much cleaner and helped us keep the insertion and
deletion fix-up logic consistent.

## Files
- `RedBlackTree.java`: the main red-black tree implementation
- `RedBlackTreeTester.java`: a simple demo runner that shows the tree working in a few clear scenarios

## What The Tree Can Do
The public API is intentionally small. The focus of this project is the
balancing algorithm, not a full ordered-map surface.

- Construction:
  - `RedBlackTree()` for natural ordering
  - `RedBlackTree(Comparator)` for custom ordering
- Size and state:
  - `size`
  - `isEmpty`
  - `clear`
- Lookup:
  - `containsKey`
  - `get`
- Mutation:
  - `put`
  - `remove`
- Inspection:
  - `toString` prints the tree in ascending key order

All balancing and search machinery (rotations, insertion and deletion fix-ups,
sentinel wiring, key validation) is private.

## Ordering
The tree supports two ways to order keys:

1. Natural ordering  
   The keys must implement `Comparable`.

2. Comparator ordering  
   A custom `Comparator` can be passed into the constructor, which means the key
   type does not have to be naturally comparable on its own.

We also made key compatibility checks happen up front, so if someone passes in a
bad key type, the error message is clearer and happens immediately.

## Null Behavior
- `null` keys are not allowed
- `null` values are allowed

That means `get(key)` returning `null` can mean one of two things:
- the key is not in the tree
- the key is in the tree and is explicitly mapped to `null`

So if that distinction matters, `containsKey(key)` should be checked too.

## Red-Black Tree Rules We Enforce
Our implementation preserves the standard red-black tree properties:

1. Every node is either red or black.
2. The root is always black.
3. No red node has a red child.
4. Every path from a node to a descendant sentinel leaf has the same number of black nodes.
5. All sentinel leaves are black.

## Demo / Tester
`RedBlackTreeTester` is intentionally simple. Instead of using random data or a
large assertion framework, it prints a few readable examples:

- empty-tree behavior
- basic insert, update, and lookup
- deletion against a pre-populated tree
- clearing a populated tree
- custom comparator ordering
- invalid key examples

We kept it simple on purpose so someone reading the project can run it and see
what the tree does without digging through a lot of test slop.

## Time Complexity
- Search: `O(log n)`
- Insert: `O(log n)`
- Remove: `O(log n)`
- `toString`: `O(n)`

## Design Notes
- We kept the public API intentionally minimal so the balancing code is the main focus.
- The class is standalone and does not plug into the Java collections framework.
- We added inline explanations in the code around rotations and fix-up cases because that is the hardest part of a red-black tree to read.
