package com.ampnet.mailservice.grpc.blockchainservice

interface BlockchainService {
    fun getBalance(hash: String): Long?
}
