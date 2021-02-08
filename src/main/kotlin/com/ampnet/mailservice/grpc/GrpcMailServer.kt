package com.ampnet.mailservice.grpc

import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.ProjectFullyFundedRequest
import com.ampnet.mailservice.proto.SuccessfullyInvestedRequest
import com.ampnet.mailservice.service.UserMailService
import io.grpc.stub.StreamObserver
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class GrpcMailServer(
    private val userMailService: UserMailService
) : MailServiceGrpc.MailServiceImplBase() {

    companion object : KLogging()

    override fun sendProjectFullyFunded(request: ProjectFullyFundedRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request sendProjectFullyFunded for wallet: ${request.walletHash}" }
        userMailService.sendProjectFullyFundedMail(request.walletHash)
        returnSuccessfulResponse(responseObserver)
    }

    override fun sendSuccessfullyInvested(
        request: SuccessfullyInvestedRequest,
        responseObserver: StreamObserver<Empty>
    ) {
        logger.debug {
            "Received gRPC request sendSuccessfullyInvested to wallet: " +
                "${request.walletHashTo} by wallet: ${request.walletHashFrom} in amount: ${request.amount}"
        }
        userMailService.sendSuccessfullyInvested(request)
        returnSuccessfulResponse(responseObserver)
    }

    private fun returnSuccessfulResponse(responseObserver: StreamObserver<Empty>) {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }
}
