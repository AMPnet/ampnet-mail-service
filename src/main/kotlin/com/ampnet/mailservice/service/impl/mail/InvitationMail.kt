package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = "invitationTemplate"
    override val title = "invitationTitle"

    fun setData(organization: String, coop: String): InvitationMail {
        data = InvitationData(organization, linkResolver.getOrganizationInvitesLink(coop))
        return this
    }

    fun setTemplate(language: String): InvitationMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class InvitationData(val organization: String, val link: String)
