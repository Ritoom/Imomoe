package com.skyd.imomoe.model.impls.custom

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IAnimeDetailModel
import com.skyd.imomoe.model.util.JsoupUtil
import org.jsoup.nodes.Element

@NoLiveLiterals
class CustomAnimeDetailModel : IAnimeDetailModel {
    override suspend fun getAnimeDetailData(
        partUrl: String
    ): Triple<ImageBean, String, ArrayList<Any>> {
        val animeDetailList: ArrayList<Any> = ArrayList()
        val const = CustomConst()
        val cover = ImageBean("", "", "")
        var title = ""
        val url = const.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        val area: Element? = document.getElementById("content-div")
        val areaItem = area?.child(0)
        val animeItem = areaItem?.children()
        if (animeItem != null) {
            var alias = ""
            var info = ""
            var year = ""
            var index = ""
            var animeArea = ""
            val animeType: MutableList<AnimeTypeBean> = ArrayList()
            val tag: MutableList<AnimeTypeBean> = ArrayList()
            for (any in animeItem) {
                when(any.className()){
                    "col-md-9 single-show-player fluid-player-desktop-styles" -> {
                        val item = any.select("div[class=video-show-panel-width]")[0].children()
                        for (element in item) {
                            if (element.attr("style").equals("margin-bottom: -6px;")){
                                year = element.select("p").text().split("|")[0]
                                animeArea = element.select("p").text().split("|")[1]
                            }
                            if (element.attr("id").equals("shareBtn-title")){
                                if (element.text().contains('[')){
                                    val tagAll = element.text().substring(element.text().indexOfFirst { it == '[' },
                                        element.text().indexOfLast { it == ']' })
                                        .replace("[","",true).split("]")
                                    for (s in tagAll) {
                                        if (s.trim().isNotBlank()){
                                            tag.add(
                                                AnimeTypeBean(
                                                    "",
                                                    "",
                                                    s
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            if (element.attr("style")
                                    .equals("font-weight: 400; margin-bottom: 0px; margin-top: 0px;")){
                                for (child in element.children()) {
                                    if (!child.attr("data-toggle").equals("modal")){
                                        val tagUrl = child.select("a").attr("href")
                                            .substringBefore("&")
                                        animeType.add(
                                            AnimeTypeBean(
                                                tagUrl,
                                                child.select("a").attr("href"),
                                                child.select("a").text()
                                            )
                                        )
                                    }
                                }
                            }
                            if (element.attr("id").equals("caption")){
                                info = element.text()
                                alias = element.select("span").text().split("。")[0]
                            }
                        }
                    }
                    "col-md-3 single-show-list" -> {
                        val item = any.select("div[id=video-playlist-wrapper]")[0].children()
                        for (element in item) {
                            if (element.className().equals("single-icon-wrapper")){
                                title = element.select("h4")[0].text()
                                cover.url = element.select("img").attr("src")
                                cover.referer = url
                            }
                            if (element.className().equals("hover-video-playlist")){
                                //分集
                                animeDetailList.add(
                                    Header1Bean(
                                        "",
                                        "播放列表"
                                    )
                                )
                                val episode = element.children()
                                var episodeList: ArrayList<AnimeEpisodeDataBean> = ArrayList()
                                for (e in episode) {
                                    episodeList.add(
                                        AnimeEpisodeDataBean(
                                            e.select("a").attr("href")
                                                .substringAfter(const.MAIN_URL+"/"),
                                            e.select("h4").text(),
                                            e.select("a").attr("href"))
                                    )
                                }

                                animeDetailList.add(
                                    HorizontalRecyclerView1Bean(
                                        "",
                                        episodeList
                                    )
                                )
                            }
                        }
                    }
                }
            }
            animeDetailList.add(
                0,
                AnimeInfo1Bean(
                    "",
                    title,
                    ImageBean("",cover.url,url),
                    alias,
                    animeArea,
                    year,
                    index,
                    animeType,
                    tag,
                    info
                )
            )
        }
        return Triple(cover, title, animeDetailList)
    }
}