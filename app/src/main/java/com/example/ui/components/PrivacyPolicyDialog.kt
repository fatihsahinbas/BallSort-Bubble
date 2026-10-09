package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import java.util.Locale

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val isTurkish = Locale.getDefault().language.equals("tr", ignoreCase = true)

    val title = if (isTurkish) "Gizlilik Politikası" else "Privacy Policy"

    val content = if (isTurkish) {
        """
Top Sıralama Bulmacası (Ball Sort Puzzle) Gizlilik Politikası

Son Güncelleme: Ekim 2026

Bu gizlilik politikası, "Top Sıralama Bulmacası" mobil uygulamamızın kullanıcı verilerini nasıl işlediğini açıklamaktadır.

1. Kişisel Veri Toplanmaması
Uygulamamız tamamen çevrimdışı (offline) çalışacak şekilde tasarlanmıştır. Kullanıcı hesabı, kayıt, giriş, bulut yedekleme veya herhangi bir kişisel veri toplama mekanizması içermez. İsim, e-posta, konum veya benzeri kişisel verileriniz asla toplanmaz veya sunucularımıza iletilmez.

2. Cihaz İçi Yerel Depolama
Oyun ilerlemeniz (açılan bölümler, ses ve titreşim tercihleri, reklam kaldırma durumu) yalnızca cihazınızın yerel hafızasında (SharedPreferences) saklanır. Cihazınızdan dışarı aktarılmaz.

3. Kullanılan İzinler
Uygulamamız yalnızca şu temel izinleri kullanır:
- INTERNET & ACCESS_NETWORK_STATE: Yalnızca Google AdMob reklamlarının ve Google Play faturalandırma hizmetinin çalışması için.
- com.google.android.gms.permission.AD_ID: Google reklam kimliği için standart Play Hizmetleri izni.
Kamera, mikrofon, konum, rehber veya dosya erişimi gibi hiçbir hassas izin istenmez.

4. Üçüncü Taraf Hizmetleri ve SDK'lar
Uygulamamız yalnızca güvenilir Google hizmetlerini içerir:
- Google Mobile Ads (AdMob) ve Google UMP: Kişiselleştirilmiş veya kişiselleştirilmemiş reklam gösterimi ve rıza yönetimi için.
- Google Play Faturalandırma: "Reklamları Kaldır" tek seferlik uygulama içi satın alımının güvenle işlenmesi için.
Uygulamamızda hiçbir üçüncü taraf analiz (Analytics) veya çökme raporlama (Crashlytics vb.) SDK'sı bulunmamaktadır.

5. Rıza ve Gizlilik Ayarları
Avrupa Ekonomik Alanı (AEA) ve Birleşik Krallık kullanıcıları için Google UMP rıza formu sunulur. Gizlilik tercihlerinizi oyun içi Ayarlar menüsünden dilediğiniz zaman güncelleyebilirsiniz.

İletişim:
Gizlilik politikası hakkında sorularınız için geliştiriciyle iletişime geçebilirsiniz.
        """.trimIndent()
    } else {
        """
Ball Sort Puzzle Privacy Policy

Last Updated: October 2026

This Privacy Policy explains how Ball Sort Puzzle handles user data.

1. No Personal Data Collection
Our game is designed to operate offline. There are no accounts, logins, registrations, or cloud databases. We do not collect, store, or share your personal information (such as name, email, contacts, or location).

2. Local Device Storage Only
Your gameplay progress (unlocked levels, sound/vibration toggles, ad-removal status) is stored strictly on your local device via Android SharedPreferences. No game data is ever sent to external servers.

3. Android Permissions
The application requires only the following standard permissions:
- INTERNET & ACCESS_NETWORK_STATE: Required solely for Google AdMob and Google Play Billing network connectivity.
- com.google.android.gms.permission.AD_ID: Standard Google Advertising ID permission for ad serving.
No sensitive permissions (camera, location, contacts, microphone, or storage) are requested.

4. Third-Party Services
The app uses only official Google SDKs:
- Google Mobile Ads (AdMob) & Google User Messaging Platform (UMP): For optional reward ads, interstitials, and user privacy consent management.
- Google Play Billing: For processing the one-time "Remove Ads" in-app purchase securely.
No third-party analytics or crash reporting SDKs are bundled.

5. Privacy Choices & Consent
You can manage or withdraw your advertising consent at any time via the "Privacy Settings" button inside the in-game Settings menu.

Contact:
If you have any questions regarding this Privacy Policy, please contact the developer.
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            val privacyUrl = "https://fatihsahinbas.github.io/ballsort/privacy"
            androidx.compose.foundation.layout.Row {
                TextButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(privacyUrl)
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Text(if (isTurkish) "Web'de Aç" else "Open in Browser")
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    )
}
