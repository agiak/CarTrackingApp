package com.agcoding.cartrackingapp.presentation.settings.permissions
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import androidx.compose.ui.tooling.preview.Preview

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.onboarding.AppPermissions
import com.agcoding.cartrackingapp.presentation.onboarding.PermissionItem
import com.agcoding.cartrackingapp.util.PermissionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val permissionsToShow = remember {
        AppPermissions.permissions.filter { item ->
            item.permission != Manifest.permission.POST_NOTIFICATIONS ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }
    }

    // Track granted state per permission
    val grantedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            permissionsToShow.forEach { item ->
                this[item.permission] = ContextCompat.checkSelfPermission(
                    context, item.permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    // Track whether each permission has been attempted (to detect permanent denial)
    val attemptedState = remember { mutableStateMapOf<String, Boolean>() }

    // Which permission is pending a launcher result
    var pendingPermission by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingPermission?.let { perm ->
            grantedState[perm] = isGranted
            attemptedState[perm] = true
        }
        pendingPermission = null
    }

    // Re-check on resume (e.g. returning from system settings)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsToShow.forEach { item ->
                    grantedState[item.permission] = ContextCompat.checkSelfPermission(
                        context, item.permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.permissions_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.permissions_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            permissionsToShow.forEach { item ->
                val isGranted = grantedState[item.permission] == true
                val wasAttempted = attemptedState[item.permission] == true
                val canRequestAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, item.permission
                )
                // Permanently denied = attempted at least once AND rationale is false AND not granted
                val isPermanentlyDenied = wasAttempted && !isGranted && !canRequestAgain

                PermissionRow(
                    item = item,
                    isGranted = isGranted,
                    isPermanentlyDenied = isPermanentlyDenied,
                    onGrant = {
                        pendingPermission = item.permission
                        launcher.launch(item.permission)
                    },
                    onOpenSystemSettings = {
                        PermissionUtil.openAppSettings(context)
                    }
                )
            }

            val allGranted = permissionsToShow.all { grantedState[it.permission] == true }
            if (allGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.all_permissions_granted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionsSettingsScreenPreview() {
    CarTrackingAppTheme {
        PermissionsSettingsScreen(onNavigateBack = {})
    }
}
