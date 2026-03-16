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
          DEFAULT: '#00FFD1',
          50: '#0a2e28',
          100: '#0d3d35',
          200: '#115c4f',
          300: '#0ee9c2',
          400: '#77F8D3',
          500: '#00FFD1',
          600: '#00cca7',
          700: '#00997d',
          800: '#C0FF81',
          900: '#293739',
        },
        dark: {
          DEFAULT: '#1F1F23',
          50: '#2C2D36',
          100: '#202228',
          200: '#181A1E',
          300: '#25262E',
          400: '#33343D',
        },
        accent: {
          orange: '#FF6000',
          gold: '#F9DF95',
          pink: '#FF97AF',
          red: '#F37370',
          lime: '#C0FF81',
          blue: '#65A9F3',
        }
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans SC', 'sans-serif']
      },
      boxShadow: {
        'glow': '0 0 30px rgba(0, 255, 209, 0.08)',
        'glow-md': '0 0 40px rgba(0, 255, 209, 0.15)',
        'glow-lg': '0 0 60px rgba(0, 255, 209, 0.2)',
        'card': '0 2px 10px rgba(0, 0, 0, 0.08)',
        'card-hover': '0 16px 50px rgba(0, 0, 0, 0.2)',
      },
      backgroundImage: {
        'gradient-primary': 'linear-gradient(to right, #C0FF81, #00FFD1)',
        'gradient-hover': 'linear-gradient(to right, #77F8D3, #65A9F3)',
      }
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography')
  ]
}
