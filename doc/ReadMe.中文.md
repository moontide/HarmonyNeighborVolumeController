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
