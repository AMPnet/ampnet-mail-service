package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositMail(
    val minted: Boolean,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Deposit"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/deposit-template.mustache")
    override val data: DepositInfo
        get() = DepositInfo(minted)
}

data class DepositInfo(val minted: Boolean)
