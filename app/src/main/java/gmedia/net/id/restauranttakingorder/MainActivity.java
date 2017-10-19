package gmedia.net.id.restauranttakingorder;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.SessionManager;

import gmedia.net.id.restauranttakingorder.Order.MainOpenOrder;
import gmedia.net.id.restauranttakingorder.Order.MainOrder;
import gmedia.net.id.restauranttakingorder.Printer.MainPrinter;
import gmedia.net.id.restauranttakingorder.Profile.MainProfile;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.MainRiwayatPemesanan;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private NavigationView navigationView;
    private View headerView;
    private TextView tvUser;
    private SessionManager session;

    private static boolean doubleBackToExitPressedOnce;
    private boolean exitState = false;
    private int timerClose = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Check close statement
        doubleBackToExitPressedOnce = false;
        Bundle bundle = getIntent().getExtras();
        boolean firstState = true;
        initUI();

        if(bundle != null){

            if(bundle.getBoolean("exit", false)){
                exitState = true;
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }

            if(bundle.getBoolean("riwayat", false)){

                firstState = false;
                navigationView.setCheckedItem(R.id.nav_riwayat_pemesanan);
                fragment = new MainRiwayatPemesanan();
                callFragment(fragment);
            }
        }

        if(savedInstanceState == null && firstState){
            navigationView.setCheckedItem(R.id.nav_order);
            /*FrameLayout flContainer = (FrameLayout) findViewById(R.id.fl_main_container);
            flContainer.removeAllViews();*/
            fragment = new MainOpenOrder();
            callFragment(fragment);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

        }
    }

    private void initUI() {

        session = new SessionManager(MainActivity.this);
        headerView = navigationView.getHeaderView(0);
        tvUser = (TextView) headerView.findViewById(R.id.tv_user);
        tvUser.setText(session.getUserInfo(SessionManager.TAG_NAMA));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("exit", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //System.exit(0);
            }

            if(!exitState && !doubleBackToExitPressedOnce){
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getResources().getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, timerClose);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        /*FrameLayout flContainer = (FrameLayout) findViewById(R.id.fl_main_container);
        flContainer.removeAllViews();*/

        setTitle(item.getTitle());

        int id = item.getItemId();

        if (id == R.id.nav_order) {
            fragment = new MainOpenOrder();
            callFragment(fragment);
        } else if (id == R.id.nav_riwayat_pemesanan) {
            fragment = new MainRiwayatPemesanan();
            callFragment(fragment);
        } else if (id == R.id.nav_printer) {
            fragment = new MainPrinter();
            callFragment(fragment);
        } else if (id == R.id.nav_profile) {
            fragment = new MainProfile();
            callFragment(fragment);
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            session.logoutUser(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Fragment fragment;
    private void callFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_main_container, fragment, fragment.getClass().getSimpleName()).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).addToBackStack(null).commit();
    }
}
