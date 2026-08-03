export interface StatsResponse {
  vencidos: number;
  alerta: number;
  total: number;
}

export interface InstrumentoListItem {
  id: number;
  numeroSequencial: string;
  identificacao: string;
  descricao: string;
  dataCertificacao: string | null;
  proxCalibFormatada: string;
  statusGeral: string;
  assinado: boolean;
  temPdfFisico: boolean;
}

export interface InstrumentoPageResponse {
  instrumentos: InstrumentoListItem[];
  totalPaginas: number;
  paginaAtual: number;
}