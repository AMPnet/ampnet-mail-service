package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Reset password"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/forgot-password-template.mustache")

    fun setData(token: String): ResetPasswordMail {
        data = ResetPasswordData(linkResolver.getResetPasswordLink(token))
        return this
    }
}

data class ResetPasswordData(val link: String)