
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

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.grx.settings.GrxPreferenceScreen;
import com.grx.settings.R;
import com.grx.settings.utils.Common;


import java.util.ArrayList;
import java.util.regex.Pattern;


public class DlgFrMultiSelect extends DialogFragment  {  //single (ListPreference) and multiselectList preferences ..

    private TextView vSelectedTxt;
    private ListView vList;

    private ArrayList<GrxInfoItem> mItemList;

    private GrxMultiSelectListener mCallBack;

    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private String mHelperFragment;
    private String mKey;
    private String mValue;
    private String mOriValue;
    private String mSeparator;
    private int mMaxNumOfAccesses;

    private int mIconsTintColor=0;

    private int mNumChecked;
    private String mSummAuxString;


    public DlgFrMultiSelect(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface GrxMultiSelectListener{
        void GrxSetMultiSelect(String value);
    }


    private void saveCallback(DlgFrMultiSelect.GrxMultiSelectListener callback){
        mCallBack =callback;
    }

    public static DlgFrMultiSelect newInstance(DlgFrMultiSelect.GrxMultiSelectListener callback,String HelperFragment, String key, String title, String value,
                                                  int id_array_options, int id_array_values, int id_array_icons, int iconstintcolor, String separtor, int maxitems
    ){


        DlgFrMultiSelect ret = new DlgFrMultiSelect();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,HelperFragment);
        bundle.putString("key",key);
        bundle.putString("tit",title);
        bundle.putString("val",value);
        bundle.putInt("opt_arr_id",id_array_options);
        bundle.putInt("val_array_id",id_array_values);
        bundle.putInt("icons_array_id", id_array_icons );
        bundle.putInt("icons_tintcolor", iconstintcolor);
        bundle.putString("separator", separtor);
        bundle.putInt("max_items", maxitems);
        ret.setArguments(bundle);
        ret.saveCallback(callback);
        return ret;

    }


    /************  DIALOG, VIEW, INSTANCE ************************/

    private void checkCallback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrMultiSelect.GrxMultiSelectListener) prefsScreen.findAndGetCallBack(mKey);
            }else mCallBack=(DlgFrMultiSelect.GrxMultiSelectListener) getFragmentManager().findFragmentByTag(mHelperFragment);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle state) {

        mHelperFragment=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mKey=getArguments().getString("key");
        String mTitle = getArguments().getString("tit");
        mOriValue=getArguments().getString("val");
        mValue=mOriValue;
        mIdOptionsArr= getArguments().getInt("opt_arr_id");
        mIdValuesArr=getArguments().getInt("val_array_id");
        mIdIconsArray=getArguments().getInt("icons_array_id");
        mIconsTintColor = getArguments().getInt("icons_tintcolor");
        mSeparator=getArguments().getString("separator");
        mMaxNumOfAccesses = getArguments().getInt("max_items");

        if (state != null) {
            mValue =  state.getString("curr_val");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(getMultiSelectView());


        if(mMaxNumOfAccesses!=1){ //is multivalue
            builder.setNegativeButton(R.string.grxs_cancel, (dialog, which) -> {
                mValue = mOriValue;
                setResultAndExit();
            });
            builder.setPositiveButton(R.string.grxs_ok, (dialog, which) -> {
                mValue= getReturnValue();
                setResultAndExit();
            });
        }
        mItemList = new ArrayList<>();
        mNumChecked=0;
        initItemsList();
        if (mMaxNumOfAccesses != 0) { mSummAuxString = "( " + mMaxNumOfAccesses + " max.)"; }
        else mSummAuxString=" ";
        updateSummary();
        setListAdapter();
        return builder.create();
    }

    private View getMultiSelectView(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.dlg_grxmultiselect, null);
        vList = view.findViewById(R.id.gid_listview);
        if(mMaxNumOfAccesses==0){
            LinearLayout vCheck = view.findViewById(R.id.gid_check_button);
            vCheck.setOnClickListener(v -> checkAllOptions(true));
            LinearLayout vUncheck = view.findViewById(R.id.gid_uncheck_button);
            vUncheck.setOnClickListener(v -> checkAllOptions(false));
        }else {
            LinearLayout vBotones = view.findViewById(R.id.gid_buttons_container);
            vBotones.setVisibility(View.GONE);
            LinearLayout vSeparador = view.findViewById(R.id.gid_separator);
            vSeparador.setVisibility(View.GONE);
        }

        vSelectedTxt = view.findViewById(R.id.gid_items_selected);
        if(mMaxNumOfAccesses==1) vSelectedTxt.setVisibility(View.GONE);
        vList.setDividerHeight(Common.cDividerHeight);
        return view;

    }



    private void initItemsList(){

        TypedArray icons_array=null;
        String[] vals_array = getResources().getStringArray(mIdValuesArr);
        String[] opt_array = getResources().getStringArray(mIdOptionsArr);
        if(mIdIconsArray!=0){
            icons_array = getResources().obtainTypedArray(mIdIconsArray);
        }
        String[] selected =null;
        if(mSeparator.isEmpty()) {
            selected = new String[1];
            selected[0]=mValue;
        }
        else{
            if(mValue!=null && !mValue.isEmpty()) selected=mValue.split(Pattern.quote(mSeparator));
        }

        for(int i=0;i<vals_array.length;i++){
            Drawable drwtmp = null;
            if(icons_array!=null) {
                drwtmp = icons_array.getDrawable(i);
            }

            int last_pos_for_single_choice=0;

            boolean isChecked = false;
            if(selected!=null){
                for (String s : selected) {
                    if (s.equals(vals_array[i])) {
                        isChecked = true;
                        mNumChecked++;
                        break;
                    }
                }
            }
            mItemList.add(new GrxInfoItem(opt_array[i], vals_array[i], drwtmp, isChecked));
        }
        if(icons_array!=null) icons_array.recycle();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("curr_val", getReturnValue());
    }


    private int getLastVisiblePosForSingleChoiceMode(){
        int pos=0;
        for(int i=0;i<mItemList.size();i++) {
            if(mItemList.get(i).isChecked()) {
                pos=i;
                break;
            }
        }
        return pos;
    }

    private void setListAdapter(){
        vList.setAdapter(mAdapter);
        if(mMaxNumOfAccesses==1) vList.setSelection(getLastVisiblePosForSingleChoiceMode());


        vList.setOnItemClickListener((parent, view, position, id) -> {

            if(mMaxNumOfAccesses==1) {
                mValue=mItemList.get(position).getValue();
                setResultAndExit();
            }else{
                boolean tmp;
                tmp=mItemList.get(position).isChecked();

                if(tmp){
                    tmp=false;
                    mNumChecked--;
                    mItemList.get(position).setChecked(tmp);
                    mAdapter.notifyDataSetChanged();
                    updateSummary();
                    // on the fly when deleted
                    mValue = getReturnValue();
                    checkCallback();
                    if (mCallBack == null) this.dismiss();
                    mCallBack.GrxSetMultiSelect(mValue);
                }
                else{
                    tmp = true;
                    if(mMaxNumOfAccesses==0 ){
                        mNumChecked++;
                        mItemList.get(position).setChecked(tmp);
                        mAdapter.notifyDataSetChanged();
                        updateSummary();
                        // on the fly when added
                        mValue = getReturnValue();
                        checkCallback();
                        if (mCallBack == null) this.dismiss();
                        mCallBack.GrxSetMultiSelect(mValue);
                    }else{
                        if(mNumChecked<mMaxNumOfAccesses) {
                            mNumChecked++;
                            mItemList.get(position).setChecked(tmp);
                            mAdapter.notifyDataSetChanged();
                            updateSummary();
                        }else Toast.makeText(getActivity(),R.string.grxs_max_choices_warning,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void setResultAndExit(){
        checkCallback();
        if (mCallBack == null) this.dismiss();
        mCallBack.GrxSetMultiSelect(mValue);
        mItemList.clear();
        this.dismiss();
    }


    private String getReturnValue(){
        StringBuilder resultado= new StringBuilder();
        for(int i=0;i<mItemList.size();i++){
            if(mItemList.get(i).isChecked()){
                resultado.append(mItemList.get(i).getValue());
                resultado.append(mSeparator);
            }
        }
        return resultado.toString();
    }


    private void checkAllOptions(boolean check){

        if(check) mNumChecked=mItemList.size();
        else mNumChecked=0;
        for(int i=0;i<mItemList.size();i++){
            mItemList.get(i).setChecked(check);
        }
        updateSummary();
        mAdapter.notifyDataSetChanged();

        // on-the-fly
        mValue = getReturnValue();
        checkCallback();
        if (mCallBack == null) this.dismiss();
        mCallBack.GrxSetMultiSelect(mValue);
    }



    private void updateSummary(){
        vSelectedTxt.setText(getString(R.string.grxs_num_selected,mNumChecked)+" "+mSummAuxString);
    }


    /******************* Item Info and Adapter ***********/

    private static class GrxInfoItem {

        private final String mLabel;
        private final String mValue;
        private final Drawable mIcon;
        private boolean mIsChecked;

        GrxInfoItem(String label, String value, Drawable icon, boolean checked){

            mLabel=label;
            mValue=value;
            mIcon=icon;
            mIsChecked=checked;

        }

        String getLabel(){
            return mLabel;
        }

        String getValue(){
            return mValue;
        }

        boolean isChecked(){
            return mIsChecked;
        }

        Drawable getIcon(){
            return mIcon;
        }

        void setChecked(boolean checked){
            mIsChecked=checked;
        }

    }



    private final BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.dlg_grxmultiselect_item, null);
                cvh.vIcon = convertView.findViewById(R.id.gid_icon);
                cvh.vLabel = convertView.findViewById(R.id.gid_text);
                cvh.vRadioButton = convertView.findViewById(R.id.gid_radiobutton);
                cvh.vCheckBox = convertView.findViewById(R.id.gid_checkbox);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            GrxInfoItem grxInfoItem = (GrxInfoItem) this.getItem(position);
            if(mMaxNumOfAccesses==1) {
                cvh.vRadioButton.setVisibility(View.VISIBLE);
                cvh.vRadioButton.setChecked(grxInfoItem.isChecked());
                cvh.vRadioButton.setClickable(false);
            }else {
                cvh.vCheckBox.setVisibility(View.VISIBLE);
                cvh.vCheckBox.setChecked(grxInfoItem.isChecked());
                cvh.vCheckBox.setClickable(false);
            }
            cvh.vLabel.setText(grxInfoItem.getLabel());
            if(mIdIconsArray==0) cvh.vIcon.setVisibility(View.GONE);
            else {
                if(mIconsTintColor!=0) cvh.vIcon.setColorFilter(mIconsTintColor);
                cvh.vIcon.setImageDrawable(grxInfoItem.getIcon());
            }


           return convertView;
        }

        class CustomViewHolder {
            ImageView vIcon;
            TextView vLabel;
            RadioButton vRadioButton;
            CheckBox vCheckBox;
        }
    };


}
