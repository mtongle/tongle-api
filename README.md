# tongle-api

一个基于 Kotlin + Spring Boot 的多功能 API 服务，提供 QQ 信息查询和免费 AI API 代理两大核心功能。

## 🚀 功能特性

### 1. QQ 信息查询服务
- **双向查询**: 支持 QQ 号和手机号的互查功能
- **实时响应**: 通过 MySQL 数据库快速返回查询结果
- **安全验证**: 严格的参数验证和 SQL 注入防护
- **API 端点**: `GET /sedb/qq?type={qqid|phone}&value={查询值}`

**请求示例**:
```bash
# 通过 QQ 号查询手机号
curl "https://api.tongle.dev/sedb/qq?type=qqid&value=12345678"

# 通过手机号查询 QQ 号
curl "https://api.tongle.dev/sedb/qq?type=phone&value=13800138000"
```

### 2. DeepInfra AI API 免费代理服务
- **Tor 匿名网络**: 集成 Tor 网络实现 IP 自动轮换
- **智能重试**: 遇到 403 错误时自动切换 IP 重试（最多 5 次）
- **全方法支持**: 兼容 GET、POST、PUT、DELETE、PATCH 等 HTTP 方法
- **无缝代理**: 透明的请求/响应透传，保持原始 API 功能
- **API 端点**: `ALL METHODS /deepinfra/v1/**`
- **格式转换**: 自动去除请求中的Authorization头，适配DeepInfra免费Api

**请求示例**:
```bash
# 代理调用 DeepInfra 的模型列表
curl "https://api.tongle.dev/deepinfra/v1/models"

# 代理调用 AI 聊天完成接口
curl -X POST "https://api.tongle.dev/deepinfra/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek-ai/DeepSeek-V3.2-Exp",
    "messages": [{"role": "user", "content": "你好"}]
  }'
```

## 🛠 技术栈

- **Kotlin 2.2.0** - 现代化 JVM 语言
- **Spring Boot 3.5.4** - 企业级应用框架
- **Java 17** - LTS 运行环境
- **Gradle Kotlin DSL** - 构建工具
- **MySQL 8.0+** - 关系型数据库
- **OkHttp3** - 高性能 HTTP 客户端
- **Tor Network** - 匿名网络支持

## 🏗 项目架构

```
src/
├── main/kotlin/com/tongle/tongleapi/
│   ├── SEDBController.kt           # QQ 查询控制器
│   ├── deepInfra/                  # DeepInfra 代理模块
│   │   ├── ProxyService.kt         # 核心代理服务
│   │   ├── InfraController.kt      # API 路由控制器
│   │   ├── TorService.kt           # Tor 网络服务
│   │   └── HttpClientService.kt    # HTTP 客户端配置
│   └── exception/
│       └── GlobalExceptionHandler.kt # 全局异常处理
```

## 🚀 快速开始

### 环境要求
- Java 17+
- MySQL 8.0+
- Tor 服务（运行在本地）

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd tongle-api
```

2. **配置数据库**
```sql
-- 创建数据库和表
CREATE DATABASE socialdb;
USE socialdb;

CREATE TABLE qq8e (
    qq VARCHAR(15) PRIMARY KEY,
    phone VARCHAR(11) UNIQUE NOT NULL
);
```

3. **启动 Tor 服务**
```bash
# Ubuntu/Debian
sudo apt install tor
sudo systemctl start tor

# 或使用 Docker
docker run -d --name tor -p 9050:9050 -p 9051:9051 dperson/torproxy
```

4. **配置应用**
编辑 `src/main/resources/application.properties`，确保数据库和 Tor 配置正确。

5. **运行应用**
```bash
# 构建项目
./gradlew build

# 启动应用
./gradlew bootRun
```

应用将在 `http://localhost:7001` 启动。

## 📋 API 文档

### QQ 查询 API
```
GET /sedb/qq
```

**参数**:
- `type`: 查询类型（`qqid` 或 `phone`）
- `value`: 查询值（QQ 号或手机号）

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "qq": "12345678",
    "phone": "13800138000"
  }
}
```

### DeepInfra 代理 API
所有 DeepInfra API 的请求通过 `/deepinfra/v1/` 路径代理。

**示例**: 聊天完成 API
```bash
POST /deepinfra/v1/chat/completions
Host: localhost:7001
Content-Type: application/json
Authorization: Bearer your-deepinfra-token

{
  "model": "deepseek-ai/DeepSeek-V3.2-Exp",
  "messages": [
    {"role": "user", "content": "Hello"}
  ],
  "max_tokens": 100
}
```

## 🔧 配置说明

### 应用配置
```properties
# 应用端口
server.port=7001
spring.application.name=tongle-api

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/socialdb
spring.datasource.username=socialdb_admin
spring.datasource.password=your-password

# 日志级别
logging.level.com.tongle.tongleapi.deepInfra=DEBUG
```

### Tor 配置
- **控制端口**: 9051
- **SOCKS 代理端口**: 9050
- **认证密码**: `142857@Tor`
- **IP 切换延迟**: 10 秒

## 🧪 测试

运行所有测试：
```bash
./gradlew test
```

运行特定测试：
```bash
./gradlew test --tests "*deepInfraTorRequestTest"
```

## 📝 开发指南

### 添加新的代理端点
1. 在 `deepInfra/InfraController.kt` 中添加路由映射
2. 在 `deepInfra/ProxyService.kt` 中实现代理逻辑
3. 使用 `HttpClientService` 创建带 Tor 支持的 HTTP 客户端

### 数据库操作
1. 使用 `JdbcTemplate` 执行 SQL 查询
2. 实现参数化查询防止 SQL 注入
3. 使用 `ResponseData` 类统一响应格式

## ⚠️ 注意事项

- 生产环境请勿在代码中硬编码数据库密码和 Tor 认证信息
- 确保 Tor 服务正常运行才能使用 DeepInfra 代理功能
- 应用设计为开发/演示用途，生产部署前请进行安全评估
- QQ 查询功能依赖预先导入的数据库数据

## 📄 许可证

[添加许可证信息]

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
