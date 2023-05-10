package com.example.handtracker

class HandPosition {
    private val buffer = CircularBuffer(10)

    object WritingValues {
        // add 50% threshold
        private const val increase = 1.5
        private const val decrease = 0.5
        object X {
            const val Min = -2.5 * increase
            const val Max = 1.1 * increase
        }
        object Y {
            const val Min = -11.0 * increase
            const val Max = -6.5 * decrease
        }
        object Z {
            const val Min = 3.5 * decrease
            const val Max = 7.9 * increase
        }
    }
    fun isWriting(x: Double, y: Double, z: Double): Boolean {
        buffer.addData(x, y, z)
        val pastX = buffer.getXValues().average()
        val pastY = buffer.getYValues().average()
        val pastZ = buffer.getZValues().average()

        return pastX in WritingValues.X.Min..WritingValues.X.Max &&
                pastY in WritingValues.Y.Min..WritingValues.Y.Max &&
                pastZ in WritingValues.Z.Min..WritingValues.Z.Max
    }

    class CircularBuffer(val capacity: Int) {
        private val xValues = DoubleArray(capacity)
        private val yValues = DoubleArray(capacity)
        private val zValues = DoubleArray(capacity)
        private var index = 0

        fun addData(x: Double, y: Double, z: Double) {
            xValues[index] = x
            yValues[index] = y
            zValues[index] = z

            index = (index + 1) % capacity
        }

        fun getXValues(): DoubleArray = xValues.copyOf()
        fun getYValues(): DoubleArray = yValues.copyOf()
        fun getZValues(): DoubleArray = zValues.copyOf()
    }
}