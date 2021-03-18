package com.ampnet.mailservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.ampnet.mailservice")
class ApplicationProperties {
    val mail: MailProperties = MailProperties()
    val grpc: GrpcProperties = GrpcProperties()
}

class MailProperties {
    lateinit var sender: String
    lateinit var baseUrl: String
    var confirmationPath: String = "mail-confirmation"
    var resetPasswordPath: String = "reset-password"
    var manageOrganizationPath: String = "dash/manage_groups"
    var newWalletPath: String = "dash/activation"
    var manageWithdrawalsPath: String = "dash/manage_withdrawals"
    var walletActivatedPath: String = "dash/wallet"
    var manageProjectPath: String = "manage_project"
    var overviewPath: String = "overview"
    var enabled: Boolean = false
}

@Suppress("MagicNumber")
class GrpcProperties {
    var userServiceTimeout: Long = 10000
    var projectServiceTimeout: Long = 10000
    var walletServiceTimeout: Long = 10000
    var blockchainServiceTimeout: Long = 10000
}
