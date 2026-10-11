<script setup lang="ts">
import { useProductivityStore } from '~/stores/productivity'

const store = useProductivityStore()

const todayLabel = computed(() => new Date().toLocaleDateString('fa-IR', { weekday: 'long', day: 'numeric', month: 'long' }))
const studyMinutes = computed(() => store.todayFocusMinutes)
const goalMinutes = computed(() => store.settings.dailyStudyGoalMinutes)
const progressPct = computed(() => goalMinutes.value ? Math.min(100, Math.round((studyMinutes.value / goalMinutes.value) * 100)) : 0)
const mainGoal = computed(() => store.goals[0])
const todaySchedule = computed(() => store.schedule)

const insight = computed(() => {
  if (!store.focusLogs.length) return 'هنوز جلسه تمرکزی ثبت نکرده‌ای — یک جلسه کوتاه شروع کن تا اولین بینش واقعی ساخته شود.'
  return studyMinutes.value >= goalMinutes.value
    ? 'به هدف مطالعه امروز رسیدی — عالیه، همین ریتم را نگه دار.'
    : `${Math.max(0, goalMinutes.value - studyMinutes.value)} دقیقه دیگر تا رسیدن به هدف امروز مانده.`
})

const widgets = ref(['progress', 'study', 'tasks', 'goal', 'focus', 'schedule', 'insight'])
const hide = (x: string) => { widgets.value = widgets.value.filter(w => w !== x) }
const restore = () => { widgets.value = ['progress', 'study', 'tasks', 'goal', 'focus', 'schedule', 'insight'] }
</script>

<template>
<div class="space-y-6">
  <div class="flex flex-col md:flex-row md:items-end justify-between gap-3">
    <div>
      <p class="text-sm text-gray-500">{{ todayLabel }}</p>
      <h1 class="text-2xl lg:text-3xl font-extrabold mt-1">امروز چه خبر؟</h1>
      <p class="text-gray-500 mt-1">داشبوردت بر اساس داده‌های واقعی همین مرورگر ساخته می‌شود.</p>
    </div>
    <div class="flex gap-2">
      <UButton to="/today" icon="i-lucide-play">شروع روز</UButton>
      <UButton to="/tasks" variant="soft" icon="i-lucide-plus">وظیفه جدید</UButton>
    </div>
  </div>

  <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
    <UCard v-if="widgets.includes('progress')">
      <template #header><div class="flex justify-between"><span>پیشرفت مطالعه امروز</span><UButton variant="ghost" size="xs" icon="i-lucide-x" @click="hide('progress')" /></div></template>
      <div class="text-3xl font-extrabold">{{ progressPct }}%</div>
      <UProgress :model-value="progressPct" class="mt-3" />
      <p class="text-xs text-gray-500 mt-2">{{ Math.floor(studyMinutes / 60) }}س {{ studyMinutes % 60 }}د از {{ Math.floor(goalMinutes / 60) }}س {{ goalMinutes % 60 }}د</p>
    </UCard>
    <UCard v-if="widgets.includes('study')">
      <template #header>مطالعه امروز</template>
      <div class="text-3xl font-extrabold">{{ Math.floor(studyMinutes / 60) }}س {{ studyMinutes % 60 }}د</div>
      <p class="text-xs text-gray-500 mt-2">هدف روزانه در تنظیمات قابل تغییر است</p>
    </UCard>
    <UCard v-if="widgets.includes('tasks')">
      <template #header>وظایف</template>
      <div class="text-3xl font-extrabold">{{ store.completedTasksCount }} / {{ store.tasks.length }}</div>
      <p class="text-xs text-gray-500 mt-2">وظیفه انجام‌شده</p>
    </UCard>
    <UCard v-if="widgets.includes('goal') && mainGoal">
      <template #header>هدف اصلی</template>
      <div class="font-bold truncate">{{ mainGoal?.title }}</div>
      <div class="mt-3 flex items-center gap-3"><UProgress :model-value="mainGoal?.progress || 0" class="flex-1" /><span class="text-sm">{{ mainGoal?.progress }}%</span></div>
    </UCard>
  </div>

  <div class="grid xl:grid-cols-3 gap-4">
    <UCard v-if="widgets.includes('schedule')" class="xl:col-span-2">
      <template #header><div class="flex items-center justify-between"><span class="font-bold">برنامه امروز</span><UButton to="/planner" variant="link">مشاهده برنامه</UButton></div></template>
      <div v-if="todaySchedule.length" class="space-y-3">
        <div v-for="s in todaySchedule" :key="s.id" class="flex items-center gap-4 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/60">
          <span class="w-24 text-sm text-gray-500">{{ s.start }}–{{ s.end }}</span>
          <div class="flex-1 font-semibold">{{ s.title }}</div>
          <UBadge variant="soft">{{ s.kind }}</UBadge>
        </div>
      </div>
      <p v-else class="text-sm text-gray-500">هنوز بلوک زمانی‌ای در برنامه‌ریز نساخته‌ای.</p>
    </UCard>
    <UCard v-if="widgets.includes('insight')">
      <template #header>بینش امروز</template>
      <div class="space-y-4">
        <div class="p-4 rounded-2xl bg-primary-50 dark:bg-primary-950/30 text-sm leading-7">{{ insight }}</div>
        <UButton to="/analytics" block variant="soft">تحلیل عمیق‌تر</UButton>
      </div>
    </UCard>
  </div>

  <UCard v-if="widgets.includes('focus')">
    <template #header><div class="flex justify-between"><span>تمرکز ۷ روز اخیر</span><UButton to="/focus" variant="link">شروع تمرکز</UButton></div></template>
    <div class="flex items-end gap-2 h-32">
      <div v-for="(v, i) in store.weeklyFocusMinutes" :key="i" class="flex-1 flex flex-col items-center gap-2">
        <div class="w-full rounded-t-lg bg-primary-500/80" :style="{ height: `${Math.max(4, Math.min(120, v))}px` }" />
        <span class="text-xs text-gray-500">{{ v }}د</span>
      </div>
    </div>
  </UCard>
  <div class="flex justify-end"><UButton variant="ghost" size="sm" @click="restore">بازگردانی ویجت‌های داشبورد</UButton></div>
</div>
</template>
