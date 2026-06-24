import http from './http'

export interface UsuarioProfile {
  id: string
  email: string
  horarioNotificacao: string | null
}

export const usuarioApi = {
  async me(): Promise<UsuarioProfile> {
    const res = await http.get<UsuarioProfile>('/usuarios/me')
    return res.data
  },
}
