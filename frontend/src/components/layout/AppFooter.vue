<template>
  <footer class="bg-gray-900 text-gray-300 mt-16">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <div class="text-white font-bold text-xl mb-3">⚡ LookForBest</div>
          <p class="text-sm text-gray-400">专业机器人产品数据库，发现并对比全球顶尖工业与服务机器人。</p>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">产品</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/robots" class="hover:text-white transition-colors">机器人库</router-link></li>
            <li><router-link to="/compare" class="hover:text-white transition-colors">产品对比</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">账户</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/login" class="hover:text-white transition-colors">登录</router-link></li>
            <li><router-link to="/register" class="hover:text-white transition-colors">注册</router-link></li>
            <li><router-link to="/user/favorites" class="hover:text-white transition-colors">我的收藏</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">关于</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="hover:text-white transition-colors">关于我们</a></li>
            <li><a href="#" class="hover:text-white transition-colors">联系我们</a></li>
          </ul>
        </div>
      </div>
      <div class="border-t border-gray-800 mt-8 pt-6 text-sm text-gray-500 text-center">
        © 2026 LookForBest. All rights reserved.
      </div>
    </div>
  </footer>
</template>
