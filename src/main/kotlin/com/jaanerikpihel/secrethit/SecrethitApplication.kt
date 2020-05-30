package com.jaanerikpihel.secrethit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication
@EnableWebMvc
open class SecrethitApplication : WebMvcAutoConfiguration() {

    @Bean
    open fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                        .addMapping("/**")
						.allowedOrigins("http://localhost:3000", "ws://localhost:3000")
						.allowedMethods("GET", "POST", "PUT", "DELETE")
            }
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(SecrethitApplication::class.java, *args)

}
