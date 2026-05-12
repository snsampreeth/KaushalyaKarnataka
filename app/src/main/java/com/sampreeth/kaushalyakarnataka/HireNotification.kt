package com.sampreeth.kaushalyakarnataka

data class HireNotification(
    val id: String = "",
    val customerEmail: String = "",
    val customerId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)