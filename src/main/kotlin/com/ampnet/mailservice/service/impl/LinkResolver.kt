package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import java.net.URL

class LinkResolver(applicationProperties: ApplicationProperties) {

    private val baseUrl: URL = URL(applicationProperties.mail.baseUrl)
    private val confirmationLink = applicationProperties.mail.confirmationLink
    private val resetPasswordLink = applicationProperties.mail.resetPasswordLink
    private val newWalletLink = applicationProperties.mail.newWalletLink
    val organizationInvitesLink = baseUrl.append(applicationProperties.mail.organizationInvitationsLink).toString()
    val manageWithdrawalsLink = baseUrl.append(applicationProperties.mail.manageWithdrawalsLink).toString()

    fun getConfirmationLink(token: String): String = baseUrl.append("$confirmationLink?token=$token").toString()
    fun getResetPasswordLink(token: String): String = baseUrl.append("$resetPasswordLink?token=$token").toString()

    fun getNewWalletLink(walletType: WalletType): String =
        when (walletType) {
            WalletType.USER -> baseUrl.append("$newWalletLink/users")
            WalletType.PROJECT -> baseUrl.append("$newWalletLink/projects")
            WalletType.ORGANIZATION -> baseUrl.append("$newWalletLink/groups")
        }.toString()
}

fun URL.append(path: String): URL = URL(this, path)
