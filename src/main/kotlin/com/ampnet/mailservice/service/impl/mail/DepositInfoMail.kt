package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            EN_LANGUAGE, "Deposit",
            DefaultMustacheFactory().compile("mustache/deposit-template.mustache")
        )
    )

    fun setData(minted: Boolean): DepositInfoMail {
        data = DepositInfo(minted)
        return this
    }
}

data class DepositInfo(val minted: Boolean)
