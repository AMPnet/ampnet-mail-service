package com.ampnet.mailservice.grpc.walletservice

import com.ampnet.walletservice.proto.WalletResponse

interface WalletService {
    fun getWalletsByHash(hashes: Set<String>): List<WalletResponse>
    fun getWalletByHash(hash: String): WalletResponse
}
