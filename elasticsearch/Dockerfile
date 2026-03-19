FROM elasticsearch:8.13.4

# 安装运行时依赖（例如 curl，用于健康检查）
USER root
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 安装 IK 中文分词插件（在构建阶段完成，避免每次启动都重复下载）
COPY scripts/es-init.sh /usr/local/bin/es-init.sh
RUN chmod +x /usr/local/bin/es-init.sh && /usr/local/bin/es-init.sh

# 恢复为 Elasticsearch 默认运行用户
USER 1000
