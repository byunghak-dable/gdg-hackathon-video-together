package org.personal.videotogether.model.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.personal.videotogether.model.local.entity.UserCacheEntity
import org.personal.videotogether.model.local.entity.UserCacheMapper
import org.personal.videotogether.model.local.UserDAO
import org.personal.videotogether.model.server.entity.UserDataMapper
import org.personal.videotogether.model.server.RetrofitRequest
import org.personal.videotogether.model.server.RequestData
import org.personal.videotogether.util.DataState
import java.lang.Exception

class UserRepository
constructor(
    private val retrofitRequest: RetrofitRequest,
    private val userDAO: UserDAO,
    private val userCacheMapper: UserCacheMapper,
    private val userDataMapper: UserDataMapper
) {

    private val TAG = javaClass.name

    // 회원가입 시 이메일 중복 체크하는 요청
    suspend fun postCheckEmailValid(email: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)

        try {
            val postData = HashMap<String, Any?>().apply {
                put("email", email)
            }
            val requestData = RequestData("checkDuplicatedEmail", postData)

            val response = retrofitRequest.checkEmailValidation(requestData)

            if (response.code() == 200) emit(DataState.Success(true))
            if (response.code() == 204) emit(DataState.Success(false))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 회원가입 완료 시 서버에 유저 데이터 업로드 요청
    // 회원정보는 룸을 사용해 로컬에 저장
    suspend fun uploadUser(email: String, password: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)

        try {
            val postData = HashMap<String,Any?>().apply {
                put("email", email)
                put("password", password)
            }
            val requestData = RequestData("uploadUser", postData)
            val response = retrofitRequest.uploadUser(requestData)

            if (response.code() == 200) {
                val insertedId = response.body()!!
                val userCacheEntity =
                    UserCacheEntity(insertedId, email, password, null, null) // 이름과 프로필 이미지를 제외한 정보 저장

                userDAO.deleteAllUserData()
                userDAO.insertUserData(userCacheEntity)
                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.ResponseError("uploadUser : 서버에서 문제 발생"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadUser: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    // 유저 프로필을 서버로 업로드하는 메소드
    suspend fun uploadUserProfile(base64Image: String, name: String): Flow<DataState<Boolean?>> = flow {
        emit(DataState.Loading)

        try {
            val userTableId  = userDAO.getUserData()[0].id
            val postData = HashMap<String, Any?>().apply {
                put("id", userTableId)
                put("base64Image", base64Image)
                put("name", name)
            }
            val requestData = RequestData("uploadUserProfile", postData)
            val response = retrofitRequest.uploadUserProfile(requestData)

            if (response.code() == 200) {
                val userData = userDataMapper.mapFromEntity(response.body()!!)
                val userCacheEntity = userCacheMapper.mapToEntity(userData)
                userDAO.updateUserData(userCacheEntity)

                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.ResponseError("uploadUserProfile : 서버에서 문제 발생"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "uploadUser: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }

    suspend fun signIn(email: String, password: String) : Flow<DataState<Boolean>>  = flow {
        try {
            val postData = HashMap<String, Any?>().apply {
                put("email", email)
                put("password", password)
            }
            val requestData = RequestData("signIn", postData)

            val response = retrofitRequest.signIn(requestData)

            if (response.code() == 200) {
                val userData = userDataMapper.mapFromEntity(response.body()!!)
                val userCacheEntity = userCacheMapper.mapToEntity(userData)
                userDAO.deleteAllUserData()
                userDAO.insertUserData(userCacheEntity)
                Log.i(TAG, "signIn: ${userDAO.getUserData()}")
                emit(DataState.Success(true))
            }
            if (response.code() == 204) emit(DataState.ResponseError("signIn : 서버에 데이터 없음"))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "postCheckEmailValid: 서버 통신 에러발생($e)")
            emit(DataState.Error(e))
        }
    }
}