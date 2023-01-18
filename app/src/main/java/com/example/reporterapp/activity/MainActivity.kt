package com.example.reporterapp.activity

import android.annotation.TargetApi
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.example.reporterapp.adapter.DashboardPagerAdapter
import com.example.reporterapp.viewmodel.CreateArticleViewModel
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.widget.Toast
import com.example.reporterapp.*
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    lateinit var loader: AlertDialog


    private lateinit var createArticleViewModel: CreateArticleViewModel
    private lateinit var userViewModel: UserViewModel
    lateinit var dashboardPagerAdapter: DashboardPagerAdapter
    private lateinit var menu: Menu

    var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    val scope = CoroutineScope(coroutineContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.elevation = 0f

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val progressRunnable = object : java.lang.Runnable {
            override fun run() {
                if(loader.isShowing) {
                    showMsg(
                        this@MainActivity,
                        "Timeout! please check your internet connection",
                        Toast.LENGTH_SHORT
                    )
                    loader.dismiss()
                }
            }

        }
        val pdCanceller = Handler();
        pdCanceller.postDelayed(progressRunnable, 20000);


        val localTime:String= intent?.extras?.get("localTime").toString()
        val isLoad= intent?.extras?.getBoolean("isLoad", true)
        fabCreateArticle.setOnClickListener {
            startActivity(Intent(this, CreateArticleActivity::class.java))
        }
        tabLayoutDashboard.setupWithViewPager(viewPagerDashboard)

        /*val pullToRefresh = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        pullToRefresh.setOnRefreshListener {
            pullToRefresh.isRefreshing = true
            refreshData() // your code
            pullToRefresh.isRefreshing = false
        }*/

        loader.show()
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)
        dashboardPagerAdapter = DashboardPagerAdapter(this, supportFragmentManager, 5)
        viewPagerDashboard.adapter = dashboardPagerAdapter
        viewPagerDashboard.offscreenPageLimit = ArticleStatus.values().size
        repeat(tabLayoutDashboard.tabCount) {
            tabLayoutDashboard.getTabAt(it)?.customView = dashboardPagerAdapter.getTabView(it)
        }

        viewPagerDashboard.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayoutDashboard))
        tabLayoutDashboard.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPagerDashboard.currentItem = tab.position
            }
        })
        if(isLoad!!) {
            createArticleViewModel = ViewModelProviders.of(this).get(CreateArticleViewModel::class.java)
            createArticleViewModel.getCategories(this)
            createArticleViewModel.fetchData(
                this, if (PreferenceManager.getInstance(getApplication()).refresh_date == null) "" else
                    PreferenceManager.getInstance(application).refresh_date
            )
            PreferenceManager.getInstance(application)
                .setRefreshDate(localToUTCForArticles())
        }
    }

    /*fun ShowProgressDialog() {
        val dialogBuilder = AlertDialog.Builder(this);
        val inflater: LayoutInflater = getSystemService( Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater;
        val dialogView:View = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        loader = dialogBuilder.create();
        loader.show();
    }

    fun HideProgressDialog() {
        loader.dismiss()
    }*/
    override fun onResume() {
        super.onResume()
        createArticleViewModel = ViewModelProviders.of(this).get(CreateArticleViewModel::class.java)
        createArticleViewModel.allLanguage
        createArticleViewModel.allCategories
        createArticleViewModel.searchByLiveData
    }
    fun refreshData() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent.apply {
            putExtra("localTime", PreferenceManager.getInstance(application).refresh_date)
        })
        overridePendingTransition(0, 0);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        this.menu = menu
        val searchMenu = menu.findItem(R.id.action_search)
        val searchView = searchMenu.actionView as SearchView

        searchView.setBackgroundColor(Color.parseColor("#C62828"));
        searchView.setIconifiedByDefault(false)

        searchMenu.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                viewPagerDashboard.stopScrolling(true)
                tabLayoutDashboard.visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewPagerDashboard.stopScrolling(false)
                tabLayoutDashboard.visibility = View.VISIBLE
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                dashboardPagerAdapter.queryCallback[viewPagerDashboard.currentItem]?.filter(p0 ?: "")
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                dashboardPagerAdapter.queryCallback[viewPagerDashboard.currentItem]?.filter(query)
                return true
            }
        })

        val notificationItem = menu.findItem(R.id.action_notification)
        notificationItem?.actionView?.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_draft -> {
                viewPagerDashboard.currentItem = 0
                return true
            }
            R.id.action_rework -> {
                viewPagerDashboard.currentItem = 1
                return true
            }
            R.id.action_pending -> {
                viewPagerDashboard.currentItem = 2
                return true
            }
            R.id.action_published -> {
                viewPagerDashboard.currentItem = 3
                return true
            }
            R.id.action_rejected -> {
                viewPagerDashboard.currentItem = 4
                return true
            }
            R.id.action_my_account -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                com.example.reporterapp.app.PreferenceManager.getInstance(this).clearToken()
                com.example.reporterapp.app.PreferenceManager.getInstance(this).clearRefreshDate()
                com.example.reporterapp.app.PreferenceManager.getInstance(this).clearUserVerifiedFlag()
                finishAffinity()
                intent = Intent(this, SignInSignUpActivity::class.java)
                intent.putExtra("clearData", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        showMsg(this, "Please click back again to exit", Toast.LENGTH_SHORT)

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}





