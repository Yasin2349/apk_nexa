import {z} from 'zod'
const Schema=z.object({id:z.string(),startedAt:z.string(),focusRating:z.number().int().min(1).max(5).optional(),difficulty:z.number().int().min(1).max(5).optional(),interruptions:z.number().int().min(0).default(0),distractions:z.array(z.string()).default([]),note:z.string().max(1000).optional()})
export default defineEventHandler(async event=>{const b=Schema.parse(await readBody(event));const start=new Date(b.startedAt).getTime();const actual=Math.max(0,Math.round((Date.now()-start)/60000));return {...b,actualMinutes:actual,endedAt:new Date().toISOString()}})
