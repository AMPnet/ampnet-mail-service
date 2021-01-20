package com.ampnet.mailservice.grpc.projectservice

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.exception.GrpcException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.projectservice.proto.GetByUuid
import com.ampnet.projectservice.proto.GetByUuids
import com.ampnet.projectservice.proto.OrganizationResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.projectservice.proto.ProjectServiceGrpc
import com.ampnet.projectservice.proto.ProjectWithDataResponse
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class ProjectServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : ProjectService {

    companion object : KLogging()

    private val serviceBlockingStub: ProjectServiceGrpc.ProjectServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("project-service")
        ProjectServiceGrpc.newBlockingStub(channel)
    }

    @Throws(ResourceNotFoundException::class)
    override fun getOrganization(uuid: UUID): OrganizationResponse {
        return getOrganizations(listOf(uuid)).firstOrNull()
            ?: throw ResourceNotFoundException("Missing organization: $uuid")
    }

    @Throws(ResourceNotFoundException::class)
    override fun getProject(uuid: UUID): ProjectResponse {
        return getProjects(listOf(uuid)).firstOrNull()
            ?: throw ResourceNotFoundException("Missing project: $uuid")
    }

    @Throws(ResourceNotFoundException::class)
    override fun getProjectWithData(uuid: UUID): ProjectWithDataResponse {
        logger.debug { "Fetching project with data: $uuid" }
        val request = GetByUuid.newBuilder()
            .setProjectUuid(uuid.toString())
            .build()
        val response = serviceWithTimeout().getProjectWithData(request)
        logger.debug { "Fetched project with data: $response" }
        return response
    }

    override fun getOrganizations(uuids: Iterable<UUID>): List<OrganizationResponse> {
        logger.debug { "Fetching organizations: $uuids" }
        if (uuids.none()) {
            return emptyList()
        }
        try {
            val request = GetByUuids.newBuilder()
                .addAllUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout().getOrganizations(request).organizationsList
            logger.debug { "Fetched organizations: ${response.map { it.uuid }}" }
            return response
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Failed to fetch organizations. ${ex.localizedMessage}", ex)
        }
    }

    override fun getProjects(uuids: Iterable<UUID>): List<ProjectResponse> {
        logger.debug { "Fetching projects: $uuids" }
        if (uuids.none()) {
            return emptyList()
        }
        try {
            val request = GetByUuids.newBuilder()
                .addAllUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout().getProjects(request).projectsList
            logger.debug { "Fetched projects: ${response.map { it.uuid }}" }
            return response.map { it }
        } catch (ex: StatusRuntimeException) {
            throw GrpcException("Failed to fetch projects. ${ex.localizedMessage}", ex)
        }
    }

    private fun serviceWithTimeout() = serviceBlockingStub
        .withDeadlineAfter(applicationProperties.grpc.projectServiceTimeout, TimeUnit.MILLISECONDS)
}
