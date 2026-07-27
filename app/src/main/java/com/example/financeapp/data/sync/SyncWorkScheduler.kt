package com.example.financeapp.data.sync

interface SyncWorkScheduler {

    fun enqueueOneTimeSync()

    fun enqueuePeriodicSync()
}
