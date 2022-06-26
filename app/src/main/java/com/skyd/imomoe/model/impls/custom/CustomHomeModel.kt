package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.model.interfaces.IHomeModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.eventbus.SelectHomeTabEvent
import org.greenrobot.eventbus.EventBus
import org.jsoup.select.Elements

class CustomHomeModel : IHomeModel {
    override suspend fun getAllTabData(): ArrayList<TabBean> {
        return ArrayList<TabBean>().apply {
            val const = CustomConst
            val url = const.MAIN_URL
            val document = JsoupUtil.getDocument(url)
            val menu: Elements? = document.getElementsByClass("nav-item")
            add(TabBean(const.MAIN_URL,const.MAIN_URL,"首页"))
            for (i in menu!!.indices) {
                if (menu[i].text().equals("H漫畫") || menu[i].text().equals("我的清單")) continue
                val a = menu[i].select("a").attr("href")
                if (a.indexOf(url) != -1){
                    add(TabBean(a.substringAfter(const.MAIN_URL), a, menu[i].text()))
                }
            }
        }
    }
}