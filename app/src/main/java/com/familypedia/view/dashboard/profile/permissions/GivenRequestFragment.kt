package com.familypedia.view.dashboard.profile.permissions

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.familypedia.R
import com.familypedia.databinding.FragmentGivenRequestBinding
import com.familypedia.network.AskCharacterPermissionRequest
import com.familypedia.network.CHARACTER_PERMISSION_LIST
import com.familypedia.network.PermissionData
import com.familypedia.network.User
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM_GIVEN_REQUEST
import com.familypedia.utils.Constants.FROM_PERMISSION
import com.familypedia.utils.listeners.PermissionsListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.view.dashboard.profile.permissions.adapter.PostRequestParentAdapter
import com.familypedia.viewmodels.PermissionsViewModel
import io.ak1.pix.helpers.hide
import io.ak1.pix.helpers.show
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GivenRequestFragment : Fragment(), PermissionsListener, ConfirmationDialogListener,
    FamilyPediaClickListener, SuccessFulListener {
    private var param1: String? = null
    private var param2: String? = null
    private var permissionsViewModel: PermissionsViewModel? = null
    private var permissionList = arrayListOf<PermissionData>()
    private var postRequestAdapter: PostRequestParentAdapter? = null
    private var _binding: FragmentGivenRequestBinding? = null
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
    ): View {
        _binding = FragmentGivenRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        setAdapter()
        initViewModel()
        clickListener()
        responseData()
        manageDataLoadingStatus()
        onGettingException()
        onGettingError()

    }

    private fun clickListener() {
       // clSomthingWentWrong.familyPediaClickListener(this)
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        permissionList.clear()
        postRequestAdapter?.notifyDataSetChanged()
        getPermissions()
    }

    override fun onViewClick(view: View) {
        when (view) {
            //clSomthingWentWrong -> refreshData()
            binding.somethingWrong.tvTapToRetry -> refreshData()
        }
    }


    private fun setAdapter() {
        postRequestAdapter =
            PostRequestParentAdapter(requireContext(), permissionList, FROM_GIVEN_REQUEST, this)
        binding.rvGivenRequest.adapter = postRequestAdapter
    }

    override fun onRequestAccept(data: AskCharacterPermissionRequest) {

    }

    override fun onRequestReject(data: AskCharacterPermissionRequest) {

    }

    var requestData: AskCharacterPermissionRequest? = null
    override fun onDelete(data: AskCharacterPermissionRequest) {
        requestData = data
        showDeleteDialog()
    }

    var confirmationDialog: ConfirmationDialog? = null
    private fun showDeleteDialog() {
        confirmationDialog = ConfirmationDialog(
            requireContext(),
            this,
            getString(R.string.remove_permission),
            getString(R.string.remove_permission_confirm),
            FROM_PERMISSION
        )
        confirmationDialog?.show()
    }

    override fun onYes() {
        requestData?.let {
            deletePermission(requestData!!)
        }
    }

    override fun onNo() {
        confirmationDialog?.dismiss()
    }

    override fun onItemClick(user: User) {
        val bundle = Bundle()
        bundle.putString(Constants.FROM, Constants.FROM_VIEW_USER)
        bundle.putString(Constants.USER_ID, user._id)
        ViewProfileActivity.open(context as Activity, bundle)
    }


    /*************** VIEW-MODEL AND API OBSERVERS **************************************/
    private fun initViewModel() {
        permissionsViewModel = ViewModelProvider(this).get(PermissionsViewModel::class.java)
        permissionsViewModel?.init()
        getPermissions()
    }

    private fun getPermissions() {
        permissionsViewModel?.getPermissionsList(requireContext(), 1)
    }

    private fun deletePermission(request: AskCharacterPermissionRequest) {
        permissionsViewModel?.removeGivenCharacterPermission(requireContext(), request)
    }

    private fun responseData() {
        permissionsViewModel?.permisssionResponseResult?.observe(requireActivity(), { response ->
            if (response.data?.data != null && response.data.data?.isNotEmpty() == true) {
                permissionList.clear()
                response.data.data.forEach { permissionData ->
                    if (permissionData.permittedUser != null && permissionData.permittedUser?.isNotEmpty() == true) {
                        permissionList.add(permissionData)
                    }
                }
                //permissionList.addAll(response.data.data)
                if (permissionList.isEmpty()) {
                    manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
                }
                postRequestAdapter?.notifyDataSetChanged()
            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        })

        permissionsViewModel?.simpleResponseResult?.observe(requireActivity(), { response ->
            if (response!=null){
                binding.clParent?.showStringSnackbar(response.message?:"")
                getPermissions()
            }
        })
    }

    private fun manageDataLoadingStatus() {
        permissionsViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner, {
            if (it.apiName == CHARACTER_PERMISSION_LIST) {
                if (it.status) {
                    binding.profressBar.progressBar.hide()
                    binding.somethingWrong.clSomthingWentWrong.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            } else {
                if (it.status) {
                    binding.profressBar.progressBar.show()
                } else {
                    binding.profressBar.progressBar.hide()
                }
            }
        })
    }

    private fun onGettingException() {
        permissionsViewModel?.exception?.observe(requireActivity(), { exceptionData ->
            if (exceptionData.apiName == CHARACTER_PERMISSION_LIST) {
                if (exceptionData.message == getString(R.string.no_internet)) {
                    manageSomethingWentWrong(ErrorType.NO_INTERNET)
                } else {
                    binding.clParent?.showStringSnackbarError(exceptionData.message)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                manageShimmer(false)
            } else {
                binding.clParent?.showStringSnackbarError(exceptionData.message)
            }
        })
    }

    private fun onGettingError() {
        permissionsViewModel?.error?.observe(requireActivity(), { errorData ->
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
                if (errorData.apiName == CHARACTER_PERMISSION_LIST) {
                    manageShimmer(false)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                binding.clParent?.showStringSnackbarError(errorData.message)
            }
        })
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            permissionList.clear()
            postRequestAdapter?.notifyDataSetChanged()
            binding.shimmerLay.shimmerLayoutRequest.showView()
            binding.shimmerLay.shimmerLayoutRequest.startShimmer()
        } else {
            binding.shimmerLay.shimmerLayoutRequest.stopShimmer()
            binding.shimmerLay.shimmerLayoutRequest.hideView()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType){
        binding.somethingWrong.clSomthingWentWrong.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.no_data_avail_new))
            binding.somethingWrong.tvOops.hideView()
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_given_request_available) , "")
        } else if (errorType == ErrorType.NO_INTERNET) { //no internet
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
            binding.somethingWrong.tvOops.hideView()
            binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
        } else {
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
            binding.somethingWrong.tvOops.hideView()
            binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
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