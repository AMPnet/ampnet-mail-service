package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    translationService: TranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, translationService) {

    override val templateName = "depositRequestTemplate"
    override val titleKey = "depositInfoTitle"

    fun setData(amount: Long) = apply { data = AmountData(amount.toMailFormat()) }
}

data class AmountData(val amount: String)
