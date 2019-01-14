package com.instructure.espresso.page

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

interface InstructureTestingContract {
    @Test fun displaysPageObjects()
    @Before fun launchActivity()
    @Rule fun chain(): TestRule
}
