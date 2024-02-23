package sh.huang.finance.configuration

import io.ktor.client.*
import io.ktor.client.engine.*
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment


@Configuration
class ApplicationConfiguration {

    @Bean
    fun httpClientEngine(): HttpClientEngine {
        return HttpClient().engine
    }

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient()
    }

    @Bean
    @Profile("test", "default")
    fun jooqConfigurationCustomizerHyersql(): DefaultConfigurationCustomizer {
        return DefaultConfigurationCustomizer { config ->
            config.setSettings(
                Settings().withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                    .withRenderNameCase(RenderNameCase.AS_IS)
            )
        }
    }
    @Bean
    @Profile("ci", "local", "prod")
    fun jooqConfigurationCustomizerPostgres(): DefaultConfigurationCustomizer {
        return DefaultConfigurationCustomizer { config ->
            config.setSettings(
                Settings().withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                    .withRenderNameCase(RenderNameCase.AS_IS)
            )
        }
    }
}