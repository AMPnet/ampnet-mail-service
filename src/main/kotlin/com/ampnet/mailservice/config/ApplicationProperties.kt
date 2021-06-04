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
    var confirmationPath: String = "auth/mail-confirmation"
    var resetPasswordPath: String = "auth/reset-password"
    var organizationPath: String = "dash/groups"
    var newWalletPath: String = "dash/activation"
    var manageWithdrawalsPath: String = "dash/manage_withdrawals"
    var walletActivatedPath: String = "dash/wallet"
    var projectPath: String = "dash/project"
    var offersPath: String = "offers"
    var enabled: Boolean = true
}

@Suppress("MagicNumber")
class GrpcProperties {
    var userServiceTimeout: Long = 10000
    var projectServiceTimeout: Long = 10000
    var walletServiceTimeout: Long = 10000
    var blockchainServiceTimeout: Long = 10000
}
