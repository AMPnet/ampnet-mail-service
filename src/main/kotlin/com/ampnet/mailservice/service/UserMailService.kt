package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.userservice.proto.UserResponse

interface UserMailService {
    fun sendConfirmationMail(request: MailConfirmationRequest)
    fun sendResetPasswordMail(request: ResetPasswordRequest)
    fun sendOrganizationInvitationMail(request: OrganizationInvitationRequest)
    fun sendDepositRequestMail(user: UserResponse, amount: Long)
    fun sendDepositInfoMail(user: UserResponse, minted: Boolean)
    fun sendWithdrawRequestMail(user: UserResponse, amount: Long)
    fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean)
    fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType, activationData: String)
    fun sendProjectFullyFundedMail(walletHash: String)
}
