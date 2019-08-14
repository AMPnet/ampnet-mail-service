package com.ampnet.mailservice.service

interface MailService {
    fun sendConfirmationMail(to: String, token: String)
    fun sendOrganizationInvitationMail(to: String, organizationName: String)
    fun sendDepositRequestMail(to: String, amount: Long)
    fun sendDepositInfoMail(to: String, minted: Boolean)
    fun sendWithdrawRequestMail(to: String, amount: Long)
    fun sendWithdrawInfoMail(to: String, burned: Boolean)
}
