package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.AdminMailService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.mailservice.service.impl.mail.NewWalletMail
import com.ampnet.mailservice.service.impl.mail.WithdrawTokenIssuerMail
import com.ampnet.userservice.proto.UserResponse
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class AdminMailServiceImpl(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolverService: LinkResolverService,
    translationService: TranslationService,
    private val userService: UserService
) : AdminMailService {

    private val withdrawTokenIssuerMail: WithdrawTokenIssuerMail by lazy {
        WithdrawTokenIssuerMail(
            linkResolverService, mailSender, applicationProperties, translationService
        )
    }
    private val newUserWalletMail: NewWalletMail by lazy {
        NewWalletMail(
            WalletType.USER, linkResolverService, mailSender, applicationProperties,
            translationService
        )
    }
    private val newOrganizationWalletMail: NewWalletMail by lazy {
        NewWalletMail(
            WalletType.ORGANIZATION, linkResolverService, mailSender, applicationProperties,
            translationService
        )
    }
    private val newProjectWalletMail: NewWalletMail by lazy {
        NewWalletMail(
            WalletType.PROJECT, linkResolverService, mailSender, applicationProperties,
            translationService
        )
    }

    override fun sendWithdrawRequestMail(user: UserResponse, amount: Long) =
        userService.getTokenIssuers(user.coop).forEach {
            withdrawTokenIssuerMail.setTemplateData(user, amount).setLanguage(user.language).sendTo(it.email)
        }

    override fun sendNewWalletNotificationMail(walletType: WalletType, coop: String, activationData: String) =
        userService.getPlatformManagers(coop).forEach {
            when (walletType) {
                WalletType.USER -> newUserWalletMail
                WalletType.PROJECT -> newProjectWalletMail
                WalletType.ORGANIZATION -> newOrganizationWalletMail
            }.setTemplateData(activationData, coop).setLanguage(it.language).sendTo(it.email)
        }
}
