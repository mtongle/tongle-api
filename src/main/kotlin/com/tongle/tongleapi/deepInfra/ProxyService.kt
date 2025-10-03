package com.tongle.tongleapi.deepInfra

import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.TimeUnit

@Service
class TorService {
    private val password = "142857@Tor"

    fun newIdentity() {
        Socket("127.0.0.1", 9051).use { socket ->
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = socket.getInputStream().bufferedReader()

            writer.println("AUTHENTICATE \"$password\"")
            writer.println("SIGNAL NEWNYM")
            val resp = reader.readLine()

            if (!resp.startsWith("250")) throw RuntimeException("Unexpected tor socket response: $resp")
            sleep(10000)
        }
    }

    @Bean
    fun torProxy() = Proxy(
        Proxy.Type.SOCKS,
        InetSocketAddress("127.0.0.1", 9050)
    )
}

@Service
class HttpClientService(
    @field:Autowired private val torProxy: Proxy
) {
    @Bean
    fun torHttpClient() = OkHttpClient().newBuilder()
        .proxy(torProxy)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}

@Service
class ProxyService(
    @field:Autowired private val torHttpClient: OkHttpClient,
    @field:Autowired private val torService: TorService,
) {
    val logger = LoggerFactory.getLogger(javaClass)

    val baseUrl = "https://api.deepinfra.com/v1/openai"

    private fun genUrl(path: String) = baseUrl + path

    fun request(method: String, headers: Map<String, String>, path: String, body: String?): Response {
        // 重试次数
        val maxRetry = 5
        var attempt = 0

        // 定义请求体
        val req = Request.Builder()
            .url(genUrl(path))
            .method(
                method = method,
                body = body?.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
            .headers(headers.toHeaders())
            .removeHeader("Authorization")
            .header("Host", "api.deepinfra.com")
            .build()

        // 开始重试循环
        var resp: Response
        while (true) {
            resp = torHttpClient.newCall(req).execute()

            // 事先读取响应流
            val body = resp.body.string()
            val code = resp.code
            val headers = resp.headers
            val protocol = resp.protocol
            val message = resp.message

            // 当获取到有用内容时返回
            if ( message != "Forbidden" ) {
                logger.debug("Response body: $body")
                return generateResponse(
                    headers = headers,
                    body = body,
                    code = code,
                    message = message,
                    protocol = protocol,
                    request = req
                )
            }

            attempt++

            // 处理重试超过指定次数
            if (attempt >= maxRetry) {
                val errorBody = "{\"detail\":{\"error\":\"Retry after $maxRetry attempts\"}\"}"
                return generateResponse(
                    headers = headers.newBuilder().set("Content-Length", body.length.toString()).build(),
                    body = errorBody,
                    code = 500,
                    message = "Internal Server Error",
                    protocol = protocol,
                    request = req
                )
            }

            // 更换tor出口ip
            torService.newIdentity()
        }
    }

    fun generateResponse(
        protocol: Protocol,
        headers: Headers,
        code: Int,
        message: String,
        body: String?,
        request: Request,
    ): Response {
        val responseBuilder = Response.Builder()
            .message(message)
            .code(code)
            .headers(headers)
            .protocol(protocol)
            .request(request)
        return body?.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())?.let {
            responseBuilder
                .body(it)
                .build()
        } ?: responseBuilder.build()
    }
}