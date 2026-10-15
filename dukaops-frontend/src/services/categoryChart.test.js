import test from 'node:test'
import assert from 'node:assert/strict'
import { categorySegments } from './categoryChart.js'

test('empty and zero inventories do not invent chart segments', () => {
  assert.deepEqual(categorySegments(undefined), [])
  const rows = categorySegments({ totalQuantity: 0, categories: [{ name: 'Zero', quantity: 0 }] })
  assert.equal(rows[0].percent, 0)
})

test('quantity chart uses database totals and preserves Uncategorized', () => {
  const rows = categorySegments({ totalQuantity: '10.000', categories: [
    { categoryId: 'one', name: 'Drinks', quantity: '7.500', value: '25.00' },
    { categoryId: null, name: 'Uncategorized', quantity: '2.500', value: '75.00' },
  ] })
  assert.equal(rows[0].percent, 75)
  assert.equal(rows[1].percent, 25)
  assert.equal(rows[1].offset, 75)
  assert.equal(rows[1].name, 'Uncategorized')
})

test('value chart uses cost value rather than quantity', () => {
  const rows = categorySegments({ totalValue: '100.00', categories: [
    { name: 'A', quantity: '90', value: '20.00' }, { name: 'B', quantity: '10', value: '80.00' },
  ] }, 'value')
  assert.equal(rows[0].percent, 20)
  assert.equal(rows[1].percent, 80)
})
