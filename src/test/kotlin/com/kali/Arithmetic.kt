package com.kali

import com.kali.ListTest.Companion.encoding2
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.math.pow

class ArithmeticTest {

    private fun Int.isPrime() =
            when {
                this < 2 -> false
                this == 3 || this == 2 -> true
                this % 3 == 0 || this % 2 == 0 -> false
                else -> generateSequence(5) { it + 6 }
                        .takeWhile { it * it <= this }
                        .find { this % it == 0 || this % (it + 2) == 0 } == null
            }

    private fun primeSeq(): Sequence<Int> =
            listOf(2, 3).asSequence() + generateSequence(5) { it + 6 }
                    .flatMap { listOf(it, it + 2).asSequence() }
                    .filter { it.isPrime() }

    @Test
    fun `test primeSeq`() {
        assertThat(primeSeq().take(10).toList())
                .isEqualTo(listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29))
    }


    @Test
    fun `is prime`() {
        assertThat((1..100).filter { it.isPrime() }).isEqualTo(
                listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97))
    }


    private fun gcd(a: Int, b: Int): Int =
            if (b == 0) a
            else gcd(b, a % b)

    @Test
    fun `gcd`() {
        assertThat(gcd(14, 49)).isEqualTo(7)
        assertThat(gcd(-14, 49)).isEqualTo(7)
        assertThat(gcd(49, 49)).isEqualTo(49)
        assertThat(gcd(49, 2)).isEqualTo(1)
        assertThat(gcd(36, 63)).isEqualTo(9)
    }

    private fun isCoprime(a: Int, b: Int): Boolean = gcd(a, b) == 1

    @Test
    fun `test coprime`() {
        assertThat(isCoprime(14, 49)).isFalse()
        assertThat(isCoprime(15, 49)).isTrue()
    }

    fun Int.totient(): Int {
        var result = this
        var n = this
        var i = 2
        while (i * i <= n) {
            if (n % i == 0) {
                while (n % i == 0) n /= i
                result -= result / i
            }
            if (i == 2) i = 1
            i += 2
        }
        if (n > 1) result -= result / n
        return result
    }

    @Test
    fun `34 test totient`() {
        assertThat((1..21).map { it.totient() })
                .isEqualTo(listOf(1, 1, 2, 2, 4, 2, 6, 4, 6, 4, 10, 4, 12, 6, 8, 8, 16, 6, 18, 8, 12))
    }

    fun Int.primeFactors(): List<Int> {
        fun factorize(x: Int, a: Int): List<Int> =
                when {
                    a * a > x -> listOf(x)
                    x % a == 0 -> listOf(a) + factorize(x / a, a)
                    else -> factorize(x, a + 1)
                }
        return factorize(this, 2)
    }

    @Test
    fun `35 test primeFactors`() {
        assertThat(32124451.primeFactors()).isEqualTo(listOf(32124451))
        assertThat(455.primeFactors()).isEqualTo(listOf(5, 7, 13))

    }

    fun Int.primeFactors2(): List<Pair<Int, Int>> {
        fun factorize(x: Int, a: Int): List<Int> =
                when {
                    a * a > x -> listOf(x)
                    x % a == 0 -> listOf(a) + factorize(x / a, a)
                    else -> factorize(x, a + 1)
                }
        return encoding2(factorize(this, 2))
    }

    @Test
    fun `36 test primeFactors2`() {
        assertThat(32124451.primeFactors2()).isEqualTo(listOf(Pair(32124451, 1)))
        assertThat(455.primeFactors2()).isEqualTo(listOf(Pair(5, 1), Pair(7, 1), Pair(13, 1)))
        assertThat(315.primeFactors2()).isEqualTo(listOf(Pair(3, 2), Pair(5, 1), Pair(7, 1)))
    }

    fun Int.totient2(): Int =
            when (this) {
                0 -> 0
                1 -> 1
                else -> this.primeFactors2()
                        .map { (it.first - 1) * it.first.toDouble().pow(it.second.toDouble() - 1) }
                        .reduce { acc: Double, d: Double -> acc * d }
                        .toInt()
            }


    @Test
    fun `38 test totient2`() {
        assertThat(1.totient2()).isEqualTo(1.totient())
        assertThat(0.totient2()).isEqualTo(0.totient())
        assertThat(10090.totient2()).isEqualTo(10090.totient())
    }


    fun primeRange(from: Int, to: Int): List<Int> =
            primeSeq().dropWhile { it < from }
                    .takeWhile { it <= to }
                    .toList()

    @Test
    fun `39 A list of prime numbers`() {
        assertThat(primeRange(7, 31)).isEqualTo(listOf(7, 11, 13, 17, 19, 23, 29, 31))
    }

    fun Int.goldbach(): Pair<Int, Int>? =
            when {
                this % 2 == 1 || this < 4 -> null
                else -> primeSeq().map { Pair(it, this - it) }
                        .find { it.second.isPrime() }
            }

    @Test
    fun `40 Test goldbach`() {
        assertThat(listOf(0, 1, 2, 3, 4, 5, 28).map { it.goldbach() })
                .isEqualTo(listOf(null, null, null, null, Pair(2, 2), null, Pair(5, 23)))
    }

    fun `printGoldbachList`(from: Int, to: Int) {
        IntRange(from, to)
                .map { it.goldbach() }
                .filterNotNull()
                .forEach { println("${it.first + it.second} = ${it.first} + ${it.second}") }
    }

    @Test
    fun `41 Test goldbach print`() {
        printGoldbachList(9, 20)
    }

    fun Int.goldbach(from: Int): Pair<Int, Int>? =
            when {
                this % 2 == 1 || this < 4 -> null
                else -> primeSeq()
                        .dropWhile { it < from  }
                        .takeWhile { it + it <= this }
                        .map { Pair(it, this - it) }
                        .find { it.second.isPrime() }
            }

    fun `printGoldbachListLimited`(from: Int, to: Int, limit: Int) {
        IntRange(from, to)
                .map { it.goldbach(limit) }
                .filterNotNull()
                .forEach { println("${it.first + it.second} = ${it.first} + ${it.second}") }
    }

    @Test
    fun `43 Test goldbach print with limit`() {
        printGoldbachListLimited(1, 2000, 50)
    }
}