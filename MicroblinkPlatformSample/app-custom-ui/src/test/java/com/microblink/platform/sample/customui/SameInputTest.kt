package com.microblink.platform.sample.customui

import com.microblink.platform.engine.InputRequest
import com.microblink.platform.engine.InteractiveStepType
import com.microblink.platform.engine.Step
import com.microblink.platform.engine.StepAction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SameInputTest {

    private fun ask(stepId: Int) = StepAction.InputStep(
        Step(stepId, InteractiveStepType.Form, null, null, false, true, 0, false, 0),
        InputRequest.Form(emptyList(), isKioskModeEnabled = false),
    )

    @Test
    fun theSameStepAskedAgainKeepsItsInput() {
        assertTrue(ask(1).isSameInputAs(ask(1)))
    }

    @Test
    fun anotherStepWithTheSameFieldsStartsEmpty() {
        assertFalse(ask(2).isSameInputAs(ask(1)))
    }
}
