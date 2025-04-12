package com.darksphere.duplicatescanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.darksphere.duplicatescanner.data.BarcodeList
import com.darksphere.duplicatescanner.ui.CameraScreen
import com.darksphere.duplicatescanner.ui.MainViewModel
import com.darksphere.duplicatescanner.ui.theme.DuplicateScannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DuplicateScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "lists") {
                        composable("lists") {
                            ListsScreen(
                                onNavigateToCamera = { listId ->
                                    navController.navigate("camera/$listId")
                                },
                                onNavigateToBarcodes = { listId ->
                                    navController.navigate("barcodes/$listId")
                                }
                            )
                        }
                        composable("camera/{listId}") { backStackEntry ->
                            val listId = backStackEntry.arguments?.getString("listId")?.toLongOrNull()
                            CameraScreen(
                                listId = listId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("barcodes/{listId}") { backStackEntry ->
                            val listId = backStackEntry.arguments?.getString("listId")?.toLongOrNull()
                            BarcodesScreen(
                                listId = listId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onNavigateToCamera: (Long) -> Unit,
    onNavigateToBarcodes: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var showCreateListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<BarcodeList?>(null) }
    
    val lists by viewModel.barcodeLists.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Barcode Scanner") },
            actions = {
                IconButton(onClick = { showCreateListDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new list"
                    )
                }
            }
        )

        if (lists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Create a list to start scanning barcodes")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(lists) { list ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = list.name,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(onClick = { onNavigateToBarcodes(list.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "View barcodes"
                                    )
                                }
                                IconButton(onClick = { onNavigateToCamera(list.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Scan barcode"
                                    )
                                }
                                IconButton(onClick = { showDeleteDialog = list }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete list"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateListDialog) {
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = { Text("Create New List") },
            text = {
                TextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            viewModel.createList(newListName)
                            showCreateListDialog = false
                            newListName = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { list ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete List") },
            text = { Text("Are you sure you want to delete '${list.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteList(list.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodesScreen(
    listId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val barcodes by viewModel.barcodes.collectAsState(initial = emptyList())
    val lists by viewModel.barcodeLists.collectAsState(initial = emptyList())
    val currentList = lists.find { it.id == listId }

    LaunchedEffect(listId) {
        listId?.let { viewModel.loadBarcodesForList(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(currentList?.name ?: "Barcodes") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(barcodes) { barcode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = barcode.value,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}