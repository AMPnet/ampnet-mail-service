package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

const val INVITATION_TEMPLATE = "invitation-template.mustache"

class InvitationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, INVITATION_TEMPLATE, "Invitation"),
        generateLanguageData(EL_LANGUAGE, INVITATION_TEMPLATE, "Πρόσκληση")
    )

    fun setData(organization: String, coop: String): InvitationMail {
        data = InvitationData(organization, linkResolver.getOrganizationInvitesLink(coop))
        return this
    }
}

data class InvitationData(val organization: String, val link: String)
