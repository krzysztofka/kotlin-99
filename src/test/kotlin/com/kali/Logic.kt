package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*
import java.util.function.BiFunction
import kotlin.jvm.functions.FunctionN


class LogicTest {

    private infix fun Boolean.nand(other: Boolean): Boolean = !(this and other)

    @Test
    fun `test and`() {
        assertThat(true.and(true)).isTrue()
        assertThat(true.and(false)).isFalse()
        assertThat(false.and(true)).isFalse()
        assertThat(false.and(false)).isFalse()
    }

    @Test
    fun `test xor`() {
        assertThat(true.xor(true)).isFalse()
        assertThat(true.xor(false)).isTrue()
        assertThat(false.xor(true)).isTrue()
        assertThat(false.xor(false)).isFalse()
    }


    fun allBoolPermutations(size: Int): List<List<Boolean>> {
        fun boolLists(lists: List<List<Boolean>>, n: Int): List<List<Boolean>> =
                if (n < 1) lists
                else boolLists(lists.flatMap { listOf(it + true, it + false) }, n - 1)

        return boolLists(listOf(emptyList()), size)
    }

    @Test
    fun `test allBoolPermutations`() {
        assertThat(allBoolPermutations(3)).containsExactlyInAnyOrderElementsOf(listOf(
                listOf(false, false, false), listOf(false, false, true), listOf(false, true, false),
                listOf(false, true, true), listOf(true, false, false),
                listOf(true, true, false), listOf(true, true, true), listOf(true, false, true)))
    }

    fun table2(expression: BiFunction<Boolean, Boolean, Boolean>) {
        println("A     B     result")
        allBoolPermutations(2)
                .map { Pair(it.first(), it[1]) }
                .forEach { (a, b) -> println("$a  $b  ${expression.apply(a, b)}") }
    }

    @Test
    fun `test table2`() {
        table2(BiFunction { a, b -> (a or b) and (a nand b) })
    }

    fun tableN(expression: FunctionN<Boolean>) {
        allBoolPermutations(expression.arity)
                .forEach { println("$it  ${expression.invoke(it)}") }
    }

    fun grayCode(digits: Int): List<List<Int>> =
            when (digits) {
                0 -> listOf(emptyList())
                1 -> listOf(listOf(0), listOf(1))
                else -> grayCode(digits - 1).map { listOf(0) + it } +
                        grayCode(digits - 1).reversed().map { listOf(1) + it }
            }


    @Test
    fun `49 test grayCode`() {
        assertThat(grayCode(3)).isEqualTo(listOf(listOf(0, 0, 0), listOf(0, 0, 1), listOf(0, 1, 1),
                listOf(0, 1, 0), listOf(1, 1, 0), listOf(1, 1, 1), listOf(1, 0, 1), listOf(1, 0, 0)))
    }


    class HuffmanTreeNode<T>(var value: Int, var left: HuffmanTreeNode<T>?, var right: HuffmanTreeNode<T>?, var elem: T?)

    fun listToInt(list: List<Int>): Int = list.map { it.toString() }
            .fold("0", { l, r -> l + r })
            .toInt()

    fun <T> huffmanCode(node: HuffmanTreeNode<T>, list: List<Int>): List<Pair<T, Int>> =
            if (node.elem != null) listOf(Pair(node.elem!!, listToInt(list)))
            else huffmanCode(node.left!!, list + listOf(0)) + huffmanCode(node.right!!, list + listOf(1))

    fun <T> huffmanTree(symbolsWithFrequencies: List<Pair<T, Int>>): HuffmanTreeNode<T> {
        val pq = PriorityQueue(Comparator<HuffmanTreeNode<T>> { o1, o2 -> o1.value.compareTo(o2.value) })
        pq.addAll(symbolsWithFrequencies
                .sortedBy { it.second }
                .map { HuffmanTreeNode(it.second, null, null, it.first) })

        while (pq.size > 1) {
            val left = pq.poll()
            val right = pq.poll()
            pq.add(HuffmanTreeNode(left.value + right.value, left, right, null))
        }

        return pq.poll()
    }

    fun <T> huffman(symbolsWithFrequencies: List<Pair<T, Int>>): List<Pair<T, Int>> =
            if (symbolsWithFrequencies.isEmpty()) emptyList()
            else huffmanCode(huffmanTree(symbolsWithFrequencies), emptyList())

    @Test
    fun `50 test huffman code`() {
        assertThat(huffman(listOf(Pair("a", 45), Pair("b", 13), Pair("c", 12), Pair("d", 16), Pair("e", 9), Pair("f", 5))))
                .isEqualTo(listOf(Pair("a", 0), Pair("c", 100), Pair("b", 101), Pair("f", 1100), Pair("e", 1101), Pair("d", 111)))

        assertThat(huffman(listOf(Pair('a', 25), Pair('b', 21), Pair('c', 18), Pair('d', 14), Pair('e', 9), Pair('f', 7), Pair('g', 6))))
                .isEqualTo(listOf(Pair('b', 0), Pair('e', 10), Pair('g', 110), Pair('f', 111), Pair('a', 10), Pair('d', 110), Pair('c', 111)))
    }
}