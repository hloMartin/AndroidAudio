package com.martin.audiolib

import java.util.concurrent.Executors

object AudioThreadManager {

    private var executor = Executors.newFixedThreadPool(2)

    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

}