package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.grpc.walletservice.WalletService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.subethamail.wiser.Wiser
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest
@EnableConfigurationProperties
@Import(JavaMailSenderImpl::class)
abstract class MailServiceTestBase : TestBase() {

    @Autowired
    protected lateinit var mailSender: JavaMailSenderImpl

    @Autowired
    protected lateinit var linkResolverService: LinkResolverService

    @Autowired
    protected lateinit var applicationProperties: ApplicationProperties

    @Autowired
    protected lateinit var templateTranslationService: TemplateTranslationService

    @MockBean
    protected lateinit var userService: UserService

    @MockBean
    protected lateinit var projectService: ProjectService

    @MockBean
    protected lateinit var walletService: WalletService

    protected lateinit var wiser: Wiser
    protected var defaultMailPort: Int = 0
    protected val testContext = TestContext()
    protected val coop = "ampnet-test"
    protected val activationData = "activation data"
    protected val confirmationMailSubject = "Confirm your email"
    protected val resetPasswordSubject = "Reset password"
    protected val invitationMailSubject = "Invitation"
    protected val depositSubject = "Deposit"
    protected val withdrawSubject = "Withdraw"
    protected val newWalletSubject = "New wallet created"
    protected val manageWithdrawalsSubject = "New withdrawal request"
    protected val walletActivatedSubject = "Wallet activated"
    protected val projectFullyFundedSubject = "Project is fully funded"
    protected val investmentSubject = "Investment"

    @BeforeEach
    fun init() {
        defaultMailPort = mailSender.port
        wiser = Wiser(0)
        wiser.start()
        mailSender.port = wiser.server.port
    }

    @AfterEach
    fun tearDown() {
        wiser.stop()
        mailSender.port = defaultMailPort
    }

    protected fun generateUserResponse(email: String): UserResponse =
        UserResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setEmail(email)
            .setEnabled(true)
            .setFirstName("First")
            .setLastName("Last")
            .setCoop(coop)
            // .setLanguage("el")
            .build()

    protected fun generateProjectResponse(createdBy: String): ProjectResponse =
        ProjectResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setCreatedByUser(createdBy)
            .setName("Duimane Investement")
            .setOrganizationUuid(UUID.randomUUID().toString())
            .build()

    protected fun generateOrganizationResponse(createdBy: String): OrganizationResponse =
        OrganizationResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setCreatedByUser(createdBy)
            .setName("Duimane Investement Organization")
            .build()

    protected fun generateWalletResponse(owner: String): WalletResponse =
        WalletResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setHash(UUID.randomUUID().toString())
            .setOwner(owner)
            .build()

    protected class TestContext {
        val receiverMail = "test@test.com"
        val tokenIssuerMail = "demoadmin@ampnet.io"
        val token = "test-token"
        val organizationName = "Organization test"
        val amount = 100L
        val receiverEmails = listOf("test@test.com", "test2@test.com")
        val coop = "ampnet-test"
        lateinit var walletOwner: String
        lateinit var project: ProjectResponse
        lateinit var projectWithData: ProjectWithDataResponse
        lateinit var organization: OrganizationResponse
        lateinit var user: UserResponse
        lateinit var walletHash: String
        lateinit var wallet: WalletResponse
        lateinit var walletFrom: WalletResponse
        lateinit var walletTo: WalletResponse
    }
}
