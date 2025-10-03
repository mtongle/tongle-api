package com.tongle.tongleapi.deepInfra

import jakarta.servlet.http.HttpServletRequest
import okhttp3.Headers
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@Controller
@RequestMapping("/deepinfra/v1")
class InfraController(@field:Autowired val proxyService: ProxyService) {

    private val logger = LoggerFactory.getLogger(InfraController::class.java)

    @RequestMapping(
        value = ["/**"],
        method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH]
    )
    fun proxy(
        request: HttpServletRequest,
        @RequestHeader headers: Map<String, String>,
        @RequestBody(required = false) body: String?,
    ): ResponseEntity<String?> {

        // 从请求URI中提取完整的端点路径
        val requestURI = request.requestURI
        val endpoint = requestURI.removePrefix("/deepinfra/v1")
        logger.info("Original URI: $requestURI")
        logger.info("Extracted endpoint: $endpoint")
        logger.info("Headers: $headers")

        // 向deepinfra发送请求
        val resp = proxyService.request(
            method = request.method,
            path = endpoint,
            headers = headers,
            body = body
        )
        logger.debug("Responded with resp: {}", resp)

        // 读取响应流
        val body = resp.body.string()
        val code = resp.code
        val headers = resp.headers

        return ResponseEntity.status(code)
            .headers(getHttpHeaders(headers))
            .body(body)
    }

    private fun getHttpHeaders(headers: Headers): HttpHeaders {
        val httpHeaders = HttpHeaders()
        headers.forEach { (key, value) ->
            httpHeaders.add(key, value)
        }
        return httpHeaders
    }

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Server is on")
    }
}