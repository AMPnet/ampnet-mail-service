package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.userservice.proto.UserResponse
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class WithdrawRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String = "Withdraw"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/withdraw-request-template.mustache")

    fun setData(amount: Long): WithdrawRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }
}

class WithdrawTokenIssuerMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String = "New withdrawal request"
    override val template: Mustache =
        DefaultMustacheFactory().compile("mustache/token-issuer-withdrawal-template.mustache")

    fun setData(user: UserResponse, amount: Long): WithdrawTokenIssuerMail {
        data = UserData(user.firstName, user.lastName, amount.toMailFormat(), linkResolver.manageWithdrawalsLink)
        return this
    }
}

data class UserData(val firstName: String, val lastName: String, val amount: String, val link: String)
