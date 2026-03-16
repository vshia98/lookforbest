#!/bin/bash

# 创建更多机器人厂商数据源

# Fanuc 机器人
curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "FANUC 机器人",
    "description": "FANUC 官网工业机器人产品列表",
    "url_pattern": "https://www.fanuc.com/robots/en/",
    "config": {
      "manufacturer": {
        "name": "FANUC",
        "name_en": "FANUC Corporation",
        "country": "日本",
        "country_code": "JP",
        "website_url": "https://www.fanuc.com"
      },
      "list_page": {
        "selector": "a.robot-card, .robot-item a, .product-list a"
      },
      "detail_fields": {
        "name": {"selector": "h1, .product-name"},
        "name_en": {"selector": "h1, .product-name"},
        "description": {"selector": ".description, .specs"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 4 * * ?",
    "max_pages": 5,
    "delay_ms": 1500
  }'

# KUKA 机器人
curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "KUKA 机器人",
    "description": "KUKA 官网工业机器人产品列表",
    "url_pattern": "https://www.kuka.com/en/products/robots",
    "config": {
      "manufacturer": {
        "name": "KUKA",
        "name_en": "KUKA AG",
        "country": "德国",
        "country_code": "DE",
        "website_url": "https://www.kuka.com"
      },
      "list_page": {
        "selector": "a.robot-card, .robot-item a"
      },
      "detail_fields": {
        "name": {"selector": "h1, .product-name"},
        "name_en": {"selector": "h1, .product-name"},
        "description": {"selector": ".description"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 4 * * ?",
    "max_pages": 5,
    "delay_ms": 1500
  }'

# Yaskawa 安川机器人
curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Yaskawa 安川机器人",
    "description": "安川电机官网工业机器人产品列表",
    "url_pattern": "https://www.yaskawa.com/products/robots/",
    "config": {
      "manufacturer": {
        "name": "Yaskawa 安川",
        "name_en": "Yaskawa Electric",
        "country": "日本",
        "country_code": "JP",
        "website_url": "https://www.yaskawa.com"
      },
      "list_page": {
        "selector": "a.robot-card, .robot-item a"
      },
      "detail_fields": {
        "name": {"selector": "h1"},
        "name_en": {"selector": "h1"},
        "description": {"selector": ".description"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 4 * * ?",
    "max_pages": 5,
    "delay_ms": 1500
  }'

# Mitsubishi 三菱机器人
curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mitsubishi 三菱机器人",
    "description": "三菱电机官网工业机器人产品列表",
    "url_pattern": "https://www.mitsubishielectric.com/fa/products/rbt/robot/",
    "config": {
      "manufacturer": {
        "name": "Mitsubishi 三菱",
        "name_en": "Mitsubishi Electric",
        "country": "日本",
        "country_code": "JP",
        "website_url": "https://www.mitsubishielectric.com"
      },
      "list_page": {
        "selector": "a.robot-card, .robot-item a"
      },
      "detail_fields": {
        "name": {"selector": "h1"},
        "name_en": {"selector": "h1"},
        "description": {"selector": ".description"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 4 * * ?",
    "max_pages": 5,
    "delay_ms": 1500
  }'

echo "数据源创建完成！"
