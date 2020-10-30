package com.ampnet.mailservice.service.pojo

data class MailConfirmationData(val link: String)
data class ResetPasswordData(val link: String)
data class InvitationData(val organization: String, val link: String)
data class DepositInfo(val minted: Boolean)
data class WithdrawInfo(val burned: Boolean)
data class AmountData(val amount: String)
data class NewWalletData(val link: String)
data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
data class WalletActivatedData(
    val link: String,
    val organizationName: String? = null,
    val projectName: String? = null
)
