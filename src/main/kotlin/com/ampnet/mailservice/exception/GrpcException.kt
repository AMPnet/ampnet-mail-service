package com.ampnet.mailservice.exception

class GrpcException(exceptionMessage: String, throwable: Throwable) : Exception(exceptionMessage, throwable)

class ResourceNotFoundException(exceptionMessage: String, throwable: Throwable? = null) :
    Exception(exceptionMessage, throwable)

class InternalException(exceptionMessage: String) : Exception(exceptionMessage)
