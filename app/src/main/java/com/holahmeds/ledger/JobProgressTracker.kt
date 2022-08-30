package com.holahmeds.ledger

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Keeps track of whether there are jobs running or not.
 * This is only for jobs that the user needs to be aware of.
 */
class JobProgressTracker {
    private val progressTrackingLock: Lock = ReentrantLock()
    private var inProgressJobs = 0
    private val isJobInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun addJobInProgress(job: Job) {
        progressTrackingLock.lock()
        try {
            inProgressJobs++
            if (inProgressJobs == 1) {
                isJobInProgress.value = true
            }
        } finally {
            progressTrackingLock.unlock()
        }
        job.invokeOnCompletion {
            progressTrackingLock.lock()
            try {
                inProgressJobs--
                if (inProgressJobs == 0) {
                    isJobInProgress.value = false
                }
            } finally {
                progressTrackingLock.unlock()
            }
        }
    }

    fun isJobInProgress(): Flow<Boolean> = isJobInProgress
}

fun Job.addToTracker(progressTracker: JobProgressTracker) {
    progressTracker.addJobInProgress(this)
}
