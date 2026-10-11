export default defineEventHandler(()=>({ok:true,service:'productivity-os',mode:useRuntimeConfig().databaseUrl?'postgresql':'demo'}))
