package com.mdcapp.ui.screens.invoicesClientDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mdcapp.domain.entities.BillingModel
import com.mdcapp.domain.entities.toPrint
import com.mdcapp.domain.logic.ReportGenerator
import com.mdcapp.ui.composables.common.LoadingIndicator
import com.mdcapp.ui.utils.AppBackHandler
import com.mdcapp.ui.utils.shareText
import com.mdcapp.ui.viewmodels.invoices.InvoicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    vm: InvoicesViewModel,
    onNavigate: () -> Unit,
    onBack: () -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    AppBackHandler { onBack() }

    val state by vm.state.collectAsState()
    val brandSelected by vm.selectedBrand.collectAsState()
    val branchSelected by vm.selectedBranch.collectAsState()
    val typeSelected by vm.selectedType.collectAsState()

    val brands = state.brandList
    val branches = state.branchList
    val types = state.typeList

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cliente: ${state.client.clientName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigate()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!state.isLoading) {
                        IconButton(onClick = {
                            val report = ReportGenerator.generateCurrentAccountReport(
                                state.client.clientName,
                                state.documents
                            )
                            shareText(
                                report,
                                "Estado de Cuenta - ${state.client.clientName}"
                            )
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Compartir Cuenta Corriente"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            val isWide = this.maxWidth > 800.dp
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(true)
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                else -> {
                    DetailClientBalance(
                        documents = state.documents,
                        balance = state.balance,
                        pendingAmount = state.pendingReconciliationAmount,
                        invoicesWithPending = state.invoicesWithPendingReconciliation,
                        brandSelected = brandSelected,
                        branchSelected = branchSelected,
                        typeSelected = typeSelected,
                        brands = brands,
                        branches = branches,
                        types = types,
                        isWideScreen = isWide,
                        onBrandSelect = { vm.setBrand(it) },
                        onBranchSelect = { vm.setBranch(it) },
                        onTypeSelect = { vm.setType(it) }
                    ) {
                        onInvoiceClick(it)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailClientBalance(
    documents: List<BillingModel>,
    balance: Double,
    pendingAmount: Double,
    invoicesWithPending: Set<String>,
    brandSelected: String,
    branchSelected: String,
    typeSelected: String,
    brands: List<String>,
    branches: List<String>,
    types: List<String>,
    isWideScreen: Boolean = false,
    onBrandSelect: (String) -> Unit,
    onBranchSelect: (String) -> Unit,
    onTypeSelect: (String) -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    var expandedBrand by remember { mutableStateOf(false) }
    var expandedBranch by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        if (isWideScreen) {
            // Layout de 2 columnas para Desktop
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Columna Izquierda: Filtros y Saldo
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        "Opciones de Filtro",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            FilterSection(
                                brandSelected,
                                brands,
                                expandedBrand,
                                { expandedBrand = it },
                                onBrandSelect,
                                branchSelected,
                                branches,
                                expandedBranch,
                                { expandedBranch = it },
                                onBranchSelect,
                                typeSelected,
                                types,
                                expandedType,
                                { expandedType = it },
                                onTypeSelect,
                                isVertical = true
                            )
                        }
                    }

                    BalanceCard(balance, pendingAmount, isWideScreen = true)
                }

                // Columna Derecha: Documentos
                Column(
                    modifier = Modifier
                        .weight(3f)
                        .padding(end = 24.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Documentos en cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    DocumentList(
                        documents = documents,
                        invoicesWithPending = invoicesWithPending,
                        wideMode = true
                    ) { onInvoiceClick(it) }
                }
            }
        } else {
            // Layout original para móvil
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterSection(
                    brandSelected,
                    brands,
                    expandedBrand,
                    { expandedBrand = it },
                    onBrandSelect,
                    branchSelected,
                    branches,
                    expandedBranch,
                    { expandedBranch = it },
                    onBranchSelect,
                    typeSelected,
                    types,
                    expandedType,
                    { expandedType = it },
                    onTypeSelect
                )

                BalanceCard(balance, pendingAmount)

                Text(
                    "Documentos en cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                DocumentList(
                    documents = documents,
                    invoicesWithPending = invoicesWithPending,
                    wideMode = false
                ) { onInvoiceClick(it) }
            }
        }
    }
}

@Composable
fun FilterSection(
    brandSelected: String,
    brands: List<String>,
    expandedBrand: Boolean,
    setExpandedBrand: (Boolean) -> Unit,
    onBrandSelect: (String) -> Unit,
    branchSelected: String,
    branches: List<String>,
    expandedBranch: Boolean,
    setExpandedBranch: (Boolean) -> Unit,
    onBranchSelect: (String) -> Unit,
    typeSelected: String,
    types: List<String>,
    expandedType: Boolean,
    setExpandedType: (Boolean) -> Unit,
    onTypeSelect: (String) -> Unit,
    isVertical: Boolean = false
) {
    if (isVertical) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FilterItem(
                "Fábrica",
                brandSelected,
                brands,
                expandedBrand,
                setExpandedBrand,
                onBrandSelect
            )
            Spacer(Modifier.height(12.dp))
            val isBranchEnabled = brandSelected != "Todas" && brandSelected.isNotEmpty()
            FilterItem(
                "Segmento",
                if (isBranchEnabled) branchSelected else "---",
                branches,
                expandedBranch,
                setExpandedBranch,
                onBranchSelect,
                enabled = isBranchEnabled
            )
            Spacer(Modifier.height(12.dp))
            FilterItem("Tipo", typeSelected, types, expandedType, setExpandedType, onTypeSelect)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FilterItem(
                    "Fábrica",
                    brandSelected,
                    brands,
                    expandedBrand,
                    setExpandedBrand,
                    onBrandSelect
                )
            }
            val isBranchEnabled = brandSelected != "Todas" && brandSelected.isNotEmpty()
            Box(modifier = Modifier.weight(1f)) {
                FilterItem(
                    "Segmento",
                    if (isBranchEnabled) branchSelected else "---",
                    branches,
                    expandedBranch,
                    setExpandedBranch,
                    onBranchSelect,
                    enabled = isBranchEnabled
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterItem("Tipo", typeSelected, types, expandedType, setExpandedType, onTypeSelect)
            }
        }
    }
}

@Composable
fun FilterItem(
    label: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            AssistChip(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onClick = { onExpandedChange(true) },
                label = {
                    Text(
                        text = selected.ifEmpty { "Todas" },
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, pendingAmount: Double, isWideScreen: Boolean = false) {
    val cardPadding = if (isWideScreen) 24.dp else 16.dp
    val cardShape = if (isWideScreen) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)

    androidx.compose.material3.ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isWideScreen) {
            // Desktop: Layout en Columna (Vertical) para aprovechar la barra lateral
            Column(
                modifier = Modifier.padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BalanceItem(
                    label = "SALDO REAL",
                    amount = balance,
                    style = MaterialTheme.typography.headlineSmall, // Tamaño profesional reducido
                    color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (pendingAmount > 0) {
                    BalanceItem(
                        label = "PENDIENTE IMPUTAR",
                        amount = pendingAmount,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFF9A825)
                    )
                }
            }
        } else {
            // Mobile: Layout en Fila (Horizontal) con texto adaptado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BalanceItem(
                    label = "SALDO REAL",
                    amount = balance,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (pendingAmount > 0) {
                    BalanceItem(
                        label = "PENDIENTE IMPUTAR",
                        amount = pendingAmount,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFF9A825),
                        horizontalAlignment = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceItem(
    label: String,
    amount: Double,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = horizontalAlignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = amount.toPrint(),
            style = style,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}
