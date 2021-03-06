package com.ampnet.mailservice.grpc

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcInterceptorConfig {

    @Bean
    fun globalInterceptorConfigurerAdapter(): GlobalClientInterceptorConfigurer {
        return GlobalClientInterceptorConfigurer { registry ->
            registry.add(GrpcLogInterceptor())
        }
    }
}
