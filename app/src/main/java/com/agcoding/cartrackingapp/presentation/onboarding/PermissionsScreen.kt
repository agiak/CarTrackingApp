package com.agcoding.cartrackingapp.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.agcoding.cartrackingapp.R

@Composable
fun PermissionsScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val permissions = viewModel.permissions

    // Track which permissions are granted
    val permissionStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            permissions.forEach { permission ->
                this[permission.permission] = ContextCompat.checkSelfPermission(
                    context,
                    permission.permission
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
    }

    var showSettingsPrompt by remember { mutableStateOf(false) }
    var permissionsRequested by remember { mutableStateOf(false) }
    var pendingPermissionIndex by remember { mutableIntStateOf(-1) }

    // Get the list of permissions to request (filtered for Android version)
    val permissionsToRequest = remember {
        permissions.filter { permission ->
            // Skip notifications on older Android
            if (permission.permission == Manifest.permission.POST_NOTIFICATIONS &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                false
            } else {
                true
            }
        }
    }

    // Store the launcher reference for use in callbacks
    var launcherRef: ((String) -> Unit)? by remember { mutableStateOf(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Update state for the permission we just requested
        val requestedPermission = permissionsToRequest.getOrNull(pendingPermissionIndex)
        if (requestedPermission != null) {
            permissionStates[requestedPermission.permission] = isGranted
        }

        // Find and request the next permission
        var nextIndex = pendingPermissionIndex + 1
        while (nextIndex < permissionsToRequest.size) {
            val permission = permissionsToRequest[nextIndex]
            if (permissionStates[permission.permission] != true) {
                pendingPermissionIndex = nextIndex
                launcherRef?.invoke(permission.permission)
                return@rememberLauncherForActivityResult
            }
            nextIndex++
        }

        // All permissions processed
        pendingPermissionIndex = -1
        permissionsRequested = true
        showSettingsPrompt = permissionStates.any { !it.value }
    }

    // Set the launcher reference
    launcherRef = { permission -> permissionLauncher.launch(permission) }

    // Function to start requesting permissions
    fun startPermissionRequests() {
        var startIndex = 0
        while (startIndex < permissionsToRequest.size) {
            val permission = permissionsToRequest[startIndex]
            if (permissionStates[permission.permission] != true) {
                pendingPermissionIndex = startIndex
                permissionLauncher.launch(permission.permission)
                return
            }
            startIndex++
        }
        // All already granted
        pendingPermissionIndex = -1
        permissionsRequested = true
    }

    val isRequestingPermissions = pendingPermissionIndex >= 0

    // Check if all permissions are already granted
    val allPermissionsGranted = permissionsToRequest.all {
        permissionStates[it.permission] == true
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = screenWidthDp >= 600 || isLandscape

    if (useSplitView) {
        // Split view for tablets and landscape
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left side: Header and info (35%)
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Title
                    Text(
                        text = "App Permissions",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = if (allPermissionsGranted)
                            "All permissions have been granted!"
                        else
                            "To provide the best experience, we need a few permissions",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Right side: Permission cards and actions (65%)
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Permission cards
                    permissionsToRequest.forEach { permission ->
                        PermissionCard(
                            permission = permission,
                            isGranted = permissionStates[permission.permission] ?: false
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                // Settings prompt when permissions were denied
                AnimatedVisibility(
                    visible = showSettingsPrompt && permissionsRequested && !allPermissionsGranted,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Some permissions were denied",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You can enable them later in Settings",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Open Settings")
                            }
                        }
                    }
                }

                // Action buttons
                if (allPermissionsGranted) {
                    Button(
                        onClick = {
                            viewModel.onPermissionsHandled()
                            onComplete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Continue",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.onPermissionsHandled()
                                onComplete()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isRequestingPermissions
                        ) {
                            Text("Skip for now")
                        }

                        Button(
                            onClick = {
                                if (permissionsRequested) {
                                    viewModel.onPermissionsHandled()
                                    onComplete()
                                } else {
                                    startPermissionRequests()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isRequestingPermissions
                        ) {
                            Text(
                                text = if (permissionsRequested) "Continue" else "Grant Permissions",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                }
            }
        }
    } else {
        // Original single-column layout for portrait phones
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "App Permissions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = if (allPermissionsGranted)
                    "All permissions have been granted!"
                else
                    "To provide the best experience, we need a few permissions",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permission cards
            permissionsToRequest.forEach { permission ->
                PermissionCard(
                    permission = permission,
                    isGranted = permissionStates[permission.permission] ?: false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

        // Settings prompt when permissions were denied
        AnimatedVisibility(
            visible = showSettingsPrompt && permissionsRequested && !allPermissionsGranted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Some permissions were denied",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You can enable them later in Settings",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open Settings")
                    }
                }
            }
        }

        // Action buttons
        if (allPermissionsGranted) {
            // Only show Continue button when all permissions are granted
            Button(
                onClick = {
                    viewModel.onPermissionsHandled()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Continue",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Skip button - only show if not all permissions are granted
                TextButton(
                    onClick = {
                        viewModel.onPermissionsHandled()
                        onComplete()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRequestingPermissions
                ) {
                    Text("Skip for now")
                }

                // Grant / Continue button
                Button(
                    onClick = {
                        if (permissionsRequested) {
                            // Already requested, just continue
                            viewModel.onPermissionsHandled()
                            onComplete()
                        } else {
                            // Start requesting permissions from the first one
                            startPermissionRequests()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isRequestingPermissions
                ) {
                    Text(
                        text = if (permissionsRequested) "Continue" else "Grant Permissions",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

            Spacer(modifier = Modifier.weight(1f))

            // Settings prompt when permissions were denied
            AnimatedVisibility(
                visible = showSettingsPrompt && permissionsRequested && !allPermissionsGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Some permissions were denied",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can enable them later in Settings",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Open Settings")
                        }
                    }
                }
            }

            // Action buttons
            if (allPermissionsGranted) {
                Button(
                    onClick = {
                        viewModel.onPermissionsHandled()
                        onComplete()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Continue",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = {
                            viewModel.onPermissionsHandled()
                            onComplete()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isRequestingPermissions
                    ) {
                        Text("Skip for now")
                    }

                    Button(
                        onClick = {
                            if (permissionsRequested) {
                                viewModel.onPermissionsHandled()
                                onComplete()
                            } else {
                                startPermissionRequests()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isRequestingPermissions
                    ) {
                        Text(
                            text = if (permissionsRequested) "Continue" else "Grant Permissions",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionItem,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Permission icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = permission.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Permission details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(permission.titleRes),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(permission.descriptionRes),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // Granted indicator
            if (isGranted) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.granted),
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
