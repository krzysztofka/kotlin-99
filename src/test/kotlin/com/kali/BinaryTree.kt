package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

sealed class Tree<out T> {
    companion object {

        @JvmStatic
        fun <K> cBalanced(numberOfNodes: Int, value: K): List<Tree<K>> =
                when {
                    numberOfNodes < 1 -> listOf(End)
                    numberOfNodes % 2 == 1 -> {
                        val subtree = cBalanced(numberOfNodes / 2, value)
                        subtree.flatMap { l -> subtree.map { r -> BinaryTreeNode(value, l, r) } }
                    }
                    else -> {
                        val oddSubtree = cBalanced((numberOfNodes - 1) / 2 + 1, value)
                        val evenSubtree = cBalanced((numberOfNodes - 1) / 2, value)
                        oddSubtree.flatMap { o ->
                            evenSubtree.flatMap { e -> listOf(BinaryTreeNode(value, o, e), BinaryTreeNode(value, e, o)) }
                        }
                    }
                }
    }

    open fun isSymmetric(): Boolean = false

    open fun isMirrorOf(t: Tree<Any?>): Boolean = false

    object End : Tree<Nothing>() {
        override fun isMirrorOf(t: Tree<Any?>): Boolean = t == End
        override fun isSymmetric(): Boolean = true
        override fun toString() = "."
    }
}

data class BinaryTreeNode<out T>(
        val value: T,
        val left: Tree<T> = End,
        val right: Tree<T> = End
) : Tree<T>() {

    override fun isSymmetric(): Boolean = left.isMirrorOf(right)

    override fun isMirrorOf(t: Tree<Any?>): Boolean =
            if (t is BinaryTreeNode) left.isMirrorOf(t.right) && right.isMirrorOf(t.left)
            else false

    override fun toString(): String {
        return "T($value $left $right)"
    }
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
}
