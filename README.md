# TranscriptionWidget简介

声网 字幕 控件(TranscriptionWidget)支持显示RTT的转写和翻译内容显示。

## 运行demo

1. 下载代码
2. 打开Android Studio
3. 打开项目
4. 配置项目
   将 `app/src/main/assets/server_config_example.json` 改名为 `app/src/main/assets/server_config.json` , 并正确填写参数
   ```
    [
    {
    "name": "test",
    "serverUrl": "http://1.1.1.1",
    "testIp": "1.1.1.1",
    "testPort": 1,
    "appId": "xxxxxxxxx",
    "appCert": ""
    }
    ]
   ```
5. 运行项目



## 集成 TranscriptionWidget 控件

### Maven 方式

```
dependencies {
    ...
    implementation("io.github.winskyan:Agora-TranscriptionWidget:0.0.1")
}
```

### 源代码模式

参考如下步骤，在App添加 TranscriptionWidget 控件：

将该项目下的 `transcription-widget` 文件夹拷贝至你的项目文件夹下。

在你的项目中引入 `transcription-widget` 控件。

打开项目的 `settings.gradle` 文件，添加如下代码：

```
include ':transcription-widget'
```

在你的项目中添加 `transcription-widget` 控件的依赖项。打开项目的 `app/build.gradle` 文件，添加如下代码：

```
dependencies {
    ...
    implementation project(':transcription-widget')
}
```

## 使用 TranscriptSubtitleView 控件对象

在项目的 xml/layout 资源文件中定义，示例代码如下：

```xml
<io.agora.transcription_widget.TranscriptSubtitleView
        android:id="@+id/transcript_subtitle_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginStart="10dp"
        android:layout_marginEnd="10dp"
        android:layout_marginBottom="20dp"
        app:layout_constraintBottom_toTopOf="@id/all_transcript_btn"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        //final 状态字体颜色
        app:finalTextColor="@color/colorAccent"
        //non final 状态字体颜色
        app:nonFinalTextColor="@color/orange"
        //item背景颜色
        app:textAreaBackgroundColor="@color/blue"
        //是否显示转写内容
        app:showTranscriptContent="true"
        //字体大小
        app:textSize="15sp" />
```

```Java
//设置从rtc的onStreamMessage获取的data和uid值
binding.transcriptSubtitleView.pushMessageData(data, uid)
//获取所有转写内容
binding.transcriptSubtitleView.getAllTranscriptText()
//获取所有翻译内容
binding.transcriptSubtitleView.getAllTranslateText()
//清空所有转写和翻译内容
binding.transcriptSubtitleView.clear()
```