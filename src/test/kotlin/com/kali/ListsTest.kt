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
            list.flatMap { (first, second) -> List(first) { second } }


    @Test
    fun `12 decode of a list`() {
        val testData = listOf(Pair(4, 'a'), Pair(1, 'b'), Pair(2, 'c'), Pair(2, 'a'), Pair(1, 'd'), Pair(4, 'e'))
        assertThat(decode(testData)).isEqualTo("aaaabccaadeeee".toList())
    }

    private fun <T> multiply(list: List<T>, k: Int): List<T> = list.flatMap { v -> List(k) { v } }

    @Test
    fun `14 duplicate elements of a list`() {
        val testData = "abccd".toList()
        assertThat(multiply(testData, 2)).isEqualTo("aabbccccdd".toList())
    }

    @Test
    fun `15 replicate elements of a list`() {
        val testData = "abccd".toList()
        val k = 3
        assertThat(multiply(testData, k)).isEqualTo("aaabbbccccccddd".toList())
    }

    private fun <T> dropN(list: List<T>, n: Int): List<T> =
            list.filterIndexed { index, _ -> (index + 1) % n > 0 }

    @Test
    fun `16 drop every N`() {
        val testData = "abcdefghik".toList()
        val n = 3
        assertThat(dropN(testData, n)).isEqualTo("abdeghk".toList())
    }

    private fun <T> split(list: List<T>, k: Int): Pair<List<T>, List<T>> = Pair(list.take(k), list.drop(k))

    @Test
    fun `17 split at k`() {
        val testData = "abcdefghik".toList()
        val k = 3
        assertThat(split(testData, k)).isEqualTo(Pair("abc".toList(), "defghik".toList()))
    }

    @Test
    fun `17 split at k index higher`() {
        val testData = "a".toList()
        val k = 4
        assertThat(split(testData, k)).isEqualTo(Pair(listOf('a'), emptyList<Char>()))
    }

    @Test
    fun `18 slice`() {
        val testData = "abcdefghik)".toList()
        val i = 3
        val k = 7
        assertThat(testData.slice(IntRange(i - 1, k - 1))).isEqualTo("cdefg".toList())
    }

    private fun <T> rotate(list: List<T>, n: Int): List<T> {
        if (list.isEmpty()) return emptyList()
        var k = n % list.size
        if (k < 0) k += list.size
        val right = list.take(k)
        return list.takeLast(list.size - k) + right
    }

    @Test
    fun `19 rotate N left`() {
        val testData = "abcdefgh".toList()

        assertThat(rotate(testData, 3)).isEqualTo("defghabc".toList())
        assertThat(rotate(testData, 11)).isEqualTo("defghabc".toList())
        assertThat(rotate(testData, -2)).isEqualTo("ghabcdef".toList())
        assertThat(rotate(testData, -10)).isEqualTo("ghabcdef".toList())
        assertThat(rotate(testData, 0)).isEqualTo("abcdefgh".toList())
        assertThat(rotate(testData, 8)).isEqualTo("abcdefgh".toList())
        assertThat(rotate(emptyList<Char>(), -2)).isEqualTo(emptyList<Char>())
    }
}

