import zh from './zh'
import en from './en'

type Locale = 'zh' | 'en'

const messages = { zh, en }

let currentLocale: Locale = (uni.getStorageSync('locale') as Locale) || 'zh'

export function t(key: keyof typeof zh): string {
  return messages[currentLocale][key] ?? messages['zh'][key] ?? key
}

export function setLocale(locale: Locale) {
  currentLocale = locale
  uni.setStorageSync('locale', locale)
}

export function getLocale(): Locale {
  return currentLocale
}
