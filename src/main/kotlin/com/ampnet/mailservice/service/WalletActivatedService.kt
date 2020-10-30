package com.ampnet.mailservice.service

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.service.pojo.WalletActivatedData
import com.ampnet.mailservice.service.pojo.WalletActivatedRequestData
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WalletActivatedService(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    val userService: UserService,
    val projectService: ProjectService
) : MailServiceALT<WalletActivatedRequestData>(mailSender, applicationProperties) {

    internal val walletActivatedSubject = "Wallet activated"
    private val userWalletActivatedTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/user-wallet-activated-template.mustache")
    }
    private val projectWalletActivatedTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/project-wallet-activated-template.mustache")
    }
    private val organizationWalletActivatedTemplate: Mustache by lazy {
        mustacheFactory.compile("mustache/organization-wallet-activated-template.mustache")
    }

    override fun sendMail(data: WalletActivatedRequestData) {
        val (walletActivatedData, userUUid) = getDataAndUser(data.walletOwner, data.walletType)
        val message = when (data.walletType) {
            WalletType.USER -> fillTemplate(userWalletActivatedTemplate, walletActivatedData)
            WalletType.PROJECT -> fillTemplate(projectWalletActivatedTemplate, walletActivatedData)
            WalletType.ORGANIZATION -> fillTemplate(organizationWalletActivatedTemplate, walletActivatedData)
        }
        val userEmail = userService.getUsers(listOf(userUUid)).map { it.email }
        val mail = createMailMessage(userEmail, walletActivatedSubject, message)
        sendEmails(mail)
    }

    private fun getDataAndUser(walletOwner: String, walletType: WalletType): Pair<WalletActivatedData, String> {
        return when (walletType) {
            WalletType.USER -> {
                val link = linkResolver.getWalletActivatedLink(walletType)
                Pair(WalletActivatedData(link), walletOwner)
            }
            WalletType.PROJECT -> {
                val project = projectService.getProject(UUID.fromString(walletOwner))
                val link = linkResolver.getWalletActivatedLink(walletType, project.organizationUuid, project.uuid)
                Pair(WalletActivatedData(link, projectName = project.name), project.createdByUser)
            }
            WalletType.ORGANIZATION -> {
                val org = projectService.getOrganization(UUID.fromString(walletOwner))
                val link = linkResolver.getWalletActivatedLink(walletType, org.uuid)
                Pair(WalletActivatedData(link, organizationName = org.name), org.createdByUser)
            }
        }
    }
}
