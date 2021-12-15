package com.currencywiki.currencyconverter.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.currencywiki.currencyconverter.fragments.BasePageFragment
import java.util.*


open class PageAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    var fragmentsList: MutableList<BasePageFragment> = ArrayList()
    override fun getItem(position: Int): BasePageFragment {
        return fragmentsList[position]
    }

    override fun getCount(): Int {
        return fragmentsList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return getItem(position).getTitle()
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun addPage(basePageFragment: BasePageFragment) {
        fragmentsList.add(basePageFragment)
    }

}