package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String = "Deposit"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/deposit-template.mustache")

    fun setData(minted: Boolean): DepositInfoMail {
        data = DepositInfo(minted)
        return this
    }
}

data class DepositInfo(val minted: Boolean)
