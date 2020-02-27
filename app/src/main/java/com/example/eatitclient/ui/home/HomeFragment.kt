package com.example.eatitclient.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import com.example.eatitclient.Adapter.MyBestDealsAdapter
import com.example.eatitclient.Adapter.MyPopularCategoriesAdapter
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.R
import org.greenrobot.eventbus.EventBus

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    var recyclerView:RecyclerView?=null
    var viewPager: LoopingViewPager? = null

    var layoutAnimationController: LayoutAnimationController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        iniView(root)
        homeViewModel.popularList.observe(this, Observer {
            val listData= it
            val adapter= MyPopularCategoriesAdapter(context!!,listData)
            recyclerView!!.adapter= adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })
        homeViewModel.bestDealList.observe(this, Observer {
            val adapter = MyBestDealsAdapter(context!!, it, false)
            viewPager!!.adapter = adapter
        })
        return root
    }

    private fun iniView(root: View) {
        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        recyclerView= root.findViewById(R.id.recycler_popular) as RecyclerView
        viewPager = root.findViewById(R.id.viewPager) as LoopingViewPager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager= LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }

    override fun onPause() {
        viewPager!!.pauseAutoScroll()
        super.onPause()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemback())
        super.onDestroy()
    }
}