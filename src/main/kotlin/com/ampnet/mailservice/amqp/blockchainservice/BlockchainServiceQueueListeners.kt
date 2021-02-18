package com.ampnet.mailservice.amqp.blockchainservice

import com.ampnet.mailservice.service.UserMailService
import mu.KLogging
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class BlockchainServiceQueueListeners(private val userMailService: UserMailService) {

    companion object : KLogging()

    @Bean
    fun projectFullyFundedQueue(): Queue = Queue(QUEUE_MAIL_PROJECT_FULLY_FUNDED)

    @Bean
    fun successfullyInvestedQueue(): Queue = Queue(QUEUE_MAIL_SUCCESSFULLY_INVESTED)

    @RabbitListener(queues = [QUEUE_MAIL_PROJECT_FULLY_FUNDED])
    fun handleProjectFullyFunded(message: ProjectFullyFundedMessage) {
        logger.debug { "Received message: $message" }
        userMailService.sendProjectFullyFundedMail(message.txHash)
    }

    @RabbitListener(queues = [QUEUE_MAIL_SUCCESSFULLY_INVESTED])
    fun handleSuccessfullyInvested(message: SuccessfullyInvestedMessage) {
        logger.debug { "Received message: $message" }
        userMailService.sendSuccessfullyInvested(message)
    }
}

const val QUEUE_MAIL_PROJECT_FULLY_FUNDED = "mail.middleware.project-funded"
const val QUEUE_MAIL_SUCCESSFULLY_INVESTED = "mail.middleware.project-invested"
