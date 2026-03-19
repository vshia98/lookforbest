<template>
  <div class="max-w-4xl mx-auto px-4 py-16">
    <!-- Hero -->
    <div class="text-center mb-16 relative">
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-80 h-80 bg-primary/5 rounded-full blur-[100px] pointer-events-none"></div>
      <div class="relative">
        <h1 class="text-4xl font-bold text-white mb-4">
          关于<span class="text-primary">寻优 / RobotLas</span>
        </h1>
        <p class="text-lg text-gray-400 max-w-2xl mx-auto leading-relaxed">
          寻优（RobotLas）是全球领先的机器人产品数据库与发现平台，致力于帮助用户高效检索、对比和选择最适合的机器人产品。
        </p>
      </div>
    </div>

    <!-- 我们做什么 -->
    <section class="mb-16">
      <h2 class="text-2xl font-bold text-white mb-6 flex items-center gap-2">
        <span class="text-primary">01</span> 我们做什么
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div class="card rounded-xl p-6 relative overflow-hidden group">
          <div class="absolute -top-8 -right-8 w-20 h-20 bg-primary/10 rounded-full blur-[40px] group-hover:bg-primary/20 transition-all"></div>
          <div class="relative">
            <div class="text-3xl mb-3">🔍</div>
            <h3 class="font-semibold text-white mb-2">产品发现</h3>
            <p class="text-sm text-gray-400 leading-relaxed">收录全球主流机器人产品，涵盖人形、四足、工业臂、轮式、仿生等各类别，支持多维度智能搜索。</p>
          </div>
        </div>
        <div class="card rounded-xl p-6 relative overflow-hidden group">
          <div class="absolute -top-8 -right-8 w-20 h-20 bg-accent-blue/10 rounded-full blur-[40px] group-hover:bg-accent-blue/20 transition-all"></div>
          <div class="relative">
            <div class="text-3xl mb-3">📊</div>
            <h3 class="font-semibold text-white mb-2">参数对比</h3>
            <p class="text-sm text-gray-400 leading-relaxed">提供详尽的技术规格参数，支持多款产品同屏对比，帮助您做出数据驱动的采购决策。</p>
          </div>
        </div>
        <div class="card rounded-xl p-6 relative overflow-hidden group">
          <div class="absolute -top-8 -right-8 w-20 h-20 bg-accent-gold/10 rounded-full blur-[40px] group-hover:bg-accent-gold/20 transition-all"></div>
          <div class="relative">
            <div class="text-3xl mb-3">🤝</div>
            <h3 class="font-semibold text-white mb-2">厂商对接</h3>
            <p class="text-sm text-gray-400 leading-relaxed">一键询价直达厂商，支持厂商入驻发布产品，构建高效的供需对接桥梁。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据规模 -->
    <section class="mb-16">
      <h2 class="text-2xl font-bold text-white mb-6 flex items-center gap-2">
        <span class="text-primary">02</span> 平台数据
      </h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="card rounded-xl p-5 text-center">
          <div class="text-3xl font-bold text-primary">300+</div>
          <div class="text-sm text-gray-500 mt-1">机器人产品</div>
        </div>
        <div class="card rounded-xl p-5 text-center">
          <div class="text-3xl font-bold text-primary">100+</div>
          <div class="text-sm text-gray-500 mt-1">收录厂商</div>
        </div>
        <div class="card rounded-xl p-5 text-center">
          <div class="text-3xl font-bold text-primary">9</div>
          <div class="text-sm text-gray-500 mt-1">产品分类</div>
        </div>
        <div class="card rounded-xl p-5 text-center">
          <div class="text-3xl font-bold text-primary">20+</div>
          <div class="text-sm text-gray-500 mt-1">覆盖国家</div>
        </div>
      </div>
    </section>

    <!-- 技术栈 -->
    <section class="mb-16">
      <h2 class="text-2xl font-bold text-white mb-6 flex items-center gap-2">
        <span class="text-primary">03</span> 技术架构
      </h2>
      <div class="card rounded-xl p-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
          <div>
            <div class="text-2xl mb-1">🖥️</div>
            <div class="text-sm font-medium text-white">前端</div>
            <div class="text-xs text-gray-500 mt-0.5">Vue 3 + Tailwind CSS</div>
          </div>
          <div>
            <div class="text-2xl mb-1">⚙️</div>
            <div class="text-sm font-medium text-white">后端</div>
            <div class="text-xs text-gray-500 mt-0.5">Spring Boot 3 + MySQL</div>
          </div>
          <div>
            <div class="text-2xl mb-1">🔍</div>
            <div class="text-sm font-medium text-white">搜索</div>
            <div class="text-xs text-gray-500 mt-0.5">Elasticsearch</div>
          </div>
          <div>
            <div class="text-2xl mb-1">🤖</div>
            <div class="text-sm font-medium text-white">数据采集</div>
            <div class="text-xs text-gray-500 mt-0.5">Python 智能爬虫</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 愿景 -->
    <section class="text-center">
      <div class="card rounded-2xl p-8 relative overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-accent-blue/5"></div>
        <div class="relative">
          <div class="text-4xl mb-4">🚀</div>
          <h2 class="text-xl font-bold text-white mb-3">我们的愿景</h2>
          <p class="text-gray-400 max-w-xl mx-auto leading-relaxed">
            成为机器人行业最值得信赖的产品发现平台，让每一位用户都能快速找到最适合自己需求的机器人产品，推动机器人技术在各行各业的落地应用。
          </p>
        </div>
      </div>
    </section>
  </div>
</template>
