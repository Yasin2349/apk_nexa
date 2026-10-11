import { useProductivityStore } from '~/stores/productivity'
const KEY='nexa-productivity-v2'
export default defineNuxtPlugin((nuxtApp)=>{const store=useProductivityStore(nuxtApp.$pinia as any);try{const raw=localStorage.getItem(KEY);store.hydrate(raw?JSON.parse(raw):{})}catch{store.hydrate({})}store.$subscribe(()=>{try{localStorage.setItem(KEY,JSON.stringify(store.$state))}catch{}} ,{detached:true})})
