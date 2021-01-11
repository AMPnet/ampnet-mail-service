package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class WithdrawInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "withdraw-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, "Withdraw"),
        generateLanguageData(EL_LANGUAGE, templateName, "Ανάληψη")
    )

    fun setData(burned: Boolean): WithdrawInfoMail {
        data = WithdrawInfo(burned)
        return this
    }
}
data class WithdrawInfo(val burned: Boolean)
