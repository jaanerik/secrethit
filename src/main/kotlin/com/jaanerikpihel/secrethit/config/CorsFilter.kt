package com.jaanerikpihel.secrethit.config

import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class CorsFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080")
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000") // For development only
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "PUT, POST, GET, OPTIONS, DELETE, PATCH, HEAD")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Headers", "content-type, authorization, origin, x-requested-with, accept")
        if (request.method == OPTIONS) response.status = HttpServletResponse.SC_OK else filterChain.doFilter(request, response)
    }

    companion object {
        const val ORIGIN = "Origin"
        const val OPTIONS = "OPTIONS"
    }
}