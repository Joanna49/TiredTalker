package com.maciejka.tiredtalker.api

import com.google.gson.annotations.SerializedName

data class RemainingWordsResponse(
    @SerializedName("remaining_words") val remaining_words: Int
)
