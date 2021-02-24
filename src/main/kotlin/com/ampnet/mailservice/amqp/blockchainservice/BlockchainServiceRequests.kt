package com.ampnet.mailservice.amqp.blockchainservice

data class ProjectFullyFundedMessage(
    val txHash: String
)

data class SuccessfullyInvestedMessage(
    val userWalletTxHash: String,
    val projectWalletTxHash: String,
    val amount: String
)
