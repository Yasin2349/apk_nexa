import { readBody } from 'h3'
import { readStore, resetStore, writeStore } from '../utils/store'

export default defineEventHandler(async (event) => {
  switch (event.method.toUpperCase()) {
    case 'GET':
      return await readStore()
    case 'PUT': {
      const body = await readBody(event)
      if (!body || typeof body !== 'object') throw createError({ statusCode: 400, statusMessage: 'Invalid data' })
      return await writeStore(body)
    }
    case 'DELETE':
      return await resetStore()
    default:
      throw createError({ statusCode: 405, statusMessage: 'Method not allowed' })
  }
})
