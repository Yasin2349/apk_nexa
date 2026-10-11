import {z} from 'zod'
const Schema=z.object({plannedMinutes:z.number().int().min(1).max(720),mode:z.string().default('focus'),taskId:z.string().optional()})
export default defineEventHandler(async event=>{const body=Schema.parse(await readBody(event));return {id:crypto.randomUUID(),...body,startedAt:new Date().toISOString(),interruptions:0}})
