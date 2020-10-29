package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.userservice.proto.UserResponse
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    val amount: Long,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Withdraw"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/withdraw-request-template.mustache")
    override val data: AmountData
        get() = AmountData(amount.toMailFormat())
}

class WithdrawTokenIssuerMail(
    val user: UserResponse,
    val amount: Long,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "New withdrawal request"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/token-issuer-withdrawal-template.mustache")
    override val data: UserData
        get() = UserData(user.firstName, user.lastName, amount.toMailFormat(), linkResolver.manageWithdrawalsLink)
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
