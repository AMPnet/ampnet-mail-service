package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = when (type) {
        WalletType.USER -> "userWalletTemplate"
        WalletType.PROJECT -> "projectWalletTemplate"
        WalletType.ORGANIZATION -> "organizationWalletTemplate"
    }
    override val titleKey = "newWalletTitle"

    fun setData(activationData: String, coop: String) = apply {
        data = if (type == WalletType.USER) NewWalletData(linkResolver.getNewWalletLink(type, coop), activationData)
        else NewWalletData(linkResolver.getNewWalletLink(type, coop))
    }
}

data class NewWalletData(val link: String, val activationData: String? = null)
