package org.personal.videotogether.view.fragments.home.nestonvideo.videodetail.nestonvideodetail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_video_detail.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.util.DataState
import org.personal.videotogether.view.adapter.ItemClickListener
import org.personal.videotogether.view.adapter.YoutubeAdapter
import org.personal.videotogether.view.fragments.home.nestonhomedetail.HomeDetailBlankFragmentDirections
import org.personal.videotogether.viewmodel.YoutubeStateEvent
import org.personal.videotogether.viewmodel.YoutubeViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VideoDetailFragment : Fragment(R.layout.fragment_video_detail), ItemClickListener, View.OnClickListener {

    private val TAG by lazy { javaClass.name }

    private lateinit var videoTogetherNavController: NavController
    private lateinit var homeDetailNavController: NavController

    private val youtubeViewModel: YoutubeViewModel by lazy { ViewModelProvider(requireActivity())[YoutubeViewModel::class.java] }

    // 리사이클러 뷰
    private val youtubeList by lazy { ArrayList<YoutubeData>() }
    private val youtubeAdapter by lazy { YoutubeAdapter(this, youtubeList, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 네비게이션 설정
        val homeDetailFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.homeDetailFragmentContainer)
        videoTogetherNavController = Navigation.findNavController(view)
        homeDetailNavController = Navigation.findNavController(homeDetailFragmentContainer)

        subscribeObservers()
        setListener()
        buildRecyclerView()
    }

    private fun subscribeObservers() {
        youtubeViewModel.currentPlayedYoutube.observe(viewLifecycleOwner, Observer { youtubeData ->
            if (youtubeData != null) {
                val youtubeListDataState = youtubeViewModel.youtubeList.value

                if (youtubeListDataState is DataState.Success<List<YoutubeData>?>) {
                    youtubeList.clear()
                    youtubeListDataState.data!!.forEach { youtubeList.add(it) }
                    youtubeAdapter.notifyDataSetChanged()
                    Log.i(TAG, "subscribeObservers: 유투브 디테일 : $youtubeList")
                }

                expandedVideoTitleTV.text = youtubeData.title
                expandedChannelTitleTV.text = youtubeData.channelTitle
            }
        })
    }

    private fun setListener() {
        videoTogetherIB.setOnClickListener(this)
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        playListRV.layoutManager = layoutManager
        playListRV.adapter = youtubeAdapter
    }


    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.videoTogetherIB ->{
                val action = HomeDetailBlankFragmentDirections
                    .actionHomeDetailBlankFragmentToSelectFriendsFragment("addVideoTogether")

                homeDetailNavController.navigate(action)
            }
        }
    }

    // ------------------ 리사이클러뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val youtubeData = youtubeList[itemPosition]
        youtubeViewModel.setStateEvent(YoutubeStateEvent.SetFrontPlayer(youtubeData))
    }
}