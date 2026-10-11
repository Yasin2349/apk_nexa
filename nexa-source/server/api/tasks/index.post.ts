import {z} from 'zod'
const Schema=z.object({title:z.string().trim().min(1).max(200),priority:z.enum(['low','medium','high','critical']).default('medium'),estimatedMinutes:z.number().int().positive().max(1440).default(30)})
export default defineEventHandler(async event=>{
 const body=Schema.parse(await readBody(event))
 return {id:crypto.randomUUID(),...body,status:'todo',actualMinutes:0,createdAt:new Date().toISOString()}
})
