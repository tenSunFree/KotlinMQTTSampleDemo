package com.home.kotlinmqttsampledemo.protocols

interface UIUpdaterInterface {
    fun resetUIWithConnection(status: Boolean)
    fun updateStatusViewWith(status: String)
    fun update(message: String)
}