package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType

interface LinkResolverService {
    fun getOrganizationInvitesLink(): String
    fun getManageWithdrawalsLink(): String
    fun getConfirmationLink(token: String): String
    fun getResetPasswordLink(token: String): String
    fun getNewWalletLink(walletType: WalletType): String
    fun getWalletActivatedLink(
        walletType: WalletType,
        organizationUUid: String? = null,
        projectUuid: String? = null
    ): String
}
