/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0066FF',
          50: '#EBF2FF',
          100: '#CCE0FF',
          500: '#0066FF',
          600: '#0052CC',
          700: '#003D99'
        }
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans SC', 'sans-serif']
      }
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography')
  ]
}
