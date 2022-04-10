package com.skyd.imomoe.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.skyd.imomoe.R
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.ext.editor
import com.skyd.imomoe.ext.sharedPreferences
import com.skyd.imomoe.ext.showMessageDialog
import com.skyd.imomoe.model.DataSourceManager
import com.skyd.imomoe.net.DnsServer.selectDnsServer
import com.skyd.imomoe.util.Util
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.util.update.AppUpdateHelper
import com.skyd.imomoe.util.update.AppUpdateStatus
import com.skyd.imomoe.view.activity.ConfigDataSourceActivity
import com.skyd.imomoe.view.component.player.PlayerCore
import com.skyd.imomoe.view.component.player.PlayerCore.selectPlayerCore
import com.skyd.imomoe.util.compare.EpisodeTitleSort
import com.skyd.imomoe.util.compare.EpisodeTitleSort.selectEpisodeTitleSortMode
import com.skyd.imomoe.view.component.preference.BasePreferenceFragment
import com.skyd.imomoe.viewmodel.SettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingFragment : BasePreferenceFragment() {
    private val viewModel: SettingViewModel by viewModels()
    private var selfUpdateCheck = false
    private val appUpdateHelper = AppUpdateHelper.instance

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 清理历史记录
        viewModel.mldDeleteAllHistory.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it) getString(R.string.delete_all_history_succeed).showToast()
            else getString(R.string.delete_all_history_failed).showToast()
            viewModel.mldDeleteAllHistory.postValue(null)
        })
        viewModel.mldAllHistoryCount.observe(viewLifecycleOwner) {
            findPreference<Preference>("delete_all_history")?.summary =
                getString(R.string.delete_all_history_summary, it)
        }
        viewModel.getAllHistoryCount()

        // 清理缓存文件
        viewModel.mldCacheSize.observe(viewLifecycleOwner) {
            findPreference<Preference>("clear_cache")?.summary =
                getString(R.string.clear_cache_summary, it)
        }
        viewModel.mldClearAllCache.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            lifecycleScope.launch(Dispatchers.IO) {
                delay(1000)
                viewModel.getCacheSize()
                if (it) getString(R.string.clear_cache_succeed).showToast()
                else getString(R.string.clear_cache_failed).showToast()
            }
            viewModel.mldClearAllCache.postValue(null)
        })
        viewModel.getCacheSize()

        appUpdateHelper.getUpdateStatus().observe(viewLifecycleOwner) {
            val text1: String = when (it) {
                AppUpdateStatus.UNCHECK -> {
                    getString(R.string.uncheck_update)
//                    appUpdateHelper.checkUpdate()
                }
                AppUpdateStatus.CHECKING -> {
                    getString(R.string.checking_update)
                }
                AppUpdateStatus.DATED -> {
                    if (selfUpdateCheck) appUpdateHelper.noticeUpdate(requireActivity())
                    getString(R.string.find_new_version)
                }
                AppUpdateStatus.VALID -> {
                    getString(R.string.is_latest_version).apply {
                        if (selfUpdateCheck) showToast()
                    }
                }
                AppUpdateStatus.LATER -> {
                    getString(R.string.delay_update)
                }
                AppUpdateStatus.ERROR -> {
                    getString(R.string.check_update_failed).apply {
                        if (selfUpdateCheck) showToast()
                    }
                }
                else -> ""
            }
            findPreference<Preference>("update")?.title =
                getString(R.string.check_update_summary, text1)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("download_path")?.apply {
            summary = Const.DownloadAnime.animeFilePath
            setOnPreferenceClickListener {
                showMessageDialog(
                    title = getString(R.string.attention),
                    message = "由于新版Android存储机制变更，因此新缓存的动漫将存储在App的私有路径，" +
                            "以前缓存的动漫依旧能够观看，其后面将有“旧”字样。新缓存的动漫与以前缓存的互不影响。" +
                            "\n\n注意：新缓存的动漫将在App被卸载或数据被清除后丢失。",
                    onPositive = { dialog, _ -> dialog.dismiss() }
                )
                false
            }
        }

        findPreference<Preference>("delete_all_history")?.apply {
            setOnPreferenceClickListener {
                showMessageDialog(
                    title = getString(R.string.warning),
                    message = getString(R.string.confirm_delete_all_history),
                    icon = R.drawable.ic_delete_24,
                    positiveText = getString(R.string.delete),
                    onPositive = { _, _ -> viewModel.deleteAllHistory() },
                    onNegative = { dialog, _ -> dialog.dismiss() }
                )
                false
            }
        }

        findPreference<Preference>("clear_cache")?.apply {
            setOnPreferenceClickListener {
                showMessageDialog(
                    title = getString(R.string.warning),
                    message = "确定清理所有缓存？不包括缓存视频",
                    icon = R.drawable.ic_sd_storage_24,
                    positiveText = getString(R.string.clean),
                    onPositive = { _, _ -> viewModel.clearAllCache() },
                    onNegative = { dialog, _ -> dialog.dismiss() }
                )
                false
            }
        }

        findPreference<Preference>("dark_mode_follow_system")?.apply {
            setOnPreferenceClickListener {
                showDarkModeMenu(listView.getChildAt(order), R.menu.menu_setting_fragment_dark_mode)
                false
            }
        }

        findPreference<Preference>("update")?.apply {
            summary = getString(R.string.current_version, Util.getAppVersionName())
            setOnPreferenceClickListener {
                selfUpdateCheck = true
                when (appUpdateHelper.getUpdateStatus().value) {
                    AppUpdateStatus.CHECKING -> {
                        "已在检查，请稍等...".showToast()
                    }
                    else -> appUpdateHelper.checkUpdate()
                }
                false
            }
        }

        findPreference<Preference>("custom_data_source")?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(activity, ConfigDataSourceActivity::class.java))
                false
            }
            title = getString(R.string.custom_data_source, DataSourceManager.dataSourceName.let {
                if (it == DataSourceManager.DEFAULT_DATA_SOURCE)
                    getString(R.string.default_data_source)
                else it
            })
        }

        findPreference<Preference>("dns_server")?.apply {
            setOnPreferenceClickListener {
                activity?.selectDnsServer()
                false
            }
        }

        findPreference<CheckBoxPreference>("show_player_bottom_progressbar")?.apply {
            isChecked = sharedPreferences().getBoolean("show_player_bottom_progressbar", false)
            setOnPreferenceChangeListener { _, newValue ->
                sharedPreferences().editor {
                    putBoolean("show_player_bottom_progressbar", newValue as? Boolean ?: false)
                }
                true
            }
        }

        findPreference<CheckBoxPreference>("auto_jump_to_last_position")?.apply {
            isChecked = sharedPreferences().getBoolean("auto_jump_to_last_position", false)
            setOnPreferenceChangeListener { _, newValue ->
                sharedPreferences().editor {
                    putBoolean("auto_jump_to_last_position", newValue as? Boolean ?: false)
                }
                true
            }
        }

        findPreference<Preference>("episode_title_sort_mode")?.apply {
            summary = getString(
                R.string.episode_title_sort_mode_summary,
                EpisodeTitleSort.episodeTitleSortMode
            )
            setOnPreferenceClickListener {
                activity?.selectEpisodeTitleSortMode {
                    summary = getString(R.string.episode_title_sort_mode_summary, it)
                }
                false
            }
        }

        findPreference<Preference>("player_core")?.apply {
            summary = getString(R.string.current_player_core, PlayerCore.playerCore.coreName)
            setOnPreferenceClickListener {
                activity?.selectPlayerCore {
                    summary = getString(R.string.current_player_core, it.coreName)
                    findPreference<CheckBoxPreference>("media_codec")?.isVisible =
                        PlayerCore.playerCore.playManager == IjkPlayerManager::class.java
                }
                false
            }
        }

        findPreference<CheckBoxPreference>("media_codec")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                PlayerCore.setMediaCodec(newValue as? Boolean ?: false)
                true
            }
            isVisible = PlayerCore.playerCore.playManager == IjkPlayerManager::class.java
        }
    }

    private fun showDarkModeMenu(v: View, @MenuRes menuRes: Int) {
        PopupMenu(requireContext(), v).apply {
            menuInflater.inflate(menuRes, menu)
            setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_item_setting_fragment_dark_yes -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    R.id.menu_item_setting_fragment_dark_no -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    R.id.menu_item_setting_fragment_dark_follow_system -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
                true
            }
            show()
        }
    }
}