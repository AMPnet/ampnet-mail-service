package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.grpc.blockchainservice.BlockchainService
import com.ampnet.mailservice.grpc.projectservice.ProjectService
import com.ampnet.mailservice.grpc.userservice.UserService
import com.ampnet.mailservice.grpc.walletservice.WalletService
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import com.ampnet.userservice.proto.CoopResponse
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
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
    protected lateinit var translationService: TranslationService

    @MockBean
    protected lateinit var fileService: FileService

    @MockBean
    protected lateinit var userService: UserService

    @MockBean
    protected lateinit var projectService: ProjectService

    @MockBean
    protected lateinit var walletService: WalletService

    @MockBean
    protected lateinit var blockchainService: BlockchainService

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
//            .setLanguage("el")
            .build()

    protected fun generateUserWithInfoResponse(email: String): UserWithInfoResponse =
        UserWithInfoResponse.newBuilder()
            .setUser(generateUserResponse(email))
            .setCoop(generateCoopResponse())
            .build()

    protected fun generateCoopResponse(): CoopResponse =
        CoopResponse.newBuilder()
            .setName(coop)
            .build()

    protected fun generateProjectResponse(createdBy: String): ProjectResponse =
        ProjectResponse.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setCreatedByUser(createdBy)
            .setName("Duimane Investement")
            .setExpectedFunding(1000L)
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
        val tosUrl = "tosUrl"
        lateinit var walletOwner: String
        lateinit var project: ProjectResponse
        lateinit var projectWithData: ProjectWithDataResponse
        lateinit var organization: OrganizationResponse
        lateinit var user: UserResponse
        lateinit var userWithInfo: UserWithInfoResponse
        lateinit var walletHash: String
        lateinit var wallet: WalletResponse
        lateinit var walletFrom: WalletResponse
        lateinit var walletTo: WalletResponse
    }

    protected fun verifyPdfFormat(data: ByteArray) {
        assertThat(data.isNotEmpty()).isTrue
        assertThat(data.size).isGreaterThan(4)

        // header
        assertThat(data[0]).isEqualTo(0x25) // %
        assertThat(data[1]).isEqualTo(0x50) // P
        assertThat(data[2]).isEqualTo(0x44) // D
        assertThat(data[3]).isEqualTo(0x46) // F
        assertThat(data[4]).isEqualTo(0x2D) // -

        // version is 1.3
        if (data[5].compareTo(0x31) == 0 && data[6].compareTo(0x2E) == 0 && data[7].compareTo(0x33) == 0) {
            // file terminator
            assertThat(data[data.size - 7]).isEqualTo(0x25) // %
            assertThat(data[data.size - 6]).isEqualTo(0x25) // %
            assertThat(data[data.size - 5]).isEqualTo(0x45) // E
            assertThat(data[data.size - 4]).isEqualTo(0x4F) // O
            assertThat(data[data.size - 3]).isEqualTo(0x46) // F
            assertThat(data[data.size - 2]).isEqualTo(0x20) // SPACE
            assertThat(data[data.size - 1]).isEqualTo(0x0A) // EOL
            return
        }

        // version is 1.4
        if (data[5].compareTo(0x31) == 0 && data[6].compareTo(0x2E) == 0 && data[7].compareTo(0x34) == 0) {
            // file terminator
            assertThat(data[data.size - 6]).isEqualTo(0x25) // %
            assertThat(data[data.size - 5]).isEqualTo(0x25) // %
            assertThat(data[data.size - 4]).isEqualTo(0x45) // E
            assertThat(data[data.size - 3]).isEqualTo(0x4F) // O
            assertThat(data[data.size - 2]).isEqualTo(0x46) // F
            assertThat(data[data.size - 1]).isEqualTo(0x0A) // EOL
            return
        }
        fail<String>("Unsupported file format")
    }
}
