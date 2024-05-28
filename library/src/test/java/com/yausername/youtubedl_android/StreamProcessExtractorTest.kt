package com.yausername.youtubedl_android

import org.junit.Test
import java.io.InputStream

class MockInputStream(private val size: Int) : InputStream() {
    private var generatedSize = 0

    override fun read(): Int {
        return if (generatedSize < size) {
            generatedSize++
            'A'.code // Return ASCII value of 'A'
        } else {
            -1 // End of stream
        }
    }
}

class StreamProcessExtractorTest {

    @Test
    fun when_crash_occurs() {
        // Create a StringBuffer to store the output
        val buffer = StringBuffer()

        // Define a callback to handle the extracted data
        val callback: ((Float, Long, String) -> Unit)? = { progress, eta, line ->
            println("Progress: $progress%, ETA: $eta seconds, Line: $line")
        }

        // Set the size to a very large value to cause OutOfMemoryError
        val largeSize = Int.MAX_VALUE

        // Create the MockInputStream
        val mockStream = MockInputStream(largeSize)

        // Create and start the StreamProcessExtractor
        val extractor = StreamProcessExtractor(buffer, mockStream, callback)

        // Ensure the extractor is properly started
        if (!extractor.isAlive) {
            extractor.start()
        }

        // Wait for the thread to complete
        extractor.join()
    }
}
