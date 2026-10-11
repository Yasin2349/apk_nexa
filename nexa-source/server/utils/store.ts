import { promises as fs } from 'node:fs'
import { resolve } from 'node:path'

export type NexaStore = {
  version: number
  tasks: unknown[]
  goals: unknown[]
  projects: unknown[]
  courses: unknown[]
  schedule: unknown[]
  focusSessions: unknown[]
  studyMinutes: number
  dailyReports: unknown[]
  notifications: unknown[]
  settings: {
    lang: string
    timezone: string
    goal: number
    workStart: string
    workEnd: string
    notificationsEnabled: boolean
    quietEnabled: boolean
    quietStart: string
    quietEnd: string
  }
}

export const createEmptyStore = (): NexaStore => ({
  version: 1,
  tasks: [],
  goals: [],
  projects: [],
  courses: [],
  schedule: [],
  focusSessions: [],
  studyMinutes: 0,
  dailyReports: [],
  notifications: [],
  settings: {
    lang: 'fa',
    timezone: '',
    goal: 0,
    workStart: '',
    workEnd: '',
    notificationsEnabled: true,
    quietEnabled: false,
    quietStart: '',
    quietEnd: ''
  }
})

const dataDir = () => process.env.NEXA_DATA_DIR ? resolve(process.env.NEXA_DATA_DIR) : resolve(process.cwd(), 'data')
const dataFile = () => resolve(dataDir(), 'nexa.json')
let writeQueue: Promise<void> = Promise.resolve()

const normalize = (value: unknown): NexaStore => {
  const empty = createEmptyStore()
  if (!value || typeof value !== 'object') return empty
  const input = value as Partial<NexaStore>
  return {
    ...empty,
    ...input,
    version: 1,
    tasks: Array.isArray(input.tasks) ? input.tasks : [],
    goals: Array.isArray(input.goals) ? input.goals : [],
    projects: Array.isArray(input.projects) ? input.projects : [],
    courses: Array.isArray(input.courses) ? input.courses : [],
    schedule: Array.isArray(input.schedule) ? input.schedule : [],
    focusSessions: Array.isArray(input.focusSessions) ? input.focusSessions : [],
    dailyReports: Array.isArray(input.dailyReports) ? input.dailyReports : [],
    notifications: Array.isArray(input.notifications) ? input.notifications : [],
    studyMinutes: Number(input.studyMinutes) >= 0 ? Number(input.studyMinutes) : 0,
    settings: { ...empty.settings, ...(input.settings || {}) }
  }
}

export const readStore = async (): Promise<NexaStore> => {
  await fs.mkdir(dataDir(), { recursive: true })
  try {
    return normalize(JSON.parse(await fs.readFile(dataFile(), 'utf8')))
  } catch {
    const empty = createEmptyStore()
    await writeStore(empty)
    return empty
  }
}

export const writeStore = async (value: unknown): Promise<NexaStore> => {
  const next = normalize(value)
  writeQueue = writeQueue.then(async () => {
    await fs.mkdir(dataDir(), { recursive: true })
    await fs.writeFile(dataFile(), JSON.stringify(next, null, 2), 'utf8')
  })
  await writeQueue
  return next
}

export const resetStore = async () => writeStore(createEmptyStore())
