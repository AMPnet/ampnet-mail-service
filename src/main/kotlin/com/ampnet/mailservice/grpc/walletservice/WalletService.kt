package com.ampnet.mailservice.grpc.walletservice

import com.ampnet.walletservice.proto.WalletResponse
import java.util.UUID

interface WalletService {
    fun getWalletsByHash(hashes: Set<String>): List<WalletResponse>
    fun getWalletByHash(hash: String): WalletResponse
    fun getWalletsByOwner(uuids: List<UUID>): List<WalletResponse>
}
