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

export interface PontoCalibracao {
  id?: number;
  pontoNominal: string;
  vIndicado1: number;
  vIndicado2: number;
}

export interface HistoricoRetroativo {
  id?: number;
  certificado: string;
  dataCertificacao: string | null;
}

export interface InstrumentoDetalhe {
  id: number;
  numeroSequencial: string | null;
  identificacao: string;
  descricao: string;
  marca: string | null;
  modelo: string | null;
  capacidade: string | null;
  precisao: string | null;
  setorId: number | null;
  localId: number | null;
  frequenciaMeses: number;
  dataCertificacao: string | null;
  certificado: string | null;
  dataProximaCalibracao: string | null;
  proxCalibFormatada: string | null;
  lpMin: number | null;
  lpMax: number | null;
  aferidoLp: number | null;
  situacaoLp: string | null;
  lnpMin: number | null;
  lnpMax: number | null;
  aferidoLnp: number | null;
  situacaoLnp: string | null;
  erro: number | null;
  incertezaMedicao: number | null;
  criterioAceitacao: number | null;
  somaQuadratica: number | null;
  resultadoMafra: string | null;
  statusGeral: string | null;
  observacoes: string | null;
  documentHash: string | null;
  metrologistaNome: string | null;
  assinaturaData: string | null;
  pontos: PontoCalibracao[];
  historicos: { id: number; data: string; certificado: string; status: string; temPdfFisico: boolean }[];
}

export interface InstrumentoRequest {
  numeroSequencial: string | null;
  identificacao: string;
  descricao: string;
  marca: string | null;
  modelo: string | null;
  capacidade: string | null;
  precisao: string | null;
  setorId: number | null;
  localId: number | null;
  frequenciaMeses: number;
  dataCertificacao: string | null;
  certificado: string | null;
  statusGeral: string | null;
  observacoes: string | null;
  lpMin: number | null;
  lpMax: number | null;
  aferidoLp: number | null;
  lnpMin: number | null;
  lnpMax: number | null;
  aferidoLnp: number | null;
  erro: number | null;
  incertezaMedicao: number | null;
  criterioAceitacao: number | null;
  pontos: PontoCalibracao[];
  historicosRetroativos: HistoricoRetroativo[];
}