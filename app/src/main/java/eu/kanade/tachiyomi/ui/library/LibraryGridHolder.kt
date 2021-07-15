package eu.kanade.tachiyomi.ui.library

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.clear
import coil.size.Precision
import coil.size.Scale
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.image.coil.loadManga
import eu.kanade.tachiyomi.databinding.MangaGridItemBinding
import eu.kanade.tachiyomi.util.lang.highlightText

/**
 * Class used to hold the displayed data of a manga in the library, like the cover or the title.
 * All the elements from the layout file "item_catalogue_grid" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @param adapter the adapter handling this holder.
 * @param listener a listener to react to single tap and long tap events.
 * @constructor creates a new library holder.
 */
class LibraryGridHolder(
    private val view: View,
    adapter: LibraryCategoryAdapter,
    var width: Int,
    compact: Boolean,
    private var fixedSize: Boolean
) : LibraryHolder(view, adapter) {

    private val binding = MangaGridItemBinding.bind(view)
    init {
        binding.playLayout.setOnClickListener { playButtonClicked() }
        if (compact) {
            binding.textLayout.isVisible = false
        } else {
            binding.compactTitle.isVisible = false
            binding.gradient.isVisible = false
            val playLayout = binding.playLayout.layoutParams as FrameLayout.LayoutParams
            val buttonLayout = binding.playButton.layoutParams as FrameLayout.LayoutParams
            playLayout.gravity = Gravity.BOTTOM or Gravity.END
            buttonLayout.gravity = Gravity.BOTTOM or Gravity.END
            binding.playLayout.layoutParams = playLayout
            binding.playButton.layoutParams = buttonLayout
        }
    }

    /**
     * Method called from [LibraryCategoryAdapter.onBindViewHolder]. It updates the data for this
     * holder with the given manga.
     *
     * @param item the manga item to bind.
     */
    override fun onSetValues(item: LibraryItem) {
        // Update the title and subtitle of the manga.
        binding.constraintLayout.isVisible = !item.manga.isBlank()
        binding.title.text = item.manga.title.highlightText(item.filter, color)
        val authorArtist = if (item.manga.author == item.manga.artist || item.manga.artist.isNullOrBlank()) {
            item.manga.author?.trim() ?: ""
        } else {
            listOfNotNull(
                item.manga.author?.trim()?.takeIf { it.isNotBlank() },
                item.manga.artist?.trim()?.takeIf { it.isNotBlank() }
            ).joinToString(", ")
        }
        binding.subtitle.text = authorArtist.highlightText(item.filter, color)

        binding.compactTitle.text = binding.title.text?.toString()?.highlightText(item.filter, color)

        binding.title.post {
            val hasAuthorInFilter =
                item.filter.isNotBlank() && authorArtist.contains(item.filter, true)
            binding.subtitle.isVisible = binding.title.lineCount <= 1 || hasAuthorInFilter
            binding.title.maxLines = if (hasAuthorInFilter) 1 else 2
        }

        setUnreadBadge(binding.unreadDownloadBadge.badgeView, item)
        setReadingButton(item)

        // Update the cover.
        if (item.manga.thumbnail_url == null) binding.coverThumbnail.clear()
        else {
            if (binding.coverThumbnail.height == 0) {
                val oldPos = flexibleAdapterPosition
                adapter.recyclerView.post {
                    if (oldPos == flexibleAdapterPosition) {
                        setCover(item.manga)
                    }
                }
            } else setCover(item.manga)
        }
    }

    private fun setCover(manga: Manga) {
        if ((adapter.recyclerView.context as? Activity)?.isDestroyed == true) return
        binding.coverThumbnail.loadManga(manga) {
            if (!fixedSize) {
                precision(Precision.INEXACT)
                scale(Scale.FIT)
            }
        }
    }

    private fun playButtonClicked() {
        adapter.libraryListener.startReading(flexibleAdapterPosition)
    }

    override fun onActionStateChanged(position: Int, actionState: Int) {
        super.onActionStateChanged(position, actionState)
        if (actionState == 2) {
            binding.card.isDragged = true
            binding.unreadDownloadBadge.badgeView.isDragged = true
        }
    }

    override fun onItemReleased(position: Int) {
        super.onItemReleased(position)
        binding.card.isDragged = false
        binding.unreadDownloadBadge.badgeView.isDragged = false
    }
}
