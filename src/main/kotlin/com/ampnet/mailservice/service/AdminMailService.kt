package com.ampnet.mailservice.service

import com.ampnet.mailservice.amqp.walletservice.WalletTypeAmqp
import java.util.UUID

interface AdminMailService {
    fun sendWithdrawRequestMail(user: UUID, amount: Long)
    fun sendNewWalletNotificationMail(walletType: WalletTypeAmqp, coop: String, activationData: String)
}
