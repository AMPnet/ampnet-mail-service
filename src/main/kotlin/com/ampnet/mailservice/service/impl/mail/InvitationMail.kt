package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val languageData = listOf(
        LanguageData(
            EN_LANGUAGE, "Invitation",
            DefaultMustacheFactory().compile("mustache/invitation-template.mustache")
        )
    )

    fun setData(organization: String, coop: String): InvitationMail {
        data = InvitationData(organization, linkResolver.getOrganizationInvitesLink(coop))
        return this
    }
}

data class InvitationData(val organization: String, val link: String)
