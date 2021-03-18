package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "depositTemplate"
    override val titleKey = "depositInfoTitle"

    fun setTemplateData(
        coop: String,
        minted: Boolean,
        hasProjectWhichCanReceiveInvestment: Boolean
    ) = apply {
        templateData = DepositInfo(
            coop, minted, hasProjectWhichCanReceiveInvestment,
            linkResolver.getProjectOffersLink(coop)
        )
    }
}

data class DepositInfo(
    val coop: String,
    val minted: Boolean,
    val hasProjectWhichCanReceiveInvestment: Boolean,
    val link: String
)
