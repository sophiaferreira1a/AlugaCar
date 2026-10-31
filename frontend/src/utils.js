export function formatarData(data) {
  if (!data) return '—'
  const [ano, mes, dia] = String(data).slice(0, 10).split('-')
  if (!ano || !mes || !dia) return data
  return `${dia}/${mes}/${ano}`
}

export function formatarDataHora(data) {
  if (!data) return '—'
  const valor = new Date(data)
  if (Number.isNaN(valor.getTime())) return data
  return valor.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

export function formatarMoeda(valor) {
  if (valor === null || valor === undefined || valor === '') return '—'
  return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

export function hojeISO() {
  return new Date().toISOString().slice(0, 10)
}

export function adicionarDiasISO(dias) {
  const data = new Date()
  data.setDate(data.getDate() + dias)
  return data.toISOString().slice(0, 10)
}
