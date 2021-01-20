package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import org.springframework.mail.javamail.JavaMailSender

const val WALLET_ACTIVATED_TITLE = "walletActivatedTitle"

class ActivatedUserWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "userWalletActivatedTemplate"
    override val title = WALLET_ACTIVATED_TITLE

    fun setData(activationData: String, coop: String): ActivatedUserWalletMail {
        data = ActivatedUserWalletData(linkResolver.getWalletActivatedLink(WalletType.USER, coop), activationData)
        return this
    }

    fun setTemplate(language: String): ActivatedUserWalletMail {
        template = TemplateRequestData(language, templateName, WALLET_ACTIVATED_TITLE)
        return this
    }
}

data class ActivatedUserWalletData(val link: String, val activationData: String)

class ActivatedOrganizationWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "organizationWalletActivatedTemplate"
    override val title = WALLET_ACTIVATED_TITLE

    fun setData(organization: OrganizationResponse, coop: String): ActivatedOrganizationWalletMail {
        data = ActivatedOrganizationWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.ORGANIZATION,
                coop,
                organizationUUid = organization.uuid
            ),
            organization.name
        )
        return this
    }

    fun setTemplate(language: String): ActivatedOrganizationWalletMail {
        template = TemplateRequestData(language, templateName, WALLET_ACTIVATED_TITLE)
        return this
    }
}

data class ActivatedOrganizationWalletData(val link: String, val organizationName: String)

class ActivatedProjectWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "projectWalletActivatedTemplate"
    override val title = WALLET_ACTIVATED_TITLE

    fun setData(project: ProjectResponse, coop: String): ActivatedProjectWalletMail {
        data = ActivatedProjectWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.PROJECT,
                coop,
                organizationUUid = project.organizationUuid,
                projectUuid = project.uuid
            ),
            project.name
        )
        return this
    }

    fun setTemplate(language: String): ActivatedProjectWalletMail {
        template = TemplateRequestData(language, templateName, WALLET_ACTIVATED_TITLE)
        return this
    }
}

data class ActivatedProjectWalletData(val link: String, val projectName: String)
