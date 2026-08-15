<template>
  <div class="poll-box">
    <div class="poll-options">
      <div v-for="(opt, i) in options" :key="i" class="poll-opt">
        <el-button size="small" :type="myVote === i ? 'primary' : ''" :disabled="myVote !== null" @click="cast(i)">
          {{ i + 1 }}. {{ opt }}
        </el-button>
        <span class="poll-count">{{ counts[i] || 0 }} 票</span>
      </div>
    </div>
    <div class="poll-meta">
      <template v-if="myVote !== null">已投票，共 {{ totalVotes }} 票</template>
      <template v-else>尚未投票</template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  poll: { type: Object, required: true },
  votes: { type: Array, default: () => [] },
  userId: { type: String, default: '' }
})
const emit = defineEmits(['vote'])

const options = computed(() => {
  const text = props.poll && props.poll.content || ''
  const lines = text.split('\n')
  const parsed = []
  for (const line of lines) {
    const m = line.trim().match(/^\s*(\d+)\.\s+(.+)$/)
    if (m) parsed.push(m[2].trim())
  }
  return parsed
})

const myVotes = computed(() =>
  props.votes.filter(v => String(v.userId) === String(props.userId))
)

const myVote = computed(() => {
  const pollId = String(props.poll && props.poll.id)
  const v = myVotes.value.find(v => String(v.content).startsWith('VOTE:' + pollId + ':'))
  if (!v) return null
  const idx = Number(String(v.content).split(':')[2])
  return Number.isInteger(idx) && idx >= 0 && idx < options.value.length ? idx : null
})

const counts = computed(() => {
  const pollId = String(props.poll && props.poll.id)
  const arr = options.value.map(() => 0)
  for (const v of props.votes) {
    if (String(v.content).startsWith('VOTE:' + pollId + ':')) {
      const idx = Number(String(v.content).split(':')[2])
      if (Number.isInteger(idx) && idx >= 0 && idx < arr.length) arr[idx]++
    }
  }
  return arr
})

const totalVotes = computed(() => counts.value.reduce((a, b) => a + b, 0))

const cast = (idx) => {
  if (myVote.value !== null) return
  emit('vote', idx)
}
</script>

<style scoped>
.poll-box { margin-top:6px }
.poll-options { display:flex; flex-direction:column; gap:6px }
.poll-opt { display:flex; align-items:center; gap:8px }
.poll-count { font-size:11px; color:var(--ts) }
.poll-meta { font-size:11px; color:var(--ts); margin-top:6px }
</style>
