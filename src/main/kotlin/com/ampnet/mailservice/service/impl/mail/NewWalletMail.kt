package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "New wallet created"
    override val template: Mustache = when (type) {
        WalletType.USER -> DefaultMustacheFactory().compile("mustache/user-wallet-template.mustache")
        WalletType.PROJECT -> DefaultMustacheFactory().compile("mustache/user-wallet-template.mustache")
        WalletType.ORGANIZATION -> DefaultMustacheFactory().compile("mustache/organization-wallet-template.mustache")
    }
    override var data: Any? = NewWalletData(linkResolver.getNewWalletLink(type))
}

data class NewWalletData(val link: String)
