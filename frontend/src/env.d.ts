/// <reference types="vite/client" />

import 'vue'

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

declare global {
  const IS_DEV: boolean
  interface Window {
    __POWERED_BY_QIANKUN__?: boolean
  }

  namespace JSX {
    interface IntrinsicElements {
      'model-viewer': React.DetailedHTMLProps<React.HTMLAttributes<HTMLElement> & {
        src?: string
        poster?: string
        'ios-src'?: string
        alt?: string
        'camera-controls'?: boolean
        'auto-rotate'?: boolean
        ar?: boolean
        'ar-modes'?: string
        'shadow-intensity'?: string
        loading?: string
        reveal?: string
        style?: Record<string, string>
        [key: string]: unknown
      }, HTMLElement>
    }
  }
}

export {}
