package dk.scheduling.schedulingfrontend.gui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refreshable(
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
            refreshState.endRefresh()
        }
    }
    Box(Modifier.nestedScroll(refreshState.nestedScrollConnection)) {
        content()

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = refreshState,
        )
    }
}
