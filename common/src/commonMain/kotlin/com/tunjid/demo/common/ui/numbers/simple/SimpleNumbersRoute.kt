/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.demo.common.ui.numbers.simple

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import com.tunjid.demo.common.ui.AppRoute
import com.tunjid.demo.common.ui.numbers.ColumnListStyle
import com.tunjid.demo.common.ui.numbers.GridListStyle
import com.tunjid.demo.common.ui.numbers.ListStyle
import com.tunjid.demo.common.ui.numbers.ScrollState
import com.tunjid.demo.common.ui.numbers.Tabbed
import com.tunjid.demo.common.ui.numbers.page
import com.tunjid.demo.common.ui.numbers.updateDirection
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlin.math.abs

object SimpleNumbersRoute : AppRoute {
    override val id: String
        get() = "simple"

    @Composable
    override fun Render() {
        Tabbed(
            listStyles = listOf(
                ColumnListStyle as ListStyle<ScrollableState>,
                GridListStyle as ListStyle<ScrollableState>,
            ),
            contentDependencies = { _, listStyle, isDark ->
                SimpleNumberFetcher(
                    itemsPerPage = listStyle.itemsPerPage,
                    isDark = isDark,
                )
            },
            content = { listStyle, dependency ->
                SimpleList(
                    listStyle = listStyle,
                    fetcher = dependency
                )
            }
        )
    }
}

@Composable
fun SimpleList(
    listStyle: ListStyle<ScrollableState>,
    fetcher: SimpleNumberFetcher
) {

    val items by fetcher.listItems.collectAsState(listOf())
    val lazyState = listStyle.rememberState()

    listStyle.Content(
        state = lazyState,
        items = items
    )

    // Load when this Composable enters the composition
    LaunchedEffect(true) {
        fetcher.fetchPage(page = 0)
    }

    // Endless scrolling
    LaunchedEffect(lazyState, items) {
        snapshotFlow {
            listStyle.scrollState(
                state = lazyState,
                items = items,
                isAscending = true
            )
        }
            .scan(ScrollState(), ScrollState::updateDirection)
            .filter { abs(it.dy) > 4 }
            .distinctUntilChangedBy(ScrollState::page)
            .collect { fetcher.fetchPage(page = it.page + 1) }
    }
}

