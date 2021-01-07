package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

const val DEPOSIT_INFO_TEMPLATE = "deposit-template.mustache"

class DepositInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, DEPOSIT_INFO_TEMPLATE, "Deposit"),
        generateLanguageData(EL_LANGUAGE, DEPOSIT_INFO_TEMPLATE, "Κατάθεση")
    )

    fun setData(minted: Boolean): DepositInfoMail {
        data = DepositInfo(minted)
        return this
    }
}

data class DepositInfo(val minted: Boolean)
