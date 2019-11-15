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
    lateinit var confirmationBaseLink: String
    lateinit var resetPasswordBaseLink: String
    lateinit var organizationInvitationsLink: String
    var enabled: Boolean = false
}

@Suppress("MagicNumber")
class GrpcProperties {
    var userServiceTimeout: Long = 1000
}
