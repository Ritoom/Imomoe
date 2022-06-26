package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.BuildConfig
import com.skyd.imomoe.model.interfaces.IConst

class CustomConst : IConst {
    companion object {
        val ANIME_RANK: String = "search"
        val ANIME_PLAY: String = "watch"
        val ANIME_DETAIL: String = "/watch"
        val ANIME_SEARCH: String = "/search"
        val ANIME_CLASSIFY: String = "/classify"
        val MAIN_URL: String by lazy { CustomConst().MAIN_URL }
    }
    override fun versionName(): String = "0.0.2"

    override fun versionCode(): Int = 2
    override val MAIN_URL: String
        get() = BuildConfig.MAIN_URL

    override fun about(): String {
        return "默认数据源，不提供任何数据，请在设置界面手动选择自定义数据源！"
    }
}
