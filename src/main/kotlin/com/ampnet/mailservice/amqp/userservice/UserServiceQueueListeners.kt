package com.ampnet.mailservice.amqp.userservice

import com.ampnet.mailservice.service.UserMailService
import mu.KLogging
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class UserServiceQueueListeners(private val userMailService: UserMailService) {

    companion object : KLogging()

    @Bean
    fun mailConfirmationQueue(): Queue = Queue(QUEUE_USER_MAIL_CONFIRMATION)

    @Bean
    fun mailResetPasswordQueue(): Queue = Queue(QUEUE_USER_RESET_PASSWORD)

    @RabbitListener(queues = [QUEUE_USER_MAIL_CONFIRMATION])
    fun handleMailConfirmation(message: MailConfirmationMessage) {
        logger.debug { "Received message: $message" }
        userMailService.sendConfirmationMail(message)
    }

    @RabbitListener(queues = [QUEUE_USER_RESET_PASSWORD])
    fun handleMailResetPassword(message: MailResetPasswordMessage) {
        logger.debug { "Received message: $message" }
        userMailService.sendResetPasswordMail(message)
    }
}

const val QUEUE_USER_MAIL_CONFIRMATION = "mail.user.confirmation"
const val QUEUE_USER_RESET_PASSWORD = "mail.user.reset-password"
