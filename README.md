# StickView
  图片标签处理
  
## Screenshots
  ![](screenshots/S60425-161409.png) ![](screenshots/S60425-161422.png)

##Feature

- 支持缩放(最小缩放),平移,并生成处理后的图片.
- 支持便签数量无限制(较多可能存在性能问题)
- 支持文字标签(实质文字转换成图片)

##Issue
- 图片生成会出现锯齿(后面会更正)
- StickView scaleType must be fitCenter,cause compose bitmap base only fitCenter scaleType.
- 需进一步优化代码

## Gradle

```groovy
compile 'com.yovenny.StickView:sticklib:1.0.0'
```

##Usage
    
###＊incode＊
```java  
   mSticker.outsideRadium(14).insideRadium(9).stickWidth(150).strokeWidth(2).locationPadding(15);
                      
```
                      
###  ＊in xml＊
```xml
       <com.yovenny.sticklib.StickerSeriesView
                  android:background="#131413"
                  android:scaleType="fitCenter"
                  android:id="@+id/process_sticker"
                  android:layout_width="match_parent"
                  android:layout_height="match_parent" />
```
