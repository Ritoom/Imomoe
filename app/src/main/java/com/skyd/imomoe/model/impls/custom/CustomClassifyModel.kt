package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IClassifyModel
import com.skyd.imomoe.model.util.JsoupUtil

class CustomClassifyModel : IClassifyModel {
    override suspend fun getClassifyData(partUrl: String): Pair<ArrayList<Any>, PageNumberBean?> {
        val const = CustomConst
        var pageNumberBean: PageNumberBean? = null
        val searchResultList: ArrayList<Any> = ArrayList()
        val url = const.MAIN_URL+partUrl
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
            }else {
                val animeList = rootContent.selectFirst("div[class=home-rows-videos-wrapper]")
                    ?.children()
                val result: ArrayList<AnimeCover3Bean> = ArrayList()
                val tag: ArrayList<AnimeTypeBean> = ArrayList()
                if (animeList != null) {
                    for (c in animeList) {
                        result.add(
                            AnimeCover3Bean(c.select("a").attr("href").substringAfter(const.MAIN_URL),
                                c.select("a").attr("href"),
                                c.select("div[class=home-rows-videos-title]").text(),
                                ImageBean(c.select("img").attr("src"),
                                    c.select("img").attr("src"),
                                    c.select("img").attr("src")),"","",tag
                            )
                        )
                    }
                }
                searchResultList.addAll(result)
            }
            val realPa = rootContent.children()
            for (r in realPa) {
                if (r.attr("class")
                        .equals("search-hentai-pagination-desktop-margin search-pagination hidden-xs")
                    || r.attr("class").equals("search-doujin-pagination-desktop-margin search-pagination hidden-xs")){
                    if(r.children().size > 0){
                        val page = r.child(0).select("li[class*=active]").text()
                        if (page.toIntOrNull() != null && page.toIntOrNull()!! > 0){
                            val pageUrl = const.ANIME_SEARCH+"/" + r.child(0)
                                .select("li[class*=active]").next()
                                .select("a").attr("href")
                            pageNumberBean = PageNumberBean(pageUrl,const.MAIN_URL+pageUrl,r
                                .child(0).select("li[class*=active]").next()
                                .select("a").text())
                        }
                    }
                }
            }
        }
        return Pair(searchResultList, pageNumberBean)
    }

    override fun clearActivity() {
    }

    override suspend fun getClassifyTabData(): ArrayList<ClassifyBean> {
        val const = CustomConst
        val classifyTabList: ArrayList<ClassifyBean> = ArrayList()
        val url = "${const.MAIN_URL}${const.ANIME_SEARCH}"
        val document = JsoupUtil.getDocument(url)
        val hentaiForm = document.getElementById("hentai-form")
        if (hentaiForm != null){
            val hentaiFormList = hentaiForm.children()
            for (form in hentaiFormList) {
                when(form.id()){
                    "tags" -> {
                        if (form != null){
                            val realTags = form.child(0).child(0)
                            val tagsBody = realTags.selectFirst("div[class=modal-body]")
                            if (tagsBody != null && tagsBody.children().size > 0){
                                val tagList = tagsBody.children()
                                for (tag in tagList) {
                                    when (tag.tagName()){
                                        "h5" -> {
                                            classifyTabList.add(
                                                ClassifyBean(
                                                    "",
                                                    tag.text(), ArrayList()
                                                )
                                            )
                                        }
                                        "label" -> {
                                            if (classifyTabList.size > 0){
                                                classifyTabList[classifyTabList.size-1].classifyDataList.add(
                                                    ClassifyTab1Bean(
                                                        "${const.ANIME_SEARCH}?tags%5B%5D=${tag.text()}",
                                                        "",
                                                        tag.text())
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "brands" -> {
                        if (form != null){
                            val realTags = form.child(0).child(0)
                            val tagsBody = realTags.selectFirst("div[class=modal-body]")
                            if (tagsBody != null && tagsBody.children().size > 0){
                                val tagList = tagsBody.children()
                                for (tag in tagList) {
                                    when (tag.tagName()){
                                        "h4" -> {
                                            classifyTabList.add(
                                                ClassifyBean(
                                                    "",
                                                    tag.text(), ArrayList()
                                                )
                                            )
                                        }
                                        "label" -> {
                                            if (classifyTabList.size > 0){
                                                classifyTabList[classifyTabList.size-1].classifyDataList.add(
                                                    ClassifyTab1Bean(
                                                        "${const.ANIME_SEARCH}?brands%5B%5D=${tag.text()}",
                                                        "",
                                                        tag.text())
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return classifyTabList
    }

    override fun setActivity(activity: Activity) {
    }
}
