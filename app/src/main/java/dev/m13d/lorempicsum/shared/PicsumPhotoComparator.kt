package dev.m13d.lorempicsum.shared

import androidx.recyclerview.widget.DiffUtil
import dev.m13d.lorempicsum.data.PicsumPhoto

class PicsumPhotoComparator : DiffUtil.ItemCallback<PicsumPhoto>() {

    override fun areItemsTheSame(oldItem: PicsumPhoto, newItem: PicsumPhoto) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: PicsumPhoto, newItem: PicsumPhoto) =
        oldItem == newItem
}