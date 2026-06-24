/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        feitu: {
          bg: '#E8E4DC',
          surface: '#F7F4EE',
          blue: '#A7C7E7',
          'blue-deep': '#5E8BB6',
          teal: '#B5EAD7',
          'teal-deep': '#2F7D63',
          peach: '#FFDAC1',
          'peach-deep': '#E07B4F',
          lavender: '#E2C6FF',
          'lavender-deep': '#8A5FC0',
          text: '#36332E',
        },
        night: {
          bg: '#16161F',
          surface: '#222230',
          card: '#262635',
          text: '#ECE6DC',
        },
      },
      fontFamily: {
        sans: ['"Space Grotesk"', 'system-ui', 'sans-serif'],
        display: ['"Instrument Serif"', 'Georgia', 'serif'],
      },
      keyframes: {
        'feitu-pop': {
          '0%': { transform: 'scale(.55)' },
          '60%': { transform: 'scale(1.14)' },
          '100%': { transform: 'scale(1)' },
        },
        'feitu-fade': {
          from: { opacity: '0', transform: 'translateY(-4px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'feitu-sheet': {
          from: { transform: 'translateY(100%)' },
          to: { transform: 'translateY(0)' },
        },
        'feitu-dim': {
          from: { opacity: '0' },
          to: { opacity: '1' },
        },
        'feitu-spring': {
          '0%': { transform: 'scale(.3)', opacity: '0' },
          '55%': { transform: 'scale(1.12)', opacity: '1' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },
      animation: {
        'feitu-pop': 'feitu-pop .26s ease',
        'feitu-fade': 'feitu-fade .2s ease',
        'feitu-sheet': 'feitu-sheet .34s cubic-bezier(.22,1,.36,1)',
        'feitu-dim': 'feitu-dim .25s ease',
        'feitu-spring': 'feitu-spring .5s cubic-bezier(.22,1,.36,1)',
      },
    },
  },
  plugins: [],
}
