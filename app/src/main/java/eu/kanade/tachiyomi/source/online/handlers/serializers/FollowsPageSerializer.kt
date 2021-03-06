package eu.kanade.tachiyomi.source.online.handlers.serializers

import kotlinx.serialization.Serializable

@Serializable
data class FollowsPageSerializer(
    val code: Int,
    val data: List<FollowPage>? = null
)

@Serializable
data class FollowsIndividualSerializer(
    val code: Int,
    val data: FollowPage? = null
)

@Serializable
data class FollowPage(
    val mangaTitle: String,
    val chapter: String,
    val followType: Int,
    val mangaId: Int,
    val volume: String
)
