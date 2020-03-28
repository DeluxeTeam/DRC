
/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */



package com.grx.settings.prefs_dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.grx.settings.GrxPreferenceScreen;
import com.grx.settings.R;
import com.grx.settings.utils.Common;


public class DlgFrEditText extends DialogFragment {

    private OnGrxEditTextListener mCallBack;
    private String mkey;
    private String mHelperFragmentName;
    private String mValue;
    private EditText mEditText;

    public DlgFrEditText(){}

    public interface OnGrxEditTextListener{
        void onEditTextDone(String text);
    }


    public static DlgFrEditText newInstance(OnGrxEditTextListener callback, String help_frg, String key, String tit, String value){

        DlgFrEditText ret = new DlgFrEditText();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,help_frg);
        bundle.putString("key",key);
        bundle.putString("tit",tit);
        bundle.putString("val",value);
        ret.setArguments(bundle);
        ret.iniFragment(callback);
        return ret;
    }


       private void iniFragment(OnGrxEditTextListener callback){
        mCallBack=callback;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mValue = mEditText.getText().toString();
        outState.putString("val", mValue);

    }





    private View getDialogView(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.dlg_grxedittext,null);
        mEditText = view.findViewById(R.id.gid_edittext);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        mHelperFragmentName=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mkey=getArguments().getString("key");
        String mTitle = getArguments().getString("tit");
        mValue=getArguments().getString("val");

        if(state!=null){
            mValue=state.getString("val");
        }
        if(mValue==null) mValue="";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle)
                .setView(getDialogView())
                .setPositiveButton(R.string.grxs_ok, (dialog, which) -> {
                    if(mCallBack==null) {
                        if(mHelperFragmentName.equals(Common.TAG_PREFSSCREEN_FRAGMENT)){
                            GrxPreferenceScreen prefsScreen =(GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                            mCallBack=(OnGrxEditTextListener) prefsScreen.findAndGetCallBack(mkey);
                        }else mCallBack=(OnGrxEditTextListener) getFragmentManager().findFragmentByTag(mHelperFragmentName);
                    }
                    if(mCallBack!=null) mCallBack.onEditTextDone(mEditText.getText().toString());
                    dismiss();
                })
                .setNegativeButton(R.string.grxs_cancel, (dialog, which) -> dismiss());
        mEditText.append(mValue);
        AlertDialog ad = builder.create();
        ad.setOnShowListener(dialog -> mEditText.setSelection(mEditText.getText().length()));
        return ad;
    }


}
