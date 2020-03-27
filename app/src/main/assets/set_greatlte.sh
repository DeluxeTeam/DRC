#!/system/bin/sh
export PATH=/system/bin:$PATH

busybox mount -o rw,remount /system

sed -i s/'SM-T820'/'SM-N950F'/gi /system/build.prop

reboot