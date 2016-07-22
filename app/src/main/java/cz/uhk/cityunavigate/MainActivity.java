package cz.uhk.cityunavigate;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapFragment.OnFragmentInteractionListener,
        TimeLineFragment.OnFragmentInteractionListener,
        GroupFragment.OnFragmentInteractionListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private FrameLayout frame;
    private FirebaseUser user;

    private TimeLineFragment tmlFrag;
    private MapFragment mapFrag;
    private GroupFragment grpFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frame = (FrameLayout)findViewById(R.id.mainContentLayoutFragment);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.getParent().getParent().requestDisallowInterceptTouchEvent(true); //při vysunutém drawer blokujeme content - nefunguje
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, StartupActivity.class));
            return;
        }


        //get DATA and SET ADAPTER
        //INIT FRAGMENT
        tmlFrag = TimeLineFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(frame.getId(), tmlFrag).commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (id == R.id.nav_timeline) {

            if(tmlFrag == null){
                tmlFrag = TimeLineFragment.newInstance();
                ft.add(frame.getId(), tmlFrag).commit();
            }
            else{
                if(mapFrag != null)
                    ft.hide(mapFrag);
                if(grpFrag != null)
                    ft.hide(grpFrag);
                ft.show(tmlFrag).commit();
            }

        } else if (id == R.id.nav_map) {

            if(mapFrag == null){
                mapFrag = cz.uhk.cityunavigate.MapFragment.newInstance();
                ft.add(frame.getId(), mapFrag).commit();

            }else{
                if(tmlFrag != null){
                    ft.hide(tmlFrag);
                if(grpFrag != null)
                    ft.hide(grpFrag);
                ft.show(mapFrag).commit();
            }

        }

        } else if (id == R.id.nav_group) {

            if(grpFrag == null){
                grpFrag = cz.uhk.cityunavigate.GroupFragment.newInstance();
                ft.add(frame.getId(), grpFrag).commit();

            }else{
                if(tmlFrag != null){
                    ft.hide(tmlFrag);
                if(mapFrag != null)
                    ft.hide(mapFrag);
                ft.show(grpFrag).commit();
                }
            }

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO KOMUNIKACE S FRAGMENTEM A AKTIVITOU
    }
}
