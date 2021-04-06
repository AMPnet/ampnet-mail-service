package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.HeadlessCmsService
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.pojo.SuccessfullyInvestedTemplateData
import org.springframework.mail.javamail.JavaMailSender

class SuccessfullyInvestedMail(
    override val mailType: MailType,
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    fun setTemplateData(data: SuccessfullyInvestedTemplateData) = apply {
        this.attachment = data.attachment
        this.coop = data.coopResponse.coop
        val project = data.project.project
        templateData = InvestmentData(
            project.name,
            project.description,
            data.amount.toMailFormat(),
            data.coopResponse.name,
        )
    }
}

data class InvestmentData(
    val projectName: String,
    val projectDescription: String,
    val amount: String,
    val coop: String,
)
