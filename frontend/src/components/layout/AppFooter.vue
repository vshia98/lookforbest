<template>
  <footer class="bg-dark-200 border-t border-white/[0.04] mt-16">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <div class="text-white font-bold text-xl mb-3"><span class="text-primary">⚡</span> 寻优</div>
          <p class="text-sm text-gray-500">专业机器人产品数据库，发现并对比全球顶尖工业与服务机器人。</p>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">产品</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/robots" class="text-gray-500 hover:text-primary transition-colors">机器人库</router-link></li>
            <li><router-link to="/compare" class="text-gray-500 hover:text-primary transition-colors">产品对比</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">账户</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/login" class="text-gray-500 hover:text-primary transition-colors">登录</router-link></li>
            <li><router-link to="/register" class="text-gray-500 hover:text-primary transition-colors">注册</router-link></li>
            <li><router-link to="/user/favorites" class="text-gray-500 hover:text-primary transition-colors">我的收藏</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">关于</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/about" class="text-gray-500 hover:text-primary transition-colors">关于我们</router-link></li>
            <li><router-link to="/contact" class="text-gray-500 hover:text-primary transition-colors">联系我们</router-link></li>
          </ul>
        </div>
      </div>
      <div class="border-t border-white/[0.04] mt-8 pt-6 text-sm text-gray-600 text-center">
        © 2026 寻优. All rights reserved.
      </div>
    </div>
  </footer>
</template>
