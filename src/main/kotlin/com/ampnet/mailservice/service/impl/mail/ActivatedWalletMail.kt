package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import org.springframework.mail.javamail.JavaMailSender

const val WALLET_ACTIVATED_TITLE_KEY = "walletActivatedTitle"

class ActivatedUserWalletMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "userWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setTemplateData(activationData: String, coop: String) = apply {
        templateData = ActivatedUserWalletData(
            linkResolver.getWalletActivatedLink(WalletType.USER, coop), activationData
        )
    }
}

data class ActivatedUserWalletData(val link: String, val activationData: String)

class ActivatedOrganizationWalletMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "organizationWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setTemplateData(organization: OrganizationResponse, coop: String) = apply {
        templateData = ActivatedOrganizationWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.ORGANIZATION,
                coop,
                organizationUUid = organization.uuid
            ),
            organization.name
        )
    }
}

data class ActivatedOrganizationWalletData(val link: String, val organizationName: String)

class ActivatedProjectWalletMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "projectWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setTemplateData(project: ProjectResponse, coop: String) = apply {
        templateData = ActivatedProjectWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.PROJECT,
                coop,
                organizationUUid = project.organizationUuid,
                projectUuid = project.uuid
            ),
            project.name
        )
    }
}

data class ActivatedProjectWalletData(val link: String, val projectName: String)
