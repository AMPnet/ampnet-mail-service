package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ActivatedUserWalletMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Wallet activated"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/user-wallet-activated-template.mustache")
    override val data: ActivatedUserWalletData
        get() = ActivatedUserWalletData(linkResolver.getWalletActivatedLink(WalletType.USER))
}

data class ActivatedUserWalletData(val link: String)

class ActivatedOrganizationWalletMail(
    val organization: OrganizationResponse,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Wallet activated"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/organization-wallet-activated-template.mustache")
    override val data: ActivatedOrganizationWalletData
        get() = ActivatedOrganizationWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.ORGANIZATION,
                organizationUUid = organization.uuid
            ),
            organization.name
        )
}

data class ActivatedOrganizationWalletData(val link: String, val organizationName: String)

class ActivatedProjectWalletMail(
    val project: ProjectResponse,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Wallet activated"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/project-wallet-activated-template.mustache")
    override val data: ActivatedProjectWalletData
        get() = ActivatedProjectWalletData(
            linkResolver.getWalletActivatedLink(
                WalletType.PROJECT,
                organizationUUid = project.organizationUuid,
                projectUuid = project.uuid
            ),
            project.name
        )
}

data class ActivatedProjectWalletData(val link: String, val projectName: String)
