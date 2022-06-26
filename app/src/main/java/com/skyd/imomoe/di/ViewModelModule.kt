package com.skyd.imomoe.di

import com.skyd.imomoe.model.DataSourceManager
import com.skyd.imomoe.model.impls.EverydayAnimeWidgetModel
import com.skyd.imomoe.model.impls.MonthAnimeModel
import com.skyd.imomoe.model.impls.Util
import com.skyd.imomoe.model.impls.custom.*
import com.skyd.imomoe.model.interfaces.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    @Provides
    fun provideAnimeDetailModel(): IAnimeDetailModel {
        return DataSourceManager.create(IAnimeDetailModel::class.java) ?: CustomAnimeDetailModel()
    }

    @Provides
    fun provideAnimeShowModel(): IAnimeShowModel {
        return DataSourceManager.create(IAnimeShowModel::class.java) ?: CustomAnimeShowModel()
    }

    @Provides
    fun provideClassifyModel(): IClassifyModel {
        return DataSourceManager.create(IClassifyModel::class.java) ?: CustomClassifyModel()
    }

    @Provides
    fun provideConst(): IConst {
        return DataSourceManager.getConst() ?: CustomConst()
    }

    @Provides
    fun provideEverydayAnimeModel(): IEverydayAnimeModel {
        return DataSourceManager.create(IEverydayAnimeModel::class.java) ?: CustomEverydayAnimeModel()
    }

    @Provides
    fun provideEverydayAnimeWidgetModel(): IEverydayAnimeWidgetModel {
        return DataSourceManager.create(IEverydayAnimeWidgetModel::class.java)
            ?: EverydayAnimeWidgetModel()
    }

    @Provides
    fun provideHomeModel(): IHomeModel {
        return DataSourceManager.create(IHomeModel::class.java) ?: CustomHomeModel()
    }

    @Provides
    fun provideMonthAnimeModel(): IMonthAnimeModel {
        return DataSourceManager.create(IMonthAnimeModel::class.java) ?: MonthAnimeModel()
    }

    @Provides
    fun providePlayModel(): IPlayModel {
        return DataSourceManager.create(IPlayModel::class.java) ?: CustomPlayModel()
    }

    @Provides
    fun provideRankListModel(): IRankListModel {
        return DataSourceManager.create(IRankListModel::class.java) ?: CustomRankListModel()
    }

    @Provides
    fun provideRankModel(): IRankModel {
        return DataSourceManager.create(IRankModel::class.java) ?: CustomRankModel()
    }

    @Provides
    fun provideRouter(): IRouter {
        return DataSourceManager.getRouter() ?: CustomRouter()
    }

    @Provides
    fun provideSearchModel(): ISearchModel {
        return DataSourceManager.create(ISearchModel::class.java) ?: CustomSearchModel()
    }

    @Provides
    fun provideUtil(): IUtil {
        return DataSourceManager.getUtil() ?: Util()
    }
}