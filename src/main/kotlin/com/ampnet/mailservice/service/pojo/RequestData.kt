package com.ampnet.mailservice.service.pojo

import com.ampnet.userservice.proto.UserResponse

data class DepositRequestData(
    val user: UserResponse,
    val amount: Long
)

data class OrganizationInvitationData(
    val emails: List<String>,
    val organization: String,
    val senderEmail: String
)