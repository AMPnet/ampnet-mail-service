package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ActivatedUserWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Wallet activated"
    override val template: Mustache =
        DefaultMustacheFactory().compile("mustache/user-wallet-activated-template.mustache")

    fun setData(activationData: String): ActivatedUserWalletMail {
        data = ActivatedUserWalletData(linkResolver.getWalletActivatedLink(WalletType.USER), activationData)
        return this
    }
}

data class ActivatedUserWalletData(val link: String, val activationData: String)

class ActivatedOrganizationWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Wallet activated"
    override val template: Mustache =
        DefaultMustacheFactory().compile("mustache/organization-wallet-activated-template.mustache")

    fun setData(organization: OrganizationResponse): ActivatedOrganizationWalletMail {
        data = ActivatedOrganizationWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.ORGANIZATION,
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
    override val title: String = "Wallet activated"
    override val template: Mustache =
        DefaultMustacheFactory().compile("mustache/project-wallet-activated-template.mustache")

    fun setData(project: ProjectResponse): ActivatedProjectWalletMail {
        data = ActivatedProjectWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.PROJECT,
                organizationUUid = project.organizationUuid,
                projectUuid = project.uuid
            ),
            project.name
        )
        return this
    }
}

data class ActivatedProjectWalletData(val link: String, val projectName: String)
