package com.ampnet.mailservice.amqp.walletservice

import com.ampnet.mailservice.service.AdminMailService
import com.ampnet.mailservice.service.UserMailService
import mu.KLogging
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class WalletServiceQueueListeners(
    private val userMailService: UserMailService,
    private val adminMailService: AdminMailService
) {

    companion object : KLogging()

    @Bean
    fun mailDeposit(): Queue = Queue(QUEUE_MAIL_DEPOSIT)

    @Bean
    fun mailWithdraw(): Queue = Queue(QUEUE_MAIL_WITHDRAW)

    @Bean
    fun mailWithdrawInfo(): Queue = Queue(QUEUE_MAIL_WITHDRAW_INFO)

    @Bean
    fun mailWalletActivated(): Queue = Queue(QUEUE_MAIL_WALLET_ACTIVATED)

    @Bean
    fun mailWalletNew(): Queue = Queue(QUEUE_MAIL_WALLET_NEW)

    @RabbitListener(queues = [QUEUE_MAIL_DEPOSIT])
    fun handleDeposit(message: DepositInfoRequest) {
        logger.debug { "Received message: $message" }
        userMailService.sendDepositInfoMail(message.user, message.minted)
    }

    @RabbitListener(queues = [QUEUE_MAIL_WITHDRAW])
    fun handleWithdraw(message: WithdrawRequest) {
        logger.debug { "Received message: $message" }
        userMailService.sendWithdrawRequestMail(message.user, message.amount)
        adminMailService.sendWithdrawRequestMail(message.user, message.amount)
    }

    @RabbitListener(queues = [QUEUE_MAIL_WITHDRAW_INFO])
    fun handleWithdraw(message: WithdrawInfoRequest) {
        logger.debug { "Received message: $message" }
        userMailService.sendWithdrawInfoMail(message.user, message.burned)
    }

    @RabbitListener(queues = [QUEUE_MAIL_WALLET_ACTIVATED])
    fun handleWalletActivated(message: WalletActivatedRequest) {
        logger.debug { "Received message: $message" }
        userMailService.sendWalletActivatedMail(message.walletOwner, message.type, message.activationData)
    }

    @RabbitListener(queues = [QUEUE_MAIL_WALLET_NEW])
    fun handleNewWallet(message: NewWalletRequest) {
        logger.debug { "Received message: $message" }
        adminMailService.sendNewWalletNotificationMail(message.type, message.coop, message.activationData)
    }
}

const val QUEUE_MAIL_DEPOSIT = "mail.wallet.deposit"
const val QUEUE_MAIL_WITHDRAW = "mail.wallet.withdraw"
const val QUEUE_MAIL_WITHDRAW_INFO = "mail.wallet.withdraw-info"
const val QUEUE_MAIL_WALLET_ACTIVATED = "mail.wallet.activated"
const val QUEUE_MAIL_WALLET_NEW = "mail.wallet.new"
