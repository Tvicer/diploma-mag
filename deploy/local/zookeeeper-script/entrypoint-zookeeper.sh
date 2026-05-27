#!/bin/bash

# Запускаем ZooKeeper в фоне
/etc/confluent/docker/run &

# Ждем пока ZooKeeper запустится
sleep 10

# Устанавливаем Python и kazoo если используем Python скрипт
if command -v apt-get &> /dev/null; then
    apt-get update && apt-get install -y python3 python3-pip curl
    pip3 install kazoo requests
elif command -v yum &> /dev/null; then
    yum install -y python3 python3-pip curl
    pip3 install kazoo requests
fi

# Запускаем watcher скрипт
python3 /zk-patroni-watcher.py &

# Ждем все процессы
wait