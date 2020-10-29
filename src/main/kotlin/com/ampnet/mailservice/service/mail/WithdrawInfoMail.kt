package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class WithdrawInfoMail(
    val minted: Boolean,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Withdraw"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/withdraw-template.mustache")
    override val data: WithdrawInfo
        get() = WithdrawInfo(minted)
}
data class WithdrawInfo(val burned: Boolean)
