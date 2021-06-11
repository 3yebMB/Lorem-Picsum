package dev.m13d.lorempicsum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.m13d.lorempicsum.databinding.ActivityMainBinding
import dev.m13d.lorempicsum.features.bookmarks.BookmarksFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.m13d.lorempicsum.features.gallery.PicsumGalleryFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var picsumGalleryFragment: PicsumGalleryFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    private val fragments: Array<Fragment>
        get() = arrayOf(
            picsumGalleryFragment,
            bookmarksFragment
        )

    private var selectedIndex = 0

    private val selectedFragment get() = fragments[selectedIndex]

    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (selectedFragment == fragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        title = when (selectedFragment) {
            is PicsumGalleryFragment -> getString(R.string.title_picsum)
            is BookmarksFragment -> getString(R.string.title_bookmarks)
            else -> ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            picsumGalleryFragment = PicsumGalleryFragment()
            bookmarksFragment = BookmarksFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, picsumGalleryFragment, TAG_GALLERY_FRAGMENT)
                .add(R.id.fragment_container, bookmarksFragment, TAG_BOOKMARKS_FRAGMENT)
                .commit()
        } else {
            picsumGalleryFragment =
                supportFragmentManager.findFragmentByTag(TAG_GALLERY_FRAGMENT) as PicsumGalleryFragment
            bookmarksFragment =
                supportFragmentManager.findFragmentByTag(TAG_BOOKMARKS_FRAGMENT) as BookmarksFragment

            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }

        selectFragment(selectedFragment)

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_gallery -> picsumGalleryFragment
                R.id.nav_bookmarks -> bookmarksFragment
                else -> throw IllegalArgumentException("Unexpected itemId")
            }

            if (selectedFragment === fragment) {
                if (fragment is OnBottomNavigationFragmentReselectedListener) {
                    fragment.onBottomNavigationFragmentReselected()
                }
            } else {
                selectFragment(fragment)
            }
            true
        }
    }

    interface OnBottomNavigationFragmentReselectedListener {
        fun onBottomNavigationFragmentReselected()
    }

    override fun onBackPressed() {
        if (selectedIndex != 0) {
            binding.bottomNav.selectedItemId = R.id.nav_gallery
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }
}

private const val TAG_GALLERY_FRAGMENT = "TAG_GALLERY_FRAGMENT"
private const val TAG_BOOKMARKS_FRAGMENT = "TAG_BOOKMARKS_FRAGMENT"
private const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"