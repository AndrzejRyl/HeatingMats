#!/usr/bin/expect -f
# This is a script which automatically makes raspberry pi discoverable through BT
# and pairs with phone with correct PIN

set prompt "#"
set timeout -1

spawn sudo bluetoothctl
expect -re $prompt

send "power on\r"
expect -re $prompt

send "discoverable on\r"
expect -re $prompt

send "agent on\r"
expect -re $prompt

send "default-agent\r"
expect -re $prompt

send "pairable on\r"
expect -re "\[agent\] Confirm passkey 854345 .*|Device .* Paired: yes"

send "yes\r"
expect -re $prompt

send "quit\r"
expect eof