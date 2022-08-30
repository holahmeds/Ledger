package com.holahmeds.ledger

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobProgressTrackerTest {
    @Test
    fun testJobInProgress() = runTest {
        val jobProgressTracker = JobProgressTracker()

        val job = launch {
            delay(1000)
        }
        jobProgressTracker.addJobInProgress(job)
        assertTrue(jobProgressTracker.isJobInProgress().first())

        job.join()
        assertFalse(jobProgressTracker.isJobInProgress().first())
    }
}
