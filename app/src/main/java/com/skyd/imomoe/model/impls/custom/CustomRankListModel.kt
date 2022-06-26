package com.skyd.imomoe.model.impls.custom

import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.AnimeCover3Bean
import com.skyd.imomoe.bean.AnimeTypeBean
import com.skyd.imomoe.bean.ImageBean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.IRankListModel
import com.skyd.imomoe.model.util.JsoupUtil
@NoLiveLiterals
class CustomRankListModel : IRankListModel {
    var rankList: MutableList<Any> = ArrayList()
    var pageNumberBean: PageNumberBean? = null
    override suspend fun getRankListData(partUrl: String): Pair<MutableList<Any>, PageNumberBean?> {
        rankList.clear()
        pageNumberBean = null
        val const = CustomConst
        val url = "${const.MAIN_URL}/${partUrl}"
        val document = JsoupUtil.getDocument(url)
        val rootContent = document.getElementById("home-rows-wrapper")
        if (rootContent != null){
            val second = rootContent.selectFirst("div[class=content-padding-new]")
            if (second != null){
                val content = second.child(0).children()
                val result: ArrayList<AnimeCover3Bean> = ArrayList()
                for (animeContent in content) {
                    val animeRealItem = animeContent.child(0).children()
                    var animeUrl = ""
                    var title = ""
                    var desc = ""
                    val tag: ArrayList<AnimeTypeBean> = ArrayList()
                    val image = ImageBean("","","")
                    for (anime in animeRealItem) {
                        when(anime.tagName()){
                            "a" -> {
                                animeUrl = anime.attr("href").substringAfter(const.MAIN_URL)
                                image.url = anime.select("img")[1].attr("src")
                                image.route = anime.select("img")[1].attr("src")
                                desc = "点击次数（" + anime.selectFirst("span[class=card-mobile-views-text]")?.text()+"）"
                            }
                            "div" -> {
                                val textContent = anime.child(0).children()
                                for (text in textContent) {
                                    when(text.tagName()){
                                        "a" -> {
                                            title = text.text()
                                        }
                                        "div" -> {
//                                            desc = text.select("div[class=card-mobile-user]").text()
                                            tag.add(
                                                AnimeTypeBean("","",text.select("span").text())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    result.add(
                        AnimeCover3Bean(
                            animeUrl,animeUrl,title,image,"",desc,tag
                        )
                    )
                }
                rankList.addAll(result)
            }
            val pagination = rootContent
                .selectFirst("div[class=search-doujin-pagination-desktop-margin search-pagination hidden-xs]")
            if (pagination != null){
                val page = pagination.child(0).select("li[class*=active]").text()
                if (page.toIntOrNull() != null && page.toIntOrNull()!! > 0){
                    val pageUrl = const.ANIME_RANK + pagination.child(0)
                        .select("li[class*=active]").next()
                        .select("a").attr("href")
                    pageNumberBean = PageNumberBean(pageUrl,const.MAIN_URL+pageUrl,pagination
                        .child(0).select("li[class*=active]").next()
                        .select("a").text())
                }
            }
        }
        return Pair(rankList, pageNumberBean)
    }
}
