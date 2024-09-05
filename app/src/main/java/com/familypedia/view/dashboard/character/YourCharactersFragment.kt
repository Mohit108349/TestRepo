package com.familypedia.view.dashboard.character

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.FragmentYourCharactersBinding
import com.familypedia.network.CharacterData
import com.familypedia.network.SearchRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM_CHARACTER_YOUR_CHARACTERS
import com.familypedia.utils.Constants.STATUS_ACCOUNT_NOT_VERIFIED
import com.familypedia.utils.Utility.unAuthorizedInactiveUser
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.view.dashboard.character.addCharacter.AddCharacterListingActivity
import com.familypedia.view.dashboard.character.addCharacter.AddNewCharacterActivity
import com.familypedia.viewmodels.CharacterViewModel

import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YourCharactersFragment : Fragment(), FamilyPediaClickListener, CharactersListener,
    SuccessFulListener {
    private var param1: String? = null
    private var param2: String? = null
    private var charactersAdapter: CharactersAdapter? = null
    private var characterViewModel: CharacterViewModel? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var _binding: FragmentYourCharactersBinding? = null
    private val binding get() = _binding!!

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
                    hitCharactersListAPI(false)
                   // charactersAdapter?.notifyDataSetChanged()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYourCharactersBinding.inflate(inflater, container, false)
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
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()


        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(mMessageReceiver, IntentFilter(Constants.NEW_CHARACTER_CREATED))
    }

    private fun initListeners() {
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            charactersList.clear()
            charactersAdapter?.notifyDataSetChanged()
            hitCharactersListAPI(false)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onViewClick(view: View) {
        when (view) {
//            tvTapToRetry -> hitCharactersListAPI(false)
            binding.somethingWrong.tvTapToRetry -> AddCharacterListingActivity.open(requireActivity())
        }
    }

    private fun setAdapter() {
        charactersAdapter = CharactersAdapter(
            requireActivity(),
            charactersList,
            this,
            FROM_CHARACTER_YOUR_CHARACTERS, resultLauncher)
        binding.rvYourCharacters.adapter = charactersAdapter
        binding.rvYourCharacters.addOnScrollListener(recyclerScrollListener)
    }


    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = binding.rvYourCharacters.layoutManager?.childCount
                    val totalItemCount = binding.rvYourCharacters.layoutManager?.itemCount
                    val pastVisiblesItems =
                        (binding.rvYourCharacters.layoutManager as LinearLayoutManager)?.findFirstVisibleItemPosition()
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
        /* if (size>0){
             clSomthingWentWrong.hideView()
         }else{
             manageSomethingWentWrong(true)
         }*/
    }

    override fun onEditCharacterClicked(characterData: CharacterData) {
        val bundle = Bundle()
        bundle.putString(Constants.FROM, Constants.FROM_EDIT_CHARACTER)
        bundle.putString(Constants.CHARACTER_NAME, characterData.name)
        bundle.putSerializable(Constants.CHARACTER_DATA, characterData)


        val intent = Intent(requireActivity(), AddNewCharacterActivity::class.java)
        intent.putExtras(bundle)
        requireActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        resultLauncher?.launch(intent)
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

        val request = SearchRequest("", currentPage)
        characterViewModel?.getUsersCharacterList(requireContext(), request)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            viewLifecycleOwner
        ) { characterResponse ->
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                if (currentPage == 1) {
                    charactersList.clear()
                    totalPage = characterResponse?.data.totalPages ?: 1
                }
                val sortedData = characterResponse.data.data.sortedBy {
                    it.name.toString()
                }
                charactersList.addAll(sortedData)
                charactersAdapter?.notifyDataSetChanged()
                binding.somethingWrong.clSomthingWentWrong.hideView()

            } else {
                if (currentPage == 1)
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
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
            if (errorData.status == STATUS_ACCOUNT_NOT_VERIFIED) {
                unAuthorizedInactiveUser(requireContext(), errorData.message)
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
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage==1) {
                binding.shimmerLay.shimmerLayout.showView()
                binding.shimmerLay.shimmerLayout.startShimmer()
            }else{
                binding.progressPaging.showView()
            }
        } else {
            if (currentPage==1) {
                binding.shimmerLay.shimmerLayout.stopShimmer()
                binding.shimmerLay.shimmerLayout.hideView()
                binding.swipeRefreshLayout.isRefreshing = false
            }else{
                binding.progressPaging.hideView()
                loading=true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        if (currentPage==1) {
            binding.somethingWrong.clSomthingWentWrong.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding.somethingWrong. ivSomethingWentWrong.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_no_biographies_found))
                binding.somethingWrong.tvOops.visibility = View.GONE
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_biographie), "")
                binding.somethingWrong.tvTapToRetry.setTextOnTextView(getString(R.string.tv_create_your_first_bio),"")
               // tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_data_available), "")
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
                binding.somethingWrong.tvTapToRetry.setTextOnTextView(getString(R.string.tv_retry),"")
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


    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("BroadCasted:: True")
            hitCharactersListAPI(false)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mMessageReceiver)

    }
}