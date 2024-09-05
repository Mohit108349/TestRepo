package com.familypedia.view.dashboard.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.network.CharacterData
import com.familypedia.network.SearchRequest
import com.familypedia.utils.*
import com.familypedia.utils.listeners.RecentSearchesListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.viewmodels.CharacterViewModel

import kotlinx.coroutines.launch
import java.util.*
import android.view.inputmethod.EditorInfo

import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.familypedia.databinding.FragmentSearchBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "SearchFragment"

class SearchFragment : Fragment(), FamilyPediaClickListener, CharactersListener,
    SuccessFulListener, RecentSearchesListener {
    private var param1: String? = null
    private var param2: String? = null
    private var charactersAdapter: CharactersAdapter? = null
    private var characterViewModel: CharacterViewModel? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var searchText = ""
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private var timer: Timer = Timer()
    private val DELAY: Long = 1500 // Milliseconds
    private var  recentSearchesList= arrayListOf<String?>()
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    charactersList.clear()
                    charactersAdapter?.notifyDataSetChanged()
                    hitSearchCharacterAPI(searchText, false)
                    // charactersAdapter?.notifyDataSetChanged()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        setAdapter()
        initListeners()
        initViewModel()
        responseData()
        searchTextListener()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        //manageSomethingWentWrong(ErrorType.INIT)
    }

    private fun initListeners() {
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.somethingWrong.clSomthingWentWrong.familyPediaClickListener(this)
        binding.ivSearch.familyPediaClickListener(this)
        /*swipeRefreshLayout.setOnRefreshListener {
            charactersList.clear()
            charactersAdapter?.notifyDataSetChanged()
            hitSearchCharacterAPI(searchText)
        }*/
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.somethingWrong.tvTapToRetry-> {
                if (errorType == ErrorType.INIT) {
                    binding.etSearch.requestFocus()
                    requireActivity().showKeyboard(requireActivity())
                } else {
                    hitSearchCharacterAPI(searchText, false)
                }

            }
            binding.somethingWrong.clSomthingWentWrong-> {
                if (errorType == ErrorType.INIT) {
                    binding.etSearch.requestFocus()
                    requireActivity().showKeyboard(requireActivity())
                } else {
                    hitSearchCharacterAPI(searchText, false)
                }

            }

            binding.ivSearch->{
                searchText = binding.etSearch.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchCharacterAPI(searchText, false)
                }
            }
        }
    }

    private fun searchTextListener() {
        binding.etSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchText = binding.etSearch.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchCharacterAPI(searchText, false)
                }
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun setAdapter() {
        linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        charactersAdapter = CharactersAdapter(
            requireActivity(),
            charactersList,
            this,
            Constants.FROM_SEARCH_CHARACTERS,
            resultLauncher)
        binding.rvCharactersList.adapter = charactersAdapter
        binding.rvCharactersList.layoutManager = linearLayoutManager
        binding.rvCharactersList.addOnScrollListener(recyclerScrollListener)

        //
        Utility.getRecentSearchesCharacter(requireContext())?.let {
            recentSearchesList.addAll(it)
        }
        if (recentSearchesList != null && recentSearchesList.isNotEmpty()) {
            binding.llRecentSearches.showView()
            recentSearchesList?.reverse()
            val recentSearchAdapter = RecentSearchAdapter(requireContext(), recentSearchesList, this)
            binding.rvRecentSearches.adapter = recentSearchAdapter
        }else{
            binding.llRecentSearches.hideView()
            manageSomethingWentWrong(ErrorType.INIT)
        }
    }
    private fun clearData(){
        charactersList.clear()
        charactersAdapter?.notifyDataSetChanged()
    }

    //For Paging
    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = linearLayoutManager?.childCount
                    val totalItemCount = linearLayoutManager?.itemCount
                    val pastVisiblesItems = linearLayoutManager?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitSearchCharacterAPI(searchText, true)
                            }
                            loading = true
                        }
                    }
                }
            }
        }

    override fun onListSizeChanged(size: Int) {
    }

    override fun onEditCharacterClicked(characterData: CharacterData) {
    }

    override fun onRecentSearchClick(searchString: String) {
        binding.etSearch.setText(searchString)
        searchText = binding.etSearch.text.toString()
        if (searchText.isNotEmpty()) {
            currentPage = 1
            hitSearchCharacterAPI(searchText, false)
        }
    }

    private fun saveSearchString(){
        if (!recentSearchesList.contains(searchText)) {
            Utility.saveRecentSearchesCharacter(requireContext(), searchText)
        }else{
            recentSearchesList.remove(searchText)
            recentSearchesList.add(0, searchText)
            recentSearchesList.reverse()
            Utility.saveRecentCharacter(requireContext(),recentSearchesList)
        }
    }
    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
    }

    private fun hitSearchCharacterAPI(searchText: String, pagenate: Boolean) {
        requireActivity().hideKeyboard(requireActivity())
        saveSearchString()
        if (pagenate) {
            currentPage++
        }
        val request = SearchRequest(searchText.trim(), currentPage)
        characterViewModel?.searchCharacter(requireContext(), request)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            viewLifecycleOwner
        ) { characterResponse ->
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                if (currentPage == 1) {
                    totalPage = characterResponse.data.totalPages ?: 1
                    charactersList.clear()
                }
                charactersList.addAll(characterResponse?.data?.data)
                charactersAdapter?.notifyDataSetChanged()
                binding.somethingWrong.clSomthingWentWrong.hideView()

            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) {
            if (it.status) {
                binding.somethingWrong.clSomthingWentWrong.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }

        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(viewLifecycleOwner) {
            if (it.message == getString(R.string.no_internet)) {
                manageSomethingWentWrong(ErrorType.NO_INTERNET)
            } else {
                binding.clParent?.showStringSnackbarError(it.message)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
            manageShimmer(false)
            clearData()
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
            if (errorData.status == Constants.STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(requireContext(), errorData.message)
                return@observe
            } else if (errorData.status == Constants.STATUS_USER_DELETED) {
                SuccessfulDialog(
                    requireContext(), Constants.FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                binding.clParent?.showStringSnackbarError(errorData.message)
                manageShimmer(false)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                clearData()
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        /*if (showShimmer) {
            shimmerLayout.showView()
            shimmerLayout.startShimmer()
        } else {
            shimmerLayout.stopShimmer()
            shimmerLayout.hideView()
            swipeRefreshLayout.isRefreshing = false
        }*/
        if (showShimmer) {
            binding.llRecentSearches.hideView()
            if (currentPage == 1) {
                charactersList.clear()
                charactersAdapter?.notifyDataSetChanged()
                binding.progressLoading.showView()
            } else {
                binding.progressPaging.showView()
            }
        } else {
            if (currentPage == 1)
                binding.progressLoading.hideView()
            else
                binding.progressPaging.hideView()
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        this.errorType = errorType
        if (currentPage == 1) {
            binding.somethingWrong.clSomthingWentWrong.showView()
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
           /* ivRefresh.showView()
            tvTapToRetry.showView()*/
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.mipmap.no_data_avail_new
                    )
                )
                binding.somethingWrong.tvOops.visibility = View.GONE
                binding.somethingWrong.tvTapToRetry.showView()
               // tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_data_available), "")
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_biographie), "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
                binding.somethingWrong.tvTapToRetry.showView()
            } else if (errorType == ErrorType.INIT) {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.mipmap.ic_search_something
                    )
                )
                binding.somethingWrong.tvOops.visibility = View.GONE
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(
                    getString(R.string.tap_to_search_character),
                    ""
                )
                binding.somethingWrong.ivRefresh.hideView()
                binding.somethingWrong.tvTapToRetry.hideView()
            } else {
                binding.somethingWrong.tvTapToRetry.showView()
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
            }
        }
    }

    override fun onUpdateSuccessfully(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    override fun onDismiss(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    private fun performAccountDeleted() {
        lifecycleScope.launch {
            Utility.clearAllPreferences(requireContext())
            AuthViewsHolderActivity.open(requireActivity(), null)
            requireActivity().finishAffinity()
        }
    }


}