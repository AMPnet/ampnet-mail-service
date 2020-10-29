package com.ampnet.mailservice.service

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.impl.FROM_CENTS_TO_EUROS
import com.ampnet.mailservice.service.impl.TWO_DECIMAL_FORMAT
import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositRequestData
import com.ampnet.mailservice.service.pojo.MailText
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class DepositRequest(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : MailServiceALT(mailSender, applicationProperties) {

    internal val depositSubject = "Deposit"
    private val depositRequestTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/deposit-request-template.mustache")
    }

    override fun sendMail(mailTextInput: MailText) {
       if (mailTextInput !is DepositRequestData) return
        val data = AmountData((TWO_DECIMAL_FORMAT.format(mailTextInput.amount / FROM_CENTS_TO_EUROS)))
        val message = fillTemplate(depositRequestTemplate, data)
        val mail = createMailMessage(listOf(mailTextInput.user.email), depositSubject, message)
        sendEmails(mail)
    }
}