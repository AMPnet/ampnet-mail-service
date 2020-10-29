package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    val organization: String,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Invitation"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/invitation-template.mustache")
    override val data: InvitationData
        get() = InvitationData(organization, linkResolver.organizationInvitesLink)
}

data class InvitationData(val organization: String, val link: String)
