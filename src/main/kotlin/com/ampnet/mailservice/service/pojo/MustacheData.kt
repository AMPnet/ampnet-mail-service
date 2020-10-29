package com.ampnet.mailservice.service.pojo

import com.ampnet.userservice.proto.UserResponse

interface MailText {

}

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
data class DepositRequestData(
    val user: UserResponse,
    val amount: Long
): MailText

class MailTextFactory {

    fun createDepositRequestData(
        user: UserResponse,
        amount: Long
    ): DepositRequestData {
        return DepositRequestData(user, amount)
    }
}
