package com.example.eatitclient.ui.foodlist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eatitclient.Adapter.MyFoodListAdapter
import com.example.eatitclient.Common.Common
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.Model.FoodModel
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
            if (it != null) {
                adapter = MyFoodListAdapter(context!!, it)
                recycler_food!!.adapter = adapter
                recycler_food!!.layoutAnimation = layoutAnimationController
            }

        })
        return root
    }

    private fun initView(root: View?) {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar!!.setTitle(Common.categorySelected!!.name)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                startSearch(query!!)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        val closeButton = searchView.findViewById<View>(R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val ed = searchView.findViewById<View>(R.id.search_src_text) as EditText
            ed.setText("")
            searchView.setQuery("", false)
            searchView.onActionViewCollapsed()
            menuItem.collapseActionView()
            foodListViewModel.getMutableFoodModelList()
        }
    }


    private fun startSearch(query: String) {
        val resultFood = ArrayList<FoodModel>()
        for (i in 0 until Common.categorySelected!!.foods!!.size) {
            val categoryModel = Common.categorySelected!!.foods!![i]
            if (categoryModel.name!!.toLowerCase().contains(query))
                resultFood.add(categoryModel)
        }
        foodListViewModel.getMutableFoodModelList().value = resultFood
    }
}