package com.deluxerom;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.grx.settings.R;
import com.grx.settings.utils.RootPrivilegedUtils;
import com.root.RootUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class ApplyKernelValues extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if ( action != null && !action.isEmpty() ) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    new Handler().postDelayed(() -> dlxApplyValues(context, "boot"), 500);
                    break;
                case "dlx_kernel_values":
                    dlxApplyValues(context, intent.getStringExtra("extravalue"));
                    break;
            }
        }
    }

    private void dlxApplyValues(Context context, String arg) {
        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("uname -a");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String kernel = log.toString();
        if ( !kernel.isEmpty() && kernel.contains("Deluxe")) {
            if ( !RootPrivilegedUtils.getIsDeviceRooted() ) {
                Toast.makeText(context, R.string.grxs_app_not_rooted, Toast.LENGTH_LONG).show();
            } else {
                if (!RootUtils.busyboxInstalled()) {
                    Toast.makeText(context, R.string.no_busybox, Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentResolver cr = context.getContentResolver();
                //RootUtils.runCommand("busybox mount -o rw,remount /system");
                switch (arg) {
                    case "boot":
                        bootCompleted(context);
                        break;
                    case "dt2w":
                        setDT2W(context);
                        break;
                    case "s2s":
                        setS2S(cr);
                        break;
                    case "s2w":
                        setS2W(cr);
                        break;
                    case "gesture_vibration":
                        setGestureVibration(cr);
                        break;
                    case "governors":
                        setGovernors(cr);
                        break;
                    case "schedulers":
                        setSchedulers(cr);
                        break;
                    case "selinux":
                        setSELinux(cr);
                        break;
                }
                //RootUtils.runCommand("busybox mount -o ro,remount /system");
            }
        }
    }

    private void bootCompleted(Context context) {
        Log.d("DLX", "SETTING KERNEL VALUES (ON-BOOT)");
        //setSELinux(contentResolver);
        final ContentResolver contentResolver = context.getContentResolver();
        setDT2W(context);
        setS2S(contentResolver);
        setS2W(contentResolver);
        setGestureVibration(contentResolver);
        setGovernors(contentResolver);
        setSchedulers(contentResolver);
    }

    private void setDT2W(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        final int dt2w = Settings.System.getInt(contentResolver, "dlx_kernel_dt2w", 0);
        RootUtils.runCommand("busybox echo " + dt2w + " > /sys/android_touch/doubletap2wake");
        // Warning when aod is tape to show and dt2w is enabled, but not if busybox is not installed
        if (dt2w == 1 && RootUtils.busyboxInstalled()
            && Settings.System.getInt(contentResolver, "aod_mode", 0) == 1 && Settings.System.getInt(contentResolver, "aod_tap_to_show_mode", 0) == 1) {
            Toast.makeText(context, R.string.aod_wake, Toast.LENGTH_LONG).show();
        }
    }

    private void setS2S(ContentResolver contentResolver) {
        RootUtils.runCommand("busybox echo " + Settings.System.getInt(contentResolver, "dlx_kernel_s2s", 0) + " > /sys/android_touch/sweep2sleep");
    }

    private void setS2W(ContentResolver contentResolver) {
        RootUtils.runCommand("busybox echo " + dlxGenerateValue(Settings.System.getString(contentResolver, "dlx_kernel_s2w")) + " > /sys/android_touch/sweep2wake");
    }

    private void setGestureVibration(ContentResolver contentResolver) {
        RootUtils.runCommand("busybox echo " + Settings.System.getInt(contentResolver, "dlx_kernel_gest_vibration", 0) + " > /sys/android_touch/vib_strength");
    }

    private void setGovernors(ContentResolver cr) {
        String all = Settings.System.getString(cr, "dlx_kernel_governors");
        if ( all == null || all.isEmpty() || !all.contains("big") || !all.contains("little") ) { all = "big;interactive|little;interactive|"; }
        final String[] main = all.split(Pattern.quote("|"));
        String big = main[0].split(";")[1];
        String path = "/sys/devices/system/cpu/cpufreq/policy4/scaling_governor";
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + big + " > " + path + "; chmod 444 " + path + ";");
        String little = main[1].split(";")[1];
        path = "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor";
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + little + " > " + path + "; chmod 444 " + path + ";");
    }

    private void setSchedulers(ContentResolver cr) {
        String path = "/sys/block/sda/queue/scheduler";
        String sch = Settings.System.getString(cr, "dlx_kernel_schedulers");
        if ( sch == null || sch.isEmpty() || !sch.contains("internal") ) { sch = "internal;cfq|external;cfq|"; }
        final String[] main = sch.split(Pattern.quote("|"));
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo '" + main[0].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
        path = path.replace("sda", "mmcblk0");
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo '" + main[1].split(";")[1] + "' > " + path + "; chmod 644 " + path + ";");
    }

    private void setSELinux(ContentResolver cr) {
        final String path = "/sys/fs/selinux/enforce";
        final int selinux = Settings.System.getInt(cr, "dlx_kernel_selinux", 1);
        RootUtils.runCommand("chmod 744 " + path + "; busybox echo " + selinux + " > " + path + "; chmod 644 " + path + ";");
    }

    private int dlxGenerateValue(String i) {
        int x = 0;
        if ( i == null || i.isEmpty() ) { i = "0,"; }
        for ( String d : i.split(Pattern.quote(",")) ) {
            x += Integer.parseInt(d);
        }
        return x;
    }

}
