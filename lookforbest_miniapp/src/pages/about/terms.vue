<template>
  <scroll-view scroll-y class="terms-page">
    <view class="container">
      <view class="header">
        <text class="title">用户协议</text>
        <text class="update-date">更新日期：2025年1月1日</text>
      </view>

      <view class="section">
        <text class="section-title">一、接受条款</text>
        <text class="section-content">
          欢迎使用 LookForBest 小程序（以下简称"本小程序"）。您访问或使用本小程序，即表示您同意受本用户协议约束。如您不同意本协议，请立即停止使用。
        </text>
      </view>

      <view class="section">
        <text class="section-title">二、服务描述</text>
        <text class="section-content">
          本小程序提供全球机器人信息搜索、详情展示、多维度对比及个人收藏等功能，旨在帮助用户了解和选择适合的机器人产品。
        </text>
      </view>

      <view class="section">
        <text class="section-title">三、账号注册与使用</text>
        <text class="section-content">
          1. 您须提供真实、准确的注册信息，并对账号安全负责。{{ '\n' }}
          2. 严禁将账号转让、出售或授权他人使用。{{ '\n' }}
          3. 发现账号被盗用，应立即联系我们。{{ '\n' }}
          4. 您对账号下的所有行为承担法律责任。
        </text>
      </view>

      <view class="section">
        <text class="section-title">四、禁止行为</text>
        <text class="section-content">
          使用本小程序时，您不得：{{ '\n' }}
          · 发布违法、虚假、诽谤或侵权内容{{ '\n' }}
          · 尝试破解、反编译或干扰本小程序{{ '\n' }}
          · 使用爬虫或自动化工具批量抓取数据{{ '\n' }}
          · 冒充他人或伪造信息{{ '\n' }}
          · 从事任何违反法律法规的活动
        </text>
      </view>

      <view class="section">
        <text class="section-title">五、知识产权</text>
        <text class="section-content">
          本小程序的所有内容（包括但不限于文字、图片、数据、代码、界面设计）均受知识产权保护。未经书面授权，不得复制、传播或商业使用。
        </text>
      </view>

      <view class="section">
        <text class="section-title">六、内容免责声明</text>
        <text class="section-content">
          本小程序展示的机器人信息来源于公开资料，仅供参考。我们尽力保证信息准确，但不对信息的完整性、时效性作出担保，不承担因使用相关信息而产生的损失。
        </text>
      </view>

      <view class="section">
        <text class="section-title">七、服务变更与终止</text>
        <text class="section-content">
          我们有权在不事先通知的情况下修改、暂停或终止服务，或修改本协议条款。重大变更将通过小程序通知用户。
        </text>
      </view>

      <view class="section">
        <text class="section-title">八、责任限制</text>
        <text class="section-content">
          在法律允许的最大范围内，本小程序对因使用或无法使用服务所造成的任何直接、间接、附带或惩罚性损害不承担责任。
        </text>
      </view>

      <view class="section">
        <text class="section-title">九、争议解决</text>
        <text class="section-content">
          本协议的签订、效力、解释、执行及争议解决均适用中华人民共和国法律。争议应协商解决，协商不成的，提交本小程序运营主体所在地有管辖权的法院诉讼解决。
        </text>
      </view>

      <view class="section">
        <text class="section-title">十、联系方式</text>
        <text class="section-content">
          如对本协议有疑问，请联系：{{ '\n' }}
          邮箱：support@lookforbest.com
        </text>
      </view>

      <view class="bottom-actions">
        <button class="agree-btn" @tap="goBack">我已阅读并同意</button>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
function goBack() {
  uni.navigateBack()
}
</script>

<style scoped>
.terms-page {
  height: 100vh;
  background-color: #f5f5f5;
}

.container {
  padding: 32rpx;
  padding-bottom: 160rpx;
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

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background: #fff;
  border-top: 1rpx solid #e0e0e0;
}

.agree-btn {
  background-color: #6366F1;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
  padding: 20rpx 0;
}
</style>
