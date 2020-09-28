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
    var confirmationLink: String = "mail-confirmation"
    var resetPasswordLink: String = "reset-password"
    var organizationInvitationsLink: String = "dash/manage_groups"
    var newWalletLink: String = "dash/activation"
    var manageWithdrawalsLink: String = "dash/manage_withdrawals"
    var enabled: Boolean = false
}

@Suppress("MagicNumber")
class GrpcProperties {
    var userServiceTimeout: Long = 1000
}
