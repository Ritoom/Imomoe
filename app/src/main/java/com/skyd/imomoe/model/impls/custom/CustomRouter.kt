package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.model.interfaces.IRouter
import com.skyd.imomoe.route.Router.buildRouteUri
import com.skyd.imomoe.route.Router.route
import com.skyd.imomoe.route.processor.*
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.view.activity.*
import java.net.URLDecoder

@NoLiveLiterals
class CustomRouter : IRouter {
    override fun route(uri: Uri, context: Context?): Boolean {
        val uriString = uri.toString()
        val const = CustomConst
        when {
            uriString.startsWith(const.ANIME_DETAIL) -> {     //番剧封面点击进入
                DetailActivityProcessor.route.buildRouteUri {
                    appendQueryParameter("partUrl", uriString)
                }.route(context)
                return true
            }
            uriString.startsWith(const.ANIME_PLAY) -> {     //番剧每一集点击进入
                val playCode = uriString.replace("watch?","")
                if (playCode.isNotEmpty()) {
                    var detailPartUrl =
                        uriString.substringAfter(const.ANIME_DETAIL, "")
                    detailPartUrl = const.ANIME_DETAIL + detailPartUrl
                    PlayActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("partUrl", uriString)
                        appendQueryParameter("detailPartUrl", detailPartUrl)
                    }.route(context)
                } else {
                    "播放集数解析错误！".showToast()
                }
                return true
            }
            uriString.startsWith(const.ANIME_SEARCH) -> {     // 进入搜索页面
                uriString.replace(const.ANIME_SEARCH, "").let {
                    val keyWord = it.replaceFirst(Regex("/.*"), "")
                    val pageNumber = it.replaceFirst(Regex("($keyWord/)|($keyWord)"), "")
                    SearchActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("keyword", keyWord)
                        appendQueryParameter("pageNumber", pageNumber)
                    }.route(context)
                }
                return true
            }
            uriString.startsWith(const.ANIME_RANK) -> {     // 排行榜
                RankActivityProcessor.route.route(context)
                return true
            }
            uriString.startsWith("/app${const.ANIME_CLASSIFY}") -> {     //如进入分类页面
                val paramList = uriString.replace("/app${const.ANIME_CLASSIFY}", "")
                    .replaceAfterLast("/","")
                val param = paramList.substring(0,paramList.length-1)
                if (param.isNotBlank()) {
                    ClassifyActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("partUrl", param)
                        appendQueryParameter("classifyTabTitle", "")
                        appendQueryParameter("classifyTitle", param)
                    }
                } else "跳转协议格式错误".showToast()
                return true
            }
            uriString.startsWith(const.ANIME_CLASSIFY) -> {     //如进入分类页面
                val paramList = uriString.replace(const.ANIME_CLASSIFY, "")
                    .replaceAfterLast("/","")
                val param = paramList.substring(0,paramList.length-1)
                if (param.isNotBlank()) {
                    ClassifyActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("partUrl", param)
                        appendQueryParameter("classifyTabTitle", "")
                        appendQueryParameter("classifyTitle", param)
                    }
                } else "跳转协议格式错误".showToast()
                return true
            }
        }
        return false
    }
}
