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

        fun <K> hbalTrees(height: Int, value: K): List<Tree<K>> =
                emptyList()
    }

    object End : Tree<Nothing>() {
        override fun isMirrorOf(t: Tree<Any?>): Boolean = t == End
        override fun isSymmetric(): Boolean = true
        override fun isBalanced(): Boolean = true
        override fun height(): Int = -1
        override fun toString() = "."
    }
}

data class BinaryTreeNode<T>(
        val value: T,
        var left: Tree<T> = End,
        var right: Tree<T> = End
) : Tree<T>() {

    override fun isSymmetric(): Boolean = left.isMirrorOf(right)

    override fun isMirrorOf(t: Tree<Any?>): Boolean =
            if (t is BinaryTreeNode) left.isMirrorOf(t.right) && right.isMirrorOf(t.left)
            else false

    override fun height(): Int = max(left.height(), right.height()) + 1

    fun isLeaf(): Boolean = left == End && right == End

    override fun isBalanced(): Boolean =
            abs(left.height() - right.height()) < 1 && left.isBalanced() && right.isBalanced()

    override fun toString(): String {
        return "T($value $left $right)"
    }
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
}
