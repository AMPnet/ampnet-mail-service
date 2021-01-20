package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import org.springframework.mail.javamail.JavaMailSender

class WithdrawInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "withdrawTemplate"
    override val title = "withdrawTitle"

    fun setData(burned: Boolean): WithdrawInfoMail {
        data = WithdrawInfo(burned)
        return this
    }

    fun setTemplate(language: String): WithdrawInfoMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}
data class WithdrawInfo(val burned: Boolean)
