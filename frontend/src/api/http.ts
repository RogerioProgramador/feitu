import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('feitu_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => res,
  (error) => {
    // Não redireciona em rotas de autenticação (ex: login inválido deve mostrar erro local)
    const isAuthRoute = error.config?.url?.startsWith('/auth/')
    if (error.response?.status === 401 && !isAuthRoute) {
      localStorage.removeItem('feitu_token')
      localStorage.removeItem('feitu_email')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default http
