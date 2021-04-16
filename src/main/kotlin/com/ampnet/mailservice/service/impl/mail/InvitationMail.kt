package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = MailType.INVITATION_MAIL

    fun setTemplateData(organization: String, coop: String) = apply {
        this.coop = coop
        templateData = InvitationData(organization, linkResolver.getOrganizationInvitesLink(coop))
    }
}

data class InvitationData(val organization: String, val link: String)
