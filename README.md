# demo-rtc-server-record

======================

## 项目描述  

- 该demo集成的功能包括会场同步和服务器录像，使用前请在开发者后台开通会场同步和服务器录像能力  

1. 会场同步。在官网开通会场同步时，填写了会场同步地址，融云将会以http请求的方式将会场状态同步给此地址，demo来处理发过来的会场状态的请求。  
2. 服务器录像。demo将资源订阅相关请求发送给录像节点，录像节点再进行相关的资源订阅操作。

## 快速Demo体验  

- 运行环境  
  Java 8+  
  能够被外网访问,用于接收融云的会场通知  
- 基于源码  Maven 打包构建  

1. 下载或克隆demo-rtc-server-record  
2. 进入项目 demo-rtc-server-record 目录  
3. 安装依赖 mvn install  
4. 打包 mvn clean package  

- 会场同步   

1. 创建Demo运行目录,并进入该目录  

2. 将maven打包好的可执行jar包demo-rtc-server-record-*.jar复制到当前目录  

3. 将项目源码根目录中的ServiceSettings.properties,log4j.properties复制到当前目录  

4. 配置ServiceSettings.properties  
   #替换为自己的appKey  
   appKey=  
   #替换为自己的secret  
   secret= 

   #服务监听的端口
   port=8800 

   #录像节点的通知地址（如果开启录像功能，需要配置）。
   recordNodeAddr=http://127.0.0.1:5000/record

   #录像模式（如果开启录像功能，需要配置）
   #1：自动全录模式；2：自定义异步录像（提供http接口，调用开始录像和结束录像），默认为1；
   recordType=1

5. 启动demo  
   nohup java -jar demo-rtc-server-record-*.jar &  

6. 验证，观察日志nohup.out，无报错，当有会场状态变时能收到请求  

- 服务器录像  

1. 会场同步已经调试成功  

2. 下载录制程序

3. 部署录像服务

   步骤：

   centos系统：

   1. 以 root 身份执行 `centos-record_install.sh`
   2. 将 `centos-record_install.sh` 加入到守候进程并启动

   ubantu系统：

   1. 以 root 身份执行 `ubuntu-record_install.sh`
   2. 将 `ubuntu-record_install.sh` 加入到守候进程并启动

## 重要类  

- 会场同步   

```
cn.rongcloud.rtc.channel

ChannelEventListener  会场事件监听接口  
ChannelSyncController 接收会场同步http请求入口  
ChannelManager        会场状态管理类，负责分发event到各个ChannelEventListener  
```

- 服务器录像  

```
cn.rongcloud.rtc.recorder

RecordManager         录像管理类，实现了ChannelEventListener接口   
CustomRecordController   自定义录像页面入口
```

##### 录像模式选择：

server支持两种录像模式（在ServiceSettings.properties配置recordType）：

1，自动全录模式，这种方式是会场建立后，server就自动开始录像，录像的文件名按照默认命名规则。

2，自定义异步录像，这种录像模式，服务器默认不录像，需要录像的会场依靠调用API接口启动、停止录像； 满足客户根据需要动态指定录制会场以及远程调用启动录像服务需要。接口的参数有userId,channelId两个参数，其中userid和channelId至少需要传一个，来找到对应需要录制的房间。

##### 异步开启录像

URL

```
POST /customrecord/start
```

body格式: json

```json
{
    "userId": "57a53c03-a1ca-d4f6-1a25-18d8c26ea488",//会场内用户的id
    "channelId": "133222323llj",//会场id
}

```

Response

```json
{
  "code":200,
  "msg":"OK"
}
```

##### 异步关闭录像

URL

```
POST /customrecord/stop
```

body格式: json

```json
{
    "userId": "57a53c03-a1ca-d4f6-1a25-18d8c26ea488",//会场内用户的id
    "channelId": "133222323llj" //会场id
}

```

Response

```json
{
  "code":200,
  "msg":"OK"
}
```

