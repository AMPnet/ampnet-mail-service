package com.ampnet.mailservice.service.pojo

data class MailConfirmationData(val link: String)
data class ResetPasswordData(val link: String)
data class InvitationData(val organization: String, val link: String)
data class DepositInfo(val minted: Boolean)
data class WithdrawInfo(val burned: Boolean)
data class AmountData(val amount: Long)
