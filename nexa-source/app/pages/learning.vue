<script setup lang="ts">
import { useProductivityStore } from '~/stores/productivity'

const store = useProductivityStore()
</script>

<template>
<div class="space-y-5">
  <div><h1 class="text-2xl font-extrabold">یادگیری</h1><p class="text-gray-500">دوره → موضوع → درس → جلسه مطالعه</p></div>
  <div v-if="store.courses.length" class="grid lg:grid-cols-2 gap-4">
    <UCard v-for="c in store.courses" :key="c.id">
      <template #header>
        <div class="flex justify-between">
          <div><b>{{ c.title }}</b><p class="text-xs text-gray-500 mt-1">{{ c.description }}</p></div>
          <UBadge variant="soft">{{ c.progress }}%</UBadge>
        </div>
      </template>
      <UProgress :model-value="c.progress" />
      <div class="grid grid-cols-2 gap-3 mt-4 text-sm">
        <div>زمان واقعی<br><b>{{ Math.floor(c.actualMinutes / 60) }} ساعت</b></div>
        <div>هدف<br><b>{{ c.estimatedHours }} ساعت</b></div>
      </div>
      <div class="mt-4 space-y-2">
        <div v-for="t in c.topics" :key="t.id" class="flex items-center gap-3">
          <span class="flex-1 text-sm">{{ t.title }}</span>
          <UProgress :model-value="t.progress" class="w-28" />
          <span class="text-xs w-8">{{ t.progress }}%</span>
          <UButton :to="`/focus?courseId=${c.id}&topicId=${t.id}`" icon="i-lucide-play" size="xs" variant="ghost" />
        </div>
      </div>
      <UButton block class="mt-4" variant="soft" :to="`/focus?courseId=${c.id}`">شروع جلسه مطالعه</UButton>
    </UCard>
  </div>
  <p v-else class="text-sm text-gray-500">هنوز دوره‌ای اضافه نکرده‌ای.</p>
</div>
</template>
