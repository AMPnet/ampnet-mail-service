package com.ampnet.mailservice.grpc.walletservice

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.GrpcException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.walletservice.proto.GetWalletsByHashRequest
import com.ampnet.walletservice.proto.GetWalletsByOwnerRequest
import com.ampnet.walletservice.proto.WalletResponse
import com.ampnet.walletservice.proto.WalletServiceGrpc
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class WalletServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : WalletService {

    companion object : KLogging()

    private val walletServiceStub: WalletServiceGrpc.WalletServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("wallet-service")
        WalletServiceGrpc.newBlockingStub(channel)
    }

    override fun getWalletsByHash(hashes: Set<String>): List<WalletResponse> {
        logger.debug { "Fetching wallets by hashes: $hashes" }
        try {
            val request = GetWalletsByHashRequest.newBuilder()
                .addAllHashes(hashes)
                .build()
            val response = serviceWithTimeout()
                .getWalletsByHash(request).walletsList
            logger.debug { "Fetched wallets: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Failed to fetch wallets. ${ex.localizedMessage}", ex)
        }
    }

    override fun getWalletsByOwner(uuids: List<UUID>): List<WalletResponse> {
        logger.debug { "Fetching wallets for owners: $uuids" }
        try {
            val request = GetWalletsByOwnerRequest.newBuilder()
                .addAllOwnersUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout()
                .getWalletsByOwner(request).walletsList
            logger.debug { "Fetched wallets: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Failed to fetch wallets. ${ex.localizedMessage}", ex)
        }
    }

    @Throws(ResourceNotFoundException::class)
    override fun getWalletByHash(hash: String): WalletResponse =
        getWalletsByHash(setOf(hash)).firstOrNull() ?: throw ResourceNotFoundException("Missing wallet: $hash")

    private fun serviceWithTimeout() = walletServiceStub
        .withDeadlineAfter(applicationProperties.grpc.walletServiceTimeout, TimeUnit.MILLISECONDS)
}
