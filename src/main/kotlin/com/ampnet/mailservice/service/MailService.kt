package com.ampnet.mailservice.service

import com.ampnet.userservice.proto.UserResponse

interface MailService {
    fun sendConfirmationMail(user: UserResponse, token: String)
    fun sendOrganizationInvitationMail(user: UserResponse, organizationName: String)
    fun sendDepositRequestMail(user: UserResponse, amount: Long)
    fun sendDepositInfoMail(user: UserResponse, minted: Boolean)
    fun sendWithdrawRequestMail(user: UserResponse, amount: Long)
    fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean)
}
