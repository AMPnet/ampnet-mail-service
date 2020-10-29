package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.MailService
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
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MailServiceImpl(
    private val mailSender: JavaMailSender,
    private val applicationProperties: ApplicationProperties,
    private val userService: UserService,
    private val projectService: ProjectService
) : MailService {

    companion object : KLogging()

    override fun sendConfirmationMail(email: String, token: String) =
        ConfirmationMail(token, mailSender, applicationProperties)
            .sendTo(email)

    override fun sendResetPasswordMail(email: String, token: String) =
        ResetPasswordMail(token, mailSender, applicationProperties)
            .sendTo(email)

    override fun sendOrganizationInvitationMail(emails: List<String>, organizationName: String, senderEmail: String) =
        InvitationMail(organizationName, mailSender, applicationProperties)
            .sendTo(emails) { failedMails ->
                val filedMailRecipients = failedMails.map { it.allRecipients.toString() }
                val failedMail = FailedDeliveryMail(filedMailRecipients, mailSender, applicationProperties)
                failedMail.sendTo(senderEmail)
            }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) =
        DepositRequestMail(amount, mailSender, applicationProperties)
            .sendTo(user.email)

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) =
        DepositMail(minted, mailSender, applicationProperties)
            .sendTo(user.email)

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) {
        WithdrawTokenIssuerMail(user, amount, mailSender, applicationProperties)
            .sendTo(userService.getTokenIssuers(user.coop).map { it.email })
        WithdrawRequestMail(amount, mailSender, applicationProperties)
            .sendTo(user.email)
    }

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) =
        WithdrawInfoMail(burned, mailSender, applicationProperties)
            .sendTo(user.email)

    override fun sendNewWalletNotificationMail(walletType: WalletType, coop: String) =
        when (walletType) {
            WalletType.USER -> NewUserWalletMail(mailSender, applicationProperties)
            WalletType.ORGANIZATION -> NewOrganizationWalletMail(mailSender, applicationProperties)
            WalletType.PROJECT -> NewProjectWalletMail(mailSender, applicationProperties)
        }.sendTo(userService.getPlatformManagers(coop).map { it.email })

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
        val userEmails = userService.getUsers(listOf(userUuid)).map { it.email }
        mail.sendTo(userEmails)
    }
}
