package com.ampnet.mailservice.grpc

import com.ampnet.mailservice.proto.DepositInfoRequest
import com.ampnet.mailservice.proto.DepositRequest
import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
import com.ampnet.mailservice.proto.WithdrawInfoRequest
import com.ampnet.mailservice.proto.WithdrawRequest
import com.ampnet.mailservice.service.MailService
import io.grpc.stub.StreamObserver
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class GrpcMailServer(private val mailService: MailService) : MailServiceGrpc.MailServiceImplBase() {

    companion object : KLogging()

    override fun sendMailConfirmation(request: MailConfirmationRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendMailConfirmationRequest to: ${request.to}" }
        mailService.sendConfirmationMail(request.to, request.token)
        returnEmptyResponse(responseObserver)
    }

    override fun sendOrganizationInvitation(
        request: OrganizationInvitationRequest,
        responseObserver: StreamObserver<Empty>
    ) {
        logger.debug { "Received gRPC request SendOrganizationInvitationRequest to: ${request.to}" }
        mailService.sendOrganizationInvitationMail(request.to, request.organization)
        returnEmptyResponse(responseObserver)
    }

    override fun sendDepositRequest(request: DepositRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendDepositRequest to: ${request.to}" }
        mailService.sendDepositRequestMail(request.to, request.amount)
        returnEmptyResponse(responseObserver)
    }

    override fun sendDepositInfo(request: DepositInfoRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendDepositInfo to: ${request.to}" }
        mailService.sendDepositInfoMail(request.to, request.minted)
        returnEmptyResponse(responseObserver)
    }

    override fun sendWithdrawRequest(request: WithdrawRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendWithdrawRequest to: ${request.to}" }
        mailService.sendWithdrawRequestMail(request.to, request.amount)
        returnEmptyResponse(responseObserver)
    }

    override fun sendWithdrawInfo(request: WithdrawInfoRequest, responseObserver: StreamObserver<Empty>) {
        logger.debug { "Received gRPC request SendWithdrawInfo to: ${request.to}" }
        mailService.sendWithdrawInfoMail(request.to, request.burned)
        returnEmptyResponse(responseObserver)
    }

    private fun returnEmptyResponse(responseObserver: StreamObserver<Empty>) {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }
}
