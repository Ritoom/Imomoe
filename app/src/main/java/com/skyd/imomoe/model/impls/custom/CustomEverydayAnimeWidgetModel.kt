package com.skyd.imomoe.model.impls.custom

import androidx.compose.runtime.NoLiveLiterals
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IEverydayAnimeWidgetModel
@NoLiveLiterals
class CustomEverydayAnimeWidgetModel : IEverydayAnimeWidgetModel {
    override fun getEverydayAnimeData(): ArrayList<List<AnimeCover10Bean>> {
        return ArrayList()
    }
}