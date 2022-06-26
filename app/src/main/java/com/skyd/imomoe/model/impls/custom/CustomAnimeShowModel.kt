package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IAnimeShowModel
import com.skyd.imomoe.model.util.JsoupUtil
import org.jsoup.select.Elements

class CustomAnimeShowModel : IAnimeShowModel {
    override suspend fun getAnimeShowData(
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        val const = CustomConst
        var pageNumberBean: PageNumberBean? = null
        var url = ""
        if (partUrl.isBlank() || partUrl == const.MAIN_URL){
            url = const.MAIN_URL
        }else {
            url = "${const.MAIN_URL}${partUrl}"
        }
        val document = JsoupUtil.getDocument(url)
        val animeShowList: ArrayList<Any> = ArrayList()
        val banner: ArrayList<AnimeCover6Bean> = ArrayList();
        if (url != const.MAIN_URL){
            //非首页页面
            val animeView = document.getElementById("hentai-form");
            if (animeView != null){
                val animeList = animeView.selectFirst("div[class=home-rows-videos-wrapper]")
                    ?.children()
                if (animeList != null) {
                    for (c in animeList) {
                        animeShowList.add(
                            AnimeCover1Bean(c.select("a").attr("href").substringAfter(const.MAIN_URL),
                                c.select("a").attr("href"),
                                c.select("div[class=home-rows-videos-title]").text(),
                                ImageBean(c.select("img").attr("src"),
                                    c.select("img").attr("src"),
                                    c.select("img").attr("src")),""
                            )
                        )
                    }
                }else {
                    val list = animeView.selectFirst("div[class=content-padding-new]")?.child(0)
                        ?.children()
                    if (list != null){
                        for (d in list) {
                            val otherAnime = d.child(0).children()
                            var image: ImageBean? = null
                            var imageUrl: String = ""
                            var imageTitle: String = ""
                            for (anime in otherAnime) {
                                if(anime.attr("style").equals("text-decoration: none;")){
                                    image = ImageBean(anime.select("img[style*=position: absolute;]").attr("src"),
                                        anime.select("img[style*=position: absolute;]").attr("src"),
                                        anime.select("img[style*=position: absolute;]").attr("src"))
                                }else {
                                    imageUrl = anime.select("a").attr("href")
                                    imageTitle = anime.select("div[class=card-mobile-title]").text()
                                }
                            }
                            animeShowList.add(
                                AnimeCover1Bean(imageUrl.substringAfter(const.MAIN_URL),
                                    imageUrl,
                                    imageTitle,
                                    image!!,""
                                )
                            )
                        }
                    }
                }
                val pa = animeView.selectFirst("div[class=search-rows-wrapper]")
                if (pa != null){
                    val realPa = pa.children()
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
            }
            return Pair(animeShowList, pageNumberBean)
        }else {
            val foucsBgElements: Elements = document.getElementsByClass("nav-bottom-padding home-content-wrapper")
            for (foucsBgElement in foucsBgElements) {
                val recent = foucsBgElement.selectFirst("div[style*=padding-top: 100px]")
                if (recent != null) {
                    val bannerAll = recent.selectFirst("div[class=hidden-xs content-padding-new home-rows-margin-top]")
                        ?.child(1)
                    val bannerItem = bannerAll?.children()
                    if (bannerItem != null) {
                        for (foucsBgElement in bannerItem) {
                            val i = foucsBgElement.select("a").attr("href").substringAfter(url)
                            banner.add(AnimeCover6Bean(foucsBgElement.select("a").attr("href").substringAfter(
                                url
                            ),
                                foucsBgElement.selectFirst("div[class=card-mobile-title]")?.text() ?: "",
                                ImageBean(foucsBgElement.select("img")[1].attr("src"),
                                    foucsBgElement.select("img")[1].attr("src"),
                                    foucsBgElement.select("img")[1].attr("src")),
                                foucsBgElement.selectFirst("div[class=card-mobile-duration]")?.text()?: "",
                                null))
                        }
                    }
                    animeShowList.add(Banner1Bean("", animeCoverList = banner))
                    val otherArea = recent.children()
                    for (element in otherArea) {
                        if (element.className().equals("content-padding-new home-rows-top")){
                            //最新里番标题
                            animeShowList.add(Header1Bean(element.select("a").attr("href"),
                                element.select("h3").text()))
                        }
                        if (element.className().equals("owl-home-row owl-carousel owl-theme")){
                            //最新里番
                            val recentItem = element.children()
                            for (i in 0..7){
                                animeShowList.add(
                                    AnimeCover1Bean(recentItem[i].select("a").attr("href").substringAfter(
                                        url
                                    ),
                                        recentItem[i].select("a").attr("href"),
                                        recentItem[i].select("div[class=owl-home-rows-title]").text(),
                                        ImageBean(recentItem[i].select("img").attr("src"),
                                            recentItem[i].select("img").attr("src"),
                                            recentItem[i].select("img").attr("src")),""
                                    )
                                )
                            }
                        }
                        if (element.selectFirst("div[style=margin-top: 20px;]") != null){
                            //当月发烧影片
                            animeShowList.add(Header1Bean(
                                element?.select("a")?.attr("href") ?: "/",
                                (element?.select("h5")?.text() ?: "本月")
                                        + (element?.select("h3")?.text()?: "發燒影片")))
                            val monthHotItem = element.selectFirst("div[class=owl-home-row owl-carousel owl-theme]")!!
                                .children()
                            for (i in 0..7){
                                animeShowList.add(
                                    AnimeCover1Bean(monthHotItem[i].select("a").attr("href").substringAfter(
                                        url
                                    ),
                                        monthHotItem[i].select("a").attr("href"),
                                        monthHotItem[i].select("div[class=owl-home-rows-title]").text(),
                                        ImageBean(monthHotItem[i].select("img").attr("src"),
                                            monthHotItem[i].select("img").attr("src"),
                                            monthHotItem[i].select("img").attr("src")),""
                                    )
                                )
                            }
                        }
                        if (element.className().equals("home-rows-margin-top")){
                            //当下热门影片
                            val hotVideo = element.selectFirst("div[class=content-padding-new]")
                            animeShowList.add(Header1Bean(
                                hotVideo?.select("a")?.attr("href") ?: "/",
                                (hotVideo?.select("h5")?.text() ?: "當下")
                                        + (hotVideo?.select("h3")?.text()?: "熱門影片")))
                            val monthHotItem = element.selectFirst("div[class=owl-home-row owl-carousel owl-theme]")!!
                                .children()
                            for (i in 0..7){
                                animeShowList.add(
                                    AnimeCover1Bean(monthHotItem[i].select("a").attr("href").substringAfter(
                                        url
                                    ),
                                        monthHotItem[i].select("a").attr("href"),
                                        monthHotItem[i].select("div[class=owl-home-rows-title]").text(),
                                        ImageBean(monthHotItem[i].select("img").attr("src"),
                                            monthHotItem[i].select("img").attr("src"),
                                            monthHotItem[i].select("img").attr("src")),""
                                    )
                                )
                            }
                        }
                    }
                }
            }
            return Pair(animeShowList, null)
        }
    }
}