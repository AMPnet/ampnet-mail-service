package com.ampnet.mailservice.amqp.userservice

import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.mailservice.service.UserMailService
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class UserServiceQueueListeners(private val userMailService: UserMailService) {

    @Bean
    fun mailConfirmationQueue(): Queue = Queue(QUEUE_USER_MAIL_CONFIRMATION)

    @Bean
    fun mailResetPasswordQueue(): Queue = Queue(QUEUE_USER_RESET_PASSWORD)

    @RabbitListener(queues = [QUEUE_USER_MAIL_CONFIRMATION])
    fun handleMailConfirmation(message: MailConfirmationMessage) {
        val request = MailConfirmationRequest.newBuilder()
            .setEmail(message.email)
            .setToken(message.token)
            .setCoop(message.coop)
            .setLanguage(message.language)
            .build()
        userMailService.sendConfirmationMail(request)
    }

    @RabbitListener(queues = [QUEUE_USER_RESET_PASSWORD])
    fun handleMailResetPassword(message: MailResetPasswordMessage) {
        val request = ResetPasswordRequest.newBuilder()
            .setEmail(message.email)
            .setToken(message.token)
            .setCoop(message.coop)
            .setLanguage(message.language)
            .build()
        userMailService.sendResetPasswordMail(request)
    }
}

const val QUEUE_USER_MAIL_CONFIRMATION = "mail.user.confirmation"
const val QUEUE_USER_RESET_PASSWORD = "mail.user.reset-password"
