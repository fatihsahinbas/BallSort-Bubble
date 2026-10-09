package com.example.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BallSortGeneratorTest {

    @Test
    fun `color curve matches design table`() {
        val expected = mapOf(
            1 to 3, 2 to 3, 3 to 4, 5 to 4, 6 to 5, 9 to 5, 10 to 6, 14 to 6, 15 to 7, 20 to 7,
            21 to 8, 27 to 8, 28 to 9, 35 to 9, 36 to 10, 44 to 10, 45 to 11, 54 to 11, 55 to 12, 500 to 12
        )
        expected.forEach { (level, colors) ->
            assertEquals("level $level", colors, BallSortGenerator.getColorsCountForLevel(level))
        }
    }

    @Test
    fun `generation is deterministic per level`() {
        for (level in 1..60) {
            assertEquals(snapshot(BallSortGenerator.generateLevel(level)), snapshot(BallSortGenerator.generateLevel(level)))
        }
    }

    @Test
    fun `every level keeps ball invariants and is not pre-solved`() {
        for (level in 1..300) {
            val tubes = BallSortGenerator.generateLevel(level)
            val colors = BallSortGenerator.getColorsCountForLevel(level)
            assertEquals(colors + 2, tubes.size)
            assertTrue(tubes.all { it.balls.size <= it.capacity })
            val counts = tubes.flatMap { it.balls }.groupingBy { it.color }.eachCount()
            assertEquals(colors, counts.size)
            assertTrue("level $level", counts.values.all { it == 4 })
            assertEquals(tubes.indices.toList(), tubes.map { it.id })
            assertFalse("level $level already solved", BallSortGenerator.isBoardSolved(tubes))
        }
    }

    @Test
    fun `early levels are solvable by forward search`() {
        for (level in 1..10) {
            assertTrue("level $level unsolvable", Solver(BallSortGenerator.generateLevel(level)).solve())
        }
    }

    @Test
    fun `tube move rules`() {
        val red = Ball(1, BallColor.RUBY_RED)
        val blue = Ball(2, BallColor.SKY_BLUE)
        val empty = Tube(id = 0)
        assertTrue(empty.canAccept(red))
        val withRed = empty.pushBall(red)
        assertTrue(withRed.canAccept(red.copy(id = 3)))
        assertFalse(withRed.canAccept(blue))
        val full = Tube(id = 1, balls = List(4) { Ball(it.toLong(), BallColor.RUBY_RED) })
        assertTrue(full.isFull)
        assertTrue(full.isCompleted)
        assertFalse(full.canAccept(red))
        assertTrue(BallSortGenerator.isBoardSolved(listOf(full, empty)))
        assertFalse(BallSortGenerator.isBoardSolved(listOf(withRed)))
    }

    private fun snapshot(tubes: List<Tube>) = tubes.map { t -> t.balls.map { it.color } }

    /** Plain DFS over legal forward moves with a visited set (order-independent of tube position). */
    private class Solver(start: List<Tube>) {
        private val capacity = start.first().capacity
        private val initial = start.map { t -> t.balls.map { it.color.id } }
        private val seen = HashSet<List<List<Int>>>()

        fun solve(): Boolean = dfs(initial)

        private fun dfs(state: List<List<Int>>): Boolean {
            if (state.all { it.isEmpty() || (it.size == capacity && it.distinct().size == 1) }) return true
            if (!seen.add(state.sortedBy { it.toString() })) return false
            for (from in state.indices) {
                val src = state[from]
                if (src.isEmpty()) continue
                for (to in state.indices) {
                    if (to == from) continue
                    val dst = state[to]
                    if (dst.size >= capacity || (dst.isNotEmpty() && dst.last() != src.last())) continue
                    val next = state.toMutableList()
                    next[from] = src.dropLast(1)
                    next[to] = dst + src.last()
                    if (dfs(next)) return true
                }
            }
            return false
        }
    }
}
