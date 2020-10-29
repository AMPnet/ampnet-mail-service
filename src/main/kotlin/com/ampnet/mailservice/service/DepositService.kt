package com.ampnet.mailservice.service

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.FROM_CENTS_TO_EUROS
import com.ampnet.mailservice.service.impl.TWO_DECIMAL_FORMAT
import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositRequestData
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositService(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : MailServiceALT<DepositRequestData>(mailSender, applicationProperties) {

    internal val depositSubject = "Deposit"
    private val depositRequestTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/deposit-request-template.mustache")
    }

    override fun sendMail(data: DepositRequestData) {
        val amountData = AmountData((TWO_DECIMAL_FORMAT.format(data.amount / FROM_CENTS_TO_EUROS)))
        val message = fillTemplate(depositRequestTemplate, amountData)
        val mail = createMailMessage(listOf(data.user.email), depositSubject, message)
        sendEmails(mail)
    }
}