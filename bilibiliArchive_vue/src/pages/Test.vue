<template>
  <div ref="chartContainer" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Array,
    default: () => [7235, 2242, 542, 383, 198, 52, 49, 64, 53, 56, 43, 51, 47, 55, 37, 30, 42, 48, 32, 39, 30, 40, 39, 48, 17, 13, 35, 58, 24, 6, 6, 1, 5, 9, 4, 4, 4, 0, 16, 11, 8, 7, 3, 0, 0, 0, 2, 0, 2, 3, 2, 5, 1, 1, 1, 1, 3, 1, 7, 59, 11, 6, 7, 9, 5, 5, 0, 4, 1, 4, 0, 2, 0, 1, 0, 0, 1, 1, 0, 0, 3, 0, 0, 1, 0, 2, 9, 16, 2, 3, 2, 1, 0, 4, 5, 0, 0, 2, 1, 0, 1, 1, 1, 1, 3, 4, 1, 1, 1, 2, 0, 1, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1]
  }
});

const chartContainer = ref(null);
let chartInstance = null;

// 生成 x 轴数据（天数）
const generateXAxis = () => 
  Array.from({ length: props.data.length }, (_, i) => i+1);

// 初始化图表
const initChart = () => {
  if (!chartContainer.value) return;

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '第 {b} 天<br/>评论数: {c}'
    },
    xAxis: {
      type: 'category',
      data: generateXAxis(),
      name: '发布后天数',
      boundaryGap: false,
      axisLabel: {
        interval: Math.floor(props.data.length / 10) // 自动间隔标签
      }
    },
    yAxis: {
      type: 'value',
      name: '评论数量'
    },
    series: [{
      name: '评论数',
      type: 'line',
      data: props.data,
      smooth: false,
      areaStyle: {
        color: 'rgba(64, 158, 255, 0.4)'
      },
      itemStyle: {
        color: '#409EFF'
      },
      // emphasis: {
      //   focus: 'series'
      // }
    }],
    grid: {
      containLabel: true,
      left: '5%',
      right: '5%',
      bottom: '10%'
    }
  };

  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value);
  }
  chartInstance.setOption(option);
};

// 响应式调整
const handleResize = () => chartInstance?.resize();

// 生命周期
onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});

// 监听数据变化
watch(() => props.data, () => {
  initChart();
  handleResize();
}, { deep: true });
</script>

<style>
.chart-container {
  width: 100%;
  height: 500px;
}
</style>