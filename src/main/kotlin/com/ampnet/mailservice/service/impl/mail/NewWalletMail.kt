package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class NewWalletMail(
    val type: WalletType,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "New wallet created"
    override val template: Mustache
        get() = when (type) {
            WalletType.USER -> mustacheFactory.compile("mustache/user-wallet-template.mustache")
            WalletType.PROJECT -> mustacheFactory.compile("mustache/user-wallet-template.mustache")
            WalletType.ORGANIZATION -> mustacheFactory.compile("mustache/organization-wallet-template.mustache")
        }
    override val data: NewWalletData
        get() = NewWalletData(linkResolver.getNewWalletLink(type))
}

data class NewWalletData(val link: String)
