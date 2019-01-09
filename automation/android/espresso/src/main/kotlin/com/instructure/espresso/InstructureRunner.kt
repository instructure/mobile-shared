/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.espresso

import android.app.UiAutomation
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.runner.MonitoringInstrumentation
import com.jakewharton.espresso.OkHttp3IdlingResource
import okhttp3.OkHttpClient

@Suppress("unused")
abstract class InstructureRunner : AndroidJUnitRunner() {

    private lateinit var resource: IdlingResource

    override fun onStart() {
        val client = setupDittoClient() // implemented by deriving class
        resource = OkHttp3IdlingResource.create("okhttp", client)
        IdlingRegistry.getInstance().register(resource)
        setupDialogHandlers()
        super.onStart()
    }

    // Set up the Ditto client and return a reference to the resulting OkHttpClient
    // to use for setting up our OkHttp3IdlingResource
    abstract fun setupDittoClient() : OkHttpClient

    /**
     * Set up dismissal actions for nuisance dialogs (for Android 18 and above)
     */
    private fun setupDialogHandlers() {
        if(android.os.Build.VERSION.SDK_INT >= 18) {
            getUiAutomation().setOnAccessibilityEventListener (
                object : UiAutomation.OnAccessibilityEventListener {
                    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
                        if (p0?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                            val rootNode = p0.source
                            dialogDismissalLogic(rootNode,"has stopped", "close app")
                        }
                    }
                }
            )
            Log.v("dialogs", "Done with setup")
        }
    }

    /** Handling logic for a dialog with a specific message
     * [messageSubString] can be any part of the target dialog message, case-insensitive
     * [buttonSubString] can be any part of the text associated with the desired dialog dismissal
     *                   button to push, case-insensitive
     **/
    private fun dialogDismissalLogic(rootNode : AccessibilityNodeInfo, messageSubString: String, buttonSubString: String) {
        val matchingTextList = rootNode.findAccessibilityNodeInfosByText(messageSubString)
        if (matchingTextList != null && matchingTextList.size > 0) {
            val matchingButtonList = rootNode.findAccessibilityNodeInfosByText(buttonSubString)
            if (matchingButtonList != null && matchingButtonList.size > 0) {
                matchingButtonList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.v("dialogs", "Dismissed " + matchingTextList[0].text)
                matchingButtonList[0].recycle()
            }
            matchingTextList[0].recycle()
        }

    }

    override fun finish(resultCode: Int, results: Bundle) {
        IdlingRegistry.getInstance().unregister(resource)
        super.finish(resultCode, results)
    }

    companion object {

        private const val START_ACTIVITY_TIMEOUT_SECONDS = 120

        init {
            try {
                // private static final int START_ACTIVITY_TIMEOUT_SECONDS = 45;
                // https://android.googlesource.com/platform/frameworks/testing/+/7a552ffc0bce492a7b87755490f3df7490dc357c/support/src/android/support/test/runner/MonitoringInstrumentation.java#78
                val field = MonitoringInstrumentation::class.java.getDeclaredField("START_ACTIVITY_TIMEOUT_SECONDS")
                field.isAccessible = true
                field.set(null, START_ACTIVITY_TIMEOUT_SECONDS)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

    }

}
