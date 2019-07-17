package com.kali

import org.junit.Test
import kotlin.sequences.generateSequence
import org.assertj.core.api.Assertions.assertThat as assertThat

class ArithmeticTest {

    private fun Int.isPrime() =
            when {
                this < 2 -> false
                this == 3 || this == 2 -> true
                this % 3 == 0 || this % 2 == 0 -> false
                else -> generateSequence(5) { it + 6 }
                        .takeWhile { it * it <= this }
                        .find { this % it == 0 || this % (it + 2)  == 0 } == null
            }

    @Test
    fun `is prime`() {
        assertThat((1..100).filter { it.isPrime() }).isEqualTo(
                listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97))
    }


    private fun gcd(a: Int, b: Int): Int =
            if (b == 0) a
            else gcd (b, a % b)

    @Test
    fun `gcd`() {
        assertThat(gcd(14, 49)).isEqualTo(7)
        assertThat(gcd(-14, 49)).isEqualTo(7)
        assertThat(gcd(49, 49)).isEqualTo(49)
        assertThat(gcd(49, 2)).isEqualTo(1)
        assertThat(gcd(36, 63)).isEqualTo(9)
    }
}