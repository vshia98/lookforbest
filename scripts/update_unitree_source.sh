#!/bin/bash

curl -X PUT http://localhost:8002/api/sources/6 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "宇树科技 Unitree",
    "description": "宇树科技官网机器人产品 - 四足机器人、人形机器人、机械臂",
    "url_pattern": "https://www.unitree.com/h1/,https://www.unitree.com/go2/,https://www.unitree.com/b2/,https://www.unitree.com/g1/,https://www.unitree.com/r1/,https://www.unitree.com/z1/,https://www.unitree.com/b1/,https://www.unitree.com/a2/",
    "config": {
      "manufacturer": {
        "name": "宇树科技",
        "name_en": "Unitree Robotics",
        "country": "中国",
        "country_code": "CN",
        "website_url": "https://www.unitree.com"
      },
      "list_page": {
        "selector": "a",
        "url_attr": "href"
      },
      "detail_fields": {
        "name": {"selector": "h1"},
        "name_en": {"selector": "h1"},
        "description": {"selector": ".description, .intro, .spec"},
        "cover_image_url": {"selector": "img", "attr": "src"},
        "category": {"value": "四足机器人/人形机器人/机械臂"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 5 * * ?",
    "max_pages": 3,
    "delay_ms": 2000
  }'

echo ""
echo "宇树科技数据源已更新"
