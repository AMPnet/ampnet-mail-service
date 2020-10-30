package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class InvitationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String = "Invitation"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/invitation-template.mustache")

    fun setData(organization: String): InvitationMail {
        data = InvitationData(organization, linkResolver.organizationInvitesLink)
        return this
    }
}

data class InvitationData(val organization: String, val link: String)
