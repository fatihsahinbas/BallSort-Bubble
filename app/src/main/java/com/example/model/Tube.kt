package com.example.model

data class Tube(
    val id: Int,
    val capacity: Int = 4,
    val balls: List<Ball> = emptyList()
) {
    val isFull: Boolean get() = balls.size >= capacity
    val isEmpty: Boolean get() = balls.isEmpty()
    val topBall: Ball? get() = balls.lastOrNull()
    val topColor: BallColor? get() = topBall?.color

    val isCompleted: Boolean
        get() = balls.size == capacity && balls.isNotEmpty() && balls.all { it.color == balls[0].color }

    fun canAccept(ball: Ball): Boolean {
        if (isFull) return false
        return isEmpty || topColor == ball.color
    }

    fun pushBall(ball: Ball): Tube {
        require(!isFull) { "Tube is full" }
        return copy(balls = balls + ball)
    }

    fun popBall(): Pair<Tube, Ball> {
        require(!isEmpty) { "Tube is empty" }
        val popped = balls.last()
        return copy(balls = balls.dropLast(1)) to popped
    }
}
