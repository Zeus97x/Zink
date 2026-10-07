package eu.kanade.presentation.library

import java.text.Normalizer
import java.util.Locale

// Ignore presentation differences, not edition names, numbers, or title words.
internal fun duplicateTitleKey(title: String): String = Normalizer.normalize(title, Normalizer.Form.NFKC)
    .lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
