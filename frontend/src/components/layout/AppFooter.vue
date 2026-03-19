<template>
  <footer class="bg-dark-200 border-t border-white/[0.04] mt-16">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <div class="text-white font-bold text-xl mb-3">
            <span class="text-primary">⚡</span>
            <span>{{ t('nav.homeSiteName') || '寻优' }}</span>
          </div>
          <p class="text-sm text-gray-500">{{ t('footer.slogan') }}</p>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">{{ t('footer.product') }}</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/robots" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.robots') }}</router-link></li>
            <li><router-link to="/compare" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.compare') }}</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">{{ t('footer.account') }}</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/login" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.login') }}</router-link></li>
            <li><router-link to="/register" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.register') }}</router-link></li>
            <li><router-link to="/user/favorites" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.favorites') }}</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-semibold mb-3 text-sm">{{ t('footer.about') }}</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/about" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.aboutUs') }}</router-link></li>
            <li><router-link to="/contact" class="text-gray-500 hover:text-primary transition-colors">{{ t('footer.contactUs') }}</router-link></li>
          </ul>
        </div>
      </div>
      <div class="border-t border-white/[0.04] mt-8 pt-6 text-sm text-gray-600 text-center">
        {{ t('footer.copyright') }}
      </div>
    </div>
  </footer>
 </template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>
