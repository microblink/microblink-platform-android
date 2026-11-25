package com.microblink.platform.sample.customui

import com.microblink.platform.engine.DateTimeSubtype
import com.microblink.platform.engine.FormField
import com.microblink.platform.engine.FormFieldValue
import com.microblink.platform.engine.NumberSubtype
import com.microblink.platform.engine.UploadFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

/**
 * The value to submit for this field, from what the user typed or picked. Blank text gives an empty
 * value, which Engine reports as missing if the field is required. Text that doesn't parse gives
 * `null`, so the form can ask for [formatHint] before sending anything.
 */
fun FormField.toValue(text: String, file: UploadFile?): FormFieldValue? {
    val typed = text.trim().ifEmpty { null }
    return when (this) {
        is FormField.Text -> FormFieldValue.Text(name, typed)
        is FormField.Bool -> FormFieldValue.Bool(name, typed?.let { it.toBooleanStrictOrNull() ?: return null })
        is FormField.Number -> when (subtype) {
            NumberSubtype.Integer -> FormFieldValue.Integer(name, typed?.let { it.toLongOrNull() ?: return null })
            NumberSubtype.Decimal -> FormFieldValue.Decimal(name, typed?.let { it.toBigDecimalOrNull() ?: return null })
        }
        is FormField.DateTime -> try {
            when (subtype) {
                DateTimeSubtype.Date -> FormFieldValue.Date(name, typed?.let(LocalDate::parse))
                DateTimeSubtype.Time -> FormFieldValue.Time(name, typed?.let(LocalTime::parse))
                DateTimeSubtype.DateTime -> FormFieldValue.DateTime(name, typed?.let(LocalDateTime::parse))
            }
        } catch (e: DateTimeParseException) {
            null
        }
        is FormField.File -> FormFieldValue.File(name, file)
    }
}

/** What to type in this field, such as "a whole number", or null when any text will do. */
fun FormField.formatHint(): String? = when (this) {
    is FormField.Number -> if (subtype == NumberSubtype.Integer) "a whole number" else "a number"
    is FormField.DateTime -> when (subtype) {
        DateTimeSubtype.Date -> "a date like 2000-01-31"
        DateTimeSubtype.Time -> "a time like 14:30"
        DateTimeSubtype.DateTime -> "a date and time like 2000-01-31T14:30"
    }
    else -> null
}
