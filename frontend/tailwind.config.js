/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
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
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
