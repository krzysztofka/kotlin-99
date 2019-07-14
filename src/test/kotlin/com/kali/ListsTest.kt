package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ListTest {
    private fun <T> kthElement(list: List<T>, index: Int): T = list[index]
    private fun <T> isPalindrome(list: List<T>): Boolean = list == list.reversed()

    @Test
    fun `1 Find the last element of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).last()).isEqualTo(8)
    }

    @Test
    fun `2 Find the last but one element of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).takeLast(2).first()).isEqualTo(5)
    }

    @Test
    fun `3 Reverse a list`() {
        assertThat(listOf(4, 1, 2, 1, 3, 5, 8).reversed()).isEqualTo(listOf(8, 5, 3, 1, 2, 1, 4))
    }

    @Test
    fun `4 Find the K'th element of a list`() {
        assertThat(kthElement(listOf(1, 1, 2, 3, 5, 8), 2)).isEqualTo(2)
    }

    @Test
    fun `5 Find the number of elements of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).size).isEqualTo(6)
    }

    @Test
    fun `6 Is not palindrome `() {
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 5, 8))).isFalse()
    }

    @Test
    fun `6 Is palindrome`() {
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 4, 4, 3, 2, 1, 1))).isTrue()
        assertThat(isPalindrome(listOf(4))).isTrue()
        assertThat(isPalindrome(listOf<Int>())).isTrue()
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 2, 1, 1))).isTrue()
    }

    fun flatList(list: List<Any>): List<Any> {
        return list.flatMap {
            when (it) {
                is List<*> -> flatList(it as List<Any>)
                else -> listOf(it)
            }
        }
    }

    @Test
    fun `7 Flat list`() {
        val testData = listOf(3, listOf(listOf<Any>(9), listOf(4, 2), 5), listOf(4, 7), listOf<Any>())
        assertThat(flatList(testData)).isEqualTo(listOf(3, 9, 4, 2, 5, 4, 7))
    }

    private fun <T> eliminateConsecutiveDuplicates(list: List<T>): List<T> {
        return list.fold(emptyList()) { left, right ->
            when (left.lastOrNull()) {
                right -> left
                else -> left + right
            }
        }
    }

    @Test
    fun `8 Eliminate consecutive duplicates of list elements`() {
        val testData = "bbaabdeeeegdddoaacwuu".toList()
        assertThat(eliminateConsecutiveDuplicates(testData)).isEqualTo("babdegdoacwu".toList())
    }

    private fun <T> pack(list: List<T>): List<List<T>> =
            if (list.isEmpty()) {
                listOf()
            } else {
                val first = list.first()
                val part = list.takeWhile { it == first }
                listOf(part) + pack(list.drop(part.size))
            }

    @Test
    fun `9 Pack consecutive duplicates of list elements into sublists If a list contains repeated elements they should be placed in separate sublists`() {
        val testData = "aaaabccaadeeee".toList()
        assertThat(pack(testData))
                .isEqualTo(listOf("aaaa".toList(), listOf('b'), "cc".toList(), "aa".toList(), listOf('d'), "eeee".toList()))
    }

    private fun <T> encoding(list: List<T>): List<Pair<Int, T>> {
        return pack(list).map {
            Pair(it.size, it.first())
        }
    }

    @Test
    fun `10 Run-length encoding of a list`() {
        val testData = "aaaabccaadeeee".toList()
        assertThat(encoding(testData))
                .isEqualTo(listOf(Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e')))
    }

    private fun <T> encoding2(list: List<T>): List<Any?> {
        return pack(list).map {
            when (it.size) {
                1 -> it.first()
                else -> Pair(it.size, it.first())
            }
        }
    }

    @Test
    fun `11 Run-length encoding of a list 2`() {
        val testData = "aaaabccaadeeee".toList()
        assertThat(encoding2(testData))
                .isEqualTo(listOf(Pair(4, 'a'), 'b', Pair(2, 'c'), Pair(2, 'a'), 'd', Pair(4, 'e')))
    }

    private fun <T> decode(list: List<Pair<Int, T>>): List<T> =
            list.flatMap { p -> List(p.first) { p.second } }


    @Test
    fun `12 decode of a list`() {
        val testData = listOf(Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e'))
        assertThat(decode(testData)).isEqualTo("aaaabccaadeeee".toList())
    }
}

