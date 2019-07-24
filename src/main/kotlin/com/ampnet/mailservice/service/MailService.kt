package com.ampnet.mailservice.service

interface MailService {
    fun sendConfirmationMail(to: String, token: String)
    fun sendOrganizationInvitationMail(to: String, organizationName: String)
}
