import {drizzle} from 'drizzle-orm/node-postgres'
import {Pool} from 'pg'
let pool: Pool|undefined
export const getDb=()=>{
 const url=useRuntimeConfig().databaseUrl
 if(!url) return null
 pool ||= new Pool({connectionString:url,max:5})
 return drizzle({client:pool})
}
