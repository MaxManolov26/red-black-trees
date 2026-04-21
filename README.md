# Red-Black Trees (Java Implementation)

## Overview
This is our Java implementation of a red-black tree. We really tried to make it correct, readable, and easy
to follow.

Our tree acts like a standalone ordered map:
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
- Natural ordering with the default constructor
- Custom ordering with the comparator constructor
- Lookup helpers:
  - `containsKey`
  - `containsValue`
  - `get`
  - `getOrDefault`
- Update helpers:
  - `put`
  - `putIfAbsent`
  - `replace`
  - `remove`
  - `remove(key, value)`
  - `putAll`
  - `clear`
- Ordered navigation:
  - `firstKey`, `lastKey`
  - `firstEntry`, `lastEntry`
  - `lowerKey`, `floorKey`, `ceilingKey`, `higherKey`
  - `lowerEntry`, `floorEntry`, `ceilingEntry`, `higherEntry`
- Snapshot views:
  - `keysInOrder`
  - `valuesInKeyOrder`
  - `entriesInOrder`
- Internal correctness check:
  - `validateInvariants`

## Ordering
The tree supports two ways to order keys:

1. Natural ordering  
   The keys must implement `Comparable`.

2. Comparator ordering  
   A custom `Comparator` can be passed into the constructor, which means the key
   type does not have to be naturally comparable on its own.

The `comparator()` method returns the active comparator. If it returns `null`,
that means the tree is using natural ordering.

We also made key compatibility checks happen up front, so if someone passes in a
bad key type, the error message is clearer and happens immediately.

## Null Behavior
- `null` keys are not allowed
- `null` values are allowed

That means `get(key)` returning `null` can mean one of two things:
- the key is not in the tree
- the key is in the tree and is explicitly mapped to `null`

So if that distinction matters, `containsKey(key)` should be checked too.

For boundary methods:
- `firstKey()` and `lastKey()` throw `NoSuchElementException` when the tree is empty
- `firstEntry()` and `lastEntry()` return `null` when the tree is empty

## Red-Black Tree Rules We Enforce
Our implementation preserves the standard red-black tree properties:

1. Every node is either red or black.
2. The root is always black.
3. No red node has a red child.
4. Every path from a node to a descendant sentinel leaf has the same number of black nodes.
5. All sentinel leaves are black.

The `validateInvariants()` method checks these rules, along with parent links,
ordering, and size consistency.

## Demo / Tester
`RedBlackTreeTester` is intentionally simple. Instead of using random data or a
large assertion framework, it prints a few readable examples:

- empty-tree behavior
- basic insert, update, lookup, and remove operations
- navigation helpers
- bulk loading and clearing
- custom comparator ordering
- invalid key examples

We kept it simple on purpose so someone reading the project can run it and see
what the tree does without digging through a lot of test slop.

## Time Complexity
- Search: `O(log n)`
- Insert: `O(log n)`
- Remove: `O(log n)`
- `putIfAbsent`: `O(log n)` with one search traversal
- Navigation queries: `O(log n)`
- Snapshot traversals: `O(n)`

## Design Notes
- We kept the class standalone instead of tying it into the full Java collections framework.
- We used snapshot helpers like `keysInOrder()` and `entriesInOrder()` because they make the tree easy to inspect.
- We added inline explanations in the code around rotations and fix-up cases because that is the hardest part of a red-black tree to read.
