export interface Tenant {
  id: number;
  name: string;
  razaoSocial: string | null;
  cnpj: string | null;
  logradouro: string | null;
  numero: string | null;
  bairro: string | null;
  cidade: string | null;
  estado: string | null;
  cep: string | null;
  telefone: string | null;
  slug: string;
  url: string | null;
  createdAt: string;
}

export interface TenantRequest {
  name: string;
  razaoSocial: string | null;
  cnpj: string | null;
  logradouro: string | null;
  numero: string | null;
  bairro: string | null;
  cidade: string | null;
  estado: string | null;
  cep: string | null;
  telefone: string | null;
  slug: string;
  url: string | null;
}

export interface ImpersonateResponse {
  accessToken: string;
  role: string;
}