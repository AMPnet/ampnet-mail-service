package com.ampnet.mailservice.service

interface MailService {
    fun sendConfirmationMail(to: String, token: String)
    fun sendOrganizationInvitationMail(to: String, organizationName: String)
    fun sendDepositInfoMail(to: String, minted: Boolean)
    fun sendWithdrawInfoMail(to: String, burned: Boolean)
}
