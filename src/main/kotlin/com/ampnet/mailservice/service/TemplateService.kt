package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData

interface TemplateService {
    fun generateTextForMailConfirmation(data: MailConfirmationData): String
    fun generateTextForInvitation(data: InvitationData): String
}
