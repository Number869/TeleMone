package com.number869.telemone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.number869.telemone.data.AppSettings
import com.number869.telemone.ui.screens.about.AboutDestinations
import com.number869.telemone.ui.screens.about.AboutNavigator
import com.number869.telemone.ui.screens.editor.EditorDestinations
import com.number869.telemone.ui.screens.editor.EditorNavigator
import com.number869.telemone.ui.screens.editor.EditorViewModel
import com.number869.telemone.ui.screens.main.MainScreen
import com.number869.telemone.ui.screens.welcome.WelcomeScreen
import com.nxoim.decomposite.core.common.navigation.NavController
import com.nxoim.decomposite.core.common.navigation.NavHost
import com.nxoim.decomposite.core.common.navigation.animations.fade
import com.nxoim.decomposite.core.common.navigation.animations.plus
import com.nxoim.decomposite.core.common.navigation.animations.scale
import com.nxoim.decomposite.core.common.navigation.animations.slide
import com.nxoim.decomposite.core.common.navigation.navController
import com.nxoim.decomposite.core.common.viewModel.viewModel
import kotlinx.serialization.Serializable

@Composable
fun Navigator() {
	val skipWelcomeScreen = AppSettings.agreedToConditions.get()
	val startDestination = if (skipWelcomeScreen) RootDestinations.Main else RootDestinations.Welcome

	val rootNavController = navController(startingDestination = startDestination)
	NavHost(
		rootNavController,
		animations = {
			scale(minimumScale = 0.7f, affectByGestures = false) + fade() + slide(dependOnSwipeEdge = true)
		}
	) {
		when (it) {
			RootDestinations.Welcome -> WelcomeScreen(rootNavController)
			RootDestinations.Main -> MainScreen(rootNavController)
			RootDestinations.Editor -> EditorNavigator(
				viewModel { EditorViewModel() },
				rootNavController,
				navController<EditorDestinations>(EditorDestinations.Editor),
				navController<EditorDestinations.Dialogs>(EditorDestinations.Dialogs.Empty)
			)
			RootDestinations.About -> AboutNavigator(
				rootNavController,
				navController<AboutDestinations>(AboutDestinations.About),
				navController<AboutDestinations.Dialogs>(AboutDestinations.Dialogs.Empty)
			)
		}
	}
}

@Immutable
@Serializable
sealed interface RootDestinations {
	@Serializable
	data object Welcome : RootDestinations

	@Serializable
	data object Main : RootDestinations

	@Serializable
	data object Editor : RootDestinations

	@Serializable
	data object About : RootDestinations
}
