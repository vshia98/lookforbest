import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'

export type MessageSchema = typeof zh

const savedLocale = localStorage.getItem('locale') || 'zh'

export const i18n = createI18n<[MessageSchema], 'zh' | 'en'>({
  legacy: false,
  locale: savedLocale as 'zh' | 'en',
  fallbackLocale: 'zh',
  messages: {
    zh,
    en,
  },
})

export function setLocale(locale: 'zh' | 'en') {
  ;(i18n.global.locale as any).value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.lang = locale
}

export function getLocale(): string {
  return (i18n.global.locale as any).value
}
