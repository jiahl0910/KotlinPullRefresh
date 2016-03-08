package com.jiahonglu.kotlinpullrefresh

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.jiahonglu.kotlinpullrefresh.view.CustomListview
import java.util.*


class MainActivity : Activity() {


    private var mlv: CustomListview? = null
    private var mList: MutableList<String>? = null
    private var mAdapter: MainActivity.MyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mlv = findViewById(R.id.mlv) as CustomListview
        mList = ArrayList<String>()
        for (i in 0..29) {
            mList!!.add(String.format("我时第%d条数据", i))
        }
        mAdapter = MyAdapter()
        mlv!!.setAdapter(mAdapter)

        mlv!!.setOnRefreshLisener(object : CustomListview.OnRefreshDownLisener {

            override fun onRefreshLisener() {
                Thread(Runnable {
                    SystemClock.sleep(3000)
                    mList!!.add(0, "我时加载更多出来第一条数据")
                    mList!!.add(0, "我时加载更多出来第二条数据")
                    mList!!.add(0, "我时加载更多出来第三条数据")
                    mList!!.add(0, "我时加载更多出来第四条数据")

                    runOnUiThread {
                        mAdapter!!.notifyDataSetChanged()
                        mlv!!.onRefreshFinish()
                    }
                }).start()
            }

            override fun onRefreshDownLisener() {
                Thread(Runnable {
                    SystemClock.sleep(3000)
                    mList!!.add("haha1")
                    mList!!.add("haha2")
                    runOnUiThread {
                        mAdapter!!.notifyDataSetChanged()
                        mlv!!.onRefreshFinish()
                    }
                }).start()

            }
        })
    }


    internal inner class MyAdapter : BaseAdapter() {

        override fun getCount(): Int {
            // TODO Auto-generated method stub
            return mList!!.size
        }

        override fun getItem(position: Int): Any {
            return mList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val tv = TextView(this@MainActivity)
            tv.text = mList!![position]
            tv.setTextColor(Color.BLACK)
            tv.textSize = 18f
            return tv
        }

    }

}
