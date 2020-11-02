package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Deposit"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/deposit-request-template.mustache")

    fun setData(amount: Long): DepositRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }
}

data class AmountData(val amount: String)
