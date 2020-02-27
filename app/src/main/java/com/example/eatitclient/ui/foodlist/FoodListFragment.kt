package com.example.eatitclient.ui.foodlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eatitclient.Adapter.MyFoodListAdapter
import com.example.eatitclient.Common.Common
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.R
import org.greenrobot.eventbus.EventBus

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel
    var recycler_food: RecyclerView? = null
    var layoutAnimationController: LayoutAnimationController? = null
    var adapter: MyFoodListAdapter? = null

    override fun onStop() {
        if (adapter != null)
            adapter!!.onStop()
        super.onStop()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel =
            ViewModelProviders.of(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)
        initView(root)
        foodListViewModel.getMutableFoodModelList().observe(this, Observer {
            adapter = MyFoodListAdapter(context!!, it)
            recycler_food!!.adapter = adapter
            recycler_food!!.layoutAnimation = layoutAnimationController

        })
        return root
    }

    private fun initView(root: View?) {
        recycler_food = root!!.findViewById(R.id.recylcer_food_list) as RecyclerView
        recycler_food!!.setHasFixedSize(true)
        recycler_food!!.layoutManager = LinearLayoutManager(context)
        layoutAnimationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemback())
        super.onDestroy()
    }
}