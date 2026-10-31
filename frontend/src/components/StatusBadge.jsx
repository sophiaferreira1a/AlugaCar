const labels = {
  PENDENTE: 'Pendente',
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  CANCELADO: 'Cancelado',
  CONTRATO_EXECUTADO: 'Contrato executado',
}

export default function StatusBadge({ status }) {
  return <span className={`badge status-${String(status).toLowerCase()}`}>{labels[status] || status}</span>
}

export { labels as statusLabels }
