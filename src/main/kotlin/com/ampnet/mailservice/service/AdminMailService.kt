package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.userservice.proto.UserResponse

interface AdminMailService {
    fun sendWithdrawRequestMail(user: UserResponse, amount: Long)
    fun sendNewWalletNotificationMail(walletType: WalletType, coop: String, activationData: String)
}
