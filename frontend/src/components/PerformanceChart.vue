<template>
  <div class="chart-container">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  label: {
    type: String,
    default: 'Performance'
  },
  color: {
    type: String,
    default: '#667eea'
  }
})

const chartData = computed(() => ({
  labels: props.data.map(point => {
    const date = new Date(point.timestamp)
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }),
  datasets: [
    {
      label: props.label,
      data: props.data.map(point => point.totalValue || point.price),
      borderColor: props.color,
      backgroundColor: `${props.color}20`,
      fill: true,
      tension: 0.4,
      pointRadius: 0,
      pointHoverRadius: 6,
      borderWidth: 2
    }
  ]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    intersect: false,
    mode: 'index'
  },
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: {
        size: 14
      },
      bodyFont: {
        size: 13
      },
      callbacks: {
        label: function(context) {
          return `${props.label}: $${context.parsed.y.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: false,
      ticks: {
        maxTicksLimit: 5,
        callback: function(value) {
          return '$' + value.toLocaleString()
        }
      },
      grid: {
        color: 'rgba(0, 0, 0, 0.05)',
        drawBorder: false
      }
    },
    x: {
      ticks: {
        maxTicksLimit: 6,
        autoSkip: true
      },
      grid: {
        display: false
      }
    }
  }
}
</script>

<style scoped>
.chart-container {
  height: 300px;
  width: 100%;
}
</style>
