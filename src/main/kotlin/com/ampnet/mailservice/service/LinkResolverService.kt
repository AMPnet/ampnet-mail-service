package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType

interface LinkResolverService {
    fun getOrganizationInvitesLink(): String
    fun getManageWithdrawalsLink(): String
    fun getConfirmationLink(token: String, coop: String): String
    fun getResetPasswordLink(token: String, coop: String): String
    fun getNewWalletLink(walletType: WalletType, coop: String): String
    fun getWalletActivatedLink(
        walletType: WalletType,
        coop: String,
        organizationUUid: String? = null,
        projectUuid: String? = null
    ): String
}
