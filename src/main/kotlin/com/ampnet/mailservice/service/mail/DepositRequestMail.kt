package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    val amount: Long,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Deposit"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/deposit-request-template.mustache")
    override val data: AmountData
        get() = AmountData(amount.toMailFormat())
}

data class AmountData(val amount: String)
