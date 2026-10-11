export type TaskStatus = 'todo' | 'in_progress' | 'completed' | 'cancelled' | 'deferred'
export type Priority = 'low' | 'medium' | 'high' | 'critical'
export type TimeCategory = 'study' | 'work' | 'programming' | 'project' | 'personal' | 'other'
export interface Task { id:string; title:string; description?:string; status:TaskStatus; priority:Priority; category:string; projectId?:string; goalId?:string; dueDate?:string; scheduledDate?:string; startTime?:string; endTime?:string; estimatedMinutes:number; actualMinutes:number; tags:string[]; createdAt:string; completedAt?:string }
export interface FocusSession { id:string; taskId?:string; projectId?:string; goalId?:string; courseId?:string; topicId?:string; label:string; mode:string; plannedMinutes:number; actualMinutes:number; startedAt:string; endedAt?:string; interruptions:number; focusRating?:number; distractions:string[]; note?:string }
export interface ActiveTimer { id:string; taskId?:string; label:string; mode:'focus'|'custom'|'schedule'; durationSeconds:number; startedAt:number; pausedAt?:number; elapsedBeforePause:number; status:'running'|'paused'; notifyOnFinish:boolean }
export interface CourseTopic { id:string; title:string; progress:number }
export interface Course { id:string; title:string; description:string; progress:number; estimatedHours:number; actualMinutes:number; targetDate?:string; topics:CourseTopic[] }
export interface Goal { id:string; title:string; description:string; progress:number; deadline?:string; priority:Priority; parentId?:string; createdAt:string }
export interface Project { id:string; title:string; description:string; status:'active'|'paused'|'completed'; priority:Priority; progress:number; deadline?:string; createdAt:string; goalId?:string }
export interface ScheduleBlock { id:string; title:string; kind:'task'|'focus'|'study'|'break'|'work'|'personal'; date:string; start:string; end:string; taskId?:string; completed:boolean }
export interface NotificationItem { id:string; type:'task'|'goal'|'focus'|'summary'|'system'; title:string; body:string; read:boolean; createdAt:string }
export interface DailyReport { date:string; achievement:string; problems:string; notes:string; energy:number; focus:number; rating:number; savedAt:string; generatedText?:string }
export interface ReportTemplate { id:string; name:string; lineCount:number; sections:string[]; includeStats:boolean; includeCompletedTasks:boolean; includeFocus:boolean; createdAt:string }
export interface AppSettings { language:'fa'|'en'; timezone:string; dailyStudyGoalMinutes:number; workStart:string; workEnd:string; quietHoursEnabled:boolean; quietStart:string; quietEnd:string; notificationsEnabled:boolean; scheduleReminderMinutes:number; motivationNotifications:boolean; timerPresets:number[]; customTimerDefault:number; autoAdvanceSchedule:boolean }
