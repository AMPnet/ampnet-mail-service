package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.mailservice.service.pojo.Attachment
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import org.springframework.mail.javamail.JavaMailSender

const val INVESTMENT_WITH_TOS_TEMPLATE = "investmentWithTosTemplate"
const val INVESTMENT_TEMPLATE = "investmentTemplate"

class SuccessfullyInvestedMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override var templateName = INVESTMENT_TEMPLATE
    override val titleKey = "investmentTitle"

    fun setTemplateData(project: ProjectWithDataResponse, amount: Long) = apply {
        templateData = InvestmentData(project.project.name, amount.toMailFormat())
    }

    fun addAttachment(attachment: Attachment) = apply {
        this.attachment = attachment
        this.templateName = INVESTMENT_WITH_TOS_TEMPLATE
    }
}

data class InvestmentData(val projectName: String, val amount: String)
