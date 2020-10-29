package com.ampnet.mailservice.service

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.FROM_CENTS_TO_EUROS
import com.ampnet.mailservice.service.impl.TWO_DECIMAL_FORMAT
import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositRequestData
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.OrganizationInvitationData
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class OrganizationInvitationService(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : MailServiceALT<OrganizationInvitationData>(mailSender, applicationProperties) {

    internal val invitationMailSubject = "Invitation"
    internal val failedDeliveryMailSubject = "Email delivery failed"
    private val invitationTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/invitation-template.mustache")
    }
    private val failedDeliveryMessageTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/failed-delivery-message-template.mustache")
    }

    override fun sendMail(data: OrganizationInvitationData) {
        val invitationData = InvitationData(data.organization, linkResolver.organizationInvitesLink)
        val message = fillTemplate(invitationTemplate, invitationData)
        val mails = createMailMessage(data.emails, invitationMailSubject, message)
        sendEmails(mails) { failedMails ->
            val failedDeliveryMessage = fillTemplate(
                failedDeliveryMessageTemplate, failedMails.map { it.allRecipients }.joinToString()
            )
            val failedDeliveryMail =
                createMailMessage(listOf(data.senderEmail), failedDeliveryMailSubject, failedDeliveryMessage).first()
            sendEmailOnFailedDelivery(failedDeliveryMail)
        }
    }
}