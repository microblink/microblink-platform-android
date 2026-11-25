package com.microblink.platform.sample.customui

import com.microblink.platform.engine.DateTimeSubtype
import com.microblink.platform.engine.FormField
import com.microblink.platform.engine.FormFieldValue
import com.microblink.platform.engine.NumberSubtype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FormValuesTest {

    private fun dateTime(subtype: DateTimeSubtype) = FormField.DateTime("when", null, null, true, null, subtype)
    private val integer = FormField.Number("age", null, null, true, null, NumberSubtype.Integer)

    @Test
    fun typedTextParses() {
        assertEquals(FormFieldValue.Date("when", LocalDate.of(2000, 1, 31)), dateTime(DateTimeSubtype.Date).toValue("2000-01-31", null))
        assertEquals(FormFieldValue.Time("when", LocalTime.of(14, 30)), dateTime(DateTimeSubtype.Time).toValue("14:30", null))
        assertEquals(FormFieldValue.Integer("age", 42), integer.toValue("42", null))
    }

    @Test
    fun blankTextIsAnEmptyValue() {
        assertEquals(FormFieldValue.Date("when", null), dateTime(DateTimeSubtype.Date).toValue(" ", null))
        assertEquals(FormFieldValue.Integer("age", null), integer.toValue("", null))
    }

    @Test
    fun unparseableTextIsNull() {
        assertNull(dateTime(DateTimeSubtype.Date).toValue("31.1.2000", null))
        assertNull(integer.toValue("1.5", null))
    }
}
