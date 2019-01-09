/**
 * Copyright (C) 2017 Drew Hannay
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule

import android.app.Activity
import android.util.Log
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.runner.MonitoringInstrumentationAccessor

// https://gist.github.com/drewhannay/7fa758847cad8a6dc26f0d1d3cb068ad

/**
 * Subclass of [ActivityTestRule] that cleanly handles finishing multiple activities.
 *
 *
 * The official ActivityTestRule only calls finish() on the initial activity. However, this can cause problems if the
 * test ends in a different activity than which it was started. In this implementation, we call finish() on all
 * Activity classes that are started and wait until they actually finish before proceeding.
 */
open class FinishingActivityTestRule<T : Activity> : IntentsTestRule<T> {

    constructor(activityClass: Class<T>) : super(activityClass) {}

    constructor(activityClass: Class<T>, initialTouchMode: Boolean) : super(activityClass, initialTouchMode) {}

    constructor(activityClass: Class<T>, initialTouchMode: Boolean, launchActivity: Boolean) : super(activityClass, initialTouchMode, launchActivity) {}

    fun finishAllActivities() {
        MonitoringInstrumentationAccessor.finishAllActivities()

        // purposefully don't call super since we've already finished all the activities
        // instead, null out the mActivity field in the parent class using reflection
        try {
            val activityField = ActivityTestRule::class.java.getDeclaredField("activity")
            activityField.isAccessible = true
            activityField.set(this, null)
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "Unable to get field through reflection", e)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Unable to get access field through reflection", e)
        }
    }

    override fun finishActivity() {
        finishAllActivities()
    }

    companion object {
        private val TAG = FinishingActivityTestRule::class.java.simpleName
    }
}
