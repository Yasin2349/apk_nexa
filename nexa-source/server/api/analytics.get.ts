import { z } from 'zod'

const Query = z.object({
  range: z.enum(['daily', 'weekly', 'monthly']).default('weekly')
})

export default defineEventHandler((event) => {
  const query = Query.parse(getQuery(event))

  return {
    range: query.range,
    metrics: {
      studyMinutes: 1420,
      taskCompletionRate: 82,
      planningAccuracy: 76,
      focusAverage: 42,
      consistency: 84,
      goalProgress: 42
    },
    insights: [
      { kind: 'observed', text: 'بیشترین زمان تمرکز در بازه صبح ثبت شده است.' },
      { kind: 'calculated', text: 'مدت واقعی وظایف به‌طور میانگین حدود ۲۰٪ بیشتر از تخمین است.' },
      { kind: 'recommendation', text: 'برای برنامه فردا ۲۰٪ حاشیه زمان برای وظایف مشابه در نظر بگیر.' }
    ]
  }
})
