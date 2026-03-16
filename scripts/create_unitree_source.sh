#!/bin/bash

curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "宇树科技 Unitree",
    "description": "宇树科技官网机器人产品列表 - 四足机器人、人形机器人",
    "url_pattern": "https://www.unitree.com",
    "config": {
      "manufacturer": {
        "name": "宇树科技",
        "name_en": "Unitree Robotics",
        "country": "中国",
        "country_code": "CN",
        "website_url": "https://www.unitree.com"
      },
      "list_page": {
        "selector": "a[href*=\"/h1\"], a[href*=\"/go2\"], a[href*=\"/b2\"], a[href*=\"/g1\"], a[href*=\"/r1\"]",
        "url_attr": "href"
      },
      "detail_fields": {
        "name": {"selector": "h1"},
        "name_en": {"selector": "h1"},
        "description": {"selector": ".description, .intro"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "category": {"value": "四足机器人/人形机器人"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 5 * * ?",
    "max_pages": 3,
    "delay_ms": 2000
  }'

echo ""
echo "宇树科技数据源创建完成"
