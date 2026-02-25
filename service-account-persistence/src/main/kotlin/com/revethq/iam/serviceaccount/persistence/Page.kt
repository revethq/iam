package com.revethq.iam.serviceaccount.persistence

data class Page<T>(
    val items: List<T>,
    val totalCount: Long,
    val startIndex: Int,
    val itemsPerPage: Int,
)
