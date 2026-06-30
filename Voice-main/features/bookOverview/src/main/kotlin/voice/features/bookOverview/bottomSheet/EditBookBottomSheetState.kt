package voice.features.bookOverview.bottomSheet

import androidx.annotation.StringRes
import voice.core.strings.R as StringsR

internal data class EditBookBottomSheetState(val items: List<BottomSheetItem>)

enum class BottomSheetItem(
  @StringRes val titleRes: Int,
) {
  Title(StringsR.string.book_edit_name_label),
  InternetCover(StringsR.string.book_edit_cover_internet),
  FileCover(StringsR.string.book_edit_cover_file),
  DeleteBook(StringsR.string.book_delete_bottom_sheet_title),
  BookCategoryMarkAsNotStarted(StringsR.string.book_category_action_mark_not_started),
  BookCategoryMarkAsCurrent(StringsR.string.book_category_action_mark_current),
  BookCategoryMarkAsCompleted(StringsR.string.book_category_action_mark_completed),
}
