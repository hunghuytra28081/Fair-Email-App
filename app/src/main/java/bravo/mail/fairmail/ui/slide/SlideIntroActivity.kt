package bravo.mail.fairmail.ui.slide

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import bravo.mail.fairmail.R
import bravo.mail.fairmail.data.model.Slide
import bravo.mail.fairmail.extension.transparentStatusBar
import bravo.mail.fairmail.extension.viewInVisible
import bravo.mail.fairmail.extension.viewVisible
import bravo.mail.fairmail.ui.slide.adapter.SlideIntroPagerAdapter
import bravo.mail.fairmail.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_slide_intro.*

class SlideIntroActivity : AppCompatActivity() {

    private val pagerAdapter by lazy { SlideIntroPagerAdapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slide_intro)

        initView()
        initData()
        initHandles()
    }

    private fun initHandles() {
        tv_next.setOnClickListener {
            view_pager.currentItem++
        }

        btn_next.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        tv_skip.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initView() {
        transparentStatusBar()
    }

    private fun initData() {
        view_pager.adapter = pagerAdapter.apply {
            addSlide(
                Slide(
                    R.drawable.ic_launcher_background,
                    resources.getString(R.string.slide_1_title),
                    resources.getString(R.string.slide_1_desc)
                )
            )

            addSlide(
                Slide(
                    R.drawable.ic_launcher_background,
                    resources.getString(R.string.slide_2_title),
                    resources.getString(R.string.slide_2_desc)
                )
            )

            addSlide(
                Slide(
                    R.drawable.ic_launcher_background,
                    resources.getString(R.string.slide_3_title),
                    resources.getString(R.string.slide_3_desc)
                )
            )
        }
        dots_indicator.setViewPager(view_pager)
        view_pager.apply {

            offscreenPageLimit = 3
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> {
                            tv_next.viewVisible()
                            tv_skip.viewVisible()
                            btn_next.viewInVisible()
                        }
                        1 -> {
                            tv_next.viewVisible()
                            tv_skip.viewVisible()
                            btn_next.viewInVisible()
                        }
                        2 -> {
                            tv_next.viewInVisible()
                            tv_skip.viewInVisible()
                            btn_next.viewVisible()
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                }

            })
        }

    }
}