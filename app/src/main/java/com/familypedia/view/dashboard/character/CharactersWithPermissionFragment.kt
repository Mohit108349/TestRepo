package com.familypedia.view.dashboard.character

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.FragmentCharactersWithPermissionBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM_CHARACTER_WITH_PERMISSION
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.viewmodels.CharacterViewModel
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CharactersWithPermissionFragment : Fragment(), CharactersListener, FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var charactersAdapter: CharactersAdapter? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var characterViewModel: CharacterViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var _binding: FragmentCharactersWithPermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharactersWithPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        binding.somethingWrong.tvTapToRetry.text=resources.getText(R.string.tv_retry)
        initListeners()
        setAdapter()
        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun setAdapter() {
        charactersAdapter = CharactersAdapter(
            requireActivity(),
            charactersList,
            this,
            FROM_CHARACTER_WITH_PERMISSION
        ,null)
        binding.rvCharacterWithPermissions.adapter = charactersAdapter
        binding.rvCharacterWithPermissions.addOnScrollListener(recyclerScrollListener)
    }

    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = binding.rvCharacterWithPermissions.layoutManager?.childCount
                    val totalItemCount = binding.rvCharacterWithPermissions.layoutManager?.itemCount
                    val pastVisiblesItems =
                        (binding.rvCharacterWithPermissions.layoutManager as LinearLayoutManager)?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitCharactersListAPI(true)
                            }
                        }
                    }
                }
            }
        }

    override fun onListSizeChanged(size: Int) {

    }

    override fun onEditCharacterClicked(characterData: CharacterData) {

    }

    private fun initListeners() {
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.swipeRefreshLayout?.setOnRefreshListener {
            charactersList.clear()
            charactersAdapter?.notifyDataSetChanged()
            hitCharactersListAPI(false)
        }
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.somethingWrong.tvTapToRetry -> hitCharactersListAPI(false)
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        hitCharactersListAPI(false)
    }

    private fun hitCharactersListAPI(pagenate: Boolean) {
        if (pagenate)
            currentPage++
        characterViewModel?.getCharacterWithUserPermission(requireContext(), currentPage)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            viewLifecycleOwner,
            { characterResponse ->
                if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                    if (currentPage == 1) {
                        totalPage = characterResponse?.data.totalPages ?: 1
                        charactersList.clear()
                    }
                    val sortedData = characterResponse.data.data.sortedBy {
                        it.name.toString()
                    }
                    charactersList.addAll(sortedData)
                    charactersAdapter?.notifyDataSetChanged()
                } else {
                    if (currentPage == 1)
                        manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
                }
            })
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner, { loading ->
            if (loading.status) {
                binding.somethingWrong.clSomthingWentWrong.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }
        })
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(viewLifecycleOwner, {
            if (it.message == getString(R.string.no_internet)) {
                manageSomethingWentWrong(ErrorType.NO_INTERNET)
            } else {
                binding.clParent?.showStringSnackbarError(it.message)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
            manageShimmer(false)
        })
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(viewLifecycleOwner, {
            binding.clParent?.showStringSnackbarError(it.message)
            manageShimmer(false)
        })
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                binding.shimmerLay.shimmerLayout.showView()
                binding.shimmerLay.shimmerLayout.startShimmer()
            } else {
                binding.progressPaging.showView()
            }
        } else {
            if (currentPage == 1) {
                binding.shimmerLay.shimmerLayout.stopShimmer()
                binding.shimmerLay.shimmerLayout.hideView()
                binding.swipeRefreshLayout?.isRefreshing = false
            } else {
                binding.progressPaging.hideView()
                loading = true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        if (currentPage == 1) {
            binding.somethingWrong.clSomthingWentWrong.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_no_biographies_found))
                binding.somethingWrong.tvOops.visibility = View.GONE
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(String.format(resources.getString(R.string.not_available),getString(R.string.tv_permitted_biographies)) , "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding.somethingWrong.tvOops.visibility = View.VISIBLE
                binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
            } else {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding.somethingWrong.tvOops.visibility = View.VISIBLE
                binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
            }
        }
    }

}