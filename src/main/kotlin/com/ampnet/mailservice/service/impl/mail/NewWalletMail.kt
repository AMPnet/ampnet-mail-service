package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = when (type) {
        WalletType.USER -> MailType.NEW_USER_WALLET_MAIL
        WalletType.PROJECT -> MailType.NEW_PROJECT_WALLET_MAIL
        WalletType.ORGANIZATION -> MailType.NEW_ORGANIZATION_WALLET_MAIL
    }

    fun setTemplateData(activationData: String, coop: String) = apply {
        this.coop = coop
        templateData = if (type == WalletType.USER)
            NewWalletData(linkResolver.getNewWalletLink(type, coop), activationData)
        else NewWalletData(linkResolver.getNewWalletLink(type, coop))
    }
}

data class NewWalletData(val link: String, val activationData: String? = null)
