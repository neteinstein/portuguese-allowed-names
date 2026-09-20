package org.neteinstein.pickaname.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.neteinstein.pickaname.core.designsystem.R
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.presentation.theme.PickANameTheme

/**
 * Small colored badge indicating which [Gender] a name is approved for.
 *
 * Color convention (explicit design decision, not the Material default): male is blue,
 * female is pink, using the dedicated [PickANameTheme.extendedColors] roles rather than the
 * theme's secondary/tertiary brand colors, so the gender signal stays a stable, unambiguous
 * color everywhere it appears.
 */
@Composable
fun GenderTag(gender: Gender, modifier: Modifier = Modifier) {
    val extendedColors = PickANameTheme.extendedColors
    val containerColor = when (gender) {
        Gender.FEMALE -> extendedColors.femaleContainer
        Gender.MALE -> extendedColors.maleContainer
    }
    val contentColor = when (gender) {
        Gender.FEMALE -> extendedColors.onFemaleContainer
        Gender.MALE -> extendedColors.onMaleContainer
    }
    val label = when (gender) {
        Gender.FEMALE -> stringResource(R.string.gender_female)
        Gender.MALE -> stringResource(R.string.gender_male)
    }
    val icon = when (gender) {
        Gender.FEMALE -> Icons.Filled.Female
        Gender.MALE -> Icons.Filled.Male
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
