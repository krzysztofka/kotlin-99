package com.kali

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ListTest {
    fun <T> kthElement(list: List<T>, index : Int) : T = list[index]
    fun <T> isPalindrome(list: List<T>) : Boolean = list == list.reversed()


    @Test fun `Find the last element of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).last()).isEqualTo(8)
    }

    @Test fun `Find the last but one element of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).takeLast(2).first()).isEqualTo(5)
    }

    @Test fun `Reverse a list`() {
        assertThat(listOf(4, 1, 2, 1, 3, 5, 8).reversed()).isEqualTo(listOf(8, 5, 3, 1, 2, 1, 4))
    }

    @Test fun `Find the K'th element of a list`() {
        assertThat(kthElement(listOf(1, 1, 2, 3, 5, 8), 2)).isEqualTo(2)
    }

    @Test fun `Find the number of elements of a list`() {
        assertThat(listOf(1, 1, 2, 3, 5, 8).size).isEqualTo(6)
    }

    @Test fun `Is not palindrome `() {
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 5, 8))).isFalse()
    }

    @Test fun `Is palindrome`() {
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 4, 4, 3, 2, 1, 1))).isTrue()
        assertThat(isPalindrome(listOf(4))).isTrue()
        assertThat(isPalindrome(listOf<Int>())).isTrue()
        assertThat(isPalindrome(listOf(1, 1, 2, 3, 2, 1, 1))).isTrue()
    }
}

