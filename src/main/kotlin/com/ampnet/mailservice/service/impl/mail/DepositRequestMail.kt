package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "depositRequestTemplate"
    override val titleKey = "depositInfoTitle"

    fun setData(amount: Long) = apply { data = AmountData(amount.toMailFormat()) }
}

data class AmountData(val amount: String)
