package com.revethq.iam.user.persistence

data class Page<T>(
    val items: List<T>,
    val totalCount: Long,
    val startIndex: Int,
    val itemsPerPage: Int,
)
