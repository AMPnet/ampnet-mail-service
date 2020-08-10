package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.pojo.AmountData
import com.ampnet.mailservice.service.pojo.DepositInfo
import com.ampnet.mailservice.service.pojo.InvitationData
import com.ampnet.mailservice.service.pojo.MailConfirmationData
import com.ampnet.mailservice.service.pojo.NewWalletData
import com.ampnet.mailservice.service.pojo.ResetPasswordData
import com.ampnet.mailservice.service.pojo.WithdrawInfo

interface TemplateService {
    fun generateTextForMailConfirmation(data: MailConfirmationData): String
    fun generateTextForResetPassword(data: ResetPasswordData): String
    fun generateTextForInvitation(data: InvitationData): String
    fun generateTextForDepositRequest(data: AmountData): String
    fun generateTextForDepositInfo(data: DepositInfo): String
    fun generateTextForWithdrawRequest(data: AmountData): String
    fun generateTextForWithdrawInfo(data: WithdrawInfo): String
    fun generateTextForNewWallet(data: NewWalletData): String
}
