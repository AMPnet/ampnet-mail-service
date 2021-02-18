package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import com.ampnet.mailservice.service.pojo.Attachment
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import org.springframework.mail.javamail.JavaMailSender

class SuccessfullyInvestedMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "investmentTemplate"
    override val titleKey = "investmentTitle"

    fun setTemplateData(project: ProjectWithDataResponse, amount: Long, withTos: Boolean) = apply {
        templateData = InvestmentData(project.project.name, amount.toMailFormat(), withTos)
    }

    fun addAttachment(attachment: Attachment?) = apply { this.attachment = attachment }
}

data class InvestmentData(val projectName: String, val amount: String, val withTos: Boolean)
