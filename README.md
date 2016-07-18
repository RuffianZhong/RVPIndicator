#1.什么是 RVPIndicator
###简单实用的ViewPageIndicator，支持item自身滚动

#####高仿MIUI但更胜于MIUI，提供多种指示器类型{下滑线，三角形，全背景}
效果图
![](hello_2.gif)

觉得这不满足你的需求？没问题，**RVPIndicator** 还支持使用图片作为指示器。只需要提供一张图片就可以修改成为自己想要的指示器效果
效果图
![](hello_1.gif)

什么？你专攻技术？你不会作图？没事，只要你够叼，随时添加一种新的指示器类型，添加两三行代码就可以增加新的指示器样式



#2. RVPIndicator 使用
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:rvp="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffffff"
    android:orientation="vertical" >

    <cn.r.vpindicator.android.view.RVPIndicator
        android:id="@+id/id_indicator"
        android:layout_width="match_parent"
        android:layout_height="45dp"
        android:background="#ADD597"
        android:orientation="horizontal"
        rvp:indicator_color="#f29b76"
        rvp:indicator_src="@drawable/heart_love"
        rvp:indicator_style="triangle"
        rvp:item_count="4"
        rvp:text_color_hightlight="#FF0000"
        rvp:text_color_normal="#fb9090" />

    <android.support.v4.view.ViewPager
        android:id="@+id/id_vp"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

</LinearLayout>
```



###2.1 自定义属性解释
```xml
		rvp:indicator_color="#f29b76"				//指示器颜色
        rvp:indicator_src="@drawable/heart_love"	//指示器图片{指示器类型为bitmap时需要}
        rvp:indicator_style="triangle"				//指示器类型
       											   //{bitmap：图片；line：下划线；square：方形全背景；triangle：三角形}
        rvp:item_count="4"							//item展示个数
        rvp:text_color_hightlight="#FF0000"			//item文字高亮颜色
        rvp:text_color_normal="#fb9090"				//item文字正常颜色
```

###2.2 代码调用
```java
		// 设置Tab上的标题
		mIndicator.setTabItemTitles(mDatas);
		// 设置关联的ViewPager
		mIndicator.setViewPager(mViewPager, 0);
```








