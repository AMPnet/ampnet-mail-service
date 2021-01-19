package com.ampnet.mailservice.exception

class GrpcException(exceptionMessage: String, throwable: Throwable) : Exception(exceptionMessage, throwable)

class ResourceNotFoundException(exceptionMessage: String) : Exception(exceptionMessage)

class InternalException(exceptionMessage: String) : Exception(exceptionMessage)
