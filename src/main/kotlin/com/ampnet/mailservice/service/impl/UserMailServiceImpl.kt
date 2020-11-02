package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.UserMailService
import com.ampnet.mailservice.service.impl.mail.ActivatedOrganizationWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedProjectWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedUserWalletMail
import com.ampnet.mailservice.service.impl.mail.ConfirmationMail
import com.ampnet.mailservice.service.impl.mail.DepositInfoMail
import com.ampnet.mailservice.service.impl.mail.DepositRequestMail
import com.ampnet.mailservice.service.impl.mail.FailedDeliveryMail
import com.ampnet.mailservice.service.impl.mail.InvitationMail
import com.ampnet.mailservice.service.impl.mail.ResetPasswordMail
import com.ampnet.mailservice.service.impl.mail.WithdrawInfoMail
import com.ampnet.mailservice.service.impl.mail.WithdrawRequestMail
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserMailServiceImpl(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolverService: LinkResolverService,
    private val userService: UserService,
    private val projectService: ProjectService
) : UserMailService {

    private val confirmationMail: ConfirmationMail by lazy {
        ConfirmationMail(mailSender, applicationProperties, linkResolverService)
    }
    private val resetPasswordMail: ResetPasswordMail by lazy {
        ResetPasswordMail(mailSender, applicationProperties, linkResolverService)
    }
    private val invitationMail: InvitationMail by lazy {
        InvitationMail(mailSender, applicationProperties, linkResolverService)
    }
    private val depositRequestMail: DepositRequestMail by lazy {
        DepositRequestMail(mailSender, applicationProperties, linkResolverService)
    }
    private val depositMail: DepositInfoMail by lazy {
        DepositInfoMail(mailSender, applicationProperties, linkResolverService)
    }
    private val withdrawRequestMail: WithdrawRequestMail by lazy {
        WithdrawRequestMail(mailSender, applicationProperties, linkResolverService)
    }
    private val withdrawInfoMail: WithdrawInfoMail by lazy {
        WithdrawInfoMail(mailSender, applicationProperties, linkResolverService)
    }
    private val activatedUserWalletMail: ActivatedUserWalletMail by lazy {
        ActivatedUserWalletMail(mailSender, applicationProperties, linkResolverService)
    }
    private val activatedOrganizationWalletMail: ActivatedOrganizationWalletMail by lazy {
        ActivatedOrganizationWalletMail(mailSender, applicationProperties, linkResolverService)
    }
    private val activatedProjectWalletMail: ActivatedProjectWalletMail by lazy {
        ActivatedProjectWalletMail(mailSender, applicationProperties, linkResolverService)
    }
    private val failedDeliveryMail: FailedDeliveryMail by lazy {
        FailedDeliveryMail(mailSender, applicationProperties, linkResolverService)
    }

    override fun sendConfirmationMail(email: String, token: String) =
        confirmationMail.setData(token).sendTo(email)

    override fun sendResetPasswordMail(email: String, token: String) =
        resetPasswordMail.setData(token).sendTo(email)

    override fun sendOrganizationInvitationMail(emails: List<String>, organizationName: String, senderEmail: String) =
        invitationMail.setData(organizationName)
            .sendTo(emails) { failedMails ->
                val filedMailRecipients = failedMails.map { it.allRecipients.toString() }
                failedDeliveryMail.setData(filedMailRecipients).sendTo(senderEmail)
            }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) =
        depositRequestMail.setData(amount).sendTo(user.email)

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) =
        depositMail.setData(minted).sendTo(user.email)

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) =
        withdrawRequestMail.setData(amount).sendTo(user.email)

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) =
        withdrawInfoMail.setData(burned).sendTo(user.email)

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
