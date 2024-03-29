package org.personal.videotogether.view.fragments.nestonmain

import android.Manifest.permission.*
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_set_profile.*
import kotlinx.android.synthetic.main.fragment_set_profile.nameTextCountTV
import kotlinx.android.synthetic.main.fragment_set_profile.profileIV
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.personal.videotogether.R
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.view.dialog.ChoiceDialog
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SetProfileFragment
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : Fragment(R.layout.fragment_set_profile), TextWatcher, View.OnClickListener, ChoiceDialog.DialogListener {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    // 프로필 이미지 관련 변수
    private lateinit var cameraImage: Uri
    private var profileImageBitmap: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainNavController = Navigation.findNavController(view)
        subscribeObservers()
        setListener()
    }

    private fun subscribeObservers() {
        // live data : 이메일 프로필 업로드
        userViewModel.uploadUserProfileState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    sharedPreferenceHelper.setBoolean(requireContext(), getString(R.string.auto_sign_in_key), true)
                    mainNavController.navigate(R.id.action_setProfileFragment_to_mainHomeFragment)
                }
                is DataState.NoData -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    Log.i(TAG, "subscribeObservers: $dataState")
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    Log.i(TAG, "subscribeObservers: ${dataState.exception}")
                }
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
            }
        })
    }

    private fun setListener() {
        nameET.addTextChangedListener(this)
        profileIV.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
    }

    // ------------------ 클릭 이벤트 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.profileIV -> {
                val bundle = Bundle().apply { putInt("arrayResource", R.array.cameraOrGallery) }
                val choiceDialog = ChoiceDialog().apply { arguments = bundle }
                choiceDialog.show(childFragmentManager, "Camera Or Gallery Dialog")
            }

            R.id.confirmBtn -> {
                if (nameET.text.toString().trim() != "") {
                    if (profileImageBitmap != null) {
                        CoroutineScope(Main).launch {
                            imageHandler.bitmapToString(profileImageBitmap).onEach { dataState ->
                                when (dataState) {
                                    is DataState.Success<String> -> {
                                        // TODO : 파이어베이스 토큰 요청 나눠 보내기
                                        val firebaseToken = sharedPreferenceHelper.getString(requireContext(), getString(R.string.firebase_messaging_token))
                                        userViewModel.setStateEvent(UserStateEvent.UploadUserProfile(dataState.data, nameET.text.toString(), firebaseToken!!))
                                    }
                                    is DataState.Error -> {
                                        Log.i(TAG, "onClick: ${dataState.exception}")
                                    }
                                }
                            }.launchIn(CoroutineScope(Main))
                        }
                    } else {
                        Toast.makeText(requireContext(), "프로필 사진을 정해주세요", Toast.LENGTH_SHORT).show() // TODO : 프로필 없을 때도 기본이미지 예외처리 하기
                    }
                } else {
                    Toast.makeText(requireContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ------------------ 이름 글자수 확인 메소드 모음 ------------------
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {
        nameTextCountTV.text = String.format("%s/20", text!!.count())
    }

    // ------------------ 카메라 또는 갤러리 선택 다이얼로그 결과 메소드 모음 ------------------
    override fun onChoice(whichDialog: Int, choice: String, itemPosition: Int?, id: Int?) {
        when (choice) {
            "갤러리" -> requestGalleryPermission.launch()
            "카메라" -> requestCameraPermission.launch()
        }
    }

    // ------------------ 퍼미션 + ActivityForResult ------------------
    // 갤러리 권한 다이얼로그
    private val requestGalleryPermission by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(), READ_EXTERNAL_STORAGE
        ) { isGranted ->
            if (isGranted) {
                getGalleryImage.launch("image/*")
            } else {
                Toast.makeText(context, "갤러리 접근 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카메라, 외부 저장소 쓰기 권한 다이얼로그
    private val requestCameraPermission by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA)
        ) { isGranted ->
            if (isGranted[WRITE_EXTERNAL_STORAGE] == null) {
                checkCameraPermission(isGranted[CAMERA]!!)
            } else {
                if (isGranted[WRITE_EXTERNAL_STORAGE]!!) {
                    checkCameraPermission(isGranted[CAMERA]!!)
                } else {
                    Toast.makeText(requireContext(), "저장소 쓰기 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkCameraPermission(isGranted: Boolean) {
        if (isGranted) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "pillPicture")
                put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            }
            cameraImage = context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraImage)
            }
            getCameraImage.launch(cameraIntent)
        } else {
            Toast.makeText(requireContext(), "카메라 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private val getGalleryImage by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) convertUriToBitmap(imageUri)
        }
    }

    // 카메라로 찍은 이미지 비트맵으로 전환해서 받아오기
    private val getCameraImage by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            convertUriToBitmap(cameraImage)
        }
    }

    private fun convertUriToBitmap(imageUri: Uri) {
        CoroutineScope(Main).launch {
            imageHandler.imageUriToBitmap(requireContext(), imageUri).onEach { dataState ->
                when (dataState) {
                    is DataState.Success<Bitmap?> -> {
                        profileIV.setImageBitmap(dataState.data)
                        profileImageBitmap = dataState.data!!
                    }
                    is DataState.Error -> {
                        Log.i(TAG, "test : Error")
                    }
                }
            }.launchIn(CoroutineScope(Main))
        }
    }
}