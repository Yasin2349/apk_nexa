<script setup lang="ts">
import { useProductivityStore } from '~/stores/productivity'

const route = useRoute()
const collapsed = ref(false)
const openMobile = ref(false)
const store = useProductivityStore()

const items = [
  { label: 'داشبورد', icon: 'i-lucide-layout-dashboard', to: '/' },
  { label: 'امروز', icon: 'i-lucide-sun', to: '/today' },
  { label: 'برنامه‌ریز', icon: 'i-lucide-calendar-days', to: '/planner' },
  { label: 'وظایف', icon: 'i-lucide-list-checks', to: '/tasks' },
  { label: 'یادگیری', icon: 'i-lucide-graduation-cap', to: '/learning' },
  { label: 'پروژه‌ها', icon: 'i-lucide-briefcase-business', to: '/projects' },
  { label: 'اهداف', icon: 'i-lucide-target', to: '/goals' },
  { label: 'تمرکز', icon: 'i-lucide-timer', to: '/focus' },
  { label: 'گزارش‌ها', icon: 'i-lucide-file-chart-column', to: '/reports' },
  { label: 'تحلیل‌ها', icon: 'i-lucide-chart-no-axes-combined', to: '/analytics' },
  { label: 'اعلان‌ها', icon: 'i-lucide-bell', to: '/notifications' },
  { label: 'تنظیمات', icon: 'i-lucide-settings', to: '/settings' }
]

const colorMode = useColorMode()
const dark = computed(() => colorMode.value === 'dark')
</script>

<template>
  <div dir="rtl" class="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100">
    <div class="flex min-h-screen">
      <aside
        :class="[collapsed ? 'w-[76px]' : 'w-64', 'hidden lg:flex flex-col border-l border-gray-200 dark:border-gray-800 bg-white/90 dark:bg-gray-900/90 backdrop-blur sticky top-0 h-screen transition-all']"
      >
        <div class="h-16 px-4 flex items-center gap-3 border-b border-gray-200 dark:border-gray-800">
          <div class="size-9 rounded-xl bg-primary-600 text-white grid place-items-center font-black">N</div>
          <span v-if="!collapsed" class="font-extrabold text-lg">Nexa</span>
          <button class="mr-auto p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800" @click="collapsed = !collapsed">
            <UIcon :name="collapsed ? 'i-lucide-panel-right-open' : 'i-lucide-panel-left-close'" />
          </button>
        </div>
        <nav class="p-3 space-y-1 flex-1 overflow-y-auto">
          <NuxtLink
            v-for="item in items"
            :key="item.to"
            :to="item.to"
            :title="item.label"
            :class="['flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition', route.path === item.to ? 'bg-primary-50 text-primary-700 dark:bg-primary-950/50 dark:text-primary-300 font-semibold' : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800']"
          >
            <UIcon :name="item.icon" class="size-5 shrink-0" />
            <span v-if="!collapsed">{{ item.label }}</span>
          </NuxtLink>
        </nav>
        <div v-if="!collapsed" class="m-3 p-3 rounded-2xl bg-gray-50 dark:bg-gray-800/60 text-xs text-gray-500">
          داده‌ها فقط در همین مرورگر (localStorage) ذخیره می‌شوند.
        </div>
      </aside>

      <UDrawer v-model:open="openMobile" direction="right">
        <template #content>
          <div class="w-72 p-4">
            <div class="font-extrabold text-lg mb-5">Nexa</div>
            <nav class="space-y-1">
              <NuxtLink
                v-for="item in items"
                :key="item.to"
                :to="item.to"
                class="flex gap-3 px-3 py-3 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-800"
                @click="openMobile = false"
              >
                <UIcon :name="item.icon" />{{ item.label }}
              </NuxtLink>
            </nav>
          </div>
        </template>
      </UDrawer>

      <main class="flex-1 min-w-0">
        <header class="h-16 border-b border-gray-200 dark:border-gray-800 bg-white/80 dark:bg-gray-900/80 backdrop-blur sticky top-0 z-30 px-4 lg:px-6 flex items-center gap-3">
          <button class="lg:hidden p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800" @click="openMobile = true">
            <UIcon name="i-lucide-menu" />
          </button>
          <div class="flex-1"><slot name="header" /></div>
          <ClientOnly>
            <UButton
              variant="ghost"
              :icon="dark ? 'i-lucide-sun' : 'i-lucide-moon'"
              @click="colorMode.preference = dark ? 'light' : 'dark'"
            />
            <template #fallback>
              <UButton variant="ghost" icon="i-lucide-moon" disabled />
            </template>
          </ClientOnly>
          <UButton v-if="store.activeTimer" to="/focus" variant="soft" icon="i-lucide-timer">تایمر فعال</UButton>
          <UChip :show="store.unreadNotifications > 0" :text="store.unreadNotifications" size="lg" color="error">
            <UButton to="/notifications" variant="ghost" icon="i-lucide-bell" />
          </UChip>
        </header>
        <div class="p-4 lg:p-6 max-w-[1600px] mx-auto"><slot /></div>
      </main>
    </div>
  </div>
</template>
