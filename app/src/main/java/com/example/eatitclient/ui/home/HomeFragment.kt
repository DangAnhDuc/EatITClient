package com.example.eatitclient.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.eatitclient.Adapter.MyPopularCategoriesAdapter
import com.example.eatitclient.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    @BindView(R.id.recycler_popular)
    var recyclerView:RecyclerView?=null

    var unbinder:Unbinder?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        unbinder= ButterKnife.bind(this,root)
        iniView(root)
        homeViewModel.popularList.observe(this, Observer {
            val listData= it
            val adapter= MyPopularCategoriesAdapter(context!!,listData)
            recyclerView!!.adapter= adapter
        })
        return root
    }

    private fun iniView(root: View) {
        recyclerView= root.findViewById(R.id.recycler_popular) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager= LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
    }
}