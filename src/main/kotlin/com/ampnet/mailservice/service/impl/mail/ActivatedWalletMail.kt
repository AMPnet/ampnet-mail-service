package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.HeadlessCmsService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import org.springframework.mail.javamail.JavaMailSender

class ActivatedUserWalletMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    override val mailType = MailType.ACTIVATED_USER_WALLET_MAIL

    fun setTemplateData(activationData: String, coop: String) = apply {
        this.coop = coop
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
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    override val mailType = MailType.ACTIVATED_ORGANIZATION_WALLET_MAIL

    fun setTemplateData(organization: OrganizationResponse, coop: String) = apply {
        this.coop = coop
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
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    override val mailType = MailType.ACTIVATED_PROJECT_WALLET_MAIL

    fun setTemplateData(project: ProjectResponse, coop: String) = apply {
        this.coop = coop
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
