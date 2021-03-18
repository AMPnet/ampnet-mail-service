package com.ampnet.mailservice.grpc.blockchainservice

import com.ampnet.crowdfunding.proto.BalanceRequest
import com.ampnet.crowdfunding.proto.BlockchainServiceGrpc
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.GrpcException
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class BlockchainServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : BlockchainService {

    companion object : KLogging()

    private val serviceBlockingStub: BlockchainServiceGrpc.BlockchainServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("blockchain-service")
        BlockchainServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getBalance(hash: String): Long? {
        logger.debug { "Fetching balance for hash: $hash" }
        return try {
            val response = serviceWithTimeout()
                .getBalance(
                    BalanceRequest.newBuilder()
                        .setWalletTxHash(hash)
                        .build()
                )
            logger.info { "Received response: $response for hash: $hash" }
            response.balance.toLongOrNull()
        } catch (ex: StatusRuntimeException) {
            logger.warn("Could not get balance for wallet: $hash", ex)
            null
        }
    }

    private fun serviceWithTimeout() = serviceBlockingStub
        .withDeadlineAfter(applicationProperties.grpc.blockchainServiceTimeout, TimeUnit.MILLISECONDS)
}
