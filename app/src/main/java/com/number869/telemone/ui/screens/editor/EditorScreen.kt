@file:OptIn(ExperimentalFoundationApi::class)

package com.number869.telemone.ui.screens.editor

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.number869.telemone.data.AppSettings
import com.number869.telemone.data.colorOf
import com.number869.telemone.shared.ui.SmallTintedLabel
import com.number869.telemone.ui.screens.editor.components.new.CurrentThemePreview
import com.number869.telemone.ui.screens.editor.components.new.EditorTopAppBar
import com.number869.telemone.ui.screens.editor.components.new.ElementColorItem
import com.number869.telemone.ui.screens.editor.components.new.SavedThemeItem
import com.number869.telemone.ui.screens.editor.components.new.ThemeSelectionToolbar
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.navigation.getExistingNavController
import com.nxoim.decomposite.core.common.ultils.ContentType
import com.nxoim.decomposite.core.common.viewModel.getExistingViewModel
import my.nanihadesuka.compose.InternalLazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionActionable


// this is prob gonna get redesigned
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
	navController: NavController<EditorDestinations> = getExistingNavController(),
	vm: EditorViewModel = getExistingViewModel<EditorViewModel>()
) {
	val topAppBarState = TopAppBarDefaults.pinnedScrollBehavior()
	val savedThemesRowState = rememberLazyListState()
	val wholeThingListState = rememberLazyListState()

	val preferences = LocalContext.current.getSharedPreferences(
		"AppPreferences.Settings",
		Context.MODE_PRIVATE
	)
	val colorDisplayTypePref = preferences.getString(
		AppSettings.SavedThemeItemDisplayType.id,
		"1"
	)
	val colorDisplayType = when (colorDisplayTypePref) {
		"1" -> ThemeColorPreviewDisplayType.SavedColorValues
		"2" -> ThemeColorPreviewDisplayType.CurrentColorSchemeWithFallback
		else -> ThemeColorPreviewDisplayType.CurrentColorScheme
	}

	LaunchedEffect(vm.themeList) {
		savedThemesRowState.animateScrollToItem(0)
	}

	Scaffold(
		Modifier.nestedScroll(topAppBarState.nestedScrollConnection),
		topBar = {
			EditorTopAppBar(
				topAppBarState,
				vm.mappedValues,
				exportCustomTheme = vm::exportCustomTheme,
				saveCurrentTheme = vm::saveCurrentTheme,
				resetCurrentTheme = vm::resetCurrentTheme,
				loadSavedTheme = vm::loadSavedTheme,
				changeValue = vm::changeValue,
			)
		},
		bottomBar = { Box {} }, // edge to edge hello
	) { scaffoldPadding ->
		Box() {
			LazyColumn(
				state = wholeThingListState,
				verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
				contentPadding = PaddingValues(
					bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
				)
			) {
				item {
					Spacer(modifier = Modifier.padding(top = scaffoldPadding.calculateTopPadding()))

					SmallTintedLabel(Modifier.padding(start = 16.dp), labelText = "Current Theme")
					CurrentThemePreview(
						colorOf = { targetUiElement ->
							vm.colorFromCurrentTheme(targetUiElement)
						}
					)

					Spacer(modifier = Modifier.height(8.dp))

					Row(
						Modifier
							.clip(CircleShape)
							.clickable {
								navController.navigate(
									EditorDestinations.Dialogs.SavedThemeTypeSelection,
									ContentType.Overlay
								)
							},
						verticalAlignment = Alignment.Bottom,
						horizontalArrangement = spacedBy(8.dp)
					) {
						SmallTintedLabel(Modifier.padding(start = 16.dp), labelText = "Saved Themes")

						FilledTonalIconButton(
							onClick = {
								navController.navigate(
									EditorDestinations.Dialogs.SavedThemeTypeSelection,
									ContentType.Overlay
								)
							},
							modifier = Modifier.size(18.dp)
						) {
							Icon(Icons.Default.MoreVert, contentDescription = "Saved theme display type")
						}
					}

					AnimatedVisibility(visible = vm.themeList.isEmpty()) {
						Box(
							Modifier
								.fillMaxWidth(1f)
								.height(120.dp)
								.clip(RoundedCornerShape(16.dp))
								.animateItemPlacement()
								.animateContentSize()
						) {
							Column(
								Modifier
									.fillMaxSize()
									.padding(horizontal = 32.dp, vertical = 16.dp)
									.clip(CircleShape)
									.background(MaterialTheme.colorScheme.surfaceVariant),
								horizontalAlignment = Alignment.CenterHorizontally,
								verticalArrangement = Arrangement.Center
							) {
								Text(
									"No saved themes",
									style = TextStyle(platformStyle = PlatformTextStyle(
										includeFontPadding = false
									)).plus(MaterialTheme.typography.headlineSmall),
									textAlign = TextAlign.Center
								)
							}
						}
					}

					AnimatedVisibility(visible = vm.themeList.isNotEmpty()) {
						Column {
							LazyRow(
								state = savedThemesRowState,
								contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
								horizontalArrangement = spacedBy(16.dp),
								modifier = Modifier.animateContentSize()
							) {
								itemsIndexed(vm.themeList, key = { _, item -> item.uuid }) { index, theme ->
									SavedThemeItem(
										Modifier.animateItemPlacement(),
										theme,
										selected = vm.selectedThemes.contains(theme.uuid),
										true,
										loadSavedTheme = { vm.loadSavedTheme(it) },
										selectOrUnselectSavedTheme = { vm.selectOrUnselectSavedTheme(theme.uuid) },
										exportTheme = { vm.exportTheme(theme.uuid, it) },
										changeSelectionMode = {
											vm.toggleThemeSelectionModeToolbar()
										},
										colorOf = { targetUiElement ->
											colorOf(
												theme.values.find { it.name == targetUiElement }!!,
												colorDisplayType
											)
										},
										themeSelectionModeIsActive = vm.themeSelectionToolbarIsVisible
									)
								}
							}

							Row(
								Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.Center
							) {
								AnimatedVisibility(
									vm.themeSelectionToolbarIsVisible,
									enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
									exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
								) {
									ThemeSelectionToolbar(
										Modifier.padding(top = 16.dp),
										selectedThemeCount = vm.selectedThemes.count(),
										allThemesAreSelected = vm.selectedThemes.count() == vm.themeList.count(),
										unselectAllThemes = vm::unselectAllThemes,
										selectAllThemes = vm::selectAllThemes,
										hideToolbarAction = { vm.hideThemeSelectionModeToolbar() }
									)
								}
							}

							Spacer(
								Modifier
									.animateContentSize()
									.height(if (vm.themeSelectionToolbarIsVisible) 12.dp else 24.dp)
							)
						}
					}
				}

				if (vm.newUiElements.isNotEmpty()) {
					item {
						SmallTintedLabel(
							Modifier
								.padding(start = 16.dp)
								.animateItemPlacement(),
							labelText = "New Values"
						)
						Spacer(modifier = Modifier
							.height(16.dp)
							.animateItemPlacement())
					}

					itemsIndexed(vm.newUiElements) { index, uiElementData ->
						ElementColorItem(
							Modifier
								.padding(horizontal = 16.dp)
								.animateItemPlacement(),
							uiElementData = uiElementData,
							index = index,
							changeValue = vm::changeValue,
							lastIndexInList = vm.newUiElements.lastIndex
						)
					}

					item { Spacer(modifier = Modifier.height(24.dp)) }
				}

				if (vm.incompatibleValues.isNotEmpty()) {
					item {
						SmallTintedLabel(
							Modifier
								.padding(start = 16.dp)
								.animateItemPlacement(),
							labelText = "Incompatible Values"
						)

						Spacer(modifier = Modifier
							.height(16.dp)
							.animateItemPlacement())
					}

					itemsIndexed(vm.incompatibleValues) { index, uiElementData ->
						ElementColorItem(
							Modifier
								.padding(horizontal = 16.dp)
								.animateItemPlacement(),
							uiElementData = uiElementData,
							index = index,
							changeValue = vm::changeValue,
							lastIndexInList = vm.mappedValues.lastIndex
						)
					}
				}

				item {
					SmallTintedLabel(
						Modifier.padding(start = 16.dp),
						labelText = "All Colors"
					)

					Spacer(modifier = Modifier.height(16.dp))
				}

				itemsIndexed(vm.mappedValues, key = { _, it -> it.name }) {index, uiElementData ->
					ElementColorItem(
						Modifier
							.padding(horizontal = 16.dp)
							.animateItemPlacement(),
						uiElementData = uiElementData,
						index = index,
						changeValue = vm::changeValue,
						lastIndexInList = vm.mappedValues.lastIndex
					)
				}
			}

			InternalLazyColumnScrollbar(
				listState = wholeThingListState,
				thumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
				thumbSelectedColor = MaterialTheme.colorScheme.primary,
				selectionActionable = ScrollbarSelectionActionable.WhenVisible,
				modifier = Modifier.padding(
					top = scaffoldPadding.calculateTopPadding() + 8.dp,
					bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
				)
			)
		}
	}
}