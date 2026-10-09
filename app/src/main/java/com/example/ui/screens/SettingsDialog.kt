package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ad.BillingManager
import com.example.ad.UMPManager
import com.example.data.GamePreferences
import com.example.ui.components.PrivacyPolicyDialog

@Composable
fun SettingsDialog(
    preferences: GamePreferences,
    billingManager: BillingManager,
    umpManager: UMPManager,
    isSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isSymbolsEnabled: Boolean,
    isAdsRemoved: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Sound Toggle
                SettingsToggleRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = stringResource(R.string.sound_effects),
                    checked = isSoundEnabled,
                    onCheckedChange = { preferences.setSoundEnabled(it) }
                )

                // Vibration Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Vibration,
                    title = stringResource(R.string.vibration),
                    checked = isVibrationEnabled,
                    onCheckedChange = { preferences.setVibrationEnabled(it) }
                )

                // Colorblind Symbols Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Visibility,
                    title = stringResource(R.string.colorblind_symbols),
                    checked = isSymbolsEnabled,
                    onCheckedChange = { preferences.setColorblindSymbolsEnabled(it) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = DividerDefaults.color.copy(alpha = 0.5f)
                )

                // Monetization / In-App Purchase
                val formattedPrice by billingManager.formattedPrice.collectAsStateWithLifecycle()

                if (isAdsRemoved) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.ads_removed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    val removeAdsLabel = if (!formattedPrice.isNullOrBlank()) {
                        "${stringResource(R.string.remove_ads)} ($formattedPrice)"
                    } else {
                        stringResource(R.string.remove_ads)
                    }

                    Button(
                        onClick = {
                            if (context is Activity) {
                                billingManager.launchBillingFlow(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("remove_ads_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(removeAdsLabel)
                    }
                }

                // Restore Purchases
                OutlinedButton(
                    onClick = {
                        billingManager.queryExistingPurchases { found ->
                            val msg = if (found) {
                                context.getString(R.string.purchase_restored)
                            } else {
                                context.getString(R.string.purchase_not_found)
                            }
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("restore_purchases_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.restore_purchases))
                }

                // Privacy Settings (UMP Consent)
                OutlinedButton(
                    onClick = {
                        if (context is Activity) {
                            umpManager.showPrivacyOptionsForm(context) {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("privacy_settings_button")
                ) {
                    Icon(Icons.Default.PrivacyTip, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.privacy_settings))
                }

                // Privacy Policy Link
                OutlinedButton(
                    onClick = { showPrivacyPolicy = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("privacy_policy_button")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.privacy_policy))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_settings_button")
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
