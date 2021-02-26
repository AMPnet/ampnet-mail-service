package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.mailservice.service.pojo.SuccessfullyInvestedTemplateData
import org.springframework.mail.javamail.JavaMailSender

class SuccessfullyInvestedMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "investmentTemplate"
    override val titleKey = "investmentTitle"

    fun setTemplateData(data: SuccessfullyInvestedTemplateData) = apply {
        this.attachment = data.attachment
        val project = data.project.project
        templateData = InvestmentData(
            project.name,
            project.description,
            data.amount.toMailFormat(),
            data.coopName,
            attachment != null
        )
    }
}

data class InvestmentData(
    val projectName: String,
    val projectDescription: String,
    val amount: String,
    val coopName: String,
    val withTos: Boolean
)
