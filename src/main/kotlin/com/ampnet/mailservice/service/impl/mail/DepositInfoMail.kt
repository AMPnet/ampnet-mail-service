package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = "depositTemplate"
    override val title = "depositInfoTitle"

    fun setData(minted: Boolean): DepositInfoMail {
        data = DepositInfo(minted)
        return this
    }

    fun setTemplate(language: String): DepositInfoMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class DepositInfo(val minted: Boolean)
