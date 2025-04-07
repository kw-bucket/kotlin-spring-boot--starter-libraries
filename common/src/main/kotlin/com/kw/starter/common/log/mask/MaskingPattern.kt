package com.kw.starter.common.log.mask

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

class MaskingPatternLayout : PatternLayout() {
    private val maskPatterns: MutableSet<MaskPattern> = mutableSetOf()

    fun addMaskPattern(maskPattern: MaskPattern) {
        maskPatterns.add(maskPattern)
    }

    override fun doLayout(event: ILoggingEvent?): String = maskMessage(super.doLayout(event))

    private fun maskMessage(message: String): String {
        if (maskPatterns.isEmpty()) return message

        val sb = StringBuilder(message)
        maskPatterns.forEach { p ->
            try {
                val pattern: Pattern = Pattern.compile(p.fieldPattern, Pattern.CASE_INSENSITIVE)
                val matcher: Matcher = pattern.matcher(sb)

                while (matcher.find()) {
                    val start = matcher.start(2)
                    val end = matcher.end(2)

                    when {
                        p.maskLast > 0 -> {
                            val length = end - start
                            val adder = length - p.maskLast
                            val maskStart = start + (if (adder >= 0) adder else 0)

                            (maskStart until end).forEach { i -> sb.setCharAt(i, p.maskWith.single()) }
                        }

                        else -> {
                            sb.replace(start, end, p.replaceWith)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        return sb.toString()
    }
}

class MaskPattern {
    lateinit var fieldName: String
    var maskLast: Int = 0
    var maskWith: String = "*"
    var replaceWith: String = "*** *** ***"

    val fieldPattern: String
        get() =
            fieldName.split(",").joinToString(separator = "|") { "\"${it.trim()}\"" }.let { f ->
                "($f)\\s*[:=/]\\s*\"(.*?)\""
            }
}
