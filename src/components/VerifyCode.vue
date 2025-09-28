<template>
  <div class="verify-code-container">
    <el-input
        v-model="inputCode"
        placeholder="请输入验证码"
        size="large"
        :prefix-icon="Lock"
        clearable
        @input="handleInput"
        maxlength="4"
    />
    <canvas 
        ref="canvasRef" 
        class="verify-code-canvas" 
        @click="generateCode"
        title="点击刷新验证码"
    ></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Lock } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const canvasRef = ref(null)
const inputCode = ref(props.modelValue)
const verifyCode = ref('')

// 生成随机数
const randomNum = (min, max) => {
  return Math.floor(Math.random() * (max - min) + min)
}

// 生成随机颜色
const randomColor = (min, max) => {
  const r = randomNum(min, max)
  const g = randomNum(min, max)
  const b = randomNum(min, max)
  return `rgb(${r}, ${g}, ${b})`
}

// 绘制验证码
const drawCode = () => {
  if (!canvasRef.value) return
  
  const ctx = canvasRef.value.getContext('2d')
  const width = canvasRef.value.width
  const height = canvasRef.value.height
  
  // 清空画布
  ctx.clearRect(0, 0, width, height)
  
  // 绘制背景色
  ctx.fillStyle = randomColor(180, 230)
  ctx.fillRect(0, 0, width, height)
  
  // 绘制文字
  const chars = verifyCode.value.split('')
  ctx.font = '20px Arial bold'
  ctx.textBaseline = 'middle'
  
  const textWidth = width / chars.length
  chars.forEach((char, index) => {
    ctx.fillStyle = randomColor(50, 160)
    const x = textWidth * index + textWidth / 2
    const y = height / 2
    ctx.fillText(char, x, y)
  })
  
  // 绘制干扰线
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = randomColor(100, 200)
    ctx.beginPath()
    ctx.moveTo(randomNum(0, width), randomNum(0, height))
    ctx.lineTo(randomNum(0, width), randomNum(0, height))
    ctx.stroke()
  }
  
  // 绘制干扰点
  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = randomColor(150, 200)
    ctx.beginPath()
    ctx.arc(randomNum(0, width), randomNum(0, height), 1, 0, 2 * Math.PI)
    ctx.fill()
  }
}

// 生成验证码
const generateCode = () => {
  let code = ''
  const chars = '0123456789'
  
  for (let i = 0; i < 4; i++) {
    code += chars[randomNum(0, chars.length)]
  }
  
  verifyCode.value = code
  drawCode()
}

// 处理输入
const handleInput = (value) => {
  emit('update:modelValue', value)
}

// 监听验证码变化
watch(verifyCode, () => {
  emit('update:modelValue', inputCode.value)
})

// 暴露方法给父组件
defineExpose({
  generateCode,
  verifyCode: () => {
    return inputCode.value.toLowerCase() === verifyCode.value.toLowerCase()
  },
  refreshCode: generateCode
})

onMounted(() => {
  generateCode()
})
</script>

<style scoped>
.verify-code-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.verify-code-canvas {
  width: 100px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
}
</style>