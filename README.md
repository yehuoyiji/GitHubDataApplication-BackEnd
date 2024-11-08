[前端代码仓库地址](https://github.com/yehuoyiji/GithubDataApplication-FrontEnd)
# 程序运行说明
## 项目名称：GitHub 数据应用
### 环境要求
- **数据库：** MySQL
- **缓存：** Redis 5.0.10
- **JDK：** Java 11
- **Node.js：** v18.16.0
- SpringBoot2
- Vue3 + TS

### 运行步骤
1. 配置数据库连接：编辑`application.yaml`文件，配置数据库和 Redis 地址。
2. 配置 GitHub API token，GitHub 主页 -> Developer Setting -> Personal access token。生成token，并通过 Jasypt 进行加密。
3. 启动后端项目
4. 前端项目 npm run serve 执行启动，访问 localhost:8080 端口即可。

# 架构设计文档
## 后端流程图
![GitHub数据应用流程图](https://github.com/user-attachments/assets/fb5016d0-cab5-4bc5-bf8b-f67a17257ff5)
## 系统模块设计
### 后端：
1. **获取个人数据模块**
   - 通过前端发送开发者用户名，后端通过用户名获取开发者基础信息，并存入缓存，设置5min缓存时长。
2. **区域/领域获取开发者列表模块**
   - 根据前端发送区域(China, America, ...)以及领域(Java, Python, ...)等参数，获取开发者列表。
   - 根据开发者列表，通过CompletableFuture实现多线程并行获取开发者仓库信息实现评分机制。
3. **置信度计算开发者分数模块**
   - 主要通过判断仓库是否是原创仓库，仓库活跃度，语言多样性，以及描述中的关键字来进行基础分数计算后，通过置信度以及第一次计算分数实现二次计算得到最终分数。
4. **接口限流及数据缓存模块**
   - 通过编写令牌桶限流算法并注册拦截器实现接口限流，通过Redis缓存机制，有效防止热点数据访问过高，降低接口压力，提高用户体验。     
### 前端：
1. **个人数据页：**
   - 针对开发者的关系网(关注，粉丝)计算出开发者地理位置，基本数据的渲染，评分机制的渲染。
2. **全球页：**
   - 通过局域(国家)，领域(语言)实现对全球范围内的GitHub开发者进行筛选，并通过评分机制进行渲染。
3. **通用模块：**
   - 实现黑夜/白天主题的切换。

# 演示视频Demo
[演示视频Demo位置](https://github.com/yehuoyiji/GitHubDataApplication-BackEnd/blob/master/src/main/resources/static/%E6%BC%94%E7%A4%BADemo.mp4)
> 提示：需要点击view raw下载视频才能观看
