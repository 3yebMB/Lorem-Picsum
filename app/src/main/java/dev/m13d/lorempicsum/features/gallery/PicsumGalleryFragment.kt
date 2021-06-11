package dev.m13d.lorempicsum.features.gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.m13d.lorempicsum.MainActivity
import dev.m13d.lorempicsum.R
import dev.m13d.lorempicsum.databinding.FragmentGalleryPicsumBinding
import dev.m13d.lorempicsum.shared.PicsumPhotoListAdapter
import dev.m13d.lorempicsum.util.Resource
import dev.m13d.lorempicsum.util.exhaustive
import dev.m13d.lorempicsum.util.showSnackbar
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PicsumGalleryFragment : Fragment(R.layout.fragment_gallery_picsum),
    MainActivity.OnBottomNavigationFragmentReselectedListener {

    private val viewModel: PicsumGalleryViewModel by viewModels()

    private var currentBinding: FragmentGalleryPicsumBinding? = null
    private val binding get() = currentBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBinding = FragmentGalleryPicsumBinding.bind(view)

        val photoAdapter = PicsumPhotoListAdapter(
            onItemClick = { photo ->
                val uri = Uri.parse(photo.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkClick = { photo ->
                viewModel.onBookmarkClick(photo)
            }
        )

        photoAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {
            recyclerView.apply {
                adapter = photoAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.picsumPhotos.collect {
                    val result = it ?: return@collect

                    swipeRefreshLayout.isRefreshing = result is Resource.Loading
                    recyclerView.isVisible = !result.data.isNullOrEmpty()
                    textViewError.isVisible = result.error != null && result.data.isNullOrEmpty()
                    buttonRetry.isVisible = result.error != null && result.data.isNullOrEmpty()
                    textViewError.text = getString(
                        R.string.could_not_refresh,
                        result.error?.localizedMessage
                            ?: getString(R.string.unknown_error_occurred)
                    )

                    photoAdapter.submitList(result.data) {
                        if (viewModel.pendingScrollToTopAfterRefresh) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            buttonRetry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect { event ->
                    when (event) {
                        is PicsumGalleryViewModel.Event.ShowErrorMessage ->
                            showSnackbar(
                                getString(
                                    R.string.could_not_refresh,
                                    event.error.localizedMessage
                                        ?: getString(R.string.unknown_error_occurred)
                                )
                            )
                    }.exhaustive
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_picsum_gallery, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }
}