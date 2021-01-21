package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "invitationTemplate"
    override val titleKey = "invitationTitle"

    fun setData(organization: String, coop: String) = apply {
        data = InvitationData(organization, linkResolver.getOrganizationInvitesLink(coop))
    }
}

data class InvitationData(val organization: String, val link: String)
