package com.ampnet.mailservice.grpc

import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.OrganizationInvitationRequest
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
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }

    override fun sendOrganizationInvitation(
        request: OrganizationInvitationRequest,
        responseObserver: StreamObserver<Empty>
    ) {
        logger.debug { "Received gRPC request SendOrganizationInvitationRequest to: ${request.to}" }

        mailService.sendOrganizationInvitationMail(request.to, request.organization)
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }
}
