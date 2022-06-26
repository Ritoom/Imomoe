package com.skyd.imomoe.model.impls.custom

import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.AnimeCover3Bean
import com.skyd.imomoe.bean.AnimeTypeBean
import com.skyd.imomoe.bean.ImageBean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.ISearchModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.Util.toEncodedUrl
@NoLiveLiterals
class CustomSearchModel : ISearchModel {
    override suspend fun getSearchData(
        keyWord: String,
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        println(partUrl)
        val const = CustomConst
        val key = keyWord.replace(" ","+")
        var pageNumberBean: PageNumberBean? = null
        val searchResultList: ArrayList<Any> = ArrayList()
        var url = ""
        if (partUrl.isBlank()){
            url = "${const.MAIN_URL}${const.ANIME_SEARCH}?query=${key.toEncodedUrl()}&page=1"
        }else{
            url = "${const.MAIN_URL}${const.ANIME_SEARCH}${partUrl}"
        }
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
                            }
                            "div" -> {
                                val textContent = anime.child(0).children()
                                for (text in textContent) {
                                    when(text.tagName()){
                                        "a" -> {
                                            title = text.text()
                                        }
                                        "div" -> {
                                            desc = text.select("div[class=card-mobile-user]").text()
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
                searchResultList.addAll(result)
            }
            val pagination = rootContent
                .selectFirst("div[class=search-doujin-pagination-desktop-margin search-pagination hidden-xs]")
            if (pagination != null){
                val page = pagination.child(0).select("li[class*=active]").text()
                if (page.toIntOrNull() != null && page.toIntOrNull()!! > 0){
                    val pageUrl = pagination.child(0).select("li[class*=active]").next()
                        .select("a").attr("href")
                    pageNumberBean = PageNumberBean(pageUrl,const.MAIN_URL+pageUrl,pagination
                        .child(0).select("li[class*=active]").next()
                        .select("a").text())
                }
            }
        }
        return Pair(searchResultList, pageNumberBean)
    }
}