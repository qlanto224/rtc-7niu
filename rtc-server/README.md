# RTC-DEMO的springboot后台

> 1.  websocket初始化配置
>
> 2.  websocket对msg的处理
>
> 3.  视图层controller,生成roomtoken

# 采用技术
> 1. springboot 2.1.3.RELEASE
>
> 2. websocket
>
> 3. maven
>
>4. jdk8  推荐使用openjdk8

# 修改指南

> 1.  application.yml 配置自己的七牛云rtc accessKey和secretKey
>
> 2.  根据自己需求修改com.ql.webrtc.controller.WebSocket.WebSocketController,WebSocketController是关于socket的消息处理
>
> 3.  根据自己需求修改com.ql.webrtc.controller.WebSocket.UserSigController,UserSigController是关于七牛云后台生成roomtoken

# 启动

```
安装maven依赖
```
```
启动QMediaApplication
```
