package ym_cosmetic.pick_perfume_be.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.apache.commons.text.StringEscapeUtils

class XssRequestWrapper(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {
    override fun getParameter(name: String?): String? {
        val value = super.getParameter(name)
        return if (value == null) null else cleanXss(value)
    }

    override fun getParameterValues(name: String?): Array<String?>? {
        val values = super.getParameterValues(name)
        if (values == null) {
            return null
        }

        val length = values.size
        val encodedValues = arrayOfNulls<String>(length)
        for (i in 0..<length) {
            encodedValues[i] = cleanXss(values[i])
        }

        return encodedValues
    }

    private fun cleanXss(value: String?): String {
        return StringEscapeUtils.escapeHtml4(value)
    }
}