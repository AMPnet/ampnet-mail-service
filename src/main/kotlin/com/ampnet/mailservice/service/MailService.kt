package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.userservice.proto.UserResponse

interface MailService {
    fun sendConfirmationMail(email: String, token: String)
    fun sendResetPasswordMail(email: String, token: String)
    fun sendOrganizationInvitationMail(email: List<String>, organizationName: String, senderEmail: String)
    fun sendDepositRequestMail(user: UserResponse, amount: Long)
    fun sendDepositInfoMail(user: UserResponse, minted: Boolean)
    fun sendWithdrawRequestMail(user: UserResponse, amount: Long)
    fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean)
    fun sendNewWalletNotificationMail(walletType: WalletType, coop: String)
    fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType)
}
