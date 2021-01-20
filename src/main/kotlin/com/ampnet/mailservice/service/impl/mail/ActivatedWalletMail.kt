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
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    translationService: TranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, translationService) {

    override val templateName = "userWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setData(activationData: String, coop: String) = apply {
        data = ActivatedUserWalletData(linkResolver.getWalletActivatedLink(WalletType.USER, coop), activationData)
    }
}

data class ActivatedUserWalletData(val link: String, val activationData: String)

class ActivatedOrganizationWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    translationService: TranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, translationService) {

    override val templateName = "organizationWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setData(organization: OrganizationResponse, coop: String) = apply {
        data = ActivatedOrganizationWalletData(
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
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    translationService: TranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, translationService) {

    override val templateName = "projectWalletActivatedTemplate"
    override val titleKey = WALLET_ACTIVATED_TITLE_KEY

    fun setData(project: ProjectResponse, coop: String) = apply {
        data = ActivatedProjectWalletData(
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
