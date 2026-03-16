#!/bin/bash

# 创建 ABB 机器人数据源
curl -X POST http://localhost:8002/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ABB 机器人",
    "description": "ABB 官网工业机器人产品列表",
    "url_pattern": "https://new.abb.com/products/robotics/robots/articulated-robots",
    "config": {
      "manufacturer": {
        "name": "ABB Robotics",
        "name_en": "ABB Robotics",
        "country": "瑞士",
        "country_code": "CH",
        "website_url": "https://new.abb.com/products/robotics"
      },
      "list_page": {
        "selector": "a.product-card, a.card-link, div.robot-card a",
        "url_attr": "href"
      },
      "detail_fields": {
        "name": {"selector": "h1, .product-title, .robot-name"},
        "name_en": {"selector": "h1, .product-title, .robot-name"},
        "description": {"selector": ".description, .product-desc, .specs"},
        "cover_image_url": {"selector": "img.product-image, img.hero", "attr": "src"},
        "payload_kg": {"selector": ".payload", "regex": "([\\d.]+)"},
        "reach_mm": {"selector": ".reach", "regex": "([\\d]+)"},
        "price_range": {"value": "inquiry"}
      }
    },
    "is_active": true,
    "cron_expr": "0 0 3 * * ?",
    "max_pages": 5,
    "delay_ms": 1500
  }'
