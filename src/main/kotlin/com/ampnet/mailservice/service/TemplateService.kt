package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.pojo.DepositInfo
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import com.ampnet.mailservice.service.pojo.WithdrawInfo

interface TemplateService {
    fun generateTextForMailConfirmation(data: MailConfirmationData): String
    fun generateTextForInvitation(data: InvitationData): String
    fun generateTextForDepositInfo(data: DepositInfo): String
    fun generateTextForWithdrawInfo(data: WithdrawInfo): String
}
