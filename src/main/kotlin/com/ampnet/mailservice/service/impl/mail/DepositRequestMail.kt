package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

const val DEPOSIT_REQUEST_TEMPLATE = "deposit-request-template.mustache"

class DepositRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, DEPOSIT_REQUEST_TEMPLATE, "Deposit"),
        generateLanguageData(EL_LANGUAGE, DEPOSIT_REQUEST_TEMPLATE, "Κατάθεση")
    )

    fun setData(amount: Long): DepositRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }
}

data class AmountData(val amount: String)
