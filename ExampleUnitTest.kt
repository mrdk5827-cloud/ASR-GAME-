package com.example

import com.example.data.action.ActionParser
import com.example.data.model.FixaAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWhatsAppExtractionHindi() {
        val query = "person A ko wattsapp per jakar ye maine khana kha liye ye massage kar do"
        val action = ActionParser.parseAction(query)
        assertNotNull(action)
        assertTrue(action is FixaAction.WhatsApp)
        val wa = action as FixaAction.WhatsApp
        assertTrue(wa.recipient?.contains("person A", ignoreCase = true) == true)
        assertTrue(wa.messageText.contains("maine khana kha li", ignoreCase = true))
    }

    @Test
    fun testWhatsAppExtractionEnglish() {
        val query = "send whatsapp message to Rahul saying I am on my way"
        val action = ActionParser.parseAction(query)
        assertNotNull(action)
        assertTrue(action is FixaAction.WhatsApp)
    }

    @Test
    fun testMathSolver() {
        val query = "solve 3x + 12 = 45"
        val action = ActionParser.parseAction(query)
        assertNotNull(action)
        assertTrue(action is FixaAction.MathSolution)
        val math = action as FixaAction.MathSolution
        assertEquals("x = 11", math.answer)
    }
}
