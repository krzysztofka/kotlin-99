package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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

    open fun toString2() = ""

    open fun bounds(): List<Pair<Int, Int>> = emptyList()

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

        @JvmStatic
        fun fromString2(str: String): Tree<String> {
            fun extractChildren(str: String): Pair<String, String> {
                var open = 0
                for ((index, c) in str.withIndex()) {
                    if (c == '{') open++
                    else if (c == ')') open--
                    else if (c == ',' && open == 0)
                        return Pair(str.substring(0, index), str.substring(index + 1))
                }
                return Pair("", "")
            }

            return when {
                str.isBlank() -> End
                str.endsWith(")") -> {
                    val value = str.substringBefore('(')
                    val interiorStr = str.dropLast(1).substring(value.length + 1)
                    val (left, right) = extractChildren(interiorStr)
                    BinaryTreeNode(value, fromString2(left), fromString2(right))
                }
                else -> BinaryTreeNode(str)
            }
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

fun <T1 : Any, T2 : Any> List<T1>.zipAll(other: List<T2>, emptyValue: T1, otherEmptyValue: T2): List<Pair<T1, T2>> {
    val i1 = this.iterator()
    val i2 = other.iterator()
    return generateSequence {
        if (i1.hasNext() || i2.hasNext()) {
            Pair(if (i1.hasNext()) i1.next() else emptyValue,
                    if (i2.hasNext()) i2.next() else otherEmptyValue)
        } else {
            null
        }
    }.toList()
}

open class BinaryTreeNode<T>(
        val value: T,
        var left: Tree<T> = End,
        var right: Tree<T> = End
) : Tree<T>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryTreeNode<*>

        if (value != other.value) return false
        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

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
    override fun toString2(): String = if (isLeaf()) "$value" else "$value(${left.toString2()},${right.toString2()})"

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

    fun layoutTree2(): PositionedBinaryTreeNode<T> {
        fun doLayout(h: Int, x: Int, y: Int, t: BinaryTreeNode<T>): PositionedBinaryTreeNode<T> {
            val xByHeightAdjust = if (h == 0) 0 else 2.0.pow(h - 1).toInt()
            val xLeftAdjusted = if (x - xByHeightAdjust > 0) x - xByHeightAdjust else 0
            val leftNode = t.left
            val left = if (leftNode is BinaryTreeNode<T>) doLayout(h - 1, xLeftAdjusted, y + 1, leftNode) else End

            val xAdjusted = if (left is PositionedBinaryTreeNode<T>) left.x + xByHeightAdjust else x
            val rightNode = t.right
            val right = if (rightNode is BinaryTreeNode<T>) doLayout(h - 1, xAdjusted + xByHeightAdjust, y + 1, rightNode) else End

            return PositionedBinaryTreeNode(t.value, left, right, xAdjusted, y)
        }
        return doLayout(height(), 0, 0, this)
    }

    override fun bounds(): List<Pair<Int, Int>> {
        val (leftBounds, rightBounds) = Pair(left.bounds(), right.bounds())
        val bounds = when {
            leftBounds.isEmpty() && rightBounds.isEmpty() -> emptyList()
            rightBounds.isEmpty() -> leftBounds.map { Pair(it.first - 1, it.second - 1) }
            leftBounds.isEmpty() -> rightBounds.map { Pair(it.first + 1, it.second + 1) }
            else -> {
                val shift = leftBounds
                        .zip(rightBounds)
                        .map { (it.first.second - it.second.first) / 2 + 1 }
                        .reduce { l, r -> max(l, r) }

                val empty = Pair(null, null)

                leftBounds.zipAll(rightBounds, empty, empty)
                        .map { (left, right) ->
                            when {
                                right == empty -> Pair((left.first!! - shift), (left.second!! - shift))
                                left == empty -> Pair((right.first!! + shift), (right.second!! + shift))
                                else -> Pair((left.first!! - shift), (right.second!! + shift))
                            }
                        }
            }
        }
        return listOf(Pair(0, 0)) + bounds
    }

    fun compactLayout(): PositionedBinaryTreeNode<T> {
        fun doLayout(x: Int, y: Int, t: BinaryTreeNode<T>): PositionedBinaryTreeNode<T> =
                if (t.isLeaf()) PositionedBinaryTreeNode(t.value, End, End, x, y)
                else {
                    val (bLeft, bRight) = t.bounds()[1]
                    val leftNode = t.left
                    val left = if (leftNode is BinaryTreeNode<T>) doLayout(x + bLeft, y + 1, leftNode) else End

                    val rightNode = t.right
                    val right = if (rightNode is BinaryTreeNode<T>) doLayout(x + bRight, y + 1, rightNode) else End
                    PositionedBinaryTreeNode(t.value, left, right, x, y)
                }

        val x = this.bounds()
                .map { it.first }
                .reduce { l, r -> min(l, r) }

        return doLayout(abs(x), 0, this)
    }
}

class PositionedBinaryTreeNode<T>(
        value: T,
        left: Tree<T> = End,
        right: Tree<T> = End,
        var x: Int,
        var y: Int) : BinaryTreeNode<T>(value, left, right) {

    override fun toString(): String = "T[$x, $y]($value $left $right)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PositionedBinaryTreeNode<*>

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        return result
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
                val count = 1 + prev + prev2
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

    @Test
    fun `65 test layout2`() {
        assertThat(BinaryTreeNode('a',
                BinaryTreeNode('b', Tree.End, BinaryTreeNode('c')),
                BinaryTreeNode('d')).layoutTree2().toString()).isEqualTo(
                "T[2, 0](a T[0, 1](b . T[1, 2](c . .)) T[4, 1](d . .))")

        assertThat((Tree.fromList(listOf('n', 'k', 'm', 'c', 'a', 'e', 'd', 'g', 'u', 'p', 'q')) as BinaryTreeNode<Char>).layoutTree2().toString())
                .isEqualTo("T[14, 0](n T[6, 1](k T[2, 2](c T[0, 3](a . .) T[4, 3](e T[3, 4](d . .) T[5, 4](g . .))) T[10, 2](m . .)) T[22, 1](u T[18, 2](p . T[20, 3](q . .)) .))")
    }

    @Test
    fun `66 test compact layout`() {
        val testTree = BinaryTreeNode("a", BinaryTreeNode("b", Tree.End, BinaryTreeNode("c")), BinaryTreeNode("d"))
        assertThat(testTree.compactLayout().toString())
                .isEqualTo("T[1, 0](a T[0, 1](b . T[1, 2](c . .)) T[2, 1](d . .))")

        assertThat((Tree.fromList(listOf('n', 'k', 'm', 'c', 'a', 'e', 'd', 'g', 'u', 'p', 'q')) as BinaryTreeNode<Char>).compactLayout().toString())
                .isEqualTo("T[4, 0](n T[2, 1](k T[1, 2](c T[0, 3](a . .) T[2, 3](e T[1, 4](d . .) T[3, 4](g . .))) T[3, 2](m . .)) T[6, 1](u T[5, 2](p . T[6, 3](q . .)) .))")
    }

    @Test
    fun `67 test toString2`() {
        val testData = BinaryTreeNode('a',
                BinaryTreeNode('b', BinaryTreeNode('d'), BinaryTreeNode('e')),
                BinaryTreeNode('c', Tree.End, BinaryTreeNode('f', BinaryTreeNode('g'), Tree.End)))

        assertThat(testData.toString2()).isEqualTo("a(b(d,e),c(,f(g,)))")
    }

    @Test
    fun `67 test fromString2`() {
        val testData = BinaryTreeNode("a",
                BinaryTreeNode("b", BinaryTreeNode("d"), BinaryTreeNode("e")),
                BinaryTreeNode("c", Tree.End, BinaryTreeNode("f", BinaryTreeNode("g"), Tree.End)))

        assertThat(Tree.fromString2("a(b(d,e),c(,f(g,)))")).isEqualTo(testData)
    }

}
