package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.grpc.walletservice.WalletService
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.mailservice.proto.SuccessfullyInvestedRequest
import com.ampnet.mailservice.service.FileService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.mailservice.service.UserMailService
import com.ampnet.mailservice.service.impl.mail.AbstractMail
import com.ampnet.mailservice.service.impl.mail.ActivatedOrganizationWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedProjectWalletMail
import com.ampnet.mailservice.service.impl.mail.ActivatedUserWalletMail
import com.ampnet.mailservice.service.impl.mail.ConfirmationMail
import com.ampnet.mailservice.service.impl.mail.DepositInfoMail
import com.ampnet.mailservice.service.impl.mail.DepositRequestMail
import com.ampnet.mailservice.service.impl.mail.FailedDeliveryMail
import com.ampnet.mailservice.service.impl.mail.InvitationMail
import com.ampnet.mailservice.service.impl.mail.ProjectFullyFundedMail
import com.ampnet.mailservice.service.impl.mail.ResetPasswordMail
import com.ampnet.mailservice.service.impl.mail.SuccessfullyInvestedMail
import com.ampnet.mailservice.service.impl.mail.WithdrawInfoMail
import com.ampnet.mailservice.service.impl.mail.WithdrawRequestMail
import com.ampnet.mailservice.service.pojo.Attachment
import com.ampnet.mailservice.service.pojo.TERMS_OF_SERVICE
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.walletservice.proto.WalletResponse
import mu.KLogging
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Suppress("TooManyFunctions", "LongParameterList")
class UserMailServiceImpl(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolverService: LinkResolverService,
    translationService: TranslationService,
    private val userService: UserService,
    private val projectService: ProjectService,
    private val walletService: WalletService,
    private val fileService: FileService
) : UserMailService {

    companion object : KLogging()

    private val confirmationMail: ConfirmationMail by lazy {
        ConfirmationMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val resetPasswordMail: ResetPasswordMail by lazy {
        ResetPasswordMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val invitationMail: InvitationMail by lazy {
        InvitationMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val depositRequestMail: DepositRequestMail by lazy {
        DepositRequestMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val depositMail: DepositInfoMail by lazy {
        DepositInfoMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val withdrawRequestMail: WithdrawRequestMail by lazy {
        WithdrawRequestMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val withdrawInfoMail: WithdrawInfoMail by lazy {
        WithdrawInfoMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val activatedUserWalletMail: ActivatedUserWalletMail by lazy {
        ActivatedUserWalletMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val activatedOrganizationWalletMail: ActivatedOrganizationWalletMail by lazy {
        ActivatedOrganizationWalletMail(
            linkResolverService, mailSender, applicationProperties, translationService
        )
    }
    private val activatedProjectWalletMail: ActivatedProjectWalletMail by lazy {
        ActivatedProjectWalletMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val failedDeliveryMail: FailedDeliveryMail by lazy {
        FailedDeliveryMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val projectFullyFundedMail: ProjectFullyFundedMail by lazy {
        ProjectFullyFundedMail(linkResolverService, mailSender, applicationProperties, translationService)
    }
    private val successfullyInvestedMail: SuccessfullyInvestedMail by lazy {
        SuccessfullyInvestedMail(linkResolverService, mailSender, applicationProperties, translationService)
    }

    override fun sendConfirmationMail(request: MailConfirmationRequest) =
        confirmationMail.setTemplateData(request.token, request.coop).setLanguage(request.language)
            .sendTo(request.email)

    override fun sendResetPasswordMail(request: ResetPasswordRequest) =
        resetPasswordMail.setTemplateData(request.token, request.coop).setLanguage(request.language)
            .sendTo(request.email)

    override fun sendOrganizationInvitationMail(request: OrganizationInvitationRequest) =
        invitationMail.setTemplateData(request.organization, request.coop).setLanguage(request.language)
            .sendTo(request.emailsList.toList()) { failedMails ->
                val filedMailRecipients = failedMails.map { it.allRecipients.toString() }
                failedDeliveryMail.setTemplateData(filedMailRecipients).setLanguage(request.language)
                    .setLanguage(request.language).sendTo(request.senderEmail)
            }

    override fun sendDepositRequestMail(user: UserResponse, amount: Long) =
        depositRequestMail.setTemplateData(amount).setLanguage(user.language).sendTo(user.email)

    override fun sendDepositInfoMail(user: UserResponse, minted: Boolean) =
        depositMail.setTemplateData(minted).setLanguage(user.language).sendTo(user.email)

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) =
        withdrawRequestMail.setTemplateData(amount).setLanguage(user.language).sendTo(user.email)

    override fun sendWithdrawInfoMail(user: UserResponse, burned: Boolean) =
        withdrawInfoMail.setTemplateData(burned).setLanguage(user.language).sendTo(user.email)

    override fun sendWalletActivatedMail(walletOwner: String, walletType: WalletType, activationData: String) {
        val (mail: AbstractMail, user: UserResponse) = when (walletType) {
            WalletType.USER -> {
                val user = getUser(walletOwner)
                Pair(
                    activatedUserWalletMail.setTemplateData(activationData, user.coop).setLanguage(user.language),
                    user
                )
            }
            WalletType.ORGANIZATION -> {
                val organization = projectService.getOrganization(UUID.fromString(walletOwner))
                val user = getUser(organization.createdByUser)
                Pair(
                    activatedOrganizationWalletMail.setTemplateData(organization, user.coop).setLanguage(user.language),
                    user
                )
            }
            WalletType.PROJECT -> {
                val project = projectService.getProject(UUID.fromString(walletOwner))
                val user = getUser(project.createdByUser)
                Pair(activatedProjectWalletMail.setTemplateData(project, user.coop).setLanguage(user.language), user)
            }
        }
        mail.sendTo(user.email)
    }

    override fun sendProjectFullyFundedMail(walletHash: String) {
        val wallet = walletService.getWalletByHash(walletHash)
        val project = projectService.getProject(UUID.fromString(wallet.owner))
        val user = getUser(project.createdByUser)
        projectFullyFundedMail.setTemplateData(user, project).setLanguage(user.language).sendTo(user.email)
    }

    override fun sendSuccessfullyInvested(request: SuccessfullyInvestedRequest) {
        val wallets = walletService.getWalletsByHash(setOf(request.walletHashFrom, request.walletHashTo))
        val user = getUser(getOwnerByHash(wallets, request.walletHashFrom))
        val project = projectService.getProjectWithData(UUID.fromString(getOwnerByHash(wallets, request.walletHashTo)))
        logger.debug("${project.project.uuid} has terms of service: ${project.tosUrl}")
        val termsOfService = if (project.tosUrl.isNotBlank()) {
            logger.debug("There should be an attachment ${project.tosUrl}")
            Attachment(TERMS_OF_SERVICE, fileService.getTermsOfService(project.tosUrl))
        } else {
            logger.debug("There is no attachment ${project.tosUrl}")
            null
        }
        successfullyInvestedMail.setTemplateData(project, request.amount.toLong(), termsOfService)
            .setLanguage(user.language)
            .sendTo(user.email)
    }

    private fun getOwnerByHash(wallets: List<WalletResponse>, hash: String): String =
        wallets.firstOrNull { it.hash == hash }?.owner
            ?: throw ResourceNotFoundException("Missing owner for wallet hash: $hash")

    private fun getUser(userUuid: String): UserResponse =
        userService.getUsers(listOf(userUuid)).firstOrNull()
            ?: throw ResourceNotFoundException("Missing user: $userUuid")
}
