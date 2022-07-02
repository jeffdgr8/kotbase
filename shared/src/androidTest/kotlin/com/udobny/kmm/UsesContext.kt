package com.udobny.kmm

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
actual abstract class UsesContext {

    @Before
    fun beforeTest() {
        println("before test")
        println("context = ${getApplicationContext<Context>()}")
    }

    companion object {

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            println("before class")
            // doesn't work here
            //println("context = ${getApplicationContext<Context>()}")
        }
    }
}
