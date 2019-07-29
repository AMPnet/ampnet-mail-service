package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMailMessage
import org.springframework.stereotype.Service
import java.util.Date
import javax.mail.internet.MimeMessage

@Service
class MailServiceImpl(
    private val mailSender: JavaMailSender,
    private val templateService: TemplateService,
    private val applicationProperties: ApplicationProperties
) : MailService {

    companion object : KLogging()

    internal val confirmationMailSubject = "Confirm your email"
    internal val invitationMailSubject = "Invitation"

    override fun sendConfirmationMail(to: String, token: String) {
        val link = getConfirmationLink(token)
        val message = templateService.generateTextForMailConfirmation(MailConfirmationData(link))
        val mail = createMailMessage(to, confirmationMailSubject, message)
        if (applicationProperties.mail.enabled) {
            sendEmail(mail)
        } else {
            logger.warn { "Sending email is disabled. \nEmail: $mail" }
        }
    }

    override fun sendOrganizationInvitationMail(to: String, organizationName: String) {
        val data = InvitationData(organizationName, applicationProperties.mail.organizationInvitationsLink)
        val message = templateService.generateTextForInvitation(data)
        val mail = createMailMessage(to, invitationMailSubject, message)
        if (applicationProperties.mail.enabled) {
            sendEmail(mail)
        } else {
            logger.warn { "Sending email is disabled. \nEmail: $mail" }
        }
    }

    private fun createMailMessage(to: String, subject: String, text: String): MimeMessage {
        val mail = mailSender.createMimeMessage()
        val helper = MimeMailMessage(mail)
        helper.setFrom(applicationProperties.mail.sender)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text)
        helper.setSentDate(Date())
        return mail
    }

    private fun sendEmail(mail: MimeMessage) {
        logger.info { "Sending email: ${mail.content} " }
        val recipients = mail.allRecipients.map { it.toString() }
        try {
            mailSender.send(mail)
            logger.info { "Successfully sent email to: $recipients" }
        } catch (ex: MailException) {
            logger.error(ex) { "Cannot send email to: $recipients" }
        }
    }

    private fun getConfirmationLink(token: String): String =
        "${applicationProperties.mail.confirmationBaseLink}?token=$token"
}