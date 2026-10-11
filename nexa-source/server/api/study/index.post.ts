import {z} from 'zod'
const Schema=z.object({courseId:z.string(),topicId:z.string().optional(),duration:z.number().int().positive().max(1440),focusRating:z.number().int().min(1).max(5).optional(),difficulty:z.number().int().min(1).max(5).optional(),notes:z.string().max(2000).optional()})
export default defineEventHandler(async event=>({id:crypto.randomUUID(),...Schema.parse(await readBody(event)),createdAt:new Date().toISOString()}))
