export const categoryColors = ['#00a69c', '#3184ee', '#8e7be2', '#f2a45c', '#e76d8a', '#34b5cd', '#7b9b49', '#926a52']

export function categorySegments(data, view = 'quantity') {
  const total = Number(view === 'quantity' ? data?.totalQuantity : data?.totalValue) || 0
  let offset = 0
  return (data?.categories || []).map((row, index) => {
    const amount = Number(row[view]) || 0
    const percent = total > 0 ? amount / total * 100 : 0
    const segment = { ...row, amount, percent, offset, color: categoryColors[index % categoryColors.length] }
    offset += percent
    return segment
  })
}
