package com.revethq.iam.scim.filter

sealed class ScimFilter {
    abstract fun matches(attributes: Map<String, Any?>): Boolean
}

data class EqFilter(
    val attribute: String,
    val value: String,
) : ScimFilter() {
    override fun matches(attributes: Map<String, Any?>): Boolean {
        val attrValue = resolveAttribute(attributes, attribute)
        return when (attrValue) {
            is String -> attrValue.equals(value, ignoreCase = true)
            is Boolean -> attrValue.toString().equals(value, ignoreCase = true)
            is Number -> attrValue.toString() == value
            null -> false
            else -> attrValue.toString() == value
        }
    }

    private fun resolveAttribute(
        attributes: Map<String, Any?>,
        path: String,
    ): Any? {
        val parts = path.split(".")
        var current: Any? = attributes

        for (part in parts) {
            current =
                when (current) {
                    is Map<*, *> -> {
                        current[part]
                    }

                    is List<*> -> {
                        // For multi-valued attributes like emails, find first match
                        current
                            .filterIsInstance<Map<*, *>>()
                            .mapNotNull { it[part] }
                            .firstOrNull()
                    }

                    else -> {
                        return null
                    }
                }
        }
        return current
    }
}

data class CoFilter(
    val attribute: String,
    val value: String,
) : ScimFilter() {
    override fun matches(attributes: Map<String, Any?>): Boolean {
        val attrValue = resolveAttribute(attributes, attribute)
        return when (attrValue) {
            is String -> attrValue.contains(value, ignoreCase = true)
            null -> false
            else -> attrValue.toString().contains(value, ignoreCase = true)
        }
    }

    private fun resolveAttribute(
        attributes: Map<String, Any?>,
        path: String,
    ): Any? {
        val parts = path.split(".")
        var current: Any? = attributes

        for (part in parts) {
            current =
                when (current) {
                    is Map<*, *> -> {
                        current[part]
                    }

                    is List<*> -> {
                        current
                            .filterIsInstance<Map<*, *>>()
                            .mapNotNull { it[part] }
                            .firstOrNull()
                    }

                    else -> {
                        return null
                    }
                }
        }
        return current
    }
}

data class SwFilter(
    val attribute: String,
    val value: String,
) : ScimFilter() {
    override fun matches(attributes: Map<String, Any?>): Boolean {
        val attrValue = resolveAttribute(attributes, attribute)
        return when (attrValue) {
            is String -> attrValue.startsWith(value, ignoreCase = true)
            null -> false
            else -> attrValue.toString().startsWith(value, ignoreCase = true)
        }
    }

    private fun resolveAttribute(
        attributes: Map<String, Any?>,
        path: String,
    ): Any? {
        val parts = path.split(".")
        var current: Any? = attributes

        for (part in parts) {
            current =
                when (current) {
                    is Map<*, *> -> {
                        current[part]
                    }

                    is List<*> -> {
                        current
                            .filterIsInstance<Map<*, *>>()
                            .mapNotNull { it[part] }
                            .firstOrNull()
                    }

                    else -> {
                        return null
                    }
                }
        }
        return current
    }
}

data class AndFilter(
    val left: ScimFilter,
    val right: ScimFilter,
) : ScimFilter() {
    override fun matches(attributes: Map<String, Any?>): Boolean = left.matches(attributes) && right.matches(attributes)
}

data class OrFilter(
    val left: ScimFilter,
    val right: ScimFilter,
) : ScimFilter() {
    override fun matches(attributes: Map<String, Any?>): Boolean = left.matches(attributes) || right.matches(attributes)
}
