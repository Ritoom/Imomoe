package com.skyd.imomoe.model.impls.custom

import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.model.impls.custom.CustomConst
import com.skyd.imomoe.model.interfaces.IRankModel
@NoLiveLiterals
class CustomRankModel : IRankModel {
    private var tabList: ArrayList<TabBean> = ArrayList()
    override suspend fun getRankTabData(): ArrayList<TabBean> {
        tabList.clear()
        val const = CustomConst
        tabList.add(
            TabBean(
                const.ANIME_RANK+"?genre=全部&sort=本日排行",
                "",
                "本日排行榜"
            )
        )
        tabList.add(
            TabBean(
                const.ANIME_RANK+"?genre=全部&sort=本週排行",
                "",
                "本週排行榜"
            )
        )
        tabList.add(
            TabBean(
                const.ANIME_RANK+"?genre=全部&sort=本月排行",
                "",
                "本月排行榜"
            )
        )
        tabList.add(
            TabBean(
                const.ANIME_RANK+"?genre=全部&sort=觀看次數",
                "",
                "觀看次數排行榜"
            )
        )
        return tabList
    }
}
