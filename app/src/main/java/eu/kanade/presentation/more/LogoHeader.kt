package eu.kanade.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun LogoHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_zink_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(top = 40.dp, bottom = 16.dp)
                .size(64.dp)
                .background(Color(0xFF202124), RoundedCornerShape(16.dp)),
        )

        Text(
            text = stringResource(MR.strings.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}
