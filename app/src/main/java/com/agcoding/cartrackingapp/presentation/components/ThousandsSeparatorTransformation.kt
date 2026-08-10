package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A Compose [VisualTransformation] that displays a numeric input string using
 * European grouping: the integer part is grouped with '.' (thousands) while ','
 * stays the decimal separator. For example the raw value "1234567,89" is shown
 * as "1.234.567,89".
 *
 * Grouping is purely visual — the raw value delivered to onValueChange never
 * contains grouping separators. Feed the field with values produced by
 * `sanitizeDecimalInput` (digits + optional single ',' decimal) so the offset
 * mapping stays correct.
 */
class ThousandsSeparatorTransformation(
    private val groupingSeparator: Char = '.',
    private val decimalSeparator: Char = ','
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val decimalIndex = original.indexOf(decimalSeparator)
        val intPart = if (decimalIndex >= 0) original.substring(0, decimalIndex) else original
        val n = intPart.length
        val firstGroup = if (n == 0) 0 else n % 3

        val sb = StringBuilder()
        // afterOrig[t] = original offset immediately after transformed char at position t.
        val afterOrig = ArrayList<Int>(original.length + original.length / 3 + 1)

        for (i in 0 until n) {
            if (i != 0 && (i - firstGroup) % 3 == 0) {
                sb.append(groupingSeparator)
                afterOrig.add(i)
            }
            sb.append(intPart[i])
            afterOrig.add(i + 1)
        }
        // Decimal separator + fractional digits are appended one-to-one.
        for (j in n until original.length) {
            sb.append(original[j])
            afterOrig.add(j + 1)
        }

        val transformed = sb.toString()

        // original offset -> transformed offset
        val origToTrans = IntArray(original.length + 1)
        origToTrans[0] = 0
        for (t in afterOrig.indices) {
            origToTrans[afterOrig[t]] = t + 1
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                origToTrans[offset.coerceIn(0, original.length)]

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                return if (clamped == 0) 0 else afterOrig[clamped - 1]
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
