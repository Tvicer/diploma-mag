#!/usr/bin/env python3
import json
import logging
import os
import requests
from kazoo.client import KazooClient
from kazoo.exceptions import NoNodeError
import time

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

ZOOKEEPER_HOSTS = ['diploma-zookeeper-patroni-1:2182', 'diploma-zookeeper-patroni-2:2183']
PATRONI_ZNODE = '/service/postgres-cluster/leader'
# WEBHOOK_URL = 'http://172.17.0.1:8080/api/v1/patroni/state/leader/change'
WEBHOOK_URL = 'http://host.docker.internal:8080/api/v1/patroni/state/leader/change'
LAST_LEADER_FILE = '/tmp/last_patroni_leader.txt'
FIRST_RUN_FLAG_FILE = '/tmp/patroni_watcher_first_run'

class PatroniLeaderWatcher:
    def __init__(self):
        self.zk = KazooClient(hosts=','.join(ZOOKEEPER_HOSTS))
        self.last_leader = self.load_last_leader()
        self.is_first_run = self.check_first_run()

    def check_first_run(self):
        """Проверяет, запущен ли скрипт в первый раз"""
        if not os.path.exists(FIRST_RUN_FLAG_FILE):
            # Создаем файл-флаг, чтобы отметить, что первый запуск уже был
            with open(FIRST_RUN_FLAG_FILE, 'w') as f:
                f.write(str(time.time()))
            logger.info("First run detected - will send leader info on startup if available")
            return True
        return False

    def load_last_leader(self):
        """Загружает последнего известного лидера из файла"""
        if os.path.exists(LAST_LEADER_FILE):
            with open(LAST_LEADER_FILE, 'r') as f:
                return f.read().strip()
        return None

    def save_last_leader(self, leader):
        """Сохраняет текущего лидера в файл"""
        with open(LAST_LEADER_FILE, 'w') as f:
            f.write(leader)

    def send_webhook(self, leader_data, event_type="leader_changed"):
        """Отправляет webhook при смене лидера или при первом запуске"""
        try:
            payload = {
                "eventType": event_type,
                "newLeaderName": leader_data,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
                "cluster": "postgres-cluster",
                "source": "zookeeper-callback",
                "firstRun": self.is_first_run if event_type == "leader_initial" else False
            }

            response = requests.post(
                WEBHOOK_URL,
                json=payload,
                timeout=5,
                headers={'Content-Type': 'application/json'}
            )

            logger.info(f"Webhook sent. Event: {event_type}, Status: {response.status_code}")
            if response.status_code != 200:
                logger.warning(f"Unexpected response: {response.text}")

        except Exception as e:
            logger.error(f"Failed to send webhook: {e}")

    def wait_for_leader_with_timeout(self, timeout=300):
        """Ожидает появления leader узла с таймаутом"""
        start_time = time.time()
        while time.time() - start_time < timeout:
            try:
                leader_data, stat = self.zk.get(PATRONI_ZNODE)
                if leader_data:
                    return leader_data.decode('utf-8')
            except NoNodeError:
                logger.info(f"Waiting for leader znode to be created... ({int(time.time() - start_time)}s)")
            time.sleep(5)
        return None

    def watch_leader(self, data, stat):
        """Callback функция при изменении leader znode"""
        if data:
            leader_info = data.decode('utf-8')
            logger.info(f"Leader node changed: {leader_info}")

            if leader_info != self.last_leader:
                logger.info(f"Leader changed from {self.last_leader} to {leader_info}")
                self.send_webhook(leader_info, "leader_changed")
                self.save_last_leader(leader_info)
                self.last_leader = leader_info
        else:
            logger.warning("Leader node data is empty")

    def run(self):
        """Основной цикл мониторинга"""
        try:
            self.zk.start()
            logger.info(f"Connected to ZooKeeper: {ZOOKEEPER_HOSTS}")
            logger.info(f"Monitoring znode: {PATRONI_ZNODE}")

            # Получаем текущего лидера
            current_leader = None
            try:
                current_leader_data, stat = self.zk.get(PATRONI_ZNODE)
                if current_leader_data:
                    current_leader = current_leader_data.decode('utf-8')
                    logger.info(f"Current leader found: {current_leader}")

                    # Отправляем информацию о лидере при первом запуске
                    if self.is_first_run:
                        logger.info(f"First run - sending initial leader info: {current_leader}")
                        self.send_webhook(current_leader, "leader_initial")
                        self.save_last_leader(current_leader)
                        self.last_leader = current_leader
                    elif current_leader != self.last_leader:
                        logger.info(f"Initial leader (not first run): {current_leader}")
                        self.send_webhook(current_leader, "leader_changed")
                        self.save_last_leader(current_leader)
                        self.last_leader = current_leader
                    else:
                        logger.info(f"Leader unchanged from previous run: {current_leader}")
            except NoNodeError:
                logger.warning(f"Leader znode {PATRONI_ZNODE} does not exist yet")

                if self.is_first_run:
                    logger.info("First run - waiting for leader znode to be created...")
                    # Ожидаем появления лидера с таймаутом
                    waited_leader = self.wait_for_leader_with_timeout()
                    if waited_leader:
                        logger.info(f"Leader appeared after waiting: {waited_leader}")
                        self.send_webhook(waited_leader, "leader_initial")
                        self.save_last_leader(waited_leader)
                        self.last_leader = waited_leader
                    else:
                        logger.warning("Timeout waiting for leader znode - no leader found")
                        # Отправляем уведомление об отсутствии лидера
                        self.send_webhook("NO_LEADER", "leader_initial")
                else:
                    # Отправляем уведомление о том, что лидер пропал
                    logger.warning("Leader znode disappeared!")
                    if self.last_leader:
                        self.send_webhook("LEADER_LOST", "leader_lost")
                        self.last_leader = None

            # Устанавливаем watcher
            @self.zk.DataWatch(PATRONI_ZNODE)
            def watch_node(data, stat):
                if data:
                    leader_info = data.decode('utf-8')
                    logger.info(f"Leader node changed: {leader_info}")

                    if leader_info != self.last_leader:
                        logger.info(f"Leader changed from {self.last_leader} to {leader_info}")
                        self.send_webhook(leader_info, "leader_changed")
                        self.save_last_leader(leader_info)
                        self.last_leader = leader_info
                else:
                    logger.warning("Leader node data is empty - leader might have been removed")
                    if self.last_leader:
                        self.send_webhook("LEADER_LOST", "leader_lost")
                        self.last_leader = None

            # Держим скрипт запущенным
            while True:
                time.sleep(1)

        except KeyboardInterrupt:
            logger.info("Stopping watcher...")
        except Exception as e:
            logger.error(f"Error in watcher: {e}")
        finally:
            self.zk.stop()
            self.zk.close()

if __name__ == '__main__':
    watcher = PatroniLeaderWatcher()
    watcher.run()