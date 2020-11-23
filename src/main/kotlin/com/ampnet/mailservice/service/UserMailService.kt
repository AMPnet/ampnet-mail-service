package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.userservice.proto.UserResponse

interface UserMailService {
    fun sendConfirmationMail(email: String, token: String, coop: String)
    fun sendResetPasswordMail(email: String, token: String, coop: String)
    fun sendOrganizationInvitationMail(emails: List<String>, organizationName: String, senderEmail: String)
    fun sendDepositRequestMail(user: UserResponse, amount: Long)
    fun sendDepositInfoMail(user: UserResponse, minted: Boolean)
    fun sendWithdrawRequestMail(user: UserResponse, amount: Long)
    fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean)
    fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType, activationData: String)
}
