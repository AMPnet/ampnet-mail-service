package com.ampnet.mailservice.service.pojo

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.userservice.proto.UserResponse

data class DepositRequestData(
    val user: UserResponse,
    val amount: Long
)

data class OrganizationInvitationRequestData(
    val emails: List<String>,
    val organization: String,
    val senderEmail: String
)

data class WalletActivatedRequestData(
    val walletOwner: String,
    val walletType: WalletType
)
