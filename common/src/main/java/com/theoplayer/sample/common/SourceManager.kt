package com.theoplayer.sample.common

// TODO: use this to manage sources for all sample apps.
class SourceManager private constructor() {

    companion object {
        val instance:SourceManager by lazy {
            SourceManager()
        }
    }

    fun doSomething() = "Doing something"
}