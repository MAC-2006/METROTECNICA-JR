export interface Usuario {
  id: number;
  email: string;
  nomeCompleto: string | null;
  role: string;
  canSign: boolean;
  tenantId: number | null;
}

export interface UsuarioRequest {
  email: string;
  password: string;
  nomeCompleto: string | null;
  role: string;
  canSign: boolean;
}

export interface UsuarioUpdateRequest {
  nomeCompleto: string | null;
  role: string;
  canSign: boolean;
  password?: string | null;
}
