package com.ampnet.mailservice.service

import com.ampnet.mailservice.amqp.blockchainservice.SuccessfullyInvestedMessage
import com.ampnet.mailservice.amqp.projectservice.MailOrgInvitationMessage
import com.ampnet.mailservice.amqp.userservice.MailConfirmationMessage
import com.ampnet.mailservice.amqp.userservice.MailResetPasswordMessage
import com.ampnet.mailservice.amqp.walletservice.WalletTypeAmqp
import java.util.UUID

interface UserMailService {
    fun sendConfirmationMail(request: MailConfirmationMessage)
    fun sendResetPasswordMail(request: MailResetPasswordMessage)
    fun sendOrganizationInvitationMail(request: MailOrgInvitationMessage)
    fun sendDepositRequestMail(user: UUID, amount: Long)
    fun sendDepositInfoMail(user: UUID, minted: Boolean)
    fun sendWithdrawRequestMail(user: UUID, amount: Long)
    fun sendWithdrawInfoMail(user: UUID, burned: Boolean)
    fun sendWalletActivatedMail(walletOwner: UUID, walletType: WalletTypeAmqp, activationData: String)
    fun sendProjectFullyFundedMail(walletHash: String)
    fun sendSuccessfullyInvested(request: SuccessfullyInvestedMessage)
}
