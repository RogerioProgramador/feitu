/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        feitu: {
          bg: '#F9F6F0',
          blue: '#A7C7E7',
          teal: '#B5EAD7',
          peach: '#FFDAC1',
          lavender: '#E2C6FF',
          text: '#3D3D3D',
        },
        night: {
          bg: '#1E1E2E',
          surface: '#2A2A3E',
          text: '#E2DDD6',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
