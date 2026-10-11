import {describe,it,expect} from 'vitest'
const completion=(done:number,total:number)=>total?Math.round(done/total*100):0
const accuracy=(planned:number,actual:number)=>actual?Math.max(0,Math.round((1-Math.abs(actual-planned)/actual)*100)):0
describe('analytics calculations',()=>{
 it('calculates completion',()=>expect(completion(8,10)).toBe(80))
 it('calculates planning accuracy',()=>expect(accuracy(100,120)).toBe(83))
 it('handles empty completion',()=>expect(completion(0,0)).toBe(0))
})
