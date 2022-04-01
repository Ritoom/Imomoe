package com.skyd.imomoe.bean

import com.skyd.imomoe.view.adapter.variety.Diff

class AnimeDescribe1Bean(
    override var actionUrl: String,
    var describe: String
) : BaseBean, Diff {
    override fun contentSameAs(o: Any?): Boolean = when {
        o !is AnimeDescribe1Bean -> false
        actionUrl == o.actionUrl && describe == o.describe -> true
        else -> false
    }
}