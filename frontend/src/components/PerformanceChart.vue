<template>
  <div class="chart-wrapper">
    <div class="chart-controls">
      <div class="chart-info">
        <span class="info-text">💡 Zoom: Molette de souris | Déplacer: Maj + Glisser</span>
      </div>
    </div>
    <div class="chart-container">
      <Line :data="chartData" :options="chartOptions" />
    </div>
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
import zoomPlugin from 'chartjs-plugin-zoom'
import annotationPlugin from 'chartjs-plugin-annotation'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  zoomPlugin,
  annotationPlugin
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
  },
  selectedRange: {
    type: String,
    default: 'ALL'
  },
  showStartPriceLine: {
    type: Boolean,
    default: false
  },
  startPrice: {
    type: Number,
    default: null
  }
})

const isMultiYear = computed(() => {
  if (props.data.length < 2) return false
  
  const firstDate = new Date(props.data[0].timestamp)
  const lastDate = new Date(props.data[props.data.length - 1].timestamp)
  
  return lastDate.getFullYear() - firstDate.getFullYear() >= 1
})

const chartData = computed(() => ({
  labels: props.data.map(point => {
    const date = new Date(point.timestamp)
    if (isMultiYear.value) {
      return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short' })
    }
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
    },
    annotation: props.showStartPriceLine && props.startPrice ? {
      annotations: {
        startLine: {
          type: 'line',
          yMin: props.startPrice,
          yMax: props.startPrice,
          borderColor: '#f59e0b',
          borderWidth: 2,
          borderDash: [5, 5],
          label: {
            display: true,
            content: `Start: $${props.startPrice.toFixed(2)}`,
            position: 'end',
            backgroundColor: '#f59e0b',
            color: 'white',
            font: {
              size: 11,
              weight: 'bold'
            },
            padding: 4
          }
        }
      }
    } : {},
    zoom: {
      zoom: {
        wheel: {
          enabled: true,
          speed: 0.1
        },
        pinch: {
          enabled: true
        },
        mode: 'x'
      },
      pan: {
        enabled: true,
        mode: 'x',
        modifierKey: 'shift'
      },
      limits: {
        x: {
          min: 'original',
          max: 'original'
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
        maxTicksLimit: isMultiYear.value ? 8 : 6,
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
.chart-wrapper {
  width: 100%;
}

.chart-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.chart-info {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.info-text {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.chart-container {
  height: 400px;
  width: 100%;
}
</style>
