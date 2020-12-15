package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class WithdrawInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            EN_LANGUAGE, "Withdraw",
            DefaultMustacheFactory().compile("mustache/withdraw-template.mustache")
        )
    )

    fun setData(burned: Boolean): WithdrawInfoMail {
        data = WithdrawInfo(burned)
        return this
    }
}
data class WithdrawInfo(val burned: Boolean)
