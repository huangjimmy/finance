package sh.huang.finance.configuration

import io.ktor.client.*
import io.ktor.client.engine.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Configuration
class ApplicationConfiguration (val env: Environment) {

    @Bean
    fun httpClientEngine(): HttpClientEngine {
        return HttpClient().engine
    }

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient()
    }
}