package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import org.springframework.mail.javamail.JavaMailSender

class SuccessfullyInvestedMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = "investmentTemplate"
    override val title = "investmentTitle"

    fun setData(project: ProjectWithDataResponse, amount: Long): SuccessfullyInvestedMail {
        data = InvestmentData(project.project.name, amount.toMailFormat(), project.tosUrl)
        return this
    }

    fun setTemplate(language: String): SuccessfullyInvestedMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class InvestmentData(val projectName: String, val amount: String, val link: String?)
