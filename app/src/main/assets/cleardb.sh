#!/system/bin/sh
export PATH=/system/bin:$PATH

for db in system secure global; do
    xml=/data/system/users/0/settings_$db.xml
    if [[ -f $xml ]]; then
        list=$(grep "dlx" $xml | cut -d= -f3 | cut -d" " -f1)
        echo $list | tr -d "\"" | tr " " "\\n" | while read line; do settings delete $db $line; done
        for key in icon_blacklist heads_up_notifications_enabled navigationbar_color; do
            settings delete $db $key
        done;
    fi
done;
