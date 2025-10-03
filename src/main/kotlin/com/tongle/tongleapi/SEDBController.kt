package com.tongle.tongleapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory
import jakarta.validation.constraints.NotBlank

@RestController
@RequestMapping("/sedb/")
class SEDBController(@param:Autowired val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(SEDBController::class.java)
    private val tableName = "qq8e"

    /**
     * 统一查询接口 - 根据QQ号或手机号查询对应信息
     * @param type 查询类型: "qq"表示根据QQ号查询手机号, "phone"表示根据手机号查询QQ号
     * @param value 查询值: QQ号或手机号
     */
    @GetMapping("/qq")
    fun qqSearch(
        @RequestParam 
        @NotBlank(message = "查询类型不能为空")
        type: String,
        
        @RequestParam 
        @NotBlank(message = "查询值不能为空")
        value: String
    ): ResponseEntity<out Any?> {
        logger.info("执行查询: type={}, value={}", type, value)
        
        // 验证表名防止SQL注入
        if (!isValidTableName(tableName)) {
            logger.error("无效的表名: {}", tableName)
            return ResponseEntity.badRequest().build()
        }
        
        return when (type) {
            "qqid" -> {
                // 验证QQ号格式
                if (!value.matches(Regex("^\\d{5,15}$"))) {
                    logger.warn("QQ号格式错误: value={}", value)
                    ResponseEntity.badRequest().body(ResponseData(false, "QQ号格式不正确", null))
                } else {
                    searchPhoneByQQ(value)
                }
            }
            "phone" -> {
                // 验证手机号格式
                if (!value.matches(Regex("^\\d{11}$"))) {
                    logger.warn("手机号格式错误: value={}", value)
                    ResponseEntity.badRequest().body(ResponseData(false, "手机号格式不正确", null))
                } else {
                    searchQQByPhone(value)
                }
            }
            else -> {
                logger.warn("不支持的查询类型: type={}", type)
                ResponseEntity.badRequest().body(ResponseData(false, "不支持的查询类型，仅支持'qqid'或'phone'", null))
            }
        }
    }
    
    /**
     * 根据QQ号查询手机号
     */
    private fun searchPhoneByQQ(qqid: String): ResponseEntity<out Any?> {
//        logger.info("根据QQ号查询手机号: qqid={}", qqid)
        
        val sql = "SELECT phone FROM $tableName WHERE qq = ?"
        
        return try {
            val phone = jdbcTemplate.queryForObject(sql, Long::class.java, qqid.toLong())
            if (phone != null) {
                logger.info("查询成功: qqid={} 对应的手机号={}", qqid, phone)
                ResponseEntity.ok(ResponseData(true, "查询成功", phone))
            } else {
                logger.info("未找到记录: qqid={}", qqid)
                ResponseEntity.ok(ResponseData(false, "未找到对应的手机号", null))
            }
        } catch (e: EmptyResultDataAccessException) {
            logger.info("未找到记录: qqid={}, 错误={}", qqid, e.message)
            ResponseEntity.ok(ResponseData(false, "未找到对应的手机号", null))
        } catch (e: NumberFormatException) {
            logger.warn("QQ号格式错误: qqid={}, 错误={}", qqid, e.message)
            ResponseEntity.badRequest().body(ResponseData(false, "QQ号格式错误", null))
        } catch (e: Exception) {
            logger.error("查询手机号时发生错误: qqid={}, 错误={}", qqid, e.message, e)
            ResponseEntity.status(500).body(ResponseData(false, "服务器内部错误", null))
        }
    }

    /**
     * 根据手机号查询QQ号
     */
    private fun searchQQByPhone(phone: String): ResponseEntity<out Any?> {
//        logger.info("根据手机号查询QQ号: phone={}", phone)
        
        val sql = "SELECT qq FROM $tableName WHERE phone = ?"
        
        return try {
            val qqid = jdbcTemplate.queryForObject(sql, Long::class.java, phone.toLong())
            if (qqid != null) {
                logger.info("查询成功: phone={} 对应的QQ号={}", phone, qqid)
                ResponseEntity.ok(ResponseData(true, "查询成功", qqid))
            } else {
                logger.info("未找到记录: phone={}", phone)
                ResponseEntity.ok(ResponseData(false, "未找到对应的QQ号", null))
            }
        } catch (e: EmptyResultDataAccessException) {
            logger.info("未找到记录: phone={}, 错误={}", phone, e.message)
            ResponseEntity.ok(ResponseData(false, "未找到对应的QQ号", null))
        } catch (e: NumberFormatException) {
            logger.warn("手机号格式错误: phone={}, 错误={}", phone, e.message)
            ResponseEntity.badRequest().body(ResponseData(false, "手机号格式错误", null))
        } catch (e: Exception) {
            logger.error("查询QQ号时发生错误: phone={}, 错误={}", phone, e.message, e)
            ResponseEntity.status(500).body(ResponseData(false, "服务器内部错误", null))
        }
    }
    
    /**
     * 验证表名防止SQL注入
     */
    private fun isValidTableName(tableName: String): Boolean {
        // 只允许字母、数字和下划线组成的表名
        return tableName.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
    
    /**
     * 统一响应数据结构
     */
    data class ResponseData<T>(
        val success: Boolean,
        val message: String,
        val data: T?
    )
}