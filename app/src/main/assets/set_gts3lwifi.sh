#!/system/bin/sh
export PATH=/system/bin:$PATH

busybox mount -o rw,remount /system

sed -i s/'SM-N950F'/'SM-T820'/gi /system/build.prop

reboot