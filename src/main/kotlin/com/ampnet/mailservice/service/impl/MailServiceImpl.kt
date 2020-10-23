package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
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
import com.ampnet.mailservice.service.pojo.WalletActivatedData
import com.ampnet.mailservice.service.pojo.WithdrawInfo
import com.ampnet.userservice.proto.UserResponse
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"

@Service
@Suppress("TooManyFunctions")
class MailServiceImpl(
    private val mailSender: JavaMailSender,
    private val templateService: TemplateService,
    val applicationProperties: ApplicationProperties,
    private val userService: UserService,
    private val projectservice: ProjectService
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
    internal val walletActivatedSubject = "Wallet activated"
    internal val failedDeliveryMailSubject = "Email delivery failed"

    override fun sendConfirmationMail(email: String, token: String) {
        val link = linkResolver.getConfirmationLink(token)
        val message = templateService.generateTextForMailConfirmation(MailConfirmationData(link))
        val mail = createMailMessage(listOf(email), confirmationMailSubject, message)
        sendEmails(mail)
    }

    override fun sendResetPasswordMail(email: String, token: String) {
        val link = linkResolver.getResetPasswordLink(token)
        val message = templateService.generateTextForResetPassword(ResetPasswordData(link))
        val mail = createMailMessage(listOf(email), resetPasswordSubject, message)
        sendEmails(mail)
    }

    override fun sendOrganizationInvitationMail(email: List<String>, organizationName: String, senderEmail: String) {
        val data = InvitationData(organizationName, linkResolver.organizationInvitesLink)
        val message = templateService.generateTextForInvitation(data)
        val mails = createMailMessage(email, invitationMailSubject, message)
        sendEmails(mails) { failedMails ->
            val failedDeliveryMessage = templateService.generateTextForFailedDeliveryMessage(
                failedMails.map { it.allRecipients }.joinToString()
            )
            val failedDeliveryMail =
                createMailMessage(listOf(senderEmail), failedDeliveryMailSubject, failedDeliveryMessage).first()
            sendEmailOnFailedDelivery(failedDeliveryMail)
        }
    }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) {
        val data = AmountData((TWO_DECIMAL_FORMAT.format(amount / FROM_CENTS_TO_EUROS)))
        val message = templateService.generateTextForDepositRequest(data)
        val mail = createMailMessage(listOf(user.email), depositSubject, message)
        sendEmails(mail)
    }

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) {
        val data = DepositInfo(minted)
        val message = templateService.generateTextForDepositInfo(data)
        val mail = createMailMessage(listOf(user.email), depositSubject, message)
        sendEmails(mail)
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

        sendEmails(tokenIssuersMail)
        sendEmails(userMail)
    }

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) {
        val data = WithdrawInfo(burned)
        val message = templateService.generateTextForWithdrawInfo(data)
        val mail = createMailMessage(listOf(user.email), withdrawSubject, message)
        sendEmails(mail)
    }

    override fun sendNewWalletNotificationMail(walletType: WalletType) {
        val link = linkResolver.getNewWalletLink(walletType)
        val message = templateService.generateTextForNewWallet(NewWalletData(link), walletType)
        val platformManagers = userService.getPlatformManagers()
        val mail = createMailMessage(platformManagers.map { it.email }, newWalletSubject, message)
        sendEmails(mail)
    }

    override fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType) {
        val (walletActivatedData, userUUid) = getDataAndUser(walletOwner, walletType)
        val message = templateService.generateTextForWalletActivated(walletActivatedData, walletType)
        val userEmail = userService.getUsers(listOf(userUUid)).map { it.email }
        val mail = createMailMessage(userEmail, walletActivatedSubject, message)
        sendEmails(mail)
    }

    private fun createMailMessage(to: List<String>, subject: String, text: String): List<MimeMessage> {
        return to.mapNotNull {
            val mail = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mail)
            try {
                helper.isValidateAddresses = true
                helper.setFrom(applicationProperties.mail.sender)
                helper.setTo(it)
                helper.setSubject(subject)
                helper.setText(text, true)
                helper.setSentDate(Date())
                mail
            } catch (ex: MessagingException) {
                logger.warn { "Cannot create mail from: $to" }
                null
            }
        }
    }

    private fun sendEmails(mails: List<MimeMessage>, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        if (applicationProperties.mail.enabled.not()) {
            logger.warn { "Sending email is disabled. \nEmail: ${mails.first().content}" }
            return
        }
        logger.info { "Sending email: ${mails.first().subject}" }
        val failed = mails.filter { sendEmail(it).not() }
        if (failed.isNotEmpty()) {
            notifySenderOnError.invoke(failed)
        }
    }

    private fun sendEmail(mail: MimeMessage): Boolean {
        return try {
            mailSender.send(mail)
            logger.info { "Successfully sent email to: ${mail.allRecipients}" }
            true
        } catch (ex: MailException) {
            logger.warn { "Cannot send email to: ${mail.allRecipients}" }
            false
        }
    }

    private fun sendEmailOnFailedDelivery(mail: MimeMessage) {
        logger.info { "Sending failed delivery email: ${mail.subject}" }
        try {
            mailSender.send(mail)
            logger.info { "Successfully sent failed delivery email to sender: ${mail.sender}" }
        } catch (ex: MailException) {
            logger.warn(ex) { "Cannot send failed delivery email to sender: ${mail.sender}" }
        }
    }

    private fun getDataAndUser(walletOwner: String, walletType: WalletType): Pair<WalletActivatedData, String> {
        return when (walletType) {
            WalletType.USER -> {
                val link = linkResolver.getWalletActivatedLink(walletType)
                Pair(WalletActivatedData(link), walletOwner)
            }
            WalletType.PROJECT -> {
                val project = projectservice.getProject(UUID.fromString(walletOwner))
                val link = linkResolver.getWalletActivatedLink(walletType, project.organizationUuid, project.uuid)
                Pair(WalletActivatedData(link, projectName = project.name), project.createdByUser)
            }
            WalletType.ORGANIZATION -> {
                val org = projectservice.getOrganization(UUID.fromString(walletOwner))
                val link = linkResolver.getWalletActivatedLink(walletType, org.uuid)
                Pair(WalletActivatedData(link, organizationName = org.name), org.createdByUser)
            }
        }
    }
}
