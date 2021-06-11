package dev.m13d.lorempicsum.shared

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.m13d.lorempicsum.R
import dev.m13d.lorempicsum.data.PicsumPhoto
import dev.m13d.lorempicsum.databinding.ItemPicsumPhotoBinding

class PicsumPhotoViewHolder(
    private val binding: ItemPicsumPhotoBinding,
    private val onItemClick: (Int) -> Unit,
    private val onBookmarkClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(article: PicsumPhoto) {
        binding.apply {
            Glide.with(itemView)
                .load(article.download_url)
                .error(R.drawable.ic_picsum)
                .into(imageView)

            textViewTitle.text = article.id ?: ""

            imageViewBookmark.setImageResource(
                when {
                    article.isBookmarked -> R.drawable.ic_bookmark_selected
                    else -> R.drawable.ic_bookmark_unselected
                }
            )
        }
    }

    init {
        binding.apply {
            root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
            imageViewBookmark.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookmarkClick(position)
                }
            }
        }
    }
}