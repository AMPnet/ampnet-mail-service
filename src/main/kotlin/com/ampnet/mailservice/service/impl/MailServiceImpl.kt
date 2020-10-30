package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.MailService
import com.ampnet.mailservice.service.impl.mail.ActivatedOrganizationWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedProjectWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedUserWalletMail
import com.ampnet.mailservice.service.impl.mail.ConfirmationMail
import com.ampnet.mailservice.service.impl.mail.DepositInfoMail
import com.ampnet.mailservice.service.impl.mail.DepositRequestMail
import com.ampnet.mailservice.service.impl.mail.FailedDeliveryMail
import com.ampnet.mailservice.service.impl.mail.InvitationMail
import com.ampnet.mailservice.service.impl.mail.NewWalletMail
import com.ampnet.mailservice.service.impl.mail.ResetPasswordMail
import com.ampnet.mailservice.service.impl.mail.WithdrawInfoMail
import com.ampnet.mailservice.service.impl.mail.WithdrawRequestMail
import com.ampnet.mailservice.service.impl.mail.WithdrawTokenIssuerMail
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

    private val confirmationMail = ConfirmationMail(mailSender, applicationProperties)
    private val resetPasswordMail = ResetPasswordMail(mailSender, applicationProperties)
    private val invitationMail = InvitationMail(mailSender, applicationProperties)
    private val depositRequestMail = DepositRequestMail(mailSender, applicationProperties)
    private val depositMail = DepositInfoMail(mailSender, applicationProperties)
    private val withdrawRequestMail = WithdrawRequestMail(mailSender, applicationProperties)
    private val withdrawTokenIssuerMail = WithdrawTokenIssuerMail(mailSender, applicationProperties)
    private val withdrawInfoMail = WithdrawInfoMail(mailSender, applicationProperties)
    private val activatedUserWalletMail = ActivatedUserWalletMail(mailSender, applicationProperties)
    private val activatedOrganizationWalletMail = ActivatedOrganizationWalletMail(mailSender, applicationProperties)
    private val activatedProjectWalletMail = ActivatedProjectWalletMail(mailSender, applicationProperties)
    private val newUserWalletMail = NewWalletMail(WalletType.USER, mailSender, applicationProperties)
    private val newOrganizationWalletMail = NewWalletMail(WalletType.ORGANIZATION, mailSender, applicationProperties)
    private val newProjectWalletMail = NewWalletMail(WalletType.PROJECT, mailSender, applicationProperties)

    override fun sendConfirmationMail(email: String, token: String) =
        confirmationMail.setData(token).sendTo(email)

    override fun sendResetPasswordMail(email: String, token: String) =
        resetPasswordMail.setData(token).sendTo(email)

    override fun sendOrganizationInvitationMail(emails: List<String>, organizationName: String, senderEmail: String) =
        invitationMail.setData(organizationName)
            .sendTo(emails) { failedMails ->
                val filedMailRecipients = failedMails.map { it.allRecipients.toString() }
                FailedDeliveryMail(mailSender, applicationProperties).setData(filedMailRecipients)
                    .sendTo(senderEmail)
            }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) =
        depositRequestMail.setData(amount).sendTo(user.email)

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) =
        depositMail.setData(minted).sendTo(user.email)

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) {
        withdrawTokenIssuerMail.setData(user, amount).sendTo(userService.getTokenIssuers(user.coop).map { it.email })
        withdrawRequestMail.setData(amount).sendTo(user.email)
    }

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) =
        withdrawInfoMail.setData(burned).sendTo(user.email)

    override fun sendNewWalletNotificationMail(walletType: WalletType, coop: String) =
        when (walletType) {
            WalletType.USER -> newUserWalletMail
            WalletType.PROJECT -> newProjectWalletMail
            WalletType.ORGANIZATION -> newOrganizationWalletMail
        }.sendTo(userService.getPlatformManagers(coop).map { it.email })

    override fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType) {
        val (mail, userUuid) = when (walletType) {
            WalletType.USER -> Pair(activatedUserWalletMail, walletOwner)
            WalletType.ORGANIZATION -> {
                val organization = projectService.getOrganization(UUID.fromString(walletOwner))
                Pair(activatedOrganizationWalletMail.setData(organization), organization.createdByUser)
            }
            WalletType.PROJECT -> {
                val project = projectService.getProject(UUID.fromString(walletOwner))
                Pair(activatedProjectWalletMail.setData(project), project.createdByUser)
            }
        }
        val userEmails = userService.getUsers(listOf(userUuid)).map { it.email }
        mail.sendTo(userEmails)
    }
}
