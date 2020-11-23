package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.stereotype.Service
import java.net.URL

@Service
class LinkResolverServiceImpl(applicationProperties: ApplicationProperties) : LinkResolverService {

    private val baseUrl = URL(applicationProperties.mail.baseUrl).toString()
    private val confirmationPath = applicationProperties.mail.confirmationPath
    private val resetPasswordPath = applicationProperties.mail.resetPasswordPath
    private val newWalletPath = applicationProperties.mail.newWalletPath
    private val walletActivatedPath = applicationProperties.mail.walletActivatedPath
    private val organizationInvitesPath = applicationProperties.mail.organizationInvitationsPath
    private val manageProjectPath = applicationProperties.mail.manageProjectPath

    private val manageWithdrawalsLink =
        "$baseUrl/${applicationProperties.mail.manageWithdrawalsPath}".removeDoubleSlashes()

    override fun getOrganizationInvitesLink(coop: String) =
        "$baseUrl/$coop/$organizationInvitesPath".removeDoubleSlashes()

    override fun getManageWithdrawalsLink() = manageWithdrawalsLink

    override fun getConfirmationLink(token: String, coop: String): String =
        "$baseUrl/$coop/$confirmationPath?token=$token".removeDoubleSlashes()

    override fun getResetPasswordLink(token: String, coop: String): String =
        "$baseUrl/$coop/$resetPasswordPath?token=$token".removeDoubleSlashes()

    override fun getNewWalletLink(walletType: WalletType, coop: String): String {
        val typePath = when (walletType) {
            WalletType.USER -> "users"
            WalletType.PROJECT -> "projects"
            WalletType.ORGANIZATION -> "groups"
        }
        return "$baseUrl/$coop/$newWalletPath/$typePath".removeDoubleSlashes()
    }

    override fun getWalletActivatedLink(
        walletType: WalletType,
        coop: String,
        organizationUUid: String?,
        projectUuid: String?
    ): String {
        val typePath = when (walletType) {
            WalletType.USER -> walletActivatedPath
            WalletType.PROJECT -> "$organizationInvitesPath/$organizationUUid/$manageProjectPath/$projectUuid"
            WalletType.ORGANIZATION -> "$organizationInvitesPath/$organizationUUid"
        }
        return "$baseUrl/$coop/$typePath".removeDoubleSlashes()
    }

    private fun String.removeDoubleSlashes() = this.replace("(?<!(http:)|(https:))//+".toRegex(), "/")
}
