package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import java.net.URL

class LinkResolver(applicationProperties: ApplicationProperties) {

    private val baseUrl: URL = URL(applicationProperties.mail.baseUrl)
    private val confirmationPath = applicationProperties.mail.confirmationPath
    private val resetPasswordPath = applicationProperties.mail.resetPasswordPath
    private val newWalletPath = applicationProperties.mail.newWalletPath
    val organizationInvitesLink = baseUrl.append(applicationProperties.mail.organizationInvitationsPath).toString()
    val manageWithdrawalsLink = baseUrl.append(applicationProperties.mail.manageWithdrawalsPath).toString()

    fun getConfirmationLink(token: String): String = baseUrl.append("$confirmationPath?token=$token").toString()
    fun getResetPasswordLink(token: String): String = baseUrl.append("$resetPasswordPath?token=$token").toString()

    fun getNewWalletLink(walletType: WalletType): String =
        when (walletType) {
            WalletType.USER -> baseUrl.append("$newWalletPath/users")
            WalletType.PROJECT -> baseUrl.append("$newWalletPath/projects")
            WalletType.ORGANIZATION -> baseUrl.append("$newWalletPath/groups")
        }.toString()
}

fun URL.append(path: String): URL = URL(this, path)
