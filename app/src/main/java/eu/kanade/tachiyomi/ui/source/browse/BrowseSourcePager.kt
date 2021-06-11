package eu.kanade.tachiyomi.ui.source.browse

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangaListPage
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

open class BrowseSourcePager(val source: Source, val query: String, val filters: FilterList) :
    Pager() {

    override fun requestNext(): Observable<MangaListPage> {
        val page = currentPage

        val observable = if (query.isBlank() && filters.isEmpty()) {
            source.fetchPopularManga(page)
        } else {
            source.fetchSearchManga(page, query, filters)
        }

        return observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it.manga.isNotEmpty()) {
                    onPageReceived(it)
                } else {
                    throw NoResultsException()
                }
            }
    }
}
