package dev.m13d.lorempicsum.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import dev.m13d.lorempicsum.data.PicsumPhoto
import dev.m13d.lorempicsum.databinding.ItemPicsumPhotoBinding

class PicsumPhotoListAdapter(
    private val onItemClick: (PicsumPhoto) -> Unit,
    private val onBookmarkClick: (PicsumPhoto) -> Unit
) : ListAdapter<PicsumPhoto, PicsumPhotoViewHolder>(PicsumPhotoComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicsumPhotoViewHolder {
        val binding =
            ItemPicsumPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PicsumPhotoViewHolder(binding,
            onItemClick = { position ->
                val photo = getItem(position)
                if (photo != null) {
                    onItemClick(photo)
                }
            },
            onBookmarkClick = { position ->
                val photo = getItem(position)
                if (photo != null) {
                    onBookmarkClick(photo)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: PicsumPhotoViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
}