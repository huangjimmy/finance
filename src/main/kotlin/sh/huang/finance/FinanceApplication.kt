package sh.huang.finance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FinanceApplication

fun main(args: Array<String>) {
	runApplication<FinanceApplication>(*args)
}
