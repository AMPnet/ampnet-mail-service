package com.ampnet.mailservice.service

import com.ampnet.mailservice.enums.WalletType
import com.ampnet.mailservice.service.pojo.WalletActivatedRequestData
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class WalletActivatedServiceTest : ServiceTestBase() {

    private val testContext = TestContext()
    private val service: WalletActivatedService by lazy {
        WalletActivatedService(mailSender, applicationProperties, userService, projectService)
    }

    @Test
    fun mustSetCorrectSendUserWalletActivatedMail() {
        suppose("Service sent mail for user wallet activated") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(user.uuid.toString())))
                .thenReturn(listOf(user))
            service.sendMail(WalletActivatedRequestData(user.uuid, WalletType.USER))
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(service.walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" + applicationProperties.mail.walletActivatedPath
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test
    fun mustSetCorrectSendProjectWalletActivatedMail() {
        suppose("Project service returns project") {
            testContext.walletOwner = UUID.randomUUID().toString()
            testContext.project = generateProjectResponse(testContext.walletOwner)
            Mockito.`when`(projectService.getProject(UUID.fromString(testContext.walletOwner)))
                .thenReturn(testContext.project)
        }
        suppose("User service returns user") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.project.createdByUser)))
                .thenReturn(listOf(user))
        }
        suppose("Service sent mail for project wallet activated") {
            service.sendMail(WalletActivatedRequestData(testContext.walletOwner, WalletType.PROJECT))
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(service.walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.project.organizationUuid +
                "/" + applicationProperties.mail.manageProjectPath + "/" + testContext.project.uuid
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    @Test
    fun mustSetCorrectSendOrganizationWalletActivatedMail() {
        suppose("Project service returns organization") {
            testContext.walletOwner = UUID.randomUUID().toString()
            testContext.organization = generateOrganizationResponse(testContext.walletOwner)
            Mockito.`when`(projectService.getOrganization(UUID.fromString(testContext.walletOwner)))
                .thenReturn(testContext.organization)
        }
        suppose("User service returns user") {
            val user = generateUserResponse(testContext.receiverMail)
            Mockito.`when`(userService.getUsers(listOf(testContext.organization.createdByUser)))
                .thenReturn(listOf(user))
        }
        suppose("Service sent mail for organization wallet activated") {
            service.sendMail(WalletActivatedRequestData(testContext.walletOwner, WalletType.ORGANIZATION))
        }

        verify("The mail is sent to right receiver and has correct data") {
            val mailList = wiser.messages
            val userMail = mailList.first()
            Assertions.assertThat(userMail.envelopeSender).isEqualTo(applicationProperties.mail.sender)
            Assertions.assertThat(userMail.envelopeReceiver).isEqualTo(testContext.receiverMail)
            Assertions.assertThat(userMail.mimeMessage.subject).isEqualTo(service.walletActivatedSubject)
            val confirmationUserLink = applicationProperties.mail.baseUrl + "/" +
                applicationProperties.mail.organizationInvitationsPath + "/" + testContext.organization.uuid
            Assertions.assertThat(userMail.mimeMessage.content.toString()).contains(confirmationUserLink)
        }
    }

    private class TestContext {
        val receiverMail = "test@test.com"
        lateinit var walletOwner: String
        lateinit var project: ProjectResponse
        lateinit var organization: OrganizationResponse
    }
}
