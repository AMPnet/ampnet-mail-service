package com.ampnet.mailservice.amqp.projectservice

import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.service.UserMailService
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ProjectServiceQueueListeners(private val userMailService: UserMailService) {

    @Bean
    fun organizationInvitationQueue(): Queue = Queue(QUEUE_MAIL_ORG_INVITATION)

    @RabbitListener(queues = [QUEUE_MAIL_ORG_INVITATION])
    fun handleMailOrgInvitation(message: MailOrgInvitationMessage) {
        val request = OrganizationInvitationRequest.newBuilder()
            .addAllEmails(message.emails)
            .setOrganization(message.organization)
            .setSenderEmail(message.senderEmail)
            .setCoop(message.coop)
            .build()
        userMailService.sendOrganizationInvitationMail(request)
    }

    data class MailOrgInvitationMessage(
        val emails: List<String>,
        val organization: String,
        val senderEmail: String,
        val coop: String
    )
}

const val QUEUE_MAIL_ORG_INVITATION = "mail.project.org-invitation"
