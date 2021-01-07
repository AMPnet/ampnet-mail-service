package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import org.springframework.mail.javamail.JavaMailSender

const val WALLET_ACTIVATED_TITLE = "Wallet activated"
const val WALLET_ACTIVATED_TITLE_EL = "Το πορτοφόλι ενεργοποιήθηκε"

class ActivatedUserWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "user-wallet-activated-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE),
        generateLanguageData(EL_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE_EL)
    )

    fun setData(activationData: String, coop: String): ActivatedUserWalletMail {
        data = ActivatedUserWalletData(linkResolver.getWalletActivatedLink(WalletType.USER, coop), activationData)
        return this
    }
}

data class ActivatedUserWalletData(val link: String, val activationData: String)

class ActivatedOrganizationWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "organization-wallet-activated-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE),
        generateLanguageData(EL_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE_EL)
    )

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
}

data class ActivatedOrganizationWalletData(val link: String, val organizationName: String)

class ActivatedProjectWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "project-wallet-activated-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE),
        generateLanguageData(EL_LANGUAGE, templateName, WALLET_ACTIVATED_TITLE_EL)
    )

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
}

data class ActivatedProjectWalletData(val link: String, val projectName: String)
