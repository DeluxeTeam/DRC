package com.grx.settings.prefs_dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grx.settings.GrxPreferenceScreen;
import com.grx.settings.R;
import com.grx.settings.utils.Common;
import com.grx.settings.utils.GrxPrefsUtils;
import com.sldv.Menu;
import com.sldv.MenuItem;
import com.sldv.SlideAndDragListView;

import java.util.ArrayList;
import java.util.regex.Pattern;


/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */

public class DlgFrGrxPerItemSingleSelection extends DialogFragment
        implements SlideAndDragListView.OnDragListener, SlideAndDragListView.OnListItemLongClickListener, SlideAndDragListView.OnListItemClickListener {

    private PerItemSingleSelectionDialogListener mCallBack = null;

    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private int mIdSpinnerValuesArray;
    private int mIdSpinnerOptionsArray;
    private boolean mShortOutOption;
    private String mHelperFragment;
    private String mKey;
    private String mValue;
    private String mOriValue;
    private String mSeparator;
    private boolean mIconblacklistDependency, mOnTheFly;

    private int mItemClicked = -1;

    private int mIconsTintColor=0;

    private ArrayList<ItemInfo> mItemList;
    private SlideAndDragListView mDragList;

    public DlgFrGrxPerItemSingleSelection(){}



    public interface PerItemSingleSelectionDialogListener{
        void onPerItemSingleSelectionSet(String value);
    }



    private void saveCallback(DlgFrGrxPerItemSingleSelection.PerItemSingleSelectionDialogListener callback){
        mCallBack =callback;
    }

    public static DlgFrGrxPerItemSingleSelection newInstance(DlgFrGrxPerItemSingleSelection.PerItemSingleSelectionDialogListener callback,String HelperFragment, String key, String title, String value,
                                                             int id_array_options, int id_array_values, int id_array_icons, int id_array_spinneroptios, int id_array_spinnervalues,
                                                             int iconstintcolor, String separtor, boolean shortout, boolean check_icon_blacklist, boolean onTheFly
    ) {


        DlgFrGrxPerItemSingleSelection ret = new DlgFrGrxPerItemSingleSelection();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY, HelperFragment);
        bundle.putString("key", key);
        bundle.putString("tit", title);
        bundle.putString("val", value);
        bundle.putInt("opt_arr_id",id_array_options);
        bundle.putInt("val_array_id",id_array_values);
        bundle.putInt("icons_array_id", id_array_icons );
        bundle.putInt("icons_tintcolor", iconstintcolor);
        bundle.putString("separator", separtor);
        bundle.putInt("spinner_options_array_id",id_array_spinneroptios);
        bundle.putInt("spinner_values_array_id",id_array_spinnervalues);
        bundle.putBoolean("shortout",shortout);
        bundle.putBoolean("check_icon_blacklist", check_icon_blacklist);
        bundle.putBoolean("onTheFly", onTheFly);
        ret.setArguments(bundle);
        ret.saveCallback(callback);
        return ret;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mValue= getReturnValue();
        outState.putString("curr_val", mValue);
    }

    /************  DIALOG, VIEW, INSTANCE ************************/

    private void checkCallback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrGrxPerItemSingleSelection.PerItemSingleSelectionDialogListener) prefsScreen.findAndGetCallBack(mKey);
            }else mCallBack=(DlgFrGrxPerItemSingleSelection.PerItemSingleSelectionDialogListener) getFragmentManager().findFragmentByTag(mHelperFragment);
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle state) {

        mHelperFragment = getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mKey = getArguments().getString("key");
        String mTitle = getArguments().getString("tit");
        mOriValue=getArguments().getString("val");
        mValue=mOriValue;
        mIdOptionsArr = getArguments().getInt("opt_arr_id");
        mIdValuesArr = getArguments().getInt("val_array_id");
        mIdIconsArray = getArguments().getInt("icons_array_id");
        mIconsTintColor = getArguments().getInt("icons_tintcolor");
        mSeparator = getArguments().getString("separator");
        mIdSpinnerOptionsArray = getArguments().getInt("spinner_options_array_id");
        mIdSpinnerValuesArray = getArguments().getInt("spinner_values_array_id");
        mShortOutOption = getArguments().getBoolean("shortout");
        mIconblacklistDependency = getArguments().getBoolean("check_icon_blacklist");
        mOnTheFly = getArguments().getBoolean("onTheFly");

        if (state != null) {
            mValue = state.getString("curr_val");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(getDialogView());
        builder.setNegativeButton(R.string.grxs_cancel, (dialog, which) -> {
                mValue= mOriValue;
                mCallBack.onPerItemSingleSelectionSet(mValue);
                dismiss();
        });
        builder.setPositiveButton(R.string.grxs_ok, (dialog, which) -> setResultAndDoCallback());
        initItemsList();
        ini_drag_list();
        return builder.create();

    }


    private void setResultAndDoCallback(){
        checkCallback();
        if(mCallBack==null ) return;
        mValue= getReturnValue();
        if(mOriValue==null) mOriValue="";
        if(mValue.equals(mOriValue)) return;
        mCallBack.onPerItemSingleSelectionSet(mValue);

    }
    private View getDialogView(){

        View view = getActivity().getLayoutInflater().inflate(R.layout.dlg_peritemsingleselection, null);
        mDragList = view.findViewById(R.id.gid_slv_listview);

        if(mShortOutOption) {
            view.findViewById(R.id.gid_list_title ).setVisibility(View.VISIBLE);
        }

       return view;
    }


    private String getReturnValue(){
        StringBuilder value = new StringBuilder();

        for(int i=0; i< mItemList.size();i++){
            value.append(mItemList.get(i).getValue());
            value.append(mSeparator);
        }
       return value.toString();
    }

    private void initItemsList(){

        mItemList = new ArrayList<>();

        TypedArray icons_array=null;
        String[] vals_array = getResources().getStringArray(mIdValuesArr);
        String[] opt_array = getResources().getStringArray(mIdOptionsArr);
        String[] spinner_vals =getResources().getStringArray(mIdSpinnerValuesArray);

        if(mIdIconsArray!=0){
            icons_array = getResources().obtainTypedArray(mIdIconsArray);
        }

        String[] values;
        if(mValue==null || mValue.isEmpty()) {
            mValue=generateDefaultValue(vals_array,spinner_vals);
        }

        values=mValue.split(Pattern.quote(mSeparator));

        for (String value : values) {
            String[] array = value.split(Pattern.quote(";"));
            int pos = GrxPrefsUtils.getPositionInStringArray(vals_array, array[0]);
            int posspinner = GrxPrefsUtils.getPositionInStringArray(spinner_vals, array[1]);
            Drawable drawable = null;
            if (icons_array != null) drawable = icons_array.getDrawable(pos);
            mItemList.add(new ItemInfo(value, opt_array[pos], vals_array[pos], spinner_vals[posspinner], drawable));
        }
        if(icons_array!=null) icons_array.recycle();

    }


    private void ini_drag_list(){
        Menu menu = new Menu(false,false);
        menu.addItem(new MenuItem.Builder().setWidth( 0 )
                .setBackground(getResources().getDrawable(R.drawable.ic_delete))
                .setText(" ")
                .setTextColor(Color.GRAY)
                .setTextSize(1)
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .build());

        mDragList.setMenu(menu);

        mDragList.setDividerHeight(Common.cDividerHeight);
        mDragList.setVerticalScrollBarEnabled(true);
        mDragList.setAdapter(mAdapter);

        if (mShortOutOption) {
            mDragList.setOnDragListener(this, mItemList);
        }
        mDragList.setOnListItemLongClickListener(this);
        mDragList.setOnListItemClickListener(this);
        //mAdapter.notifyDataSetChanged();
    }

    private String generateDefaultValue(String[] vals_array, String[] spinner_values_array){
        String returnval = "";
        for (String s : vals_array) {
            returnval += s;
            returnval += ";";
            returnval += spinner_values_array[0];
            returnval += mSeparator;
        }

        return returnval;
    }



    @Override
    public void onDragViewStart(int position) {
    }

    @Override
    public void onDragViewMoving(int position) {
    }

    @Override
    public void onDragViewDown(int position) {
    }


    @Override
    public void onListItemClick(View v, final int position) {

        if (mIconblacklistDependency && shouldHide(((DlgFrGrxPerItemSingleSelection.ItemInfo) mAdapter.getItem(position)).getValue())) {
            Toast.makeText(getActivity(), R.string.unhide_first, Toast.LENGTH_LONG).show();
            return;
        }

        mItemClicked=position;
        final String curval = mItemList.get(position).getSpinnerValue();
        int selected = GrxPrefsUtils.getPositionInStringArray(getResources().getStringArray(mIdSpinnerValuesArray),mItemList.get(position).getSpinnerValue());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.grxs_select_option);
        builder.setSingleChoiceItems(getResources().getStringArray(mIdSpinnerOptionsArray),
                selected, (dialogInterface, i) -> {

                    String new_value = getResources().getStringArray(mIdSpinnerValuesArray)[i];
                    if(!curval.equals(new_value)) {
                        mItemList.get(mItemClicked).setSpinnervalue(new_value);
                        mAdapter.notifyDataSetChanged();
                        if (mOnTheFly) setResultAndDoCallback();
                    }
                    mItemClicked=-1;
                    dialogInterface.dismiss();
                });

        builder.setSingleChoiceItems(getResources().getStringArray(mIdSpinnerOptionsArray), selected, (dialog, which) -> {
            String new_value = getResources().getStringArray(mIdSpinnerValuesArray)[which];
            if(!curval.equals(new_value)) {
                mItemList.get(mItemClicked).setSpinnervalue(new_value);
                mAdapter.notifyDataSetChanged();
                if (mOnTheFly) setResultAndDoCallback();
            }
            mItemClicked=-1;
            dialog.dismiss();
        });

        builder.setNegativeButton(R.string.grxs_cancel, (dialog, which) -> {
            mItemClicked=-1;
            dialog.dismiss();
        });
        builder.setPositiveButton(R.string.grxs_ok, (dialog, which) -> {
            mItemClicked=-1;
              dialog.dismiss();
        });
        builder.setOnDismissListener(dialogInterface -> mItemClicked=-1);
        builder.create().show();

    }


    @Override
    public void onListItemLongClick(View view, int position) {
    }


    private static class ItemInfo {
        private String mValue="";
        private String mOptionValue;
        private String mOptionText;
        private String mSpinnerValue;
        private Drawable mDrawable=null;


        ItemInfo(String value, String optiontext, String optionval, String spinnerval, Drawable drawable){
            if(value==null || value.isEmpty() ) return;
            mValue = value;
            mOptionValue=optionval;
            mOptionText=optiontext;
            mSpinnerValue=spinnerval;
            mDrawable = drawable;


        }
        String getText(){
            return mOptionText;
        }

        String getValue(){
            return mValue;
        }

        String getSpinnerValue(){return mSpinnerValue;}

        Drawable getmDrawable(){
            return mDrawable;
        }

        void setSpinnervalue(String value){
            mSpinnerValue=value;
            mValue=mOptionValue+";"+value;
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_dlg_peritem_single_sel, null);
                cvh.vCtxt = convertView.findViewById(R.id.gid_item_text);
                cvh.vIcon = convertView.findViewById(R.id.gid_item_img);
                cvh.vValue = convertView.findViewById(R.id.gid_item_value);

                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            DlgFrGrxPerItemSingleSelection.ItemInfo grxInfoItem = (DlgFrGrxPerItemSingleSelection.ItemInfo) this.getItem(position);
            cvh.vCtxt.setText(grxInfoItem.getText());

             int pos =  GrxPrefsUtils.getPositionInStringArray(getResources().getStringArray(mIdSpinnerValuesArray),
                           grxInfoItem.getSpinnerValue());
            cvh.vValue.setText(getResources().getStringArray(mIdSpinnerOptionsArray)[pos]);


            if(mIdIconsArray==0) cvh.vIcon.setVisibility(View.GONE);
            else {
                cvh.vIcon.setImageDrawable(grxInfoItem.getmDrawable());
                if(mIconsTintColor!=0)cvh.vIcon.setColorFilter(mIconsTintColor);
            }

            if (mIconblacklistDependency) {
                final float alpha = shouldHide(grxInfoItem.getValue()) ? 0.5f : 1;
                cvh.vCtxt.setAlpha(alpha);
                cvh.vValue.setAlpha(alpha);
            }

            // convertView.setMinimumHeight(minheight);
            return convertView;
        }

        class CustomViewHolder {
            TextView vCtxt;
            ImageView vIcon;
            TextView vValue;

        }
    };

    private final String blacklist = Common.sp.getString("dlx_icon_blacklist", "rotate,headset,circularbt,minit,weather,netspeed,sb_date,");

    private boolean shouldHide(String value) {
        if (value.contains("netspeed")) {
            return blacklist.contains("netspeed");
        } else if (value.contains("minit")) {
            return blacklist.contains("minit");
        } else if (value.contains("circular")) {
            return blacklist.contains("circular");
        } else if (value.contains("weather")) {
            return blacklist.contains("weather");
        } else if (value.contains("date")) {
            return blacklist.contains("date");
        } else if (value.contains("carrier")) {
            return blacklist.contains("carrier");
        } else if (value.contains("clock")) {
            return blacklist.contains(",clock");
        } else if (value.contains("battery")) {
            return blacklist.contains("battery");
        }
        return false;
    }

}
