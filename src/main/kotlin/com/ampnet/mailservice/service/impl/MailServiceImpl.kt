package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositInfo
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import com.ampnet.mailservice.service.pojo.WithdrawInfo
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
    internal val depositSubject = "Deposit"
    internal val withdrawSubject = "Withdraw"

    override fun sendConfirmationMail(to: String, token: String) {
        val link = getConfirmationLink(token)
        val message = templateService.generateTextForMailConfirmation(MailConfirmationData(link))
        val mail = createMailMessage(to, confirmationMailSubject, message)
        sendEmail(mail)
    }

    override fun sendOrganizationInvitationMail(to: String, organizationName: String) {
        val data = InvitationData(organizationName, applicationProperties.mail.organizationInvitationsLink)
        val message = templateService.generateTextForInvitation(data)
        val mail = createMailMessage(to, invitationMailSubject, message)
        sendEmail(mail)
    }

    override fun sendDepositRequestMail(to: String, amount: Long) {
        val data = AmountData(amount)
        val message = templateService.generateTextForDepositRequest(data)
        val mail = createMailMessage(to, depositSubject, message)
        sendEmail(mail)
    }

    override fun sendDepositInfoMail(to: String, minted: Boolean) {
        val data = DepositInfo(minted)
        val message = templateService.generateTextForDepositInfo(data)
        val mail = createMailMessage(to, depositSubject, message)
        sendEmail(mail)
    }

    override fun sendWithdrawRequestMail(to: String, amount: Long) {
        val data = AmountData(amount)
        val message = templateService.generateTextForWithdrawRequest(data)
        val mail = createMailMessage(to, withdrawSubject, message)
        sendEmail(mail)
    }

    override fun sendWithdrawInfoMail(to: String, burned: Boolean) {
        val data = WithdrawInfo(burned)
        val message = templateService.generateTextForWithdrawInfo(data)
        val mail = createMailMessage(to, withdrawSubject, message)
        sendEmail(mail)
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
        if (applicationProperties.mail.enabled.not()) {
            logger.warn { "Sending email is disabled. \nEmail: ${mail.content}" }
            return
        }

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
