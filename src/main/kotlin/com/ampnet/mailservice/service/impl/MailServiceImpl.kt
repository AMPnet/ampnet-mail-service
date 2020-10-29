package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.mail.AbstractMail
import com.ampnet.mailservice.service.mail.ActivatedOrganizationWalletMail
import com.ampnet.mailservice.service.mail.ActivatedProjectWalletMail
import com.ampnet.mailservice.service.mail.ActivatedUserWalletMail
import com.ampnet.mailservice.service.mail.ConfirmationMail
import com.ampnet.mailservice.service.mail.DepositMail
import com.ampnet.mailservice.service.mail.DepositRequestMail
import com.ampnet.mailservice.service.mail.FailedDeliveryMail
import com.ampnet.mailservice.service.mail.InvitationMail
import com.ampnet.mailservice.service.mail.NewOrganizationWalletMail
import com.ampnet.mailservice.service.mail.NewProjectWalletMail
import com.ampnet.mailservice.service.mail.NewUserWalletMail
import com.ampnet.mailservice.service.mail.ResetPasswordMail
import com.ampnet.mailservice.service.mail.WithdrawInfoMail
import com.ampnet.mailservice.service.mail.WithdrawRequestMail
import com.ampnet.mailservice.service.mail.WithdrawTokenIssuerMail
import com.ampnet.userservice.proto.UserResponse
import mu.KLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.UUID
import javax.mail.internet.MimeMessage

@Service
@Suppress("TooManyFunctions")
class MailServiceImpl(
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    private val userService: UserService,
    private val projectService: ProjectService
) : MailService {

    companion object : KLogging()

    override fun sendConfirmationMail(email: String, token: String) {
        val mail = ConfirmationMail(token, mailSender, applicationProperties)
        sendEmail(mail, email)
    }

    override fun sendResetPasswordMail(email: String, token: String) {
        val mail = ResetPasswordMail(token, mailSender, applicationProperties)
        sendEmail(mail, email)
    }

    override fun sendOrganizationInvitationMail(email: List<String>, organizationName: String, senderEmail: String) {
        val mail = InvitationMail(organizationName, mailSender, applicationProperties)
        sendEmail(mail, email) { failedMails ->
            val failedMail =
                FailedDeliveryMail(failedMails.map { it.allRecipients.toString() }, mailSender, applicationProperties)
            sendEmailOnFailedDelivery(failedMail.generateMails(listOf(senderEmail)).first())
        }
    }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) {
        val mail = DepositRequestMail(amount, mailSender, applicationProperties)
        sendEmail(mail, user.email)
    }

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) {
        val mail = DepositMail(minted, mailSender, applicationProperties)
        sendEmail(mail, user.email)
    }

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) {
        val tokenIssuers = userService.getTokenIssuers(user.coop)
        val tokenInfoMail = WithdrawTokenIssuerMail(user, amount, mailSender, applicationProperties)
        sendEmail(tokenInfoMail, tokenIssuers.map { it.email })

        val mail = WithdrawRequestMail(amount, mailSender, applicationProperties)
        sendEmail(mail, user.email)
    }

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) {
        val mail = WithdrawInfoMail(burned, mailSender, applicationProperties)
        sendEmail(mail, user.email)
    }

    override fun sendNewWalletNotificationMail(walletType: WalletType, coop: String) {
        val mail = when (walletType) {
            WalletType.USER -> NewUserWalletMail(mailSender, applicationProperties)
            WalletType.ORGANIZATION -> NewOrganizationWalletMail(mailSender, applicationProperties)
            WalletType.PROJECT -> NewProjectWalletMail(mailSender, applicationProperties)
        }
        val platformManagers = userService.getPlatformManagers(coop).map { it.email }
        sendEmail(mail, platformManagers)
    }

    override fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType) {
        val (mail, userUuid) = when (walletType) {
            WalletType.USER -> Pair(ActivatedUserWalletMail(mailSender, applicationProperties), walletOwner)
            WalletType.ORGANIZATION -> {
                val org = projectService.getOrganization(UUID.fromString(walletOwner))
                Pair(ActivatedOrganizationWalletMail(org, mailSender, applicationProperties), org.createdByUser)
            }
            WalletType.PROJECT -> {
                val project = projectService.getProject(UUID.fromString(walletOwner))
                Pair(ActivatedProjectWalletMail(project, mailSender, applicationProperties), project.createdByUser)
            }
        }
        val userEmail = userService.getUsers(listOf(userUuid)).map { it.email }
        sendEmail(mail, userEmail)
    }

    private fun sendEmail(mail: AbstractMail, to: List<String>, notifySenderOnError: (List<MimeMessage>) -> Unit = {}) {
        sendEmails(mail.generateMails(to), notifySenderOnError)
    }

    private fun sendEmail(mail: AbstractMail, to: String) = sendEmails(mail.generateMails(to))

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
}

const val FROM_CENTS_TO_EUROS = 100.0
const val TWO_DECIMAL_FORMAT = "%.2f"
fun Long.toMailFormat(): String = TWO_DECIMAL_FORMAT.format(this / FROM_CENTS_TO_EUROS)
