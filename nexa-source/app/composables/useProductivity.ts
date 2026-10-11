import { computed, onMounted } from 'vue'
import { useState } from '#app'
import type { Task, Course, Goal, Project, ScheduleBlock, FocusSession, DailyReport, NotificationItem, NexaSettings, Priority } from '~/types/domain'

const iso = (date = new Date()) => date.toISOString()
const emptySettings = (): NexaSettings => ({
  lang: 'fa', timezone: '', goal: 0, workStart: '', workEnd: '',
  notificationsEnabled: true, quietEnabled: false, quietStart: '', quietEnd: ''
})

let initialized = false
let hydrated = false
let loadPromise: Promise<void> | null = null
let savePromise: Promise<unknown> | null = null

export const useProductivity = () => {
  const tasks = useState<Task[]>('tasks', () => [])
  const goals = useState<Goal[]>('goals', () => [])
  const projects = useState<Project[]>('projects', () => [])
  const courses = useState<Course[]>('courses', () => [])
  const schedule = useState<ScheduleBlock[]>('schedule', () => [])
  const focusSessions = useState<FocusSession[]>('focus-sessions', () => [])
  const studyMinutes = useState<number>('study-minutes', () => 0)
  const dailyReports = useState<DailyReport[]>('daily-reports', () => [])
  const notifications = useState<NotificationItem[]>('notifications', () => [])
  const settings = useState<NexaSettings>('settings', emptySettings)
  const ready = useState<boolean>('nexa-data-ready', () => false)

  const snapshot = () => ({
    version: 1,
    tasks: tasks.value,
    goals: goals.value,
    projects: projects.value,
    courses: courses.value,
    schedule: schedule.value,
    focusSessions: focusSessions.value,
    studyMinutes: studyMinutes.value,
    dailyReports: dailyReports.value,
    notifications: notifications.value,
    settings: settings.value
  })

  const load = async () => {
    if (hydrated) return
    if (!loadPromise) {
      loadPromise = (async () => {
        try {
          const data = await $fetch<Partial<ReturnType<typeof snapshot>>>('/api/data')
          tasks.value = Array.isArray(data.tasks) ? data.tasks : []
          goals.value = Array.isArray(data.goals) ? data.goals : []
          projects.value = Array.isArray(data.projects) ? data.projects : []
          courses.value = Array.isArray(data.courses) ? data.courses : []
          schedule.value = Array.isArray(data.schedule) ? data.schedule : []
          focusSessions.value = Array.isArray(data.focusSessions) ? data.focusSessions : []
          studyMinutes.value = Number(data.studyMinutes) || 0
          dailyReports.value = Array.isArray(data.dailyReports) ? data.dailyReports : []
          notifications.value = Array.isArray(data.notifications) ? data.notifications : []
          settings.value = { ...emptySettings(), ...(data.settings || {}) }
        } catch (error) {
          console.error('Nexa data load failed', error)
        } finally {
          hydrated = true
          ready.value = true
        }
      })()
    }
    await loadPromise
  }

  const persist = async () => {
    if (!import.meta.client || !hydrated) return
    const body = snapshot()
    // Serialize writes so rapid clicks cannot overwrite each other out of order.
    savePromise = (savePromise || Promise.resolve()).then(() => $fetch('/api/data', { method: 'PUT', body }))
    try { await savePromise } catch (error) { console.error('Nexa data save failed', error) }
  }

  if (import.meta.client && !initialized) {
    initialized = true
    onMounted(() => { void load() })
  }

  const completed = computed(() => tasks.value.filter(t => t.status === 'completed').length)
  const completionRate = computed(() => tasks.value.length ? Math.round((completed.value / tasks.value.length) * 100) : 0)
  const totalEstimated = computed(() => tasks.value.reduce((n, t) => n + t.estimatedMinutes, 0))
  const totalActual = computed(() => tasks.value.reduce((n, t) => n + t.actualMinutes, 0))
  const todayStudyMinutes = computed(() => {
    const today = new Date().toISOString().slice(0, 10)
    return focusSessions.value.reduce((sum, session) => session.startedAt.slice(0, 10) === today ? sum + Math.max(0, Number(session.actualMinutes) || 0) : sum, 0)
  })
  const weeklyStudy = computed(() => {
    const result = Array(7).fill(0) as number[]
    const now = new Date()
    const start = new Date(now)
    start.setHours(0, 0, 0, 0)
    start.setDate(start.getDate() - 6)
    for (const session of focusSessions.value) {
      const d = new Date(session.startedAt)
      if (d < start || d > now) continue
      const dayIndex = (d.getDay() + 1) % 7
      result[dayIndex] = (result[dayIndex] ?? 0) + Math.max(0, Number(session.actualMinutes) || 0)
    }
    return result
  })

  const finishTask = (id: string) => {
    const task = tasks.value.find(x => x.id === id)
    if (!task) return
    task.status = 'completed'
    task.completedAt = iso()
    if (!task.actualMinutes) task.actualMinutes = task.estimatedMinutes
    void persist()
  }
  const reopenTask = (id: string) => {
    const task = tasks.value.find(x => x.id === id)
    if (!task) return
    task.status = 'todo'
    task.completedAt = undefined
    void persist()
  }
  const deleteTask = (id: string) => { tasks.value = tasks.value.filter(x => x.id !== id); void persist() }
  const addTask = (input: Pick<Task, 'title' | 'priority' | 'estimatedMinutes' | 'category'>) => {
    const title = input.title.trim()
    if (!title) return
    tasks.value.unshift({ id: crypto.randomUUID(), title, status: 'todo', priority: input.priority, category: input.category, estimatedMinutes: Math.max(1, input.estimatedMinutes), actualMinutes: 0, tags: [], createdAt: iso() })
    void persist()
  }

  const addGoal = (input: { title: string; description?: string; priority: Priority; progress?: number; deadline?: string }) => {
    const title = input.title.trim()
    if (!title) return
    goals.value.unshift({ id: crypto.randomUUID(), title, description: input.description?.trim() ?? '', priority: input.priority, progress: Math.min(100, Math.max(0, Number(input.progress) || 0)), deadline: input.deadline || undefined })
    void persist()
  }
  const deleteGoal = (id: string) => { goals.value = goals.value.filter(x => x.id !== id); void persist() }

  const addProject = (input: { title: string; description?: string; priority: Priority }) => {
    const title = input.title.trim()
    if (!title) return
    projects.value.unshift({ id: crypto.randomUUID(), title, description: input.description?.trim() ?? '', status: 'active', priority: input.priority, progress: 0 })
    void persist()
  }
  const deleteProject = (id: string) => { projects.value = projects.value.filter(x => x.id !== id); void persist() }

  const addCourse = (input: { title: string; description?: string; topics: string[] }) => {
    const title = input.title.trim()
    if (!title) return ''
    const courseId = crypto.randomUUID()
    const topics = input.topics.map(x => x.trim()).filter(Boolean).map(title => ({ id: crypto.randomUUID(), title, progress: 0 }))
    courses.value.unshift({ id: courseId, title, description: input.description?.trim() || '', progress: 0, estimatedHours: 0, actualMinutes: 0, topics })
    void persist()
    return courseId
  }
  const updateCourseTopic = (courseId: string, topicId: string) => {
    const course = courses.value.find(c => c.id === courseId)
    const topic = course?.topics.find(t => t.id === topicId)
    if (!course || !topic) return
    topic.progress = Math.min(100, topic.progress + 5)
    course.progress = course.topics.length ? Math.round(course.topics.reduce((n, t) => n + t.progress, 0) / course.topics.length) : 0
    void persist()
  }
  const deleteCourse = (id: string) => { courses.value = courses.value.filter(x => x.id !== id); void persist() }

  const addScheduleBlock = (input: Pick<ScheduleBlock, 'title' | 'start' | 'end' | 'kind' | 'date'>) => {
    const title = input.title.trim()
    if (!title || input.end <= input.start) return false
    schedule.value.push({ id: crypto.randomUUID(), ...input, date: input.date || iso().slice(0, 10) })
    schedule.value.sort((a, b) => `${a.date || ''}${a.start}`.localeCompare(`${b.date || ''}${b.start}`))
    void persist()
    return true
  }
  const removeScheduleBlock = (id: string) => { schedule.value = schedule.value.filter(x => x.id !== id); void persist() }

  const saveFocusSession = (session: FocusSession) => {
    if (session.actualMinutes <= 0) return
    focusSessions.value.unshift(session)
    studyMinutes.value += Math.max(0, session.actualMinutes)
    void persist()
  }
  const deleteFocusSession = (id: string) => {
    const session = focusSessions.value.find(x => x.id === id)
    if (!session) return
    studyMinutes.value = Math.max(0, studyMinutes.value - Math.max(0, session.actualMinutes))
    focusSessions.value = focusSessions.value.filter(x => x.id !== id)
    void persist()
  }

  const saveReport = (report: DailyReport) => {
    const index = dailyReports.value.findIndex(x => x.id === report.id)
    if (index >= 0) dailyReports.value[index] = report
    else dailyReports.value.unshift(report)
    void persist()
  }
  const deleteReport = (id: string) => { dailyReports.value = dailyReports.value.filter(x => x.id !== id); void persist() }
  const deleteNotification = (id: string) => { notifications.value = notifications.value.filter(x => x.id !== id); void persist() }
  const markNotificationRead = (id: string) => { const n = notifications.value.find(x => x.id === id); if (n) { n.read = true; void persist() } }
  const addNotification = (input: Pick<NotificationItem, 'title' | 'body'> & Partial<Pick<NotificationItem, 'time'>>) => {
    const title = input.title.trim()
    const body = input.body.trim()
    if (!title || !body) return
    notifications.value.unshift({ id: crypto.randomUUID(), title, body, time: input.time || iso(), read: false, createdAt: iso() })
    void persist()
  }

  const saveSettings = () => { void persist() }

  const resetAll = async () => {
    await $fetch('/api/data', { method: 'DELETE' })
    tasks.value = []
    goals.value = []
    projects.value = []
    courses.value = []
    schedule.value = []
    focusSessions.value = []
    studyMinutes.value = 0
    dailyReports.value = []
    notifications.value = []
    settings.value = emptySettings()
    hydrated = true
    ready.value = true
  }

  return {
    tasks, goals, projects, courses, schedule, focusSessions, studyMinutes, dailyReports, notifications, settings, ready,
    completed, completionRate, totalEstimated, totalActual, todayStudyMinutes, weeklyStudy,
    finishTask, reopenTask, deleteTask, addTask, addGoal, deleteGoal, addProject, deleteProject,
    addCourse, updateCourseTopic, deleteCourse, addScheduleBlock, removeScheduleBlock,
    saveFocusSession, deleteFocusSession, saveReport, deleteReport, deleteNotification,
    markNotificationRead, addNotification, saveSettings, resetAll, persist
  }
}
