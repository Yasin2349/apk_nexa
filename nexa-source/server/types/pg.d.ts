declare module 'pg' {
  export class Pool {
    constructor(config?: Record<string, unknown>)
    connect(...args: unknown[]): Promise<unknown>
    query(...args: unknown[]): Promise<unknown>
    end(...args: unknown[]): Promise<void>
  }
}
