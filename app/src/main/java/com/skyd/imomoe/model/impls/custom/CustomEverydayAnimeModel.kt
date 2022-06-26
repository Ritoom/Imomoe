package com.skyd.imomoe.model.impls.custom

import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.impls.custom.CustomConst
import com.skyd.imomoe.model.interfaces.IEverydayAnimeModel
import com.skyd.imomoe.model.util.JsoupUtil
import java.time.LocalDateTime
@NoLiveLiterals
class CustomEverydayAnimeModel : IEverydayAnimeModel {
    override suspend fun getEverydayAnimeData(): Triple<ArrayList<TabBean>, ArrayList<List<Any>>, String> {
        val tabList = ArrayList<TabBean>()
        val const = CustomConst
        val header = "新番預告"
        val everydayAnimeList: ArrayList<List<Any>> = ArrayList()
        val now: LocalDateTime = LocalDateTime.now()
        val year = now.year
        var month = now.monthValue-1
        for (i in 0..2){
            month++
            val monthFormat:String = if (month < 10){
                "0$month"
            }else{
                "$month"
            }
            val url = const.MAIN_URL+"/previews/"+year+monthFormat
            val document = JsoupUtil.getDocument(url)
            val content = document.getElementById("content-div")
            val everyMonthAnimeList: ArrayList<Any> = ArrayList()
            if (content != null){
                val contentList = content.children()
                for (contentItem in contentList) {
                    when(contentItem.className()){
                        "row no-gutter video-show-width" -> {
                            if (!contentItem.attr("style").equals("margin-top: -80px;")){
                                //标题
                                val text = contentItem.child(0)
                                    .select("div[class=preview-top-content]").select("h1")
                                    .text()
//                                header = text
                                tabList.add(
                                    TabBean("","",text)
                                )
                            }
                        }
                        "content-padding" -> {
                            //list List<List<AnimeCover12Bean>>
                            val animeList = contentItem.select("div[class=row]")
                            for (anime in animeList) {
                                var title = ""
                                val num = AnimeEpisodeDataBean("","","")
                                val animeContent = anime.children()
                                for (child in animeContent) {
                                    if (child.attr("class").equals("col-md-12")){
                                        val childChild = child.children()
                                        for (c in childChild) {
                                            when(c.className()){
                                                "preview-info-cover" -> {
                                                    num.title = c.text()
                                                }
                                                "preview-info-content" -> {
                                                    title = c.select("h3").text()
                                                }
                                            }
                                        }
                                    }
                                }
                                everyMonthAnimeList.add(
                                    AnimeCover12Bean(
                                        "",
                                        "",
                                        title,
                                        num
                                    )
                                )
                            }
                        }
                    }
                }
                everydayAnimeList.add(everyMonthAnimeList)
            }
        }
        return Triple(tabList, everydayAnimeList, header)
    }
}