package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import eu.kanade.domain.sync.SyncPreferences
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.tachiyomi.data.sync.SyncDataJob
import eu.kanade.tachiyomi.data.sync.models.SyncTriggerOptions
import eu.kanade.tachiyomi.data.sync.service.ZAppsAccount
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

@Composable
internal fun getZAppsAccountPreferences(syncPreferences: SyncPreferences): List<Preference> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val account = remember { ZAppsAccount(syncPreferences) }
    val sessionValue by syncPreferences.zAppsSession.collectAsState()
    val session = remember(sessionValue) { account.savedSession() }
    var showSignIn by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    if (showSignIn) {
        // Deliberately not rememberSaveable: a password must not enter saved instance state.
        var email by remember { mutableStateOf(session?.user?.email.orEmpty()) }
        var password by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!busy) showSignIn = false },
            title = { Text(stringResource(MR.strings.zink_zapps_sign_in)) },
            text = {
                Column {
                    Text(stringResource(MR.strings.zink_zapps_account_info))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(MR.strings.zink_account_email)) },
                        enabled = !busy,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(MR.strings.zink_account_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !busy,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                account.signIn(email, password)
                                password = ""
                                if (syncPreferences.syncInterval.get() == 0) syncPreferences.syncInterval.set(60)
                                val triggers = syncPreferences.getSyncTriggerOptions()
                                if (!triggers.syncOnChapterRead && !triggers.syncOnChapterOpen &&
                                    !triggers.syncOnAppStart && !triggers.syncOnAppResume
                                ) {
                                    syncPreferences.setSyncTriggerOptions(
                                        SyncTriggerOptions(
                                            syncOnChapterRead = true,
                                            syncOnChapterOpen = false,
                                            syncOnAppStart = true,
                                            syncOnAppResume = true,
                                        ),
                                    )
                                }
                                SyncDataJob.setupTask(context)
                                showSignIn = false
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                error = e.message ?: "Unable to sign in. Try again."
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text(stringResource(MR.strings.zink_zapps_sign_in)) }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { showSignIn = false }) {
                    Text(stringResource(MR.strings.action_cancel))
                }
            },
        )
    }

    return listOf(
        Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.zink_cloud_sync_info)),
        Preference.PreferenceItem.TextPreference(
            title = stringResource(MR.strings.zink_zapps_sign_in),
            subtitle = session?.user?.email ?: stringResource(MR.strings.zink_zapps_account_info),
            enabled = !busy,
            onClick = { showSignIn = true },
        ),
    ) + if (session != null) {
        listOf(
            Preference.PreferenceItem.TextPreference(
                title = stringResource(MR.strings.zink_zapps_sign_out),
                enabled = !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        try {
                            account.signOut()
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            context.toast("Signed out locally. The server could not be reached.")
                        } finally {
                            SyncDataJob.setupTask(context)
                            busy = false
                        }
                    }
                },
            ),
        )
    } else {
        emptyList()
    }
}
