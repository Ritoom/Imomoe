package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IPlayModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.showToast
import org.jsoup.nodes.Element
import java.lang.ref.SoftReference
@NoLiveLiterals
class CustomPlayModel : IPlayModel {
    private var mActivity: SoftReference<Activity>? = null
    override suspend fun getPlayData(
        partUrl: String,
        animeEpisodeDataBean: AnimeEpisodeDataBean
    ): Triple<ArrayList<Any>, ArrayList<AnimeEpisodeDataBean>, PlayBean> {
        val const = CustomConst
        val playBeanDataList: ArrayList<Any> = ArrayList()
        val episodesList: ArrayList<AnimeEpisodeDataBean> = ArrayList()
        val title = AnimeTitleBean("", "")
        val episode =
            AnimeEpisodeDataBean(
                "", "",
                ""
            )
        val url = const.MAIN_URL + "/" + partUrl
        val document = JsoupUtil.getDocument(url)
        val content: Element = document.getElementById("content-div")!!
        val realContent = content.child(0).children()
        playBeanDataList.add(
            Header1Bean("","相關影片")
        )
        for (element in realContent) {
            if (element.attr("id").equals("player-div-wrapper")){
                val videoList = element.select("source")
                if (videoList.size > 0){
                    animeEpisodeDataBean.videoUrl = videoList[videoList.size-1].attr("src")
                }else{
                    val video = element.select("video")
                    if (video.size < 1){
                        "无法解析视频资源".showToast()
                    }
                    animeEpisodeDataBean.videoUrl = video.attr("src")
                }
                if (animeEpisodeDataBean.videoUrl.isBlank()){
                    "无法解析视频资源 请前往视频源网站播放".showToast()
                }
                if (animeEpisodeDataBean.videoUrl.startsWith("blob")){
                    "HTML5 blob格式资源 请前往视频源网站播放".showToast()
                }
                val videoItem = element.children()
                for (item in videoItem) {
                    if (item.attr("class").equals("video-show-panel-width")){
                        title.title = item.select("h5[id=caption]").select("span").text()
                        title.route = url
                        episode.title = title.title
                        animeEpisodeDataBean.title = title.title
                    }
                    else if (item.attr("id").equals("related-tabcontent")){
                        val recentAnime = item.select("div[class=row ]")
                        if (recentAnime.size > 0){
                            val otherAnimeList = recentAnime[0].children()
                            for (other in otherAnimeList) {
                                if (playBeanDataList.size <= 16){
                                    if (other.select("div[class=home-rows-videos-title]").size > 0){
                                        playBeanDataList.add(
                                            AnimeCover1Bean(
                                                other.select("a").attr("href"),
                                                other.select("a").attr("href"),
                                                other.select("div[class=home-rows-videos-title]").text()
                                                    .substring(0,other.select("div[class=home-rows-videos-title]").text().length-2),
                                                ImageBean(
                                                    other.select("img").attr("src"),
                                                    other.select("img").attr("src"),
                                                    ""
                                                ),
                                                "第"+ other.select("div[class=home-rows-videos-title]").text()
                                                    .substring(other.select("div[class=home-rows-videos-title]").text().length-1,
                                                        other.select("div[class=home-rows-videos-title]").text().length)
                                                        +"集"
                                            )
                                        )
                                    }
                                }
                            }
                        }else {
                            val unAnimeList = item.selectFirst("div[class*=row ]")
                            if (unAnimeList != null){
                                val unAnimeViewList = unAnimeList.children()
                                for (unAnime in unAnimeViewList) {
                                    if (playBeanDataList.size <= 16){
                                        playBeanDataList.add(
                                            AnimeCover1Bean(
                                                unAnime.select("a").attr("href"),
                                                unAnime.select("a").attr("href"),
                                                unAnime.select("s").attr("title"),
                                                ImageBean(
                                                    unAnime.select("img[style=width: 100%; height: 100%;]").attr("data-src"),
                                                    unAnime.select("img[style=width: 100%; height: 100%;]").attr("data-src"),
                                                    ""
                                                ),
                                                ""
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }else if (element.attr("class").equals("col-md-3 single-show-list")){
                val episodeList = element.select("div[id=playlist-scroll]")[0].children()
                for (e in episodeList) {
                    episodesList.add(
                        AnimeEpisodeDataBean(
                            e.select("a").attr("href"),
                            e.select("h4").text(),
                            e.select("a").attr("href")
                        )
                    )
                }
            }
        }
        val playBean = PlayBean("", title, episode,"", playBeanDataList)
        return Triple(
            playBeanDataList, episodesList, playBean
        )
    }

    //todo: 播放其他集数
    override suspend fun playAnotherEpisode(partUrl: String): AnimeEpisodeDataBean? {
        return null
    }

    override suspend fun getAnimeCoverImageBean(detailPartUrl: String): ImageBean? {
        val const = CustomConst
        val url = const.MAIN_URL + detailPartUrl
        val document = JsoupUtil.getDocument(url)
        val content: Element = document.getElementById("content-div")!!
        val realContent = content.child(0).children()
        for (element in realContent) {
            if (element.attr("class").equals("col-md-3 single-show-list")){
                val episoDetail = element.select("div[class=single-icon-wrapper]")[0]
                return ImageBean(
                    episoDetail.select("img").attr("src"),
                    episoDetail.select("img").attr("src"),
                    ""
                )
            }
        }
        return null
    }

    override fun setActivity(activity: Activity) {
        mActivity = SoftReference(activity)
    }

    override fun clearActivity() {
        mActivity = null
    }

    override suspend fun getAnimeDownloadUrl(partUrl: String): String? {
        return null
    }

}