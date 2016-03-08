package com.jiahonglu.kotlinpullrefresh.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView

import com.jiahonglu.kotlinpullrefresh.R

import java.text.SimpleDateFormat
import java.util.Date


class CustomListview : ListView, OnScrollListener {

    private var iv_arrow: ImageView? = null
    private var pb_show: ProgressBar? = null
    private var tv_refresh: TextView? = null
    private var tv_time: TextView? = null
    private var downY: Int = 0
    private var mHeight: Int = 0
    private var mHeaderView: View? = null
    private val PULL_DOWN = 0
    private val PULL_RELEASEREFRESH = 1
    private val PULL_RFRESH = 2
    private var currentState = PULL_DOWN
    private var animationUp: RotateAnimation? = null
    private var animationDown: RotateAnimation? = null
    private var mOnRefreshDownLisener: OnRefreshDownLisener? = null
    private var mViewFooter: View? = null
    private var mDown: Int = 0
    private var idLoading = false

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :

    super(context, attrs, defStyle) {
        initHeaderView()
        initFootView()
    }

    constructor(context: Context, attrs: AttributeSet) :

    super(context, attrs) {
        initHeaderView()
        initFootView()
    }

    constructor(context: Context) :

    super(context) {
        initHeaderView()
        initFootView()
    }

    /**
     * 下拉更多的
     */
    private fun initFootView() {

        mViewFooter = View.inflate(context, R.layout.layout_download, null)
        mViewFooter!!.measure(0, 0)
        mDown = mViewFooter!!.measuredHeight
        mViewFooter!!.setPadding(0, -mDown, 0, 0)
        this.addFooterView(mViewFooter)
        this.setOnScrollListener(this)
    }

    private fun initHeaderView() {

        mHeaderView = View.inflate(context, R.layout.layout_up, null)

        mHeaderView!!.measure(0, 0)
        mHeight = mHeaderView!!.measuredHeight

        iv_arrow = mHeaderView!!.findViewById(R.id.iv_arrow) as ImageView
        pb_show = mHeaderView!!.findViewById(R.id.pb_show) as ProgressBar
        tv_refresh = mHeaderView!!.findViewById(R.id.tv_refresh) as TextView
        tv_time = mHeaderView!!.findViewById(R.id.tv_time) as TextView
        mHeaderView!!.setPadding(0, -mHeight, 0, 0)

        this.addHeaderView(mHeaderView)
        tv_time!!.text = "最后刷新时间：" + currentTime
        initAnimation()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> downY = ev.y.toInt()
            MotionEvent.ACTION_MOVE -> {
                val moveY = ev.y.toInt()

                val diffY = moveY - downY
                if (diffY > 0 && firstVisiblePosition == 0) {
                    val padingTop = -mHeight + diffY
                    if (padingTop > 0 && currentState != PULL_RELEASEREFRESH) {
                        //
                        currentState = PULL_RELEASEREFRESH
                        refreshState(currentState)
                    } else if (padingTop < 0 && currentState != PULL_DOWN) {
                        currentState = PULL_DOWN
                        refreshState(currentState)
                    }
                    mHeaderView!!.setPadding(0, padingTop, 0, 0)
                    return true
                }
            }
            MotionEvent.ACTION_UP
            -> if (currentState == PULL_DOWN) {
                mHeaderView!!.setPadding(0, -mHeight, 0, 0)
            } else if (currentState == PULL_RELEASEREFRESH) {

                currentState = PULL_RFRESH
                mHeaderView!!.setPadding(0, 0, 0, 0)
                refreshState(currentState)
                if (mOnRefreshDownLisener != null) {
                    mOnRefreshDownLisener!!.onRefreshLisener()
                }
            }

            else -> {
            }
        }
        return super.onTouchEvent(ev)
    }

    /**
     * 根据状态来刷新
     */
    private fun refreshState(state: Int) {

        when (state) {
            PULL_DOWN -> {

                iv_arrow!!.startAnimation(animationDown)
                tv_refresh!!.text = "下拉刷新"
            }
            PULL_RELEASEREFRESH -> {
                iv_arrow!!.startAnimation(animationUp)
                tv_refresh!!.text = "释放刷新"
            }
            PULL_RFRESH -> {
                iv_arrow!!.clearAnimation()
                pb_show!!.visibility = View.VISIBLE
                iv_arrow!!.visibility = View.INVISIBLE
                tv_refresh!!.text = "正在刷新"
            }
        }
    }

    private fun initAnimation() {

        animationUp = RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animationUp!!.duration = 500
        animationUp!!.fillAfter = true

        animationDown = RotateAnimation(0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f)
        animationDown!!.duration = 500
        animationDown!!.fillAfter = true

    }

    private val currentTime: String
        get() {

            val sdf = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
            val time = sdf.format(Date())
            return time
        }

    /**
     * 刷新完成的回调
     */
    fun setOnRefreshLisener(lisener: OnRefreshDownLisener) {

        this.mOnRefreshDownLisener = lisener
    }

    interface OnRefreshDownLisener {

        fun onRefreshLisener()

        fun onRefreshDownLisener()
    }

    /**
     * 刷新结束
     */
    fun onRefreshFinish() {

        if (idLoading) {
            idLoading = false
            mViewFooter!!.setPadding(0, -mDown, 0, 0)
        } else {
            mHeaderView!!.setPadding(0, -mHeight, 0, 0)
            iv_arrow!!.visibility = View.VISIBLE
            pb_show!!.visibility = View.INVISIBLE
            currentState = PULL_DOWN
            tv_time!!.text = "上次刷新时间：" + currentTime
        }

    }

    /**
     * 滑动状态的监听

     * @param view
     * *
     * @param scrollState
     */
    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

        if (idLoading) {
            return
        }
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            if (lastVisiblePosition == count - 1) {

                mViewFooter!!.setPadding(0, 0, 0, 0)
                idLoading = true
                this.setSelection(count)

            }
            if (mOnRefreshDownLisener != null) {
                mOnRefreshDownLisener!!.onRefreshDownLisener()
            }
        }

    }

    /**
     * @param view
     * *
     * @param firstVisibleItem
     * *
     * @param visibleItemCount
     * *
     * @param totalItemCount
     */
    override fun onScroll(view: AbsListView, firstVisibleItem: Int,
                          visibleItemCount: Int, totalItemCount: Int) {


    }

}

