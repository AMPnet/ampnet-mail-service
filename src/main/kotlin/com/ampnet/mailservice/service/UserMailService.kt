package com.ampnet.mailservice.service

import com.ampnet.mailservice.amqp.walletservice.WalletTypeAmqp
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.mailservice.proto.SuccessfullyInvestedRequest
import java.util.UUID

interface UserMailService {
    fun sendConfirmationMail(request: MailConfirmationRequest)
    fun sendResetPasswordMail(request: ResetPasswordRequest)
    fun sendOrganizationInvitationMail(request: OrganizationInvitationRequest)
    fun sendDepositRequestMail(user: UUID, amount: Long)
    fun sendDepositInfoMail(user: UUID, minted: Boolean)
    fun sendWithdrawRequestMail(user: UUID, amount: Long)
    fun sendWithdrawInfoMail(user: UUID, burned: Boolean)
    fun sendWalletActivatedMail(walletOwner: UUID, walletType: WalletTypeAmqp, activationData: String)
    fun sendProjectFullyFundedMail(walletHash: String)
    fun sendSuccessfullyInvested(request: SuccessfullyInvestedRequest)
}
