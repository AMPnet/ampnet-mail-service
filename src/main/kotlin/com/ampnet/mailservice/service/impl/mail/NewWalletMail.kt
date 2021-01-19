package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = when (type) {
        WalletType.USER -> "userWalletTemplate"
        WalletType.PROJECT -> "projectWalletTemplate"
        WalletType.ORGANIZATION -> "organizationWalletTemplate"
    }
    override val title = "newWalletTitle"

    fun setData(activationData: String, coop: String): NewWalletMail {
        data = if (type == WalletType.USER) NewWalletData(linkResolver.getNewWalletLink(type, coop), activationData)
        else NewWalletData(linkResolver.getNewWalletLink(type, coop))
        return this
    }

    fun setTemplate(language: String): NewWalletMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class NewWalletData(val link: String, val activationData: String? = null)
