package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import org.springframework.mail.javamail.JavaMailSender

class SuccessfullyInvestedMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData: List<LanguageData>
        get() = listOf(generateLanguageData(EN_LANGUAGE, "investment-template.mustache", "Investment"))

    fun setData(project: ProjectWithDataResponse, amount: Long): SuccessfullyInvestedMail {
        data = InvestmentData(project.project.name, amount.toMailFormat(), project.tosUrl)
        return this
    }
}

data class InvestmentData(val projectName: String, val amount: String, val link: String?)
