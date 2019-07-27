package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max

sealed class Tree<out T> {

    open fun isSymmetric(): Boolean = false

    open fun isBalanced(): Boolean = false

    open fun height(): Int = 0

    open fun isMirrorOf(t: Tree<Any?>): Boolean = false

    open fun leafs(): List<Tree<T>> = emptyList()

    open fun internalNodes(): List<Tree<T>> = emptyList()

    open fun atLevel(level: Int): List<Tree<T>> = emptyList()

    open fun leafCount(): Int = leafs().size

    open fun value(): T? = null

    open fun size(): Int = 0

    companion object {
        @JvmStatic
        fun <K> cBalanced(size: Int, value: K): List<Tree<K>> =
                when {
                    size < 1 -> listOf(End)
                    size % 2 == 1 -> {
                        val subtree = cBalanced(size / 2, value)
                        subtree.flatMap { l -> subtree.map { r -> BinaryTreeNode(value, l, r) } }
                    }
                    else -> {
                        val oddSubtree = cBalanced((size - 1) / 2 + 1, value)
                        val evenSubtree = cBalanced((size - 1) / 2, value)
                        oddSubtree.flatMap { o ->
                            evenSubtree.flatMap { e -> listOf(BinaryTreeNode(value, o, e), BinaryTreeNode(value, e, o)) }
                        }
                    }
                }

        @JvmStatic
        fun <K> symmetricBalancedTrees(size: Int, value: K): List<Tree<K>> =
                cBalanced(size, value).filter { it.isSymmetric() }

        @JvmStatic
        fun <K : Comparable<K>> fromList(list: List<K>): Tree<K> =
                list.fold(End as Tree<K>, { l, r -> l.add(r) })

        @JvmStatic
        fun <K> hbalTreesWithHeight(height: Int, value: K): List<Tree<K>> =
                when {
                    height < 0 -> emptyList()
                    height == 0 -> listOf(BinaryTreeNode(value))
                    height == 1 -> listOf(
                            BinaryTreeNode(value, BinaryTreeNode(value), End),
                            BinaryTreeNode(value, End, BinaryTreeNode(value)),
                            BinaryTreeNode(value, BinaryTreeNode(value), BinaryTreeNode(value)))
                    else -> {
                        val highTrees = hbalTreesWithHeight(height - 1, value)
                        val shortTrees = hbalTreesWithHeight(height - 2, value)
                        highTrees.flatMap { h ->
                            highTrees.map { r -> BinaryTreeNode(value, h, r) } +
                                    shortTrees.flatMap { s ->
                                        listOf(
                                                BinaryTreeNode(value, h, s),
                                                BinaryTreeNode(value, s, h))
                                    }
                        }
                    }
                }

        @JvmStatic
        fun <K> hbalTreesWithNodes(nodes: Int, value: K): List<Tree<K>> {
            val minHeight = minHeightBalancedHeight(nodes)
            val maxHeight = maxHeightBalancedHeight(nodes)
            return (minHeight..maxHeight).flatMap { hbalTreesWithHeight(it, value) }
                    .filter { it.size() == nodes }
        }

        @JvmStatic
        fun <K> completeBinaryTree(nodes: Int, value: K): Tree<K> {
            fun generateTree(address: Int): Tree<K> =
                    if (address > nodes) End
                    else BinaryTreeNode(value, generateTree(2 * address), generateTree(2 * address + 1))

            return generateTree(1)
        }
    }

    object End : Tree<Nothing>() {
        override fun isMirrorOf(t: Tree<Any?>): Boolean = t == End
        override fun isSymmetric(): Boolean = true
        override fun isBalanced(): Boolean = true
        override fun height(): Int = -1
        override fun toString() = "."
    }
}

open class BinaryTreeNode<T>(
        val value: T,
        var left: Tree<T> = End,
        var right: Tree<T> = End
) : Tree<T>() {

    override fun isSymmetric(): Boolean = left.isMirrorOf(right)

    override fun isMirrorOf(t: Tree<Any?>): Boolean =
            if (t is BinaryTreeNode) left.isMirrorOf(t.right) && right.isMirrorOf(t.left)
            else false

    override fun height(): Int = max(left.height(), right.height()) + 1

    override fun isBalanced(): Boolean =
            abs(left.height() - right.height()) <= 1 && left.isBalanced() && right.isBalanced()

    override fun leafs(): List<Tree<T>> =
            if (isLeaf()) listOf(this)
            else left.leafs() + right.leafs()

    override fun internalNodes(): List<Tree<T>> =
            if (isLeaf()) emptyList()
            else listOf(this) + left.internalNodes() + right.internalNodes()

    override fun atLevel(level: Int): List<Tree<T>> =
            when {
                level < 0 -> emptyList()
                level == 0 -> listOf(this)
                else -> left.atLevel(level - 1) + right.atLevel(level - 1)
            }

    override fun value(): T? = value

    override fun size(): Int = left.size() + right.size() + 1

    override fun toString(): String = "T($value $left $right)"

    fun isLeaf(): Boolean = left == End && right == End

    fun layoutTree1(): PositionedBinaryTreeNode<T> {
        fun doLayout(x: Int, y: Int, t: BinaryTreeNode<T>): PositionedBinaryTreeNode<T> {
            val left = if (t.left is BinaryTreeNode<T>) doLayout(x, y + 1, t.left as BinaryTreeNode<T>) else End
            val xLeftAdjusted = t.left.size() + x
            val right = if (t.right is BinaryTreeNode<T>) doLayout(xLeftAdjusted + 1, y + 1, t.right as BinaryTreeNode<T>) else End
            return PositionedBinaryTreeNode(t.value, left, right, xLeftAdjusted, y)
        }
        return doLayout(0, 0, this)
    }

 /*   fun layoutTree2(): PositionedBinaryTreeNode<T> {
        fun doLayout(h: Int, x: Int, y: Int, t: BinaryTreeNode<T>): PositionedBinaryTreeNode<T> {
            if (t.isLeaf()) return PositionedBinaryTreeNode(t.value, t.left, t.right, x, y)
            else {
                
            }
        }
        return doLayout(0, 0, this)
    }*/
}

class PositionedBinaryTreeNode<T>(
        value: T,
        left: Tree<T> = End,
        right: Tree<T> = End,
        var x: Int,
        var y: Int) : BinaryTreeNode<T>(value, left, right) {

    override fun toString(): String = "T[$x, $y]($value $left $right)"
}

fun <T : Comparable<T>> Tree<T>.add(value: T): Tree<T> =
        when {
            this is BinaryTreeNode<T> -> {
                if (this.value < value) right = right.add(value)
                else left = left.add(value)
                this
            }
            else -> BinaryTreeNode(value)
        }

fun minHeightBalancedNodes(height: Int): Int =
        if (height < 0) 0
        else 1 + minHeightBalancedNodes(height - 1) + minHeightBalancedNodes(height - 2)


fun maxHeightBalancedHeight(nodes: Int): Int =
        if (nodes <= 1) 0
        else {
            var height = 0
            var prev = 1
            var prev2 = 0
            do {
                var count = 1 + prev + prev2
                prev2 = prev
                prev = count
                if (count <= nodes) {
                    height++
                }
            } while (count < nodes)
            height
        }

fun minHeightBalancedHeight(nodes: Int): Int =
        if (nodes <= 1) 0
        else minHeightBalancedHeight(nodes / 2 - 1) + 1

class BinaryTreeTest {

    @Test
    fun `55 test c balanced`() {
        assertThat(Tree.cBalanced(4, "x").toString())
                .isEqualTo("[T(x T(x T(x . .) .) T(x . .)), T(x T(x . .) T(x T(x . .) .)), T(x T(x . T(x . .)) T(x . .)), T(x T(x . .) T(x . T(x . .)))]")
    }

    @Test
    fun `56 test symmetric`() {
        assertThat(BinaryTreeNode('a', BinaryTreeNode('b'), BinaryTreeNode('c')).isSymmetric())
                .isTrue()

        assertThat(BinaryTreeNode('a',
                BinaryTreeNode('b', BinaryTreeNode('d'), Tree.End),
                BinaryTreeNode('c')).isSymmetric())
                .isFalse()

        assertThat(BinaryTreeNode('a',
                BinaryTreeNode('b', BinaryTreeNode('d'), Tree.End),
                BinaryTreeNode('c', Tree.End, BinaryTreeNode('f')))
                .isSymmetric())
                .isTrue()
    }

    @Test
    fun `57 test add`() {
        assertThat(Tree.End.add(2))
                .isEqualTo(BinaryTreeNode(2))
        assertThat(BinaryTreeNode(2).add(3))
                .isEqualTo(BinaryTreeNode(2, Tree.End, BinaryTreeNode(3)))
        assertThat(BinaryTreeNode(2, Tree.End, BinaryTreeNode(3)).add(0))
                .isEqualTo(BinaryTreeNode(2, BinaryTreeNode(0), BinaryTreeNode(3)))
    }

    @Test
    fun `57 test fromList and symmetric`() {
        assertThat(Tree.fromList(listOf(3, 2, 5, 7, 1)))
                .isEqualTo(BinaryTreeNode(3, BinaryTreeNode(2, BinaryTreeNode(1), Tree.End), BinaryTreeNode(5, Tree.End, BinaryTreeNode(7))))

        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).isSymmetric()).isTrue()
        assertThat(Tree.fromList(listOf(3, 2, 5, 7, 4)).isSymmetric()).isFalse()
    }

    @Test
    fun `58 Generate-and-test paradigm`() {
        assertThat(Tree.symmetricBalancedTrees(5, "x").toString())
                .isEqualTo("[T(x T(x T(x . .) .) T(x . T(x . .))), T(x T(x . T(x . .)) T(x T(x . .) .))]")
    }

    @Test
    fun `59 test height balanced trees`() {
        assertThat(Tree.hbalTreesWithHeight(-1, "x")).isEmpty()
        assertThat(Tree.hbalTreesWithHeight(0, "x").toString())
                .isEqualTo("[T(x . .)]")
        assertThat(Tree.hbalTreesWithHeight(1, "x").toString())
                .isEqualTo("[T(x T(x . .) .), T(x . T(x . .)), T(x T(x . .) T(x . .))]")
        assertThat(Tree.hbalTreesWithHeight(2, "x").filter { !it.isBalanced() }).isEmpty()
        assertThat(Tree.hbalTreesWithHeight(3, "x").filter { !it.isBalanced() }).isEmpty()
        assertThat(Tree.hbalTreesWithHeight(3, "x").size).isEqualTo(315)
        assertThat(Tree.hbalTreesWithHeight(4, "x").size).isEqualTo(108675)
    }

    @Test
    fun `60 test min height balanced nodes count`() {
        assertThat(minHeightBalancedNodes(-1)).isEqualTo(0)
        assertThat(minHeightBalancedNodes(0)).isEqualTo(1)
        assertThat(minHeightBalancedNodes(1)).isEqualTo(2)
        assertThat(minHeightBalancedNodes(2)).isEqualTo(4)
        assertThat(minHeightBalancedNodes(3)).isEqualTo(7)
        assertThat(minHeightBalancedNodes(4)).isEqualTo(12)
        assertThat(minHeightBalancedNodes(5)).isEqualTo(20)
    }

    @Test
    fun `60 test max height for balanced tree given nodes count`() {
        assertThat(maxHeightBalancedHeight(-1)).isEqualTo(0)
        assertThat(maxHeightBalancedHeight(0)).isEqualTo(0)
        assertThat(maxHeightBalancedHeight(1)).isEqualTo(0)
        assertThat(maxHeightBalancedHeight(2)).isEqualTo(1)
        assertThat(maxHeightBalancedHeight(3)).isEqualTo(1)
        assertThat(maxHeightBalancedHeight(4)).isEqualTo(2)
        assertThat(maxHeightBalancedHeight(5)).isEqualTo(2)
        assertThat(maxHeightBalancedHeight(6)).isEqualTo(2)
        assertThat(maxHeightBalancedHeight(7)).isEqualTo(3)
        assertThat(maxHeightBalancedHeight(8)).isEqualTo(3)
        assertThat(maxHeightBalancedHeight(9)).isEqualTo(3)
        assertThat(maxHeightBalancedHeight(10)).isEqualTo(3)
        assertThat(maxHeightBalancedHeight(11)).isEqualTo(3)
        assertThat(maxHeightBalancedHeight(12)).isEqualTo(4)
    }

    @Test
    fun `60 test hbalTreesWithNodes`() {
        assertThat(Tree.hbalTreesWithNodes(0, 'x')).isEqualTo(emptyList<Char>())
        assertThat(Tree.hbalTreesWithNodes(1, 'x').size).isEqualTo(1)
        assertThat(Tree.hbalTreesWithNodes(2, 'x').size).isEqualTo(2)
        assertThat(Tree.hbalTreesWithNodes(3, 'x').size).isEqualTo(1)
        assertThat(Tree.hbalTreesWithNodes(4, 'x').size).isEqualTo(4)
        assertThat(Tree.hbalTreesWithNodes(5, 'x').size).isEqualTo(6)
        assertThat(Tree.hbalTreesWithNodes(15, 'x').size).isEqualTo(1553)
    }

    @Test
    fun `61 test leafs`() {
        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).leafs())
                .isEqualTo(listOf(BinaryTreeNode(1), BinaryTreeNode(4), BinaryTreeNode(12), BinaryTreeNode(21)))
        assertThat(Tree.fromList(listOf(5)).leafs())
                .isEqualTo(listOf(BinaryTreeNode(5)))
        assertThat(Tree.fromList(emptyList<Nothing>()).leafs()).isEmpty()
    }

    @Test
    fun `61a test leafs COUNT`() {
        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).leafs())
                .isEqualTo(listOf(BinaryTreeNode(1), BinaryTreeNode(4), BinaryTreeNode(12), BinaryTreeNode(21)))

        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).leafCount()).isEqualTo(4)
        assertThat(Tree.fromList(listOf(5)).leafCount()).isEqualTo(1)
    }

    @Test
    fun `62 test internal nodes`() {
        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).internalNodes().map { it.value()!! })
                .isEqualTo(listOf(5, 3, 18))
        assertThat(Tree.fromList(listOf(5)).internalNodes()).isEmpty()
        assertThat(Tree.fromList(emptyList<Nothing>()).internalNodes()).isEmpty()
    }

    @Test
    fun `62b test nodes at level`() {
        assertThat(BinaryTreeNode('a', BinaryTreeNode('b'), BinaryTreeNode('c', BinaryTreeNode('d'), BinaryTreeNode('e'))).atLevel(2).map { it.value()!! })
                .isEqualTo(listOf('d', 'e'))

        assertThat(Tree.fromList(listOf(5, 3, 18, 1, 4, 12, 21)).leafCount()).isEqualTo(4)
        assertThat(Tree.fromList(listOf(5)).leafCount()).isEqualTo(1)
    }

    @Test
    fun `63 test complete binary tree`() {
        assertThat(Tree.completeBinaryTree(6, 'x')).isEqualTo(
                BinaryTreeNode('x', BinaryTreeNode('x', BinaryTreeNode('x'), BinaryTreeNode('x')),
                        BinaryTreeNode('x', BinaryTreeNode('x'), Tree.End)))
    }

    @Test
    fun `64 test layout1`() {
        assertThat(BinaryTreeNode('a',
                BinaryTreeNode('b', Tree.End, BinaryTreeNode('c')),
                BinaryTreeNode('d')).layoutTree1().toString()).isEqualTo(
                "T[2, 0](a T[0, 1](b . T[1, 2](c . .)) T[3, 1](d . .))")
    }
}
