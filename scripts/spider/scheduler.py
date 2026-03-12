#!/usr/bin/env python3
"""
定时任务调度器：每天凌晨2点运行爬虫
用法: python scheduler.py
后台运行: nohup python scheduler.py > spider.log 2>&1 &
"""
import schedule
import time
import subprocess
import sys
import os
from datetime import datetime


def run_spider():
    print(f"[{datetime.now()}] 开始运行爬虫...")
    result = subprocess.run(
        [sys.executable, "main.py"],
        cwd=os.path.dirname(os.path.abspath(__file__)),
        capture_output=False,
    )
    print(f"[{datetime.now()}] 爬虫完成，退出码: {result.returncode}")


# 每天凌晨 02:00 运行
schedule.every().day.at("02:00").do(run_spider)

print("爬虫调度器已启动，每天 02:00 运行。按 Ctrl+C 退出。")
while True:
    schedule.run_pending()
    time.sleep(60)
