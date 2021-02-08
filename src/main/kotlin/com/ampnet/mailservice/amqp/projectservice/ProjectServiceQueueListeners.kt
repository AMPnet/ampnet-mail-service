package com.ampnet.mailservice.amqp.projectservice

import com.ampnet.mailservice.service.UserMailService
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProjectServiceQueueListeners(private val userMailService: UserMailService) {

    @Bean
    fun organizationInvitationQueue(): Queue = Queue(QUEUE_MAIL_ORG_INVITATION)

    @RabbitListener(queues = [QUEUE_MAIL_ORG_INVITATION])
    fun handleMailOrgInvitation(message: MailOrgInvitationMessage) {
        userMailService.sendOrganizationInvitationMail(message)
    }
}

data class MailOrgInvitationMessage(
    val emails: List<String>,
    val organizationName: String,
    val sender: UUID,
    val coop: String
)

const val QUEUE_MAIL_ORG_INVITATION = "mail.project.org-invitation"
