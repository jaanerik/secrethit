package com.jaanerikpihel.secrethit

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebMvc
class SecrethitApplication : WebMvcAutoConfiguration()

fun main(args: Array<String>) {
	SpringApplication.run(SecrethitApplication::class.java, *args)
}
