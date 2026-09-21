package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.BacktestScreen
import com.example.ui.screens.CombinationsScreen
import com.example.ui.screens.FrequenciesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlusScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.RandomnessScreen
import com.example.ui.screens.RelationsScreen
import com.example.ui.screens.ReportsGridsScreen
import com.example.ui.theme.LotoBackground
import com.example.ui.theme.LotoBorder
import com.example.ui.theme.LotoOrange
import com.example.ui.theme.LotoSurface
import com.example.ui.theme.LotoSurfaceVariant
import com.example.ui.theme.LotoText
import com.example.ui.theme.LotoTextMuted
import com.example.ui.theme.MyApplicationTheme

enum class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Accueil", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    ANALYSIS("Analyse", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_analysis"),
    RELATIONS("Relations", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz, "nav_relations"),
    BACKTEST("Backtest", Icons.Filled.Timeline, Icons.Outlined.Timeline, "nav_backtest"),
    PLUS("Plus", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "nav_plus"),
    PREMIUM("Premium", Icons.Filled.Star, Icons.Outlined.Star, "nav_premium")
}

enum class PlusSubScreen {
    MAIN,
    FREQUENCIES,
    COMBINATIONS,
    REPORTS_GRIDS,
    HISTORY,
    RANDOMNESS,
    ADMIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var plusSubScreen by remember { mutableStateOf(PlusSubScreen.MAIN) }

    val userTier by viewModel.userTier.collectAsState()
    val activationCode by viewModel.activationCode.collectAsState()
    val expirationTimestamp by viewModel.expirationTimestamp.collectAsState()
    val isActivating by viewModel.isActivating.collectAsState()
    val activationError by viewModel.activationError.collectAsState()
    val isOfflineGraceActive by viewModel.isOfflineGraceActive.collectAsState()
    val remainingOfflineHours by viewModel.remainingOfflineHours.collectAsState()

    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()
    val adminCodes by viewModel.adminCodes.collectAsState()
    val isLoadingAdmin by viewModel.isLoadingAdmin.collectAsState()
    val createdCodeResult by viewModel.createdCodeResult.collectAsState()

    val allDraws by viewModel.allDraws.collectAsState()
    val customDraws by viewModel.customDraws.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    val numberStats by viewModel.numberStats.collectAsState()
    val selectedNumA by viewModel.selectedNumA.collectAsState()
    val relationsAB by viewModel.relationsAB.collectAsState()
    val backtestResults by viewModel.backtestResults.collectAsState()
    val selectedComboSize by viewModel.selectedComboSize.collectAsState()
    val combinations by viewModel.combinations.collectAsState()
    val randomnessResult by viewModel.randomnessResult.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = LotoSurface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .border(1.dp, LotoBorder)
                    .navigationBarsPadding()
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            if (tab != NavTab.PLUS) {
                                plusSubScreen = PlusSubScreen.MAIN
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LotoOrange,
                            selectedTextColor = LotoOrange,
                            unselectedIconColor = LotoTextMuted,
                            unselectedTextColor = LotoTextMuted,
                            indicatorColor = LotoSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = LotoBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.HOME -> {
                    HomeScreen(
                        draws = allDraws,
                        customDraws = customDraws,
                        numberStats = numberStats,
                        onAddDraw = { date, nums -> viewModel.addSingleDraw(date, nums) },
                        onMassImport = { text -> viewModel.importMassDraws(text) },
                        onDeleteCustomDraw = { id -> viewModel.deleteCustomDraw(id) },
                        onClearCustomDraws = { viewModel.clearAllCustomDraws() },
                        onNavigateToAnalysis = { currentTab = NavTab.ANALYSIS },
                        onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                    )
                }

                NavTab.ANALYSIS -> {
                    AnalysisScreen(
                        numberStats = numberStats,
                        userTier = userTier,
                        onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                    )
                }

                NavTab.RELATIONS -> {
                    RelationsScreen(
                        selectedNumA = selectedNumA,
                        relations = relationsAB,
                        userTier = userTier,
                        onSelectNumA = { viewModel.setSelectedNumA(it) },
                        onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                    )
                }

                NavTab.BACKTEST -> {
                    BacktestScreen(
                        backtestResults = backtestResults,
                        isCalculating = isCalculating,
                        userTier = userTier,
                        onRunBacktest = { window -> viewModel.triggerWalkForwardBacktest(window) },
                        onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                    )
                }

                NavTab.PLUS -> {
                    when (plusSubScreen) {
                        PlusSubScreen.MAIN -> {
                            PlusScreen(
                                userTier = userTier,
                                onNavigateToFrequencies = { plusSubScreen = PlusSubScreen.FREQUENCIES },
                                onNavigateToCombinations = { plusSubScreen = PlusSubScreen.COMBINATIONS },
                                onNavigateToReportsGrids = { plusSubScreen = PlusSubScreen.REPORTS_GRIDS },
                                onNavigateToHistory = { plusSubScreen = PlusSubScreen.HISTORY },
                                onNavigateToRandomness = { plusSubScreen = PlusSubScreen.RANDOMNESS },
                                onNavigateToAdmin = { plusSubScreen = PlusSubScreen.ADMIN }
                            )
                        }

                        PlusSubScreen.FREQUENCIES -> {
                            FrequenciesScreen(
                                draws = allDraws,
                                userTier = userTier,
                                onBack = { plusSubScreen = PlusSubScreen.MAIN },
                                onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                            )
                        }

                        PlusSubScreen.COMBINATIONS -> {
                            CombinationsScreen(
                                selectedComboSize = selectedComboSize,
                                combinations = combinations,
                                isCalculating = isCalculating,
                                userTier = userTier,
                                onSelectComboSize = { viewModel.setSelectedComboSize(it) },
                                onBack = { plusSubScreen = PlusSubScreen.MAIN },
                                onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                            )
                        }

                        PlusSubScreen.REPORTS_GRIDS -> {
                            ReportsGridsScreen(
                                draws = allDraws,
                                numberStats = numberStats,
                                userTier = userTier,
                                onBack = { plusSubScreen = PlusSubScreen.MAIN },
                                onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                            )
                        }

                        PlusSubScreen.HISTORY -> {
                            HistoryScreen(
                                draws = allDraws,
                                userTier = userTier,
                                onBack = { plusSubScreen = PlusSubScreen.MAIN },
                                onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                            )
                        }

                        PlusSubScreen.RANDOMNESS -> {
                            RandomnessScreen(
                                result = randomnessResult,
                                userTier = userTier,
                                onBack = { plusSubScreen = PlusSubScreen.MAIN },
                                onNavigateToPremium = { currentTab = NavTab.PREMIUM }
                            )
                        }

                        PlusSubScreen.ADMIN -> {
                            AdminScreen(
                                isAdminLoggedIn = isAdminLoggedIn,
                                adminEmail = adminEmail,
                                codesList = adminCodes,
                                isLoading = isLoadingAdmin,
                                createdCode = createdCodeResult,
                                onLogin = { email, pass -> viewModel.loginAdmin(email, pass) },
                                onLogout = { viewModel.logoutAdmin() },
                                onRefresh = { viewModel.refreshAdminCodes() },
                                onCreateCode = { tier, note -> viewModel.createCode(tier, note) },
                                onDismissCreatedCode = { viewModel.dismissCreatedCode() },
                                onRevokeCode = { codeId -> viewModel.revokeCode(codeId) },
                                onProlongerCode = { codeId, days -> viewModel.prolongerCode(codeId, days) },
                                onResetDevices = { codeId -> viewModel.resetCodeDevices(codeId) },
                                onBack = { plusSubScreen = PlusSubScreen.MAIN }
                            )
                        }
                    }
                }

                NavTab.PREMIUM -> {
                    PremiumScreen(
                        currentTier = userTier,
                        activeCode = activationCode,
                        expirationTimestamp = expirationTimestamp,
                        deviceId = viewModel.deviceId,
                        isActivating = isActivating,
                        activationError = activationError,
                        isOfflineGraceActive = isOfflineGraceActive,
                        remainingOfflineHours = remainingOfflineHours,
                        onActivateCode = { viewModel.saveActivationCode(it) },
                        onResetCode = { viewModel.clearActivationCode() },
                        onOpenAdmin = {
                            currentTab = NavTab.PLUS
                            plusSubScreen = PlusSubScreen.ADMIN
                        }
                    )
                }
            }
        }
    }
}
