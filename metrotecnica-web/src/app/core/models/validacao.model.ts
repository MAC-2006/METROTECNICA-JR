export interface ValidacaoCertificado {
  valido: boolean;
  identificacao: string | null;
  descricao: string | null;
  marca: string | null;
  modelo: string | null;
  tenantNome: string | null;
  dataCertificacao: string | null;
  proxCalibFormatada: string | null;
  statusGeral: string | null;
  metrologistaNome: string | null;
  responsavelNome: string | null;
  documentHashParcial: string | null;
}