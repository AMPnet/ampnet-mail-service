package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import java.net.URL

class LinkResolver(applicationProperties: ApplicationProperties) {

    private val baseUrl = URL(applicationProperties.mail.baseUrl).toString()
    private val confirmationPath = applicationProperties.mail.confirmationPath
    private val resetPasswordPath = applicationProperties.mail.resetPasswordPath
    private val newWalletPath = applicationProperties.mail.newWalletPath
    val organizationInvitesLink =
        "$baseUrl/${applicationProperties.mail.organizationInvitationsPath}".removeDoubleSlashes()
    val manageWithdrawalsLink = "$baseUrl/${applicationProperties.mail.manageWithdrawalsPath}".removeDoubleSlashes()

    fun getConfirmationLink(token: String): String = "$baseUrl/$confirmationPath?token=$token".removeDoubleSlashes()
    fun getResetPasswordLink(token: String): String = "$baseUrl/$resetPasswordPath?token=$token".removeDoubleSlashes()

    fun getNewWalletLink(walletType: WalletType): String {
        val typePath = when (walletType) {
            WalletType.USER -> "users"
            WalletType.PROJECT -> "projects"
            WalletType.ORGANIZATION -> "groups"
        }
        return "$baseUrl/$newWalletPath/$typePath".removeDoubleSlashes()
    }
}

fun String.removeDoubleSlashes() = this.replace("(?<!http:)//".toRegex(), "/")
