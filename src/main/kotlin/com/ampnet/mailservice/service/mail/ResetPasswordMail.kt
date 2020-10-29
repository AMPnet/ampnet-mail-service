package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    val token: String,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Reset password"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/forgot-password-template.mustache")
    override val data: ResetPasswordData
        get() = ResetPasswordData(linkResolver.getResetPasswordLink(token))
}

data class ResetPasswordData(val link: String)
