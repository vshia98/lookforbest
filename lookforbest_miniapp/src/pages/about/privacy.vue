<template>
  <scroll-view scroll-y class="privacy-page">
    <view class="container">
      <view class="header">
        <text class="title">隐私政策</text>
        <text class="update-date">更新日期：2025年1月1日</text>
      </view>

      <view class="section">
        <text class="section-title">一、引言</text>
        <text class="section-content">
          欢迎使用 LookForBest（以下简称"本小程序"）。我们非常重视您的隐私保护，并承诺依据本隐私政策收集、使用和保护您的个人信息。请您在使用本小程序前仔细阅读本政策。
        </text>
      </view>

      <view class="section">
        <text class="section-title">二、我们收集的信息</text>
        <text class="section-content">
          1. 账号信息：您注册或登录时填写的用户名、密码（加密存储）等信息。{{ '\n' }}
          2. 使用数据：您浏览机器人详情、收藏、对比等操作记录，用于改善推荐体验。{{ '\n' }}
          3. 设备信息：设备型号、操作系统版本、网络状态等，用于提供适配服务。{{ '\n' }}
          4. 微信开放平台信息：若您使用微信登录，我们将获取您授权的微信昵称和头像。
        </text>
      </view>

      <view class="section">
        <text class="section-title">三、信息使用目的</text>
        <text class="section-content">
          我们收集的信息仅用于：{{ '\n' }}
          · 提供机器人搜索、展示、对比和收藏功能{{ '\n' }}
          · 个性化推荐内容{{ '\n' }}
          · 改善产品体验和技术问题排查{{ '\n' }}
          · 保障账号安全
        </text>
      </view>

      <view class="section">
        <text class="section-title">四、信息共享与披露</text>
        <text class="section-content">
          我们不会将您的个人信息出售给第三方。以下情况除外：{{ '\n' }}
          · 获得您的明确同意{{ '\n' }}
          · 法律法规要求{{ '\n' }}
          · 保护本小程序或用户的合法权益
        </text>
      </view>

      <view class="section">
        <text class="section-title">五、数据安全</text>
        <text class="section-content">
          我们采用行业标准的安全措施保护您的信息，包括 HTTPS 传输加密、密码哈希存储等。但请注意，互联网环境并非百分百安全，我们将尽力保护您的信息。
        </text>
      </view>

      <view class="section">
        <text class="section-title">六、您的权利</text>
        <text class="section-content">
          您有权：{{ '\n' }}
          · 查看、修改您的个人信息{{ '\n' }}
          · 删除您的账号及相关数据{{ '\n' }}
          · 撤回授权同意{{ '\n' }}
          如需行使上述权利，请通过小程序内"设置"页面或联系我们。
        </text>
      </view>

      <view class="section">
        <text class="section-title">七、未成年人保护</text>
        <text class="section-content">
          本小程序不针对18周岁以下未成年人提供服务。如您是未成年人，请在监护人陪同下使用并获得监护人同意。
        </text>
      </view>

      <view class="section">
        <text class="section-title">八、政策更新</text>
        <text class="section-content">
          我们可能根据业务和法律要求更新本隐私政策。重大变更时，我们将通过小程序通知您。继续使用视为接受更新后的政策。
        </text>
      </view>

      <view class="section">
        <text class="section-title">九、联系我们</text>
        <text class="section-content">
          如对本隐私政策有任何疑问，请联系：{{ '\n' }}
          邮箱：privacy@lookforbest.com
        </text>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
// 隐私政策页面 - 无需额外逻辑
</script>

<style scoped>
.privacy-page {
  height: 100vh;
  background-color: #f5f5f5;
}

.container {
  padding: 32rpx;
}

.header {
  margin-bottom: 40rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #e0e0e0;
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.update-date {
  font-size: 24rpx;
  color: #999;
}

.section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.section-content {
  font-size: 26rpx;
  color: #555;
  line-height: 1.8;
}
</style>
