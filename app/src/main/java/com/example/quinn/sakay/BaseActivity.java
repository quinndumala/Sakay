package com.example.quinn.sakay;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_FRAGMENT = "tagDialogFragment";

    protected void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev == null) {
            ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(message);
            fragment.show(ft, TAG_DIALOG_FRAGMENT);
        }
    }

    protected void dismissProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getExistingDialogFragment();
        if (prev != null) {
            ft.remove(prev).commit();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private Fragment getExistingDialogFragment() {
        return getSupportFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENT);
    }

    public void launchMoreThanTwoWeeksDialog(){
        new MaterialDialog.Builder(this)
                .title("Warning")
                .content("Ride schedule is set to more than two weeks from now. You might want to change it" +
                        " to an earlier date.")
                .positiveText("OK")
                .cancelable(false)
                .show();
    }

    public void launchTimePassedDialog(){
        new MaterialDialog.Builder(this)
                .title("Warning")
                .content("Ride schedule can't be set to the past!")
                .positiveText("OK")
                .cancelable(false)
                .show();
    }

    public void missingInformationAlert(){
        new MaterialDialog.Builder(this)
                .content("Missing some information")
                .positiveText("OK")
                .cancelable(false)
                .show();
    }
}
