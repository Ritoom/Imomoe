package com.skyd.imomoe.view.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.material.composethemeadapter3.Mdc3Theme
import com.skyd.imomoe.R
import com.skyd.imomoe.database.entity.UrlMapEntity
import com.skyd.imomoe.ext.activity
import com.skyd.imomoe.ext.showInputDialog
import com.skyd.imomoe.state.DataState
import com.skyd.imomoe.view.component.compose.AnimeTopBar
import com.skyd.imomoe.view.component.compose.AnimeTopBarStyle
import com.skyd.imomoe.view.component.compose.TopBarIcon
import com.skyd.imomoe.viewmodel.UrlMapViewModel

class UrlMapActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mdc3Theme(
                setTextColors = true,
                setDefaultFontFamily = true
            ) {
                UrlMapScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlMapScreen(viewModel: UrlMapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    Scaffold(topBar = {
        AnimeTopBar(
            style = AnimeTopBarStyle.Small,
            title = {
                Text(text = stringResource(R.string.url_map_activity_title))
            },
            navigationIcon = {
                TopBarIcon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_24),
                    contentDescription = null,
                    onClick = { context.activity.finish() }
                )
            }
        )
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            text = {
                Text(text = stringResource(id = R.string.add))
            },
            icon = {
                Icon(Icons.Rounded.Add, null)
            },
            onClick = {
                val activity = context.activity
                activity.showInputDialog(
                    hint = activity.getString(R.string.url_map_activity_input_old),
                    multipleLine = true
                ) { _, _, old ->
                    activity.showInputDialog(
                        hint = activity.getString(R.string.url_map_activity_input_new),
                        multipleLine = true
                    ) { _, _, new ->
                        viewModel.setUrlMap(old.toString(), new.toString())
                    }
                }
            },
        )
    }) {
        UrlMapList(it)
    }
}

/**
 * URL替换总开关
 */
private var urlMapEnabled by mutableStateOf(com.skyd.imomoe.net.urlMapEnabled)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlMapEnabledCard() {
    Card(
        modifier = Modifier
            .padding(vertical = 7.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    com.skyd.imomoe.net.urlMapEnabled = !urlMapEnabled
                    urlMapEnabled = !urlMapEnabled
                }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 15.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.url_map_activity_enable),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier.padding(top = 7.dp),
                    text = stringResource(id = R.string.url_map_activity_enable_disadvantage),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = urlMapEnabled,
                onCheckedChange = {
                    com.skyd.imomoe.net.urlMapEnabled = it
                    urlMapEnabled = it
                }
            )
        }
    }
}

/**
 * 展示列表
 */
@Composable
fun UrlMapList(paddingValues: PaddingValues) {
    val viewModel: UrlMapViewModel = hiltViewModel()
    val urlMapListState by viewModel.urlMapList.collectAsState()
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
    ) {
        item {
            UrlMapEnabledCard()
        }
        when (urlMapListState) {
            is DataState.Success -> {
                val list = urlMapListState.read()
                items(list.size) { index ->
                    UrlMapItem(list[index])
                }
            }
            else -> {}
        }
    }
}

/**
 * 列表的每一项
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UrlMapItem(urlMapEntity: UrlMapEntity) {
    val viewModel: UrlMapViewModel = hiltViewModel()
    val enabledData = urlMapEntity.enabled
    var enabled by remember { mutableStateOf(enabledData) }
    Card(
        modifier = Modifier
            .padding(vertical = 7.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = {
                        showWarnDeleteDialog.value = true
                        warnDeleteDialogOldUrl.value = urlMapEntity.oldUrl
                    },
                    onClick = { if (urlMapEnabled) enabled = !enabled }
                )
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 15.dp)
            ) {
                Text(
                    text = urlMapEntity.oldUrl,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier.padding(top = 17.dp),
                    text = stringResource(R.string.url_map_activity_new, urlMapEntity.newUrl),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    viewModel.enabledUrlMap(urlMapEntity.oldUrl, it)
                },
                enabled = urlMapEnabled
            )
        }
    }
    if (showWarnDeleteDialog.value && warnDeleteDialogOldUrl.value == urlMapEntity.oldUrl) {
        WarnDeleteDialog(urlMapEntity.oldUrl)
    }
}

val showWarnDeleteDialog = mutableStateOf(false)
val warnDeleteDialogOldUrl = mutableStateOf<String?>(null)

@Composable
fun WarnDeleteDialog(oldUrl: String, viewModel: UrlMapViewModel = hiltViewModel()) {
    AlertDialog(
        onDismissRequest = {
            showWarnDeleteDialog.value = false
            warnDeleteDialogOldUrl.value = null
        },
        title = {
            Text(text = stringResource(id = R.string.warning))
        },
        text = {
            Text(text = stringResource(id = R.string.url_map_activity_delete))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showWarnDeleteDialog.value = false
                    warnDeleteDialogOldUrl.value = null
                    viewModel.deleteUrlMap(oldUrl)
                }
            ) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showWarnDeleteDialog.value = false
                    warnDeleteDialogOldUrl.value = null
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
