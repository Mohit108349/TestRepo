package com.familypedia.view.dashboard.character.aboutCharacters

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.familypedia.R
import com.familypedia.databinding.FragmentAboutCharacterBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.view.dashboard.character.aboutCharacters.adapter.PhotosAdapter
import com.familypedia.viewmodels.CharacterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.familypedia.network.GET_CHARACTER_PROFILE
import com.familypedia.utils.Constants.STATUS_ALIVE
import com.familypedia.utils.Constants.STATUS_DEAD


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AboutCharacterFragment(
    var bundleData: Bundle?,
    var characterViewModel: CharacterViewModel,
    var permittedToPost: PermittedToPost
) : Fragment(), FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var photosAdapter: PhotosAdapter? = null

    //private var characterViewModel: CharacterViewModel? = null
    private var characterId = ""
    private var listOfImages = arrayListOf<String>()
    private var _binding: FragmentAboutCharacterBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        characterId = bundleData?.getString(USER_ID) ?: ""

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutCharacterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        manageShimmer(true)
        if (permittedToPost != PermittedToPost.YES) {
            binding?.ivLocked?.showView()
        } else {
            binding?.ivLocked?.hideView()
            setAdapter()
        }
        initListeners()
        responseData()
        onGettingError()
        onGettingException()
        manageDataLoadingStatus()
    }

    private fun initListeners() {
        binding?.somethingWrong?.clSomthingWentWrong?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            //clSomthingWentWrong -> hitCharacterProfileAPI()
        }
    }

    private fun setAdapter() {
        photosAdapter = PhotosAdapter(
            requireContext(),
            listOfImages
        )
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        //layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        // rvPhotos.itemAnimator = DefaultItemAnimator()

        binding?.rvPhotos?.layoutManager = layoutManager
        binding?.rvPhotos?.adapter = photosAdapter
    }

    private fun setProfileData(charactersData: CharacterData) {
        val defaultText = getString(R.string.n_a)
        if (charactersData.status == STATUS_ALIVE)
            binding?.tvStatus?.setTextOnTextView(getString(R.string.yes), defaultText)
        else if (charactersData.status == STATUS_DEAD)
            binding?.tvStatus?.setTextOnTextView(getString(R.string.no), defaultText)

        binding?.tvDob?.setTextOnTextView(charactersData.date_of_birth, defaultText)
        binding?.tvCityOfBirth?.setTextOnTextView(charactersData.city_of_birth, defaultText)
        binding?.tvCountryOfBirth?.setTextOnTextView(charactersData.country_of_birth, defaultText)
        if (charactersData.date_of_death != null && charactersData.date_of_death.isNotEmpty()) {
            binding?.tvDod?.setTextOnTextView(charactersData.date_of_death, defaultText)
        } else {
            binding?.tvDod?.hideView()
            binding?.tvDodStatic?.hideView()
        }
        listOfImages.clear()
        if (charactersData?.images != null && charactersData?.images?.isNotEmpty() == true) {
            binding?.tvPhotos?.showView()
            for (item in charactersData.images) {
                if (item != null)
                    listOfImages.add(item)
            }
            //listOfImages.addAll(charactersData.images)
            photosAdapter?.notifyDataSetChanged()
        } else {
            binding?.tvPhotos?.hideView()
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        hitCharacterProfileAPI()
    }

    private fun hitCharacterProfileAPI() {
        lifecycleScope.launch {
            delay(1000)
            characterViewModel?.getCharacterProfile(requireActivity(), characterId)
        }
    }

    private fun responseData() {
        characterViewModel?.characterResponseResult?.observe(
            viewLifecycleOwner
        ) { characterResponse ->
            if (characterResponse.apiName == GET_CHARACTER_PROFILE) {
                if (characterResponse?.data != null) {
                    setProfileData(characterResponse.data)
                } else {
                    //manageSomethingWentWrong(true)
                }
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(viewLifecycleOwner) { exceptionData ->
            if (exceptionData.apiName == GET_CHARACTER_PROFILE) {
                manageShimmer(false)
                if (exceptionData.message == getString(R.string.no_internet)) {
                    //manageSomethingWentWrong(false)
                } else {
                    binding?.clParent?.showStringSnackbarError(exceptionData.message)
                    //manageSomethingWentWrong(true)
                }
            }
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
            if (errorData.apiName == GET_CHARACTER_PROFILE) {
                binding?.clParent?.showStringSnackbarError(errorData.message)
                manageShimmer(false)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) { loadingData ->
            if (loadingData.apiName == GET_CHARACTER_PROFILE) {
                if (loadingData.status) {
                    binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            }
        }
    }

    private fun manageSomethingWentWrong(isNoData: Boolean) {
        binding?.somethingWrong?.clSomthingWentWrong?.showView()
        binding?.clChild?.hideView()
        if (isNoData) {
            binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_data_available), "")
        } else { //no internet
            binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            binding.shimmerLay?.shimmerLayoutAboutCharacter?.showView()
            binding.shimmerLay?.shimmerLayoutAboutCharacter?.startShimmer()
            binding?.clChild?.hideView()
        } else {
            binding.shimmerLay?.shimmerLayoutAboutCharacter?.stopShimmer()
            binding.shimmerLay?.shimmerLayoutAboutCharacter?.hideView()
            binding?.clChild?.showView()
        }
    }
}