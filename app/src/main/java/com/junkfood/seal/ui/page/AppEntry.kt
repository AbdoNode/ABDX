package com.junkfood.seal.ui.page

import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BottomBar
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.animatedComposableVariant
import com.junkfood.seal.ui.common.arg
import com.junkfood.seal.ui.common.id
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.page.command.TaskListPage
import com.junkfood.seal.ui.page.command.TaskLogPage
import com.junkfood.seal.ui.page.downloadv2.DownloadPageV2
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.UpdatePage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.DarkThemePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.command.TemplateEditPage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.directory.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.format.SubtitlePreference
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.interaction.InteractionPreferencePage
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.settings.troubleshooting.TroubleShootingPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel


private val TopDestinations =
    listOf(
        Route.HOME,
        Route.DOWNLOADS,
        Route.TASK_LIST,
        Route.SETTINGS
    )


@Composable
fun AppEntry(
    dialogViewModel: DownloadDialogViewModel
) {

    val navController = rememberNavController()

    val sheetState by dialogViewModel.sheetStateFlow
        .collectAsStateWithLifecycle()

    val cookiesViewModel: CookiesViewModel = koinViewModel()


    val currentRoute =
        navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route


    var currentTopDestination by rememberSaveable {
        mutableStateOf(currentRoute)
    }


    LaunchedEffect(currentRoute) {
        if(currentRoute in TopDestinations){
            currentTopDestination = currentRoute
        }
    }


    if(sheetState is DownloadDialogViewModel.SheetState.Configure){

        if(navController.currentDestination?.route != Route.HOME){

            navController.popBackStack(
                route = Route.HOME,
                inclusive = false
            )
        }
    }



    Scaffold(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ),


        bottomBar = {

            BottomBar(
                currentRoute = currentTopDestination,
                onNavigate = {

                    navController.navigate(it){

                        launchSingleTop = true

                        popUpTo(Route.HOME){
                            saveState = true
                        }

                        restoreState = true
                    }

                }
            )
        }


    ){ padding ->


        NavHost(

            modifier =
                Modifier.padding(padding),

            navController = navController,

            startDestination = Route.HOME

        ){


            animatedComposable(Route.HOME){

                DownloadPageV2(

                    dialogViewModel = dialogViewModel,

                    onMenuOpen = {}

                )
            }



            animatedComposable(Route.DOWNLOADS){

                VideoListPage{

                    navController.popBackStack()

                }
            }




            animatedComposableVariant(Route.TASK_LIST){

                TaskListPage(

                    onNavigateBack = {
                        navController.popBackStack()
                    },

                    onNavigateToDetail = {

                        navController.navigate(
                            Route.TASK_LOG id it
                        )

                    }
                )
            }



            slideInVerticallyComposable(

                Route.TASK_LOG arg Route.TASK_HASHCODE,

                arguments =
                    listOf(
                        navArgument(Route.TASK_HASHCODE){

                            type = NavType.IntType

                        }
                    )

            ){

                TaskLogPage(

                    onNavigateBack = {

                        navController.popBackStack()

                    },

                    taskHashCode =
                        it.arguments?.getInt(
                            Route.TASK_HASHCODE
                        ) ?: -1

                )
            }



            animatedComposable(Route.SETTINGS){

                SettingsPage(

                    onNavigateBack = {

                        navController.popBackStack()

                    },

                    onNavigateTo = {

                        navController.navigate(it)

                    }

                )

            }



            settingsGraph(

                onNavigateBack = {

                    navController.popBackStack()

                },

                onNavigateTo = {

                    navController.navigate(it)

                },

                cookiesViewModel = cookiesViewModel

            )

        }



        AppUpdater()
        YtdlpUpdater()


    }

}






fun NavGraphBuilder.settingsGraph(
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit,
    cookiesViewModel: CookiesViewModel,
) {


    navigation(

        startDestination = Route.SETTINGS_PAGE,

        route = Route.SETTINGS

    ){


        animatedComposable(Route.SETTINGS_PAGE){

            SettingsPage(
                onNavigateBack,
                onNavigateTo
            )

        }



        animatedComposable(Route.APPEARANCE){

            AppearancePreferences(
                onNavigateBack,
                onNavigateTo
            )

        }



        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES){

            GeneralDownloadPreferences(
                onNavigateBack
            ){

                onNavigateTo(Route.TEMPLATE)

            }

        }



        animatedComposable(Route.DOWNLOAD_DIRECTORY){

            DownloadDirectoryPreferences(
                onNavigateBack
            )

        }




        animatedComposable(Route.DOWNLOAD_FORMAT){

            DownloadFormatPreferences(
                onNavigateBack
            ){

                onNavigateTo(
                    Route.SUBTITLE_PREFERENCES
                )

            }

        }



        animatedComposable(Route.SUBTITLE_PREFERENCES){

            SubtitlePreference{

                onNavigateBack()

            }

        }



        animatedComposable(Route.ABOUT){

            AboutPage(

                onNavigateBack,

                onNavigateToCreditsPage = {

                    onNavigateTo(Route.CREDITS)

                },

                onNavigateToUpdatePage = {

                    onNavigateTo(Route.AUTO_UPDATE)

                },

                onNavigateToDonatePage = {}

            )

        }



        animatedComposable(Route.CREDITS){

            CreditsPage(onNavigateBack)

        }



        animatedComposable(Route.AUTO_UPDATE){

            UpdatePage(onNavigateBack)

        }



        animatedComposable(Route.LANGUAGES){

            LanguagePage{

                onNavigateBack()

            }

        }



        animatedComposable(Route.DARK_THEME){

            DarkThemePreferences{

                onNavigateBack()

            }

        }



        animatedComposable(Route.TEMPLATE){

            TemplateListPage(

                onNavigateBack

            ){

                onNavigateTo(
                    Route.TEMPLATE_EDIT id it
                )

            }

        }



        animatedComposable(

            Route.TEMPLATE_EDIT arg Route.TEMPLATE_ID,

            arguments =
                listOf(
                    navArgument(Route.TEMPLATE_ID){

                        type = NavType.IntType

                    }
                )

        ){

            TemplateEditPage(

                onNavigateBack,

                it.arguments?.getInt(
                    Route.TEMPLATE_ID
                ) ?: -1

            )

        }



        animatedComposable(Route.NETWORK_PREFERENCES){

            NetworkPreferences(

                navigateToCookieProfilePage = {

                    onNavigateTo(
                        Route.COOKIE_PROFILE
                    )

                }

            ){

                onNavigateBack()

            }

        }




        animatedComposable(Route.COOKIE_PROFILE){

            CookieProfilePage(

                cookiesViewModel,

                navigateToCookieGeneratorPage = {

                    onNavigateTo(
                        Route.COOKIE_GENERATOR_WEBVIEW
                    )

                }

            ){

                onNavigateBack()

            }

        }



        animatedComposable(Route.COOKIE_GENERATOR_WEBVIEW){

            WebViewPage(
                cookiesViewModel
            ){

                onNavigateBack()

                CookieManager
                    .getInstance()
                    .flush()

            }

        }



        animatedComposable(Route.TROUBLESHOOTING){

            TroubleShootingPage(

                onNavigateTo,

                onNavigateBack

            )

        }

    }

}