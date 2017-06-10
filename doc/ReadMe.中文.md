<div style='text-align:right;'><span>中文</span> | <a href='/doc/ReadMe.Chinglish.md'>Chinglish</a></div>

----

# “睦邻音量控制器”是什么 #
鲁迅说过：
> 单身久了，就容易搞这种寻找存在感的东西…

*我没说过！* -- 鲁迅

**“睦邻音量控制器”让您的邻居调节你的电脑的播放音量的 Web 应用程序。**

如果你的电脑运行 linux 并且使用 PulseAudio 作为声音服务器，那么当你的电脑音量开的比较大（或者比较小），邻居抱怨时，则邻居可以通过该 Web 网页对你的电脑的播放音量进行调节。

一般来说，建议配合运行 OpenWRT 或者 DD-WRT 或者任何能够搭建 Captive Portal 的固件的路由器使用：

* 在路由器中创建一个虚拟无线网络接口
* 在路由器中配置一个 Captive Portal，并让该 Portal 服务上面创建的虚拟无线网络接口，且在 Captive Portal 中允许任意 IP 访问你的电脑（默认 80 以及 8080 端口，这取决于 Tomcat 或类似服务器所采用的端口）
* Captive Portal 的页面直接嵌入一个 iframe，该 iframe 是你的电脑上的本 Web 应用程序主页面。
* 邻居在连入 SSID 后就可以控制你的电脑的音量，或者进行留言反馈。


# 安装与配置 #
## 安装 ##
### 在你的电脑上安装依赖的软件包 ###
* 首先这个音量控制器是需要调用 PulseAudio 的 `pactl` 命令的，所以必须安装 `pulseaudio-utils` 包（不同的 linux 品牌可能所用的包名也不同，安装相应的包即可）
* 安装 tomcat
* 安装 apache-commons-lang3
* 把安装后 apache-commons-lang3 带的 .jar 文件复制到 tomcat 的 `lib` 文件夹下

### 在你的电脑上安装本程序 ###
* `git clone https://github.com/moontide/HarmonyNeighborVolumeController`
* 将 `HarmonyNeighborVolumeController` 文件夹下的 `web - jsp on computer` 复制到 tomcat 的 `webapps` 文件夹下，并改名为一个你喜欢的名字，比如 `HarmonyNeighborVolumeController`、 `nvc` 什么的…

### 在路由器上安装你熟悉的 Captive Portal 软件 （可选操作，但强烈建议安装） ###
这一步是可选的，但我强烈建议安装 Captive Portal，**因为 WiFi 信号有传输距离限制，该限制通常跟高音量能影响到的距离有一定关系**。
所以可以利用该限制，达到“**只有路由器附近的 WiFi 终端设备（比如手机）能调节你的电脑音量**”的目的。

这里只用运行 OpenWRT 的路由器、用 nodogsplash 做一下简单说明，其他的 （比如 DD-WRT、wifidog 等需要自己去探索）

* 进入到路由器的 shell
* 执行 `opkg update && opkg install nodogsplash`
* 执行 `/etc/init.d/nodogsplash enable`
* TODO

## 配置 ##
### 配置你的电脑 ###
* 将前面刚刚复制后的文件夹下的 `config.template.jsp` 复制为 `config.jsp`，并根据自己电脑的实际情况修改一下里面的参数。可用 `pactl list sinks` 来确定要控制的声卡是哪一个，默认是 `0`，即：第一个声卡。

### 配置路由器 ###
TODO
