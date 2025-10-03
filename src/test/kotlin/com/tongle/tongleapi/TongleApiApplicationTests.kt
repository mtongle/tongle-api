package com.tongle.tongleapi

import com.tongle.tongleapi.deepInfra.ProxyService
import com.tongle.tongleapi.deepInfra.TorService
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import kotlin.test.asserter

@SpringBootTest
class TongleApiApplicationTests {
    private val logger = LoggerFactory.getLogger(TongleApiApplicationTests::class.java)

    @Test
    fun deepInfraTorRequestTest(@Autowired proxyService: ProxyService) {
        val endpoint = "/models"
        val resp = proxyService.request(
            method = HttpMethod.GET.toString(),
            path = endpoint,
            headers = mapOf(),
            body = null
        )
        logger.info("Response: $resp")
        asserter.assertNotNull("/models response body is null", resp.body)
    }

    @Test
    fun torChangeRequestTest(@Autowired torService: TorService) {
        torService.newIdentity()
    }

    @Test
    fun deepInfraGenerateRequestTest(@Autowired proxyService: ProxyService) {
        val endpoint = "/chat/completions"
        val resp = proxyService.request(
            method = HttpMethod.POST.toString(),
            path = endpoint,
            headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer testandtest"
            ),
            body = """
                {
                "model": "deepseek-ai/DeepSeek-V3.2-Exp",
                "messages": [
                  {
                    "role": "user",
                    "content": "Hello!"
                  }
                ]
              }
            """.trimIndent()
        )
        logger.info("Response: $resp")
        asserter.assertNotNull("/chat/completions response body is null", resp.body)
    }

}
