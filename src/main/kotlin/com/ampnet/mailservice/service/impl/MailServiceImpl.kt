package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositInfo
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import com.ampnet.mailservice.service.pojo.NewWalletData
import com.ampnet.mailservice.service.pojo.ResetPasswordData
import com.ampnet.mailservice.service.pojo.UserData
import com.ampnet.mailservice.service.pojo.WithdrawInfo
import com.ampnet.userservice.proto.UserResponse
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.Date
import javax.mail.internet.MimeMessage

const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"

@Service
@Suppress("TooManyFunctions", "SpreadOperator")
class MailServiceImpl(
    private val mailSender: JavaMailSender,
    private val templateService: TemplateService,
    val applicationProperties: ApplicationProperties,
    private val userService: UserService
) : MailService {

    companion object : KLogging()

    private val linkResolver = LinkResolver(applicationProperties)
    internal val confirmationMailSubject = "Confirm your email"
    internal val resetPasswordSubject = "Reset password"
    internal val invitationMailSubject = "Invitation"
    internal val depositSubject = "Deposit"
    internal val withdrawSubject = "Withdraw"
    internal val newWalletSubject = "New wallet created"
    internal val manageWithdrawalsSubject = "New withdrawal request"

    override fun sendConfirmationMail(email: String, token: String) {
        val link = linkResolver.getConfirmationLink(token)
        val message = templateService.generateTextForMailConfirmation(MailConfirmationData(link))
        val mail = createMailMessage(listOf(email), confirmationMailSubject, message)
        sendEmail(mail)
    }

    override fun sendResetPasswordMail(email: String, token: String) {
        val link = linkResolver.getResetPasswordLink(token)
        val message = templateService.generateTextForResetPassword(ResetPasswordData(link))
        val mail = createMailMessage(listOf(email), resetPasswordSubject, message)
        sendEmail(mail)
    }

    override fun sendOrganizationInvitationMail(email: List<String>, organizationName: String) {
        val data = InvitationData(organizationName, linkResolver.organizationInvitesLink)
        val message = templateService.generateTextForInvitation(data)
        val mail = createMailMessage(email, invitationMailSubject, message)
        sendEmail(mail)
    }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) {
        val data = AmountData((TWO_DECIMAL_FORMAT.format(amount / FROM_CENTS_TO_EUROS)))
        val message = templateService.generateTextForDepositRequest(data)
        val mail = createMailMessage(listOf(user.email), depositSubject, message)
        sendEmail(mail)
    }

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) {
        val data = DepositInfo(minted)
        val message = templateService.generateTextForDepositInfo(data)
        val mail = createMailMessage(listOf(user.email), depositSubject, message)
        sendEmail(mail)
    }

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) {
        val tokenIssuers = userService.getTokenIssuers()
        val link = linkResolver.manageWithdrawalsLink
        val userData = UserData(
            user.firstName, user.lastName,
            TWO_DECIMAL_FORMAT.format(amount / FROM_CENTS_TO_EUROS), link
        )
        val tokenIssuersMessage = templateService.generateTextForTokenIssuerWithdrawRequest(userData)
        val tokenIssuersMail =
            createMailMessage(tokenIssuers.map { it.email }, manageWithdrawalsSubject, tokenIssuersMessage)

        val userMessage = templateService.generateTextForWithdrawRequest(
            AmountData(TWO_DECIMAL_FORMAT.format(amount / FROM_CENTS_TO_EUROS))
        )
        val userMail = createMailMessage(listOf(user.email), withdrawSubject, userMessage)

        sendEmail(tokenIssuersMail)
        sendEmail(userMail)
    }

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) {
        val data = WithdrawInfo(burned)
        val message = templateService.generateTextForWithdrawInfo(data)
        val mail = createMailMessage(listOf(user.email), withdrawSubject, message)
        sendEmail(mail)
    }

    override fun sendNewWalletNotificationMail(walletType: WalletType) {
        val link = linkResolver.getNewWalletLink(walletType)
        val message = templateService.generateTextForNewWallet(NewWalletData(link), walletType)
        val platformManagers = userService.getPlatformManagers()
        val mail = createMailMessage(platformManagers.map { it.email }, newWalletSubject, message)
        sendEmail(mail)
    }

    private fun createMailMessage(to: List<String>, subject: String, text: String): List<MimeMessage> {
        return to.map {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail)
            helper.setFrom(applicationProperties.mail.sender)
            helper.setTo(it)
            helper.setSubject(subject)
            helper.setText(text, true)
            helper.setSentDate(Date())
            mail
        }
    }

    private fun sendEmail(mails: List<MimeMessage>) {
        if (applicationProperties.mail.enabled.not()) {
            logger.warn { "Sending email is disabled. \nEmail: ${mails.first().content}" }
            return
        }
        logger.info { "Sending email: ${mails.first().subject} " }
        val recipients = mails.map { it.allRecipients.first().toString() }
        try {
            mailSender.send(*mails.toTypedArray())
            logger.info { "Successfully sent email to: $recipients" }
        } catch (ex: MailException) {
            logger.error(ex) { "Cannot send email to: $recipients" }
        }
    }
}
