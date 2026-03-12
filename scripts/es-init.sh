#!/bin/sh
# 检查并安装 IK 中文分词插件
set -e

PLUGIN_DIR=/usr/share/elasticsearch/plugins
IK_VERSION=${ES_VERSION:-8.13.4}

if [ -d "$PLUGIN_DIR/analysis-ik" ]; then
    echo "IK plugin already installed, skipping."
    exit 0
fi

echo "Installing IK analysis plugin v${IK_VERSION}..."

# 优先从官方或镜像源安装
/usr/share/elasticsearch/bin/elasticsearch-plugin install --batch \
    "https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-${IK_VERSION}.zip" 2>/dev/null || \
/usr/share/elasticsearch/bin/elasticsearch-plugin install --batch \
    "analysis-ik" 2>/dev/null || \
echo "WARNING: IK plugin installation failed. Search will use standard analyzer."

echo "Done."
